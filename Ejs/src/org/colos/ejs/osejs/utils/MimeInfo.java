package org.colos.ejs.osejs.utils;

import java.io.File;

public class MimeInfo {
  String mTargetFolder;
  String mMimeType;

  private MimeInfo (String folder, String type) {
    mTargetFolder = folder;
    mMimeType = type;
  }

  public String getMimeType() { return mMimeType; }
  
  public String getTargetFolder() { return mTargetFolder; }
  
  static public boolean shouldBeConvertedToBase64(File file) {
    String extension = org.colos.ejs.osejs.utils.FileUtils.getPlainNameAndExtension(file).getSecondString().toLowerCase();
    String targetFolder = infoFor(extension).mTargetFolder;
    if (targetFolder.equals("Images")) return true;
    //if (targetFolder.equals("Audio"))  return true;
    return false;
  }

  static public String mimeTypeFor(String extension) {
    return infoFor(extension).mMimeType;
  }

  static public MimeInfo infoFor(String extension) {
    if (extension.equals("xhtml") || extension.equals("xhtml") || 
        extension.equals("xht") ) return new MimeInfo("Text","application/xhtml+xml");

    if (extension.equals("xml"))  return new MimeInfo("Misc","application/xhtml+xml");

    if (extension.equals("html") || extension.equals("htm")) return new MimeInfo("Text","text/html"); // Shoudl it be Misc

    if (extension.equals("gif"))                             return new MimeInfo("Images","image/gif");
    if (extension.equals("png"))                             return new MimeInfo("Images","image/png");
    if (extension.equals("jpg") || extension.equals("jpeg") || 
        extension.equals("jpe") || extension.equals("jfif") || 
        extension.equals("jfi") || extension.equals("jif"))  return new MimeInfo("Images","image/jpeg");
    if (extension.equals("svg") || extension.equals("svgz")) return new MimeInfo("Images","image/svg+xml");

    if (extension.equals("m4a") || extension.equals("m4b") || 
        extension.equals("m4p") || extension.equals("m4v") || 
        extension.equals("m4r") || extension.equals("3gp") || 
        extension.equals("mp4") || extension.equals("aac")) return new MimeInfo("Audio","audio/mp4");
    if (extension.equals("midi")|| extension.equals("mid")) return new MimeInfo("Audio","application/midi");

    if (extension.equals("mp3")) return new MimeInfo("Audio","audio/mpeg");

    if (extension.equals("mp4") || extension.equals("mpg") ||
        extension.equals("mpeg") ) return new MimeInfo("Video","audio/mp4");

    if (extension.equals("css")) return new MimeInfo("Styles","text/css");

    if (extension.equals("js"))  return new MimeInfo("Misc","text/javascript");

    if (extension.equals("woff"))  return new MimeInfo("Fonts","application/font-woff");
    if (extension.equals("woff2")) return new MimeInfo("Fonts","application/font-woff2");
    if (extension.equals("ttf"))   return new MimeInfo("Fonts","application/x-font-truetype");
    if (extension.equals("otf"))   return new MimeInfo("Fonts","application/x-font-opentype");
    if (extension.equals("sfnt"))  return new MimeInfo("Fonts","application/font-sfnt");
    if (extension.equals("eot"))   return new MimeInfo("Fonts","application/vnd.ms-fontobject");

    if (extension.equals("pdf"))   return new MimeInfo("Misc","application/pdf");
    
    return new MimeInfo("Misc","text/"+extension);
  }

}