package pt.up.fe.comp2025.analysis.passes;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.comp2025.analysis.AnalysisVisitor;
import pt.up.fe.comp2025.ast.Kind;
import pt.up.fe.comp2025.ast.TypeUtils;

import java.util.List;

public class ObjectAssignment extends AnalysisVisitor {

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
        // Get the operands
        System.out.println(assignStmt);
        System.out.println("children " + assignStmt.getChildren());
        //System.out.println("assign name " + assignStmt.get("name"));

        // Get operand types
        TypeUtils typeUtils = new TypeUtils(table);
        String leftType = typeUtils.getExprType(assignStmt.getChild(0)).toString();
        String rightType = typeUtils.getExprType(assignStmt.getChild(1)).toString();

        System.out.println("rightType: " + rightType);
        System.out.println("leftType: " + leftType);

        System.out.println("visit assign stmt");

        // If the types are not compatible, report an error
        if (!isTypeCompatible(rightType, leftType, table)) {
            var message = String.format(
                    "Assignment expression has on the left type %s and right type %s",
                    leftType, rightType
            );
            addReport(Report.newError(Stage.SEMANTIC, assignStmt.getLine(), assignStmt.getColumn(), message, null));
        }

        return null;
    }

    public static boolean isTypeCompatible( String rightType, String leftType, SymbolTable table) {
        List<String> primitiveTypes = List.of("int", "int[]", "boolean");

        // Check if the first matches the second
        if (rightType.equals(leftType)) return true;

        System.out.println("different");
         return false;

        //To prevent the case from ArrayInitWrong2 - assign an array to a non-array type

        // Rule -> if a class is being imported, assume the types of the expression where it is used are correct
//        if (table.getImports().contains(rightType) && !primitiveTypes.contains(leftType)) return true;
//
//        //Rule -> If the class extends another class, assume the method exists in one of the super classes
//        if (rightType.equals(table.getClassName()) && leftType.equals(table.getSuper())) return true;
//
//        //Not sure about this rule to be honest :p
//        if (rightType.equals("NewObjectExpr")) {
//            return true;
//        }
//
//        return false;
    }

    private String getNodeType(JmmNode node, SymbolTable table) {
        if (node.getKind().equals("VarRefExpr")) {
            return table.getLocalVariables(currentMethod).stream()
                    .filter(x -> x.getName().equals(node.get("name")))
                    .map(Symbol::getType)
                    .findFirst()
                    .orElse(null)
                    .getName();
        }
        return ("unknown kind: " + node.getKind());
    }
}
