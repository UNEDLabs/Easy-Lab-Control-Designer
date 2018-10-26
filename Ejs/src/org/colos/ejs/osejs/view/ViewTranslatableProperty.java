package org.colos.ejs.osejs.view;

import org.colos.ejs.library.utils.LocaleItem;
import org.colos.ejs.osejs.edition.translation.TranslatableProperty;
import org.colos.ejs.osejs.edition.translation.TranslationEditor;

/**
 * Holds information about a property that has been or can be translated
 * @author Paco
 *
 */
class ViewTranslatableProperty implements TranslatableProperty {
  ViewElement element;
  String property;
  
  ViewTranslatableProperty(ViewElement _element, String _property) {
    this.element = _element;
    this.property = _property;
  }

  public boolean isNameEditable() { return false; }

  public void setName(String name) { }
  
  public String getName() { return "View."+element.getName()+"."+property; }
  
  public String getTooltip() { 
    return element.getName()+" : " + element.getResourceString(property,false) + " : " + element.getResourceString(property,true); 
  }

  public String getValue(LocaleItem _item) { return element.getTranslatedProperty( _item, property); }
  
  public void setValue(LocaleItem _item, String _value) { 
    element.setTranslatedProperty( _item, property, TranslationEditor.removeQuotes(_value)); 
  }
  
  public int compareTo(TranslatableProperty arg0) {
    if (! (arg0 instanceof ViewTranslatableProperty)) return 1;
    return getName().toLowerCase().compareTo(arg0.getName().toLowerCase());
  }

}
