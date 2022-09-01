package pattern;


import java.io.IOException;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.NullLiteralExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.type.ClassOrInterfaceType;

public class EndpointGenerator extends BaseEndpointGenerator {
	
	private final String endpointName = null;
	String entityClass ;
	String dtoClass ;
	String scriptservice ;
	String injectedFieldName ;
	

	public EndpointGenerator(String entityClass, String dtoClass, String scriptservice, String injectedFieldName) {
		super();
		this.entityClass = entityClass;
		this.dtoClass = dtoClass;
		this.scriptservice = scriptservice;
		this.injectedFieldName = getNonCapitalizeName(scriptservice);
	}


	@Override
	public void generate() {
		CompilationUnit cu = new CompilationUnit();
		cu.setPackageDeclaration("org.meveo.mymodule.resource");
		cu.addImport("java.util", false, true);

		ClassOrInterfaceDeclaration clazz=generateRestClass(cu);

		MethodDeclaration restMethod = generateRestMethod(clazz);

		BlockStmt beforeTryblock = new BlockStmt();

		VariableDeclarator var_result = new VariableDeclarator();
		var_result.setName("result");
		var_result.setType("String");
		var_result.setInitializer(new NullLiteralExpr());

		NodeList<VariableDeclarator> var_result_declarator = new NodeList<>();
		var_result_declarator.add(var_result);

		beforeTryblock.addStatement(new ExpressionStmt().setExpression(new VariableDeclarationExpr().setVariables(var_result_declarator)));

		beforeTryblock.addStatement(new ExpressionStmt(new NameExpr("parameterMap = new HashMap<String, Object>()")));

		MethodCallExpr getEntity_methodCall = new MethodCallExpr(new NameExpr("parameterMap"), "put");
		getEntity_methodCall.addArgument(new StringLiteralExpr(getNonCapitalizeName(entityClass)));
		getEntity_methodCall.addArgument(new MethodCallExpr(new NameExpr(getNonCapitalizeName(dtoClass)), "get" + entityClass));

		beforeTryblock.addStatement(getEntity_methodCall);

		MethodCallExpr getType_methodCall = new MethodCallExpr(new NameExpr("parameterMap"), "put");
		getType_methodCall.addArgument(new StringLiteralExpr("type"));
		getType_methodCall.addArgument(new MethodCallExpr(new NameExpr(getNonCapitalizeName(dtoClass)), "getType"));

		beforeTryblock.addStatement(getType_methodCall);

		beforeTryblock.addStatement(new ExpressionStmt(new NameExpr("setRequestResponse()")));

		Statement trystatement = generateTryBlock(var_result);

		beforeTryblock.addStatement(trystatement);
		restMethod.setBody(beforeTryblock);    

		restMethod.getBody().get().getStatements().add(getReturnType());

		System.out.println(cu);

		//return new GeneratedFile(QUERY_TYPE, generatedFilePath(), cu.toString());
	}

	
	  private ClassOrInterfaceDeclaration generateRestClass(CompilationUnit cu) {
		    ClassOrInterfaceDeclaration clazz = cu.addClass(getRestClassName(entityClass, "POST"),	Modifier.Keyword.PUBLIC);
			clazz.addSingleMemberAnnotation("Path", new StringLiteralExpr("myproduct"));
			clazz.addMarkerAnnotation("RequestScoped");
			
			var injectedfield = clazz.addField(scriptservice, injectedFieldName, Modifier.Keyword.PRIVATE);
			injectedfield.addMarkerAnnotation("Inject");

			NodeList<ClassOrInterfaceType> extendsList = new NodeList<>();
			extendsList.add(new ClassOrInterfaceType("CustomEndpointResource"));
			clazz.setExtendedTypes(extendsList);
			return clazz;
	  }
	 
	private MethodDeclaration generateRestMethod(ClassOrInterfaceDeclaration clazz) {
		MethodDeclaration restMethod = clazz.addMethod(getRestMethodName(entityClass, "POST"),	Modifier.Keyword.PUBLIC);
		restMethod.addParameter(dtoClass, getNonCapitalizeName(dtoClass));
		restMethod.setType("Response");
		restMethod.addMarkerAnnotation("Post");
		restMethod.addSingleMemberAnnotation("Produces", "MediaType.APPLICATION_JSON");
		restMethod.addSingleMemberAnnotation("Consumes", "MediaType.APPLICATION_JSON");
		restMethod.addThrownException(IOException.class);
		// restMethod.addThrownException(ServletException.class);
        return restMethod;

	}
	
	private Statement generateTryBlock(VariableDeclarator assignmentVariable) {
		BlockStmt tryblock = new BlockStmt();
		tryblock.addStatement(new MethodCallExpr(new NameExpr(injectedFieldName), "set" + entityClass).addArgument(new MethodCallExpr(new NameExpr(getNonCapitalizeName(dtoClass)), "get" + entityClass)));
		tryblock.addStatement(new MethodCallExpr(new NameExpr(injectedFieldName), "init").addArgument("parameterMap"));
		tryblock.addStatement(new MethodCallExpr(new NameExpr(injectedFieldName), "execute").addArgument("parameterMap"));
		tryblock.addStatement(new MethodCallExpr(new NameExpr(injectedFieldName), "finalize").addArgument("parameterMap"));
		tryblock.addStatement(assignment(assignmentVariable.getNameAsString(), injectedFieldName, "getResult"));
		Statement trystatement = addingException(tryblock);
		
		return trystatement;
	}

	private ReturnStmt getReturnType() {
		
		return new ReturnStmt(new NameExpr("Response.status(Response.Status.OK).entity(result).build()"));
	}

}
