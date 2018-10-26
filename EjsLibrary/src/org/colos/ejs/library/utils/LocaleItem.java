package org.colos.ejs.library.utils;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.opensourcephysics.tools.ToolsRes;

/**
 *  A class that encapsulates information about locales. Each locale is encapsulated only once.
 */
public class LocaleItem implements Comparable<LocaleItem> {
  static private final String LABEL_FOR_DEFAULT = "_default_"; //$NON-NLS-1$
  static private final Charset UTF16_CHARSET = java.nio.charset.Charset.forName("UTF-16"); //$NON-NLS-1$

  static private final LocaleItem DEFAULT_ITEM = new LocaleItem(LABEL_FOR_DEFAULT,null);
  static private final Map <String,LocaleItem> LOCALE_MAP = new HashMap<String,LocaleItem>();

  /**
   * The Charset used to save and read the properties files
   * @return
   */
  static public Charset getCharset() { return UTF16_CHARSET; }
  
  /**
   * The default item, which encapsulates a null locale.
   * @return
   */
  static public LocaleItem getDefaultItem() { return DEFAULT_ITEM; }

//  /**
//   * Sets the default Locale for the default LocaleItem.
//   * This is used by a simulation's model to be able to get the original language of the author when selecting "default".
//   * @param _locale
//   */
//  static public void setLocaleForDefault(Locale _locale) { DEFAULT_ITEM.locale = _locale; }
  
  /**
   * Retrieves always the same LocaleItem object for each String description.
   * Accepts either "es", "es ES", or "es_ES" formats as _keyword.
   */
  static public LocaleItem getLocaleItem(String _keyword) {
    if (_keyword.equals(LABEL_FOR_DEFAULT)) return DEFAULT_ITEM;

    // Decipher the language
    String lang, country;
    int l = _keyword.length();
    if (l>=4) {
      lang = _keyword.substring(0,2).toLowerCase();
      country = " "+_keyword.substring(l-2).toUpperCase();
    }
    else if (l>=2) {
      lang = _keyword.substring(0,2);
      country = "";
    }
    else return null;
    // Normalize the keyword so that "en_UK" and "en UK" are the same
    _keyword = lang+country; 
    LocaleItem localeItem = LOCALE_MAP.get(_keyword);
    if (localeItem!=null) return localeItem;
    if (country.length()>0) localeItem = new LocaleItem(_keyword,new Locale(lang,country));
    else localeItem = new LocaleItem(_keyword,new Locale(lang));
    LOCALE_MAP.put(_keyword, localeItem);
    return localeItem;
  }

  /**
   * Gets the LocaleItem corresponding to the default Locale.
   * If the default locale is "en US", and it is not found, but "en" is there, if will return the "en" LocaleItem
   * To be used by Simulation only
   */
  static public LocaleItem getDefaultLocaleItem() {
    Locale locale = Locale.getDefault();
    String lang = locale.getLanguage();
    String country = locale.getCountry();
    String keyword = (country.length()>0) ? lang : lang + " "+country;
    LocaleItem localeItem = LOCALE_MAP.get(keyword);
    if (localeItem!=null) return localeItem;
    localeItem = LOCALE_MAP.get(lang);
    if (localeItem!=null) return localeItem;
    localeItem = new LocaleItem(keyword,locale);
    LOCALE_MAP.put(lang, localeItem);
    return localeItem;
  }

  // --------------------------
  // non-static part
  // --------------------------
  
  private String keyword;
  private String displayName;
  private Locale locale;
  
  private LocaleItem(String _keyword, Locale _locale) {
    keyword = _keyword;
    locale = _locale;
    if (locale==null) displayName = ToolsRes.getString("TranslatorTool.Language.Default");
    else displayName = locale.getDisplayName(locale);
  }

  /**
   * The keyword with which the locale item was created. 
   * Usually the "en" or "en_US" name.
   * @return
   */
  public String getKeyword() { return keyword; }

  /**
   * The Locale object encapsulated by the item
   * @return
   */
  public Locale getLocale() { return locale; }
  
  /**
   * A human-readable name for the locale
   */
  public String toString() { return displayName; }
  
  /**
   * Whether this item encapsulates the default locale
   */
  public boolean isDefaultItem() { return this.equals(DEFAULT_ITEM); }
  
  /**
   * Implemented for comparison purposes
   */
  public int compareTo(LocaleItem arg0) {
    if (locale==null) return -1;
    if (arg0.locale==null) return 1;
    return displayName.compareTo(arg0.displayName);
  }

} // end of private class