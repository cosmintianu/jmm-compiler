package pt.up.fe.comp2025.backend;

import org.specs.comp.ollir.*;
import org.specs.comp.ollir.inst.*;
import org.specs.comp.ollir.tree.TreeNode;
import org.specs.comp.ollir.type.ArrayType;
import org.specs.comp.ollir.type.BuiltinType;
import org.specs.comp.ollir.type.ClassType;
import org.specs.comp.ollir.type.Type;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp2025.JavammParser;
import pt.up.fe.specs.util.classmap.FunctionClassMap;
import pt.up.fe.specs.util.exceptions.NotImplementedException;
import pt.up.fe.specs.util.utilities.StringLines;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Generates Jasmin code from an OllirResult.
 * <p>
 * One JasminGenerator instance per OllirResult.
 */
public class JasminGenerator {

    private static final String NL = "\n";
    private static final String TAB = "   ";
    private static final String SPACE = " ";

    private final OllirResult ollirResult;

    List<Report> reports;

    String code;

    Method currentMethod;
    int currentStack;
    int limitStack;

    private final JasminUtils types;

    private final FunctionClassMap<TreeNode, String> generators;
    private Map<String, String> imports_map = new HashMap<>();

    public JasminGenerator(OllirResult ollirResult) {
        this.ollirResult = ollirResult;

        reports = new ArrayList<>();
        code = null;
        currentMethod = null;

        types = new JasminUtils(ollirResult);

        this.generators = new FunctionClassMap<>();
        generators.put(ClassUnit.class, this::generateClassUnit);
        generators.put(Method.class, this::generateMethod);
        generators.put(AssignInstruction.class, this::generateAssign);
        generators.put(SingleOpInstruction.class, this::generateSingleOp);
        generators.put(LiteralElement.class, this::generateLiteral);
        generators.put(Operand.class, this::generateOperand);
        generators.put(BinaryOpInstruction.class, this::generateBinaryOp);
        generators.put(ReturnInstruction.class, this::generateReturn);
        generators.put(NewInstruction.class, this::generateNewInstruction);
        generators.put(InvokeSpecialInstruction.class, this::generateInvokeSpecial);
        generators.put(Field.class, this::generateField);
    }

    private String apply(TreeNode node) {
        var code = new StringBuilder();

        // Print the corresponding OLLIR code as a comment
        //code.append("; ").append(node).append(NL);

        code.append(generators.apply(node));

        return code.toString();
    }


    public List<Report> getReports() {
        return reports;
    }

    public String build() {

        // This way, build is idempotent
        if (code == null) {
            code = apply(ollirResult.getOllirClass());
        }

        return code;
    }

    private void updateStack(int value) {
        currentStack+=value;
    }

    private String getJasminType(Type type) {

        String operand_type = "";

        if (type instanceof BuiltinType builtinType){
            switch (builtinType.getKind()){
                case INT32 -> operand_type = "I";
                case BOOLEAN -> operand_type = "Z";
                case VOID -> operand_type = "V";
                case STRING -> operand_type = "Ljava/lang/String;";
            }
        }
        //TODO: to be reviewed
        else if (type instanceof ArrayType arrayType) {
            var elementType = getJasminType(arrayType.getElementType());
            //var numDimensions = arrayType.getNumDimensions(); -- We just have one dimension
            operand_type = "[" + elementType;
        }
        else if (type instanceof ClassType classType) {
            switch (classType.getKind()){
                case CLASS -> operand_type = "L" + currentMethod.getClass().getName().toLowerCase() + ";";
                case OBJECTREF -> operand_type = "Ljava/lang/Object;";
                case THIS -> operand_type = "L" + currentMethod.getOllirClass().getClassName() + ";";
            }
        }

        return operand_type;
    }

    private String getType(Type type) {

        String operand_type = "";

        if (type instanceof ArrayType arrayType) {
            operand_type = "..";
        }
        else if (type instanceof ClassType classType) {
            switch (classType.getKind()){
                case CLASS, OBJECTREF -> operand_type = currentMethod.getClass().getName().toLowerCase();
            }
        }

        return operand_type;
    }

