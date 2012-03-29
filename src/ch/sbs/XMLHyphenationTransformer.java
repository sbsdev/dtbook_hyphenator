package ch.sbs;

import ch.sbs.jhyphen.Hyphenator;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

public class XMLHyphenationTransformer {
	static final String dtb = "http://www.daisy.org/z3986/2005/dtbook/";
	static final String brl = "http://www.daisy.org/z3986/2009/braille/";
	
	//static final char SOFT_HYPHEN = '\u00AD';
	static final char SOFT_HYPHEN = '=';
	
	static final Set<QName> nonHyphenatedElements;
	static final QName xml_lang = new QName("http://www.w3.org/XML/1998/namespace",
			"lang");

	static {
		Set<QName> tmp = new HashSet<QName>();
		tmp.addAll(Arrays.asList(new QName(dtb, "a"), new QName(dtb, "abbr"),
				new QName(dtb, "abbr"), new QName(dtb, "h1"), new QName(dtb,
						"h2"), new QName(dtb, "h3"), new QName(dtb, "h4"),
				new QName(dtb, "h5"), new QName(dtb, "h6"), new QName(brl,
						"running-line"), new QName(brl, "literal"), new QName(
						brl, "num")));
		nonHyphenatedElements = Collections.unmodifiableSet(tmp);
	}

	XMLEventFactory m_eventFactory = XMLEventFactory.newInstance();

	public XMLHyphenationTransformer() {
	}

	public static void main(String[] args) {
		if (args.length < 1) {
			System.out.println("Usage: Specify XML File Name");
		}

		try {
			XMLHyphenationTransformer transformer = new XMLHyphenationTransformer();

			XMLEventReader reader = XMLInputFactory.newInstance()
					.createXMLEventReader(new java.io.FileInputStream(args[0]));
			XMLEventWriter writer = XMLOutputFactory.newInstance()
					.createXMLEventWriter(System.out);

			boolean hyphenate = true;
			Stack<Tuple> languages = new Stack<Tuple>();

			while (reader.hasNext()) {
				XMLEvent event = (XMLEvent) reader.next();

				if (event.isStartElement()) {
					if (event.asStartElement().getAttributeByName(xml_lang) != null) {
						StartElement element = event.asStartElement();
						languages.push(new Tuple(element.getName(), 
								new Hyphenator(element.getAttributeByName(xml_lang).getValue())));
					}
					if (nonHyphenatedElements.contains(event.asStartElement()
							.getName())) {
						hyphenate = false;
					}
					writer.add(event);
				} else if (event.isEndElement()) {
					if (event.asEndElement().getName()
							.equals(languages.peek().getNode())) {
						languages.peek().getHyphenator().close();
						languages.pop();
					}
					if (nonHyphenatedElements.contains(event.asEndElement()
							.getName())) {
						hyphenate = true;
					}
					writer.add(event);
				} else if (event.isCharacters()) {
					if (hyphenate && !languages.empty()) {
						writer.add(transformer.hyphenate(event.asCharacters(),
								languages.peek().getHyphenator()));
					} else {
						writer.add(event);
					}
				} else {
					writer.add(event);
				}
			}
			writer.flush();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	Characters hyphenate(Characters event, Hyphenator hyphenator) {
		return m_eventFactory.createCharacters(hyphenator.hyphenate(event.getData(), SOFT_HYPHEN));
	}
}