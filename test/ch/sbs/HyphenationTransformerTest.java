package ch.sbs;

import static org.junit.Assert.*;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.UnsupportedCharsetException;

import javax.xml.stream.XMLStreamException;

import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Ignore;
import org.junit.Test;
import org.xml.sax.SAXException;

public class HyphenationTransformerTest {
	
	static {
		XMLUnit.setIgnoreWhitespace(true);
	}
	
	@Test
	public void testMisc()
			throws UnsupportedCharsetException, FileNotFoundException, XMLStreamException, IOException, SAXException {

		String correctlyHyphenated = "<dtbook version=\"2005-3\"" +
									 "        xmlns=\"http://www.daisy.org/z3986/2005/dtbook/\"" +
									 "        xml:lang=\"de-DE\">" +
									 "  <book>" +
									 "    <bodymatter>" +
									 "      <level1>" +
									 "        <h1>Dampfschiff</h1>" +
									 "        <p>Dampf\u00ADschiff</p>" +
									 "        <p>Dampf-\u00ADschiff</p>" +
									 "        <p><a>www.dampfschiff.ch</a></p>" +
									 "      </level1>" +
									 "    </bodymatter>" +
									 "  </book>" +
									 "</dtbook>";
		
		checkHyphenation(correctlyHyphenated);
	}
	
	@Test
	public void testVeryLongWord()
			throws UnsupportedCharsetException, FileNotFoundException, XMLStreamException, IOException, SAXException {

		String correctlyHyphenated = "<dtbook version=\"2005-3\"" +
									 "        xmlns=\"http://www.daisy.org/z3986/2005/dtbook/\"" +
									 "        xml:lang=\"de-DE\">" +
									 "  <book>" +
									 "    <bodymatter>" +
									 "      <level1>" +
									 "        <p>EinSehr­Lan­ges­Wort­Mit­Stud­ly­Caps­Dass­Sich­Über­Meh­re­re­Zei­len­Hin­weg­ziehtUnd­Zu­de­mEin­fach­Noch­Wahn­sin­nig­Gut­Aus­sieht</p>" +
									 "      </level1>" +
									 "    </bodymatter>" +
									 "  </book>" +
									 "</dtbook>";
		
		checkHyphenation(correctlyHyphenated);
	}
	
	@Test
	public void testBrlComputer()
	    throws UnsupportedCharsetException, FileNotFoundException, XMLStreamException, IOException, SAXException {

	    String unHyphenated = "<dtbook version=\"2005-3\"" +
		"        xmlns=\"http://www.daisy.org/z3986/2005/dtbook/\"" +
		"        xmlns:brl=\"http://www.daisy.org/z3986/2009/braille/\"" +
		"        xml:lang=\"de-DE\">" +
		"  <book>" +
		"    <bodymatter>" +
		"      <level1>" +
		"        <p>Wahnsinnig</p>" +
		"        <brl:computer>Wahnsinnig</brl:computer>" +
		"      </level1>" +
		"    </bodymatter>" +
		"  </book>" +
		"</dtbook>";

	    String correctlyHyphenated = "<dtbook version=\"2005-3\"" +
		"        xmlns=\"http://www.daisy.org/z3986/2005/dtbook/\"" +
		"        xmlns:brl=\"http://www.daisy.org/z3986/2009/braille/\"" +
		"        xml:lang=\"de-DE\">" +
		"  <book>" +
		"    <bodymatter>" +
		"      <level1>" +
		"        <p>Wahn­sin­nig</p>" +
		"        <brl:computer>Wahnsinnig</brl:computer>" +
		"      </level1>" +
		"    </bodymatter>" +
		"  </book>" +
		"</dtbook>";

	    checkHyphenation(correctlyHyphenated);
	}

	@Ignore
	@Test
	public void testLargeFile()
			throws UnsupportedCharsetException, XMLStreamException, SAXException, IOException, URISyntaxException {
		
		String largeFile = getClass().getResource("/ch/sbs/resources/Franz Kafka - Der Prozeß.xml").getFile();
		String largeHyphenatedFile = getClass().getResource("/ch/sbs/resources/Franz Kafka - Der Prozeß [hyphenated].xml").getFile();
		StringWriter hyphenated = new StringWriter();
		new HyphenationTransformer().transform(new FileReader(new URI(largeFile).getPath()), hyphenated);
		hyphenated.flush();
	    Diff myDiff = new Diff(new StringReader(hyphenated.toString()), new FileReader(new URI(largeHyphenatedFile).getPath()));
	    
	    assertTrue("not equal\n" + myDiff, myDiff.identical());
	}
	
	/**
	 * Check hyphenation by first removing all soft hyphens and then inserting them again
	 *   with HyphenationTransformer
	 * @param correctlyHyphenated
	 */
	public static void checkHyphenation(String correctlyHyphenated)
			throws UnsupportedCharsetException, XMLStreamException, SAXException, IOException {
		
		String unhyphenated = correctlyHyphenated.replaceAll("\u00AD", "");
		checkHyphenation(unhyphenated, correctlyHyphenated);
	}
	
	public static void checkHyphenation(String unhyphenated, String correctlyHyphenated)
			throws UnsupportedCharsetException, XMLStreamException, SAXException, IOException {
		
		StringWriter hyphenated = new StringWriter();
		new HyphenationTransformer().transform(new StringReader(unhyphenated), hyphenated);
		hyphenated.flush();
	    Diff myDiff = new Diff(correctlyHyphenated, hyphenated.toString());
        
        assertTrue("not equal\n" + myDiff, myDiff.identical());
	}
}
