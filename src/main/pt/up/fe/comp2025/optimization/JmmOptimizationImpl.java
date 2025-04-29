package pt.up.fe.comp2025.optimization;

import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.JmmNodeImpl;
import pt.up.fe.comp.jmm.ollir.JmmOptimization;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp2025.ConfigOptions;
import pt.up.fe.comp2025.symboltable.JmmSymbolTableBuilder;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class JmmOptimizationImpl implements JmmOptimization {

    private OptimizationVisitor optimizationVisitor;

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

        if(isOptimized) {

            this.optimizationVisitor = new OptimizationVisitor();
            this.optimizationVisitor.buildVisitor();

            var rootNode = semanticsResult.getRootNode();

            JmmSymbolTableBuilder tableBuilder = new JmmSymbolTableBuilder();
            SymbolTable table = tableBuilder.build(rootNode);
            int counter = 1;

            do {
                System.out.println("Visiting for the " + counter + " time..");
                counter++;
                this.optimizationVisitor.opt = false;
                this.optimizationVisitor.optimize(rootNode, table);
            } while (this.optimizationVisitor.opt);
        }

        return semanticsResult;
    }

    @Override
    public OllirResult optimize(OllirResult ollirResult) {

        //TODO: Do your OLLIR-based optimizations here

        return ollirResult;
    }


}
