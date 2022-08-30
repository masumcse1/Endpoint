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
	
		ClassOrInterfaceDeclaration restEndpointClass = compilationUnit.addClass("ProductCreate",Modifier.Keyword.PUBLIC);
		restEndpointClass.addSingleMemberAnnotation("Path",new StringLiteralExpr("myproduct"));
		restEndpointClass.addMarkerAnnotation("RequestScoped");
		
		String injectedFieldName=getNonCapitalizeNameFromClassName("CreateMyProduct");
		var injectedfield=restEndpointClass.addField("CreateMyProduct", injectedFieldName, Modifier.Keyword.PRIVATE);
		injectedfield.addMarkerAnnotation("Inject");
		
		NodeList<ClassOrInterfaceType> extendsList = new NodeList<>();
		extendsList.add(new ClassOrInterfaceType("CustomEndpointResource"));
		restEndpointClass.setExtendedTypes(extendsList);

		MethodDeclaration restMethod = restEndpointClass.addMethod("saveProduct", Modifier.Keyword.PUBLIC);
		restMethod.addParameter("ProductDto", "productDto");
		restMethod.setType("Response");
		restMethod.addMarkerAnnotation("Post");
		restMethod.addSingleMemberAnnotation("Produces", "MediaType.APPLICATION_JSON");
		restMethod.addSingleMemberAnnotation("Consumes", "MediaType.APPLICATION_JSON");
		restMethod.addThrownException(IOException.class);
		// restMethod.addThrownException(ServletException.class);
	
		BlockStmt blockStmt = new BlockStmt();
		//blockStmt.addStatement(new ExpressionStmt(new NameExpr("String result = null")));	
		
		VariableDeclarator variableDeclarator = new VariableDeclarator();
		variableDeclarator.setName("result");
		variableDeclarator.setType("String");
		variableDeclarator.setInitializer(new NullLiteralExpr());	
		
		NodeList<VariableDeclarator> variableDeclarators = new NodeList<>();
		variableDeclarators.add(variableDeclarator);
		
		blockStmt.addStatement(new ExpressionStmt().setExpression(new VariableDeclarationExpr().setVariables(variableDeclarators)));

		blockStmt.addStatement(new ExpressionStmt(new NameExpr("parameterMap = new HashMap<String, Object>()")));	
	
		MethodCallExpr methodCallExpr = new MethodCallExpr(new NameExpr("parameterMap"), "put");
		methodCallExpr.addArgument(new StringLiteralExpr("product"));
		methodCallExpr.addArgument(new MethodCallExpr(new NameExpr("productDto"), "getProduct"));
		
	
		
		blockStmt.addStatement(methodCallExpr);

	
		MethodCallExpr methodCallExpr1 = new MethodCallExpr(new NameExpr("parameterMap"), "put");
		methodCallExpr1.addArgument(new StringLiteralExpr("type"));
		methodCallExpr1.addArgument(new MethodCallExpr(new NameExpr("productDto"), "getType"));

		blockStmt.addStatement(methodCallExpr1);

		blockStmt.addStatement(new ExpressionStmt(new NameExpr("setRequestResponse()")));

		BlockStmt tryblock = new BlockStmt();
		tryblock.addStatement(new MethodCallExpr(new NameExpr(injectedFieldName), "setProduct").addArgument("productDto.getProduct()"));
		tryblock.addStatement(new MethodCallExpr(new NameExpr(injectedFieldName), "init").addArgument("parameterMap"));
		tryblock.addStatement(new MethodCallExpr(new NameExpr(injectedFieldName), "execute").addArgument("parameterMap"));
		tryblock.addStatement(new MethodCallExpr(new NameExpr(injectedFieldName), "finalize").addArgument("parameterMap"));
		tryblock.addStatement(makeAssignment(variableDeclarator.getNameAsString(),injectedFieldName,"anyMethod"));
		
		Statement trystatement = addingException(tryblock);

		blockStmt.addStatement(trystatement);
		restMethod.setBody(blockStmt);

		restMethod.getBody().get().getStatements().add(new ReturnStmt(new NameExpr("Response.status(Response.Status.OK).entity(result).build()")));

		
	
		System.out.println(compilationUnit);

	}

	private static Statement addingException(BlockStmt body) {
		TryStmt ts = new TryStmt();
		ts.setTryBlock(body);
		CatchClause cc = new CatchClause();
		String exceptionName = "e";
		cc.setParameter(new Parameter().setName(exceptionName).setType(Exception.class));
		BlockStmt cb = new BlockStmt();
		cb.addStatement(new ExpressionStmt(	new NameExpr("return Response.status(Response.Status.BAD_REQUEST).entity(result).build()")));
		// cb.addStatement(new ThrowStmt(new NameExpr(exceptionName)));
		cc.setBody(cb);
		ts.setCatchClauses(new NodeList<>(cc));
		// return new BlockStmt(new NodeList<>(ts));
		return ts;
	}
	
	public static Statement makeAssignment(String assignOject ,String callOBject , String methodName) {
		MethodCallExpr methodCallExpr=new MethodCallExpr(new NameExpr(callOBject), methodName);
		AssignExpr assignExpr = new AssignExpr(new NameExpr(assignOject),	methodCallExpr,	AssignExpr.Operator.ASSIGN);
		return new ExpressionStmt(assignExpr);
	}
	
	public static  String getNonCapitalizeNameFromClassName(String className) {

		if (className == null || className.length() == 0)
			return className;

		String objectReferenceName = className.substring(0, 1).toLowerCase() + className.substring(1);
		return objectReferenceName;

	}

}
