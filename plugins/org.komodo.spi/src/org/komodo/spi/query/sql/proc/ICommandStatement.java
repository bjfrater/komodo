/*************************************************************************************
 * Copyright (c) 2014 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.komodo.spi.query.sql.proc;

import org.komodo.spi.query.sql.ILanguageVisitor;
import org.komodo.spi.query.sql.lang.ICommand;
import org.komodo.spi.query.sql.lang.ISubqueryContainer;


/**
 *
 */
public interface ICommandStatement <LV extends ILanguageVisitor, C extends ICommand>
    extends IStatement<LV>, ISubqueryContainer<C> {

}