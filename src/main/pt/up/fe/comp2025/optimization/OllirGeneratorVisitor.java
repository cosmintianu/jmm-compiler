package pt.up.fe.comp2025.optimization;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp2025.ast.Kind;
import pt.up.fe.comp2025.ast.TypeUtils;

import javax.swing.*;
import java.util.List;
import java.util.stream.Collectors;

import static pt.up.fe.comp2025.ast.Kind.*;

/**
 * Generates OLLIR code from JmmNodes that are not expressions.
 */
public class OllirGeneratorVisitor extends AJmmVisitor<Void, String> {

    private static final String SPACE = " ";
    private static final String ASSIGN = ":=";
    private final String END_STMT = ";\n";
    private final String NL = "\n";

    private final String L_BRACKET = " {\n";
    private final String R_BRACKET = "}\n";
    private final String L_PARENTHESES = "(";
    private final String R_PARENTHESES = ")";
    private final String L_SQUARE = "[";
    private final String R_SQUARE = "]";

    private final String ARRAY = "array";
    private final String DOT = ".";
    private final String NEW = "new";
    private final Character COMMA = ',';
    private final String GOTO = "goto";
    private final String COLON = ":\n";
    private final String IF = "if";
    private final Character QUOTATION = '"';

    private final SymbolTable table;

    private final TypeUtils types;
    private final OptUtils ollirTypes;


    private final OllirExprGeneratorVisitor exprVisitor;

    public OllirGeneratorVisitor(SymbolTable table) {
        this.table = table;
        this.types = new TypeUtils(table);
        this.ollirTypes = new OptUtils(types);
        exprVisitor = new OllirExprGeneratorVisitor(table, ollirTypes);
    }


    @Override
    protected void buildVisitor() {

        addVisit(PROGRAM, this::visitProgram);
        addVisit(CLASS_DECL, this::visitClass);
        addVisit(METHOD_DECL, this::visitMethodDecl);
        addVisit(PARAM, this::visitParam);
        addVisit(RETURN_STMT, this::visitReturn);
        addVisit(VAR_ASSIGN_STMT, this::visitAssignStmt);
        addVisit(IMPORT_DECL, this::visitImport);
        addVisit(VAR_DECL, this::visitVarDecl);
        addVisit(EXPR_STMT, this::visitExprStmt);
        addVisit(ARRAY_INIT_STMT, this::visitArrayInitStmt);
        addVisit(IF_STMT, this::visitIfStmt);
        addVisit(BRACKET_STMT, this::visitBracketStmt);
        addVisit(WHILE_STMT, this::visitWhileStmt);
        addVisit(ARRAY_ASSIGN_STMT, this::visitArrayAssignStmt);

//        setDefaultVisit(this::defaultVisit);
    }

    private String visitArrayAssignStmt(JmmNode node, Void unused){

        StringBuilder code = new StringBuilder();

        var nameNode = node.get("name");
        var intType = TypeUtils.newIntType();
        String ollirType = ollirTypes.toOllirType(intType);

        String right = exprVisitor.visit(node.getChild(0)).getCode(); //visiting index
        String left = exprVisitor.visit(node.getChild(1)).getCode();

        code.append(nameNode);

        code.append(L_SQUARE);
        code.append(right);
        code.append(R_SQUARE);
        code.append(ollirType);

        code.append(SPACE);
        code.append(ASSIGN).append(ollirType);
        code.append(SPACE);

        code.append(left);
        code.append(END_STMT);

        return code.toString();
    }

    private String visitWhileStmt(JmmNode node, Void unused){
        StringBuilder code = new StringBuilder();

        var tempWhile = ollirTypes.nextWhile();
        var whileCondition = exprVisitor.visit(node.getChild(0));

        code.append(tempWhile);
        code.append(COLON);
        code.append(whileCondition.getComputation());

        String tempEndif = ollirTypes.nextIf("endif");

        //omg why ollir defines a while this way x.x
        code.append(IF);
        code.append(SPACE);
        code.append(L_PARENTHESES);

        code.append("!.bool");
        code.append(SPACE);
        code.append(whileCondition.getCode());
        code.append(R_PARENTHESES);
        code.append(SPACE);
        code.append(GOTO);
        code.append(SPACE);
        code.append(tempEndif);
        code.append(END_STMT);

        //While body
        var whileBody = visit(node.getChild(1));

        code.append(whileBody).append(NL);
        code.append(GOTO);
        code.append(SPACE);
        code.append(tempWhile);
        code.append(END_STMT);

        code.append(tempEndif).append(COLON);

        return code.toString();
    }

