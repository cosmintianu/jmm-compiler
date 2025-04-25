package pt.up.fe.comp2025.optimization;

import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.JmmNodeImpl;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;
import pt.up.fe.comp2025.ast.Kind;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OptimizationVisitor extends PreorderJmmVisitor<SymbolTable, Void> {

    public Boolean opt = false;
    public Map<String, String> constants = new HashMap<String, String>();

    @Override
    protected void buildVisitor() {
        addVisit(Kind.VAR_ASSIGN_STMT, this::constantPropagation);
        addVisit(Kind.VAR_REF_EXPR, this::constantPropagation_varRef);

        //TODO -> Constant Folding
        // When it is a BinaryExpr, if both sides are literals replace the BinaryExpr with the resulting value
    }

    public void optimize(JmmNode rootNode, SymbolTable table){
        visit(rootNode, table);
    }

    //************ Constant Propagation *************

    public Void constantPropagation(JmmNode node, SymbolTable table) {

        constants.clear();

        var node_name = node.get("name");
        var child = node.getChild(0);
        var child_kind = child.getKind();

        if (child_kind.equals("IntegerLiteral")){
            constants.put(node_name, child.get("value"));
        } else if (child_kind.equals("BooleanLiteral")){
            constants.put(node_name, child.get("name"));
        } else {
            constants.remove(node_name);
        }

        return null;
    }

    public Void constantPropagation_varRef(JmmNode node, SymbolTable table) {

        constants.clear();

        String node_name = node.get("name");

        if (constants.containsKey(node_name)){

            String val = constants.get(node_name);
            String bool_or_int = val.equals("true") || val.equals("false")? "BooleanLiteral" : "IntegerLiteral";

            List<String> kindHierarchy = List.of(bool_or_int);
            JmmNode replacement = new JmmNodeImpl(kindHierarchy);

            replacement.put("value", val);

            node.replace(replacement);
        }

        return null;
    }
}
