/*******************************************************************************
 * Copyright (c) 2009, 2012 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.db;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.ASTTypeUtil;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ISemanticProblem;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateArgument;
import org.eclipse.cdt.internal.core.dom.parser.ISerializableEvaluation;
import org.eclipse.cdt.internal.core.dom.parser.ISerializableType;
import org.eclipse.cdt.internal.core.dom.parser.ITypeMarshalBuffer;
import org.eclipse.cdt.internal.core.dom.parser.ProblemType;
import org.eclipse.cdt.internal.core.dom.parser.Value;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPTemplateNonTypeArgument;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPTemplateTypeArgument;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPEvaluation;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMBinding;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMLinkage;
import org.eclipse.core.runtime.CoreException;

/**
 * For marshalling types to byte arrays.
 */
public class TypeMarshalBuffer implements ITypeMarshalBuffer {
	public static final byte[] EMPTY= { 0, 0, 0, 0, 0, 0 };
	public static final byte NULL_TYPE= 0;
	public static final byte INDIRECT_TYPE= (byte) -1;
	public static final byte BINDING_TYPE= (byte) -2;
	public static final byte UNSTORABLE_TYPE= (byte) -3;

	public static final IType UNSTORABLE_TYPE_PROBLEM = new ProblemType(ISemanticProblem.TYPE_NOT_PERSISTED);

	static {
		assert EMPTY.length == Database.TYPE_SIZE;
	}

	private final PDOMLinkage fLinkage;
	private int fPos;
	private byte[] fBuffer;

	/**
	 * Constructor for output buffer.
	 */
	public TypeMarshalBuffer(PDOMLinkage linkage) {
		fLinkage= linkage;
	}

	/**
	 * Constructor for input buffer.
	 */
	public TypeMarshalBuffer(PDOMLinkage linkage, byte[] data) {
		fLinkage= linkage;
		fBuffer= data;
	}

	public int getPosition() {
		return fPos;
	}

	public byte[] getBuffer() {
		return fBuffer;
	}

	@Override
	public void marshalBinding(IBinding binding) throws CoreException {
		if (binding instanceof ISerializableType) {
			((ISerializableType) binding).marshal(this);
		} else if (binding == null) {
			putByte(NULL_TYPE);
		} else {
			PDOMBinding pb= fLinkage.addTypeBinding(binding);
			if (pb == null) {
				putByte(UNSTORABLE_TYPE);
			} else {
				putByte(BINDING_TYPE);
				putByte((byte) 0);
				putRecordPointer(pb.getRecord());
			}
		}
	}

	@Override
	public IBinding unmarshalBinding() throws CoreException {
		if (fPos >= fBuffer.length)
			throw unmarshallingError();

		byte firstByte= fBuffer[fPos];
		if (firstByte == BINDING_TYPE) {
			fPos += 2;
			long rec= getRecordPointer();
			return (IBinding) fLinkage.getNode(rec);
		} else if (firstByte == NULL_TYPE || firstByte == UNSTORABLE_TYPE) {
			fPos++;
			return null;
		}

		return fLinkage.unmarshalBinding(this);
	}

	@Override
	public void marshalType(IType type) throws CoreException {
		if (type instanceof ISerializableType) {
			((ISerializableType) type).marshal(this);
		} else if (type == null) {
			putByte(NULL_TYPE);
		} else if (type instanceof IBinding) {
			marshalBinding((IBinding) type);
		} else {
			assert false : "Cannot serialize " + ASTTypeUtil.getType(type) + " (" + type.getClass().getName() + ")";   //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			putByte(UNSTORABLE_TYPE);
		}
	}

	@Override
	public IType unmarshalType() throws CoreException {
		if (fPos >= fBuffer.length)
			throw unmarshallingError();

		byte firstByte= fBuffer[fPos];
		if (firstByte == BINDING_TYPE) {
			fPos += 2;
			long rec= getRecordPointer();
			return (IType) fLinkage.getNode(rec);
		} else if (firstByte == NULL_TYPE) {
			fPos++;
			return null;
		} else if (firstByte == UNSTORABLE_TYPE) {
			fPos++;
			return UNSTORABLE_TYPE_PROBLEM;
		}

		return fLinkage.unmarshalType(this);
	}

	@Override
	public void marshalEvaluation(ISerializableEvaluation eval, boolean includeValues) throws CoreException {
		if (eval == null) {
			putByte(NULL_TYPE);
		} else {
			eval.marshal(this, includeValues);
		}
	}

	@Override
	public ISerializableEvaluation unmarshalEvaluation() throws CoreException {
		if (fPos >= fBuffer.length)
			throw unmarshallingError();

		byte firstByte= fBuffer[fPos];
		if (firstByte == NULL_TYPE) {
			fPos++;
			return null;
		}
		return fLinkage.unmarshalEvaluation(this);
	}

	@Override
	public void marshalValue(IValue value) throws CoreException {
		if (value instanceof Value) {
			((Value) value).marshall(this);
		} else {
			putByte(NULL_TYPE);
		}
	}

	@Override
	public IValue unmarshalValue() throws CoreException {
		if (fPos >= fBuffer.length)
			throw unmarshallingError();

		return Value.unmarshal(this);
	}

	@Override
	public void marshalTemplateArgument(ICPPTemplateArgument arg) throws CoreException {
		if (arg.isNonTypeValue()) {
			putByte(VALUE);
			arg.getNonTypeEvaluation().marshal(this, true);
		} else {
			marshalType(arg.getTypeValue());
			marshalType(arg.getOriginalTypeValue());
		}
	}

