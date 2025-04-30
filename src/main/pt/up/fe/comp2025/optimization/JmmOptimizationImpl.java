package pt.up.fe.comp2025.optimization;

import org.specs.comp.ollir.Descriptor;
import org.specs.comp.ollir.Method;
import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.JmmNodeImpl;
import pt.up.fe.comp.jmm.ollir.JmmOptimization;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp2025.ConfigOptions;
import pt.up.fe.comp2025.symboltable.JmmSymbolTableBuilder;

import java.util.*;

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

        //TODO: OLLIR-based optimizations here

        var config = ollirResult.getConfig();
        int n = ConfigOptions.getRegisterAllocation(config);

        //The compiler will use as many variables as originally present in the OLLIR representation
        if (n == -1)
            return ollirResult;

        //**************  Implement Graph Coloring Heuristic *****************
        var ollirClass = ollirResult.getOllirClass();
        ArrayList<Method> methodList = ollirClass.getMethods();

        final Set<Integer> registers = new HashSet<>();

        // each OLLIR method has to reflect the new register allocation
        for (Method method : methodList) {
            // maps names of variables to a Descriptor
            Map<String, Descriptor> varTable = method.getVarTable();

            var instructions = method.getInstructions();
            for (var i = method.getInstructions().size(); i > 0; i--) {
                var current_instruction = instructions.get(i);

                //Se for um assign, def é o lado esquerdo
                //Use é oq é usado do lado direito
                //In é Use + (out - def)
                //Out é uniao dos ins que vem depois de uma instruçao
            }

            //Build interference map
            Map<String, String> interference_graph = new HashMap<>();

        }

        var get_register = ollirResult.getConfig().get("registerAllocation");



        return ollirResult;
    }


}
