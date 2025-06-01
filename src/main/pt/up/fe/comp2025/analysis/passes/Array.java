package pt.up.fe.comp2025.analysis.passes;

import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp2025.analysis.AnalysisVisitor;
import pt.up.fe.comp2025.ast.Kind;
import pt.up.fe.comp2025.ast.TypeUtils;

import java.util.HashMap;
import java.util.Map;

public class Array extends AnalysisVisitor {

    private Map<String, Integer> arrayCapacities = new HashMap<>();

    @Override
    protected void buildVisitor() {
        addVisit(Kind.ARRAY_INIT_STMT, this::visitArrayInitStmt);
        addVisit(Kind.ARRAY_LITERAL, this::visitArrayLiteral);
        addVisit(Kind.INDEX_ACCESS_EXPR, this::visitIndexAccessExpr);
        addVisit(Kind.ARRAY_ASSIGN_STMT, this::visitArrayAssign);
        addVisit(Kind.VAR_ASSIGN_STMT, this::visitVarAssignStmt);
        addVisit(Kind.NEW_ARRAY_EXPR, this::visitNewArrayExpr);
    }

    // Handle regular assignment statements that might initialize arrays
    private Void visitVarAssignStmt(JmmNode assignStmt, SymbolTable symbolTable) {
        System.out.println("DEBUG: Visiting var assignment statement: " + assignStmt);

        if (assignStmt.getChildren().size() >= 2) {
            JmmNode target = assignStmt.getChild(0);
            JmmNode value = assignStmt.getChild(1);

            System.out.println("DEBUG: Assignment target: " + target + ", value: " + value);
            System.out.println("DEBUG: Value kind: " + value.getKind());

            // Check if the value is an array creation expression
            if (value.getKind().equals(Kind.NEW_ARRAY_EXPR.toString())) {

                String arrayName = target.get("name");

                // Look for the size in the array creation expression
                if (value.getChildren().size() > 0) {
                    JmmNode sizeNode = value.getChild(0);  // First child should be the size
                    if (sizeNode.getKind().equals(Kind.INTEGER_LITERAL.toString())) {

                        int arraySize = Integer.parseInt(sizeNode.get("value"));
                        arrayCapacities.put(arrayName, arraySize);
                        System.out.println("DEBUG: Stored array capacity: " + arrayName + " = " + arraySize);
                    }
                }
            }
        }

        return null;
    }

    // Handle array creation expressions directly
    private Void visitNewArrayExpr(JmmNode newArrayExpr, SymbolTable symbolTable) {
        return null;
    }

    private Void visitArrayAssign(JmmNode node, SymbolTable symbolTable) {

        //Check capacity
        var array_name = node.get("name");
        var actualCapacity = arrayCapacities.get(array_name);
        if (actualCapacity == null) {
            return null;
        }

        if (actualCapacity == 0) {
            addNewErrorReport(node, "Array cannot have defined capacity of zero");
        }

        var index_node = node.getChild(0);
        var indexAccessed = 0;

        if (index_node.getKind().equals("IntegerLiteral")) {
            indexAccessed = Integer.valueOf(node.getChild(0).get("value"));
        } else if (index_node.getKind().equals("VarRefExpr")) {
            //TODO: handle this situation
            indexAccessed = 2;
        }

        if (indexAccessed > actualCapacity - 1) {
            addNewErrorReport(node, "Array has the capacity of  " + actualCapacity +
                    " while " + indexAccessed + " is trying to be accessed.");
            return null;
        }

        return null;
    }

    // Store the capacity of arrays when initialised
    private Void visitArrayInitStmt(JmmNode arrayInitStmt, SymbolTable symbolTable) {
        JmmNode capacity = arrayInitStmt.getChild(0);

        //It is a VarAssignStmt
        var parent = arrayInitStmt.getParent();
        var array_name = parent.getChild(0).get("name");

        // Store the capacities of all initialised arrays
        arrayCapacities.put(array_name, Integer.valueOf(capacity.get("value")));

        System.out.println("DEBUG: Array capacities: " + arrayCapacities);

        return null;
    }

    // Prevents trying to access an array through an invalid variable
    private Void visitIndexAccessExpr(JmmNode array, SymbolTable table) {

        //I rewrote ArrayIndexNotIntPass & ArrayAccessOnInt here! @cosmin Thank you, @Amanda
        TypeUtils typeUtils = new TypeUtils(table);

        JmmNode child_1 = array.getChild(1);
        String child_1_name = typeUtils.getExprType(child_1).getName();

        JmmNode child_0 = array.getChild(0);

        if (!child_1_name.equals("int")) {
            var message = String.format("Cannot access array/varargs through variable '%s' which is not an int.", child_1.get("name"));
            addNewErrorReport(child_1, message);
        } else if ((!typeUtils.getExprType(child_0).isArray()) && !(typeUtils.getExprType(child_0).getBoolean("isVarargs", false))) {
            var message = String.format("Cannot index variable '%s' because it is not an array or varargs.", child_0.get("name"));
            addNewErrorReport(child_0, message);
        }

        // Checks error for out of bounds index
        if (child_1.getKind().equals(Kind.INTEGER_LITERAL.toString())) {
            String arrayName = null;

            // Try to get the array name
            if (child_0.hasAttribute("name")) {
                arrayName = child_0.get("name");
            }

            var actualCapacity = arrayCapacities.get(arrayName);

            if (actualCapacity == null) {
                return null;
            }

            int indexAccessed = Integer.parseInt(child_1.get("value"));

            if (indexAccessed > actualCapacity - 1) {
                addNewErrorReport(array, "Array has the capacity of  " + actualCapacity +
                        " while " + indexAccessed + " is trying to be accessed.");
                return null;
            }
        }
        return null;
    }

    // Prevents an array of being composed by different elements
    private Void visitArrayLiteral(JmmNode array, SymbolTable table) {

        if (array.getChildren().isEmpty()) return null;

        var first_type = array.getChild(0).getKind();
        for (var element : array.getChildren()) {
            if (!element.getKind().equals(first_type)) {
                var message = String.format("Array cannot be composed of different types '%s' and '%s'", first_type, element);
                addNewErrorReport(element, message);
                return null;
            }
        }
        return null;
    }
}