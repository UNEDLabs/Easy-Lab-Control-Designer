package org.colos.ejs.osejs.utils;

import java.io.IOException;
import java.util.Scanner;
//import org.eclipse.core.runtime.*;
//import org.eclipse.core.internal.runtime.Messages;
//import org.eclipse.core.internal.runtime.Messages;
//import org.eclipse.jdt.internal.compiler.messages;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit; 
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.StringLiteral;

public class MethodParser {
	//use ASTParse to parse string
	public static String parsedTxt = "";
	public void parse(String Sstr) {
		char[] str = ("class a {int a =" + Sstr).toCharArray();   //Add a class header and definition
		//messages dd = new messages();
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setSource(str);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		//NullProgressMonitor s = new NullProgressMonitor();
		final CompilationUnit cu = (CompilationUnit) parser.createAST(null);
		cu.recordModifications();		//Save the modifications in the visited node
		cu.accept(new ASTVisitor() {
			@Override
			@SuppressWarnings({"unchecked", "resource" })
			public boolean visit(MethodInvocation node) {		//Calling _view.XXX.methodxx() is considered a MethodInvocation
				String caller = null;
				Expression exp = node.getExpression();				
				if(exp != null){caller = exp.toString();}			//Extract _view.XXX
			    final String nodeName = node.getName().toString();	//Method name
			    //final String caller1 = caller.substring(0, caller.indexOf("."));	// _view
			    final String caller2 = caller.substring(caller.indexOf(".")+1);		//view element
			    SimpleName sn = node.getName();
			    sn.setIdentifier("sendViewCommand");	//Change the method to sendViewCommand
			    node.setExpression(node.getAST().newName("_simulation"));	//Change object to _simulation
			    
			    StringLiteral elementLiteral = node.getAST().newStringLiteral();	//To Add a parameter with \" we need to define a literal first
			    elementLiteral = node.getAST().newStringLiteral();
			    elementLiteral.setLiteralValue(caller2);
			    node.arguments().add(0,elementLiteral);			//Name of view element as string literal
			    
			    node.arguments().add(1,node.getAST().newName("_htmlView"+"."+caller2));		//SocketViewElement to communicate with javascript
			    
			    StringLiteral methodLiteral = node.getAST().newStringLiteral();	//To Add a parameter with \" we need to define a literal first
			    methodLiteral = node.getAST().newStringLiteral();
			    methodLiteral.setLiteralValue(nodeName);
			    node.arguments().add(2,methodLiteral);			// MethodName as string literal
			    
				Scanner scanner = new Scanner(cu.toString());
			    String nodeLines[] = new String[5]; 
			    int numLines = 0;
				while (scanner.hasNextLine()) {					//Extract all the lines
					nodeLines[numLines] = scanner.nextLine();
					numLines++;
				}
				if (numLines == 4)		parsedTxt = " " + nodeLines[2];					//Deleting initial header and definition 
				else if (numLines == 3) parsedTxt = "   " + nodeLines[1].substring(8);
			    return true;
			}
		});
	}
	//loop directory to get file list
	public String parseTxt(String str) throws IOException{
		StringBuffer txt = new StringBuffer();
		Scanner scanner = new Scanner(str);
		while (scanner.hasNextLine()) {
		  String line = scanner.nextLine();
		  //System.out.println("FullLine: " + line);
      String[] commentsLine = line.split("//");
		  line = commentsLine[0];
      //System.out.println("WithOutComments: " + line);
		  if (line.contains("_view.")){
			  parse(line);
			  txt.append(parsedTxt);
	      //System.out.println("ParsedLine: " + parsedTxt);
		  }else{
			  txt.append(line);
		  }
		  if (commentsLine.length > 1){
		    txt.append("//" + commentsLine[1]);
		    //System.out.println("CommentsLine: " + "//" + commentsLine[1]);
		  }
		  txt.append("\n");
		}
		scanner.close();
		//System.out.println("================= Lines Extracted =================");
		//System.out.println(txt);
		//System.out.println("================== Original Lines ==================");
		//System.out.println(str);
		
		return txt.toString();
		
	}
 
	/*public static void main(String[] args) throws IOException {
		String texto = "   double[] xT = {100.0, 101.0, 102.0, 103.0};  \n   int un = patata.huevo(codigo);    \n   double[] yT = {100.0, 101.0, 102.0, 103.0};      \n   _view.shape.setY(2);  \n   int fg = _view.shape.setY(3,_view.trail.addPoints(new Object[] {xT,yT} )); \n  }";
		String textoProcesado = ParseTxt(texto);
		System.out.println("================= Lines Extracted =================");
		System.out.println(textoProcesado);
		System.out.println("===================================================");
	}*/
}

