package ch.sbs;

import ch.sbs.jhyphen.Hyphenator;
import ch.sbs.jhyphen.CompilationException;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.io.File;

import java.nio.charset.UnsupportedCharsetException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.apache.commons.io.input.BOMInputStream;

public class HyphenationTransformer {
	static final String dtb = "http://www.daisy.org/z3986/2005/dtbook/";
	static final String brl = "http://www.daisy.org/z3986/2009/braille/";
	
	static final char SOFT_HYPHEN = '\u00AD';
	static final char ZERO_WIDTH_SPACE = '\u200B';
	
	static final Set<QName> nonHyphenatedElements;
	static final QName xml_lang = new QName("http://www.w3.org/XML/1998/namespace",
			"lang");

        // FIXME: replace new HashMap with Map.of as soon as we move to modern Java
        static final Map<String, File> dictionaries = new HashMap<String, File>() {{
		put("de", new File("/usr/share/hyphen/hyph_de_DE.dic"));
		put("de-CH", new File("/usr/share/hyphen/hyph_de_DE.dic"));
		put("de_CH", new File("/usr/share/hyphen/hyph_de_DE.dic"));
		put("de-DE", new File("/usr/share/hyphen/hyph_de_DE.dic"));
		put("de_DE", new File("/usr/share/hyphen/hyph_de_DE.dic"));
		put("de-1901", new File("/usr/share/hyphen/hyph_de_DE_OLDSPELL.dic"));
		put("de-CH-1901", new File("/usr/share/hyphen/hyph_de_DE_OLDSPELL.dic"));
		put("gsw", new File("/usr/share/hyphen/hyph_de_DE.dic"));
		put("en", new File("/usr/share/hyphen/hyph_en_US.dic"));
	    }};

	// static final Map<String, File> dictionaries = Map.of(
	// 	"de", new File("/usr/share/hyphen/hyph_de_DE.dic"),
	// 	"de-CH", new File("/usr/share/hyphen/hyph_de_DE.dic"),
	// 	"de_CH", new File("/usr/share/hyphen/hyph_de_DE.dic"),
	// 	"de-DE", new File("/usr/share/hyphen/hyph_de_DE.dic"),
	// 	"de_DE", new File("/usr/share/hyphen/hyph_de_DE.dic"),
	// 	"de-1901", new File("/usr/share/hyphen/hyph_de_DE_OLDSPELL.dic"),
	// 	"de-CH-1901", new File("/usr/share/hyphen/hyph_de_DE_OLDSPELL.dic"),
	// 	"gsw", new File("/usr/share/hyphen/hyph_de_DE.dic"),
		
	// 	"en", new File("/usr/share/hyphen/hyph_en_US.dic")
	// );
	
	static {
		Set<QName> tmp = new HashSet<QName>();
		tmp.addAll(Arrays.asList(new QName(dtb, "a"), 
					 new QName(dtb, "abbr"),
					 new QName(dtb, "h1"), 
					 new QName(dtb,	"h2"), 
					 new QName(dtb, "h3"), 
					 new QName(dtb, "h4"),
					 new QName(dtb, "h5"), 
					 new QName(dtb, "h6"),
					 new QName(dtb, "code"),
					 new QName(dtb, "sup"),
					 new QName(dtb, "sub"),
					 new QName(dtb, "th"),
					 new QName(dtb, "td"),
					 new QName(brl, "running-line"),
					 new QName(brl, "literal"),
					 new QName(brl, "computer"),
					 new QName(brl, "num")));
		nonHyphenatedElements = Collections.unmodifiableSet(tmp);
	}

	XMLEventFactory m_eventFactory = XMLEventFactory.newInstance();

	public HyphenationTransformer() {
	}

	public static void main(String[] args) {
		if (args.length < 1) {
			System.out.println("Usage: Specify XML File Name");
			System.exit(1);
		}

		HyphenationTransformer transformer = new HyphenationTransformer();
		
		try {
			transformer.transform(new BOMInputStream(new FileInputStream(args[0])), System.out);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void transform(InputStream input, OutputStream output)
			throws XMLStreamException {
		
		transform(XMLInputFactory.newInstance().createXMLEventReader(input),
				XMLOutputFactory.newInstance().createXMLEventWriter(output));
	}
	
	public void transform(Reader input, Writer output)
			throws XMLStreamException {
		
		transform(XMLInputFactory.newInstance().createXMLEventReader(input),
				XMLOutputFactory.newInstance().createXMLEventWriter(output));
	}
	
	public void transform(XMLEventReader reader, XMLEventWriter writer)
			throws XMLStreamException {

		boolean hyphenate = true;
		Stack<Tuple> languages = new Stack<Tuple>();

		boolean insidePoem = false;
		QName poem = new QName(dtb, "poem");
		QName line = new QName(dtb, "line");

		while (reader.hasNext()) {

			XMLEvent event = reader.nextEvent();
			
			if (event.isStartElement()) {
				if (event.asStartElement().getAttributeByName(xml_lang) != null) {
					StartElement element = event.asStartElement();
					Hyphenator hyphenator = null;
					String lang = element.getAttributeByName(xml_lang).getValue();
					try {
						hyphenator = new Hyphenator(dictionaries.get(lang));
					} catch (UnsupportedCharsetException e) {
					} catch (CompilationException e) {
					} catch (FileNotFoundException e) {
					}
					languages.push(new Tuple(element.getName(), hyphenator));
				}
				if (nonHyphenatedElements.contains(event.asStartElement()
						.getName())) {
					hyphenate = false;
				}
				if (event.asStartElement().getName().equals(poem)) {
					insidePoem = true;
				}
				if (insidePoem && event.asStartElement().getName().equals(line)) {
					hyphenate = false;
				}
				writer.add(event);
			} else if (event.isEndElement()) {
				if (event.asEndElement().getName()
						.equals(languages.peek().getNode())) {
					Hyphenator hyphenator = languages.pop().getHyphenator();
					if (hyphenator != null) { hyphenator.close(); }
				}
				if (nonHyphenatedElements.contains(event.asEndElement()
						.getName())) {
					hyphenate = true;
				}
				if (event.asEndElement().getName().equals(poem)) {
					insidePoem = false;
				}
				if (insidePoem && event.asEndElement().getName().equals(line)) {
					hyphenate = true;
				}
				writer.add(event);
			} else if (event.isCharacters()) {
				if (hyphenate && !languages.empty()) {
					Hyphenator hyphenator = languages.peek().getHyphenator();
					if (hyphenator != null) {
						writer.add(hyphenate(event.asCharacters(), hyphenator));
					} else {
						writer.add(event);
					}
				} else {
					writer.add(event);
				}
			} else {
				writer.add(event);
			}
		}
		writer.flush();
	}

	Characters hyphenate(Characters event, Hyphenator hyphenator) {
	    return m_eventFactory.createCharacters(hyphenator.hyphenate(event.getData(), SOFT_HYPHEN, SOFT_HYPHEN));
	}
}
