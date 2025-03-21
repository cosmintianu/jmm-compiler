package pt.up.fe.comp2025.analysis.passes;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp2025.analysis.AnalysisVisitor;
import pt.up.fe.comp2025.ast.Kind;
import pt.up.fe.comp2025.ast.TypeUtils;

import java.util.List;

public class Method extends AnalysisVisitor {

    @Override
    protected void buildVisitor() {
        addVisit(Kind.METHOD_DECL, this::visitMethodDeclaration);
    }

    // Prevents varargs from being declared as any argument other than the last
    private Void visitMethodDeclaration(JmmNode method, SymbolTable table) {

        List<JmmNode> parameters = method.getChildren(Kind.PARAM);

        for (int i = 0; i < parameters.size(); i++){

            var param = parameters.get(i).get("nameType");

            //if it is a varargs type and not in the last position
            if ( param.contains("VarargsType") && i!=(parameters.size()-1) ){
                var message = String.format("Varargs '%s' must be the last parameter in the method declaration.", parameters.get(i).get("name"));
                addNewErrorReport(parameters.get(i), message);
            }
        }

        return null;
    }

}
