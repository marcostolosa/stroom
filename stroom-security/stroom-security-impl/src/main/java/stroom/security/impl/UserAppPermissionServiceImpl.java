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

package stroom.security.impl;

import stroom.security.shared.PermissionNames;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
class UserAppPermissionServiceImpl implements UserAppPermissionService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserAppPermissionServiceImpl.class);

    private static final Set<String> ALL_PERMISSIONS = Set.of(PermissionNames.PERMISSIONS);
    private final AppPermissionDao appPermissionDao;

    @Inject
    UserAppPermissionServiceImpl(final AppPermissionDao appPermissionDao) {
        this.appPermissionDao = appPermissionDao;
    }

    @Override
    public Set<String> getPermissionNamesForUser(final String userUuid) {
        return appPermissionDao.getPermissionsForUser(userUuid);
    }

    @Override
    public Set<String> getAllPermissionNames() {
        return ALL_PERMISSIONS;
    }

    @Override
    public Set<String> getPermissionNamesForUserName(String userName) {
        return appPermissionDao.getPermissionsForUserName(userName);
    }

    @Override
    public void addPermission(final String userUuid, final String permission) {
        validatePermissionName(permission);
        try {
            appPermissionDao.addPermission(userUuid, permission);
        } catch (final RuntimeException e) {
            LOGGER.error("addPermission()", e);
            throw e;
        }
    }

    @Override
    public void removePermission(final String userUuid, final String permission) {
        validatePermissionName(permission);
        try {
            appPermissionDao.removePermission(userUuid, permission);
        } catch (final RuntimeException e) {
            LOGGER.error("removePermission()", e);
            throw e;
        }
    }

    private void validatePermissionName(final String permission) {
        if (!ALL_PERMISSIONS.contains(permission)) {
            throw new RuntimeException(permission + " is not a valid permission name");
        }
    }
}
