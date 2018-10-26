package org.opensourcephysics.drawing3d.java3d;



import javax.media.j3d.BranchGroup;
import javax.media.j3d.GeometryArray;
import javax.media.j3d.LineStripArray;
import javax.media.j3d.Shape3D;
import javax.media.j3d.Transform3D;
import javax.vecmath.TexCoord2f;
import javax.vecmath.Vector3d;


import org.opensourcephysics.display3d.simple3d.utils.VectorAlgebra;
import org.opensourcephysics.drawing3d.Element;
import org.opensourcephysics.drawing3d.ElementSpring;
import org.opensourcephysics.drawing3d.utils.Resolution;
import org.opensourcephysics.drawing3d.utils.Style;

/**
 * <p>Title: Java3dElementSpring </p>
 * <p>Description: A 3D spring object using Java 3D.</p>
 * @author Carlos Jara Bravo
 * @author Francisco Esquembre
 * @version September 2009
 */
public class Java3dElementSpring extends Java3dElement {

	private LineStripArray lines = null;
	private BranchGroup bg;
	private TexCoord2f[] texCoord = null; 
	private int[] stripVertexCounts = null;
	private double[] points = null;
	private int length = 0;
	private double[] nextPoint = null;
	
	public Java3dElementSpring(ElementSpring _element) {
		super(_element);
		points = new double[300];
		nextPoint = new double[3];
		getAppearance().getLineAttributes().setLineAntialiasingEnable(true);
		getAppearance().getPointAttributes().setPointSize(0.1f);
	    element.getStyle().setResolution(new Resolution (8, 15, 1)); // the 1 is meaningless 
	    element.addChange(Element.CHANGE_SHAPE);
	}

	public void processChanges(int _change, int _cummulativeChange) {
		super.processChanges(_change, _cummulativeChange);
		if ((_change & Element.CHANGE_SHAPE) != 0 || (_change & Element.CHANGE_SIZE) != 0) computePoints();
		if ((_change & Element.CHANGE_POSITION) != 0 || (_change & Element.CHANGE_POSITION_AND_SIZE)!=0) setScaleSpring();
	}
	
	 public void styleChanged(int _change) {
		 super.styleChanged(_change);
		 switch (_change) {
		    case Style.CHANGED_LINE_COLOR :
			 element.getStyle().setFillColor(element.getStyle().getLineColor());
			 break;
		 }
	 }
	
	
	@Override
	public boolean isPrimitive() {return true;}
	
	private void init(){
		stripVertexCounts = new int[]{Math.max(2, length)};
		lines = new LineStripArray(points.length/3, LineStripArray.COORDINATES | 
													  LineStripArray.COLOR_3 | LineStripArray.NORMALS | 
													    LineStripArray.TEXTURE_COORDINATE_2,
									              		    stripVertexCounts);

		lines.setCapability(GeometryArray.ALLOW_COLOR_WRITE);
		lines.setCapability(GeometryArray.ALLOW_COUNT_WRITE);
		lines.setCapability(GeometryArray.ALLOW_COORDINATE_WRITE);
		lines.setCapability(GeometryArray.ALLOW_TEXCOORD_WRITE);
		lines.setCoordinates(0, points);
		
		bg = new BranchGroup();
		bg.setCapability(BranchGroup.ALLOW_DETACH);
		bg.addChild(new Shape3D(lines, getAppearance()));
		addNode(bg);
	}
	
