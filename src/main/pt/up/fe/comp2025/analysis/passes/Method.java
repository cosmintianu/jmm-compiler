package pt.up.fe.comp2025.analysis.passes;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp2025.analysis.AnalysisVisitor;
import pt.up.fe.comp2025.ast.Kind;
import pt.up.fe.comp2025.ast.TypeUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Method extends AnalysisVisitor {

    private boolean isMethodStatic;
    private String className;
    // Map to hold the parameters types for all methods
    private Map<String, List<Type>> methodParamsMap = new HashMap<>();


    @Override
    protected void buildVisitor() {
        addVisit(Kind.CLASS_DECL, this::visitClassDecl);
        addVisit(Kind.METHOD_DECL, this::visitMethodDeclaration);
        addVisit(Kind.METHOD_CALL_EXPR, this::visitMethodCallExpr);
    }

    private Void visitClassDecl(JmmNode classDecl, SymbolTable symbolTable) {
        className = classDecl.get("name");
        return null;
    }

    private Void visitMethodDeclaration(JmmNode method, SymbolTable table) {

        isMethodStatic = Boolean.parseBoolean(method.get("isStatic"));

        String methodName = method.get("nameMethod");

//        System.out.println("The method: " + methodName);

        List<JmmNode> parameters = method.getChildren(Kind.PARAM);

        List<Type> paramTypes = new ArrayList<>();

        for (Symbol param : table.getParameters(methodName)) {
            //System.out.println("param: " + param);
            paramTypes.add(param.getType());
        }

        // Add for each method the list with their parameter types in the map
        methodParamsMap.put(methodName, paramTypes);

        // Prevents varargs from being declared as any argument other than the last
        for (int i = 0; i < parameters.size(); i++) {

            var param = parameters.get(i).get("nameType");

            //if it is a varargs type and not in the last position
            if (param.contains("VarargsType") && i != (parameters.size() - 1)) {
                var message = String.format("Varargs '%s' must be the last parameter in the method declaration.", parameters.get(i).get("name"));
                addNewErrorReport(parameters.get(i), message);
            }
        }


        return null;
    }

    // Check undeclared method pass
    // Moved it here @Amanda Oks, got it! @cosmin
    private Void visitMethodCallExpr(JmmNode methodCallExpr, SymbolTable table) {
        TypeUtils typeUtils = new TypeUtils(table);

        JmmNode varRefExpr = methodCallExpr.getChild(0);

        // Check if the println method is called from io class, supposed static, return
        // Check before varType assignment because getExprType returns null, the io class is imported, cant get type
        if (varRefExpr.get("name").equals("io") && methodCallExpr.get("name").equals("println")) {
            return null;
        }

        Type varType = typeUtils.getExprType(varRefExpr);

        // Check if class is not static, "this" can be used, return
        if (!isMethodStatic && varType.getName().equals("this")) {
            return null;
        }

        // Check if the class of the variable is imported, return
        if (table.getImports().stream().anyMatch(methodName -> methodName.equals(varType.getName()))) {
            return null;
        }

        // Check if the super class of the variable is imported, return
        if (table.getImports().stream().anyMatch(methodName -> methodName.equals(table.getSuper()))) {
            return null;
        }

        // Boolean that allows one the correct report to be added
        boolean printMethodNotDeclaredReport = false;

        // Check if the variable is the same type as the class
        if (varType.getName().equals(className)) {
            if (table.getMethods().stream().anyMatch(methodName -> methodName.equals(methodCallExpr.get("name")))) {

                List<JmmNode> args = methodCallExpr.getChildren();

                // Remove the class variable on which the method is called
                // so we only have the arguments
                args.removeFirst();

                List<Type> argTypes = new ArrayList<>();

                for (JmmNode arg : args) {
                    argTypes.add(typeUtils.getExprType(arg));
                }
                // Find the parameters of the called method
                List<Type> actualParamTypes = methodParamsMap.get(methodCallExpr.get("name"));

                if (actualParamTypes != null) {
                    // Check compatibility
                    for (int i = 0; i < actualParamTypes.size(); i++) {
                        if (!actualParamTypes.get(i).equals(argTypes.get(i))) {
                            addNewErrorReport(methodCallExpr, "Argument on position "
                                    + i + " of type: " + argTypes.get(i) + " doesn't match parameter of type "
                                    + actualParamTypes.get(i) + " in method declaration.");
                        }
                    }
                }

                return null;
            }
            printMethodNotDeclaredReport = true;
            addNewErrorReport(methodCallExpr, "Method " + methodCallExpr.get("name") + " isn't declared in this class.");
        }

//        JmmNode methodParam = methodCallExpr.getChild(1);
//        System.out.println("DAaaaaaaaaaaaaa " + methodParam);

        if (!printMethodNotDeclaredReport) {
            addNewErrorReport(methodCallExpr, "Variable '" + varType.getName() + "' on which a method is called is not declared.");
        }
        return null;
    }
}
