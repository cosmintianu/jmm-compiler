package pt.up.fe.comp2025.ast;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.Stage;
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
        return new Type(name, isArray);
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
            case INTEGER_LITERAL -> new Type("int", false);
            case ARRAY_LITERAL -> getArrayExprType(expr);
            case BINARY_EXPR ->getBinExprType(expr);
            case VAR_REF_EXPR -> getVarRefExprType(expr);
            case BOOLEAN_LITERAL -> new Type("boolean", false);
            case NEW_ARRAY_EXPR -> getArrayExprType(expr);
            default -> throw new UnsupportedOperationException("Unknown Kind" + Kind.fromString(expr.getKind()) + "'");
        };

        return type;
    }

    private Type getArrayExprType(JmmNode arrayExpr) {

        var first_type = getExprType(arrayExpr.getChild(0));



        for (var element : arrayExpr.getChildren()) {

            if (!getExprType(element).equals(first_type)) {

                return new Type("invalidArray", true);
            }
        }

        return new Type("int", true);
    }

    private static Type getBinExprType(JmmNode binaryExpr) {

        return switch (binaryExpr.get("op")) {
            case "+", "*","-", "/" -> new Type("int", false);
            case "&&", "<", "!" -> new Type("boolean", false);
            default -> throw new RuntimeException("Unknown operator '" + binaryExpr.get("op"));
        };
    }

    private Type getVarRefExprType(JmmNode varRefExpr) {

        if(!varRefExpr.getAncestor(Kind.METHOD_DECL).isPresent())
            return null;

        String methodName = varRefExpr.getAncestor(Kind.METHOD_DECL).get().get("nameMethod");
        String varName = varRefExpr.get("name");

        for (Symbol field : table.getFields()){
            if (field.getName().equals(varName))
                return field.getType();
        }

        for (Symbol param : table.getParameters(methodName)){
            if (param.getName().equals(varName))
                return param.getType();
        }

        for (Symbol localVar : table.getLocalVariables(methodName)){
            if (localVar.getName().equals(varName)){
                return localVar.getType();

            }
        }

        return null;
    }


}
