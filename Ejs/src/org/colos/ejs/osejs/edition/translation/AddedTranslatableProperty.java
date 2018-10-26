package org.colos.ejs.osejs.edition.translation;

import java.util.HashMap;
import java.util.Map;

import org.colos.ejs.library.utils.LocaleItem;
import org.colos.ejs.osejs.utils.FileUtils;

/**
 * TranslatableProperty that can be added by the user
 * @author Paco
 *
 */
class AddedTranslatableProperty implements TranslatableProperty {
  private String name;
  private Map<LocaleItem,String> values = new HashMap<LocaleItem,String>();

  /**
   * Creates the property with default value equal to its name
   * @param _name
   */
  AddedTranslatableProperty(String _name) {
    this.name = FileUtils.removeQuotes(_name);
    values.put(LocaleItem.getDefaultItem(), name);
  }

  public boolean isNameEditable () { return true; }
  
  public void setName (String _name) { 
    name = FileUtils.removeQuotes(_name); 
  }
  
  public String getName() { return name; }
  
  public String getTooltip() { return null; }

  public String getValue(LocaleItem _item) {
    String value = values.get(_item);
    if (value==null) value = values.get(LocaleItem.getDefaultItem());
    return value;
  }
  
  public void setValue(LocaleItem _item, String _value) { 
    if (_value==null) {
      if (!_item.isDefaultItem()) values.remove(_item);
    }
    else {
      values.put(_item, TranslationEditor.removeQuotes(_value));
    }
  }
  
  public int compareTo(TranslatableProperty arg0) {
    if (! (arg0 instanceof AddedTranslatableProperty)) return -1;
    return name.toLowerCase().compareTo(arg0.getName().toLowerCase());
  }
  
//  /**
//   * Returns the set of used LocaleItem 
//   * @return
//   */
//  public Set<LocaleItem> getAvailableLocaleItems() { return values.keySet(); }
  
}
