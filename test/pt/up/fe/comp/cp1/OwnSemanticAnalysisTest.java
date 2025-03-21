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
    }

    @Test
    public void IntLessThanBoolean() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/ownsemanticanalysis/IntLessThanBoolean.jmm"));
        TestUtils.mustFail(result);
    }

    @Test
    public void ObjectAndInt() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/ownsemanticanalysis/ObjectAndInt.jmm"));
        TestUtils.mustFail(result);
    }

}
