package pt.up.fe.comp2025.analysis.passes;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.comp2025.analysis.AnalysisVisitor;
import pt.up.fe.comp2025.ast.Kind;

public class ObjectAssignment extends AnalysisVisitor {

    private String currentMethod;

    @Override
    protected void buildVisitor() {
        addVisit(Kind.METHOD_DECL, this::visitMethodDecl);
        addVisit(Kind.ASSIGN_STMT, this::visitAssignStmt);
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
        JmmNode leftOperand = assignStmt.getChild(0);
        JmmNode rightOperand = assignStmt.getChild(1);

        // Get operand types
        String leftType = leftOperand.getKind();
        String rightType = rightOperand.getKind();

        if (rightType.equals("NewObjectExpr")) {
            return null;
        }

        // Check if the first is an int
        // and second matches the first
        if (leftType.equals(rightType)) {
            if (leftType.equals("VarRefExpr")) {
                leftType = table.getLocalVariables(currentMethod).stream().filter(x -> x.getName().equals(leftOperand.get("name"))).map(Symbol::getType).findFirst().orElse(null).getName(); //Call to 'toString()' was redundant
            }
            if (rightType.equals("VarRefExpr")) {
                rightType = table.getLocalVariables(currentMethod).stream().filter(x -> x.getName().equals(rightOperand.get("name"))).map(Symbol::getType).findFirst().orElse(null).getName();
//                System.out.println(Optional.of(var.get("nameExtendClass")));
//                System.out.println(table.getSuper().toString());
            }

            // Add verification -> if a class is being imported, assume the types of the expression where it is used are correct
            if (table.getImports().contains(leftType) && table.getImports().contains(rightType)) {
                return null;
            }

            var superClass = table.getSuper();
//            String superClassString;
//            if (superClass != null) {
////              superClassString = superClass.toString();
//            }
            if (superClass != null && leftType.equals(table.getSuper())) {
                return null;
            }

            //TO DO Clean up
        }

        if (rightType.equals("NewArrayExpr")) {
            if (leftType.equals("VarRefExpr")) {
                var actualType = table.getLocalVariables(currentMethod).stream().filter(x -> x.getName().equals(leftOperand.get("name"))).map(Symbol::getType).findFirst();
                if (actualType.get().isArray()) {
                    return null;
                }
            }


        }
//
        // Create error report
        var message = String.format("Assignment expression has on the left type %s and right type %s", leftType, rightType);
        addReport(Report.newError(Stage.SEMANTIC, assignStmt.getLine(), assignStmt.getColumn(), message, null));

        return null;
    }
}
