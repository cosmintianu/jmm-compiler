package pt.up.fe.comp2025.analysis.passes;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.comp2025.analysis.AnalysisVisitor;
import pt.up.fe.comp2025.ast.Kind;

public class ArrayIndexNotIntPass extends AnalysisVisitor {

    private String currentMethod;

    @Override
    protected void buildVisitor() {
        addVisit(Kind.METHOD_DECL, this::visitMethodDecl);
        addVisit(Kind.ARRAY_ACCESS_EXPR, this::visitArrayAccessExpr);

    }

    private Void visitMethodDecl(JmmNode methodDecl, SymbolTable table) {
        currentMethod = methodDecl.get("nameMethod");
        return null;
    }

    private Void visitArrayAccessExpr(JmmNode arrayAccessExpr, SymbolTable table) {

        JmmNode varRefExpr = arrayAccessExpr.getChild(1);
        var varRefName = varRefExpr.get("name");

        if (table.getLocalVariables(currentMethod).stream()
                .filter(var -> var.getName().equals(varRefName))
                .map(Symbol::getType)
                .findFirst()
                .orElse(null).getName()
                .equals(Kind.INTEGER_LITERAL.toString())) {
            return null;
        }

        var message = String.format("Cannot access array through variable '%s' which is not an int.", varRefName);

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
