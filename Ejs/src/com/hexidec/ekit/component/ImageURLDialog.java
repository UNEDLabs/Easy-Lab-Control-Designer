/*
GNU Lesser General Public License

ImageURLDialog
Copyright (C) 2005  Mattias Malmgren & Howard Kistler

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/

package com.hexidec.ekit.component;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class ImageURLDialog extends JDialog implements ActionListener
{
    JTextField url_text_field;
    String sUrl = null;

    public ImageURLDialog(Frame parent, String title, boolean modal)
    {
		super(parent, title, modal);

		// Layout stuff
		GridBagLayout g = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		JPanel jPanel = new JPanel();
		jPanel.setLayout(g);
		c.insets=new Insets(3,3,3,3);

		// The textfield to write the URL in
		url_text_field = new JTextField("http://",40);
		g.setConstraints(url_text_field, c);
		jPanel.add(url_text_field);

		// The load button
		JButton load = new JButton("Load");
		load.addActionListener(this);
		g.setConstraints(load, c);
		jPanel.add(load);

		// Att the panel to the content pane
		getContentPane().add(jPanel);
    }

	public void actionPerformed(ActionEvent e)
	{
		String argument = e.getActionCommand();
		if(argument.equals("Load"))
		{
			sUrl = url_text_field.getText().trim();
			setVisible(false);
			dispose();
		}
	}

	public String getURL()
	{
		return sUrl;
	}
}
