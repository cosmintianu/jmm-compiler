package pt.up.fe.comp.cp1;

import org.junit.Test;
import pt.up.fe.comp.TestUtils;
import pt.up.fe.specs.util.SpecsIo;

public class OwnSemanticAnalysisTest {

    @Test
    public void varNotDeclared2() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/ownsemanticanalysis/VarNotDeclared2.jmm"));
        TestUtils.noErrors(result);
    }

    @Test
    public void varNotDeclared3() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/ownsemanticanalysis/VarNotDeclared3.jmm"));
        TestUtils.noErrors(result);
    }

    @Test
    public void varNotDeclared4() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/ownsemanticanalysis/VarNotDeclared4.jmm"));
        TestUtils.noErrors(result);
    }

    @Test
    public void varNotDeclared5() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/ownsemanticanalysis/VarNotDeclared5.jmm"));
        TestUtils.noErrors(result);
    }

    @Test
    public void varNotDeclared6() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/ownsemanticanalysis/VarNotDeclared6.jmm"));
        TestUtils.mustFail(result);
        System.out.println(result.getReports());
    }

    @Test
    public void IntLessThanBoolean() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/ownsemanticanalysis/IntLessThanBoolean.jmm"));
        TestUtils.mustFail(result);
        System.out.println(result.getReports());
    }

    @Test
    public void ObjectAndInt() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/ownsemanticanalysis/ObjectAndInt.jmm"));
        TestUtils.mustFail(result);
        System.out.println(result.getReports());
    }

    @Test
    public void AssignIntToInt() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/ownsemanticanalysis/AssignIntToInt.jmm"));
        TestUtils.noErrors(result);
    }

    @Test
    public void WhileLoop() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/ownsemanticanalysis/WhileLoop.jmm"));
        TestUtils.noErrors(result);
    }

    @Test
    public void ImportJavaio() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/ownsemanticanalysis/ImportJavaio.jmm"));
        TestUtils.noErrors(result);
    }


    @Test
    public void MathOperations() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/ownsemanticanalysis/MathOperations.jmm"));
        TestUtils.noErrors(result);
    }

    @Test
    public void ReturnArray() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/ownsemanticanalysis/ReturnArray.jmm"));
        TestUtils.mustFail(result);
        System.out.println(result.getReports());
    }

    @Test
    public void ImportAndFieldAccess() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/ownsemanticanalysis/ImportAndFieldAccess.jmm"));
        TestUtils.noErrors(result);
    }

    @Test
    public void ChainedMethod() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/ownsemanticanalysis/ChainedMethod.jmm"));
        TestUtils.noErrors(result);
    }

    @Test
    public void FieldAssign() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/ownsemanticanalysis/FieldAssign.jmm"));
        TestUtils.noErrors(result);
    }

    @Test
    public void InstanceVariable() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/ownsemanticanalysis/InstanceVariable.jmm"));
        TestUtils.noErrors(result);
    }

    @Test
    public void ReturnArrayCorrect() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/ownsemanticanalysis/ReturnArrayCorrect.jmm"));
        TestUtils.noErrors(result);
    }

    @Test
    public void CompatibleArguments() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/ownsemanticanalysis/CompatibleArguments.jmm"));
        TestUtils.noErrors(result);
    }

    @Test
    public void WhileLoop2() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/ownsemanticanalysis/WhileLoop2.jmm"));
        TestUtils.noErrors(result);
    }

    @Test
    public void WhileLoop3() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/ownsemanticanalysis/WhileLoop3.jmm"));
        TestUtils.mustFail(result);
        System.out.println(result.getReports());
    }

    @Test
    public void String() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/ownsemanticanalysis/String.jmm"));
        TestUtils.noErrors(result);
    }

    @Test
    public void ParentExpr() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/ownsemanticanalysis/ParentExpr.jmm"));
        TestUtils.noErrors(result);
    }

    @Test
    public void IndexFail() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/ownsemanticanalysis/IndexFail.jmm"));
        TestUtils.mustFail(result);
        System.out.println(result.getReports());
    }

    @Test
    public void WrongReturnType() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/ownsemanticanalysis/WrongReturnType.jmm"));
        TestUtils.mustFail(result);
        System.out.println(result.getReports());
    }

    @Test
    public void UnaryExpression() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/ownsemanticanalysis/UnaryExpr.jmm"));
        TestUtils.noErrors(result);
    }

    @Test
    public void IndexFail2() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/ownsemanticanalysis/IndexFail2.jmm"));
        TestUtils.mustFail(result);
        System.out.println(result.getReports());
    }

    @Test
    public void AssignValueToInt() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/ownsemanticanalysis/AssignValueToInt.jmm"));
        TestUtils.noErrors(result);
    }

    @Test
    public void WrongIf() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/ownsemanticanalysis/WrongIf.jmm"));
        TestUtils.mustFail(result);
        System.out.println(result.getReports());
    }

    @Test
    public void WrongLoop() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/ownsemanticanalysis/WrongLoop.jmm"));
        TestUtils.mustFail(result);
        System.out.println(result.getReports());
    }

    @Test
    public void WhileAndIf() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/ownsemanticanalysis/WhileAndIf.jmm"));
        TestUtils.noErrors(result);
    }

    @Test
    public void ReservedNames() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/ownsemanticanalysis/ReservedNames.jmm"));
        TestUtils.mustFail(result);
        System.out.println(result.getReports());
    }

    @Test
    public void ReservedNames2() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/ownsemanticanalysis/ReservedNames2.jmm"));
        TestUtils.mustFail(result);
        System.out.println(result.getReports());
    }

    @Test
    public void MethodImport() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/ownsemanticanalysis/MethodImport.jmm"));
        TestUtils.noErrors(result);
    }

    //Not sure If it should pass or not :c What do you think?
    @Test
    public void CreateInstance() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/ownsemanticanalysis/CreateInstance.jmm"));
        TestUtils.noErrors(result);
    }



}
