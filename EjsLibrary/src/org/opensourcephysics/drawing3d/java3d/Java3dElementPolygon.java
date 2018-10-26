package org.opensourcephysics.drawing3d.java3d;




import java.awt.Color;

import javax.media.j3d.BranchGroup;
import javax.media.j3d.GeometryArray;
import javax.media.j3d.LineArray;
import javax.media.j3d.LineStripArray;
import javax.media.j3d.PolygonAttributes;
import javax.media.j3d.Shape3D;
import javax.vecmath.Color3f;
import javax.vecmath.Point3d;
import javax.vecmath.TexCoord2f;

import org.opensourcephysics.drawing3d.Element;
import org.opensourcephysics.drawing3d.ElementPolygon;
import org.opensourcephysics.drawing3d.utils.Style;

import com.sun.j3d.utils.geometry.GeometryInfo;
import com.sun.j3d.utils.geometry.NormalGenerator;
import com.sun.j3d.utils.geometry.Stripifier;
import com.sun.j3d.utils.geometry.Triangulator;


/**
 * <p>Title: Java3dElementPolygon</p>
 * <p>Description: A polygon using Java 3D.</p>
 * @author Carlos Jara Bravo
 * @author Francisco Esquembre
 * @version September 2009
 */
public class Java3dElementPolygon extends Java3dElement {
	
	//Java 3D variables
	private GeometryArray tsa1 = null ;
	//private Shape3D shape1 = null; 
	private TexCoord2f[] texCoord = null;
	private Point3d[] coords = null;
	private BranchGroup bg = null;

	public Java3dElementPolygon (ElementPolygon _element){
		super(_element);
		getAppearance().getPolygonAttributes().setCullFace(PolygonAttributes.CULL_NONE);
		
		//Construct shape with proper Appearance attachment
		//shape1 = new Shape3D();
		//shape1.setAppearance(getAppearance());
	}
	
	public void processChanges(int _change, int _cummulativeChange) {
		super.processChanges(_change, _cummulativeChange);
		if (((_change & Element.CHANGE_SHAPE) != 0) || (_change & Element.CHANGE_POSITION) != 0)  computePolygon();
	}
		
	public void styleChanged(int _change) {
		if(_change==Style.CHANGED_DRAWING_FILL){
			getAppearance().getPolygonAttributes().setPolygonMode(PolygonAttributes.POLYGON_FILL);
			java.awt.Paint fill = element.getStyle().getFillColor();
			if (fill instanceof Color) getAppearance().getMaterial().setDiffuseColor(new Color3f((Color)fill));
			getAppearance().getMaterial().setSpecularColor(new Color3f(Color.white));
			super.styleChanged(Style.CHANGED_DRAWING_LINES);
		}
		else  super.styleChanged(_change);
	}
	
	private void computePolygon(){
		//Creation of variables
		int points = ((ElementPolygon)element).getData().length;
		if(points<=1) return;
		if(element.getStyle().isDrawingFill()){
			coords = new Point3d[points+1];
			texCoord = new TexCoord2f[points+1];
			
			//Creation of points and normals
			for(int n = 0; n < points;  n++){
				
				coords[n] = new Point3d(((ElementPolygon)element).getData()[n][0],
						                     ((ElementPolygon)element).getData()[n][1],
						                       ((ElementPolygon)element).getData()[n][2]);
				
				if(n==0 || n%3==0)texCoord[n] = new TexCoord2f(0.0f, 0.0f); 
				else if (n%2==0) texCoord[n] = new TexCoord2f(1.0f, 0.0f);
				else texCoord[n] = new TexCoord2f(0.5f, 0.5f);
			}
			coords[points] = new Point3d(((ElementPolygon)element).getData()[0][0],
	            	 						 ((ElementPolygon)element).getData()[0][1],
	            	 							((ElementPolygon)element).getData()[0][2]);
			texCoord[points] = new TexCoord2f(0.0f, 0.0f); 
		}
		else{
			coords = new Point3d[points*2+1];
			texCoord = new TexCoord2f[points*2+1];
			
			//Creation of points and normals
			for(int n = 0; n < points;  n++){
				
					coords[n] = new Point3d(((ElementPolygon)element).getData()[n][0],
						                     ((ElementPolygon)element).getData()[n][1],
						                       ((ElementPolygon)element).getData()[n][2]);
				
					coords[points*2-n] = new Point3d(((ElementPolygon)element).getData()[n][0],
						                             	 ((ElementPolygon)element).getData()[n][1],
						                             		((ElementPolygon)element).getData()[n][2]);
				
		
				if(n==0 || n%3==0){ 
					texCoord[n] = new TexCoord2f(0.0f, 0.0f); 
					texCoord[points*2-n] = new TexCoord2f(0.0f, 0.0f);
				}
				else if (n%2==0){
					texCoord[n] = new TexCoord2f(1.0f, 0.0f);
					texCoord[points*2-n] = new TexCoord2f(1.0f, 0.0f);
				}
				else{
					texCoord[n] = new TexCoord2f(0.5f, 0.5f);
					texCoord[points*2-n] = new TexCoord2f(0.5f, 0.5f);
				}
			}
			coords[points] = new Point3d(((ElementPolygon)element).getData()[0][0],
						((ElementPolygon)element).getData()[0][1],
							((ElementPolygon)element).getData()[0][2]);
			texCoord[points] = new TexCoord2f(0.0f, 0.0f); 
		}
		addShape();
	}
	
