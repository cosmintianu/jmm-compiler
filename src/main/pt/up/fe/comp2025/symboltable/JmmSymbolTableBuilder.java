package pt.up.fe.comp2025.symboltable;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.comp2025.ast.Kind;
import pt.up.fe.comp2025.ast.TypeUtils;
import pt.up.fe.specs.util.SpecsCheck;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
        String superClassName = classDecl.hasAttribute("extendedClass") ? classDecl.get("extendedClass") : null;
        var methods = buildMethods(classDecl);
        var returnTypes = buildReturnTypes(classDecl);
        var params = buildParams(classDecl);
        var locals = buildLocals(classDecl);
        var fields = buildFields(classDecl);
        var imports = buildImports(root);
    
        return new JmmSymbolTable(className, superClassName, methods, returnTypes, params, locals, imports, fields);
    }

    private static List<String> buildImports(JmmNode root) {
        List<String> imports = new ArrayList<>();
    
        for (JmmNode child : root.getChildren()) {
            if (child.getKind().equals("ImportDecl")) {
                StringBuilder importPath = new StringBuilder();
                for (JmmNode part : child.getChildren()) {
                    if (importPath.length() > 0) {
                        importPath.append(".");
                    }
                    importPath.append(part.get("name"));
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
            Type type = new Type(varDecl.getChild(0).get("name"),isArray);
            String name = varDecl.get("name");
            Locals.add(new Symbol(type,name));
        }  );

        return Locals;
    }

    private Map<String, Type> buildReturnTypes(JmmNode classDecl) {
        Map<String, Type> map = new HashMap<>();
    
        for (var method : classDecl.getChildren(Kind.METHOD_DECL)) {
            var name = method.get("name");
            var returnTypeNode = method.getChildren().get(0);
            var returnType = TypeUtils.convertType(returnTypeNode);
            map.put(name, returnType);
        }
    
        return map;
    }

    private Map<String, List<Symbol>> buildParams(JmmNode classDecl) {
        Map<String, List<Symbol>> map = new HashMap<>();
    
        for (var method : classDecl.getChildren(Kind.METHOD_DECL)) {
            var name = method.get("name");
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
            var name = method.get("name");
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
                .map(method -> method.get("name"))
                .toList();
    }
}