	private void computePoints(){
		clear();
		int theLoops = 0, thePPL = 0;
		Resolution res = element.getStyle().getResolution();
		if (res!=null) {
		      switch(res.getType()) {
		         case Resolution.DIVISIONS :
		           theLoops = Math.max(res.getN1(), 0);
		           thePPL = Math.max(res.getN2(), 1);
		           break;
		         case Resolution.MAX_LENGTH :
		             theLoops = Math.max((int) Math.round(0.49+element.getDiagonalSize()/res.getMaxLength()), 1);
		             thePPL = 15;
		             break;
		    }
		}
		else{
		    theLoops = 8;
		    thePPL = 15;
		}
		    
		ElementSpring spring = (ElementSpring) element;
		int segments = theLoops*thePPL+3;
		texCoord = new TexCoord2f[segments+1];
		double delta=2.0*Math.PI/thePPL;
		double radius = spring.getRadius(), solenoid = spring.getSolenoid(); 
		if (radius<0) delta*=-1;
		int pre = thePPL/2;
		double[] size = element.getSize();
		double[] v = {0,0,0}; 
	    v[0] = size[0]; v[1] = size[1]; v[2] = size[2];
	    if(size[0]==0.0){ size[0]=1.0; }
	    if(size[1]==0.0){ size[1]=1.0; }
	    if(size[2]==0.0){ size[2]=1.0; }
	    setScaleSpring();
	    
	    double[] u1 = VectorAlgebra.normalTo(v);
	    double[] u2 = VectorAlgebra.normalize(VectorAlgebra.crossProduct(v, u1));
		    
		for (int i=0; i<=segments; i++) {
		        int k;
		        if (spring.isThinExtremes()) {
		          if (i < pre) k = 0;
		          else if (i < thePPL) k = i - pre;
		          else if (i > (segments - pre)) k = 0;
		          else if (i > (segments - thePPL)) k = segments - i - pre;
		          else k = pre;
		        }
		        else k = pre;
		        double angle = i*delta;
		        double cos = Math.cos(angle), sin = Math.sin(angle);
		        double x,y,z;
		        if (solenoid!=0.0)  {
		          double cte = k*Math.cos(i*2*Math.PI/thePPL)/pre;
		          x = (solenoid*cte*v[0] + i*v[0]/segments+k*radius*(cos*u1[0]+sin*u2[0])/pre);
		          y = (solenoid*cte*v[1] + i*v[1]/segments+k*radius*(cos*u1[1]+sin*u2[1])/pre);
		          z = (solenoid*cte*v[2] + i*v[2]/segments+k*radius*(cos*u1[2]+sin*u2[2])/pre);
		          addPoint(x,y,z);
		        }
		        else {
		          x = (i*v[0]/segments+k*radius*(cos*u1[0]+sin*u2[0])/pre);
		          y = (i*v[1]/segments+k*radius*(cos*u1[1]+sin*u2[1])/pre);
		          z = (i*v[2]/segments+k*radius*(cos*u1[2]+sin*u2[2])/pre);
		          addPoint(x,y,z);
		        }
		}
	}
	
	private void setScaleSpring(){
		Transform3D scale = new Transform3D(); Vector3d vscale = new Vector3d();
		this.getTransformGroup().getTransform(scale); //setTransform(scale);
	    scale.getScale(vscale);
	    vscale.x=1.0;vscale.y=1.0;vscale.z=1.0;
	    scale.setScale(vscale);
	    this.getTransformGroup().setTransform(scale);
	}
	
	private void clear(){
		 length = 0;
		 init();
	}
	 
	
	 private void addPoint(double x, double y, double z) {
		nextPoint[0] = x; nextPoint[1] = y;  nextPoint[2] = z;
		if (length*3 == points.length) {
			double[] newArray = new double[length*3*2];
			System.arraycopy(points, 0, newArray, 0, length*3);
			points = newArray;
			init();
			styleChanged(Style.CHANGED_TEXTURES);
		}
		else {
			if (lines == null)	init();
			updatePoints();
		}
	 }

	 private void updatePoints(){
	
		//Points
		points[length*3] = nextPoint[0];points[length*3+1] = nextPoint[1];points[length*3+2] = nextPoint[2];
		lines.setCoordinate(length, new double[]{points[length*3],points[length*3+1],points[length*3+2]}); 
		
		//Texture
		texCoord[length] = new TexCoord2f();
		texCoord[length].x = (float) (nextPoint[0]/Math.sqrt(nextPoint[0]*nextPoint[0]+nextPoint[1]*nextPoint[1]+nextPoint[2]*nextPoint[2]));
		texCoord[length].y = (float) (nextPoint[1]/Math.sqrt(nextPoint[0]*nextPoint[0]+nextPoint[1]*nextPoint[1]+nextPoint[2]*nextPoint[2]));
		lines.setTextureCoordinate(0,length,texCoord[length]);
	
		 //Increment Length
		 length++;
	
		 if (length < 2) { //Fill the next slot with the same point so we don't get a random line
			 points[length*3] = nextPoint[0];
			 points[length*3+1] = nextPoint[1];
			 points[length*3+2] = nextPoint[2];
			 lines.setCoordinate(length, new double[]{points[length*3],points[length*3+1],points[length*3+2]});
		
			 texCoord[length] = new TexCoord2f();
			 texCoord[length].x = (float) (nextPoint[0]/Math.sqrt(nextPoint[0]*nextPoint[0]+nextPoint[1]*nextPoint[1]+nextPoint[2]*nextPoint[2]));
			 texCoord[length].y = (float) (nextPoint[1]/Math.sqrt(nextPoint[0]*nextPoint[0]+nextPoint[1]*nextPoint[1]+nextPoint[2]*nextPoint[2]));
			 lines.setTextureCoordinate(0,length,texCoord[length]);
		 }
	
		 //Strip vertex
		 stripVertexCounts[0] = Math.max(2, length);
		 lines.setStripVertexCounts(stripVertexCounts);
	}

}
