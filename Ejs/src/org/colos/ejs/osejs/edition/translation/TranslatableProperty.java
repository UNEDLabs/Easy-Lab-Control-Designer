package org.colos.ejs.osejs.edition.translation;

import org.colos.ejs.library.utils.LocaleItem;

/**
 * Holds information about a property that has been or can be translated
 * @author Paco
 *
 */
public interface TranslatableProperty extends Comparable<TranslatableProperty> {

  /**
   * Whether its name can be edited
   * @return
   */
  public boolean isNameEditable ();
  
  /**
   * Sets its name
   * @param _name
   */
  public void setName (String _name);
  
  /**
   * Return its name
   * @return
   */
  public String getName();
  
  /**
   * Returns its tooltip
   * @return null if it doesn't have one
   */
  public String getTooltip();

  /**
   * Returns the value of the property for the given LocaleItem.
   * If no value has been given for this LocaleItem, the default value (i.e. that for the default LocaleItem is returned)
   * @param _item
   * @return
   */
  public String getValue(LocaleItem _item);
  
  /**
   * Sets the value of the property for the given LocaleItem.
   * If the value is null, the translation is removed (except for the default locale item).
   * @param _item
   * @param _value
   */
  public void setValue(LocaleItem _item, String _value);
  
}
