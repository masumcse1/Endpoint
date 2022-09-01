package pattern;

import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.CatchClause;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.stmt.TryStmt;

public abstract class BaseEndpointGenerator {
    
    public abstract void generate();
    
    protected  String getRestClassName(String entityClass, String method) {
		String className = null;
		if (method.equals("POST")) {
			className = entityClass + "Create";
		}
		return className;
	}

    protected  String getRestMethodName(String entityClass, String method) {
		String methodName = null;
		if (method.equals("POST")) {
			methodName = "save" + entityClass;
		}
		return methodName;
	}

    protected  Statement addingException(BlockStmt body) {
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
	
		return ts;
	}

    protected  Statement assignment(String assignOject, String callOBject, String methodName) {
		MethodCallExpr methodCallExpr = new MethodCallExpr(new NameExpr(callOBject), methodName);
		AssignExpr assignExpr = new AssignExpr(new NameExpr(assignOject), methodCallExpr, AssignExpr.Operator.ASSIGN);
		return new ExpressionStmt(assignExpr);
	}

    protected  String getNonCapitalizeName(String className) {
		if (className == null || className.length() == 0)
			return className;
		String objectReferenceName = className.substring(0, 1).toLowerCase() + className.substring(1);
		return objectReferenceName;

	}

	
	

}
