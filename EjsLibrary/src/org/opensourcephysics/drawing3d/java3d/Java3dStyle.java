/*
 * Created on January 29, 2005
 * Complete copy of the JOGL version
 */

package org.opensourcephysics.drawing3d.java3d;

import java.awt.Color;
import java.awt.image.BufferedImage;

import javax.media.j3d.*;
import javax.vecmath.Color3f;
import javax.vecmath.Vector4f;
import com.sun.j3d.utils.image.TextureLoader;

import org.opensourcephysics.drawing3d.*;
import org.opensourcephysics.drawing3d.utils.Style;
import org.opensourcephysics.tools.ResourceLoader;

/**
 * Uses Style infromation to keep the necessary Java3D structures
 * @author Carlos Alberto Jara
 * @author Francisco Esquembre
 * @author Glenn Ford
 * @version August 2009
 */
public class Java3dStyle {
	private Style style;
	private float shininess = 50;
	private Appearance appearance;
	private TransparencyAttributes	transparency;
	private Texture texture1 = null, texture2 = null;
	private Texture2D tex2D = null;
	private ImageComponent2D image2D = null; 
	private int imgWidth = 0, imgHeight = 0; 

	Java3dStyle (Style _style) {
		this.style = _style;

		appearance = new Appearance();
		appearance.setCapability(Appearance.ALLOW_COLORING_ATTRIBUTES_READ);
		appearance.setCapability(Appearance.ALLOW_COLORING_ATTRIBUTES_WRITE);
		appearance.setCapability(Appearance.ALLOW_POLYGON_ATTRIBUTES_WRITE);
		appearance.setCapability(Appearance.ALLOW_POLYGON_ATTRIBUTES_READ);
		appearance.setCapability(Appearance.ALLOW_MATERIAL_WRITE);
		appearance.setCapability(Appearance.ALLOW_MATERIAL_READ);
    
		appearance.setCapability(Appearance.ALLOW_TEXTURE_WRITE);
		appearance.setCapability(Appearance.ALLOW_RENDERING_ATTRIBUTES_WRITE);
		appearance.setCapability(Appearance.ALLOW_RENDERING_ATTRIBUTES_READ);
		// Added by Paco
		appearance.setCapability(Appearance.ALLOW_TEXTURE_ATTRIBUTES_WRITE);
		appearance.setCapability(Appearance.ALLOW_LINE_ATTRIBUTES_WRITE);
		appearance.setCapability(Appearance.ALLOW_LINE_ATTRIBUTES_READ);
		appearance.setCapability(Appearance.ALLOW_TEXGEN_WRITE);
		appearance.setCapability(Appearance.ALLOW_TRANSPARENCY_ATTRIBUTES_WRITE);
		appearance.setCapability(Appearance.ALLOW_TEXTURE_UNIT_STATE_WRITE);
		appearance.setCapability(Appearance.ALLOW_POINT_ATTRIBUTES_WRITE);

		// appearance.setCapability(Appearance.ALLOW_COLORING_ATTRIBUTES_READ);
		// appearance.setCapability(Appearance.ALLOW_POLYGON_ATTRIBUTES_READ);
		// appearance.setCapability(Appearance.ALLOW_MATERIAL_READ);
		// appearance.setCapability(Appearance.ALLOW_TEXTURE_READ);
		// appearance.setCapability(Appearance.ALLOW_RENDERING_ATTRIBUTES_READ);
		// appearance.setCapability(Appearance.ALLOW_TEXTURE_ATTRIBUTES_READ);
		// appearance.setCapability(Appearance.ALLOW_LINE_ATTRIBUTES_READ);
		// appearance.setCapability(Appearance.ALLOW_TEXGEN_READ);
		// appearance.setCapability(Appearance.ALLOW_TRANSPARENCY_ATTRIBUTES_READ);
		// appearance.setCapability(Appearance.ALLOW_TEXTURE_UNIT_STATE_READ);
		
		//Used to make face culling work
		PolygonAttributes pa = new PolygonAttributes();
		pa.setCapability(PolygonAttributes.ALLOW_CULL_FACE_WRITE);
		pa.setCapability(PolygonAttributes.ALLOW_MODE_WRITE);
		pa.setCapability(PolygonAttributes.ALLOW_OFFSET_WRITE);
		appearance.setPolygonAttributes(pa);

		LineAttributes la = new LineAttributes();
		la.setLineAntialiasingEnable(true);
		la.setLineWidth(style.getLineWidth());
		la.setCapability(LineAttributes.ALLOW_WIDTH_WRITE); // Added by Paco
		appearance.setLineAttributes(la);
		
		PointAttributes pat = new PointAttributes();
		pat.setCapability(PointAttributes.ALLOW_SIZE_WRITE);
		pat.setCapability(PointAttributes.ALLOW_ANTIALIASING_WRITE);
		appearance.setPointAttributes(pat);
		
		//Used to make setVisible() work
		RenderingAttributes ra = new RenderingAttributes();
		ra.setCapability(RenderingAttributes.ALLOW_VISIBLE_WRITE);
		ra.setCapability(RenderingAttributes.ALLOW_IGNORE_VERTEX_COLORS_WRITE);
		appearance.setRenderingAttributes(ra);
		
		//Used to enable transparent colors
		transparency = new TransparencyAttributes();
		transparency.setCapability(TransparencyAttributes.ALLOW_VALUE_WRITE);
		transparency.setCapability(TransparencyAttributes.ALLOW_MODE_WRITE);
		appearance.setTransparencyAttributes(transparency);
		
		//Used to enable colors
		Material m = new Material();
		m.setCapability(Material.ALLOW_COMPONENT_WRITE);
		appearance.setMaterial(m);
		
		ColoringAttributes c = new ColoringAttributes();
		c.setCapability(ColoringAttributes.ALLOW_COLOR_WRITE);
		appearance.setColoringAttributes(c);
	
		applyStyleChange(Style.CHANGED_LINE_COLOR); applyStyleChange(Style.CHANGED_FILL_COLOR);
		applyStyleChange(Style.CHANGED_LINE_WIDTH); applyStyleChange(Style.CHANGED_DRAWING_FILL);
		applyStyleChange(Style.CHANGED_DRAWING_LINES);	
	}

