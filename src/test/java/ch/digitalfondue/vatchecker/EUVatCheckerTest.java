package ch.digitalfondue.vatchecker;

import org.junit.Assert;
import org.junit.Test;


public class EUVatCheckerTest {

    @Test
    public void testSuccess() {
        EUVatCheckResponse resp = EUVatChecker.doCheck("IT", "00950501007");
        Assert.assertEquals(true, resp.isValid());
        Assert.assertEquals("BANCA D'ITALIA", resp.getName());
        Assert.assertEquals("VIA NAZIONALE 91 \n00184 ROMA RM\n", resp.getAddress());
    }

    @Test
    public void testCorrectCountryCodeButInvalidVAT() {
        EUVatCheckResponse resp = EUVatChecker.doCheck("IT", "00950501000");
        Assert.assertEquals(false, resp.isValid());
        Assert.assertEquals("---", resp.getName());
        Assert.assertEquals("---", resp.getAddress());
    }

    @Test
    public void testIncorrectCountry() {
        EUVatCheckResponse resp = EUVatChecker.doCheck("AB", "009505010075353");
        Assert.assertEquals(false, resp.isValid());
        Assert.assertEquals(null, resp.getName());
        Assert.assertEquals(null, resp.getAddress());
    }

    @Test
    public void testInstance() {
        EUVatChecker euVatChecker = new EUVatChecker();
        EUVatCheckResponse resp = euVatChecker.check("IT", "00950501007");
        Assert.assertEquals(true, resp.isValid());
        Assert.assertEquals("BANCA D'ITALIA", resp.getName());
        Assert.assertEquals("VIA NAZIONALE 91 \n00184 ROMA RM\n", resp.getAddress());
    }
}
