package SOEN691.patterns;

import java.util.HashMap;
import java.util.HashSet;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.dom.*;

import SOEN691.handlers.SampleHandler;
import SOEN691.visitors.CatchClauseVisitor;

public class ExceptionFinder {
	HashMap<MethodDeclaration, String> suspectMethods = new HashMap<>();
	
	HashSet<MethodDeclaration> multiLineLogCatchMethod = new HashSet<>();
	HashSet<MethodDeclaration> destructiveWrappingMethod = new HashSet<>();
	HashSet<MethodDeclaration> overCatchMethod = new HashSet<>();

	public HashMap<MethodDeclaration, String> getSuspectMethods() {
		return suspectMethods;
	}

	public void findExceptions(IProject project) throws JavaModelException {
		IPackageFragment[] packages = JavaCore.create(project).getPackageFragments();

		for(IPackageFragment mypackage : packages){
			findTargetCatchClauses(mypackage);
		}
	}

	private void findTargetCatchClauses(IPackageFragment packageFragment) throws JavaModelException {


		for (ICompilationUnit unit : packageFragment.getCompilationUnits()) {
			CompilationUnit parsedCompilationUnit = parse(unit);

			CatchClauseVisitor exceptionVisitor = new CatchClauseVisitor();
			parsedCompilationUnit.accept(exceptionVisitor);

//			printExceptions(exceptionVisitor);
			getMethodsWithTargetCatchClauses(exceptionVisitor);
		}
	}
	
	private void getMethodsWithTargetCatchClauses(CatchClauseVisitor catchClauseVisitor) {
		
		for(CatchClause multiLineCatch: catchClauseVisitor.getMultipleLineLogCatches()) {
			multiLineLogCatchMethod.add(findMethodForCatch(multiLineCatch));
			suspectMethods.put(findMethodForCatch(multiLineCatch), "MultiLineLogCatch");
		}
		
		for(CatchClause destructiveWrappingCatch: catchClauseVisitor.getDestructiveWrappingCatches()) {
			destructiveWrappingMethod.add(findMethodForCatch(destructiveWrappingCatch));
			suspectMethods.put(findMethodForCatch(destructiveWrappingCatch), "destructiveWrappingCatch");
		}
		
		for(CatchClause overCatch: catchClauseVisitor.getOverCatches()) {
			overCatchMethod.add(findMethodForCatch(overCatch));
			suspectMethods.put(findMethodForCatch(overCatch), "overCatch");
		}
		
		printInvocations();
	}
	
	private MethodDeclaration findMethodForCatch(CatchClause catchClause) {
		return (MethodDeclaration) findParentMethodDeclaration(catchClause);
	}
	
	private ASTNode findParentMethodDeclaration(ASTNode node) {
		if(node.getParent().getNodeType() == ASTNode.METHOD_DECLARATION) {
			return node.getParent();
		} else {
			return findParentMethodDeclaration(node.getParent());
		}
	}

	private void printExceptions(CatchClauseVisitor visitor) {
		SampleHandler.printMessage("__________________MULTIPLE LINE CATCHES___________________");
		for(CatchClause statement: visitor.getMultipleLineLogCatches()) {
			SampleHandler.printMessage(statement.toString());
		}
		SampleHandler.printMessage("__________________DESTRUCTIVE WRAPPING CATCHES___________________");
		for(CatchClause statement: visitor.getDestructiveWrappingCatches()) {
			SampleHandler.printMessage(statement.toString());
		}

		SampleHandler.printMessage("__________________OVER CATCHES___________________");
		for(CatchClause statement: visitor.getOverCatches()) {
			SampleHandler.printMessage(statement.toString());
		}

	}

	public static CompilationUnit parse(ICompilationUnit unit) {
		ASTParser parser = ASTParser.newParser(AST.JLS11);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setSource(unit);
		parser.setResolveBindings(true);
		parser.setBindingsRecovery(true);
		parser.setStatementsRecovery(true);
		return (CompilationUnit) parser.createAST(null); // parse
	}
	
	private void printInvocations() {
		
		for(MethodDeclaration declaration : multiLineLogCatchMethod) {
			SampleHandler.printMessage(String.format("Following method suffers from the %s pattern", "multiLine-Log"));
			SampleHandler.printMessage(declaration.toString());
		}
		
		for(MethodDeclaration declaration : destructiveWrappingMethod) {
			SampleHandler.printMessage(String.format("Following method suffers from the %s pattern", "destructive Wrapping"));
			SampleHandler.printMessage(declaration.toString());
		}
		
		for(MethodDeclaration declaration : overCatchMethod) {
			SampleHandler.printMessage(String.format("Following method suffers from the %s pattern", "over Catch"));
			SampleHandler.printMessage(declaration.toString());
		}
	}
}