/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 

package org.eclipse.cdt.ui.tests.callhierarchy;

import java.io.IOException;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.resources.IFile;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

import org.eclipse.cdt.ui.tests.BaseTestCase;

import org.eclipse.cdt.internal.ui.editor.CEditor;


public class BasicCallHierarchyTest extends CallHierarchyBaseTest {
	
	public BasicCallHierarchyTest(String name) {
		super(name);
	}

	public static Test suite() {
		TestSuite suite= new TestSuite("BasicCallHierarchyTest");
		suite.addTestSuite(BasicCallHierarchyTest.class);
		suite.addTest(getFailingTests());
		return suite;
	}

	private static Test getFailingTests() {
		TestSuite suite= new TestSuite("Failing Tests");
        suite.addTest(getFailingTest("_testAnonymousEnumeratorC"));
        suite.addTest(getFailingTest("_testAnonymousEnumeratorCpp"));
        suite.addTest(getFailingTest("_testAnonymousStructMembersC"));
        suite.addTest(getFailingTest("_testAnonymousStructMembersCpp"));
        suite.addTest(getFailingTest("_testAnonymousUnionMembersC"));
        suite.addTest(getFailingTest("_testAnonymousUnionMembersCpp"));
        return suite;
	}

	private static Test getFailingTest(String name) {
		BaseTestCase failingTest= new BasicCallHierarchyTest(name);
		failingTest.setExpectFailure(0);
		return failingTest;
	}

	public void testFunctionsC() throws Exception {
		doTestFunctions("functions.c");
	}

	public void testFunctionsCpp() throws Exception {
		doTestFunctions("functions.cpp");
	}

	// {testFunctions}
	// void proto();
	// void func() {
	// };
	// void main() {
	//    proto(); //ref
	//    func(); //ref
	// };
	private void doTestFunctions(String filename) throws IOException, Exception, PartInitException {
		String content = readTaggedComment("testFunctions");
		IFile file= createFile(getProject(), filename, content);
		waitForIndexer(fPdom, file, 1000);
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		CEditor editor= (CEditor) IDE.openEditor(page, file);

		editor.selectAndReveal(content.indexOf("proto"), 5);
		openCallHierarchy(editor);
		Tree tree = getCHTree(page);
		checkTreeNode(tree, 0, "proto()");
		checkTreeNode(tree, 0, 0, "main()");

		editor.selectAndReveal(content.indexOf("func"), 2);
		openCallHierarchy(editor);
		checkTreeNode(tree, 0, "func()");
		checkTreeNode(tree, 0, 0, "main()");

		editor.selectAndReveal(content.indexOf("proto(); //ref"), 0);
		openCallHierarchy(editor);
		tree = getCHTree(page);
		checkTreeNode(tree, 0, "proto()");
		checkTreeNode(tree, 0, 0, "main()");

		editor.selectAndReveal(content.indexOf("func(); //ref"), 7);
		openCallHierarchy(editor);
		tree = getCHTree(page);
		checkTreeNode(tree, 0, "func()");
		checkTreeNode(tree, 0, 0, "main()");
	}

	public void testVariablesC() throws Exception {
		doTestVariables("variables.c");
	}
	
	public void testVariablesCpp() throws Exception {
		doTestVariables("variables.cpp");
	}
	
	// {testVariables}
	// extern int extern_var;
	// int global_var= 0;
	// void main() {
	//    int i= extern_var; //ref
	//    i= global_var; //ref
	// };
	private void doTestVariables(String filename) throws Exception {
		String content = readTaggedComment("testVariables");
		IFile file= createFile(getProject(), filename, content);
		waitForIndexer(fPdom, file, 1000);
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		CEditor editor= (CEditor) IDE.openEditor(page, file);

		editor.selectAndReveal(content.indexOf("extern_var"), 0);
		openCallHierarchy(editor);
		Tree tree = getCHTree(page);
		checkTreeNode(tree, 0, "extern_var");
		checkTreeNode(tree, 0, 0, "main()");

		editor.selectAndReveal(content.indexOf("global_var"), 2);
		openCallHierarchy(editor);
		checkTreeNode(tree, 0, "global_var");
		checkTreeNode(tree, 0, 0, "main()");

		editor.selectAndReveal(content.indexOf("extern_var; //ref"), 0);
		openCallHierarchy(editor);
		tree = getCHTree(page);
		checkTreeNode(tree, 0, "extern_var");
		checkTreeNode(tree, 0, 0, "main()");

		editor.selectAndReveal(content.indexOf("global_var; //ref"), 7);
		openCallHierarchy(editor);
		tree = getCHTree(page);
		checkTreeNode(tree, 0, "global_var");
		checkTreeNode(tree, 0, 0, "main()");
	}

