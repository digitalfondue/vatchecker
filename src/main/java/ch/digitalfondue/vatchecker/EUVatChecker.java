package ch.digitalfondue.vatchecker;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class EUVatChecker {


    private static String SOAP_CALL_TEMPLATE = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
            "<soapenv:Header/>" +
            "<soapenv:Body>" +
            "<checkVat xmlns=\"urn:ec.europa.eu:taxud:vies:services:checkVat:types\">" +
            "<countryCode></countryCode><vatNumber></vatNumber>" +
            "</checkVat>" +
            "</soapenv:Body>" +
            "</soapenv:Envelope>";

    private static final String ENDPOINT = "http://ec.europa.eu/taxation_customs/vies/services/checkVatService";


    private static String prepareTemplate(String countryCode, String vatNumber) {
        Document doc = toDocument(new StringReader(SOAP_CALL_TEMPLATE));
        doc.getElementsByTagName("countryCode").item(0).setTextContent(countryCode);
        doc.getElementsByTagName("vatNumber").item(0).setTextContent(vatNumber);
        return fromDocument(doc);
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

    static String doCheck(String countryCode, String vatNumber) {
        try {
            String body = prepareTemplate(countryCode, vatNumber);
            URL url = new URL(ENDPOINT);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "text/xml;charset=UTF-8");
            conn.setDoOutput(true);
            conn.getOutputStream().write(body.getBytes(StandardCharsets.UTF_8));
            conn.getOutputStream().flush();

            try (InputStream is = conn.getInputStream(); Reader isr = new InputStreamReader(is, StandardCharsets.UTF_8)) {
                Document result = toDocument(isr);
                return fromDocument(result);
            }
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
