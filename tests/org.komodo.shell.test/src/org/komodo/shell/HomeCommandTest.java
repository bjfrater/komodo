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
package org.komodo.shell;

import static org.junit.Assert.assertEquals;
import java.io.File;
import java.io.FileWriter;
import org.junit.Test;
import org.komodo.shell.api.CommandResult;
import org.komodo.shell.commands.HomeCommand;
import org.komodo.shell.util.KomodoObjectUtils;

/**
 * Test Class to test HomeCommand
 *
 */
@SuppressWarnings({"javadoc"})
public class HomeCommandTest extends AbstractCommandTest {

	/**
	 * Test for HomeCommand
	 */
	public HomeCommandTest( ) {
		super();
	}

    @Test
    public void test1() throws Exception {
        File cmdFile = File.createTempFile("TestCommand", ".txt");  //$NON-NLS-1$  //$NON-NLS-2$
        cmdFile.deleteOnExit();
        
        FileWriter writer = new FileWriter(cmdFile);
        writer.write("workspace" + NEW_LINE);  //$NON-NLS-1$
        writer.write("home" + NEW_LINE);  //$NON-NLS-1$
        writer.close();
        
    	setup(cmdFile.getAbsolutePath(), HomeCommand.class);

        CommandResult result = execute();
        assertCommandResultOk(result);

        String contextPath = KomodoObjectUtils.getFullName(wsStatus, wsStatus.getCurrentContext());
        assertEquals("/", contextPath); //$NON-NLS-1$
    }

}
