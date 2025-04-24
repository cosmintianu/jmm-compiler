package pt.up.fe.comp2025.optimization;

import com.sun.jdi.BooleanType;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp2025.ast.Kind;
import pt.up.fe.comp2025.ast.TypeUtils;

import java.util.ArrayList;
import java.util.List;

import static pt.up.fe.comp2025.ast.Kind.*;

/**
 * Generates OLLIR code from JmmNodes that are expressions.
 */
public class OllirExprGeneratorVisitor extends AJmmVisitor<Void, OllirExprResult> {

    private final Character QUOTATION = '"';
    private final Character COMMA = ',';
    private static final String SPACE = " ";
    private static final String ASSIGN = ":=";
    private final String END_STMT = ";\n";
    private final String ARRAY_LEN = "arraylength";

    private final String L_PARENTHESES = "(";
    private final String R_PARENTHESES = ")";
    private final String L_BRACKET = "[";
    private final String R_BRACKET = "]";

    private final String NEW = "new";

    private final SymbolTable table;

    private final TypeUtils types;
    private final OptUtils ollirTypes;

    private boolean isMethodStatic;


    public OllirExprGeneratorVisitor(SymbolTable table, OptUtils ollirTypes) {
        this.table = table;
        this.types = new TypeUtils(table);
        this.ollirTypes = ollirTypes;
    }


    @Override
    protected void buildVisitor() {
        addVisit(VAR_REF_EXPR, this::visitVarRef);
        addVisit(BINARY_EXPR, this::visitBinExpr);
        addVisit(INTEGER_LITERAL, this::visitInteger);
        addVisit(BOOLEAN_LITERAL, this::visitBoolean);
        addVisit(METHOD_CALL_EXPR, this::visitMethodCallExpr);
        addVisit(LENGTH_EXPR, this::visitLengthExpr);
        addVisit(NEW_OBJECT_EXPR, this::visitNewObjectExpr);
        addVisit(INDEX_ACCESS_EXPR, this::visitAccessExpr);
        addVisit(NEW_ARRAY_EXPR, this::visitNewArrayExpr);
        addVisit(UNARY_EXPR, this::visitUnaryExpr);
        addVisit(PAREN_EXPR, this::visitParentExpr);
        addVisit(THIS_EXPR, this::visitThisExpr);
        addVisit(ARRAY_LITERAL, this::visitArrayLiteral);

//        setDefaultVisit(this::defaultVisit);
    }

/*
    ********************************************************
                    Visit Expr Implementations
    ********************************************************
*/

    private OllirExprResult visitArrayLiteral(JmmNode node, Void unused){
        Type type = types.getExprType(node);
        String ollirType = ollirTypes.toOllirType(type);
        var nextTemp = ollirTypes.nextTemp();
        String code = nextTemp + ollirType;

        String assign = SPACE + ASSIGN + ollirType + SPACE;

        var arg_type = ollirTypes.toOllirType(type.getName());
        String assign_arg = SPACE + ASSIGN + arg_type + SPACE;

        StringBuilder computation = new StringBuilder();

        StringBuilder args = new StringBuilder();

        int num_arg = 0;

        for (num_arg = 0; num_arg < node.getChildren().size(); num_arg++) {
            var arg = visit(node.getChild(num_arg));

            args.append(nextTemp).
                    append(L_BRACKET).
                    append(num_arg).append(arg_type). //index
                    append(R_BRACKET).append(arg_type).
                    append(assign_arg).
                    append(arg.getCode()).
                    append(END_STMT);
        }

        computation.append(code).
                append(assign).
                append(NEW).
                append(L_PARENTHESES).
                append("array, ").append(num_arg).append(arg_type).
                append(R_PARENTHESES).
                append(ollirType).
                append(END_STMT);

        computation.append(args);

        return new OllirExprResult(code, computation.toString());
    }

    private OllirExprResult visitThisExpr(JmmNode node, Void unused){

        Type parent_type = types.getExprType(node.getParent().getChild(0));
        String ollirType = ollirTypes.toOllirType(parent_type);

        String code = node.get("name") + ollirType;

        return new OllirExprResult(code);
    }

    private OllirExprResult visitParentExpr(JmmNode node, Void unused){
        var expr = visit(node.getChild(0));

        return new OllirExprResult(expr.getCode(), expr.getComputation());
    }

