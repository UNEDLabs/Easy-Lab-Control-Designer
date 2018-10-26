package org.colos.ejs.osejs.edition.html_view;

public class SizeOption {

  private String mName;
  private int mWidth;
  private int mHeight;
  
  public SizeOption(String name, int width, int height) {
    mName = name;
    mWidth = width;
    mHeight = height;
  }
  
  public String toString() {
    return mName;
  }

  public int getWidth() {
    return mWidth;
  }

  public int getHeight() {
    return mHeight;
  }

}
