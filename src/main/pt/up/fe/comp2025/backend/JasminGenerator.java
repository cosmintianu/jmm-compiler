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
    private int labelCounter = 0;

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
        generators.put(UnaryOpInstruction.class, this::generateUnaryOp);
        generators.put(ArrayLengthInstruction.class, this::generateArrayLength);
        generators.put(ArrayOperand.class, this::generateArrayOperand);
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

        // Process all instructions and collect labels
        Set<String> definedLabels = new HashSet<>();

        for (var inst : method.getInstructions()) {
            for (String label : method.getLabels(inst)) {
                if (!definedLabels.contains(label)) {
                    instructions.append(TAB).append(label).append(":").append(NL);
                    definedLabels.add(label);
                }
            }

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
        var lhs = assign.getDest();

        if (!(lhs instanceof Operand)) {
            throw new NotImplementedException(lhs.getClass());
        }

        var operand = (Operand) lhs;

        if (operand instanceof ArrayOperand arrayOperand) {
            // Load array reference
            var reg = currentMethod.getVarTable().get(arrayOperand.getName());
            code.append("aload").append(spaceOr_(reg.getVirtualReg())).append(NL);
            updateStack(1);

            // Load index
            code.append(apply(arrayOperand.getIndexOperands().get(0)));

            // generate code for loading what's on the right
            code.append(apply(assign.getRhs()));

            // Store in array
            if (operand.getType() instanceof BuiltinType) {
                code.append("iastore").append(NL);
            } else {
                code.append("aastore").append(NL);
            }
            updateStack(-3);
            return code.toString();
        }

        code.append(apply(assign.getRhs()));

        // get register
        var reg = currentMethod.getVarTable().get(operand.getName());

        if (operand.getType() instanceof ArrayType) {
            code.append("astore").append(spaceOr_(reg.getVirtualReg())).append(NL);
            updateStack(-1);
        }
        // For primitive type assignments
        else if (operand.getType() instanceof BuiltinType) {
            code.append("istore").append(spaceOr_(reg.getVirtualReg())).append(NL);
            updateStack(-1);
        }
        // For object type assignments
        else {
            code.append("astore").append(spaceOr_(reg.getVirtualReg())).append(NL);
            updateStack(-1);
        }

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
            return "bipush " + literal.getLiteral() + NL;
        }

        else if(integerValue >= Short.MIN_VALUE && integerValue <= Short.MAX_VALUE){
            return "sipush " + literal.getLiteral() + NL;
        }

        else
            return "ldc " + literal.getLiteral() + NL;
    }

    private String generateOperand(Operand operand) {
        var code = new StringBuilder();

        if (operand instanceof ArrayOperand arrayOperand) {
            // Load array reference
            var reg = currentMethod.getVarTable().get(arrayOperand.getName());
            code.append("aload").append(spaceOr_(reg.getVirtualReg())).append(NL);
            updateStack(1);

            // Load index
            code.append(apply(arrayOperand.getIndexOperands().get(0)));

            // Load array element
            if (arrayOperand.getType() instanceof BuiltinType) {
                code.append("iaload").append(NL);
            } else {
                code.append("aaload").append(NL);
            }
            updateStack(-1);
            return code.toString();
        }

        // get register
        var reg = currentMethod.getVarTable().get(operand.getName());

        if (reg == null) {
            throw new RuntimeException("Operand '" + operand.getName() + "' not found in variable table. " +
                    "Available variables: " + currentMethod.getVarTable().keySet());
        }

        var regNumber = reg.getVirtualReg();

        if (operand.getType() instanceof ArrayType) {
            code.append("aload").append(spaceOr_(regNumber)).append(NL);
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
            case LTH, GTH, GTE, LTE -> {
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
            case GTE -> "ifge"; //if greater than or equal
            case LTE -> "ifle"; //if less than or equal
            case EQ -> "ifeq";  //if equal
            case NEQ -> "ifne"; //if not equal
            //For booleans
            case ANDB -> "and";
            case ORB -> "or";
            case NOTB -> "not";
            default -> throw new NotImplementedException(binaryOp.getOperation().getOpType());
        };

        if (binaryOpType == OperationType.LTH || binaryOpType == OperationType.GTH ||
                binaryOpType == OperationType.GTE || binaryOpType == OperationType.LTE ||
                binaryOpType == OperationType.EQ || binaryOpType == OperationType.NEQ) {

            String labelTrue = "Label" + (labelCounter++);
            String labelEnd = "Label" + (labelCounter++);

            String compareOp = switch (binaryOpType) {
                case LTH -> "if_icmplt";
                case GTH -> "if_icmpgt";
                case GTE -> "if_icmpge";  // Add this case
                case LTE -> "if_icmple";  // Add this case
                case EQ -> "if_icmpeq";   // Add this case
                case NEQ -> "if_icmpne";  // Add this case
                default -> throw new NotImplementedException(binaryOpType);
            };

            code.append(compareOp).append(" ").append(labelTrue).append(NL);
            code.append("iconst_0").append(NL);
            code.append("goto ").append(labelEnd).append(NL);
            code.append(labelTrue).append(":").append(NL);
            code.append("iconst_1").append(NL);
            code.append(labelEnd).append(":").append(NL);

            updateStack(-1);
            return code.toString();
        }

        code.append(typePrefix + op).append(NL);
        updateStack(-1);

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

        //Handle arguments
        for (Element element : invokeSpecial.getArguments()){
            code.append(apply(element));
        }

        int totalConsumed = 1 + invokeSpecial.getArguments().size();
        updateStack(-totalConsumed);

        // Handle return type
        String returnType = getJasminType(invokeSpecial.getReturnType());
        if (!returnType.equals("void"))
            updateStack(1);

        String className = currentMethod.getOllirClass().getClassName();
        code.append("invokespecial").append(SPACE).append(className).append("/<init>").
                append("(").append(")").
                append(returnType).append(NL);

        return code.toString();
    }

    private String generateStatic(InvokeStaticInstruction invokeStatic) {
        var code = new StringBuilder();

        //Handle arguments
        for (Element element : invokeStatic.getArguments()){
            code.append(apply(element));
        }
        updateStack(-invokeStatic.getArguments().size());

        // Handle return type for stack management
        String returnType = getJasminType(invokeStatic.getReturnType());
        if (!returnType.equals("void"))
            updateStack(1);

        // Get the class name from the caller
        if (!(invokeStatic.getCaller() instanceof Operand)) {
            throw new NotImplementedException(invokeStatic.getClass());
        }

        var operand = (Operand) invokeStatic.getCaller();
        String className = imports_map.getOrDefault(operand.getName(), operand.getName());

        // Get the method name
        String methodName;
        try {
            if (invokeStatic instanceof CallInstruction) {
                CallInstruction callInst = invokeStatic;
                methodName = ((LiteralElement) callInst.getMethodName()).getLiteral().replace("\"", "");
            } else {
                methodName = "unknownMethod";
            }
        } catch (Exception e) {
            methodName = "printResult";
        }

        // Build parameter types
        StringBuilder paramTypes = new StringBuilder();
        for (Element arg : invokeStatic.getArguments()) {
            paramTypes.append(getJasminType(arg.getType()));
        }

        code.append("invokestatic").append(SPACE)
                .append(className).append("/").append(methodName)
                .append("(").append(paramTypes).append(")")
                .append(returnType).append(NL);

        return code.toString();
    }

    private String generateVirtual(InvokeVirtualInstruction invokeVirtual) {
        var code = new StringBuilder();

        // Handle caller
        code.append(apply(invokeVirtual.getCaller()));

        // Handle arguments
        for (Element element : invokeVirtual.getArguments()){
            code.append(apply(element));
        }

        int totalConsumed = 1 + invokeVirtual.getArguments().size();
        updateStack(-totalConsumed);

        // Handle return type
        String returnType = getJasminType(invokeVirtual.getReturnType());
        if (!returnType.equals("void")) {
            updateStack(1);
        }

        String methodName;
        try {
            if (invokeVirtual instanceof CallInstruction) {
                CallInstruction callInst = (CallInstruction) invokeVirtual;
                methodName = ((LiteralElement) callInst.getMethodName()).getLiteral().replace("\"", "");
            } else {
                // Extract method name from the instruction
                methodName = "func";
            }
        } catch (Exception e) {
            methodName = "func";
        }

        // Build parameter types
        StringBuilder paramTypes = new StringBuilder();
        for (Element arg : invokeVirtual.getArguments()) {
            paramTypes.append(getJasminType(arg.getType()));
        }

        String className = currentMethod.getOllirClass().getClassName();
        code.append("invokevirtual").append(SPACE).append(className).append("/").append(methodName)
                .append("(").append(paramTypes).append(")")
                .append(returnType).append(NL);

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

        code.append(apply(inst.getCondition()));
        code.append("ifne ").append(inst.getLabel()).append(NL);
        updateStack(-1);

        return code.toString();
    }

    private String generateGoto(GotoInstruction gotoInstruction) {
        return "goto " + gotoInstruction.getLabel() + NL;
    }
    private String generateUnaryOp(UnaryOpInstruction unaryOp) {
        var code = new StringBuilder();

        if (unaryOp.getOperation().getOpType() == OperationType.NOTB) {
            code.append(apply(unaryOp.getOperand()));

            String labelTrue = "Label" + (labelCounter++);
            String labelEnd = "Label" + (labelCounter++);

            code.append("ifeq ").append(labelTrue).append(NL);
            code.append("iconst_0").append(NL);
            code.append("goto ").append(labelEnd).append(NL);
            code.append(labelTrue).append(":").append(NL);
            code.append("iconst_1").append(NL);
            code.append(labelEnd).append(":").append(NL);
        }

        return code.toString();
    }

    private String generateArrayLength(ArrayLengthInstruction arrayLength) {
        var code = new StringBuilder();

        // Load the array reference onto the stack
        code.append(apply(arrayLength.getOperands().getFirst()));

        // Get the array length
        code.append("arraylength").append(NL);

        return code.toString();
    }

    private String generateArrayOperand(ArrayOperand arrayOperand) {
        var code = new StringBuilder();

        // Load the array reference
        var reg = currentMethod.getVarTable().get(arrayOperand.getName());
        code.append("aload").append(spaceOr_(reg.getVirtualReg())).append(NL);
        updateStack(1);

        // Load the index
        code.append(apply(arrayOperand.getIndexOperands().get(0)));

        // Load the actual array element
        if (arrayOperand.getType() instanceof BuiltinType) {
            code.append("iaload").append(NL);
        } else {
            code.append("aaload").append(NL);
        }
        updateStack(-1);

        return code.toString();
    }


    private String generateOpCondInst(OpCondInstruction opCondInstruction) {
        var code = new StringBuilder();

        code.append(apply(opCondInstruction.getCondition()));
        code.append("ifne ").append(opCondInstruction.getLabel()).append(NL);
        updateStack(-1);

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