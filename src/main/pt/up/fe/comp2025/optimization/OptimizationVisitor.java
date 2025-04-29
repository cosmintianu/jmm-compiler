package pt.up.fe.comp2025.optimization;

import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.JmmNodeImpl;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;
import pt.up.fe.comp2025.ast.Kind;
import pt.up.fe.comp2025.ast.TypeUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OptimizationVisitor extends PreorderJmmVisitor<SymbolTable, Void> {

    public Boolean opt = false;
    public Map<String, String> constants = new HashMap<String, String>();

    @Override
    protected void buildVisitor() {
        addVisit(Kind.METHOD_DECL, this::visitMethod);
        addVisit(Kind.WHILE_STMT, this::visitWhile);
        addVisit(Kind.VAR_ASSIGN_STMT, this::constantPropagation);
        addVisit(Kind.VAR_REF_EXPR, this::constantPropagation_varRef);
        addVisit(Kind.BINARY_EXPR, this::constantFolding);
        setDefaultVisit(this::defaultVisit);
    }

    public void optimize(JmmNode rootNode, SymbolTable table){
        visit(rootNode, table);
    }

    private Void defaultVisit(JmmNode node, SymbolTable table) {
        //visitAllChildren(node, table);
        return null;
    }

    private Void visitMethod(JmmNode node, SymbolTable table) {
        constants.clear();
        return null;
    }

    //************ Constant Folding ****************

    public Void visitWhile(JmmNode node, SymbolTable table) {

        var rhs = node.getChild(1); // bracketExpr
        var lhs = node.getChild(0); // condition

//        System.out.println("consts before -> " + constants);

        for (var assign : rhs.getDescendants(Kind.VAR_ASSIGN_STMT)) {
            var child = assign.getChild(0);
            if (child.getKind().equals("VarRefExpr")){
                var child_name = child.get("name");
                constants.remove(child_name);
            }
        }

//        System.out.println("consts after -> " + constants);

        return null;
    }

    // if both sides are literals replace the BinaryExpr with the resulting value
    public Void constantFolding(JmmNode node, SymbolTable table) {

        var rhs = node.getChild(1);
        var lhs = node.getChild(0);

//        System.out.println("rhs -> " + rhs + "lhs -> " + lhs);

        if (lhs.getKind().equals("IntegerLiteral") && rhs.getKind().equals("IntegerLiteral")) {

            var left = Integer.parseInt(lhs.get("value"));
            var right = Integer.parseInt(rhs.get("value"));
            var op = node.get("op");

            JmmNode replacement = getJmmNode(op, left, right);
            node.replace(replacement);
            opt = true;

        } else if (lhs.getKind().equals("BooleanLiteral") && rhs.getKind().equals("BooleanLiteral")) {

            var left = Boolean.parseBoolean(lhs.get("name"));
            var right = Boolean.parseBoolean(rhs.get("name"));
            var op = node.get("op");

            JmmNode replacement = getJmmNode(op,left,right);
            node.replace(replacement);
            opt = true;
        }

        return null;
    }

    private static JmmNode getJmmNode(String op, boolean left, boolean right) {
        String result;

        switch (op) {
            case "&&"-> result = String.valueOf(left && right);
            case "!" -> result = String.valueOf(!left);
            default -> throw new IllegalStateException("Unexpected value: " + op);
        }

        JmmNode replacement = new JmmNodeImpl(List.of("BooleanLiteral"));
        replacement.put("name", result);
        return replacement;
    }

    private static JmmNode getJmmNode(String op, int left, int right) {
        String result;

        switch (op) {
            case "+"-> result = String.valueOf(left + right);
            case "*"-> result = String.valueOf(left * right);
            case "-" -> result = String.valueOf(left - right) ;
            case  "/" -> result = String.valueOf(left / right);
            case "<" -> {
                result = String.valueOf(left < right);
                JmmNode replacement = new JmmNodeImpl(List.of("BooleanLiteral"));
                replacement.put("name", result);
                return replacement;
            }
            default -> throw new IllegalStateException("Unexpected value: " + op);
        }

        JmmNode replacement = new JmmNodeImpl(List.of("IntegerLiteral"));
        replacement.put("value", result);
        return replacement;
    }


    //************ Constant Propagation *************

    public Void constantPropagation(JmmNode node, SymbolTable table) {

        var left_child = node.getChild(0);
        var right_child = node.getChild(1);

        var node_name = left_child.get("name");
        var right_kind = right_child.getKind();

        visit(left_child, table);

        if (right_kind.equals("IntegerLiteral")){
            constants.put(node_name, right_child.get("value"));
        } else if (right_kind.equals("BooleanLiteral")){
            constants.put(node_name, right_child.get("name"));
        } else {
            constants.remove(node_name);
        }

        return null;
    }

    public Void constantPropagation_varRef(JmmNode node, SymbolTable table) {

        var node_name = node.get("name");

        if (constants.containsKey(node_name)){

            String val = constants.get(node_name);

            String bool_or_int = val.equals("true") || val.equals("false")? "BooleanLiteral" : "IntegerLiteral";

            List<String> kindHierarchy = List.of(bool_or_int);
            JmmNode replacement = new JmmNodeImpl(kindHierarchy);

            replacement.put("value", val);

            if (!node.getParent().getKind().equals("VarAssignStmt")){
                opt = true;
                node.replace(replacement);
            }

        }

        return null;
    }



}
