/*
 * Copyright 2017 Crown Copyright
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package stroom.dashboard.expression.v1;

@FunctionDef(
        name = False.NAME,
        commonCategory = FunctionCategory.VALUE,
        commonReturnType = ValBoolean.class,
        commonReturnDescription = "",
        signatures = @FunctionSignature(
                description = "",
                args = {
//                        @FunctionArg(
//                                name = "",
//                                description = "",
//                                argType = .class)
                }))
class False extends AbstractStaticFunction {
    static final String NAME = "false";

    public False(final String name) {
        super(name, ValBoolean.FALSE);
    }
}
