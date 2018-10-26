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
import javax.vecmath.TexCoord2f;
import javax.vecmath.Vector3f;

import org.opensourcephysics.drawing3d.Element;
import org.opensourcephysics.drawing3d.ElementDisk;
import org.opensourcephysics.drawing3d.ElementCylinder;
import org.opensourcephysics.drawing3d.utils.Resolution;
import org.opensourcephysics.drawing3d.utils.Style;



/**
 * <p>Title: Java3dElementDisk</p>
 * <p>Description: A disk using Java 3D.</p>
 * @author Carlos Jara Bravo
 * @author Francisco Esquembre
 * @version August 2010
 */
public class Java3dElementDisk extends Java3dElement {
	
	   //Java 3D variables
	   private BranchGroup bg;
	   private QuadArray tetra, line;
	   private Shape3D shape1, shape2;
	   private TexCoord2f texCoord[] = null;
	   private Appearance appLines = null;
	      
	   //Configuration
	   double[][][] standardCircle;
	   private int nr, nu;  
	   
	   public Java3dElementDisk(ElementDisk _element) {
		   super(_element);
		   appLines = getAppLines();
		   setData();
	   }
		
		public void processChanges(int _change, int _cummulativeChange) {
			super.processChanges(_change, _cummulativeChange);
			
			if ((_change & Element.CHANGE_RESOLUTION) != 0) {
				int slices, stacks;
				if (element.getStyle().getResolution().getType() == Resolution.MAX_LENGTH) {
					slices = (int)Math.floor(Math.min(element.getSizeX(), element.getSizeZ()) / element.getStyle().getResolution().getMaxLength());
					stacks = (int)Math.floor(element.getSizeY() / element.getStyle().getResolution().getMaxLength());
				}
				else {
					slices = element.getStyle().getResolution().getN2();
					stacks = element.getStyle().getResolution().getN1();
				}
				slices = Math.max(3, slices);
				stacks = Math.max(1, stacks);
				nr = stacks;
				nu = slices;
				setData();
			}
			if ((_change & Element.CHANGE_SHAPE) != 0 ) setData();
		}

