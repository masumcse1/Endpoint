package com.abc;

import java.io.IOException;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.NullLiteralExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.CatchClause;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.stmt.TryStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;

public class ProductCreate {

	public static void main(String[] args) {
		CompilationUnit compilationUnit = new CompilationUnit();
		compilationUnit.setPackageDeclaration("org.meveo.mymodule.resource");
		compilationUnit.addImport("java.util", false, true);

		String entityClass = "Product";
		String dtoClass = "ProductDto";
		String scriptservice = "CreateMyProduct";

		ClassOrInterfaceDeclaration restEndpointClass = compilationUnit.addClass(getRestClassName(entityClass, "POST"),
				Modifier.Keyword.PUBLIC);
		restEndpointClass.addSingleMemberAnnotation("Path", new StringLiteralExpr("myproduct"));
		restEndpointClass.addMarkerAnnotation("RequestScoped");

		String injectedFieldName = getNonCapitalizeName(scriptservice);
		var injectedfield = restEndpointClass.addField(scriptservice, injectedFieldName, Modifier.Keyword.PRIVATE);
		injectedfield.addMarkerAnnotation("Inject");

		NodeList<ClassOrInterfaceType> extendsList = new NodeList<>();
		extendsList.add(new ClassOrInterfaceType("CustomEndpointResource"));
		restEndpointClass.setExtendedTypes(extendsList);

		MethodDeclaration restMethod = restEndpointClass.addMethod(getRestMethodName(entityClass, "POST"),
				Modifier.Keyword.PUBLIC);
		restMethod.addParameter(dtoClass, getNonCapitalizeName(dtoClass));
		restMethod.setType("Response");
		restMethod.addMarkerAnnotation("Post");
		restMethod.addSingleMemberAnnotation("Produces", "MediaType.APPLICATION_JSON");
		restMethod.addSingleMemberAnnotation("Consumes", "MediaType.APPLICATION_JSON");
		restMethod.addThrownException(IOException.class);
		// restMethod.addThrownException(ServletException.class);

		BlockStmt blockStmt = new BlockStmt();

		VariableDeclarator var_result = new VariableDeclarator();
		var_result.setName("result");
		var_result.setType("String");
		var_result.setInitializer(new NullLiteralExpr());

		NodeList<VariableDeclarator> variableDeclarators = new NodeList<>();
		variableDeclarators.add(var_result);

		blockStmt.addStatement(
				new ExpressionStmt().setExpression(new VariableDeclarationExpr().setVariables(variableDeclarators)));

		blockStmt.addStatement(new ExpressionStmt(new NameExpr("parameterMap = new HashMap<String, Object>()")));

		MethodCallExpr getEntity_methodCall = new MethodCallExpr(new NameExpr("parameterMap"), "put");
		getEntity_methodCall.addArgument(new StringLiteralExpr(getNonCapitalizeName(entityClass)));
		getEntity_methodCall
				.addArgument(new MethodCallExpr(new NameExpr(getNonCapitalizeName(dtoClass)), "get" + entityClass));

		blockStmt.addStatement(getEntity_methodCall);

		MethodCallExpr getType_methodCall = new MethodCallExpr(new NameExpr("parameterMap"), "put");
		getType_methodCall.addArgument(new StringLiteralExpr("type"));
		getType_methodCall.addArgument(new MethodCallExpr(new NameExpr(getNonCapitalizeName(dtoClass)), "getType"));

		blockStmt.addStatement(getType_methodCall);

		blockStmt.addStatement(new ExpressionStmt(new NameExpr("setRequestResponse()")));

		BlockStmt tryblock = new BlockStmt();
		tryblock.addStatement(new MethodCallExpr(new NameExpr(injectedFieldName), "set" + entityClass)
				.addArgument(new MethodCallExpr(new NameExpr(getNonCapitalizeName(dtoClass)), "get" + entityClass)));
		tryblock.addStatement(new MethodCallExpr(new NameExpr(injectedFieldName), "init").addArgument("parameterMap"));
		tryblock.addStatement(
				new MethodCallExpr(new NameExpr(injectedFieldName), "execute").addArgument("parameterMap"));
		tryblock.addStatement(
				new MethodCallExpr(new NameExpr(injectedFieldName), "finalize").addArgument("parameterMap"));
		tryblock.addStatement(assignment(var_result.getNameAsString(), injectedFieldName, "getResult"));

		Statement trystatement = addingException(tryblock);

		blockStmt.addStatement(trystatement);
		restMethod.setBody(blockStmt);

		restMethod.getBody().get().getStatements()
				.add(new ReturnStmt(new NameExpr("Response.status(Response.Status.OK).entity(result).build()")));

		System.out.println(compilationUnit);

	}

	private static String getRestClassName(String entityClass, String method) {
		String className = null;
		if (method.equals("POST")) {
			className = entityClass + "Create";
		}
		return className;
	}

	private static String getRestMethodName(String entityClass, String method) {
		String methodName = null;
		if (method.equals("POST")) {
			methodName = "save" + entityClass;
		}
		return methodName;
	}

	private static Statement addingException(BlockStmt body) {
		TryStmt ts = new TryStmt();
		ts.setTryBlock(body);
		CatchClause cc = new CatchClause();
		String exceptionName = "e";
		cc.setParameter(new Parameter().setName(exceptionName).setType(Exception.class));
		BlockStmt cb = new BlockStmt();
		cb.addStatement(new ExpressionStmt(
				new NameExpr("return Response.status(Response.Status.BAD_REQUEST).entity(result).build()")));
		// cb.addStatement(new ThrowStmt(new NameExpr(exceptionName)));
		cc.setBody(cb);
		ts.setCatchClauses(new NodeList<>(cc));
		// return new BlockStmt(new NodeList<>(ts));
		return ts;
	}

	public static Statement assignment(String assignOject, String callOBject, String methodName) {
		MethodCallExpr methodCallExpr = new MethodCallExpr(new NameExpr(callOBject), methodName);
		AssignExpr assignExpr = new AssignExpr(new NameExpr(assignOject), methodCallExpr, AssignExpr.Operator.ASSIGN);
		return new ExpressionStmt(assignExpr);
	}

	public static String getNonCapitalizeName(String className) {
		if (className == null || className.length() == 0)
			return className;
		String objectReferenceName = className.substring(0, 1).toLowerCase() + className.substring(1);
		return objectReferenceName;

	}

}