	public Appearance getAppearance() { return appearance; }

	public void applyStyleChange(int _change) {
		  switch (_change) {
		    case Style.CHANGED_LINE_COLOR :
		      Color lineColor = style.getLineColor();
		      float[] lineComponents = lineColor.getRGBComponents(null);
		      appearance.getRenderingAttributes().setIgnoreVertexColors(true);
		      appearance.getColoringAttributes().setColor(lineComponents[0], lineComponents[1], lineComponents[2]);
		      transparency.setTransparency(1f - lineComponents[3]);
		      if (lineComponents[3] != 1.0f) {
		        appearance.getPolygonAttributes().setCullFace(PolygonAttributes.CULL_NONE);
		        transparency.setTransparencyMode(TransparencyAttributes.NICEST);
		      }
		      else {
		        appearance.getPolygonAttributes().setCullFace(PolygonAttributes.CULL_BACK);
		        transparency.setTransparencyMode(TransparencyAttributes.NONE);
		      }
		      appearance.getMaterial().setSpecularColor(new Color3f(Color.white));
		      appearance.getMaterial().setShininess(shininess);
		      appearance.getMaterial().setDiffuseColor(lineComponents[0], lineComponents[1], lineComponents[2]);

		       
		      break;
		    case Style.CHANGED_LINE_WIDTH : 
		      appearance.getLineAttributes().setLineWidth(style.getLineWidth()); 
		      break;
		    case Style.CHANGED_FILL_COLOR : 
		      java.awt.Paint fillPaint = style.getFillColor();
		      if (fillPaint instanceof Color) {
		        Color fillColor = (Color) fillPaint;
		        float[] fillComponents = fillColor.getRGBComponents(null);
		        appearance.getMaterial().setAmbientColor((float)(fillComponents[0]*style.getAmbientFactor()),
		        										 (float) (fillComponents[1]*style.getAmbientFactor()),
		        										 (float) (fillComponents[2]*style.getAmbientFactor())); 
		        appearance.getMaterial().setDiffuseColor(fillComponents[0], fillComponents[1], fillComponents[2]);
		        
		        if (fillComponents[3] != 1.0f) {
		          //appearance.getPolygonAttributes().setCullFace(PolygonAttributes.CULL_NONE);
		          transparency.setTransparencyMode(TransparencyAttributes.BLENDED);
		        }
		        else {
		          appearance.getPolygonAttributes().setCullFace(PolygonAttributes.CULL_BACK);
		          transparency.setTransparencyMode(TransparencyAttributes.NONE);
		        }
		        transparency.setTransparency(1f - fillComponents[3]);
		        appearance.getMaterial().setSpecularColor(new Color3f(Color.white));
		        appearance.getMaterial().setShininess(shininess);
		      }
		      break;
		    case Style.CHANGED_DRAWING_FILL :
		      if (style.isDrawingFill()) this.turnFillOn();
		      else if (style.isDrawingLines()) this.turnLinesOn();
		      break;
		    case Style.CHANGED_DRAWING_LINES :
		      if (style.isDrawingLines() && !style.isDrawingFill()) turnLinesOn();
		      //if (style.isDrawingLines()) turnLinesOn();
		      break;
		    case Style.CHANGED_TEXTURES :
		      setTexture(style.getTextures(),style.getTransparency(),style.getCombine());
		      break;
		    case Style.CHANGED_DEPTH_FACTOR :
		      appearance.getPolygonAttributes().setPolygonOffset(0.1f);
		      appearance.getPolygonAttributes().setPolygonOffsetFactor((float)style.getDepthFactor());
		      break;
		  }
	}

