/*
 * Copyright 2017 Crown Copyright
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package stroom.dictionary.impl;

import stroom.dictionary.api.WordListProvider;
import stroom.dictionary.shared.DictionaryDoc;
import stroom.docref.DocRef;
import stroom.docref.DocRefInfo;
import stroom.docstore.api.AuditFieldFilter;
import stroom.docstore.api.DependencyRemapper;
import stroom.docstore.api.DocumentSerialiser2;
import stroom.docstore.api.Serialiser2Factory;
import stroom.docstore.api.Store;
import stroom.docstore.api.StoreFactory;
import stroom.docstore.api.UniqueNameUtil;
import stroom.explorer.shared.DocumentType;
import stroom.importexport.shared.ImportState;
import stroom.importexport.shared.ImportState.ImportMode;
import stroom.security.api.SecurityContext;
import stroom.util.shared.Message;
import stroom.util.shared.Severity;
import stroom.util.string.EncodingUtil;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

@Singleton
class DictionaryStoreImpl implements DictionaryStore, WordListProvider {
    private final Store<DictionaryDoc> store;
    private final SecurityContext securityContext;
    private final DocumentSerialiser2<DictionaryDoc> serialiser;
    private final DocumentSerialiser2<OldDictionaryDoc> oldSerialiser;

    @Inject
    DictionaryStoreImpl(final StoreFactory storeFactory,
                        final SecurityContext securityContext,
                        final DictionarySerialiser serialiser,
                        final Serialiser2Factory serialiser2Factory) {
        this.store = storeFactory.createStore(serialiser, DictionaryDoc.ENTITY_TYPE, DictionaryDoc.class);
        this.securityContext = securityContext;
        this.serialiser = serialiser;
        this.oldSerialiser = serialiser2Factory.createSerialiser(OldDictionaryDoc.class);
    }

    ////////////////////////////////////////////////////////////////////////
    // START OF ExplorerActionHandler
    ////////////////////////////////////////////////////////////////////////

    @Override
    public DocRef createDocument(final String name) {
        return store.createDocument(name);
    }

    @Override
    public DocRef copyDocument(final DocRef docRef, final Set<String> existingNames) {
        final String newName = UniqueNameUtil.getCopyName(docRef.getName(), existingNames);
        return store.copyDocument(docRef.getUuid(), newName);
    }

    @Override
    public DocRef moveDocument(final String uuid) {
        return store.moveDocument(uuid);
    }

    @Override
    public DocRef renameDocument(final String uuid, final String name) {
        return store.renameDocument(uuid, name);
    }

    @Override
    public void deleteDocument(final String uuid) {
        store.deleteDocument(uuid);
    }

    @Override
    public DocRefInfo info(String uuid) {
        return store.info(uuid);
    }

    @Override
    public DocumentType getDocumentType() {
        return new DocumentType(9, DictionaryDoc.ENTITY_TYPE, DictionaryDoc.ENTITY_TYPE);
    }

    ////////////////////////////////////////////////////////////////////////
    // END OF ExplorerActionHandler
    ////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////
    // START OF HasDependencies
    ////////////////////////////////////////////////////////////////////////

    @Override
    public Map<DocRef, Set<DocRef>> getDependencies() {
        return store.getDependencies(createMapper());
    }

    @Override
    public Set<DocRef> getDependencies(final DocRef docRef) {
        return store.getDependencies(docRef, createMapper());
    }

    @Override
    public void remapDependencies(final DocRef docRef,
                                  final Map<DocRef, DocRef> remappings) {
        store.remapDependencies(docRef, remappings, createMapper());
    }

    private BiConsumer<DictionaryDoc, DependencyRemapper> createMapper() {
        return (doc, dependencyRemapper) -> {
            if (doc.getImports() != null) {
                final List<DocRef> replacedDocRefImports = doc
                        .getImports()
                        .stream()
                        .map(dependencyRemapper::remap)
                        .collect(Collectors.toList());
                doc.setImports(replacedDocRefImports);
            }
        };
    }

    ////////////////////////////////////////////////////////////////////////
    // END OF HasDependencies
    ////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////
    // START OF DocumentActionHandler
    ////////////////////////////////////////////////////////////////////////

    @Override
    public DictionaryDoc readDocument(final DocRef docRef) {
        return store.readDocument(docRef);
    }

    @Override
    public DictionaryDoc writeDocument(final DictionaryDoc document) {
        return store.writeDocument(document);
    }

    ////////////////////////////////////////////////////////////////////////
    // END OF DocumentActionHandler
    ////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////
    // START OF ImportExportActionHandler
    ////////////////////////////////////////////////////////////////////////

    @Override
    public Set<DocRef> listDocuments() {
        return store.listDocuments();
    }

    @Override
    public ImpexDetails importDocument(final DocRef docRef, final Map<String, byte[]> dataMap, final ImportState importState, final ImportMode importMode) {
        // Convert legacy import format to the new format.
        final Map<String, byte[]> map = convert(docRef, dataMap, importState, importMode);
        if (map != null) {
            return store.importDocument(docRef, map, importState, importMode);
        }

        return new ImpexDetails(docRef);
    }

    @Override
    public Map<String, byte[]> exportDocument(final DocRef docRef, final boolean omitAuditFields, final List<Message> messageList) {
        if (omitAuditFields) {
            return store.exportDocument(docRef, messageList, new AuditFieldFilter<>());
        }
        return store.exportDocument(docRef, messageList, d -> d);
    }

    private Map<String, byte[]> convert(final DocRef docRef, final Map<String, byte[]> dataMap, final ImportState importState, final ImportMode importMode) {
        Map<String, byte[]> result = dataMap;

        try {
            if (!dataMap.containsKey("meta")) {
                // The latest version has a 'meta' file for the core details about the dictionary so convert this data.

                if (dataMap.containsKey("dat")) {
                    // Version 6.0 stored the whole dictionary in a single JSON file ending in 'dat' so convert this.
                    dataMap.put("meta", dataMap.remove("dat"));
                    final OldDictionaryDoc oldDocument = oldSerialiser.read(dataMap);

                    final DictionaryDoc document = new DictionaryDoc();
                    document.setVersion(oldDocument.getVersion());
                    document.setCreateTimeMs(oldDocument.getCreateTimeMs());
                    document.setUpdateTimeMs(oldDocument.getUpdateTimeMs());
                    document.setCreateUser(oldDocument.getCreateUser());
                    document.setUpdateUser(oldDocument.getUpdateUser());
                    document.setType(oldDocument.getType());
                    document.setUuid(oldDocument.getUuid());
                    document.setName(oldDocument.getName());
                    document.setDescription(oldDocument.getDescription());
                    document.setImports(oldDocument.getImports());
                    document.setData(oldDocument.getData());

                    result = serialiser.write(document);
                } else {
                    // If we don't have a 'dat' file then this version is pre 6.0. We need to create the dictionary meta and put the data in the map.

                    final boolean exists = store.exists(docRef);
                    DictionaryDoc document;
                    if (exists) {
                        document = readDocument(docRef);

                    } else {
                        final long now = System.currentTimeMillis();
                        final String userId = securityContext.getUserId();

                        document = new DictionaryDoc();
                        document.setType(docRef.getType());
                        document.setUuid(docRef.getUuid());
                        document.setName(docRef.getName());
                        document.setVersion(UUID.randomUUID().toString());
                        document.setCreateTimeMs(now);
                        document.setUpdateTimeMs(now);
                        document.setCreateUser(userId);
                        document.setUpdateUser(userId);
                    }

                    if (dataMap.containsKey("data.xml")) {
                        document.setData(EncodingUtil.asString(dataMap.get("data.xml")));
                    }

                    result = serialiser.write(document);
                }
            }
        } catch (final IOException | RuntimeException e) {
            importState.addMessage(Severity.ERROR, e.getMessage());
            result = null;
        }

        return result;
    }

    @Override
    public String getType() {
        return DictionaryDoc.ENTITY_TYPE;
    }

    @Override
    public Set<DocRef> findAssociatedNonExplorerDocRefs(DocRef docRef) {
        return null;
    }

    ////////////////////////////////////////////////////////////////////////
    // END OF ImportExportActionHandler
    ////////////////////////////////////////////////////////////////////////

    @Override
    public List<DocRef> findByName(final String name) {
        return store.findByName(name);
    }

    @Override
    public List<DocRef> list() {
        return store.list();
    }

    @Override
    public String getCombinedData(final DocRef docRef) {
        return doGetCombinedData(docRef, new HashSet<>());
    }

    @Override
    public String[] getWords(final DocRef dictionaryRef) {
//            return wordMap.computeIfAbsent(docRef, k -> {
        final String words = getCombinedData(dictionaryRef);
        if (words != null) {
            return words.trim().split("\n");
        }

        return null;
//            });
    }

    private String doGetCombinedData(final DocRef docRef, final Set<DocRef> visited) {
        final DictionaryDoc doc = readDocument(docRef);
        if (doc != null && !visited.contains(docRef)) {
            // Prevent circular dependencies.
            visited.add(docRef);

            final StringBuilder sb = new StringBuilder();
            if (doc.getImports() != null) {
                for (final DocRef ref : doc.getImports()) {
                    final String data = doGetCombinedData(ref, visited);
                    if (data != null && !data.isEmpty()) {
                        if (sb.length() > 0) {
                            sb.append("\n");
                        }
                        sb.append(data);
                    }
                }
            }
            if (doc.getData() != null) {
                if (sb.length() > 0) {
                    sb.append("\n");
                }
                sb.append(doc.getData());
            }
            return sb.toString();
        }
        return null;
    }
}
