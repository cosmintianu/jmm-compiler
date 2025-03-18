package pt.up.fe.comp2025.analysis.passes;

import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.comp2025.analysis.AnalysisVisitor;
import pt.up.fe.comp2025.ast.Kind;

public class CompatibleOperandsBinaryExpr extends AnalysisVisitor {
    @Override
    protected void buildVisitor() {
        addVisit(Kind.BINARY_EXPR, this::visitBinaryExpr);
    }

    private Void visitBinaryExpr(JmmNode binaryExpr, SymbolTable table) {
        // Get the operands
        JmmNode leftOperand = binaryExpr.getChild(0);
        JmmNode rightOperand = binaryExpr.getChild(1);

        // Get operand types
        String leftType = leftOperand.getKind();
        String rightType = rightOperand.getKind();

        // Check if the first is an int
        // and second matches the first

        // TO DO: AND,OR
        if (leftType.equals(Kind.INTEGER_LITERAL.toString()) &&
                leftType.equals(rightType)) {
            return null;
        }

        // Create error report
        var message = String.format("Binary expression has the left type %s and left type %s", leftType, rightType);
        addReport(Report.newError(
                Stage.SEMANTIC,
                binaryExpr.getLine(),
                binaryExpr.getColumn(),
                message,
                null)
        );

        return null;
    }
}
