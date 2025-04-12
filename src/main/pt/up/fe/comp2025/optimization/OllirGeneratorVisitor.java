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
    private final String ARRAY = "array";
    private final String DOT = ".";
    private final String NEW = "new";
    private final String L_PARENTHESES = "(";
    private final String R_PARENTHESES = ")";
    private final Character COMMA = ',';

    private final SymbolTable table;

    private final TypeUtils types;
    private final OptUtils ollirTypes;


    private final OllirExprGeneratorVisitor exprVisitor;

    public OllirGeneratorVisitor(SymbolTable table) {
        this.table = table;
        this.types = new TypeUtils(table);
        this.ollirTypes = new OptUtils(types);
        exprVisitor = new OllirExprGeneratorVisitor(table);
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

//        setDefaultVisit(this::defaultVisit);
    }

    private String visitArrayInitStmt(JmmNode node, Void unused) {

        //TODO: To be reviewed I am not so confident :,)

        StringBuilder code = new StringBuilder();
        Type resType = types.getExprType(node.getChild(0));

        //in the way we have defined in the grammar we have 'type=int {array=false}' [ capacity=expr ]
        String resOllirType = ollirTypes.toOllirType(resType) + DOT + ARRAY;

        String temp0 = ollirTypes.nextTemp() + resOllirType;

        code.append(temp0).append(SPACE).append(ASSIGN).append(resOllirType).append(SPACE);
        code.append(NEW).append(L_PARENTHESES).append(ARRAY).append(COMMA).append(SPACE);

        var expr = node.getChild(1);
        code.append(exprVisitor.visit(expr).getCode()).append(R_PARENTHESES);
        code.append(resOllirType).append(END_STMT);

        //Getting the temp_var Ollir code

        String arrayName = node.get("name");
        code.append(arrayName).append(resOllirType).append(SPACE).append(ASSIGN);
        code.append(resOllirType).append(SPACE).append(temp0).append(END_STMT);

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

        // code to compute self
        // statement has type of lhs
        var left = node.getChild(0);
        Type thisType = types.getExprType(left);
        String typeString = ollirTypes.toOllirType(thisType);
        var varCode = left.get("name") + typeString; //in our case we have 'expr' as the left argument, instead of name=id

        code.append(varCode);
        code.append(SPACE);

        code.append(ASSIGN);
        code.append(typeString);
        code.append(SPACE);

        code.append(rhs.getCode());

        code.append(END_STMT);

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