    private OllirExprResult visitUnaryExpr(JmmNode node, Void unused){

        Type type = types.getExprType(node);
        String ollirType = ollirTypes.toOllirType(type);
        String code = ollirTypes.nextTemp() + ollirType;

        StringBuilder computation = new StringBuilder();

        computation.append(code).
                append(SPACE).
                append(ASSIGN).
                append(ollirType).
                append(SPACE).
                append("!").append(ollirType).
                append(SPACE);

        var true_or_false = visit(node.getChild(0));

        computation.append(true_or_false.getCode()).append(END_STMT);

        return new OllirExprResult(code, computation.toString());
    }

    private OllirExprResult visitNewArrayExpr(JmmNode node, Void unused) {

        Type type = types.getExprType(node);
        String ollirType = ollirTypes.toOllirType(type);
        String code = ollirTypes.nextTemp() + ollirType;

        StringBuilder computation = new StringBuilder();

        OllirExprResult index = visit(node.getChild(0));

        computation.append(code).
                append(SPACE).
                append(ASSIGN).
                append(ollirType).
                append(SPACE);

        computation.append(NEW).
                append(L_PARENTHESES).
                append("array").
                append(COMMA).
                append(SPACE).
                append(index.getCode()).
                append(R_PARENTHESES).
                append(ollirType).
                append(END_STMT);


        return new OllirExprResult(code, computation.toString());
    }

    private OllirExprResult visitNewObjectExpr(JmmNode node, Void unused) {

        Type type = types.getExprType(node);
        String ollirType = ollirTypes.toOllirType(type);
        String code = ollirTypes.nextTemp() + ollirType;

        var objectClass = node.get("name");

        StringBuilder computation = new StringBuilder();

        String methodInvocation =
                "invokespecial(" + code + COMMA + SPACE + QUOTATION + "<init>" + QUOTATION + ").V;\n";

        computation.append(code);
        computation.append(SPACE);
        computation.append(ASSIGN).append(ollirType);
        computation.append(SPACE);

        computation.append(NEW)
            .append(L_PARENTHESES)
            .append(objectClass)
            .append(R_PARENTHESES);

        computation.append(ollirType)
            .append(END_STMT);

        computation.append(methodInvocation);

        return new OllirExprResult(code, computation);
    }

    private OllirExprResult visitAccessExpr(JmmNode node, Void unused) {

        StringBuilder computation = new StringBuilder();

        var left = visit(node.getChild(0));
        var right = visit(node.getChild(1));

        computation.append(right.getComputation());

        Type type = types.getExprType(node);
        String ollirType = ollirTypes.toOllirType(type);
        String code = ollirTypes.nextTemp() + ollirType;

        computation.append(code);
        computation.append(SPACE);
        computation.append(ASSIGN);
        computation.append(ollirType);
        computation.append(SPACE);

        computation.append(left.getCode());

        String leftComputation = L_BRACKET + right.getCode() + R_BRACKET + ollirType;

        computation.append(leftComputation);
        computation.append(END_STMT);

        return new OllirExprResult(code, computation);
    }

    private OllirExprResult visitLengthExpr(JmmNode node, Void unused) {

        StringBuilder computation = new StringBuilder();

        Type type = types.getExprType(node);
        String ollirType = ollirTypes.toOllirType(type);
        String code = ollirTypes.nextTemp() + ollirType;

        computation.append(code);

        computation.append(SPACE)
                .append(ASSIGN)
                .append(ollirType)
                .append(SPACE);

        computation.append(ARRAY_LEN)
                .append(L_PARENTHESES);

        var child = visit(node.getChild(0));
        computation.append(child.getComputation()).append(child.getCode());

        computation.append(R_PARENTHESES)
                .append(ollirType)
                .append(END_STMT);

        return new OllirExprResult(code, computation);
    }

