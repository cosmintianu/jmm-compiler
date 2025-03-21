package pt.up.fe.comp2025.analysis.passes;

import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp2025.analysis.AnalysisVisitor;
import pt.up.fe.comp2025.ast.Kind;
import pt.up.fe.comp2025.ast.TypeUtils;

public class BinaryExpr extends AnalysisVisitor {

    @Override
    protected void buildVisitor() {
        addVisit(Kind.BINARY_EXPR, this::visitBinaryExpr);
    }

    private Void visitBinaryExpr(JmmNode binaryExpr, SymbolTable table) {
        // Debug
        System.out.println("Checking Binary Expression");

        TypeUtils typeUtils = new TypeUtils(table);

        // Get op and operands types
        Type opType = typeUtils.getExprType(binaryExpr);
        Type leftOperandType = typeUtils.getExprType(binaryExpr.getChild(0));
        Type rightOperandType = typeUtils.getExprType(binaryExpr.getChild(1));

        // Debug
//        System.out.println("expr type: " + opType);
//        System.out.println("expr type: " + leftOperandType);
//        System.out.println("expr type: " + rightOperandType);

        // Check int operations
        if (opType.getName().equals("int") || binaryExpr.get("op").equals("<")) {
            if (leftOperandType.getName().equals("int") && rightOperandType.getName().equals("int")
                    && leftOperandType.isArray() == rightOperandType.isArray()) {
                return null;
            }
        }

        // Check boolean operations
        if (opType.getName().equals("boolean") && !binaryExpr.get("op").equals("<")) {
            if (leftOperandType.getName().equals("boolean") && rightOperandType.getName().equals("boolean")) {
                return null;
            }
        }

        // Create error report
        addNewErrorReport(binaryExpr, "Binary expression has the left type " +
                leftOperandType.getName() + " and left type " + rightOperandType.getName());

        return null;
    }
}
