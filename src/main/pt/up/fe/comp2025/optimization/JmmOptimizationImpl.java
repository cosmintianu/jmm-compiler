package pt.up.fe.comp2025.optimization;

import org.specs.comp.ollir.*;
import org.specs.comp.ollir.inst.*;
import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.ollir.JmmOptimization;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp2025.ConfigOptions;
import pt.up.fe.comp2025.symboltable.JmmSymbolTableBuilder;

import java.util.*;

public class JmmOptimizationImpl implements JmmOptimization {

    private OptimizationVisitor optimizationVisitor;
    Map<String, Integer> varToRegister = new HashMap<>();

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

        if (isOptimized) {

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

        ClassUnit classUnit = ollirResult.getOllirClass();

        for (Method method : classUnit.getMethods()) {
            method.buildCFG();

            Map<Instruction, Set<String>> in = new HashMap<>();
            Map<Instruction, Set<String>> out = new HashMap<>();
            List<Instruction> instructions = method.getInstructions();

            // Initialize maps
            for (Instruction instr : instructions) {
                in.put(instr, new HashSet<>());
                out.put(instr, new HashSet<>());
            }

            // Liveness analysis
            boolean changed;
            do {
                changed = false;

                for (int i = instructions.size() - 1; i >= 0; i--) {
                    Instruction instr = instructions.get(i);

                    Set<String> oldIn = new HashSet<>(in.get(instr));
                    Set<String> oldOut = new HashSet<>(out.get(instr));

                    Set<String> use = getUsedVariables(instr);
                    Set<String> def = getDefinedVariable(instr);

                    Set<String> newOut = new HashSet<>();
                    if (instr.getSucc1() instanceof Instruction succ1) {
                        newOut.addAll(in.get(succ1));
                    }
                    if (instr.getSucc2() instanceof Instruction succ2) {
                        newOut.addAll(in.get(succ2));
                    }

                    Set<String> newIn = new HashSet<>(use);
                    Set<String> temp = new HashSet<>(newOut);
                    temp.removeAll(def);
                    newIn.addAll(temp);

                    in.put(instr, newIn);
                    out.put(instr, newOut);

                    if (!newIn.equals(oldIn) || !newOut.equals(oldOut)) {
                        changed = true;
                    }
                }

            } while (changed);

            // Build interference graph
            Map<String, Set<String>> interferenceGraph = new HashMap<>();
            for (Instruction instr : instructions) {
                Set<String> def = getDefinedVariable(instr);
                Set<String> liveOut = out.get(instr);

                for (String d : def) {
                    interferenceGraph.putIfAbsent(d, new HashSet<>());
                    for (String o : liveOut) {
                        if (!o.equals(d)) {
                            interferenceGraph.get(d).add(o);
                            interferenceGraph.putIfAbsent(o, new HashSet<>());
                            interferenceGraph.get(o).add(d);
                        }
                    }
                }
            }

            // Graph coloring
            Map<String, Integer> regMap = colorGraph(interferenceGraph);

            // Apply register allocation to varTable
            for (Map.Entry<String, Descriptor> entry : method.getVarTable().entrySet()) {
                String var = entry.getKey();
                if (regMap.containsKey(var)) {
                    entry.getValue().setVirtualReg(regMap.get(var));
                }
            }
        }

        return ollirResult;
    }

