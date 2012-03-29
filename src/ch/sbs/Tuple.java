package ch.sbs;

import javax.xml.namespace.QName;

public class Tuple {

	  private final String language;
	  private final QName node;

	  public Tuple(String language, QName node) {
	    this.language = language;
	    this.node = node;
	  }

	  public String getLanguage() { return language; }
	  public QName getNode() { return node; }

	  @Override
	  public int hashCode() { return language.hashCode() ^ node.hashCode(); }

	  @Override
	  public boolean equals(Object o) {
	    if (o == null) return false;
	    if (!(o instanceof Tuple)) return false;
	    Tuple other = (Tuple) o;
	    return this.language.equals(other.getLanguage()) &&
	           this.node.equals(other.getNode());
	  }

	}
