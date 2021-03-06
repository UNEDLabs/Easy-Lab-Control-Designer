package javasci ;
/********************************************************************************************************/
/* Allan CORNET */
/* INRIA 2006 */
/********************************************************************************************************/
public class SciDouble implements java.io.Serializable
{
/********************************************************************************************************/
  private SciDoubleArray pObjSciDouble;
/********************************************************************************************************/
/**
* See SCI/examples/callsci/callsciJava/others for some simple examples
*/
/********************************************************************************************************/

/********************************************************************************************************/
  public SciDouble(String name,SciDouble Obj)
  {
	pObjSciDouble = new SciDoubleArray(name,1,1,new double[]{Obj.getData()});
  }
/********************************************************************************************************/
  public SciDouble(String name)
  {
	pObjSciDouble = new SciDoubleArray(name,1,1);
  }
 /********************************************************************************************************/
  public SciDouble(String name,double Value )
  {
	pObjSciDouble = new SciDoubleArray(name,1,1,new double[]{Value});
  }
/********************************************************************************************************/
  public String getName()
  {
    return  pObjSciDouble.getName();
  }
/********************************************************************************************************/
  public double getData()
  {
    Get();
    return pObjSciDouble.getData()[0];
  }
/********************************************************************************************************/
  public void Get()
  {
   	pObjSciDouble.Get();
  }
 /********************************************************************************************************/
  public boolean Job(String job)
  {
  	return pObjSciDouble.Job(job);
  }
 /********************************************************************************************************/
  public void Send()
  {
  	pObjSciDouble.Send();
  }
  /********************************************************************************************************/
  public void disp()
  {
    Get();
	System.out.println("double "+ getName() +"=");
    Job( "disp(" + getName() +");");
  }
}
/********************************************************************************************************/
