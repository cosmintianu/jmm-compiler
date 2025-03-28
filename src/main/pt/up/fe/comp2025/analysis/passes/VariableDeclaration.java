package pt.up.fe.comp2025.analysis.passes;

import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp2025.analysis.AnalysisVisitor;
import pt.up.fe.comp2025.ast.Kind;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class VariableDeclaration extends AnalysisVisitor {

    private String currentMethod;
    private List<JmmNode> var_decl;


    @Override
    public void buildVisitor() {
        addVisit(Kind.METHOD_DECL, this::visitMethodDecl);
        addVisit(Kind.VAR_DECL, this::visitVarDecl);
    }

    private Void visitMethodDecl(JmmNode method, SymbolTable table) {
        currentMethod = method.get("nameMethod");

        var_decl = method.getChildren(Kind.VAR_DECL);

        return null;
    }

    private Void visitVarDecl(JmmNode varDecl, SymbolTable table) {

        Set<String> duplicates = new HashSet<>();

        for (var element : var_decl) {

            if(!duplicates.add(element.get("name"))){
                addNewErrorReport(varDecl, "Variable '" + varDecl.get("name") + "' was defined multiple times.");
            }
        }

        return null;
    }
}