    private String visitBracketStmt(JmmNode node, Void unused){

        return visit(node.getChild(0));
    }

    private String visitIfStmt(JmmNode node, Void unused){
        StringBuilder code = new StringBuilder();
        OllirExprResult child = exprVisitor.visit(node.getChild(0));

        code.append(child.getComputation());
        code.append(IF).append(SPACE).append(L_PARENTHESES);

        code.append(child.getCode()).append(R_PARENTHESES).append(SPACE);

        //Get Else body Ollir Code
        var elseBody = visit(node.getChild(2));

        //Get If body Ollir Code
        var ifBody = visit(node.getChild(1));

        String tempThen = ollirTypes.nextIf("then");
        String tempEndif = ollirTypes.nextIf("endif");

        code.append(GOTO).append(SPACE).append(tempThen).append(END_STMT).append(NL);

        //Append else computed body
        code.append(elseBody).append(GOTO).append(SPACE).append(tempEndif).append(END_STMT);
        code.append(tempThen).append(COLON).append(NL);

        //Append if computed body
        code.append(ifBody).append(tempEndif).append(COLON).append(NL);


        return code.toString();
    }

    private String visitArrayInitStmt(JmmNode node, Void unused) {

        StringBuilder code = new StringBuilder();
        Type resType = types.getExprType(node.getChild(0));

        //in the way we have defined in the grammar we have 'type=int {array=false}' [ capacity=expr ]
        String resOllirType = DOT + ARRAY + ollirTypes.toOllirType(resType);

        String temp0 = ollirTypes.nextTemp() + resOllirType;

        code.append(temp0);

        code.append(SPACE);
        code.append(ASSIGN).append(resOllirType);
        code.append(SPACE);

        code.append(NEW);
        code.append(L_PARENTHESES);
        code.append(ARRAY);
        code.append(COMMA);
        code.append(SPACE);

        var expr = node.getChild(1);
        var visitExpr = exprVisitor.visit(expr);

        code.append(visitExpr.getComputation()).append(visitExpr.getCode());
        code.append(R_PARENTHESES);
        code.append(resOllirType);
        code.append(END_STMT);

        //Getting the temp_var Ollir code
        String arrayName = node.get("name");
        code.append(arrayName);
        code.append(resOllirType);

        code.append(SPACE);
        code.append(ASSIGN).append(resOllirType);
        code.append(SPACE);

        code.append(temp0);
        code.append(END_STMT);


        return code.toString();
    }

    private String visitExprStmt(JmmNode node, Void unused) {
        var code = exprVisitor.visit(node.getChild(0));
        return code.getComputation();
    }

    private String visitVarDecl(JmmNode node,Void unused) {
        var typeCode = ollirTypes.toOllirType(node.getChild(0));
        var id = node.get("name");

        String code = id + typeCode;

        return code;
    }

    private String visitAssignStmt(JmmNode node, Void unused) {
    
        var rhs = exprVisitor.visit(node.getChild(1));

        StringBuilder code = new StringBuilder();

        // code to compute the children
        code.append(rhs.getComputation());

        var lhs = node.getChild(0);
        OllirExprResult lhsResult = exprVisitor.visit(lhs);
    
        Type lhsType = types.getExprType(lhs);
        String ollirLhsType = ollirTypes.toOllirType(lhsType);
    
        String lhsName = lhs.get("name"); 
        String currentMethodName = node.getAncestor(Kind.METHOD_DECL).get().get("nameMethod");
    
        boolean isLocalOrParam = table.getLocalVariables(currentMethodName).stream().anyMatch(var -> var.getName().equals(lhsName)) ||
                                  table.getParameters(currentMethodName).stream().anyMatch(param -> param.getName().equals(lhsName));
    
        if (lhs.isInstance(INDEX_ACCESS_EXPR)) {
            // Array store
            code.append(lhsResult.getCode())
                .append(SPACE)
                .append(ASSIGN)
                .append(ollirLhsType)
                .append(SPACE)
                .append(rhs.getCode())
                .append(END_STMT);
    
        } else if (!isLocalOrParam) {
            // Field store 
            code.append("putfield")
                .append(L_PARENTHESES)
                .append("this")
                .append(COMMA)
                .append(SPACE)
                .append(lhsName)
                .append(ollirLhsType)
                .append(COMMA)
                .append(SPACE)
                .append(rhs.getCode())
                .append(R_PARENTHESES)
                .append(DOT)
                .append("V")
                .append(END_STMT);
    
        } else {
            // Local variable or parameter store
            code.append(lhsResult.getCode())
                .append(SPACE)
                .append(ASSIGN)
                .append(ollirLhsType)
                .append(SPACE)
                .append(rhs.getCode())
                .append(END_STMT);
        }
    
        return code.toString();
    }
    
    

