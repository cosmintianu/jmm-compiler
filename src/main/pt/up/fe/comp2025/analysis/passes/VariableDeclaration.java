package pt.up.fe.comp2025.analysis.passes;

import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp2025.analysis.AnalysisVisitor;
import pt.up.fe.comp2025.ast.Kind;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class VariableDeclaration extends AnalysisVisitor {

    private JmmNode currentMethod;
    private List<JmmNode> var_decl;
    private Set<String> alreadyDeclaredFields = new HashSet<>();


    @Override
    public void buildVisitor() {
        addVisit(Kind.METHOD_DECL, this::visitMethodDecl);
        addVisit(Kind.VAR_DECL, this::visitVarDecl);
    }

    private Void visitMethodDecl(JmmNode method, SymbolTable table) {
        currentMethod = method;
        return null;
    }

    private Void visitVarDecl(JmmNode varDecl, SymbolTable table) {

        // Check if the variable is already declared in the current method
//        if (currentMethod != null)
//            if (table.getLocalVariables(currentMethod.get("methodName")).contains(varDecl.get("name"))) {
//                addNewErrorReport(varDecl, "Variable '" + varDecl.get("ID") + "' was already declared in the current method.");
//            }

//        System.out.println(table.getFields());

        // Check if the variable is already declared in the current class
        String name = varDecl.get("name");

        if (alreadyDeclaredFields.contains(name)) {
            addNewErrorReport(varDecl, "Variable '" + varDecl.get("name") + "' was already declared in the current class.");
        }

        alreadyDeclaredFields.add(name);


        if (currentMethod != null) {

            var_decl = currentMethod.getChildren(Kind.VAR_DECL);
            Set<String> duplicates = new HashSet<>();

            for (var element : var_decl) {

                if (!duplicates.add(element.get("name"))) {
                    addNewErrorReport(varDecl, "Variable '" + varDecl.get("name") + "' was defined multiple times.");
                }
            }

        }

        return null;
    }
}
