/*
GNU Lesser General Public License

TableInputDialog
Copyright (C) 2000 Howard Kistler

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

import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import com.hexidec.util.Translatrix;

/** Class for providing a dialog that lets the user specify values for tag attributes
  */
public class TableInputDialog extends JDialog
{
	private String inputRows   = new String();
	private String inputCols   = new String();
	private String inputBorder = new String();
	private String inputSpace  = new String();
	private String inputPad    = new String();
	private JOptionPane jOptionPane;

	public TableInputDialog(Frame parent, String title, boolean bModal)
	{
		super(parent, title, bModal);
		final JTextField jtxfRows   = new JTextField(3);
		final JTextField jtxfCols   = new JTextField(3);
		final JTextField jtxfBorder = new JTextField(3);
		final JTextField jtxfSpace  = new JTextField(3);
		final JTextField jtxfPad    = new JTextField(3);
		final Object[] buttonLabels = { Translatrix.getTranslationString("DialogAccept"), Translatrix.getTranslationString("DialogCancel") };
		Object[] panelContents = {
			Translatrix.getTranslationString("TableRows"),        jtxfRows,
			Translatrix.getTranslationString("TableColumns"),     jtxfCols,
			Translatrix.getTranslationString("TableBorder"),      jtxfBorder,
			Translatrix.getTranslationString("TableCellSpacing"), jtxfSpace,
			Translatrix.getTranslationString("TableCellPadding"), jtxfPad
		};
		jOptionPane = new JOptionPane(panelContents, JOptionPane.QUESTION_MESSAGE, JOptionPane.OK_CANCEL_OPTION, null, buttonLabels, buttonLabels[0]);

		setContentPane(jOptionPane);
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent we)
			{
				jOptionPane.setValue(new Integer(JOptionPane.CLOSED_OPTION));
			}
		});

		jOptionPane.addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent e)
			{
				String prop = e.getPropertyName();
				if(isVisible() 
					&& (e.getSource() == jOptionPane)
					&& (prop.equals(JOptionPane.VALUE_PROPERTY) || prop.equals(JOptionPane.INPUT_VALUE_PROPERTY)))
				{
					Object value = jOptionPane.getValue();
					if(value == JOptionPane.UNINITIALIZED_VALUE)
					{
						return;
					}
					jOptionPane.setValue(JOptionPane.UNINITIALIZED_VALUE);
					if(value.equals(buttonLabels[0]))
					{
						inputRows   = jtxfRows.getText();
						inputCols   = jtxfCols.getText();
						inputBorder = jtxfBorder.getText();
						inputSpace  = jtxfSpace.getText();
						inputPad    = jtxfPad.getText();
						setVisible(false);
					}
					else
					{
						inputRows   = "";
						inputCols   = "";
						inputBorder = "";
						inputSpace  = "";
						inputPad    = "";
						setVisible(false);
					}
				}
			}
		});
		this.pack();
	}

	public int getRows()
	{
		try
		{
			return Integer.parseInt(inputRows);
		}
		catch(NumberFormatException nfe)
		{
			return -1;
		}
	}

	public int getCols()
	{
		try
		{
			return Integer.parseInt(inputCols);
		}
		catch(NumberFormatException nfe)
		{
			return -1;
		}
	}

	public int getBorder()
	{
		try
		{
			return Integer.parseInt(inputBorder);
		}
		catch(NumberFormatException nfe)
		{
			return -1;
		}
	}

	public int getSpacing()
	{
		try
		{
			return Integer.parseInt(inputSpace);
		}
		catch(NumberFormatException nfe)
		{
			return -1;
		}
	}

	public int getPadding()
	{
		try
		{
			return Integer.parseInt(inputPad);
		}
		catch(NumberFormatException nfe)
		{
			return -1;
		}
	}
}

