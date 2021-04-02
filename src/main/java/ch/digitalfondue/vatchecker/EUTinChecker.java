/*
 *
 * Copyright © 2018-2021 digitalfondue (info@digitalfondue.ch)
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
import org.w3c.dom.Node;

import javax.xml.xpath.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Objects;
import java.util.function.BiFunction;

/**
 * A small utility for calling the TIN webservice. See https://ec.europa.eu/taxation_customs/tin/ .
 *
 * The main entry points are {@link #doCheck(String, String)} and if more customization is needed {@link #doCheck(String, String, BiFunction)}.
 */
public class EUTinChecker {

    private static final String ENDPOINT = "https://ec.europa.eu/taxation_customs/tin/services/checkTinService";
    private final BiFunction<String, String, InputStream> documentFetcher;

    private static final Document BASE_DOCUMENT_TEMPLATE;
    private static final XPathExpression VALID_ELEMENT_MATCHER;
    private static final XPathExpression VALID_STRUCTURE_MATCHER;
    private static final XPathExpression VALID_SYNTAX_MATCHER;

    static {
        String soapCallTemplate = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
                "<soapenv:Header/>" +
                "<soapenv:Body>" +
                "<checkTin xmlns=\"urn:ec.europa.eu:taxud:tin:services:checkTin:types\">" +
                "<countryCode></countryCode><tinNumber></tinNumber>" +
                "</checkTin>" +
                "</soapenv:Body>" +
                "</soapenv:Envelope>";

        BASE_DOCUMENT_TEMPLATE = Utils.toDocument(new StringReader(soapCallTemplate));
        XPath xPath = XPathFactory.newInstance().newXPath();
        try {
            VALID_ELEMENT_MATCHER = xPath.compile("//*[local-name()='checkTinResponse']");
            VALID_STRUCTURE_MATCHER = xPath.compile("//*[local-name()='checkTinResponse']/*[local-name()='validStructure']");
            VALID_SYNTAX_MATCHER = xPath.compile("//*[local-name()='checkTinResponse']/*[local-name()='validSyntax']");
        } catch (XPathExpressionException e) {
            throw new IllegalStateException(e);
        }
    }

    public EUTinChecker() {
        this(Utils::doCall);
    }

    public EUTinChecker(BiFunction<String, String, InputStream> documentFetcher) {
        this.documentFetcher = documentFetcher;
    }

    /**
     * See {@link #doCheck(String, String)}.
     *
     * @param countryCode 2 character ISO country code. Note: Greece is EL, not GR.
     * @param tinNr TIN number
     * @return the response, see {@link EUTinCheckResponse}
     */
    public EUTinCheckResponse check(String countryCode, String tinNr) {
        return doCheck(countryCode, tinNr, this.documentFetcher);
    }

    /**
     * Do a call to the EU tin checker web service.
     *
     * @param countryCode 2 character ISO country code. Note: Greece is EL, not GR.
     * @param tinNr the tin number to check
     * @return the response, see {@link EUTinCheckResponse}
     */
    public static EUTinCheckResponse doCheck(String countryCode, String tinNr) {
        return doCheck(countryCode, tinNr, Utils::doCall);
    }

    /**
     * See {@link #doCheck(String, String)}. This method accept a documentFetcher if you need to customize the
     * http client.
     *
     * @param countryCode 2 character ISO country code. Note: Greece is EL, not GR.
     * @param tinNumber TIN number
     * @param documentFetcher the function that, given the url of the web service and the body to post, return the resulting body as InputStream
     * @return the response, see {@link EUTinCheckResponse}
     */
    public static EUTinCheckResponse doCheck(String countryCode, String tinNumber, BiFunction<String, String, InputStream> documentFetcher) {
        Objects.requireNonNull(countryCode, "countryCode cannot be null");
        Objects.requireNonNull(tinNumber, "tinNumber cannot be null");
        try {
            HashMap<String, String> params = new HashMap<>();
            params.put("countryCode", countryCode);
            params.put("tinNumber", tinNumber);
            String body = Utils.prepareTemplate(BASE_DOCUMENT_TEMPLATE, params);
            try (InputStream is = documentFetcher.apply(ENDPOINT, body); Reader isr = new InputStreamReader(is, StandardCharsets.UTF_8)) {
                Document result = Utils.toDocument(isr);
                Node validNode = (Node) VALID_ELEMENT_MATCHER.evaluate(result, XPathConstants.NODE);
                Node faultNode = (Node) Utils.SOAP_FAULT_MATCHER.evaluate(result, XPathConstants.NODE);
                if (validNode != null) {
                    Node validStructure = (Node) VALID_STRUCTURE_MATCHER.evaluate(result, XPathConstants.NODE);
                    Node validSyntax = (Node) VALID_SYNTAX_MATCHER.evaluate(result, XPathConstants.NODE);
                    return new EUTinCheckResponse("true".equals(Utils.textNode(validSyntax)), "true".equals(Utils.textNode(validStructure)), false, null);
                } else if (faultNode != null) {
                    Node faultCode = (Node) Utils.SOAP_FAULT_CODE_MATCHER.evaluate(result, XPathConstants.NODE);
                    Node faultString = (Node) Utils.SOAP_FAULT_STRING_MATCHER.evaluate(result, XPathConstants.NODE);
                    return new EUTinCheckResponse(false, false, true, new EUTinCheckResponse.Fault(Utils.textNode(faultCode), Utils.textNode(faultString)));
                } else {
                    return new EUTinCheckResponse(false, false, true, null); // should not enter here in theory
                }
            }
        } catch (IOException | XPathExpressionException e) {
            throw new IllegalStateException(e);
        }
    }

}
