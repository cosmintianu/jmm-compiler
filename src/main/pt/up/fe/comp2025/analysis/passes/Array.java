package pt.up.fe.comp2025.analysis.passes;

import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp2025.analysis.AnalysisVisitor;
import pt.up.fe.comp2025.ast.Kind;
import pt.up.fe.comp2025.ast.TypeUtils;

public class Array extends AnalysisVisitor {

    @Override
    protected void buildVisitor() {
        addVisit(Kind.ARRAY_LITERAL, this::visitArrayLiteral);
        addVisit(Kind.INDEX_ACCESS_EXPR, this::visitIndexAccessExpr);
    }

    // Prevents trying to access an array through an invalid variable
    private Void visitIndexAccessExpr(JmmNode array, SymbolTable table) {

        //I rewrote ArrayIndexNotIntPass & ArrayAccesOnInt here! @cosmin Thank you @Amanda
        TypeUtils typeUtils = new TypeUtils(table);

        JmmNode child_1 = array.getChild(1);
        String child_1_name = typeUtils.getExprType(child_1).getName();

        JmmNode child_0 = array.getChild(0);

        if (!child_1_name.equals("int")) {
            var message = String.format("Cannot access array/varargs through variable '%s' which is not an int.", child_1.get("name"));
            addNewErrorReport(child_1, message);
        } else if ((!typeUtils.getExprType(child_0).isArray()) && !(typeUtils.getExprType(child_0).getBoolean("isVarargs", false))) {
            System.out.println(child_0.get("name") + typeUtils.getExprType(child_0).isArray());
            var message = String.format("Cannot index variable '%s' because it is not an array or varargs.", child_0.get("name"));
            addNewErrorReport(child_0, message);
        }

        return null;
    }

    // Prevents an array of being composed by different elements
    private Void visitArrayLiteral(JmmNode array, SymbolTable table) {

        if (array.getChildren().isEmpty()) return null;

        var first_type = array.getChild(0).getKind();
        for (var element : array.getChildren()) {
            if (!element.getKind().equals(first_type)) {
                var message = String.format("Array cannot be composed of different types '%s' and '%s'", first_type, element);
                addNewErrorReport(element, message);
                return null;
            }
        }
        return null;

    }


}
