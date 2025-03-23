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
    private String className;

    @Override
    protected void buildVisitor() {
        addVisit(Kind.CLASS_DECL, this::visitClassDecl);
        addVisit(Kind.METHOD_DECL, this::visitMethodDeclaration);
        addVisit(Kind.METHOD_CALL_EXPR, this::visitMethodCallExpr);
    }

    private Void visitClassDecl(JmmNode classDecl, SymbolTable symbolTable) {
        className = classDecl.get("name");
        return null;
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

        // Check if the println method is called from io class, supposed static, return
        // Check before varType assignment because getExprType returns null, the io class is imported, cant get type
        if (varRefExpr.get("name").equals("io") && methodCallExpr.get("name").equals("println")) {
            return null;
        }

        Type varType = typeUtils.getExprType(varRefExpr);

        // Check if class is not static, "this" can be used, return
        if (!isMethodStatic && varType.getName().equals("this")) {
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

        // Boolean that allows one the correct report to be added
        boolean printMethodNotDeclaredReport = false;

        // Check if the variable is the same type as the class
        if (varType.getName().equals(className)) {
            if (table.getMethods().stream().anyMatch(methodName -> methodName.equals(methodCallExpr.get("name")))) {
                return null;
            }
            printMethodNotDeclaredReport = true;
            addNewErrorReport(methodCallExpr, "Method " + methodCallExpr.get("name") + " isn't declared in this class.");
        }

        if (!printMethodNotDeclaredReport) {
            addNewErrorReport(methodCallExpr, "Variable '" + varType.getName() + "' on which a method is called is not declared.");
        }
        return null;
    }
}
