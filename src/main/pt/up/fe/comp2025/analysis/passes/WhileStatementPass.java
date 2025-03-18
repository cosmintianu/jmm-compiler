package pt.up.fe.comp2025.analysis.passes;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.comp2025.analysis.AnalysisVisitor;
import pt.up.fe.comp2025.ast.Kind;

public class WhileStatementPass extends AnalysisVisitor {

    private String currentMethod;

    @Override
    protected void buildVisitor() {
        addVisit(Kind.WHILE_STMT, this::visitWhileStmt);
        addVisit(Kind.METHOD_DECL, this::visitMethodDecl);
    }

    private Void visitMethodDecl(JmmNode methodDecl, SymbolTable table) {
        currentMethod = methodDecl.get("nameMethod");
        return null;
    }

    private Void visitWhileStmt(JmmNode whileStmt, SymbolTable symbolTable) {
        JmmNode child = whileStmt.getChild(0);

        System.out.println(child);


        if (child.getKind().equals(Kind.VAR_REF_EXPR.toString())) {
            var aux = symbolTable.getLocalVariables(currentMethod).
                    stream().filter(x -> x.getName().equals(child.get("name")))
                    .map(Symbol::getType).findFirst().orElse(null);
            if (aux != null) {
                if (aux.getName().equals(Kind.ASSIGN_STMT.toString())) {

                    return null;
                }
            }
        }

        var message = String.format("The while statement doesn't have the condition of type boolean.");
        addReport(Report.newError(
                Stage.SEMANTIC,
                whileStmt.getLine(),
                whileStmt.getColumn(),
                message,
                null)
        );


        return null;
    }
}