	@SuppressWarnings("deprecation")
	private void addShape(){
		if(coords == null) return;
		int points = ((ElementPolygon)element).getData().length;
		if(((ElementPolygon)element).isClosed() && points>2){	
			int pGeometry = 0;
			if(element.getStyle().isDrawingFill()) pGeometry = points+1;
			else pGeometry = points*2+1;
			
			GeometryInfo gi = new GeometryInfo (GeometryInfo.POLYGON_ARRAY);
			gi.setTextureCoordinateParams(1, 2);
			gi.setTextureCoordinates(0, texCoord);
			gi.setCoordinates(coords);
			gi.setStripCounts(new int[]{pGeometry});
			gi.recomputeIndices();

			Triangulator tr = new Triangulator();
			tr.triangulate(gi);
			gi.recomputeIndices();
			
			Stripifier st = new Stripifier();
			st.stripify(gi);
			gi.recomputeIndices();
			
			NormalGenerator ng = new NormalGenerator();
			ng.setCreaseAngle((float) Math.toRadians(30)); // ángulo de pliegue 		
			ng.generateNormals(gi);
			gi.recomputeIndices();
			
			tsa1 = gi.getGeometryArray();
		}
		else{
			LineStripArray line = new LineStripArray(((ElementPolygon)element).getDataArray().length,
														LineArray.COORDINATES | LineArray.COLOR_3 | LineArray.TEXTURE_COORDINATE_2, 
															new int[] {((ElementPolygon)element).getData().length});
			
			for(int i=0;i<((ElementPolygon)element).getData().length;i++){
				line.setCoordinate(i, coords[i]);
				line.setTextureCoordinate(0, i, texCoord[i]);
			}
			tsa1 = line;
		}
		Shape3D shape = new Shape3D();
		shape = new Shape3D(tsa1, getAppearance());
		bg = new BranchGroup();
		bg.setCapability(BranchGroup.ALLOW_DETACH);
		bg.addChild(shape);
		addNode(bg);
	}
	
	/*private Vector3f[] createNormals(double[][] data) {
		int nTotal = data.length;
		Vector3f[] normals = new Vector3f[nTotal];
		int n = 2;
		for(int i=0; i<nTotal; i++){
			if(i!=0 && i%3==0) n++;
			Vector3f u1 = new Vector3f();
			u1.sub(new Vector3f((float)data[n-2][0], (float)data[n-2][1], (float)data[n-2][2]),
					new Vector3f((float)data[n-1][0], (float)data[n-1][1], (float)data[n-1][2]));
			Vector3f u2 = new Vector3f();
			u2.sub(new Vector3f((float)data[n-1][0], (float)data[n-1][1], (float)data[n-1][2]),
					new Vector3f((float)data[n][0], (float)data[n][1], (float)data[n][2]));
			normals[i] = new Vector3f();
			normals[i].cross(u1, u2);
			normals[i].normalize();
		}
		return normals;
	}*/
	
	
	@Override
	public boolean isPrimitive() {return true;}

	
}
