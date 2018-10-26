To re-generate Ejs from the root do the following:

1.- Open the Ejs Workspace with Eclipse.
2.- Clean and rebuild the EjsLibrqry, Ejs and the OSP_core projects.
3.- Right-click the OSP_core file osp_core.jardesc and select "Create JAR". 
    Accept all messages that appear. This will cause a recompilation.
4.- Run the class PackageEjsLibrary in the EJSLibrary project
    The console will show some messages concerning missing files. This is pretty normal. I 5.- Run the class org.colos.ejs.PackageEjs in the EJS project
    The console will show some messages concerning missing files. This is pretty normal. I use these messages for debugging purposes.
6.- Run the class org.colos.ejs.CreateDistributionEjs and you'll get a new Ejs.

Francisco Esquembre
Universidad de Murcia
SPAIN

http://www.um.es/fem/Ejs
October 2008
Modified Sep 2014