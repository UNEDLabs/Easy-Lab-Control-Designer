package org.opensourcephysics.drawing3d.java3d;

import java.awt.Color;

import javax.media.j3d.Appearance;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.ColoringAttributes;
import javax.media.j3d.GeometryArray;
import javax.media.j3d.LineAttributes;
import javax.media.j3d.PolygonAttributes;
import javax.media.j3d.QuadArray;
import javax.media.j3d.RenderingAttributes;
import javax.media.j3d.Shape3D;
import javax.media.j3d.TransparencyAttributes;
import javax.vecmath.Vector3f;

import org.opensourcephysics.drawing3d.Element;
import org.opensourcephysics.drawing3d.ElementSurface;
import org.opensourcephysics.drawing3d.utils.Style;

/**
 * <p>Title: Java3dElementSurface</p>
 * <p>Description: A surface using Java 3D.</p>
 * @author Carlos Jara Bravo
 * @author Francisco Esquembre
 * @version September 2009
 */
public class Java3dElementSurface extends Java3dAbstractTile{

	//Java 3D variables
	private BranchGroup bgSurface;
	private QuadArray lines = null;
	private Shape3D shape = null;
	private Appearance appLines = null;
	
	public Java3dElementSurface (ElementSurface _element){
		super(_element);
		appLines = getAppLines();
	}

	public boolean isPrimitive() {return false;}

	protected Vector3f[] createStandardNormals(double[][][] data, int _nx, int _ny, int _nz) {
		int nTotal = data.length;
		Vector3f[] normals = new Vector3f[nTotal];
		for(int i=0; i<nTotal; i++){
			Vector3f u1 = new Vector3f();
			u1.sub(new Vector3f((float)data[i][0][0], (float)data[i][0][1], (float)data[i][0][2]),
					new Vector3f((float)data[i][1][0], (float)data[i][1][1], (float)data[i][1][2]));
			Vector3f u2 = new Vector3f();
			u2.sub(new Vector3f((float)data[i][1][0], (float)data[i][1][1], (float)data[i][1][2]),
					new Vector3f((float)data[i][2][0], (float)data[i][2][1], (float)data[i][2][2]));
			normals[i] = new Vector3f();
			normals[i].cross(u1, u2);
			normals[i].normalize();
		}
		return normals;
	}

	protected double[][][] createStandardTile(int _nx, int _ny, int _nz) {
		double[][][] data = ((ElementSurface) element).getData();
		if (data==null) return new double[0][0][3];
		int nu = data.length-1, nv=data[0].length-1;
		double [][][] coord = new double[nu*nv][4][3];
		int tile = 0, i = 0;
		int nvertex = nu*nv*4;
		lines = new QuadArray(nvertex*2, GeometryArray.COORDINATES | GeometryArray.NORMALS);
		lines.setCapability(GeometryArray.ALLOW_COORDINATE_READ);
		lines.setCapability(GeometryArray.ALLOW_COORDINATE_WRITE);
		
		for (int v = 0; v<nv; v++) {
		   for (int u = 0; u<nu; u++, tile++) {
		       for (int k = 0; k<3; k++) {
		          coord[tile][0][k] = data[u][v][k];
		          coord[tile][1][k] = data[u+1][v][k];
		          coord[tile][2][k] = data[u+1][v+1][k];
		          coord[tile][3][k] = data[u][v+1][k];
		       }
		       lines.setCoordinate(i++, coord[tile][0]);
		       lines.setCoordinate(i++, coord[tile][1]);
		       lines.setCoordinate(i++, coord[tile][2]);
		       lines.setCoordinate(i++, coord[tile][3]);
		       lines.setCoordinate(i++, coord[tile][3]);
		       lines.setCoordinate(i++, coord[tile][2]);
		       lines.setCoordinate(i++, coord[tile][1]);
		       lines.setCoordinate(i++, coord[tile][0]);
		    }
		}
		setLines();
	    return coord;
	}
	
