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

package stroom.entity.server.util;

import stroom.util.logging.StroomLogger;
import org.springframework.jdbc.datasource.ConnectionHandle;
import org.springframework.orm.jpa.vendor.HibernateJpaDialect;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import java.sql.SQLException;
import java.util.ArrayDeque;

/**
 * TODO: Not sure we need this anymore
 */
public class StroomHibernateJpaVendorAdapter extends HibernateJpaVendorAdapter {
    private static final StroomLogger LOGGER = StroomLogger.getLogger(StroomHibernateJpaVendorAdapter.class);

    static ThreadLocal<ArrayDeque<StackTraceElement[]>> threadTransactionStack = ThreadLocal.withInitial(ArrayDeque::new);

    public static class StroomHibernateJpaDialect extends HibernateJpaDialect {
        private static final long serialVersionUID = 1L;

        public ConnectionHandle getJdbcConnection(javax.persistence.EntityManager entityManager, boolean readOnly)
                throws javax.persistence.PersistenceException, java.sql.SQLException {
            ConnectionHandle connectionHandle = super.getJdbcConnection(entityManager, readOnly);
            connectionHandle.getConnection().setReadOnly(readOnly);
            return connectionHandle;
        }

        @Override
        public Object beginTransaction(EntityManager entityManager, TransactionDefinition definition)
                throws PersistenceException, SQLException, TransactionException {
            final ArrayDeque<StackTraceElement[]> stack = threadTransactionStack.get();
            stack.push(Thread.currentThread().getStackTrace());

            if (stack.size() > 2) {
                StringBuilder trace = new StringBuilder();
                int t = 0;
                for (StackTraceElement[] frame : stack) {
                    t++;
                    for (int i = 0; i < frame.length; i++) {
                        trace.append(t);
                        trace.append(" ");
                        trace.append(frame[i].toString());
                        trace.append("\n");
                    }
                }

                LOGGER.warn("beginTransaction() - \n%s", trace);

            }

            return super.beginTransaction(entityManager, definition);
        }

        @Override
        public void cleanupTransaction(Object transactionData) {
            threadTransactionStack.get().pop();
            super.cleanupTransaction(transactionData);
        }
    }

    private HibernateJpaDialect jpaDialect = new StroomHibernateJpaDialect();

    @Override
    public HibernateJpaDialect getJpaDialect() {
        return jpaDialect;
    }
}