		private void setData(){
			
			//Configuration variables
			int nvertex, nfaces;
		    Vector3f v = new Vector3f();
		    v.x = 1; v.y = 0; v.z = 1;
		    
		    //reallocate arrays
		    standardCircle = ElementCylinder.createStandardCylinder(nr, nu, nr, ((ElementDisk)element).getMinimumAngle(), ((ElementCylinder)element).getMaximumAngle(),
					                                                true, false, true, true);
		    if(standardCircle == null) return;
		    nfaces = standardCircle.length; 
		    if(nfaces == 0) return;
		    nvertex = standardCircle.length * standardCircle[0].length;
		    texCoord =  new TexCoord2f[nvertex*2];
		    
		    //Vertex
		    tetra = new QuadArray(nvertex*2, GeometryArray.COORDINATES | GeometryArray.NORMALS |
					     			          GeometryArray.TEXTURE_COORDINATE_2);
		    line = new QuadArray(nvertex*2, GeometryArray.COORDINATES | GeometryArray.NORMALS);
		    
		    for (int i = 0, j = 0; j < nfaces; j++) {
		    	tetra.setCoordinate(i,standardCircle[j][0]);
		    	line.setCoordinate(i,standardCircle[j][0]);
		    	texCoord[i] = new TexCoord2f((float)standardCircle[j][0][0]+0.5f, (float)standardCircle[j][0][1]+0.5f);
		    	tetra.setTextureCoordinate(0,i,texCoord[i]); 
		    	tetra.setNormal(i, v);
		    	line.setNormal(i++, v);
		
		    	tetra.setCoordinate(i,standardCircle[j][1]);
		    	line.setCoordinate(i,standardCircle[j][1]);
		      	texCoord[i] = new TexCoord2f((float)standardCircle[j][1][0]+0.5f, (float)standardCircle[j][1][1]+0.5f);
		    	tetra.setTextureCoordinate(0,i,texCoord[i]); 
		    	tetra.setNormal(i, v);
		    	line.setNormal(i++, v);
		    	
		    	tetra.setCoordinate(i,standardCircle[j][2]);
		    	line.setCoordinate(i,standardCircle[j][2]);
		      	texCoord[i] = new TexCoord2f((float)standardCircle[j][2][0]+0.5f, (float)standardCircle[j][2][1]+0.5f);
		    	tetra.setTextureCoordinate(0,i,texCoord[i]);
		    	tetra.setNormal(i, v);
		    	line.setNormal(i++, v);
		    	
		       	tetra.setCoordinate(i,standardCircle[j][3]);
		       	line.setCoordinate(i,standardCircle[j][3]);
		      	texCoord[i] = new TexCoord2f((float)standardCircle[j][3][0]+0.5f, (float)standardCircle[j][3][1]+0.5f);
		    	tetra.setTextureCoordinate(0,i,texCoord[i]);
		    	tetra.setNormal(i, v);
		    	line.setNormal(i++, v);
		        
		    	tetra.setCoordinate(i,standardCircle[j][3]);
		    	line.setCoordinate(i,standardCircle[j][3]);
		      	texCoord[i] = new TexCoord2f((float)standardCircle[j][3][0]+0.5f, (float)standardCircle[j][3][1]+0.5f);
		    	tetra.setTextureCoordinate(0,i,texCoord[i]);
		    	tetra.setNormal(i, v);
		    	line.setNormal(i++, v);   
		        
		    	tetra.setCoordinate(i,standardCircle[j][2]);
		    	line.setCoordinate(i,standardCircle[j][2]);
		    	texCoord[i] = new TexCoord2f((float)standardCircle[j][2][0]+0.5f, (float)standardCircle[j][2][1]+0.5f);
		    	tetra.setTextureCoordinate(0,i,texCoord[i]);
		    	tetra.setNormal(i, v);
		    	line.setNormal(i++, v);
		        
		    	tetra.setCoordinate(i,standardCircle[j][1]);
		    	line.setCoordinate(i,standardCircle[j][1]);
		    	texCoord[i] = new TexCoord2f((float)standardCircle[j][1][0]+0.5f, (float)standardCircle[j][1][1]+0.5f);
		    	tetra.setTextureCoordinate(0,i,texCoord[i]); 
		    	tetra.setNormal(i, v);
		    	line.setNormal(i++, v);   
		        
		    	tetra.setCoordinate(i,standardCircle[j][0]);
		    	line.setCoordinate(i,standardCircle[j][0]);
		    	texCoord[i] = new TexCoord2f((float)standardCircle[j][0][0]+0.5f, (float)standardCircle[j][0][1]+0.5f);
		    	tetra.setTextureCoordinate(0,i,texCoord[i]); 
		    	tetra.setNormal(i, v);
		    	line.setNormal(i++, v);
		   }		    
		
		   shape1 = new Shape3D();
		   shape1.setGeometry(tetra);
		   shape2 = new Shape3D();
		   shape2.setGeometry(line);
		   shape1.setAppearance(getAppearance());
		   shape2.setAppearance(appLines);
		   
		   bg = new BranchGroup();
		   bg.setCapability(BranchGroup.ALLOW_DETACH);
		   bg.addChild(shape1);
		   bg.addChild(shape2);
		   addNode(bg);
		}
		
		private Appearance getAppLines(){
			Appearance app = new Appearance();
			app.setCapability(Appearance.ALLOW_POLYGON_ATTRIBUTES_WRITE);
			app.setCapability(Appearance.ALLOW_POLYGON_ATTRIBUTES_READ);
			app.setCapability(Appearance.ALLOW_COLORING_ATTRIBUTES_WRITE);
			
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
		
		public void styleChanged(int _change) {
			//super.styleChanged(_change);
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
				 if(appLines==null) return;
				 if(element.getStyle().getTextures()[0]==null && element.getStyle().getTextures()[1]==null) 
				     appLines.getRenderingAttributes().setVisible(true);
				 else appLines.getRenderingAttributes().setVisible(false);
				 super.styleChanged(_change);
			 }
			 else super.styleChanged(_change);

		}
		
		@Override
		public boolean isPrimitive() {return true;}

		
}
