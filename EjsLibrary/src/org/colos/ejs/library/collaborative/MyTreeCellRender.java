/**
 * The package contains classes to generate an applet collaborative
 * Copyright (c) Dec 2007 C. Jara and F. Candelas
 * @author C. Jara and F. Candelas (http://www.aurova.ua.es).
 */

package org.colos.ejs.library.collaborative;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeCellRenderer;


/**
 * TreeCellRenderer to manage the ListTree
 */

public class MyTreeCellRender extends JLabel implements TreeCellRenderer {
	

	//Serial version ID
	private static final long serialVersionUID = 1L;
	
	//Font
    static protected Font defaultFontRoot;
    static protected Font defaultFontStudent;
    
    //Icons
    static protected Icon collapsedIconStudent;
    static protected Icon expandedIconStudent;
    static protected Icon collapsedIconRoot;
    static protected Icon expandedIconRoot;
    static protected Icon leafIcon;
    
    //Collaboration
    private boolean chalk = false;
    private boolean selected = false;

    //Color
    static protected final Color SelectedBackgroundColor = Color.yellow;

    static
    {
    	try {defaultFontRoot = new Font("Serif", Font.BOLD, 16);
    		 defaultFontStudent = new Font("Serif", Font.BOLD, 12);}catch (Exception ignored) {}
    	try {
    		/*collapsedIconStudent = new ImageIcon(MyTreeCellRender.class.getResource("/org/colos/ejs/library/collaborative/images/collapsed.gif"));
    		expandedIconStudent = new ImageIcon(MyTreeCellRender.class.getResource("/org/colos/ejs/library/collaborative/images/expanded.gif"));
    		collapsedIconRoot = new ImageIcon(MyTreeCellRender.class.getResource("/org/colos/ejs/library/collaborative/images/collapsedR.gif"));
    		expandedIconRoot = new ImageIcon(MyTreeCellRender.class.getResource("/org/colos/ejs/library/collaborative/images/expandedR.gif"));
    		leafIcon = new ImageIcon(MyTreeCellRender.class.getResource("/org/colos/ejs/library/collaborative/images/leaf.gif"));*/
    		collapsedIconStudent = org.opensourcephysics.tools.ResourceLoader.getIcon("/org/colos/ejs/library/collaborative/images/collapsed.gif");
    		expandedIconStudent = org.opensourcephysics.tools.ResourceLoader.getIcon("/org/colos/ejs/library/collaborative/images/expanded.gif");
    		collapsedIconRoot = org.opensourcephysics.tools.ResourceLoader.getIcon("/org/colos/ejs/library/collaborative/images/collapsedR.gif");
    		expandedIconRoot =org.opensourcephysics.tools.ResourceLoader.getIcon("/org/colos/ejs/library/collaborative/images/expandedR.gif");
    		leafIcon = org.opensourcephysics.tools.ResourceLoader.getIcon("/org/colos/ejs/library/collaborative/images/leaf.gif");
    	}catch (Exception e) {System.out.println("Images not found: " + e);}
    }

	public Component getTreeCellRendererComponent(JTree tree, Object value,
			boolean _selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
		// TODO Auto-generated method stub
		
		String stringValue = tree.convertValueToText(value, selected,expanded, leaf, row, hasFocus);
		
		setText(stringValue);
		/* Tooltips used by the tree. */
		setToolTipText(stringValue);
		/* Set the image. */
		if(expanded)
		{
			if(((DefaultMutableTreeNode)value).isRoot()) setIcon(expandedIconRoot);
			else setIcon(expandedIconStudent);
		}
		else if(!leaf)
		{
			 if(((DefaultMutableTreeNode)value).isRoot()) setIcon(collapsedIconRoot);
			 else setIcon(collapsedIconStudent);
		}
		else
		    setIcon(leafIcon);//leaf

		/* Set the color and the font based on the SampleData userObject. */
		DataNode userObject = (DataNode)((DefaultMutableTreeNode)value).getUserObject();
		                              
		if(hasFocus)
		    setForeground(Color.red);
		else
		    setForeground(userObject.getColor());
		
		if(userObject.getFont() == null)
		{
		    if(((DefaultMutableTreeNode)value).isRoot()) setFont(defaultFontRoot);
		    else setFont(defaultFontStudent);
		}
		else
		    setFont(userObject.getFont());
		
		this.selected = _selected;
		return this;
	}
	
	public void setChalk(boolean value){
		chalk = value;
	}
	
	public void paint(Graphics g) {
		Color cColor = null;
		Icon currentIcon = getIcon();
		if (chalk & selected)
			cColor = Color.yellow;
		else if(chalk)
			setForeground(Color.blue);
		else if(getParent() != null)
		    cColor = getParent().getBackground();
		else
		    cColor = getBackground();
		
		g.setColor(cColor);
			if(currentIcon != null && getText() != null) {
			    int offset = (currentIcon.getIconWidth() + getIconTextGap());
		            if (getComponentOrientation().isLeftToRight()) {
		                g.fillRect(offset, 0, getWidth() - 1 - offset,getHeight() - 1);
		            }
		            else {
		                g.fillRect(0, 0, getWidth() - 1 - offset, getHeight() - 1);
		            }
			}
			else
			    g.fillRect(0, 0, getWidth()-1, getHeight()-1);
			super.paint(g);
	}

}
