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
 */

import * as React from "react";
import ConfirmPasswordResetEmail from "./ConfirmPasswordResetEmail";
import * as queryString from "query-string";
import useRouter from "../../../lib/useRouter";

export const ConfirmPasswordResetEmailContainer: React.FunctionComponent<any> =
  () => {
    let redirectUri: string;

    const { router } = useRouter();
    if (!!router && !!router.location) {
      const query = queryString.parse(router.location.search);
      if (query.redirect_uri) {
        redirectUri = query.redirect_uri + "";
      }
    }

    return (
      <ConfirmPasswordResetEmail
        onBack={() => {
          window.location.href = redirectUri;
        }}
      />
    );
  };
