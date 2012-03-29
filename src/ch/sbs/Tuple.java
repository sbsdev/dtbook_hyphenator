package ch.sbs;

import ch.sbs.jhyphen.Hyphenator;

import javax.xml.namespace.QName;

public class Tuple {

	  private final QName node;
	  private final Hyphenator hyphenator;

	  public Tuple(QName node, Hyphenator hyphenator) {
	    this.node = node;
	    this.hyphenator = hyphenator;
	  }

	  public QName getNode() { return node; }
	  public Hyphenator getHyphenator() { return hyphenator; }

}
