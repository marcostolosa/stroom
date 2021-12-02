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

package stroom.ui.config.shared;

import stroom.util.shared.AbstractConfig;
import stroom.util.shared.validation.ValidRegex;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.Objects;
import javax.validation.constraints.Pattern;

@JsonPropertyOrder(alphabetic = true)
@JsonInclude(Include.NON_NULL)
public class UiConfig extends AbstractConfig {

    @JsonProperty
    @JsonPropertyDescription("The welcome message that is displayed in the welcome tab when logging in to Stroom. " +
            "The welcome message is in HTML format.")
    private final String welcomeHtml;

    @JsonProperty
    @JsonPropertyDescription("The about message that is displayed when selecting Help -> About. " +
            "The about message is in HTML format.")
    private final String aboutHtml;

    @JsonProperty
    @JsonPropertyDescription("Provide a warning message to users about an outage or other significant event.")
    private final String maintenanceMessage;

    @JsonProperty
    @JsonPropertyDescription("The default maximum number of search results to return to the dashboard, unless the " +
            "user requests lower values.")
    private final String defaultMaxResults;

    @JsonProperty
    private final ProcessConfig process;

    @JsonProperty
    @JsonPropertyDescription("The URL of hosted help files.")
    private final String helpUrl;

    @JsonProperty
    private final ThemeConfig theme;

    @JsonProperty
    private final QueryConfig query;

    @ValidRegex
    @JsonProperty
    @JsonPropertyDescription("The regex pattern for entity names.")
    private final String namePattern;

    @JsonProperty
    @JsonPropertyDescription("The title to use for the application in the browser.")
    private final String htmlTitle;

    @Pattern(regexp = "^return (true|false);$")
    @JsonProperty
    @JsonPropertyDescription("Determines the behaviour of the browser built-in context menu. This property is " +
            "for developer use only. Set to 'return false;' to see Stroom's context menu. Set to 'return true;' " +
            "to see the standard " +
            "browser menu.")
    private final String oncontextmenu;

    @JsonProperty
    private final SplashConfig splash;

    @JsonProperty
    private final ActivityConfig activity;

    @JsonProperty
    private final UiPreferences uiPreferences;

    @JsonProperty
    private final SourceConfig source;

    @JsonProperty
    @JsonPropertyDescription("The Stroom GWT UI is now wrapped in a new React UI that provides some additional " +
            "features. To use the React UI the GWT UI must be wrapped in an IFrame which is hosted at the root URL. " +
            "If a user navigates to the GWT UI directly via `stroom/ui` then the React additions will not function. " +
            "When this property is set to true that will be prevented as the user will be redirected back to the " +
            "root URL. This behaviour is configurable as development of the GWT UI still requires direct access via " +
            "`stroom/ui`")
    private final Boolean requireReactWrapper;

    public UiConfig() {
        welcomeHtml = "<h1>About Stroom</h1><p>Stroom is designed to receive data from multiple systems.</p>";
        aboutHtml = "<h1>About Stroom</h1><p>Stroom is designed to receive data from multiple systems.</p>";
        maintenanceMessage = null;
        defaultMaxResults = "1000000,100,10,1";
        process = new ProcessConfig();
        helpUrl = "https://gchq.github.io/stroom-docs";
        theme = new ThemeConfig();
        query = new QueryConfig();
        namePattern = "^[a-zA-Z0-9_\\- \\.\\(\\)]{1,}$";
        htmlTitle = "Stroom";
        oncontextmenu = "return false;";
        splash = new SplashConfig();
        activity = new ActivityConfig();
        uiPreferences = new UiPreferences();
        source = new SourceConfig();
        requireReactWrapper = true;
    }

