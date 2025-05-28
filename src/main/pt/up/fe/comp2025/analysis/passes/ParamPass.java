package pt.up.fe.comp2025.analysis.passes;

import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp2025.analysis.AnalysisVisitor;
import pt.up.fe.comp2025.ast.Kind;

import java.util.HashSet;
import java.util.Set;

public class ParamPass extends AnalysisVisitor {
    Set<String> alreadyDeclaredParams = new HashSet<>();

    @Override
    protected void buildVisitor() {
        addVisit(Kind.PARAM, this::visitParam);
    }

    private Void visitParam(JmmNode param, SymbolTable table) {
        if (alreadyDeclaredParams.contains(param.get("name"))) {
            addNewErrorReport(param, "Parameter '" + param.get("name") + "' already exists.");
        }
        alreadyDeclaredParams.add(param.get("name"));
//        System.out.println(param);
        return null;
    }
}
