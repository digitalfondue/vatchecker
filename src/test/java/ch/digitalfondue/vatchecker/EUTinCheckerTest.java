/*
 * Copyright © 2018-2024 digitalfondue (info@digitalfondue.ch)
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

public class EUTinCheckerTest {

    @Test
    public void testValidSyntaxAndStructure() {
        EUTinCheckResponse resp = EUTinChecker.doCheck("BE", "00012511119");
        Assert.assertEquals(true, resp.isValidStructure());
        Assert.assertEquals(true, resp.isValidSyntax());
        Assert.assertEquals(false, resp.isError());
        Assert.assertNull(resp.getFault());
    }

    @Test
    public void testValidStructureInvalidSyntax() {
        EUTinCheckResponse resp = EUTinChecker.doCheck("BE", "00012511118");
        Assert.assertEquals(true, resp.isValidStructure());
        Assert.assertEquals(false, resp.isValidSyntax());
        Assert.assertEquals(false, resp.isError());
        Assert.assertNull(resp.getFault());
    }

    @Test
    public void testBothInvalid() {
        EUTinCheckResponse resp = EUTinChecker.doCheck("BE", "000125111189874");
        Assert.assertEquals(false, resp.isValidStructure());
        Assert.assertEquals(false, resp.isValidSyntax());
        Assert.assertEquals(false, resp.isError());
        Assert.assertNull(resp.getFault());
    }

    @Test
    public void testInvalidInput() {
        EUTinCheckResponse resp = EUTinChecker.doCheck("US", "000125111189874");
        Assert.assertEquals(false, resp.isValidStructure());
        Assert.assertEquals(false, resp.isValidSyntax());
        Assert.assertEquals(true, resp.isError());
        Assert.assertNotNull(resp.getFault());
        Assert.assertEquals(EUTinCheckResponse.FaultType.INVALID_INPUT, resp.getFault().getFaultType());
    }
}