	private void setLines(){
		if(lines==null) return;
		shape = new Shape3D();
		shape.setGeometry(lines);
		shape.setAppearance(appLines);
		int i = bg.indexOfChild(bgSurface);
		bgSurface = new BranchGroup();
		bgSurface.setCapability(BranchGroup.ALLOW_DETACH);
		bgSurface.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);
		bgSurface.setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);
		bgSurface.setCapability(BranchGroup.ALLOW_CHILDREN_READ);
		bgSurface.addChild(shape);
		if(bg.numChildren()>1 && i!=-1) bg.setChild(bgSurface,i);		
		else if (bg.numChildren()>1 && i==-1){ bg.setChild(bgSurface,1); }
		else bg.addChild(bgSurface);
	}
	
	private Appearance getAppLines(){
		Appearance app = new Appearance();
		app.setCapability(Appearance.ALLOW_POLYGON_ATTRIBUTES_WRITE);
		app.setCapability(Appearance.ALLOW_POLYGON_ATTRIBUTES_READ);
		app.setCapability(Appearance.ALLOW_COLORING_ATTRIBUTES_WRITE);
    app.setCapability(Appearance.ALLOW_RENDERING_ATTRIBUTES_READ);
		
		PolygonAttributes pa = new PolygonAttributes();
		pa.setCapability(PolygonAttributes.ALLOW_CULL_FACE_WRITE);
		pa.setCapability(PolygonAttributes.ALLOW_MODE_WRITE);
		pa.setCapability(PolygonAttributes.ALLOW_OFFSET_WRITE);
		app.setPolygonAttributes(pa);
		app.getPolygonAttributes().setPolygonMode(PolygonAttributes.POLYGON_LINE);
		
		LineAttributes la = new LineAttributes();
		la.setLineAntialiasingEnable(true);
		la.setLineWidth(1);
		la.setCapability(LineAttributes.ALLOW_WIDTH_READ); 
		la.setCapability(LineAttributes.ALLOW_WIDTH_WRITE); 
		app.setLineAttributes(la);
		
		RenderingAttributes ra = new RenderingAttributes();
		ra.setCapability(RenderingAttributes.ALLOW_VISIBLE_WRITE);
		ra.setCapability(RenderingAttributes.ALLOW_IGNORE_VERTEX_COLORS_WRITE);
		app.setRenderingAttributes(ra);
		
		TransparencyAttributes transparency = new TransparencyAttributes();
		transparency.setCapability(TransparencyAttributes.ALLOW_VALUE_WRITE);
		transparency.setCapability(TransparencyAttributes.ALLOW_MODE_WRITE);
		app.setTransparencyAttributes(transparency);
		
		ColoringAttributes c = new ColoringAttributes();
		c.setCapability(ColoringAttributes.ALLOW_COLOR_WRITE);
		app.setColoringAttributes(c);
		app.getColoringAttributes().setColor(0, 0, 0);
		return app;
	}
	
	public void processChanges(int _change, int _cummulativeChange) {
		super.processChanges(_change, _cummulativeChange);
		if ((_change & Element.CHANGE_VISIBILITY) != 0) {
			appLines.getRenderingAttributes().setVisible(element.isVisible());
		}
	}
	
	public void styleChanged(int _change) {
		 if(appLines == null) return;
		 if(_change==Style.CHANGED_DRAWING_LINES){
			 if(!element.getStyle().isDrawingLines()) appLines.getRenderingAttributes().setVisible(false);
			 else  appLines.getRenderingAttributes().setVisible(true);
	     }	
		 else if(_change==Style.CHANGED_LINE_WIDTH) appLines.getLineAttributes().setLineWidth(element.getStyle().getLineWidth());
		 else if(_change==Style.CHANGED_LINE_COLOR){
			  Color lineColor = element.getStyle().getLineColor();
		      float[] lineComponents = lineColor.getRGBComponents(null);
		      appLines.getRenderingAttributes().setIgnoreVertexColors(true);
		      appLines.getColoringAttributes().setColor(lineComponents[0], lineComponents[1], lineComponents[2]);
		      appLines.getPolygonAttributes().setCullFace(PolygonAttributes.CULL_NONE);
		      appLines.getTransparencyAttributes().setTransparencyMode(TransparencyAttributes.NONE);
		 }
		 else if(_change==Style.CHANGED_TEXTURES){
			 if(element.getStyle().getTextures()[0]==null && element.getStyle().getTextures()[1]==null){ 
			     appLines.getRenderingAttributes().setVisible(element.getStyle().isDrawingLines());
			 }
			 else appLines.getRenderingAttributes().setVisible(false);
			 super.styleChanged(_change);
		 }
		 else super.styleChanged(_change);

	}
	
}
