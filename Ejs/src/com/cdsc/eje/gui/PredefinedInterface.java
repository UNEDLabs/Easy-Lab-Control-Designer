package com.cdsc.eje.gui;

import java.util.*;
import org.colos.ejs.osejs.utils.ResourceUtil;
import org.colos.ejs.osejs.edition.*;

/*
 * Paco Esquembre
 *
 */

/**
 * Define the interface for Ejs predefined methods
 */

public class PredefinedInterface extends JavaInterface {
        private static int preRowLength1 = 0;
        static private final ResourceUtil res = new ResourceUtil("Resources");

        /**
         * Try to find a class with the name className in the classpath and fill the
         * object with methods and attributes.
         */
        @SuppressWarnings("unchecked")
        public PredefinedInterface(ModelEditor modelEditor) {
                preRowLength1 = 0;
                if (modelEditor!=null) {
                  for (String line : modelEditor.getAllVariables()) {
                    int index = line.indexOf(':');
                    MemberLine memberLine;
                    if (index>0) memberLine = new MemberLine(line.substring(0,index), line.substring(index+1));
                    else memberLine = new MemberLine(line,"");
                    addElement(memberLine);
                    int memberLength = memberLine.toString().length();
                    if (memberLength > preRowLength1) preRowLength1 = memberLength;
                  }
                  for (String line :  modelEditor.getCustomMethods(null)) {
                    int index = line.indexOf(':');
                    MemberLine memberLine;
                    if (index>0) memberLine = new MemberLine(line.substring(0,index), line.substring(index+1));
                    else memberLine = new MemberLine(line,"");
                    addElement(memberLine);
                    int memberLength = memberLine.toString().length();
                    if (memberLength > preRowLength1) preRowLength1 = memberLength;
                  }
                }
                String[] predefinedActions = modelEditor.getPredefinedActions();
                for (int i = 0; i < predefinedActions.length; i++) {
                        String name = predefinedActions[i];
                        String prefix =  name;
                        int index = name.indexOf('(');
                        if (index>0) prefix = prefix.substring(0,index);
                        MemberLine memberLine = new MemberLine("_"+name, res.getString("EditorForVariables."+prefix));
                        addElement(memberLine);
                        int memberLength = memberLine.toString().length();
                        if (memberLength > preRowLength1) preRowLength1 = memberLength;
                }
                for (int i = 0; i < this.size(); i++) {
                        setElementAt(((MemberLine) elementAt(i)).formatToString(), i);
                }
                Collections.sort(this);
        }
}
