/*******************************************************************************
 * Copyright (c) 2018 Manish Khurana , Nathan Ridge and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.cdt.lsp.core;

import java.net.URI;

/*
 * Encapsulates functionality specific to a particular C++ language server (e.g., CQuery)
 */
public interface ICPPLanguageServer {

	public Object getLSSpecificInitializationOptions(Object defaultInitOptions, URI rootPath);

}
