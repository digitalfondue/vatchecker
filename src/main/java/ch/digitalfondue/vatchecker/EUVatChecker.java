package ch.digitalfondue.vatchecker;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.function.BiFunction;

/**
 * A small utility for calling the VIES webservice. See http://ec.europa.eu/taxation_customs/vies/ .
 *
 * The main entry points are {@link #doCheck(String, String)} and if more customization is needed {@link #doCheck(String, String, BiFunction)}.
 */
public class EUVatChecker {

    private static final Document BASE_DOCUMENT_TEMPLATE;

    private static final String ENDPOINT = "http://ec.europa.eu/taxation_customs/vies/services/checkVatService";
    private static final XPathExpression VALID_ELEMENT_MATCHER;
    private static final XPathExpression NAME_ELEMENT_MATCHER;
    private static final XPathExpression ADDRESS_ELEMENT_MATCHER;

    static {
        XPath xPath = XPathFactory.newInstance().newXPath();
        try {
            VALID_ELEMENT_MATCHER = xPath.compile("//checkVatResponse/valid");
            NAME_ELEMENT_MATCHER = xPath.compile("//checkVatResponse/name");
            ADDRESS_ELEMENT_MATCHER = xPath.compile("//checkVatResponse/address");

            String soapCallTemplate = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
                    "<soapenv:Header/>" +
                    "<soapenv:Body>" +
                    "<checkVat xmlns=\"urn:ec.europa.eu:taxud:vies:services:checkVat:types\">" +
                    "<countryCode></countryCode><vatNumber></vatNumber>" +
                    "</checkVat>" +
                    "</soapenv:Body>" +
                    "</soapenv:Envelope>";

            BASE_DOCUMENT_TEMPLATE = toDocument(new StringReader(soapCallTemplate));

        } catch (XPathExpressionException e) {
            throw new IllegalStateException(e);
        }
    }

    private static String prepareTemplate(String countryCode, String vatNumber) {
        Document doc = copyDocument(BASE_DOCUMENT_TEMPLATE);
        doc.getElementsByTagName("countryCode").item(0).setTextContent(countryCode);
        doc.getElementsByTagName("vatNumber").item(0).setTextContent(vatNumber);
        return fromDocument(doc);
    }

    private static Document copyDocument(Document document) {
        try {
            Transformer tx   = TransformerFactory.newInstance().newTransformer();
            DOMSource source = new DOMSource(document);
            DOMResult result = new DOMResult();
            tx.transform(source,result);
            return (Document)result.getNode();
        } catch (TransformerException e) {
            throw new IllegalStateException(e);
        }
    }

    private static Document toDocument(Reader reader) {
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            return dBuilder.parse(new InputSource(reader));
        } catch (ParserConfigurationException | IOException | SAXException e) {
            throw new IllegalStateException(e);
        }
    }

    private static String fromDocument(Document doc) {
        try {
            DOMSource domSource = new DOMSource(doc);
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            StringWriter sw = new StringWriter();
            StreamResult sr = new StreamResult(sw);
            transformer.transform(domSource, sr);
            return sw.toString();
        } catch (TransformerException e) {
            throw new IllegalStateException(e);
        }
    }


    private static InputStream doCall(String endpointUrl, String document) {
        try {
            URL url = new URL(endpointUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "text/xml;charset=UTF-8");
            conn.setDoOutput(true);
            conn.getOutputStream().write(document.getBytes(StandardCharsets.UTF_8));
            conn.getOutputStream().flush();
            return conn.getInputStream();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Do a call to the EU vat checker web service.
     *
     * @param countryCode 2 character ISO country code. Note: Greece is EL, not GR.
     * @param vatNumber the vat number to check
     * @return
     */
    public static EUVatCheckResponse doCheck(String countryCode, String vatNumber) {
        return doCheck(countryCode, vatNumber, EUVatChecker::doCall);
    }

    /**
     * See {@link #doCheck(String, String)}. This method accept a documentFetcher if you need to customize the
     * http client.
     *
     * @param countryCode 2 character ISO country code. Note: Greece is EL, not GR.
     * @param vatNumber the vat number to check
     * @param documentFetcher the function that, given the url of the web service and the body to post, return the resulting body as InputStream
     * @return
     */
    public static EUVatCheckResponse doCheck(String countryCode, String vatNumber, BiFunction<String, String, InputStream> documentFetcher) {
        try {
            String body = prepareTemplate(countryCode, vatNumber);
            try (InputStream is = documentFetcher.apply(ENDPOINT, body); Reader isr = new InputStreamReader(is, StandardCharsets.UTF_8)) {
                Document result = toDocument(isr);
                Node validNode = (Node) VALID_ELEMENT_MATCHER.evaluate(result, XPathConstants.NODE);
                if (validNode != null) {
                    Node nameNode = (Node) NAME_ELEMENT_MATCHER.evaluate(result, XPathConstants.NODE);
                    Node addressNode = (Node) ADDRESS_ELEMENT_MATCHER.evaluate(result, XPathConstants.NODE);
                    return new EUVatCheckResponse("true".equals(textNode(validNode)), textNode(nameNode), textNode(addressNode));
                } else {
                    return new EUVatCheckResponse(false, null, null);
                }
            }
        } catch (IOException | XPathExpressionException e) {
            throw new IllegalStateException(e);
        }
    }

    private static final String textNode(Node node) {
        return node != null ? node.getTextContent() : null;
    }
}
