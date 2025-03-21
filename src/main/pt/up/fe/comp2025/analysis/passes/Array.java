package pt.up.fe.comp2025.analysis.passes;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.comp2025.analysis.AnalysisVisitor;
import pt.up.fe.comp2025.ast.Kind;

public class Array extends AnalysisVisitor{

    @Override
    protected void buildVisitor() {
        addVisit(Kind.ARRAY_LITERAL, this::visitArrayLiteral);
    }

    //Prevents an array of being composed by different elements
    private Void visitArrayLiteral(JmmNode arrayExpr, SymbolTable table) {

        if (arrayExpr.getChildren().isEmpty()) return null;

        var first_type = arrayExpr.getChild(0).getKind();
        for (var element : arrayExpr.getChildren()) {
            if (!element.getKind().equals(first_type)) {
                var message = String.format("Array cannot be composed of different types '%s' and '%s'", first_type, element);
                addNewReport(element, message);
                return null;
            }
        }
        return null;

    }
}