	public void testEnumeratorC() throws Exception {
		doTestEnumerator("enumerator.c", "testEnumerator");
	}
	
	public void testEnumeratorCpp() throws Exception {
		doTestEnumerator("enumerator.cpp", "testEnumerator");
	}

	public void _testAnonymousEnumeratorC() throws Exception {
		doTestEnumerator("enumerator.c", "testAnonymousEnumerator");
	}
	
	public void _testAnonymousEnumeratorCpp() throws Exception {
		doTestEnumerator("enumerator.cpp", "testAnonymousEnumerator");
	}

	// {testEnumerator}
	// enum Enum {enumerator=12};
	// void main() {
	//    int i= enumerator; //ref
	// };

	// {testAnonymousEnumerator}
	// enum {enumerator};
	// void main() {
	//    int i= enumerator; //ref
	// };
	private void doTestEnumerator(String filename, String contentTag) throws Exception {
		String content = readTaggedComment(contentTag);
		IFile file= createFile(getProject(), filename, content);
		waitForIndexer(fPdom, file, 1000);
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		CEditor editor= (CEditor) IDE.openEditor(page, file);

		editor.selectAndReveal(content.indexOf("enumerator"), 0);
		openCallHierarchy(editor);
		Tree tree = getCHTree(page);
		checkTreeNode(tree, 0, "enumerator");
		checkTreeNode(tree, 0, 0, "main()");

		editor.selectAndReveal(content.indexOf("main"), 2);
		openCallHierarchy(editor);
		checkTreeNode(tree, 0, "main()");

		editor.selectAndReveal(content.indexOf("enumerator; //ref"), 0);
		openCallHierarchy(editor);
		tree = getCHTree(page);
		checkTreeNode(tree, 0, "enumerator");
		checkTreeNode(tree, 0, 0, "main()");

		editor.selectAndReveal(content.indexOf("main"), 2);
		openCallHierarchy(editor);
		checkTreeNode(tree, 0, "main()");
	}
	
	public void testStructMembersC() throws Exception {
		doTestStructMembers("struct_member.c");
	}
	
	public void testStructMembersCpp() throws Exception {
		doTestStructMembers("struct_member.cpp");
	}
	
