package org.colos.ejss.xml;


public interface ErrorOutput {

  public void clear ();

  public void message (String _prefix, String _text);

  public void println (String _text);
  
}