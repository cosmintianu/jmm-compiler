package pt.up.fe.comp2025.analysis.passes;

import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp2025.analysis.AnalysisVisitor;
import pt.up.fe.comp2025.ast.Kind;

import java.util.HashSet;
import java.util.Set;

public class ImportPass extends AnalysisVisitor {
    Set<String> alreadyDeclaredImports = new HashSet<>();

    @Override
    protected void buildVisitor() {
        addVisit(Kind.IMPORT_DECL, this::visitImportDecl);
    }

    private Void visitImportDecl(JmmNode importDecl, SymbolTable table) {

        String importName = importDecl.get("ID");
        if (alreadyDeclaredImports.contains(importName)) {
            addNewErrorReport(importDecl, "Import '" + importName + "' already exists.");
        }
        alreadyDeclaredImports.add(importName);
        return null;
    }
}