	// {testStructMembers}
	// struct s1 {
	//    int mem1;
	// };
	// typedef struct s2 {
	//    int mem2;
	// } t2;
	// typedef struct {
	//    int mem3;
	// } t3;
	// struct s4 {
	//    struct {
	//       int mem5;
	//    } mem4;
	// };
	//
	// void main() {
	//    struct s1 vs1;
	//	  struct s2 vs2;
	//	  struct s4 vs4;
	//	  t2 vt2;
	//	  t3 vt3;
	//    int i;
	//    i= vs1.mem1; //ref
	//    i= vs2.mem2; //ref
	//    i= vs4.mem4.mem5; //ref
	//    i= vt2.mem2; //ref
	//    i= vt3.mem3; //ref
	// };
	private void doTestStructMembers(String filename) throws Exception {
		String content = readTaggedComment("testStructMembers");
		IFile file= createFile(getProject(), filename, content);
		waitForIndexer(fPdom, file, 1000);
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		CEditor editor= (CEditor) IDE.openEditor(page, file);

		editor.selectAndReveal(content.indexOf("mem1"), 0);
		openCallHierarchy(editor);
		Tree tree = getCHTree(page);
		checkTreeNode(tree, 0, "s1::mem1");
		checkTreeNode(tree, 0, 0, "main()");

		editor.selectAndReveal(content.indexOf("mem2"), 0);
		openCallHierarchy(editor);
		checkTreeNode(tree, 0, "s2::mem2");
		checkTreeNode(tree, 0, 0, "main()");
		
		editor.selectAndReveal(content.indexOf("mem3"), 0);
		openCallHierarchy(editor);
		checkTreeNode(tree, 0, "(anon)::mem3");

		editor.selectAndReveal(content.indexOf("mem4"), 0);
		openCallHierarchy(editor);
		checkTreeNode(tree, 0, "s4::mem4");
		checkTreeNode(tree, 0, 0, "main()");

		editor.selectAndReveal(content.indexOf("mem5"), 0);
		openCallHierarchy(editor);
		checkTreeNode(tree, 0, "s4::(anon)::mem5");

		editor.selectAndReveal(content.indexOf("mem1; //ref"), 0);
		openCallHierarchy(editor);
		tree = getCHTree(page);
		checkTreeNode(tree, 0, "s1::mem1");
		checkTreeNode(tree, 0, 0, "main()");

		editor.selectAndReveal(content.indexOf("mem2; //ref"), 0);
		openCallHierarchy(editor);
		tree = getCHTree(page);
		checkTreeNode(tree, 0, "s2::mem2");
		checkTreeNode(tree, 0, 0, "main()");

		editor.selectAndReveal(content.indexOf("mem4."), 0);
		openCallHierarchy(editor);
		tree = getCHTree(page);
		checkTreeNode(tree, 0, "s4::mem4");
		checkTreeNode(tree, 0, 0, "main()");
	}

	public void _testAnonymousStructMembersC() throws Exception {
		doTestAnonymousStructMembers("anon_struct_member.c");
	}
	
	public void _testAnonymousStructMembersCpp() throws Exception {
		doTestAnonymousStructMembers("anon_struct_member.cpp");
	}
	
	private void doTestAnonymousStructMembers(String filename) throws Exception {
		String content = readTaggedComment("testStructMembers");
		IFile file= createFile(getProject(), filename, content);
		waitForIndexer(fPdom, file, 1000);
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		CEditor editor= (CEditor) IDE.openEditor(page, file);

		editor.selectAndReveal(content.indexOf("mem3"), 0);
		openCallHierarchy(editor);
		Tree tree = getCHTree(page);
		checkTreeNode(tree, 0, "(anon)::mem3");
		checkTreeNode(tree, 0, 0, "main()");

		editor.selectAndReveal(content.indexOf("mem5"), 0);
		openCallHierarchy(editor);
		checkTreeNode(tree, 0, "s4::(anon)::mem5");
		checkTreeNode(tree, 0, 0, "main()");

		editor.selectAndReveal(content.indexOf("mem3; //ref"), 0);
		openCallHierarchy(editor);
		tree = getCHTree(page);
		checkTreeNode(tree, 0, "(anon)::mem3");
		checkTreeNode(tree, 0, 0, "main()");

		editor.selectAndReveal(content.indexOf("mem5; //ref"), 0);
		openCallHierarchy(editor);
		tree = getCHTree(page);
		checkTreeNode(tree, 0, "s4::(anon)::mem5");
		checkTreeNode(tree, 0, 0, "main()");
	}
	
	
	public void testUnionMembersC() throws Exception {
		doTestUnionMembers("union_member.c");
	}
	
	public void testUnionMembersCpp() throws Exception {
		doTestUnionMembers("union_member.cpp");
	}

