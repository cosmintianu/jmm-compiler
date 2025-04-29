package pt.up.fe.comp2025.optimization;

import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp2025.ast.TypeUtils;
import pt.up.fe.specs.util.collections.AccumulatorMap;
import pt.up.fe.specs.util.exceptions.NotImplementedException;

import static pt.up.fe.comp2025.ast.Kind.TYPE;

/**
 * Utility methods related to the optimization middle-end.
 */
public class OptUtils {


    private final AccumulatorMap<String> temporaries;
    private final AccumulatorMap<String> ifTemporaries;
    private final AccumulatorMap<String> whileTemporaries;
    private final AccumulatorMap<String> andTemporaries;

    private final TypeUtils types;

    public OptUtils(TypeUtils types) {
        this.types = types;
        this.temporaries = new AccumulatorMap<>();
        this.ifTemporaries = new AccumulatorMap<>();
        this.whileTemporaries = new AccumulatorMap<>();
        this.andTemporaries = new AccumulatorMap<>();
    }

    public String nextAnd(){
        String prefix = "andTemp";
        var nextAnd = andTemporaries.add(prefix) - 1;
        return prefix + nextAnd;
    }

    public String nextWhile(){
        String prefix = "while";
        var nextWhileNum = ifTemporaries.add(prefix) - 1;
        return prefix + nextWhileNum;
    }

    public String nextIf(String prefix){
        var nextIfNum = ifTemporaries.add(prefix) - 1;
        return prefix + nextIfNum;
    }


    public String nextTemp() {

        return nextTemp("tmp");
    }

    public String nextTemp(String prefix) {

        // Subtract 1 because the base is 1
        var nextTempNum = temporaries.add(prefix) - 1;

        return prefix + nextTempNum;
    }


    public String toOllirType(JmmNode typeNode) {

        TYPE.checkOrThrow(typeNode);

        return toOllirType(types.convertType(typeNode));
    }

    public String toOllirType(Type type) {
        return type.isArray() ? ".array" + toOllirType(type.getName()) : toOllirType(type.getName());
    }

    public String toOllirType(String typeName) {

        String type = "." + switch (typeName) {
            case "int" -> "i32";
            case "boolean" -> "bool";
            case "void" -> "V";
            case "String" -> "String";
            default -> typeName;
        };

        return type;
    }


}
