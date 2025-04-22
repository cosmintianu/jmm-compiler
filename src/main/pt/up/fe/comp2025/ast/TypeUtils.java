package pt.up.fe.comp2025.ast;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp2025.symboltable.JmmSymbolTable;

/**
 * Utility methods regarding types.
 */
public class TypeUtils {


    private final JmmSymbolTable table;

    public TypeUtils(SymbolTable table) {
        this.table = (JmmSymbolTable) table;
    }

    public static Type newIntType() {
        return new Type("int", false);
    }

    public static Type convertType(JmmNode typeNode) {

        var name = typeNode.get("name");
        var isArray = Boolean.parseBoolean(typeNode.get("isArray"));

        Type newType = new Type(name, isArray);
        newType.putObject("isVarargs", typeNode.get("isVarargs"));

        return newType;
    }

    /**
     * Gets the {@link Type} of an arbitrary expression.
     *
     * @param expr
     * @return
     */
    public Type getExprType(JmmNode expr) {

        // TODO: Update when there are new types
        //System.out.println("type: " + expr);
        Type type = switch (Kind.fromString(expr.getKind())) {
            case BINARY_EXPR -> getBinExprType(expr);
            case METHOD_CALL_EXPR -> getMethodExprType(expr);
            case PAREN_EXPR, UNARY_EXPR -> getParentExprType(expr);
            case NEW_ARRAY_EXPR, ARRAY_LITERAL -> new Type("int", true);
            case NEW_OBJECT_EXPR, CLASS_TYPE -> new Type(expr.get("name"), false);
            case INTEGER_LITERAL, LENGTH_EXPR -> new Type("int", false);
            case BOOLEAN_LITERAL -> new Type("boolean", false);
            case VAR_REF_EXPR -> getVarRefExprType(expr);
            case THIS_EXPR -> new Type("this", false);
            case INDEX_ACCESS_EXPR -> new Type("int", false); // It is int for now
            case PARAM -> getExprType(expr.getChild(0));
            case INT_TYPE -> new Type("int", Boolean.parseBoolean(expr.get("isArray")));
            case BOOLEAN_TYPE -> new Type("boolean", Boolean.parseBoolean(expr.get("isArray")));
            case VARARGS_TYPE -> new Type("int", true); // Maybe need to check this
            default -> throw new UnsupportedOperationException("Unknown Kind" + Kind.fromString(expr.getKind()) + "'");
        };

        //System.out.println("type " + type);
        return type;
    }

    private Type getParentExprType(JmmNode expr) {
        var child = expr.getChild(0);
        return getExprType(child);
    }

    private Type getMethodExprType(JmmNode methodExpr) {

        String idName = methodExpr.get("name");

        if (table.getReturnType(idName) != null) {
            return table.getReturnType(idName);
        }
        else {
            return new Type("methodExpr_assign", false);
        }
    }


    private static Type getBinExprType(JmmNode binaryExpr) {

        return switch (binaryExpr.get("op")) {
            case "+", "*", "-", "/" -> new Type("int", false);
            case "&&", "||", "<", "!", ">" -> new Type("boolean", false);
            default -> throw new RuntimeException("Unknown operator '" + binaryExpr.get("op"));
        };
    }

    public Type getVarRefExprType(JmmNode varRefExpr) {

        if (varRefExpr.getAncestor(Kind.METHOD_DECL).isEmpty()) {
            return null;
        }

        String methodName = varRefExpr.getAncestor(Kind.METHOD_DECL).get().get("nameMethod");
        String varName = varRefExpr.get("name");

        for (Symbol field : table.getFields()) {
            if (field.getName().equals(varName)) {
                return field.getType();
            }
        }

        for (Symbol param : table.getParameters(methodName)) {
            if (param.getName().equals(varName)) {
                return param.getType();
            }
        }

        for (Symbol localVar : table.getLocalVariables(methodName)) {
            if (localVar.getName().equals(varName)) {
                return localVar.getType();

            }
        }

        return null;
    }


}
