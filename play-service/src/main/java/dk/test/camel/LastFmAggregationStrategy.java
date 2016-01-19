package dk.test.camel;

import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.apache.camel.Exchange;
import org.apache.camel.processor.aggregate.AggregationStrategy;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

public class LastFmAggregationStrategy implements AggregationStrategy {

    @Override
    public Exchange aggregate(Exchange oldExchange, Exchange newExchange) {
        Track track = oldExchange.getIn().getBody(Track.class);
        oldExchange.setProperty(Exchange.CHARSET_NAME, "UTF-8");
        newExchange.setProperty(Exchange.CHARSET_NAME, "UTF-8");
        String body = newExchange.getIn().getBody(String.class);
        DocumentBuilderFactory factoryBuilder = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        try {
            builder = factoryBuilder.newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new StringReader(body)));
            XPathFactory factory = XPathFactory.newInstance();
            XPath xpath = factory.newXPath();
            track.setAlbum(getStringFromXPath(doc, xpath, "/lfm/track/album/title/text()"));
            track.setAlbumImageUrl(getStringFromXPath(doc, xpath, "/lfm/track/album/image[@size='medium']/text()"));
        } catch (Exception e) {
        }

        return oldExchange;
    }

    private String getStringFromXPath(Document doc, XPath xpath, String expression) throws Exception {
        XPathExpression expr = xpath.compile(expression);
        return expr.evaluate(doc, XPathConstants.STRING).toString();
    }

}
