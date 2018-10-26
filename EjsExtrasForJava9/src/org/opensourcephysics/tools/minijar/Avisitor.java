/**
 * The minijar package contains utilities to create the minimal
 * jar file that contains a series of classes and their dependencies.
 * Copyright (c) January 2008 F. Esquembre
 * @author F. Esquembre (http://www.um.es/fem).
 */

package org.opensourcephysics.tools.minijar;

/*
 * Inspired in Autojar's Avisitor class
 */

import org.apache.bcel.classfile.*;

/** Visitor, handles ConstantClass entries 
 * Inspired in Autojar's Avisitor class
 * @author Francisco Esquembre http://www.um.es/fem
 */

class Avisitor extends EmptyVisitor {
    private JavaClass   klass;
    private int         indexForName;
    private MiniJar     miniJar;

    //----------------------------------------------------------------------

    /** @param klass JavaClass object containing the code */
    
    Avisitor(MiniJar MiniJar, JavaClass klass)    {
        this.miniJar = MiniJar;
        this.klass = klass;
        indexForName = -1;
    }

    //----------------------------------------------------------------------

    int getIndexForName() { return indexForName; }
    
    //----------------------------------------------------------------------

    public void visitConstantClass(ConstantClass cc) {
      String cstr = klass.getConstantPool().getConstant(cc.getNameIndex()).toString();
      // System.out.println ("Visiting contant class "+cstr);
      int     ia = cstr.indexOf('"'), ie = cstr.lastIndexOf('"');
      String  name = cstr.substring(ia + 1, ie);
      if (name.startsWith("[")) return; // skip arrays
      //System.out.println ("Visiting constant class Not array "+name);
      if (name.endsWith(".class")) name = name.substring(0, name.length() - 6);
      name = name.replace('.', '/') + ".class";
      miniJar.processClass(name);

    }

    //----------------------------------------------------------------------

    public void visitConstantMethodref(ConstantMethodref ref)  {
      ConstantPool    pool = klass.getConstantPool();
      String          cstr = ref.getClass(pool);
      //System.out.println ("Visiting constant method ref "+cstr);
      if (cstr.equals("java.lang.Class")) {
        int     iname = ref.getNameAndTypeIndex();
        String  name = ((ConstantNameAndType)pool.getConstant(iname)).getName(pool);
        if (name.equals("forName")) indexForName = iname;
      }
    }
    
}
