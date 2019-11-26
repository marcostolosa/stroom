/*
 * Copyright 2016 Crown Copyright
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
 */

package stroom.cache.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.xml.sax.ErrorHandler;
import stroom.entity.server.DocumentPermissionCache;
import stroom.entity.server.event.EntityEvent;
import stroom.entity.server.event.EntityEventHandler;
import stroom.node.server.StroomPropertyService;
import stroom.pipeline.server.DefaultLocationFactory;
import stroom.pipeline.server.LocationFactory;
import stroom.pipeline.server.errorhandler.ErrorHandlerAdaptor;
import stroom.pipeline.server.errorhandler.StoredErrorReceiver;
import stroom.pipeline.shared.TextConverter;
import stroom.pipeline.shared.TextConverter.TextConverterType;
import stroom.pool.AbstractEntityPool;
import stroom.security.Insecure;
import stroom.security.SecurityContext;
import stroom.util.cache.CacheManager;
import stroom.util.io.StreamUtil;
import stroom.util.shared.Severity;
import stroom.xml.converter.ParserFactory;
import stroom.xml.converter.xmlfragment.XMLFragmentParserFactory;
import stroom.xmlschema.shared.XMLSchema;

import javax.inject.Inject;

@Insecure
@Component
@EntityEventHandler(type = XMLSchema.ENTITY_TYPE)
class ParserFactoryPoolImpl
        extends AbstractEntityPool<TextConverter, StoredParserFactory>
        implements ParserFactoryPool, EntityEvent.Handler {
    private static final Logger LOGGER = LoggerFactory.getLogger(ParserFactoryPool.class);

    private static final String MAXIMUM_SIZE_PROPERTY = "stroom.pipeline.parser.maxPoolSize";
    private static final long DEFAULT_MAXIMUM_SIZE = 1000;

    private final DSChooser dsChooser;

    @Inject
    ParserFactoryPoolImpl(final CacheManager cacheManager,
                          final StroomPropertyService stroomPropertyService,
                          final DocumentPermissionCache documentPermissionCache,
                          final SecurityContext securityContext,
                          final DSChooser dsChooser) {
        super(cacheManager, "Parser Factory Pool", stroomPropertyService.getLongProperty(MAXIMUM_SIZE_PROPERTY, DEFAULT_MAXIMUM_SIZE), documentPermissionCache, securityContext);
        this.dsChooser = dsChooser;
    }

    @Override
    protected StoredParserFactory createValue(final TextConverter textConverter) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Creating parser factory: " + textConverter.toString());
        }

        final StoredErrorReceiver errorReceiver = new StoredErrorReceiver();
        final LocationFactory locationFactory = new DefaultLocationFactory();
        final ErrorHandler errorHandler = new ErrorHandlerAdaptor(getClass().getSimpleName(), locationFactory,
                errorReceiver);
        ParserFactory parserFactory = null;

        try {
            if (TextConverterType.DATA_SPLITTER.equals(textConverter.getConverterType())) {
                parserFactory = dsChooser.configure(textConverter.getData(), errorHandler);

                // } else if
                // (textConverter.getConverterType().equals(TextConverterType.JAVA_CC))
                // {
                // final JavaCCParserFactory javaCCParserFactory =
                // JavaCCParserFactory
                // .create(StreamUtil.stringToStream(textConverter.getData()),
                // errorHandler);
                //
                // parserFactory = javaCCParserFactory;

            } else if (textConverter.getConverterType().equals(TextConverterType.XML_FRAGMENT)) {
                parserFactory = XMLFragmentParserFactory.create(StreamUtil.stringToStream(textConverter.getData()), errorHandler);

            } else {
                parserFactory = dsChooser.configure(textConverter.getData(), errorHandler);

//                    final String message = "Unknown text converter type: " + textConverter.getConverterType().toString();
//                    throw new ProcessException(message);
            }

        } catch (final Throwable e) {
            LOGGER.debug(e.getMessage(), e);
            errorReceiver.log(Severity.FATAL_ERROR, null, getClass().getSimpleName(), e.getMessage(), e);
        }

        return new StoredParserFactory(parserFactory, errorReceiver);
    }

    @Override
    public void onChange(final EntityEvent event) {
        clear();
    }
}
