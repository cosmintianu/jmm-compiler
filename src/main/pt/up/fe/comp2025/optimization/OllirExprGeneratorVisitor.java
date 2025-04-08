package pt.up.fe.comp2025.optimization;

import com.sun.jdi.BooleanType;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp2025.ast.Kind;
import pt.up.fe.comp2025.ast.TypeUtils;

import java.util.ArrayList;
import java.util.List;

import static pt.up.fe.comp2025.ast.Kind.*;

/**
 * Generates OLLIR code from JmmNodes that are expressions.
 */
public class OllirExprGeneratorVisitor extends AJmmVisitor<Void, OllirExprResult> {

    private final Character QUOTATION = '"';
    private final Character COMMA = ',';
    private static final String SPACE = " ";
    private static final String ASSIGN = ":=";
    private final String END_STMT = ";\n";

    private final SymbolTable table;

    private final TypeUtils types;
    private final OptUtils ollirTypes;

    private boolean isMethodStatic;


    public OllirExprGeneratorVisitor(SymbolTable table) {
        this.table = table;
        this.types = new TypeUtils(table);
        this.ollirTypes = new OptUtils(types);
    }


    @Override
    protected void buildVisitor() {
        addVisit(VAR_REF_EXPR, this::visitVarRef);
        addVisit(BINARY_EXPR, this::visitBinExpr);
        addVisit(INTEGER_LITERAL, this::visitInteger);
        addVisit(BOOLEAN_LITERAL, this::visitBoolean);
        addVisit(METHOD_CALL_EXPR, this::visitMethodCallExpr);

//        setDefaultVisit(this::defaultVisit);
    }

    private OllirExprResult visitMethodCallExpr(JmmNode node, Void unused){

        StringBuilder computation = new StringBuilder();

        //get target
        String varRefExprName = node.getChild(0).get("name");

        //check if it is static or virtual

        boolean isMethodStatic = false;

        //are there other cases when a method is static?
        if (table.getImports().stream()
                .map(importName -> importName.substring(importName.lastIndexOf('.') + 1))
                .anyMatch(methodName -> methodName.equals(varRefExprName))) {
            isMethodStatic = true;
        }

        String methodInvocation = isMethodStatic ? "invokestatic" : "invokevirtual";
        computation.append(methodInvocation).append("(").append(varRefExprName).append(COMMA).append(SPACE);

        //get method name
        String methodName = node.get("name");
        computation.append(QUOTATION).append(methodName).append(QUOTATION).append(COMMA).append(SPACE);

        //visit method params

        for (int i = 1; i < node.getChildren().size(); i++) {
            var param = visit(node.getChild(i));
            computation.append(param.getCode());
            if (i!= (node.getChildren().size() - 1)) //multiple params
                computation.append(COMMA).append(SPACE);
        }

        String code = ".V";

        computation.append(")").append(code).append(END_STMT);


        return new OllirExprResult(code, computation);
    }

    private OllirExprResult visitInteger(JmmNode node, Void unused) {
        var intType = TypeUtils.newIntType();
        String ollirIntType = ollirTypes.toOllirType(intType);
        String code = node.get("value") + ollirIntType;
        return new OllirExprResult(code);
    }

    private OllirExprResult visitBoolean(JmmNode node, Void unused) {
        var booleanType = new Type("boolean", false);
        String ollirBooleanType = ollirTypes.toOllirType(booleanType);
        String code = node.get("name") + ollirBooleanType;
        return new OllirExprResult(code);
    }


    private OllirExprResult visitBinExpr(JmmNode node, Void unused) {

        var lhs = visit(node.getChild(0));
        var rhs = visit(node.getChild(1));

        StringBuilder computation = new StringBuilder();

        // code to compute the children
        computation.append(lhs.getComputation());
        computation.append(rhs.getComputation());

        // code to compute self
        Type resType = types.getExprType(node);
        String resOllirType = ollirTypes.toOllirType(resType);
        String code = ollirTypes.nextTemp() + resOllirType;

        computation.append(code).append(SPACE)
                .append(ASSIGN).append(resOllirType).append(SPACE)
                .append(lhs.getCode()).append(SPACE);

        Type type = types.getExprType(node);
        computation.append(node.get("op")).append(ollirTypes.toOllirType(type)).append(SPACE)
                .append(rhs.getCode()).append(END_STMT);

        return new OllirExprResult(code, computation);
    }


    private OllirExprResult visitVarRef(JmmNode node, Void unused) {

        var id = node.get("name");
        Type type = types.getExprType(node);
        String ollirType = ollirTypes.toOllirType(type);

        String code = id + ollirType;

        return new OllirExprResult(code);
    }

    /**
     * Default visitor. Visits every child node and return an empty result.
     *
     * @param node
     * @param unused
     * @return
     */
    private OllirExprResult defaultVisit(JmmNode node, Void unused) {

        for (var child : node.getChildren()) {
            visit(child);
        }

        return OllirExprResult.EMPTY;
    }

}