    private String generateClassUnit(ClassUnit classUnit) {

        var code = new StringBuilder();

        // generate class name
        var className = ollirResult.getOllirClass().getClassName();
        code.append(".class ").append(className).append(NL).append(NL);

        var fullSuperClass = "java/lang/Object";

        //Build imports table
        for (String entry : ollirResult.getOllirClass().getImports()){
            var import_parts = entry.split("\\.");
            imports_map.put(import_parts[import_parts.length -1],entry.replace(".","/"));
        }

        if (classUnit.getSuperClass() != null) {

            var superClass = classUnit.getSuperClass();
            fullSuperClass = imports_map.get(superClass);

        }

        code.append(".super ").append(fullSuperClass).append(NL).append(NL);

        //Append fields
        for (var field : ollirResult.getOllirClass().getFields()) {
            code.append(generators.apply(field));
        }

        // generate a single constructor method
        var defaultConstructor = """
                ;default constructor
                .method public <init>()V
                    aload_0
                    invokespecial %s/<init>()V
                    return
                .end method
                """.formatted(fullSuperClass);
        code.append(defaultConstructor);

        // generate code for all other methods
        for (var method : ollirResult.getOllirClass().getMethods()) {

            // Ignore constructor, since there is always one constructor
            // that receives no arguments, and has been already added
            // previously
            if (method.isConstructMethod()) {
                continue;
            }

            code.append(apply(method));
        }

        return code.toString();
    }


    private String generateMethod(Method method) {
        //System.out.println("STARTING METHOD " + method.getMethodName());
        // set method
        currentMethod = method;
        currentStack = 0;
        limitStack = 0;

        var code = new StringBuilder();

        // calculate modifier
        var modifier = types.getModifier(method.getMethodAccessModifier());

        var methodName = method.getMethodName();

        if (method.isStaticMethod()) {
            modifier += "static ";
        }

        //Append params
        StringBuilder params = new StringBuilder();
        for (var param : method.getParams()) {
            params.append(getJasminType(param.getType()));
        }

        //Define Return Jasmin Type
        var returnType = "I";
        //TODO: get return using this function is the correct way I think
        // var returnType = generateReturn();

        code.append("\n.method ").append(modifier)
                .append(methodName)
                .append("(" + params + ")" + returnType).append(NL);

        StringBuilder instructions = new StringBuilder();
        for (var inst : method.getInstructions()) {
            var instCode = StringLines.getLines(apply(inst)).stream()
                    .collect(Collectors.joining(NL + TAB, TAB, NL));

            instructions.append(instCode);
        }

        //Calculate Locals
        HashSet<Integer> locals = new HashSet<>();
        for( var key: method.getVarTable().keySet()){
            locals.add(method.getVarTable().get(key).getVirtualReg());
        }
        int local = locals.size();

        // Add limits
        code.append(TAB).append(".limit stack ").append(limitStack).append(NL);
        code.append(TAB).append(".limit locals ").append(local).append(NL);

        code.append(instructions).append(".end method\n");

        // unset method
        currentMethod = null;
        //System.out.println("ENDING METHOD " + method.getMethodName());
        return code.toString();
    }

    private String generateAssign(AssignInstruction assign) {

        var code = new StringBuilder();

        // generate code for loading what's on the right
        code.append(apply(assign.getRhs()));

        // store value in the stack in destination
        var lhs = assign.getDest();

        if (!(lhs instanceof Operand)) {
            throw new NotImplementedException(lhs.getClass());
        }

        var operand = (Operand) lhs;

        // get register
        var reg = currentMethod.getVarTable().get(operand.getName());


        // TODO: Hardcoded for int type, needs to be expanded
        code.append("istore ").append(reg.getVirtualReg()).append(NL);

        return code.toString();
    }

    private String generateSingleOp(SingleOpInstruction singleOp) {
        return apply(singleOp.getSingleOperand());
    }

    private String generateLiteral(LiteralElement literal) {
        return "ldc " + literal.getLiteral() + NL;
    }

