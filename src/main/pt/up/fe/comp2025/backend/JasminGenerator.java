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
    int maxStack;

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
        generators.put(InvokeStaticInstruction.class, this::generateStatic);
        generators.put(InvokeVirtualInstruction.class, this::generateVirtual);
        generators.put(Field.class, this::generateField);
        generators.put(PutFieldInstruction.class, this::generatePutField);
        generators.put(GetFieldInstruction.class, this::generateGetField);
        generators.put(SingleOpCondInstruction.class, this::generateSingleOpCond);
        generators.put(GotoInstruction.class, this::generateGoto);
        generators.put(OpCondInstruction.class, this::generateOpCondInst);
    }

    private void updateStack(int value) {
        currentStack += value;
        maxStack = Math.max(maxStack, currentStack);
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

    private String spaceOr_ (int regNumber) {

        if (regNumber < 4) return "_" + regNumber;
        else return " " + regNumber;
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
            code.append(apply(field));
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
        maxStack = 0;

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
        var returnType = getJasminType(method.getReturnType());

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
        code.append(TAB).append(".limit stack ").append(Math.min(99, maxStack)).append(NL);
        code.append(TAB).append(".limit locals ").append(local).append(NL);

        code.append(instructions).append(".end method\n");

        // unset method
        currentMethod = null;
        //System.out.println("ENDING METHOD " + method.getMethodName());
        return code.toString();
    }

    private String getStoreType(Operand operand) {

        var code = new StringBuilder();
        String operandName = operand.getName();
        Type operandType = operand.getType();

        if (operandType instanceof ArrayType) {
            code.append("iastore").append(NL);
            updateStack(-3);
            return code.toString();
        }

        else {
            var regName = currentMethod.getVarTable().get(operandName);
            var reg = regName.getVirtualReg();
            var regType = regName.getVarType();

            if (regType instanceof BuiltinType) {
                code.append("istore").append(spaceOr_(reg)).append(NL);
            }
            else {
                code.append("astore").append(spaceOr_(reg)).append(NL);
            }
            updateStack(-1);
        }

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

        if (operand instanceof ArrayOperand arrayOperand)
            code.append(apply(arrayOperand));

        // get register
        var reg = currentMethod.getVarTable().get(operand.getName());

        String store = getStoreType(operand);
        code.append(store).append(NL);

        return code.toString();
    }

    private String generateSingleOp(SingleOpInstruction singleOp) {
        return apply(singleOp.getSingleOperand());
    }

    private String generateLiteral(LiteralElement literal) {

        var integerValue = Integer.parseInt(literal.getLiteral());

        updateStack(1);

        //sipush when the constant fits in a short
        if (integerValue >= -1 && integerValue <= 5) {
            return "iconst_" + literal.getLiteral() + NL;
        }

        else if (integerValue >= Byte.MIN_VALUE && integerValue <= Byte.MAX_VALUE){
            return "bpush " + literal.getLiteral() + NL;
        }

        else if(integerValue >= Short.MIN_VALUE && integerValue <= Short.MAX_VALUE){
            return "sipush " + literal.getLiteral() + NL;
        }

        else
            return "ldc " + literal.getLiteral() + NL;
    }

    private String generateOperand(Operand operand) {

        var code = new StringBuilder();

        // get register
        var reg = currentMethod.getVarTable().get(operand.getName());
        var regNumber = reg.getVirtualReg();

        if (operand.getType() instanceof ArrayType) {
            code.append("aload").append(spaceOr_(regNumber)).append(NL);

            //TODO: handling index
        }

        else if (operand.getType() instanceof BuiltinType) {
            code.append("iload").append(spaceOr_(regNumber)).append(NL);
        }

        else if (operand.getType() instanceof  ClassType){
            code.append("aload").append(spaceOr_(regNumber)).append(NL);
        }

        updateStack(1);
        return code.toString();
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

        var typePrefix = "i";

        if (binaryOp.getLeftOperand().getType() instanceof BuiltinType builtinType) {

            switch (builtinType.getKind()){
                case INT32:
                    typePrefix = "i";
                    break;
                case BOOLEAN:
                    typePrefix = "b";
                    break;
            }
        }

        var binaryOpType = binaryOp.getOperation().getOpType();

        // apply operation
        var op = switch (binaryOpType) {
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

        if (binaryOpType == OperationType.LTH || binaryOpType == OperationType.GTH) {

            String label = String.valueOf(currentMethod.getLabels().size());
            String labelTrue = "LabelTrue" + label;
            String labelEnd = "LabelEnd" + label;

            currentMethod.addLabel(labelTrue, binaryOp);
            currentMethod.addLabel(labelEnd, binaryOp);

            code.append(SPACE).append(labelTrue).append(NL).
                    append("iconst_0").append(NL).append("goto ").append(labelEnd).append(NL).
                    append("iconst_1").append(NL).append(labelEnd).append(":");
        }

        code.append(typePrefix + op).append(NL);

        return code.toString();
    }

    private String generateReturn(ReturnInstruction returnInst) {
        var code = new StringBuilder();

        Type returnType = returnInst.getReturnType();

        if (returnType.toString().equals("VOID")) {
            code.append("return").append(NL);
        }

        else if (returnType instanceof BuiltinType) {
            returnInst.getOperand().ifPresent(op -> code.append(apply(op)));
            updateStack(-1);
            code.append("ireturn").append(NL);
        }

        else {
            returnInst.getOperand().ifPresent(op -> code.append(apply(op)));
            updateStack(-1);
            code.append("areturn").append(NL);
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
            updateStack(1);
        }

        return code.toString();
    }

    private String generateInvokeSpecial(InvokeSpecialInstruction invokeSpecial) {
        var code = new StringBuilder();

        //Handle caller
        code.append(apply(invokeSpecial.getCaller()));
        updateStack(-1);

        //Handle arguments
        for (Element element : invokeSpecial.getArguments()){
            code.append(apply(element));
        }
        updateStack(-invokeSpecial.getArguments().size());

        //
        String returnType = getJasminType(invokeSpecial.getReturnType());
        if (!returnType.equals("void"))
            updateStack(1);

        String className = currentMethod.getOllirClass().getClassName();
        code.append("invokenonvirtual").append(SPACE).append(className).append("/<init>").
                append("(").append(")").
                append(returnType).append(NL);

        return code.toString();
    }

    private String generateStatic(InvokeStaticInstruction invokeStatic) {
        var code = new StringBuilder();

        //Handle caller
        code.append(apply(invokeStatic.getCaller()));

        //Handle arguments
        for (Element element : invokeStatic.getArguments()){
            code.append(apply(element));
        }
        updateStack(-invokeStatic.getArguments().size());

        //
        String returnType = getJasminType(invokeStatic.getReturnType());
        if (!returnType.equals("void"))
            updateStack(1);

        if (!(invokeStatic.getCaller() instanceof Operand)) {
            throw new NotImplementedException(invokeStatic.getClass());
        }

        var operand = (Operand) invokeStatic.getCaller();
        String className = imports_map.getOrDefault(operand.getName(), operand.getName());

        code.append("invokestatic").append(SPACE).append(className).append("/<init>").
                append("(").append(")").
                append(returnType).append(NL);

        return code.toString();
    }

    private String generateVirtual(InvokeVirtualInstruction invokeVirtual) {
        var code = new StringBuilder();

        //Handle caller
        code.append(apply(invokeVirtual.getCaller()));
        updateStack(-1);

        //Handle arguments
        for (Element element : invokeVirtual.getArguments()){
            code.append(apply(element));
        }
        updateStack(-invokeVirtual.getArguments().size());

        //
        String returnType = getJasminType(invokeVirtual.getReturnType());
        if (!returnType.equals("void"))
            updateStack(1);

        String className = currentMethod.getOllirClass().getClassName();
        code.append("invokevirtual").append(SPACE).append(className).append("/<init>").
                append("(").append(")").
                append(returnType).append(NL);

        return code.toString();
    }

    private String generateField(Field field) {
        var code = new StringBuilder();
        code.append(".field").append(SPACE);

        switch (field.getFieldAccessModifier()){
            case PUBLIC -> code.append("public");
            case PRIVATE -> code.append("private");
            case PROTECTED -> code.append("protected");
            case DEFAULT -> code.append("public");
        }

        code.append(SPACE).append("'").append(field.getFieldName()).append("'").
            append(SPACE).append(getJasminType(field.getFieldType())).append(NL);

        return code.toString();
    }

    private String generatePutField(PutFieldInstruction putfield) {
        var code = new StringBuilder();

        apply(putfield.getValue());
        updateStack(-2);

        code.append("aload_0").append(NL);

        String className = currentMethod.getOllirClass().getClassName();
        String name = putfield.getField().getName();
        String type = getJasminType(putfield.getField().getType());

        code.append("putfield ").append(className).append("/").append(name).
                append(SPACE).append(type).append(NL);

        return code.toString();
    }

    private String generateGetField(GetFieldInstruction getField) {
        var code = new StringBuilder();

        String className = currentMethod.getOllirClass().getClassName();
        String name = getField.getField().getName();
        String type = getJasminType(getField.getFieldType());

        code.append("aload_0").append(NL);
        code.append("getfield ").append(className).append("/").append(name).
                append(SPACE).append(type).append(NL);
        return code.toString();
    }

    private String generateSingleOpCond(SingleOpCondInstruction inst) {
        var code = new StringBuilder();

        apply(inst.getCondition());
        code.append("ifne ").append(inst.getLabel()).append(NL);

        return code.toString();
    }

    private String generateGoto(GotoInstruction gotoInstruction) {
        return "goto " + gotoInstruction.getLabel() + NL;
    }

    //TODO: to be implemented
    private String generateOpCondInst(OpCondInstruction opCondInstruction) {

        var code = new StringBuilder();
        code.append("generateOpCond").append(NL);
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