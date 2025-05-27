package pt.up.fe.comp2025.analysis.passes;

import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp2025.analysis.AnalysisVisitor;
import pt.up.fe.comp2025.ast.Kind;
import pt.up.fe.specs.util.SpecsCheck;

/**
 * Checks if the type of the expression in a return statement is compatible with the method return type.
 *
 * @author JBispo
 */
public class UndeclaredVariable extends AnalysisVisitor {

    private String currentMethod;
    private String currentClass;

    @Override
    public void buildVisitor() {
        addVisit(Kind.CLASS_DECL, this::visitClassDecl);
        addVisit(Kind.METHOD_DECL, this::visitMethodDecl);
        addVisit(Kind.VAR_REF_EXPR, this::visitVarRefExpr);
        addVisit(Kind.CLASS_TYPE, this::visitClassType);
    }

    private Void visitClassDecl(JmmNode classDecl, SymbolTable symbolTable) {
        currentClass = classDecl.get("name");
//        System.out.println("Class " + currentClass);
        return null;
    }

    private Void visitMethodDecl(JmmNode method, SymbolTable table) {
        currentMethod = method.get("methodName");

        return null;
    }

    private Void visitClassType(JmmNode classType, SymbolTable table) {
        var className = classType.get("name");

        if (className.equals("String")) {
            return null;
        }

        // Check if the object has the same class as the current one
        if (currentClass.equals(className)) {
            return null;
        }

        // Check if the class name is an imported class
        if (table.getImports().stream().map(importDecl -> importDecl.substring(importDecl.lastIndexOf('.') + 1)) // Extract simple class name
                .anyMatch(importedClass -> importedClass.equals(className))) {
            return null;
        }

        addNewErrorReport(classType, "Class type " + className + " does not exist.");

        return null;
    }

    private Void visitVarRefExpr(JmmNode varRefExpr, SymbolTable table) {
        SpecsCheck.checkNotNull(currentMethod, () -> "Expected current method to be set");

        // Check if exists a parameter or variable declaration with the same name as the variable reference
        var varRefName = varRefExpr.get("name");

        // Var is a declared local variable, return
        if (table.getLocalVariables(currentMethod).stream().anyMatch(varDecl -> varDecl.getName().equals(varRefName))) {
            return null;
        }

        // Var is a parameter, return
        if (table.getParameters(currentMethod).stream().anyMatch(param -> param.getName().equals(varRefName))) {
            return null;
        }

        // Check if the variable is a global field
        if (table.getFields().stream().anyMatch(field -> field.getName().equals(varRefName))) {
            return null;
        }

        // Check if variable is imported
        if (table.getImports().stream().map(importDecl -> importDecl.substring(importDecl.lastIndexOf('.') + 1)) // Extract simple class name
                .anyMatch(importedClass -> importedClass.equals(varRefName))) {
            return null;
        }

        // Create error report
        addNewErrorReport(varRefExpr, "Variable " + varRefName + " does not exist.");
        return null;
    }
}
