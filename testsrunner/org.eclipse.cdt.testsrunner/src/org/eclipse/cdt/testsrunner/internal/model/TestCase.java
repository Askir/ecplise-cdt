/*******************************************************************************
 * Copyright (c) 2011, 2012 Anton Gorenkov 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Anton Gorenkov - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.testsrunner.internal.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.testsrunner.model.IModelVisitor;
import org.eclipse.cdt.testsrunner.model.ITestCase;

/**
 * Represents the test case (test) of the test hierarchy.
 */
public class TestCase extends TestItem implements ITestCase {

	/** Test case status (failed, passed, ...). */
	private Status status;

	/** Test case execution time. */
	private int testingTime;

	/**
	 * The messages that were generated by testing framework during test
	 * running.
	 */
	private List<TestMessage> testMessages = new ArrayList<TestMessage>();

	
	public TestCase(String name, TestSuite parent) {
		super(name, parent);
		reset();
	}
	
	@Override
	public Status getStatus() {
		return status;
	}

	@Override
	public int getTestingTime() {
		return testingTime;
	}

	@Override
	public TestMessage[] getTestMessages() {
		return testMessages.toArray(new TestMessage[testMessages.size()]);
	}
	
	@Override
	public void visit(IModelVisitor visitor) {
		visitor.visit(this);
		for (TestMessage message : testMessages) {
			message.visit(visitor);
		}
		visitor.leave(this);
	}

	/**
	 * Resets the test case to the default state.
	 */
	public void reset() {
		status = Status.Skipped;
		testingTime = 0;
		testMessages.clear();
	}

	/**
	 * Modifies the status of the test case.
	 * 
	 * @param status new test status
	 */
	public void setStatus(Status status) {
		this.status = status;
	}

	/**
	 * Modifies the execution time of the test case.
	 * 
	 * @param testingTime new test execution time
	 */
	public void setTestingTime(int testingTime) {
		this.testingTime = testingTime;
	}

	/**
	 * Adds a new test message to the test case.
	 * 
	 * @param testMessage message
	 */
	public void addTestMessage(TestMessage testMessage) {
		testMessages.add(testMessage);
	}

}
