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

package stroom.node.shared;

import stroom.util.shared.SharedObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.StreamSupport;

public class ClientProperties implements SharedObject {
    public static final String HTML_TITLE = "stroom.htmlTitle";
    public static final String LOGIN_HTML = "stroom.loginHTML";
    public static final String WELCOME_HTML = "stroom.welcomeHTML";
    public static final String ABOUT_HTML = "stroom.aboutHTML";
    public static final String BUILD_DATE = "buildDate";
    public static final String BUILD_VERSION = "buildVersion";
    public static final String NODE_NAME = "stroom.node";
    public static final String UP_DATE = "upDate";
    public static final String MAINTENANCE_MESSAGE = "stroom.maintenance.message";
    //TODO this one should be stroom.dashboard
    public static final String DEFAULT_MAX_RESULTS = "stroom.dashboard.defaultMaxResults";
    public static final String PROCESS_TIME_LIMIT = "stroom.search.process.defaultTimeLimit";
    public static final String PROCESS_RECORD_LIMIT = "stroom.search.process.defaultRecordLimit";
    public static final String STATISTIC_ENGINES = "stroom.statistics.common.statisticEngines";
    public static final String NAME_PATTERN = "stroom.namePattern";
    public static final String LABEL_COLOURS = "stroom.theme.labelColours";
    public static final String HELP_URL = "stroom.helpUrl";
    public static final String URL_LIST = "stroom.url.list";
    public static final String URL_BASE = "stroom.url.";
    public static final String URL_KIBANA_UI = URL_BASE + "kibana-ui";
    public static final String URL_ELASTIC_EXPLORER = URL_BASE + "elastic-explorer";
    public static final String URL_ELASTIC_QUERY_UI = URL_BASE + "elastic-query-ui";
    public static final String URL_ANNOTATIONS_QUERY_UI = URL_BASE + "annotations-query-ui";
    public static final String URL_ANNOTATIONS_SERVICE = URL_BASE + "annotations-service";
    public static final String URL_DASHBOARD = URL_BASE + "annotations-dashboard";
    public static final String AUTHENTICATION_SERVICE_URL = "stroom.auth.authentication.service.url";

    public static final String USERS_UI_URL = "stroom.users.ui.url";
    public static final String API_KEYS_UI_URL = "stroom.apikeys.ui.url";
    public static final String CHANGE_PASSWORD_UI_URL = "stroom.changepassword.url";



    private static final long serialVersionUID = 8717922468620533698L;
    private HashMap<String, String> map;

    public ClientProperties() {
        map = new HashMap<>();
    }

    public void put(final String key, final String value) {
        map.put(key, value);
    }

    public String get(final String key) {
        return map.get(key);
    }

    public long getLong(final String key, final long defaultValue) {
        final String value = map.get(key);
        if (value != null) {
            try {
                return Long.valueOf(value);
            } catch (final Exception e) {
            }
        }

        return defaultValue;
    }

    public Map<String, String> getLookupTable(final String listProp, final String base) {
        final Map<String, String> result = new HashMap<>();

        final String keyList = get(listProp);
        if (null != keyList) {
            final String[] keys = keyList.split(",");
            for (final String key : keys) {
                final String value = get(base + key);
                result.put(key, value);
            }
        }

        return result;
    }

    /**
     * This method exists to stop a developer or IDE from making the map final as GWT requires access to it
     */
    @SuppressWarnings("unused")
    private void setMap(final HashMap<String, String> map) {
        this.map = map;
    }
}
