/**
 ** com/charliemouse/cambozola/accessories/ZoomOutAccessory.java
 **  Copyright (C) Andy Wilcock, 2003.
 **  Available from http://www.charliemouse.com
 **
 **  Cambozola is free software; you can redistribute it and/or modify
 **  it under the terms of the GNU General Public License as published by
 **  the Free Software Foundation; either version 2 of the License, or
 **  (at your option) any later version.
 **
 **  Cambozola is distributed in the hope that it will be useful,
 **  but WITHOUT ANY WARRANTY; without even the implied warranty of
 **  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 **  GNU General Public License for more details.
 **
 **  You should have received a copy of the GNU General Public License
 **  along with Cambozola; if not, write to the Free Software
 **  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 **/
package com.charliemouse.cambozola;

import java.awt.Point;

//import com.charliemouse.cambozola.Accessory;
//import com.charliemouse.cambozola.ViewerAttributeInterface;

public class ZoomOutAccessory extends Accessory
{
	public ZoomOutAccessory()
	{
		super();
	}

    public String getName()
    {
        return "Zoom Out";
    }

    public String getIconLocation()
    {
        return "zoomout.gif";
    }

    public String getDescription()
    {
        return "Zoom out of stream image";
    }

    public void actionPerformed(Point p)
    {
        ViewerAttributeInterface vfi = getViewerAttributes();
        vfi.getViewArea().zoomOut();
        vfi.repaint();
    }
}
