package pt.up.fe.comp2025.analysis.passes;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.comp2025.analysis.AnalysisVisitor;
import pt.up.fe.comp2025.ast.Kind;

public class ObjectAssignment extends AnalysisVisitor {

    private String currentMethod;

    @Override
    protected void buildVisitor() {
        addVisit(Kind.METHOD_DECL, this::visitMethodDecl);
        addVisit(Kind.ASSIGN_STMT, this::visitAssignStmt);
    }

    private Void visitMethodDecl(JmmNode methodDecl, SymbolTable table) {
        currentMethod = methodDecl.get("nameMethod");
        return null;
    }

    private Void visitAssignStmt(JmmNode assignStmt, SymbolTable table) {
        // Get the operands
        JmmNode leftOperand = assignStmt.getChild(0);
        JmmNode rightOperand = assignStmt.getChild(1);

        // Get operand types
        String leftType = leftOperand.getKind();
        String rightType = rightOperand.getKind();

        // Check if the first is an int
        // and second matches the first
        if (leftType.equals(rightType)) {
            if (leftType.equals("VarRefExpr")) {
                leftType = table.getLocalVariables(currentMethod).stream().filter(x -> x.getName().equals(leftOperand.get("name"))).map(Symbol::getType).findFirst().orElse(null).getName().toString();
}
            if (rightType.equals("VarRefExpr")) {
                rightType = table.getLocalVariables(currentMethod).stream().filter(x -> x.getName().equals(rightOperand.get("name"))).map(Symbol::getType).findFirst().orElse(null).getName().toString();
            }

            if (leftType.equals(rightType)) {
                return null;
            }
        }

        // Create error report
        var message = String.format("Assignment expression has type %s and left type %s", leftType, rightType);
        addReport(Report.newError(Stage.SEMANTIC, assignStmt.getLine(), assignStmt.getColumn(), message, null));

        return null;
    }
}
