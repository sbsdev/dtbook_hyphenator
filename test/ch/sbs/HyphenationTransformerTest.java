package ch.sbs;

import static org.junit.Assert.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.UnsupportedCharsetException;

import javax.xml.stream.XMLStreamException;

import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Test;
import org.xml.sax.SAXException;

public class HyphenationTransformerTest {
	
	static {
		XMLUnit.setIgnoreWhitespace(true);
	}
	
	@Test
	public void testMisc()
			throws UnsupportedCharsetException, FileNotFoundException, XMLStreamException, IOException, SAXException {
		
		String unhyphenated = "<dtbook version=\"2005-3\"" +
				  			  "        xmlns=\"http://www.daisy.org/z3986/2005/dtbook/\"" +
							  "        xml:lang=\"de-DE\">" +
							  "  <book>" +
							  "    <bodymatter>" +
							  "      <level1>" +
							  "        <h1>Dampfschiff</h1>" +
							  "        <p>Dampfschiff</p>" +
							  "        <p>Dampf-schiff</p>" +
							  "        <p><a>www.dampfschiff.ch</a></p>" +
							  "      </level1>" +
							  "    </bodymatter>" +
							  "  </book>" +
							  "</dtbook>";

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
