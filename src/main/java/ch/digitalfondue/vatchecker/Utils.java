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
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

class Utils {

    private static final XPathExpression SOAP_FAULT_MATCHER;
    private static final XPathExpression SOAP_FAULT_CODE_MATCHER;
    private static final XPathExpression SOAP_FAULT_STRING_MATCHER;

    static {
        XPath xPath = XPathFactory.newInstance().newXPath();
        try {
            SOAP_FAULT_MATCHER = xPath.compile("//*[local-name()='Fault']");
            SOAP_FAULT_CODE_MATCHER = xPath.compile("//*[local-name()='Fault']/*[local-name()='faultcode']");
            SOAP_FAULT_STRING_MATCHER = xPath.compile("//*[local-name()='Fault']/*[local-name()='faultstring']");
        } catch (XPathExpressionException e) {
            throw new IllegalStateException(e);
        }
    }

    private static Document copyDocument(Document document) {
        try {
            Transformer tx = getTransformer();
            DOMSource source = new DOMSource(document);
            DOMResult result = new DOMResult();
            tx.transform(source, result);
            return (Document) result.getNode();
        } catch (TransformerException e) {
            throw new IllegalStateException(e);
        }
    }

    private static Transformer getTransformer() throws TransformerConfigurationException {
        TransformerFactory tf = TransformerFactory.newInstance();
        setAttribute(tf, XMLConstants.FEATURE_SECURE_PROCESSING, true);
        setAttribute(tf, XMLConstants.ACCESS_EXTERNAL_DTD, "");
        setAttribute(tf, XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
        return tf.newTransformer();
    }

    private static void setAttribute(TransformerFactory tf, String key, Object value) {
        try {
            tf.setAttribute(key, value);
        } catch (IllegalArgumentException e) {
            // ignore
        }
    }

    static Document toDocument(Reader reader) {
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            dbFactory.setNamespaceAware(true);
            //
            setFeature(dbFactory, "http://apache.org/xml/features/disallow-doctype-decl", true);
            setFeature(dbFactory,"http://xml.org/sax/features/external-general-entities", false);
            setFeature(dbFactory,"http://xml.org/sax/features/external-parameter-entities", false);
            setFeature(dbFactory,"http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            dbFactory.setXIncludeAware(false);
            dbFactory.setExpandEntityReferences(false);
            //
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            return dBuilder.parse(new InputSource(reader));
        } catch (ParserConfigurationException | IOException | SAXException e) {
            throw new IllegalStateException(e);
        }
    }

    private static void setFeature(DocumentBuilderFactory dbFactory, String feature, boolean value) {
        try {
            dbFactory.setFeature(feature, value);
        } catch (ParserConfigurationException e) {
            // ignore
        }
    }

    private static String fromDocument(Document doc) {
        try {
            DOMSource domSource = new DOMSource(doc);
            Transformer transformer = getTransformer();
            StringWriter sw = new StringWriter();
            StreamResult sr = new StreamResult(sw);
            transformer.transform(domSource, sr);
            return sw.toString();
        } catch (TransformerException e) {
            throw new IllegalStateException(e);
        }
    }

    static InputStream doCall(String endpointUrl, String document) {
        try {
            URL url = new URL(endpointUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "text/xml;charset=UTF-8");
            conn.setDoOutput(true);
            try (OutputStream os = conn.getOutputStream()) {
                os.write(document.getBytes(StandardCharsets.UTF_8));
                os.flush();
            }
            return conn.getInputStream();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private static String prepareTemplate(Document document, Map<String, String> data) {
        Document doc = copyDocument(document);
        for (Map.Entry<String, String> kv : data.entrySet()) {
            doc.getElementsByTagName(kv.getKey()).item(0).setTextContent(kv.getValue());
        }
        return fromDocument(doc);
    }

    private static String textNode(Node node) {
        return node != null ? node.getTextContent() : null;
    }

    static class ExtractionResult {
        final Node validNode;
        final Node faultNode;
        final List<String> extracted;

        ExtractionResult(Node validNode, Node faultNode, List<String> extracted) {
            this.validNode = validNode;
            this.faultNode = faultNode;
            this.extracted = extracted;
        }
    }

    static ExtractionResult doCallAndExtract(Document document,
                                  Map<String, String> params,
                                  String endpointUrl,
                                  BiFunction<String, String, InputStream> documentFetcher,
                                  XPathExpression validElementMatcher,
                                  XPathExpression[] validElementExtractors) {
        String body = Utils.prepareTemplate(document, params);
        try (InputStream is = documentFetcher.apply(endpointUrl, body); Reader isr = new InputStreamReader(is, StandardCharsets.UTF_8)) {
            Document result = Utils.toDocument(isr);
            Node validNode = (Node) validElementMatcher.evaluate(result, XPathConstants.NODE);
            Node faultNode = (Node) SOAP_FAULT_MATCHER.evaluate(result, XPathConstants.NODE);
            List<String> extracted = new ArrayList<>(validElementExtractors.length);
            if (validNode != null) {
                for (XPathExpression exp : validElementExtractors) {
                    extracted.add(textNode((Node) exp.evaluate(result, XPathConstants.NODE)));
                }
            } else if (faultNode != null) {
                extracted.add(textNode((Node) Utils.SOAP_FAULT_CODE_MATCHER.evaluate(result, XPathConstants.NODE)));
                extracted.add(textNode((Node) Utils.SOAP_FAULT_STRING_MATCHER.evaluate(result, XPathConstants.NODE)));
            }
            return new ExtractionResult(validNode, faultNode, extracted);
        } catch (IOException | XPathExpressionException e) {
            throw new IllegalStateException(e);
        }
    }
}
