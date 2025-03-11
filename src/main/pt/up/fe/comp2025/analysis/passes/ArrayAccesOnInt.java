package pt.up.fe.comp2025.analysis.passes;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.comp2025.analysis.AnalysisVisitor;
import pt.up.fe.comp2025.ast.Kind;

public class ArrayAccesOnInt extends AnalysisVisitor {

    private String currentMethod;

    @Override
    protected void buildVisitor() {
        addVisit(Kind.METHOD_DECL, this::visitMethodDecl);
        addVisit(Kind.ARRAY_ACCESS_EXPR, this::visitArrayAccessExpr);
    }

    private Void visitMethodDecl(JmmNode method, SymbolTable table) {
        currentMethod = method.get("nameMethod");
        return null;
    }

    private Void visitArrayAccessExpr(JmmNode arrayAccessExpr, SymbolTable table) {

        // Check if it exists as a parameter or var declaration with the same name
        JmmNode varRefExpr = arrayAccessExpr.getChild(0);

        var varRefName = varRefExpr.get("name");

        // Var is an array, return
        if ((table.getLocalVariables(currentMethod).stream()
                .filter(var -> var.getName().equals(varRefName))
                .map(Symbol::getType)
                .findFirst()
                .orElse(null)).isArray()) {
            return null;
        }

        // Create error report
        var message = String.format("Variable '%s' is not an array.", varRefName);
        addReport(Report.newError(
                Stage.SEMANTIC,
                varRefExpr.getLine(),
                varRefExpr.getColumn(),
                message,
                null)
        );

        return null;
    }
}