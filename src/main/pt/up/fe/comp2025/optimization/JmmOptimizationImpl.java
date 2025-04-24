package pt.up.fe.comp2025.optimization;

import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.ollir.JmmOptimization;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp2025.ConfigOptions;

import java.util.Collections;
import java.util.Map;

public class JmmOptimizationImpl implements JmmOptimization {

    @Override
    public OllirResult toOllir(JmmSemanticsResult semanticsResult) {

        // Create visitor that will generate the OLLIR code
        var visitor = new OllirGeneratorVisitor(semanticsResult.getSymbolTable());

        // Visit the AST and obtain OLLIR code
        var ollirCode = visitor.visit(semanticsResult.getRootNode());

        //System.out.println("\nOLLIR:\n\n" + ollirCode);

        return new OllirResult(semanticsResult, ollirCode, Collections.emptyList());
    }

    @Override
    //AST-based optimizations here
    public JmmSemanticsResult optimize(JmmSemanticsResult semanticsResult) {

        Map<String, String> config = semanticsResult.getConfig();
        var isOptimized = ConfigOptions.getOptimize(config);

        if (!isOptimized)
        {
            var rootNode = semanticsResult.getRootNode();
            //TODO -> Constant Propagation

            // When it is an VarAssignStmt, check if child0 it is Literal and substitute constant for value
            if (rootNode.getKind().equals("VarAssignStmt")){
                System.out.println("AAAAAAAAAAAAAAAAAAAAAAAAAAAAA");

                var root_name = rootNode.get("name");
                var child = rootNode.getChild(0);
                var child_kind = child.getKind();

                if (child_kind.equals("IntegerLiteral")){
                    config.put(root_name, child.get("value"));
                } else if (child_kind.equals("BooleanLiteral")){
                    config.put(root_name, child.get("name"));
                } else {
                    config.remove(root_name);
                }
            }
            // When it is VarRefExpr, do the same
            else if (rootNode.getKind().equals("VarRefExpr")){

            }



            //TODO -> Constant Folding
            // When it is a BinaryExpr, if both sides are literals replace the BinaryExpr with the resulting value
        }


        return semanticsResult;
    }

    @Override
    public OllirResult optimize(OllirResult ollirResult) {

        //TODO: Do your OLLIR-based optimizations here

        return ollirResult;
    }


}
