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

import org.w3c.dom.Document;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.InputStream;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;

/**
 * A small utility for calling the VIES webservice. See https://ec.europa.eu/taxation_customs/vies/ .
 * <p>
 * The main entry points are {@link #doCheck(String, String)} and if more customization is needed {@link #doCheck(String, String, BiFunction)}.
 */
public class EUVatChecker {

    private static final Document BASE_DOCUMENT_TEMPLATE;

    private static final String ENDPOINT = "https://ec.europa.eu/taxation_customs/vies/services/checkVatService";
    private static final XPathExpression VALID_ELEMENT_MATCHER;
    private static final XPathExpression[] VALID_EXTRACTORS;

    private final BiFunction<String, String, InputStream> documentFetcher;


    /**
     *
     */
    public EUVatChecker() {
        this(Utils::doCall);
    }

    /**
     * @param documentFetcher the function that, given the url of the web service and the body to post, return the resulting body as InputStream
     */
    public EUVatChecker(BiFunction<String, String, InputStream> documentFetcher) {
        this.documentFetcher = documentFetcher;
    }

    /**
     * See {@link #doCheck(String, String)}.
     *
     * @param countryCode 2 character ISO country code. Note: Greece is EL, not GR. See http://ec.europa.eu/taxation_customs/vies/faq.html#item_11
     * @param vatNr       vat number
     * @return the response, see {@link EUVatCheckResponse}
     */
    public EUVatCheckResponse check(String countryCode, String vatNr) {
        return doCheck(countryCode, vatNr, this.documentFetcher);
    }

    static {
        XPath xPath = XPathFactory.newInstance().newXPath();
        try {
            VALID_ELEMENT_MATCHER = xPath.compile("//*[local-name()='checkVatResponse']/*[local-name()='valid']");
            VALID_EXTRACTORS = new XPathExpression[]{
                    VALID_ELEMENT_MATCHER,
                    xPath.compile("//*[local-name()='checkVatResponse']/*[local-name()='name']"),
                    xPath.compile("//*[local-name()='checkVatResponse']/*[local-name()='address']")
            };

            String soapCallTemplate = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
                    "<soapenv:Header/>" +
                    "<soapenv:Body>" +
                    "<checkVat xmlns=\"urn:ec.europa.eu:taxud:vies:services:checkVat:types\">" +
                    "<countryCode></countryCode><vatNumber></vatNumber>" +
                    "</checkVat>" +
                    "</soapenv:Body>" +
                    "</soapenv:Envelope>";

            BASE_DOCUMENT_TEMPLATE = Utils.toDocument(new StringReader(soapCallTemplate));

        } catch (XPathExpressionException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Do a call to the EU vat checker web service.
     *
     * @param countryCode 2 character ISO country code. Note: Greece is EL, not GR. See http://ec.europa.eu/taxation_customs/vies/faq.html#item_11
     * @param vatNumber   the vat number to check
     * @return the response, see {@link EUVatCheckResponse}
     */
    public static EUVatCheckResponse doCheck(String countryCode, String vatNumber) {
        return doCheck(countryCode, vatNumber, Utils::doCall);
    }

    /**
     * See {@link #doCheck(String, String)}. This method accept a documentFetcher if you need to customize the
     * http client.
     *
     * @param countryCode     2 character ISO country code. Note: Greece is EL, not GR. See http://ec.europa.eu/taxation_customs/vies/faq.html#item_11
     * @param vatNumber       the vat number to check
     * @param documentFetcher the function that, given the url of the web service and the body to post, return the resulting body as InputStream
     * @return the response, see {@link EUVatCheckResponse}
     */
    public static EUVatCheckResponse doCheck(String countryCode, String vatNumber, BiFunction<String, String, InputStream> documentFetcher) {
        Objects.requireNonNull(countryCode, "countryCode cannot be null");
        Objects.requireNonNull(vatNumber, "vatNumber cannot be null");
        Map<String, String> params = new HashMap<>();
        params.put("countryCode", countryCode);
        params.put("vatNumber", vatNumber);
        Utils.ExtractionResult res = Utils.doCallAndExtract(BASE_DOCUMENT_TEMPLATE, params, ENDPOINT, documentFetcher, VALID_ELEMENT_MATCHER, VALID_EXTRACTORS);
        if (res.validNode != null) {
            return new EUVatCheckResponse("true".equals(res.extracted.get(0)), res.extracted.get(1), res.extracted.get(2), false, null);
        } else if (res.faultNode != null) {
            return new EUVatCheckResponse(false, null, null, true, new EUVatCheckResponse.Fault(res.extracted.get(0), res.extracted.get(1)));
        } else {
            return new EUVatCheckResponse(false, null, null, true, null); // should not enter here in theory
        }
    }
}
