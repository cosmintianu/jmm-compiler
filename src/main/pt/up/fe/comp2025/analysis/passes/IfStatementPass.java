package pt.up.fe.comp2025.analysis.passes;

import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;
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

        if (ifStmt.getChildren().isEmpty()) {
            // This indicates a problem with the AST structure itself if an ifStmt node has no children.
            addReport(Report.newError(
                    Stage.SEMANTIC,
                    ifStmt.getLine(),
                    ifStmt.getColumn(),// Assuming JmmNode has getLineCol() or separate getLine()/getCol()
                    "If statement is missing its condition expression.",
                    null)
            );
            return null;
        }

        JmmNode child = ifStmt.getChild(0);
        TypeUtils typeUtils = new TypeUtils(symbolTable);
        Type conditionType = typeUtils.getExprType(child);

        if (typeUtils.isErrorType(conditionType)) {
            addNewErrorReport(ifStmt, "condition not boolean");
            return null;
        }

        if (conditionType != null
                && conditionType.getName().equals("boolean")
                && !conditionType.isArray()) {

            return null;
        } else {
            // The condition's type is not boolean (and wasn't an earlier error).
            String actualTypeStr = "unknown";
            if (conditionType != null) {
                actualTypeStr = conditionType.getName() + (conditionType.isArray() ? "[]" : "");
            }

            addReport(Report.newError(
                    Stage.SEMANTIC,
                    child.getLine(),
                    child.getColumn(), // Report on the condition expression itself
                    String.format("If statement condition must evaluate to 'boolean', but found type '%s'.", actualTypeStr),
                    null)
            );
            return null;
        }

//        if (child.getKind().equals(Kind.VAR_REF_EXPR.toString())) {
//            var varType = typeUtils.getExprType(child);
//            if (varType.getName().equals("boolean")) {
//                return null;
//            }
//        }
//
//        if ("&&<>".contains(child.get("op"))) {
//            return null;
//        }
        
    }

}
