package pt.up.fe.comp2025.analysis.passes;

import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.comp2025.analysis.AnalysisVisitor;
import pt.up.fe.comp2025.ast.Kind;
import pt.up.fe.specs.util.SpecsCheck;

public class UndeclaredMethodPass extends AnalysisVisitor {

    private String currentMethod;

    @Override
    protected void buildVisitor() {
        addVisit(Kind.METHOD_DECL, this::visitMethodDecl);
        addVisit(Kind.METHOD_CALL_EXPR, this::visitMethodCallExpr);
    }

    private Void visitMethodDecl(JmmNode method, SymbolTable table) {
        currentMethod = method.get("nameMethod");
        return null;
    }

    private Void visitMethodCallExpr(JmmNode methodCallExpr, SymbolTable table) {
        SpecsCheck.checkNotNull(currentMethod, () -> "Expected current method to be set");

        var methodCallExprName = methodCallExpr.get("name");

        if (table.getMethods().stream().anyMatch(methodCallExprName::equals)) {
            return null;
//            System.out.println("DA");
        } else {
//            System.out.println("NU");
        }

        var message = String.format("Method '%s' does not exist.", methodCallExprName);
        addReport(Report.newError(
                Stage.SEMANTIC,
                methodCallExpr.getLine(),
                methodCallExpr.getColumn(),
                message,
                null)
        );


        return null;
    }
}
