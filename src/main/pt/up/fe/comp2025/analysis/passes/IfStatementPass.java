package pt.up.fe.comp2025.analysis.passes;

import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp2025.analysis.AnalysisVisitor;
import pt.up.fe.comp2025.ast.Kind;

public class IfStatementPass extends AnalysisVisitor {
    @Override
    protected void buildVisitor() {
//        addVisit(Kind.IF_STMT, this::visitIfStmt);
        addVisit(Kind.VAR_REF_EXPR, this::visitVarRefExpr);
    }

    private Void visitVarRefExpr(JmmNode varRefExpr, SymbolTable symbolTable) {
        System.out.println(varRefExpr);
        return null;
    }

    private Void visitIfStmt(JmmNode ifStmt, SymbolTable symbolTable) {
        JmmNode child = ifStmt.getChild(0);

        String childType = child.getKind().toString();

        if (childType.equals(Kind.BOOLEAN_TYPE.toString())) {
            return null;
        }

//        var message = String.format("The if statement has a type of '%s'.", childType);
//        addReport(Report.newError(
//                Stage.SEMANTIC,
//                ifStmt.getLine(),
//                ifStmt.getColumn(),
//                message,
//                null)
//        );


        return null;
    }

}