	@Override
	public ICPPTemplateArgument unmarshalTemplateArgument() throws CoreException {
		int firstByte= getByte();
		if (firstByte == VALUE) {
			return new CPPTemplateNonTypeArgument((ICPPEvaluation) unmarshalEvaluation(), null);
		} else {
			fPos--;
			IType type = unmarshalType();
			IType originalType = unmarshalType();
			return new CPPTemplateTypeArgument(type, originalType);
		}
	}

	private void request(int i) {
		if (fBuffer == null) {
			if (i <= Database.TYPE_SIZE) {
				fBuffer= new byte[Database.TYPE_SIZE];
			} else {
				fBuffer= new byte[i];
			}
		} else {
			final int bufLen = fBuffer.length;
			int needLen = fPos + i;
			if (needLen > bufLen) {
				needLen= Math.max(needLen, 2 * bufLen);
				byte[] newBuffer= new byte[needLen];
				System.arraycopy(fBuffer, 0, newBuffer, 0, fPos);
				fBuffer= newBuffer;
			}
		}
	}

	@Override
	public void putByte(byte b) {
		request(1);
		fBuffer[fPos++]= b;
	}

	@Override
	public int getByte() throws CoreException {
		if (fPos + 1 > fBuffer.length)
			throw unmarshallingError();
		return 0xff & fBuffer[fPos++];
	}

	@Override
	public CoreException unmarshallingError() {
		return new CoreException(CCorePlugin.createStatus("Unmarshalling error")); //$NON-NLS-1$
	}

	public CoreException marshallingError() {
		return new CoreException(CCorePlugin.createStatus("Marshalling error")); //$NON-NLS-1$
	}

	@Override
	public void putShort(short value) {
		request(2);
		fBuffer[fPos++]= (byte) (value >> 8);
		fBuffer[fPos++]= (byte) (value);
	}

	@Override
	public int getShort() throws CoreException {
		if (fPos + 2 > fBuffer.length)
			throw unmarshallingError();
		final int byte1 = 0xff & fBuffer[fPos++];
		final int byte2 = 0xff & fBuffer[fPos++];
		return (((byte1 << 8) | (byte2 & 0xff)));
	}

	@Override
	public void putInt(int value) {
		request(4);
		fPos += 4;
		int p= fPos;
		fBuffer[--p]= (byte) (value); value >>= 8;
		fBuffer[--p]= (byte) (value); value >>= 8;
		fBuffer[--p]= (byte) (value); value >>= 8;
		fBuffer[--p]= (byte) (value);
	}

	@Override
	public int getInt() throws CoreException {
		if (fPos + 4 > fBuffer.length)
			throw unmarshallingError();
		int result= 0;
		result |= fBuffer[fPos++] & 0xff; result <<= 8;
		result |= fBuffer[fPos++] & 0xff; result <<= 8;
		result |= fBuffer[fPos++] & 0xff; result <<= 8;
		result |= fBuffer[fPos++] & 0xff;
		return result;
	}

	@Override
	public void putLong(long value) {
		request(8);
		fPos += 8;
		int p= fPos;
		fBuffer[--p]= (byte) (value); value >>= 8;
		fBuffer[--p]= (byte) (value); value >>= 8;
		fBuffer[--p]= (byte) (value); value >>= 8;
		fBuffer[--p]= (byte) (value); value >>= 8;
		fBuffer[--p]= (byte) (value); value >>= 8;
		fBuffer[--p]= (byte) (value); value >>= 8;
		fBuffer[--p]= (byte) (value); value >>= 8;
		fBuffer[--p]= (byte) (value);
	}

	@Override
	public long getLong() throws CoreException {
		if (fPos + 8 > fBuffer.length)
			throw unmarshallingError();
		long result= 0;
		result |= fBuffer[fPos++] & 0xff; result <<= 8;
		result |= fBuffer[fPos++] & 0xff; result <<= 8;
		result |= fBuffer[fPos++] & 0xff; result <<= 8;
		result |= fBuffer[fPos++] & 0xff; result <<= 8;
		result |= fBuffer[fPos++] & 0xff; result <<= 8;
		result |= fBuffer[fPos++] & 0xff; result <<= 8;
		result |= fBuffer[fPos++] & 0xff; result <<= 8;
		result |= fBuffer[fPos++] & 0xff;
		return result;
	}

	private void putRecordPointer(long record) {
		request(Database.PTR_SIZE);
		Chunk.putRecPtr(record, fBuffer, fPos);
		fPos += Database.PTR_SIZE;
	}

	private long getRecordPointer() throws CoreException {
		final int pos= fPos;
		fPos += Database.PTR_SIZE;
		if (fPos > fBuffer.length) {
			fPos= fBuffer.length;
			throw unmarshallingError();
		}
		return Chunk.getRecPtr(fBuffer, pos);
	}

	@Override
	public void putCharArray(char[] chars) {
		putShort((short) chars.length);
		for (char c : chars) {
			putShort((short) c);
		}
	}

	@Override
	public char[] getCharArray() throws CoreException {
		int len= getShort();
		char[] expr= new char[len];
		for (int i = 0; i < expr.length; i++) {
			expr[i]= (char) getShort();
		}
		return expr;
	}
}
