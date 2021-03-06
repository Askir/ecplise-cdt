/*******************************************************************************
 * Copyright (c) 2005, 2014 Symbian Software Limited
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Symbian Software Limited - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.dom;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class Messages {
	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(Messages.class.getName());

	private Messages() {
	}

	public static String getString(String key) {
		try {
			return RESOURCE_BUNDLE.getString(key);
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}
}
