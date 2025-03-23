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
        TestUtils.noErrors(result);
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

}
