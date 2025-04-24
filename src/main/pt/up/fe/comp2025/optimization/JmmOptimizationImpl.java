package pt.up.fe.comp2025.optimization;

import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.ollir.JmmOptimization;
import pt.up.fe.comp.jmm.ollir.OllirResult;

import java.util.Collections;

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
    public JmmSemanticsResult optimize(JmmSemanticsResult semanticsResult) {

        var config = semanticsResult.getConfig();
        var isOptimized = config.getOrDefault("optimize", "false");

        //TODO: Do your AST-based optimizations here

        //TODO -> Constant Propagation
        // When it is an VarAssignStmt, check if child0 it is Literal and substitute constant for value
        // When it is VarRefExpr, do the same

        //TODO -> Constant Folding
        // When it is a BinaryExpr, if both sides are literals replace the BinaryExpr with the resulting value

        return semanticsResult;
    }

    @Override
    public OllirResult optimize(OllirResult ollirResult) {

        //TODO: Do your OLLIR-based optimizations here

        return ollirResult;
    }


}
