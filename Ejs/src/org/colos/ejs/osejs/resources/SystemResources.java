/**
 * The resource package contains utils and definitions for
 * multilingual use of the whole project
 * Copyright (c) November 2001 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.osejs.resources;

//See also org.colos.ejs.osejs._EjsSConstants
//See also org.colos.ejs.library._EjsConstants

public class SystemResources extends java.util.ListResourceBundle {
  public       Object[][] getContents() { return contents;}
  static final Object[][] contents = {

{"Osejs.File.Extension"    ,"ejs,xml,zip,ejss,ejsh"},
{"Osejs.Name"              ,"Easy Java(script) Simulations"},
{"Osejs.Title"             ,"EjsS"},

{"Osejs.MainToolBar"       ,"Description Model View HtmlView "}, // HtmlView Experiment
{"Osejs.Main.Description"  ,"org.colos.ejs.osejs.edition.DescriptionEditor"},
{"Osejs.Main.Model"        ,"org.colos.ejs.osejs.edition.ModelEditor"},
{"Osejs.Main.View"         ,"org.colos.ejs.osejs.edition.ViewEditor"},
{"Osejs.Main.Experiment"   ,"org.colos.ejs.osejs.edition.ExperimentEditor"},
{"Osejs.Main.HtmlView"     ,"org.colos.ejs.osejs.edition.Html5Editor"},

{"Osejs.IconToolBar"       ,"New - Open Save SaveAs Search - Run Package - Options Info"},// - ReadFromServer SaveToServer Net"},
{"Osejs.Icon.New"          ,"data/icons/file.gif"},
{"Osejs.Icon.Open"         ,"data/icons/openSmall.gif"},
{"Osejs.Icon.Merge"        ,"data/icons/merge.gif"},
{"Osejs.Icon.Save"         ,"data/icons/saveSmall.gif"},
{"Osejs.Icon.SaveAs"       ,"data/icons/saveAsSmall.gif"},
{"Osejs.Icon.Search"       ,"data/icons/find.gif"},
{"Osejs.Icon.Run"          ,"data/icons/launch.gif"},
{"Osejs.Icon.Running"      ,"data/icons/stop.png"},
{"Osejs.Icon.Package"      ,"data/icons/package.gif"},
{"Osejs.Icon.Options"      ,"data/icons/edit.gif"},
{"Osejs.Icon.Font"         ,"data/icons/font.gif"},
{"Osejs.Icon.Info"         ,"data/icons/info.gif"},
{"Osejs.Icon.Translate"    ,"data/icons/translate.gif"},
{"Osejs.Icon.SimInfo"     ,"data/icons/properties.gif"},
{"Osejs.Icon.ReadFromServer"     ,"data/icons/netOpen.gif"},
//{"Osejs.Icon.SaveToServer"       ,"data/icons/saveNet3.gif"},
{"Osejs.Icon.EjsIcon"     ,"data/icons/EjsIcon.gif"},
{"Osejs.Icon.ConsoleIcon" ,"data/icons/ConsoleIcon.gif"},
{"Osejs.Icon.ConsoleIcon" ,"data/icons/ConsoleIcon.gif"},
{"Exclamation.Icon"       ,"data/icons/exclamation.gif"},
{"Osejs.Icon.Up"          ,"data/icons/font_plus.png"},
{"Osejs.Icon.Down"        ,"data/icons/font_minus.png"},

{"VariablesEditor.VariableTypes"      ,"boolean int double String Object"},
{"VariablesEditor.VariableDomains"    ,"input output protected public"},

{"EquationEditor.Select.Icon"         ,"data/icons/select.gif"},
{"EquationEditor.Events.EditIcon"     ,"data/icons/write.gif"},
{"EquationEditor.Events.Solvers"      ,"Bisection"},
{"EquationEditor.Events.Bisection"    ,"ODEBisectionEventSolver"},

{"EquationEditor.ODESolvers"          ,"Euler MidPoint VelocityVerlet BogackiShampine23 RungeKutta RungeKuttaFehlberg Dopri5 Dopri853 Fehlberg8 Fehlberg78 Radau5"}, 
{"EquationEditor.DefaultSolver"       ,"RungeKuttaFehlberg"},
{"EquationEditor.Euler"               ,"rk.Euler"},
{"EquationEditor.MidPoint"            ,"rk.EulerRichardson"},
{"EquationEditor.VelocityVerlet"      ,"symplectic.VelocityVerlet"},
{"EquationEditor.Fehlberg8"           ,"rk.Fehlberg8"},
{"EquationEditor.RungeKutta"          ,"rk.RK4"},
{"EquationEditor.RungeKuttaFehlberg"  ,"rk.CashKarp45+"},
{"EquationEditor.Dopri853"            ,"rk.Dopri853+"},
{"EquationEditor.Dopri5"              ,"rk.Dopri5+"},
{"EquationEditor.Radau5"              ,"rk.Radau5+"},
{"EquationEditor.BogackiShampine23"   ,"rk.BogackiShampine23+"},
{"EquationEditor.Fehlberg78"          ,"rk.Fehlberg78+"},
{"EquationEditor.QSS3"                ,"qss.Qss3+"},

{"JavascriptEquationEditor.ODESolvers","Euler MidPoint VelocityVerlet BogackiShampine23 RungeKutta RungeKuttaFehlberg Dopri5 Dopri853 Fehlberg8 Fehlberg78"}, 
{"JavascriptEquationEditor.DefaultSolver" ,"RungeKuttaFehlberg"},
{"JavascriptEquationEditor.Euler"         ,"EJSS_ODE_SOLVERS.euler"},
{"JavascriptEquationEditor.MidPoint"      ,"EJSS_ODE_SOLVERS.eulerRichardson"},
{"JavascriptEquationEditor.VelocityVerlet"     ,"EJSS_ODE_SOLVERS.velocityVerlet"},
{"JavascriptEquationEditor.BogackiShampine23"  ,"EJSS_ODE_SOLVERS.bogackiShampine23+"},
{"JavascriptEquationEditor.RungeKutta"    ,"EJSS_ODE_SOLVERS.rungeKutta4"},
{"JavascriptEquationEditor.RungeKuttaFehlberg" ,"EJSS_ODE_SOLVERS.cashKarp45+"},
{"JavascriptEquationEditor.Fehlberg8"          ,"EJSS_ODE_SOLVERS.fehlberg8"},
{"JavascriptEquationEditor.Fehlberg78"         ,"EJSS_ODE_SOLVERS.fehlberg78+"},
{"JavascriptEquationEditor.Dopri5"             ,"EJSS_ODE_SOLVERS.doPri5+"},
{"JavascriptEquationEditor.Dopri853"           ,"EJSS_ODE_SOLVERS.doPri853+"},


{"MatlabEditor.ColumnHeaders"      ,"Name Value Type Dimension Connected"},
{"VariablesEditor.File.Icon"       ,"data/icons/openM.gif"},
{"VariablesEditor.Link.Icon"       ,"data/icons/select.gif"},

{"EditorForVariables.ActionList"        ,
  "play() "+
  "pause() "+
  "step() " +
  "setFPS(int) " +
  "setDelay(int) "+
  "getDelay() "+
  "setSPD(int) " +
  "reset() "+
  "initialize() "+
  "isApplet() "+
  "isPlaying() "+
  "isPaused() "+
  "setPageEnabled(\"pageName\",true) "+
  "breakAfterThisPage() " +
//  "initializeSolvers() "+
  "resetSolvers() "+

//  "resetView() "+
//  "clearView() "+
  
  "getStringProperty(\"propertyName\") "+
//  "getStringProperty(\"propertyName\",\"defaultValue\") "+
  "getArguments() "+
  "getParameter(\"parameterName\") "+
  "hasDefaultState() "+
  "readDefaultState() "+
  "saveDefaultStateToJar() "+
  "saveDefaultStateToJar(\"filenames\") "+
  "saveState(\"filename\") "+
  "readState(\"filename\") "+
  "saveVariables(\"filename\",\"variablesList\") "+
  "readVariables(\"filename\",\"variablesList\") " +
  "saveText(\"filename\",\"text\") "+
  "saveText(\"filename\",\"type\",\"text\") "+
  "readText(\"filename\") "+
  "readText(\"filename\",\"type\") "+
  "saveImage(\"filename\",\"Element\") "+
  
  "isMoodleConnected() " + 

//  "setVariables(\"command\",\"sep\",\"arraySep\") "+
//  "setVariables(\"command\") "+
//  "getVariable(\"variableName\") "+
  
  "tools.showTable(_view.dataElement) "+
  "tools.showTable(_view.parentComponent,_view.dataElement) "+
  "tools.showDataTool(_view.dataElement) "+
  "tools.showDataTool(_view.parentComponent,_view.dataElement) "+
  "tools.clearDataTool() "+
  "tools.getDataTool() "+
  "tools.openData(\"filename\") "+
  "tools.openData(_view.parentComponent,\"filename\") "+
  "tools.showFourierTool(_view.dataElement) "+
  "tools.showFourierTool(_view.parentComponent,_view.dataElement) "+
  "tools.clearFourierTool() "+
  "tools.getFourierTool() "+
  "tools.openFourierData(\"filename\") "+
  "tools.openFourierData(_view.parentComponent,\"filename\") "+

  "view.setLocale(\"language\") " +
  "view.setLocale(locale) " +
  "view.getLocale() " +
  "view.getLocaleLanguage() " +
  "view.alert(element,\"Title\",\"Message\") "+
  "view.alert(\"elementName\",\"Title\",\"Message\") "+
  "view.clearMessages() "+
  "view.format(value,\"format\") "+
  "view.hasJava3D() "+
  "view.print(\"Message\") "+
  "view.println(\"Message\") "+
//  "view.println() "+
  
  "view.resetElements() "+
  "view.clearElements() "+
  "view.clearData() " +
  "view.resetTraces() " +
  "view.update() " +
  "view.setUpdateView(boolean) " +
  "view.getElement(\"elementName\") "+
  "view.getTopLevelAncestor(\"elementName\") "+
  "view.setParentComponent(\"elementName\") "+
  
//  "view.getComponent(\"elementName\") "+
//  "view.getContainer(\"elementName\") "+
//  "view.getObject(\"Element\",\"objectName\") "+
//  "view.getFunction(\"Element\",\"functionName\") "+
//  "view.getVisual(\"Element\") "+
  "view.getDescriptionPageURL(\"pageName\") "+
  "view.captureVideo(\"elementName\") "+
  "view.createDescriptionDialog(_view.aWindow,\"pageName\") "+
  "view.createHTMLDialog(_view.ownerWindow,\"pageURL\") "+
  "view.createHTMLDialog(_view.ownerWindow,aURL) "+
  "view.createHTMLPage(\"pageURL\") "+
  "view.createHTMLPage(aURL) "+
  "view.createDialog(_view.ownerWindow,aComponent) "+
  "view.openDescriptionPageInBrowser(\"pageName\") "+
  "view.openDescriptionPagesInBrowser() "+
  "view.setDescriptionPageVisible(\"pageName\",false) "+
  "view.showDescriptionAtStartUp(false) "+
  "view.showDescription(true) "+
  "view.showDocument(\"filename\") "
},

{"EditorForVariables.JavascriptActionList",
  "tools.showInputDialog() "+
  "tools.showOkDialog() "+
  "tools.showOkCancelDialog() "+
  "tools.downloadText('filename','text') "+
  "tools.uploadText(function('text')) "+

  "play() "+
  "pause() "+
  "step() " +
  "reset() "+
  "update() "+
  "initialize() "+
  "setFPS(int) " +
  "setDelay(int) "+
  "setStepsPerDisplay(int) " +
  "setUpdateView(boolean) "+
  "setAutoplay(boolean) "+
  "model.isPlaying() "+
  "model.isPaused() "+
  "model.setRunningMode() "+
//  "model.setRunAlways() "+
  "println() "+
  "breakAfterThisPage() " +
  "resetSolvers() "+
  "saveText(\"name\",\"content\") " +
  "saveText(\"name\",\"type\",\"content\") " +
  "saveState(\"name\") " +
  "saveImage(\"name\",\"panelname\") " +
  "setPageEnabled(\"pageName\",true) "+
  "readText(\"url\",\"type\",\"varName\") " +
  "readState(\"type\",\"url\") " +
  "getStringProperty(\"propertyName\") " +

  "view._collectData() "+
  "view._initialize() "+
  "view._update() " +
  "view._addResizeListener(listener) "+
  "view._addFontResizeListener(listener) "+
  "view._openDescriptionPage(\"pagename\") " +
  "view._showDocument(\"docURL\") "
},

{"View.FileExtension.Logo" ,"png"},
{"View.FileExtension.image" ,"gif,jpg,png"},
{"View.FileExtension.imageOn" ,"gif,jpg,png"},
{"View.FileExtension.imageOff" ,"gif,jpg,png"},
{"View.FileExtension.imageFile" ,"gif,jpg,png"},
{"View.FileExtension.selectedimage" ,"gif,jpg,png"},
{"View.FileExtension.audiofile" ,"au,aiff,wav"},
{"View.FileExtension.externalfile" ,"m,mdl,exe,sq,sci,cos"},
{"View.FileExtension.xmlfile" ,"xml"},
{"View.FileExtension.videofile" ,"mov,mp4,flv,wmv,gif,jpg,png"},
{"View.FileExtension.library" ,"jar,zip,class"},
{"View.FileExtension.3Dfile" , "3ds,obj"},
{"View.FileExtension.VRMLfile" , "wrl,obj"},
{"View.FileExtension.texture" ,"gif,jpg,png"},
{"View.FileExtension.textureSecond" ,"gif,jpg,png"},
{"View.FileExtension.properties" ,"properties"},
{"View.FileExtension.configuration" ,"dat"},
{"View.FileExtension.ImageUrl" ,"gif,jpg,png,svg"},
{"View.FileExtension.ImageOnUrl" ,"gif,jpg,png"},
{"View.FileExtension.ImageOffUrl" ,"gif,jpg,png"},
{"View.FileExtension.TextureUrl" ,"gif,jpg,png"},
{"View.FileExtension.Description" ,"js"},
{"View.FileExtension.VideoUrl" ,"mpeg,mpg,mp4,ogg,webm"},
{"View.FileExtension.AudioUrl" ,"mp3,mpeg,mpg,ogg"},
{"View.FileExtension.css" ,"css"},
{"View.FileExtension.Url" ,"html,xhtml"},

{"Tree.Main.Icon"                       ,"data/icons/root.gif"},
{"Tree.Capture.Icon"                    ,"data/icons/capture.gif"},
{"Tree.Create.Icon"                     ,"data/icons/create.gif"},

{"View.Action.Icon"                     ,"data/icons/action.gif"},
{"View.Link.Icon"                       ,"data/icons/link.gif"},
{"View.Unlink.Icon"                     ,"data/icons/unlinked.gif"},
{"View.Edit.Icon"                       ,"data/icons/edit.gif"},
{"View.Write.Icon"                      ,"data/icons/write.gif"},

{"Elements.EMPTY.Icon"                  , "data/icons/Elements/EMPTY.gif"},
// Provided for backward compatibility. Dataset was replaced by Trace in release 3.01
{"Elements.Dataset.Icon"                , "data/icons/Elements/Trace.gif"},

{"CodeWizards.Wizards"                  ,"If IfElse For While DoWhile TryCatch Method"},

{"FileNetUtils.DirectoryUp.Icon"        ,"data/icons/directoryUp.gif"},
{"FileNetUtils.Home.Icon"               ,"data/icons/home.gif"},
{"FileNetUtils.Directory.Icon"          ,"data/icons/directory.gif"},
{"FileNetUtils.File.Icon"               ,"data/icons/file.gif"},
{"FileNetUtils.Network.Icon"            ,"data/icons/globe.gif"},

{"EjsOptions.Add.Icon"                  ,"data/icons/add.gif"},
{"EjsOptions.Edit.Icon"                 ,"data/icons/edit.gif"},
{"EjsOptions.Remove.Icon"               ,"data/icons/remove.gif"},
{"SimInfoEditor.Find.Icon"              ,"data/icons/find.gif"},
{"SimInfoEditor.Refresh.Icon"           ,"data/icons/refreshMedium.gif"},
{"EjsConsole.EjsLogo"                   ,"data/icons/EjsSLogo.png"},
{"HTMLEditor.Refresh.Icon"              ,"data/icons/refresh.gif"},
{"HTMLEditor.External.Icon"             ,"data/icons/link.gif"},
{"HTMLEditor.Edit.Icon"                 ,"data/icons/edit.gif"},
{"HTMLEditor.Unlink.Icon"               ,"data/icons/unlinked.gif"},

{"View.Elements.Groups.Containers.Icon"  ,"data/icons/Groups/Containers.gif"},
{"View.Elements.Groups.Buttons.Icon"     ,"data/icons/Groups/Buttons.gif"},
{"View.Elements.Groups.IO.Icon"          ,"data/icons/Groups/IO.gif"},
{"View.Elements.Groups.Menu.Icon"        ,"data/icons/Groups/Menu.gif"},
{"View.Elements.Groups.Compound.Icon"    ,"data/icons/Groups/Compound.gif"},
{"View.Elements.Groups.SwingDeprecated.Icon"    ,"data/icons/Groups/Deprecated.gif"},

{"View.Elements.Groups.Basic2D.Icon"     ,"data/icons/Groups/Basic2D.gif"},
{"View.Elements.Groups.Sets2D.Icon"    ,"data/icons/Groups/Sets2D.gif"},
{"View.Elements.Groups.Fields2D.Icon"    ,"data/icons/Groups/Fields2D.gif"},
{"View.Elements.Groups.Control2D.Icon"   ,"data/icons/Groups/Control2D.gif"},
{"View.Elements.Groups.DrawablesDeprecated.Icon"    ,"data/icons/Groups/Deprecated.gif"},
{"View.Elements.Groups.Drawables2Deprecated.Icon"   ,"data/icons/Groups/Deprecated.gif"},

{"View.Elements.Groups.Bodies3D.Icon"   ,"data/icons/Groups/Bodies3D.gif"},
{"View.Elements.Groups.Basic3D.Icon"   ,"data/icons/Groups/Basic3D.gif"},
{"View.Elements.Groups.Sets3D.Icon"    ,"data/icons/Groups/Sets3D.gif"},
{"View.Elements.Groups.Other3D.Icon"   ,"data/icons/Groups/Other3D.gif"},
{"View.Elements.Groups.Drawables3DDeprecated.Icon"    ,"data/icons/Groups/Deprecated.gif"},

{"TabbedEditor.DefaultXMLCode.Restriction", "<Code><![CDATA[// Wether the given point is valid\npublic boolean allowsPoint(double x, double y, double z) {\n  return true;\n}\n\n // What to do if trying to access an invalid point\npublic void action() {\n  // do nothing\n}]]></Code>\n"},

};

}  // End of class
