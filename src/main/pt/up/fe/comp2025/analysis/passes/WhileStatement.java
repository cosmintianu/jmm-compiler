package pt.up.fe.comp2025.analysis.passes;

import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp2025.analysis.AnalysisVisitor;
import pt.up.fe.comp2025.ast.Kind;
import pt.up.fe.comp2025.ast.TypeUtils;

public class WhileStatement extends AnalysisVisitor {

    private String currentMethod;

    @Override
    protected void buildVisitor() {
        addVisit(Kind.WHILE_STMT, this::visitWhileStmt);
        addVisit(Kind.METHOD_DECL, this::visitMethodDecl);
    }

    private Void visitMethodDecl(JmmNode methodDecl, SymbolTable table) {
        currentMethod = methodDecl.get("methodName");
        return null;
    }

    private Void visitWhileStmt(JmmNode whileStmt, SymbolTable symbolTable) {
        TypeUtils typeUtils = new TypeUtils(symbolTable);

        JmmNode binaryExpr = whileStmt.getChild(0);

        if (typeUtils.getExprType(binaryExpr).getName().equals("boolean")) {
            return null;
        }

        var message = String.format("The while statement doesn't have the condition of type boolean.");

        addNewErrorReport(whileStmt, message);
        return null;
    }
}
