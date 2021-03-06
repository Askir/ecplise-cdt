/*******************************************************************************
 *  Copyright (c) 2006, 2013 IBM Corporation and others.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.upc.ast;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.upc.ast.IUPCASTEnumerationSpecifier;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTEnumerationSpecifier;

@SuppressWarnings("restriction")
public class UPCASTEnumerationSpecifier extends CASTEnumerationSpecifier implements IUPCASTEnumerationSpecifier {

	private int referenceType;
	private int sharedQualifier;
	private IASTExpression blockSizeExpression;

	public UPCASTEnumerationSpecifier() {
	}

	public UPCASTEnumerationSpecifier(IASTName name) {
		super(name);
	}

	public UPCASTEnumerationSpecifier(IASTName name, IASTExpression blockSizeExpression) {
		super(name);
		setBlockSizeExpression(blockSizeExpression);
	}

	@Override
	public UPCASTEnumerationSpecifier copy() {
		return copy(CopyStyle.withoutLocations);
	}

	@Override
	public UPCASTEnumerationSpecifier copy(CopyStyle style) {
		UPCASTEnumerationSpecifier copy = new UPCASTEnumerationSpecifier();
		copy.referenceType = referenceType;
		copy.sharedQualifier = sharedQualifier;
		copy.setBlockSizeExpression(blockSizeExpression == null ? null : blockSizeExpression.copy(style));
		return copy(copy, style);
	}

	@Override
	public IASTExpression getBlockSizeExpression() {
		return blockSizeExpression;
	}

	@Override
	public int getReferenceType() {
		return referenceType;
	}

	@Override
	public int getSharedQualifier() {
		return sharedQualifier;
	}

	@Override
	public void setBlockSizeExpression(IASTExpression expr) {
		this.blockSizeExpression = expr;
		if (expr != null) {
			expr.setParent(this);
			expr.setPropertyInParent(BLOCK_SIZE_EXPRESSION);
		}
	}

	@Override
	public void setReferenceType(int referenceType) {
		this.referenceType = referenceType;
	}

	@Override
	public void setSharedQualifier(int shared) {
		this.sharedQualifier = shared;
	}

	@Override
	public boolean accept(ASTVisitor action) {
		if (action.shouldVisitDeclSpecifiers) {
			switch (action.visit(this)) {
			case ASTVisitor.PROCESS_ABORT:
				return false;
			case ASTVisitor.PROCESS_SKIP:
				return true;
			default:
				break;
			}
		}
		if (getName() != null)
			if (!getName().accept(action))
				return false;
		if (blockSizeExpression != null)
			if (!blockSizeExpression.accept(action))
				return false;

		IASTEnumerator[] etors = getEnumerators();
		for (int i = 0; i < etors.length; i++) {
			if (!etors[i].accept(action))
				return false;
		}
		if (action.shouldVisitDeclSpecifiers) {
			switch (action.leave(this)) {
			case ASTVisitor.PROCESS_ABORT:
				return false;
			case ASTVisitor.PROCESS_SKIP:
				return true;
			default:
				break;
			}
		}
		return true;
	}

}
