/*
 * Copyright 2020 Crown Copyright
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

package stroom.event.logging.rs.impl;

import stroom.util.shared.HasId;
import stroom.util.shared.HasName;
import stroom.util.shared.HasUuid;

import javax.ws.rs.core.MultivaluedMap;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import static stroom.event.logging.rs.impl.RestResourceAutoLoggerImpl.LOGGER;

class RequestInfo {

    private final ContainerResourceInfo containerResourceInfo;
    private final Object requestObj;


    public RequestInfo(final ContainerResourceInfo containerResourceInfo) {
        this.containerResourceInfo = containerResourceInfo;
        this.requestObj = findRequestObj();
    }

    public RequestInfo(final ContainerResourceInfo containerResourceInfo, Object requestObj) {
        this.containerResourceInfo = containerResourceInfo;
        if (requestObj == null) {
            requestObj = findRequestObj();
        }
        this.requestObj = requestObj;
    }

    public Object getRequestObj() {
        return requestObj;
    }

    public ContainerResourceInfo getContainerResourceInfo() {
        return containerResourceInfo;
    }

    public boolean shouldLog(boolean logByDefault) {
        return getContainerResourceInfo().shouldLog(logByDefault);
    }


    private Object findRequestObj() {
        int numberOfPathParms = containerResourceInfo.getRequestContext().getUriInfo().getPathParameters(false).keySet().size();
        int numberOfQueryParams = containerResourceInfo.getRequestContext().getUriInfo().getQueryParameters(false).keySet().size();
        int numberOfPathAndQueryParms = numberOfPathParms + numberOfQueryParams;

        if (numberOfPathAndQueryParms == 0) {
            return null;
        }

        if (numberOfPathAndQueryParms > 1) {
            WithParameters obj = new WithParameters(containerResourceInfo.getRequestContext().getUriInfo().getPathParameters(false));
            obj.addParams(containerResourceInfo.getRequestContext().getUriInfo().getQueryParameters(false));
            return obj;
        } else {
            final MultivaluedMap<String, String> paramMap;
            if (numberOfPathParms == 1) {
                paramMap = containerResourceInfo.getRequestContext().getUriInfo().getPathParameters(false);
            } else {
                paramMap = containerResourceInfo.getRequestContext().getUriInfo().getQueryParameters(false);
            }
            String paramName = paramMap.keySet().stream().findFirst().get();
            String paramValue = String.join(", ", paramMap.get(paramName));
            if ("id".equals(paramName)) {
                return new ObjectId(paramValue);
            } else if ("uuid".equals(paramName)) {
                return new ObjectUuid(paramValue);
            } else {
                WithParameters obj = new WithParameters(containerResourceInfo.getRequestContext().getUriInfo().getPathParameters(false));
                obj.addParams(containerResourceInfo.getRequestContext().getUriInfo().getQueryParameters(false));
                return obj;
            }
        }

    }

    private static class ObjectId implements HasId {

        private final long id;

        public ObjectId(String val) {
            long id = 0;
            try {
                id = Long.parseLong(val);
            } catch (NumberFormatException ex) {
                LOGGER.error("Unable to log id of entity with non-numeric id " + val);
            } finally {
                this.id = id;
            }
        }

        @Override
        public long getId() {
            return id;
        }
    }

    private static class ObjectUuid implements HasUuid {

        private final String uuid;

        public ObjectUuid(String uuid) {
            this.uuid = uuid;
        }

        @Override
        public String getUuid() {
            return uuid;
        }
    }

    private static class WithParameters implements HasName {

        private String name;

        public WithParameters(MultivaluedMap<String, String> origParms) {
            Set<Entry<String, String>> parms = createParms(origParms);

            name = parms.stream()
                    .map(e ->
                            e.getKey() + " = " + e.getValue())
                    .collect(Collectors.joining(", "));
        }

        private Set<Entry<String, String>> createParms(MultivaluedMap<String, String> origParms) {
            return origParms.keySet().stream().map(k -> {
                return new Entry<String, String>() {
                    @Override
                    public String getKey() {
                        return k;
                    }

                    @Override
                    public String getValue() {
                        return origParms.get(k)
                                .stream()
                                .collect(Collectors.joining(", "));
                    }

                    @Override
                    public String setValue(final String value) {
                        return null;
                    }
                };
            }).collect(Collectors.toSet());
        }

        public void addParams(MultivaluedMap<String, String> origParms) {
            name = name.length() > 0 ? name + ", " : "" +
                    createParms(origParms).stream()
                            .map(e ->
                                    e.getKey() + " = " + e.getValue())
                            .collect(Collectors.joining(", "));
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public void setName(final String name) {
            this.name = name;
        }
    }
}
