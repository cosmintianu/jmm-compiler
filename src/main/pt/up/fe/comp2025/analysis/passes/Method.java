package pt.up.fe.comp2025.analysis.passes;

import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp2025.analysis.AnalysisVisitor;
import pt.up.fe.comp2025.ast.Kind;
import pt.up.fe.comp2025.ast.TypeUtils;

import java.util.List;

public class Method extends AnalysisVisitor {

    private boolean isMethodStatic;

    @Override
    protected void buildVisitor() {
        addVisit(Kind.METHOD_DECL, this::visitMethodDeclaration);
        addVisit(Kind.METHOD_CALL_EXPR, this::visitMethodCallExpr);
    }

    // Prevents varargs from being declared as any argument other than the last
    private Void visitMethodDeclaration(JmmNode method, SymbolTable table) {
        isMethodStatic = Boolean.parseBoolean(method.get("isStatic"));

        List<JmmNode> parameters = method.getChildren(Kind.PARAM);

        for (int i = 0; i < parameters.size(); i++) {

            var param = parameters.get(i).get("nameType");

            //if it is a varargs type and not in the last position
            if (param.contains("VarargsType") && i != (parameters.size() - 1)) {
                var message = String.format("Varargs '%s' must be the last parameter in the method declaration.", parameters.get(i).get("name"));
                addNewErrorReport(parameters.get(i), message);
            }
        }

        return null;
    }

    // Check undeclared method pass
    // Moved it here @Amanda Oks, got it! @cosmin
    private Void visitMethodCallExpr(JmmNode methodCallExpr, SymbolTable table) {
        TypeUtils typeUtils = new TypeUtils(table);

        JmmNode varRefExpr = methodCallExpr.getChild(0);

//        System.out.println(table.getSuper());
        Type varType = typeUtils.getExprType(varRefExpr);

//        System.out.println("varRefExpr type: " + varType);

        // Check if class is not static "this" can be used, return
        if (!isMethodStatic && varType.getName().equals("this")) {
            return null;
        }

        // Check if the println method is called from io class, supposed static, return
        if (varRefExpr.get("name").equals("io") && methodCallExpr.get("name").equals("println")) {
            return null;
        }

        // Check if the class of the variable is imported, return
        if (table.getImports().stream().anyMatch(methodName -> methodName.equals(varType.getName()))) {
            return null;
        }

        // Check if the super class of the variable is imported, return
        if (table.getImports().stream().anyMatch(methodName -> methodName.equals(table.getSuper()))) {
            return null;
        }


        addNewErrorReport(methodCallExpr, "Class/Super class of " + varType.getName() + " is not imported.");
        return null;
    }

}