    private String visitReturn(JmmNode node, Void unused) {
        String nameMethod = node.getAncestor(Kind.METHOD_DECL).get().get("nameMethod");
        Type retType = table.getReturnType(nameMethod);

        StringBuilder code = new StringBuilder();

        var expr = node.getNumChildren() > 0 ? exprVisitor.visit(node.getChild(0)) : OllirExprResult.EMPTY;

        code.append(expr.getComputation());
        code.append("ret");
        code.append(ollirTypes.toOllirType(retType));
        code.append(SPACE);

        code.append(expr.getCode());

        code.append(END_STMT);

        return code.toString();
    }


    private String visitParam(JmmNode node, Void unused) {

        var typeCode = ollirTypes.toOllirType(node.getChild(0));
        var id = node.get("name");

        String code = id + typeCode;

        return code;
    }


    private String visitMethodDecl(JmmNode node, Void unused) {

        StringBuilder code = new StringBuilder(".method ");

        boolean isPublic = node.getBoolean("isPublic", false);
        boolean isStatic = node.getBoolean("isStatic", false);
        boolean isMain = node.getBoolean("isMain", false);

        if (isPublic) {
            code.append("public ");
        }

        if (isStatic) {
            code.append("static ");
        }

        // name
        var nameMethod = node.get("nameMethod");
        code.append(nameMethod);

        // params
        List<Symbol> params = table.getParameters(nameMethod);

        if(nameMethod.equals("varargs"))
            code.append(SPACE + QUOTATION + "varargs" + QUOTATION);

        code.append("(");
        for (int i = 0; i < params.size(); i++) {
                String ollirChildType = ollirTypes.toOllirType(params.get(i).getType());
                code.append(params.get(i).getName() + ollirChildType);
                if (i!= (params.size() - 1)) //multiple params
                    code.append(",").append(SPACE);
            }
        code.append(")");

        // type
        var retType = table.getReturnType(nameMethod);
        var ollirRetType = ollirTypes.toOllirType(retType);
        code.append(ollirRetType);
        code.append(L_BRACKET);


        // rest of its children stmts
        var stmtsCode = node.getChildren(STMT).stream()
                .map(this::visit)
                .collect(Collectors.joining("\n   ", "   ", ""));

        code.append(stmtsCode);

        if(isMain){
            code.append("ret.V").append(END_STMT);
        }

        code.append(R_BRACKET);
        code.append(NL);

        return code.toString();
    }


    private String visitClass(JmmNode node, Void unused) {

        StringBuilder code = new StringBuilder();

        code.append(NL);
        code.append(table.getClassName());

        String superClassName = node.getOptional("nameExtendClass").orElse(null);
        if (superClassName != null) {
            code.append(SPACE);
            code.append("extends");
            code.append(SPACE);
            code.append(superClassName);
        }

        
        code.append(L_BRACKET);
        code.append(NL);
        code.append(NL);

        for (var field : table.getFields()) {
            String fieldName = field.getName();
            String fieldType = ollirTypes.toOllirType(field.getType());
            code.append(".field public ").append(fieldName).append(fieldType).append(";").append(NL);
        }
    
        code.append(NL);
        
        code.append(buildConstructor());
        code.append(NL);

        for (var child : node.getChildren(METHOD_DECL)) {
            var result = visit(child);
            code.append(result);
        }

        code.append(R_BRACKET);

        return code.toString();
    }

    private String buildConstructor() {

        return """
                .construct %s().V {
                    invokespecial(this, "<init>").V;
                }
                """.formatted(table.getClassName());
    }

    //Handle kind ImportDecl
    private String visitImport(JmmNode node, Void unused) {

        StringBuilder code = new StringBuilder();
        code.append("import").append(SPACE).append(node.get("ID")).append(END_STMT);

        return code.toString();
    }


    private String visitProgram(JmmNode node, Void unused) {

        StringBuilder code = new StringBuilder();

        node.getChildren().stream()
                .map(this::visit)
                .forEach(code::append);

        return code.toString();
    }

    /**
     * Default visitor. Visits every child node and return an empty string.
     *
     * @param node
     * @param unused
     * @return
     */
    private String defaultVisit(JmmNode node, Void unused) {

        for (var child : node.getChildren()) {
            visit(child);
        }

        return "";
    }
}