    private OllirExprResult visitMethodCallExpr(JmmNode node, Void unused){

        TypeUtils typeUtils = new TypeUtils(table);

        StringBuilder computation = new StringBuilder();
        String code = "";

        //get target
        var varRefExpr = node.getChild(0);
        String varRefExprName = varRefExpr.get("name");

        //check if it is static or virtual
        boolean isMethodStatic = false;
        boolean isThis = varRefExprName.equals("this");

        //are there other cases when a method is static?
        if (table.getImports().stream()
                .map(importName -> importName.substring(importName.lastIndexOf('.') + 1))
                .anyMatch(methodName -> methodName.equals(varRefExprName))) {
            isMethodStatic = true;
        }

        String methodInvocation = isMethodStatic ? "invokestatic" : "invokevirtual";

        //get method name
        String methodName = node.get("name");

        String currentMethodName = varRefExpr.getAncestor(Kind.METHOD_DECL).get().get("nameMethod");

        StringBuilder invocation = new StringBuilder();

        // *************** Finding return Type *****************

        String ollirRetType;

        if (!isMethodStatic) {
            Type expectedRetType = typeUtils.getExprType(node);

            if (expectedRetType != null) {
                ollirRetType = ollirTypes.toOllirType(expectedRetType);
            } else {
                ollirRetType = ".V";
            }
        } else{
            if (node.getParent().isInstance(ARRAY_ASSIGN_STMT))
                ollirRetType = ".i32";

            else if (node.getParent().isInstance(VAR_ASSIGN_STMT)) {
                //String currentMethodName = varRefExpr.getAncestor(Kind.METHOD_DECL).get().get("nameMethod");
                String typeName = node.getParent().getChild(0).get("name");

                for (Symbol localVar : table.getLocalVariables(currentMethodName)) {
                    if (localVar.getName().equals(typeName)) {
                        typeName = localVar.getType().getName();
                    }
                }

                for (Symbol param : table.getParameters(currentMethodName)) {
                    if (param.getName().equals(typeName)) {
                        typeName = param.getType().getName();
                    }
                }

                ollirRetType = ollirTypes.toOllirType(typeName);
            } else
                ollirRetType = ".V";

        }

        //******************************************

        if(!ollirRetType.equals(".V") && !node.getParent().isInstance(EXPR_STMT)) {
            code = ollirTypes.nextTemp() + ollirRetType;
            invocation.append(code).append(SPACE).append(ASSIGN).append(ollirRetType).append(SPACE);
        }

        invocation.append(methodInvocation).append(L_PARENTHESES).append(varRefExprName);


        //if invokevirtual, specify the type
        if (!isMethodStatic) {

            if(!isThis){
                Type varType = typeUtils.getExprType(varRefExpr);
                invocation.append(".").append(varType.getName());
            }else {
                String className = table.getClassName();
                invocation.append(".").append(className);
            }

        }

        invocation.append(COMMA).append(SPACE).append(QUOTATION).
                append(methodName).append(QUOTATION);

        //visit method params
        for (int i = 1; i < node.getChildren().size(); i++) {
            if (i == 1)
                invocation.append(COMMA).append(SPACE);
            var param = visit(node.getChild(i));
            computation.append(param.getComputation());
            invocation.append(param.getCode());

            if (i!= (node.getChildren().size() - 1)) //multiple params
                invocation.append(COMMA).append(SPACE);

//            if ( i == (node.getChildren().size() - 1)){} //handle varargs -- not mandatory for now

        }

        invocation.append(R_PARENTHESES).
                append(ollirRetType);

        computation.append(invocation).append(END_STMT);

        return new OllirExprResult(code, computation);
    }

    private OllirExprResult visitInteger(JmmNode node, Void unused) {
        var intType = TypeUtils.newIntType();
        String ollirIntType = ollirTypes.toOllirType(intType);
        String code = node.get("value") + ollirIntType;
        return new OllirExprResult(code);
    }

    private OllirExprResult visitBoolean(JmmNode node, Void unused) {
        var booleanType = new Type("boolean", false);
        String ollirBooleanType = ollirTypes.toOllirType(booleanType);
        String code = node.get("name") + ollirBooleanType;
        return new OllirExprResult(code);
    }


    private OllirExprResult visitBinExpr(JmmNode node, Void unused) {

        var lhs = visit(node.getChild(0));
        var rhs = visit(node.getChild(1));

        StringBuilder computation = new StringBuilder();

        // code to compute the children
        computation.append(lhs.getComputation());
        computation.append(rhs.getComputation());

        // code to compute self
        Type resType = types.getExprType(node);
        String resOllirType = ollirTypes.toOllirType(resType);
        String code = ollirTypes.nextTemp() + resOllirType;

        computation.append(code).append(SPACE)
                .append(ASSIGN).append(resOllirType).append(SPACE)
                .append(lhs.getCode()).append(SPACE);

        Type type = types.getExprType(node);
        computation.append(node.get("op")).append(ollirTypes.toOllirType(type)).append(SPACE)
                .append(rhs.getCode()).append(END_STMT);

        return new OllirExprResult(code, computation);
    }


    private OllirExprResult visitVarRef(JmmNode node, Void unused) {

        var id = node.get("name");
        Type type = types.getExprType(node);
        String ollirType = ollirTypes.toOllirType(type);

        String code = id + ollirType;

        return new OllirExprResult(code);
    }


    /**
     * Default visitor. Visits every child node and return an empty result.
     *
     * @param node
     * @param unused
     * @return
     */
    private OllirExprResult defaultVisit(JmmNode node, Void unused) {

        for (var child : node.getChildren()) {
            visit(child);
        }

        return OllirExprResult.EMPTY;
    }

}
