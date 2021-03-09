/*
 * Copyright 2018 Crown Copyright
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

package stroom.pipeline.xsltfunctions;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;

public abstract class AbstractXsltFunctionModule extends AbstractModule {
    private  Multibinder<StroomExtensionFunctionDefinition> multibinder;

    @Override
    protected void configure() {
        multibinder = Multibinder.newSetBinder(binder(), StroomExtensionFunctionDefinition.class);
        configureFunctions();
    }

    /**
     * Override this method to call {@link #bindElement}.
     */
    protected abstract void configureFunctions();

    protected <T extends StroomExtensionFunctionDefinition> void bindFunction(final Class<T> elementClass) {
        multibinder.addBinding().to(elementClass);
    }
}
