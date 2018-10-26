package org.colos.ejs.library.utils;

public class HtmlPageInfo {
  private LocaleItem item;
  private String title;
  private String link;

  public HtmlPageInfo(LocaleItem _item, String _title, String _link) {
    this.item = _item;
    this.title = _title;
    this.link  = _link;
  }
  
  public LocaleItem getLocaleItem() { return item; }
  
  public String getTitle() { return title; }
  
  public String getLink() { return link; }
}
