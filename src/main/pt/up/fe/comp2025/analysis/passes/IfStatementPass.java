package pt.up.fe.comp2025.analysis.passes;

import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.comp2025.analysis.AnalysisVisitor;
import pt.up.fe.comp2025.ast.Kind;
import pt.up.fe.comp2025.ast.TypeUtils;

public class IfStatementPass extends AnalysisVisitor {
    @Override
    protected void buildVisitor() {
        addVisit(Kind.IF_STMT, this::visitIfStmt);
    }

    private Void visitIfStmt(JmmNode ifStmt, SymbolTable symbolTable) {
        JmmNode child = ifStmt.getChild(0);

        TypeUtils typeUtils = new TypeUtils(symbolTable);

        if(child.getKind().equals(Kind.VAR_REF_EXPR.toString())){
            var varType = typeUtils.getExprType(child);
            if(varType.getName().equals("boolean")){
                return null;
            }
        }

        if ("&&<>".contains(child.get("op"))) {
            return null;
        }


        var message = String.format("The if statement has the operator: '%s'.", child.get("op"));
        addReport(Report.newError(
                Stage.SEMANTIC,
                ifStmt.getLine(),
                ifStmt.getColumn(),
                message,
                null)
        );


        return null;
    }

}
