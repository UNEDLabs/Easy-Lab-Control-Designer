package org.opensourcephysics.drawing3d.java3d;





import javax.media.j3d.BranchGroup;
import javax.media.j3d.GeometryArray;
import javax.media.j3d.LineStripArray;
import javax.media.j3d.Shape3D;


import org.opensourcephysics.drawing3d.Element;
import org.opensourcephysics.drawing3d.ElementTrail;

/**
 * <p>Title: Java3dElemenTrail</p>
 * <p>Description: A single (and simple) trail of 3D points using Java 3D.</p>
 * @author Carlos Jara Bravo
 * @author Francisco Esquembre
 * @version September 2009
 */

public class Java3dElementTrail extends Java3dElement{

	private LineStripArray lines = null;
	private int[] stripVertexCounts;
	private int currentSize;
	private int temp;
	private double[] points;
	private static int previousSize = 2500;
	//private boolean resize = false;
	private BranchGroup bg = null;
	
	public Java3dElementTrail (ElementTrail _element){
		super(_element);
		getAppearance().getLineAttributes().setLineAntialiasingEnable(true);
		getAppearance().getPointAttributes().setPointSize(0.1f);
		points = new double[3*previousSize];
		currentSize = temp = 0;
		initTrail();
	}
	
	
	
	public void processChanges(int _change, int _cummulativeChange) {
		super.processChanges(_change, _cummulativeChange);
		if ((_change & Element.CHANGE_SHAPE) != 0) {
		  double[][] p = ((ElementTrail)element).getData2D();
			if(p!=null){
				if(p[0].length==1 && lines!=null){
					points = new double[3*previousSize]; 
					initTrail(); /*lines.setCoordRefDouble(points);*/
				} 
				currentSize = p[0].length;
				if(currentSize>0 && currentSize<previousSize){
					/*if(currentSize == (temp+1)){
						temp = currentSize; 
						points[(currentSize-1)*3] = p[0][currentSize-1];
						points[(currentSize-1)*3+1] = p[1][currentSize-1];
						points[(currentSize-1)*3+2] = p[2][currentSize-1];
					}*/
					if(currentSize > temp){
						for(int i=temp;i<currentSize;i++){
							points[i*3] = p[0][i];
							points[i*3+1] = p[1][i];
							points[i*3+2] = p[2][i];
						}
						temp = currentSize;
					}
					else{
						for(int i=0;i<currentSize;i++){
							points[i*3] = p[0][i];
							points[i*3+1] = p[1][i];
							points[i*3+2] = p[2][i];
						}
						temp = currentSize;
					}
				}  
				if(currentSize>=previousSize || currentSize*3>=points.length){	
					//resize = true;
					double[] newArray = new double[temp*3];
					System.arraycopy(points, 0, newArray, 0, temp*3);
					points = new double[currentSize*3*2];
					for(int i=0;i<temp;i++){
						points[i*3] = newArray[i*3];
						points[i*3+1] = newArray[i*3+1];
						points[i*3+2] = newArray[i*3+2];
					}
					if(currentSize>previousSize || currentSize*3>points.length){
						for(int i=temp;i<currentSize;i++){
							points[i*3] = p[0][i];
							points[i*3+1] = p[1][i];
							points[i*3+2] = p[2][i];
						}
						temp = currentSize;
					}
					previousSize = currentSize*2; 
					initTrail();
				}
				if(currentSize>2){
					stripVertexCounts[0] = Math.max(2,currentSize);
					lines.setStripVertexCounts(stripVertexCounts);
				}
				if (currentSize == ((ElementTrail)element).getMaximum()) {
					//Shift the points back to account for the max
					for (int i = 0; i <currentSize-1; i++) {
						points[i*3] = points[(i+1)*3];
						points[i*3+1] = points[(i+1)*3+1];
						points[i*3+2] = points[(i+1)*3+2];
					}
				}
			}
		}
	}
	
	private void initTrail(){
		stripVertexCounts = new int[]{Math.max(2, currentSize)};
		lines = new LineStripArray(points.length/3, GeometryArray.COORDINATES | 
													   GeometryArray.BY_REFERENCE, stripVertexCounts);
									              		  
		lines.setCapability(GeometryArray.ALLOW_REF_DATA_WRITE);
		lines.setCapability(GeometryArray.ALLOW_COLOR_WRITE);
		lines.setCapability(GeometryArray.ALLOW_COUNT_WRITE);
		lines.setCapability(GeometryArray.ALLOW_COORDINATE_WRITE);
		lines.setCoordRefDouble(points);		
    
		bg = new BranchGroup();
		bg.setCapability(BranchGroup.ALLOW_DETACH);
		bg.addChild(new Shape3D(lines, getAppearance()));
		addNode(bg);
	}
  
	@Override
	public boolean isPrimitive() {return false;}


	
}