    @JsonCreator
    public UiConfig(@JsonProperty("welcomeHtml") final String welcomeHtml,
                    @JsonProperty("aboutHtml") final String aboutHtml,
                    @JsonProperty("maintenanceMessage") final String maintenanceMessage,
                    @JsonProperty("defaultMaxResults") final String defaultMaxResults,
                    @JsonProperty("process") final ProcessConfig process,
                    @JsonProperty("helpUrl") final String helpUrl,
                    @JsonProperty("theme") final ThemeConfig theme,
                    @JsonProperty("query") final QueryConfig query,
                    @JsonProperty("namePattern") @ValidRegex final String namePattern,
                    @JsonProperty("htmlTitle") final String htmlTitle,
                    @JsonProperty("oncontextmenu") final String oncontextmenu,
                    @JsonProperty("splash") final SplashConfig splash,
                    @JsonProperty("activity") final ActivityConfig activity,
                    @JsonProperty("uiPreferences") final UiPreferences uiPreferences,
                    @JsonProperty("source") final SourceConfig source,
                    @JsonProperty("requireReactWrapper") Boolean requireReactWrapper) {
        this.welcomeHtml = welcomeHtml;
        this.aboutHtml = aboutHtml;
        this.maintenanceMessage = maintenanceMessage;
        this.defaultMaxResults = defaultMaxResults;
        this.process = process;
        this.helpUrl = helpUrl;
        this.theme = theme;
        this.query = query;
        this.namePattern = namePattern;
        this.htmlTitle = htmlTitle;
        this.oncontextmenu = oncontextmenu;
        this.splash = splash;
        this.activity = activity;
        this.uiPreferences = uiPreferences;
        this.source = source;
        this.requireReactWrapper = requireReactWrapper;
    }

    public String getWelcomeHtml() {
        return welcomeHtml;
    }

    public String getAboutHtml() {
        return aboutHtml;
    }

    public String getMaintenanceMessage() {
        return maintenanceMessage;
    }

    public String getDefaultMaxResults() {
        return defaultMaxResults;
    }

    public ProcessConfig getProcess() {
        return process;
    }

    public String getHelpUrl() {
        return helpUrl;
    }

    public ThemeConfig getTheme() {
        return theme;
    }

    public QueryConfig getQuery() {
        return query;
    }

    public String getNamePattern() {
        return namePattern;
    }

    public SplashConfig getSplash() {
        return splash;
    }

    public ActivityConfig getActivity() {
        return activity;
    }

    public String getHtmlTitle() {
        return htmlTitle;
    }

    public String getOncontextmenu() {
        return oncontextmenu;
    }

    public UiPreferences getUiPreferences() {
        return uiPreferences;
    }

    public SourceConfig getSource() {
        return source;
    }

    public Boolean getRequireReactWrapper() {
        return requireReactWrapper;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final UiConfig uiConfig = (UiConfig) o;
        return Objects.equals(welcomeHtml, uiConfig.welcomeHtml)
                && Objects.equals(aboutHtml, uiConfig.aboutHtml)
                && Objects.equals(maintenanceMessage, uiConfig.maintenanceMessage)
                && Objects.equals(defaultMaxResults, uiConfig.defaultMaxResults)
                && Objects.equals(process, uiConfig.process)
                && Objects.equals(helpUrl, uiConfig.helpUrl)
                && Objects.equals(theme, uiConfig.theme)
                && Objects.equals(query, uiConfig.query)
                && Objects.equals(namePattern, uiConfig.namePattern)
                && Objects.equals(htmlTitle, uiConfig.htmlTitle)
                && Objects.equals(oncontextmenu, uiConfig.oncontextmenu)
                && Objects.equals(splash, uiConfig.splash)
                && Objects.equals(activity, uiConfig.activity)
                && Objects.equals(uiPreferences, uiConfig.uiPreferences)
                && Objects.equals(source, uiConfig.source)
                && Objects.equals(requireReactWrapper, uiConfig.requireReactWrapper);
    }

    @Override
    public int hashCode() {
        return Objects.hash(welcomeHtml,
                aboutHtml,
                maintenanceMessage,
                defaultMaxResults,
                process,
                helpUrl,
                theme,
                query,
                namePattern,
                htmlTitle,
                oncontextmenu,
                splash,
                activity,
                uiPreferences,
                source,
                requireReactWrapper);
    }

    @Override
    public String toString() {
        return "UiConfig{" +
                "welcomeHtml='" + welcomeHtml + '\'' +
                ", aboutHtml='" + aboutHtml + '\'' +
                ", maintenanceMessage='" + maintenanceMessage + '\'' +
                ", defaultMaxResults='" + defaultMaxResults + '\'' +
                ", process=" + process +
                ", helpUrl='" + helpUrl + '\'' +
                ", theme=" + theme +
                ", query=" + query +
                ", namePattern='" + namePattern + '\'' +
                ", htmlTitle='" + htmlTitle + '\'' +
                ", oncontextmenu='" + oncontextmenu + '\'' +
                ", splash=" + splash +
                ", activity=" + activity +
                ", uiPreferences=" + uiPreferences +
                ", source=" + source +
                ", requireReactWrapper=" + requireReactWrapper +
                '}';
    }
}