	// {testUnionMembers}
	// union u1 {
	//    int mem1;
	//    char c;
	// };
	// typedef union u2 {
	//    int mem2;
	//    char c;
	// } t2;
	// typedef union {
	//    int mem3;
	//    char c;
	// } t3;
	// union u4 {
	//    union {
	//       int mem5;
	//       char c;
	//    } mem4;
	//    char c;
	// };
	//
	// void main() {
	//    union u1 vs1;
	//	  union u2 vs2;
	//	  union u4 vs4;
	//	  t2 vt2;
	//	  t3 vt3;
	//    int i;
	//    i= vs1.mem1; //ref
	//    i= vs2.mem2; //ref
	//    i= vs4.mem4.mem5; //ref
	//    i= vt2.mem2; //ref
	//    i= vt3.mem3; //ref
	// };
	private void doTestUnionMembers(String filename) throws Exception {
		String content = readTaggedComment("testUnionMembers");
		IFile file= createFile(getProject(), filename, content);
		waitForIndexer(fPdom, file, 1000);
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		CEditor editor= (CEditor) IDE.openEditor(page, file);

		editor.selectAndReveal(content.indexOf("mem1"), 0);
		openCallHierarchy(editor);
		Tree tree = getCHTree(page);
		checkTreeNode(tree, 0, "u1::mem1");
		checkTreeNode(tree, 0, 0, "main()");

		editor.selectAndReveal(content.indexOf("mem2"), 0);
		openCallHierarchy(editor);
		checkTreeNode(tree, 0, "u2::mem2");
		checkTreeNode(tree, 0, 0, "main()");
		
		editor.selectAndReveal(content.indexOf("mem3"), 0);
		openCallHierarchy(editor);
		checkTreeNode(tree, 0, "(anon)::mem3");

		editor.selectAndReveal(content.indexOf("mem4"), 0);
		openCallHierarchy(editor);
		checkTreeNode(tree, 0, "u4::mem4");
		checkTreeNode(tree, 0, 0, "main()");

		editor.selectAndReveal(content.indexOf("mem5"), 0);
		openCallHierarchy(editor);
		checkTreeNode(tree, 0, "u4::(anon)::mem5");

		editor.selectAndReveal(content.indexOf("mem1; //ref"), 0);
		openCallHierarchy(editor);
		tree = getCHTree(page);
		checkTreeNode(tree, 0, "u1::mem1");
		checkTreeNode(tree, 0, 0, "main()");

		editor.selectAndReveal(content.indexOf("mem2; //ref"), 0);
		openCallHierarchy(editor);
		tree = getCHTree(page);
		checkTreeNode(tree, 0, "u2::mem2");
		checkTreeNode(tree, 0, 0, "main()");

		editor.selectAndReveal(content.indexOf("mem4."), 0);
		openCallHierarchy(editor);
		tree = getCHTree(page);
		checkTreeNode(tree, 0, "u4::mem4");
		checkTreeNode(tree, 0, 0, "main()");
	}

	public void _testAnonymousUnionMembersC() throws Exception {
		doTestAnonymousUnionMembers("anon_union_member.c");
	}
	
	public void _testAnonymousUnionMembersCpp() throws Exception {
		doTestAnonymousUnionMembers("anon_union_member.cpp");
	}
	
	private void doTestAnonymousUnionMembers(String filename) throws Exception {
		String content = readTaggedComment("testUnionMembers");
		IFile file= createFile(getProject(), filename, content);
		waitForIndexer(fPdom, file, 1000);
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		CEditor editor= (CEditor) IDE.openEditor(page, file);

		editor.selectAndReveal(content.indexOf("mem3"), 0);
		openCallHierarchy(editor);
		Tree tree = getCHTree(page);
		checkTreeNode(tree, 0, "(anon)::mem3");
		checkTreeNode(tree, 0, 0, "main()");

		editor.selectAndReveal(content.indexOf("mem5"), 0);
		openCallHierarchy(editor);
		checkTreeNode(tree, 0, "u4::(anon)::mem5");
		checkTreeNode(tree, 0, 0, "main()");

		editor.selectAndReveal(content.indexOf("mem3; //ref"), 0);
		openCallHierarchy(editor);
		tree = getCHTree(page);
		checkTreeNode(tree, 0, "(anon)::mem3");
		checkTreeNode(tree, 0, 0, "main()");

		editor.selectAndReveal(content.indexOf("mem5; //ref"), 0);
		openCallHierarchy(editor);
		tree = getCHTree(page);
		checkTreeNode(tree, 0, "u4::(anon)::mem5");
		checkTreeNode(tree, 0, 0, "main()");
	}
}
