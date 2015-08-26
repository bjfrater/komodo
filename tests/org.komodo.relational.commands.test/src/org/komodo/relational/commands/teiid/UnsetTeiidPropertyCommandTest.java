/*
 * Copyright 2014 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.komodo.relational.commands.teiid;

import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.komodo.relational.commands.AbstractCommandTest;
import org.komodo.relational.teiid.Teiid;
import org.komodo.relational.workspace.WorkspaceManager;
import org.komodo.shell.api.CommandResult;

/**
 * Test Class to test UnsetTeiidPropertyCommand
 *
 */
@SuppressWarnings( {"javadoc", "nls"} )
public class UnsetTeiidPropertyCommandTest extends AbstractCommandTest {

    @Test
    public void testUnsetProperty1() throws Exception {
        final String[] commands = { 
            "workspace",
            "create-teiid testTeiid",
            "cd testTeiid",
            "set-property adminPort 88",
            "unset-property adminPort" };

        setup( commands  );

        CommandResult result = execute();
        assertCommandResultOk(result);

        WorkspaceManager wkspMgr = WorkspaceManager.getInstance(_repo);
        Teiid[] teiids = wkspMgr.findTeiids(getTransaction());

        assertEquals(1, teiids.length);
        assertEquals("testTeiid", teiids[0].getName(getTransaction())); //$NON-NLS-1$

        assertEquals(9999, teiids[0].getAdminPort(getTransaction()));
    }

}