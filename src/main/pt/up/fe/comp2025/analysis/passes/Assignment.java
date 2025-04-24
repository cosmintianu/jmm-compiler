package pt.up.fe.comp2025.analysis.passes;

import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.comp2025.analysis.AnalysisVisitor;
import pt.up.fe.comp2025.ast.Kind;
import pt.up.fe.comp2025.ast.TypeUtils;

public class Assignment extends AnalysisVisitor {

    private String currentMethod;

    @Override
    protected void buildVisitor() {
        addVisit(Kind.METHOD_DECL, this::visitMethodDecl);
        addVisit(Kind.VAR_ASSIGN_STMT, this::visitAssignStmt);
        addVisit(Kind.CLASS_DECL, this::visitClassDecl);
    }

    private Void visitMethodDecl(JmmNode methodDecl, SymbolTable table) {
        currentMethod = methodDecl.get("nameMethod");
        return null;
    }

    private Void visitClassDecl(JmmNode classDecl, SymbolTable table) {
//        System.out.println(classDecl.get("name"));

        return null;
    }

    private Void visitAssignStmt(JmmNode assignStmt, SymbolTable table) {

        // Get operand types
        TypeUtils typeUtils = new TypeUtils(table);
        Type leftType = typeUtils.getExprType(assignStmt.getChild(0));
        Type rightType = typeUtils.getExprType(assignStmt.getChild(1));

//        Debug
//        System.out.println("leftType " + leftType + " & rightType " + rightType);

        // rightType copys leftType value
//        if(rightType.getName().equals("methodExpr_assign")){
//            rightType = typeUtils.getExprType(assignStmt.getChild(0));
//        }

        if (rightType.getName().equals("this")) {
            rightType = new Type(table.getClassName(), false);
        }

        if (leftType.getName().equals("this")) {
            leftType = new Type(table.getClassName(), false);
        }

        // If the types are not compatible, report an error
        if (!isTypeCompatible(rightType, leftType, table, typeUtils)) {
            var message = String.format(
                    "Assignment expression has on the left type %s and right type %s",
                    leftType, rightType
            );
            addReport(Report.newError(Stage.SEMANTIC, assignStmt.getLine(), assignStmt.getColumn(), message, null));
        }

        return null;
    }

    public static boolean isTypeCompatible(Type rightType, Type leftType, SymbolTable table, TypeUtils typeUtils) {

        // Rule -> if a class is being imported, assume the types of the expression where it is used are correct
        if (table.getImports().contains(leftType.getName()) && table.getImports().contains(rightType.getName()))
            return true;

        // Check if the first matches the second
        if (rightType.equals(leftType)) return true;

        // Rule -> If the class extends another class, assume the method exists in one of the super classes
        if (rightType.getName().equals(table.getClassName()) && leftType.getName().equals(table.getSuper()))
            return true;

        // Check if first is IndexAccessExpr is compatible with right type
        if (leftType.getName().equals(Kind.INDEX_ACCESS_EXPR.toString()) && rightType.getName().equals("int")) {
            return true;
        }


        return false;
    }
}