	private void setTexture(Object[] textures, double transparency, boolean combine){

	  //Transparency attributes
	  TransparencyAttributes ta = null;
	  TextureUnitState textureUnitState[] = new TextureUnitState[2];
	  boolean t1 = false, t2 = false;
	  
	  //Update texture variables
	  if (transparency<=0 || transparency>=1) transparency = Double.NaN;
	  if (textures[0]==null && textures[1]==null) { 
	    appearance.setTexture(null);
	    return;
	  }

	  if (textures[0]!=null) {
	    if (textures[0] instanceof BufferedImage){		
	    	if( ((BufferedImage)textures[0]).getWidth()!=imgWidth || ((BufferedImage)textures[0]).getHeight()!=imgHeight){
	    		image2D = new ImageComponent2D(ImageComponent.FORMAT_RGBA, ((BufferedImage)textures[0]).getWidth(), ((BufferedImage)textures[0]).getHeight());
	   			image2D.setCapability(ImageComponent.ALLOW_IMAGE_WRITE);
	   			tex2D = new Texture2D(Texture.BASE_LEVEL, Texture.RGBA,
	   					image2D.getWidth(), image2D.getHeight());
	   			tex2D.setCapability(Texture.ALLOW_IMAGE_WRITE);
    			tex2D.setCapability(Texture.ALLOW_ENABLE_WRITE);	    			
    			tex2D.setMagFilter(Texture.NICEST);
	    		image2D.set((BufferedImage)textures[0]);
	    		tex2D.setImage(0, image2D);
	    		tex2D.setEnable(true);
	    		texture1 = tex2D;
	    		imgWidth = ((BufferedImage)textures[0]).getWidth();
	    		imgHeight = ((BufferedImage)textures[0]).getHeight();
	    	}
	    	else{
	    		image2D.set((BufferedImage)textures[0]);
	    		tex2D.setImage(0, image2D);
	    		tex2D.setEnable(true);
	    		texture1 = tex2D;
	    	}
	    }
	    else {
	    	org.opensourcephysics.tools.Resource res = ResourceLoader.getResource(textures[0].toString());
	    	if (res!=null) {
	    		java.net.URL url = res.getURL();
	    		texture1 = new TextureLoader(url,TextureLoader.GENERATE_MIPMAP,null).getTexture();
	    		texture1.setBoundaryModeT(Texture.WRAP);
	    		texture1.setBoundaryModeS(Texture.WRAP);
	    	}
	    	else System.err.println("Warning: Texture file not found = "+textures[0]);
	    }
	 }

	  if (textures[1]!=null){
      if (textures[1] instanceof BufferedImage) texture2 = new TextureLoader((BufferedImage)textures[1],TextureLoader.GENERATE_MIPMAP | TextureLoader.Y_UP).getTexture();
      else {
        java.net.URL url = ResourceLoader.getResource(textures[1].toString()).getURL();
        texture2 = new TextureLoader(url,TextureLoader.GENERATE_MIPMAP,null).getTexture();
      }
	    texture2.setBoundaryModeT(Texture.WRAP);
	    texture2.setBoundaryModeS(Texture.WRAP);
	  }

	  //Transparency attributes
	  if (!Double.isNaN(transparency)) {
	    ta = new TransparencyAttributes();
	    ta.setTransparencyMode (TransparencyAttributes.BLENDED);
	    ta.setTransparency ((float)transparency);
	  }

	  // Texture attributes creation
	  TextureAttributes texAttr1 = new TextureAttributes();
	  if (combine) texAttr1.setTextureMode(TextureAttributes.DECAL);
	  else texAttr1.setTextureMode(TextureAttributes.REPLACE);

	  TextureAttributes texAttr2 = new TextureAttributes();
	  if (combine) texAttr2.setTextureMode(TextureAttributes.MODULATE);
	  else texAttr2.setTextureMode(TextureAttributes.REPLACE);

	  // TextureUnitState
	  if (textures[0]!=null && textures[1]!=null) {
	    textureUnitState[0] = new TextureUnitState(texture1, texAttr1, null);
	    textureUnitState[0].setCapability(TextureUnitState.ALLOW_STATE_WRITE);
	    textureUnitState[1] = new TextureUnitState(texture2, texAttr2, null);
	    textureUnitState[1].setCapability(TextureUnitState.ALLOW_STATE_WRITE);
	    t1 = t2 = true;
	  }
	  else if (textures[0]==null && textures[1]!=null){t1=false; t2=true;}
	  else {t1=true;t2=false;}

	  Element element = style.getElement();
	  Java3dElement j3dElement = (Java3dElement) element.getImplementingObject();

	  if (!j3dElement.isPrimitive()){
	    if(texture1!=null) texture1.setMinFilter(Texture.MULTI_LEVEL_LINEAR);
	    if(texture2!=null) texture2.setMinFilter(Texture.MULTI_LEVEL_LINEAR);
	    int flag = TexCoordGeneration.OBJECT_LINEAR;
	    float[] u = {0.0f,0.0f,0.0f,0.0f};
	    float[] v = {0.0f,0.0f,0.0f,0.0f};
	 	if((element instanceof ElementSphere) || (element instanceof ElementEllipsoid) || (element instanceof ElementObject)) {u[0]=1.0f;v[2]=1.0f;}
	    if(element instanceof ElementCylinder){u[0]=1.0f;v[2]=1.0f;}
	    if((element instanceof ElementCone) || (element instanceof ElementSurface)){u[0]=1.0f;v[1]=1.0f;}
	    TexCoordGeneration texGen = new TexCoordGeneration(flag, TexCoordGeneration.TEXTURE_COORDINATE_3,
	 																new Vector4f(u), new Vector4f(v), 
	 																	new Vector4f(0.0f,0.0f,0.0f,1.0f));
	    texGen.setCapability(TexCoordGeneration.ALLOW_ENABLE_WRITE);
	    texGen.setEnable(true);
	    appearance.setTexCoordGeneration(texGen);
	  }
	  else appearance.setTexCoordGeneration(null);
	 
	  
	  if(t1 && !t2 && j3dElement.isPrimitive()){appearance.setTextureUnitState(null); appearance.setTexture(texture1);}
	  else if (t1 && !t2 && !j3dElement.isPrimitive()) appearance.setTexture(texture1);
	  else if(!t1 && t2 && j3dElement.isPrimitive()){appearance.setTextureUnitState(null); appearance.setTexture(texture2);}
	  else if (!t1 && t2 && !j3dElement.isPrimitive()) appearance.setTexture(texture2);
	  else appearance.setTextureUnitState(textureUnitState);
	  appearance.setTransparencyAttributes(ta);
	  element.addChange(Element.CHANGE_COLOR);
	}

	private void turnFillOn() {
	  java.awt.Paint fill = style.getFillColor();
	  appearance.getPolygonAttributes().setPolygonMode(PolygonAttributes.POLYGON_FILL);
	  appearance.getPolygonAttributes().setCullFace(PolygonAttributes.CULL_BACK);
	  if (fill instanceof Color) appearance.getMaterial().setDiffuseColor(new Color3f((Color)fill));
	  appearance.getMaterial().setSpecularColor(new Color3f(Color.white));
	}

	private void turnLinesOn() {
	  appearance.getPolygonAttributes().setPolygonMode(PolygonAttributes.POLYGON_LINE);
	  appearance.getPolygonAttributes().setCullFace(PolygonAttributes.CULL_NONE);
	  appearance.getMaterial().setDiffuseColor(new Color3f(style.getLineColor()));
	  appearance.getMaterial().setSpecularColor(new Color3f(Color.white));
	}


}
