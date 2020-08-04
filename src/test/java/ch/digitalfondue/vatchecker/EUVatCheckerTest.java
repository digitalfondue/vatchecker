/**
 * Copyright © 2018 digitalfondue (info@digitalfondue.ch)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
        Assert.assertEquals(false, resp.isError());
        Assert.assertNull(resp.getFault());
    }

    @Test
    public void testCorrectCountryCodeButInvalidVAT() {
        EUVatCheckResponse resp = EUVatChecker.doCheck("IT", "00950501000");
        Assert.assertEquals(false, resp.isValid());
        Assert.assertEquals("---", resp.getName());
        Assert.assertEquals("---", resp.getAddress());
        Assert.assertEquals(false, resp.isError());
        Assert.assertNull(resp.getFault());
    }

    @Test
    public void testInvalidVATFormatSoapFault() {
        EUVatCheckResponse resp = EUVatChecker.doCheck("IT", "");
        Assert.assertEquals(false, resp.isValid());
        Assert.assertEquals(null, resp.getName());
        Assert.assertEquals(null, resp.getAddress());
        Assert.assertTrue(resp.isError());
        Assert.assertEquals(EUVatCheckResponse.FaultType.INVALID_INPUT, resp.getFault().getFaultType());
        Assert.assertEquals("INVALID_INPUT", resp.getFault().getFault());
        Assert.assertEquals("soap:Server", resp.getFault().getFaultCode());
    }

    @Test
    public void testIncorrectCountry() {
        EUVatCheckResponse resp = EUVatChecker.doCheck("AB", "009505010075353");
        Assert.assertEquals(false, resp.isValid());
        Assert.assertEquals(null, resp.getName());
        Assert.assertEquals(null, resp.getAddress());
        Assert.assertTrue(resp.isError());
        Assert.assertEquals(EUVatCheckResponse.FaultType.INVALID_INPUT, resp.getFault().getFaultType());
        Assert.assertEquals("INVALID_INPUT", resp.getFault().getFault());
        Assert.assertEquals("soap:Server", resp.getFault().getFaultCode());
    }

    @Test
    public void testInstance() {
        EUVatChecker euVatChecker = new EUVatChecker();
        EUVatCheckResponse resp = euVatChecker.check("IT", "00950501007");
        Assert.assertEquals(true, resp.isValid());
        Assert.assertEquals("BANCA D'ITALIA", resp.getName());
        Assert.assertEquals("VIA NAZIONALE 91 \n00184 ROMA RM\n", resp.getAddress());
        Assert.assertEquals(false, resp.isError());
        Assert.assertNull(resp.getFault());
    }
}
