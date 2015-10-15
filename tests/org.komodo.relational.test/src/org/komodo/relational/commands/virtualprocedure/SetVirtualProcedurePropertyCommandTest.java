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
package org.komodo.relational.commands.virtualprocedure;

import java.io.File;
import java.io.FileWriter;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.komodo.relational.AbstractCommandTest;
import org.komodo.relational.model.Model;
import org.komodo.relational.model.Procedure;
import org.komodo.relational.model.VirtualProcedure;
import org.komodo.relational.vdb.Vdb;
import org.komodo.relational.workspace.WorkspaceManager;
import org.komodo.shell.api.CommandResult;

/**
 * Test Class to test SetVirtualProcedurePropertyCommand
 *
 */
@SuppressWarnings("javadoc")
public class SetVirtualProcedurePropertyCommandTest extends AbstractCommandTest {

    /**
	 * Test for SetVirtualProcedurePropertyCommand
	 */
	public SetVirtualProcedurePropertyCommandTest( ) {
		super();
	}

    @Test
    public void testSetProperty1() throws Exception {
        File cmdFile = File.createTempFile("TestCommand", ".txt");  //$NON-NLS-1$  //$NON-NLS-2$
        cmdFile.deleteOnExit();
        
        FileWriter writer = new FileWriter(cmdFile);
        writer.write("workspace" + NEW_LINE);  //$NON-NLS-1$
        writer.write("create-vdb myVdb vdbPath" + NEW_LINE);  //$NON-NLS-1$
        writer.write("cd myVdb" + NEW_LINE);  //$NON-NLS-1$
        writer.write("add-model myModel " + NEW_LINE);  //$NON-NLS-1$
        writer.write("cd myModel" + NEW_LINE);  //$NON-NLS-1$
        writer.write("add-virtual-procedure myVirtualProcedure" + NEW_LINE);  //$NON-NLS-1$
        writer.write("cd myVirtualProcedure" + NEW_LINE);  //$NON-NLS-1$
        writer.write("set-property name-in-source myNameInSource" + NEW_LINE);  //$NON-NLS-1$
        writer.close();

        setup(cmdFile.getAbsolutePath(), SetVirtualProcedurePropertyCommand.class);

        CommandResult result = execute();
        assertCommandResultOk(result);

        WorkspaceManager wkspMgr = WorkspaceManager.getInstance(_repo);
        Vdb[] vdbs = wkspMgr.findVdbs(uow);
        
        assertEquals(vdbs.length,1);
        
        Model[] models = vdbs[0].getModels(uow);
        assertEquals(1, models.length);
        assertEquals("myModel", models[0].getName(uow)); //$NON-NLS-1$
        
        Procedure[] procs = models[0].getProcedures(uow);
        assertEquals(1, procs.length);
        assertEquals(true, procs[0] instanceof VirtualProcedure);
        assertEquals("myVirtualProcedure", procs[0].getName(uow)); //$NON-NLS-1$
        
        assertEquals("myNameInSource", ((VirtualProcedure)procs[0]).getNameInSource(uow)); //$NON-NLS-1$
    }

}
