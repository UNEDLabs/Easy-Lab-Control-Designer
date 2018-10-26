package org.opensourcephysics.tools.minijar;

public class TestMinijar {

  public static void main(String[] args) {
    // TODO Auto-generated method stub
    MiniJar mj = new MiniJar();
    
    test(mj,"java/lang/Object.class");
    test(mj,"java/awt/Color.class");
    test(mj,"javax/swing/JComponent.class");
    test(mj,"org.opensourcephysics.tools.minijar.MiniJar.class");
    

  }
  
  static private void test(MiniJar mj, String path) {
    System.out.println ("Path: "+path + " is a system class: "+mj.isSystemClass(path));
  }

}