    private Map<String, Integer> colorGraph(Map<String, Set<String>> graph) {
        Map<String, Integer> colors = new HashMap<>();
        Stack<String> stack = new Stack<>();
        Map<String, Set<String>> copy = new HashMap<>();

        for (var entry : graph.entrySet()) {
            copy.put(entry.getKey(), new HashSet<>(entry.getValue()));
        }

        while (!copy.isEmpty()) {
            String remove = null;
            for (String node : copy.keySet()) {
                if (copy.get(node).size() < 1000) { // assuming infinite registers
                    remove = node;
                    break;
                }
            }

            if (remove == null) break;

            stack.push(remove);
            for (String neighbor : copy.get(remove)) {
                copy.get(neighbor).remove(remove);
            }
            copy.remove(remove);
        }

        while (!stack.isEmpty()) {
            String var = stack.pop();
            Set<Integer> neighborColors = new HashSet<>();
            for (String neighbor : graph.getOrDefault(var, Set.of())) {
                if (colors.containsKey(neighbor)) {
                    neighborColors.add(colors.get(neighbor));
                }
            }

            int color = 0;
            while (neighborColors.contains(color)) color++;
            colors.put(var, color);
        }

        return colors;
    }


//    public Map<Integer, Set<String>> computeLiveness(Method method, ClassUnit classUnit) {
//        Map<Instruction, Set<String>> in = new HashMap<>();
//        Map<Instruction, Set<String>> out = new HashMap<>();
//
//        List<Instruction> instructions = method.getInstructions();
//
//        for (Instruction instruction : instructions) {
//            in.put(instruction, new HashSet<>());
//            out.put(instruction, new HashSet<>());
//        }
//
//        boolean changed;
//        do {
//            changed = false;
//
//            for (int i = instructions.size() - 1; i >= 0; i--) {
//                Instruction instr = instructions.get(i);
//
//                Set<String> oldIn = new HashSet<>(in.get(instr));
//                Set<String> oldOut = new HashSet<>(out.get(instr));
//
//                Set<String> use = getUsedVariables(instr);
//                Set<String> def = getUsedVariables(instr);
//
//                Set<String> newOut = new HashSet<>();
//                if (instr.getSucc1() instanceof Instruction succ1) {
//                    newOut.addAll(in.get(succ1));
//                }
//                if (instr.getSucc2() instanceof Instruction succ2) {
//                    newOut.addAll(in.get(succ2));
//                }
//
//                Set<String> newIn = new HashSet<>(use);
//                Set<String> temp = new HashSet<>(newOut);
//                temp.removeAll(def);
//                newIn.addAll(temp);
//
//                in.put(instr, newIn);
//                out.put(instr, newOut);
//
//                if (!newIn.equals(oldIn) || !newOut.equals(oldOut)) {
//                    changed = true;
//                }
//            }
//        } while (changed);
//
//
//        return liveOut; // or liveIn if needed
//    }

    private Set<String> getUsedVariables(Instruction instr) {
        Set<String> used = new HashSet<>();

        switch (instr.getInstType()) {
            case ASSIGN -> {
                AssignInstruction assign = (AssignInstruction) instr;
                Instruction rhs = assign.getRhs();
                used.addAll(getUsedVariables(rhs));
            }
            case BINARYOPER -> {
                BinaryOpInstruction binOp = (BinaryOpInstruction) instr;
                Element left = binOp.getLeftOperand();
                Element right = binOp.getRightOperand();
                if (left instanceof Operand && !left.isLiteral()) used.add(((Operand) left).getName());
                if (right instanceof Operand && !right.isLiteral()) used.add(((Operand) right).getName());
            }
            case UNARYOPER -> {
                UnaryOpInstruction unOp = (UnaryOpInstruction) instr;
                Element operand = unOp.getOperand();
                if (operand instanceof Operand && !operand.isLiteral()) used.add(((Operand) operand).getName());
            }
            case NOPER -> {
                SingleOpInstruction sop = (SingleOpInstruction) instr;
                Element operand = sop.getSingleOperand();
                if (operand instanceof Operand && !operand.isLiteral()) used.add(((Operand) operand).getName());
            }
            case CALL -> {
                CallInstruction call = (CallInstruction) instr;
                for (Element arg : call.getOperands()) {
                    if (arg instanceof Operand && !arg.isLiteral()) used.add(((Operand) arg).getName());
                }
            }
            case BRANCH -> {
                CondBranchInstruction cond = (CondBranchInstruction) instr;
                for (Element condOp : cond.getOperands()) {
                    if (condOp instanceof Operand && !condOp.isLiteral()) used.add(((Operand) condOp).getName());
                }
            }
            case GETFIELD -> {
                GetFieldInstruction gfield = (GetFieldInstruction) instr;
                Element obj = gfield.getObject();
                if (obj instanceof Operand && !obj.isLiteral()) used.add(((Operand) obj).getName());
            }
            case PUTFIELD -> {
                PutFieldInstruction pfield = (PutFieldInstruction) instr;
                Element obj = pfield.getObject();
                Element value = pfield.getValue();
                if (obj instanceof Operand && !obj.isLiteral()) used.add(((Operand) obj).getName());
                if (value instanceof Operand && !value.isLiteral()) used.add(((Operand) value).getName());
            }
        }

        return used;
    }


    private Set<String> getDefinedVariable(Instruction instr) {
        Set<String> def = new HashSet<>();
        if (instr instanceof AssignInstruction assign) {
            Element lhs = assign.getDest();
            if (lhs instanceof Operand && !lhs.isLiteral()) {
                def.add(((Operand) lhs).getName());
            }
        }
        return def;
    }

}

