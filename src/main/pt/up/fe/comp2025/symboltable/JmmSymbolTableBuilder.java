package pt.up.fe.comp2025.symboltable;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.AJmmNode;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.comp2025.ast.Kind;
import pt.up.fe.comp2025.ast.TypeUtils;

import java.util.*;

import static pt.up.fe.comp2025.ast.Kind.*;

public class JmmSymbolTableBuilder {

    // In case we want to already check for some semantic errors during symbol table building.
    private List<Report> reports;

    public List<Report> getReports() {
        return reports;
    }

    private static Report newError(JmmNode node, String message) {
        return Report.newError(
                Stage.SEMANTIC,
                node.getLine(),
                node.getColumn(),
                message,
                null);
    }

    public JmmSymbolTable build(JmmNode root) {
        reports = new ArrayList<>();

        List<JmmNode> classDecls = root.getChildren(CLASS_DECL);
        var classDecl = classDecls.get(0);

        String className = classDecl.get("name");
        String superClassName = classDecl.getOptional("nameExtendClass").orElse(null);
        var methods = buildMethods(classDecl);
        var returnTypes = buildReturnTypes(classDecl);
        var params = buildParams(classDecl);
        var locals = buildLocals(classDecl);
        var fields = buildFields(classDecl);
        var imports = buildImports(root);

        JmmSymbolTable table = new JmmSymbolTable(className, superClassName, methods, returnTypes, params, locals, imports, fields);

        System.out.println("Symbol Table generated: \n" + table);

        return table;
    }

    private static List<String> buildImports(JmmNode root) {
        List<String> imports = new ArrayList<>();

        for (JmmNode child : root.getChildren()) {
            if (child.getKind().equals("ImportDecl")) {

                StringBuilder importPath = new StringBuilder();

                importPath.append(child.get("nameImport"));

                for (JmmNode part : child.getChildren("ID")) {
                    importPath.append(".").append(part.get("name"));
                }

                imports.add(importPath.toString());
            }
        }

        return imports;
    }

    private List<Symbol> buildFields(JmmNode classDecl) {
        List<Symbol> Locals = new ArrayList<>();
        classDecl.getChildren(VAR_DECL).stream().forEach(varDecl -> {
            boolean isArray = Objects.equals(varDecl.getChild(0).getKind(), "VarArray");
            Type type = new Type(varDecl.getChild(0).get("name"), isArray);
            String name = varDecl.get("name");
            Locals.add(new Symbol(type, name));
        });

        return Locals;
    }

    private Map<String, Type> buildReturnTypes(JmmNode classDecl) {
        Map<String, Type> map = new HashMap<>();

        for (var method : classDecl.getChildren(Kind.METHOD_DECL)) {
            var name = method.get("nameMethod");
            boolean isMain = method.getBoolean("isMain", false);

            if (isMain) {
                var returnType = new Type("void", false);
                map.put(name, returnType);
            } else {
                if (!method.getChildren().isEmpty()) {
                    var returnTypeNode = method.getChildren().getFirst();
                    var returnType = TypeUtils.convertType(returnTypeNode);
                    map.put(name, returnType);
                }
            }
        }

        return map;
    }

    private Map<String, List<Symbol>> buildParams(JmmNode classDecl) {
        Map<String, List<Symbol>> map = new HashMap<>();

        for (var method : classDecl.getChildren(Kind.METHOD_DECL)) {
            var name = method.get("nameMethod");
            var params = method.getChildren(Kind.PARAM).stream()
                    .map(param -> {
                        var typeNode = param.getChildren().get(0);
                        var type = TypeUtils.convertType(typeNode);
                        return new Symbol(type, param.get("name"));
                    })
                    .toList();

            map.put(name, params);
        }

        return map;
    }

    private Map<String, List<Symbol>> buildLocals(JmmNode classDecl) {
        var map = new HashMap<String, List<Symbol>>();

        for (var method : classDecl.getChildren(Kind.METHOD_DECL)) {
            var name = method.get("nameMethod");
            var locals = method.getChildren(Kind.VAR_DECL).stream()
                    .map(varDecl -> {
                        var typeNode = varDecl.getChildren().get(0);
                        var type = TypeUtils.convertType(typeNode);
                        return new Symbol(type, varDecl.get("name"));
                    })
                    .toList();

            map.put(name, locals);
        }

        return map;
    }

    private List<String> buildMethods(JmmNode classDecl) {
        return classDecl.getChildren(Kind.METHOD_DECL).stream()
                .map(method -> method.get("nameMethod"))
                .toList();
    }
}