    private String generateOperand(Operand operand) {
        // get register
        var reg = currentMethod.getVarTable().get(operand.getName());

        // TODO: Hardcoded for int type, needs to be expanded
        return "iload " + reg.getVirtualReg() + NL;
    }

    private String generateBinaryOp(BinaryOpInstruction binaryOp) {
        var code = new StringBuilder();

        // load values on the left and on the right
        code.append(apply(binaryOp.getLeftOperand()));
        code.append(apply(binaryOp.getRightOperand()));

        //Optimizations request
        boolean comparasionToZero = false;

        //If left op is a comparasion and the right argument equals zero, condition is true
        switch (binaryOp.getOperation().getOpType()) {
            case LTH, GTH -> {
                if (binaryOp.getRightOperand().equals("0"))
                    comparasionToZero = true;
            }
        }

        // TODO: Hardcoded for int type, needs to be expanded
        var type = binaryOp.getLeftOperand().getType();

        var typePrefix = "i";

        // apply operation
        var op = switch (binaryOp.getOperation().getOpType()) {
            case ADD -> "add";
            case MUL -> "mul";
            case SUB -> "sub";
            case DIV -> "div";
            case LTH -> "iflt"; //if less than
            case GTH -> "ifgt"; //if greater than
            //For booleans
            case ANDB -> "and";
            case ORB -> "or";
            case NOTB -> "not";
            default -> throw new NotImplementedException(binaryOp.getOperation().getOpType());
        };

        code.append(typePrefix + op).append(NL);

        return code.toString();
    }

    private String generateReturn(ReturnInstruction returnInst) {
        var code = new StringBuilder();

        String returnType = returnInst.getReturnType().toString();

        switch (returnType) {
            case "INT32", "BOOLEAN" -> {
                //code.append(generators.apply(returnInst.getOperand()));
                updateStack(-1);
                code.append("ireturn").append(NL);
            }
//            case "OBJECTREF", "ARRAYREF", "STRING", "THIS", -> {
//                code.append(generators.apply(returnInst.getOperand()));
//                updateStack(-1);
//                code.append("areturn").append(NL);
//            }
            case "VOID" -> code.append("return").append(NL);
        }

        return code.toString();
    }

    private String generateNewInstruction(NewInstruction newInst) {
        StringBuilder code = new StringBuilder();

        var caller = newInst.getCaller().getType();
        if(caller instanceof ArrayType arrayType){

            //Implemented on Array Semantics also
            if(newInst.getArguments().size() < 1){
                throw new IllegalArgumentException();
            }

            code.append(apply(newInst.getArguments().get(0)));
            code.append("newarray int").append(NL);
        }

        else if (caller instanceof ClassType classType){
            var className = imports_map.getOrDefault(classType.getName(), classType.getName());
            code.append("new ").append(className).append(NL);
            limitStack++;
        }

        return code.toString();
    }

    private String generateInvokeSpecial(InvokeSpecialInstruction invokeSpecial) {
        var code = new StringBuilder();

        code.append("InvokeSpecial").append(NL);
        return code.toString();
    }

    private String generateField(Field field) {
        var code = new StringBuilder();
        code.append(".field").append(SPACE);

        switch (field.getFieldAccessModifier()){
            case PUBLIC -> code.append("public");
            case PRIVATE -> code.append("private");
            case PROTECTED -> code.append("protected");
            case DEFAULT -> code.append("default");
        }

        //TODO: still needs to transform Type into JasminType - getJasminType is in progress
        code.append(SPACE).append("'").append(field.getFieldName()).append("'").
            append(SPACE).append((field.getFieldType())).append(NL);

        return code.toString();
    }
}


// ********* Binary Op - Operation Type Class *************

//        ADD,
//        SUB,
//        MUL,
//        DIV,
//        REM, remainder
//        SHR, shif right
//        SHL, shift lef
//        SHRR,  shift tbm
//        XOR, nn tem na gramatica
//        AND, &
//        OR,
//        LTH, <
//        GTH, >
//        EQ, ==
//        NEQ, !=
//        LTE, <=
//        GTE, >=
//        ANDB, && - booleanos
//        ORB, ||
//        NOTB, !
//        NOT; ~ -- Inverter todos os bits