/*
GNU Lesser General Public License

ImageDialog
Copyright (C) 2003 Howard Kistler & other contributors

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

import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.awt.image.ImageObserver;

import javax.swing.border.*;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.WindowConstants;

public class ImageDialog extends JDialog implements ActionListener
{
  private static final long serialVersionUID = 1L;

//  private final String[] Borders = new String[]
//	{
//		"none",
//		"solid",
//		"dotted",
//		"dashed",
//		"double",
//		"groove",
//		"ridge",
//		"inset",
//		"outset"
//	};
//	private String[] BorderColors = new String[]
//	{
//		"none",
//		"aqua",
//		"black",
//		"blue",
//		"fuschia",
//		"gray",
//		"green",
//		"lime",
//		"maroon",
//		"navy",
//		"olive",
//		"purple",
//		"red",
//		"silver",
//		"teal",
//		"white",
//		"yellow"
//	};
	private String[]BorderSizes = new String[]
	{
		"none",
		"1",
		"2",
		"3",
		"4",
		"5",
		"6",
		"7",
		"8",
		"9",
		"10"
	};
	private final String[]Wraps = new String[]
	{
		"none",
		"left",
		"right",
		"top",
		"middle",
		"bottom"
	};

	private Frame parent;
	private ExtendedHTMLEditorKit ImageHtmlKit;
	private HTMLDocument ImageHtmlDoc;
	@SuppressWarnings("rawtypes")
  private JList WrapList;
//	private JList BorderList;
	@SuppressWarnings("rawtypes")
  private JList BorderSizeList;
//	private JList BorderColorList;
	@SuppressWarnings("rawtypes")
  private JList ImageList;
	private JTextField ImageAltText;
	private JTextField ImageWidth;
	private JTextField ImageHeight;
	private JEditorPane PreviewPane;

	private String   imageDir;
	private String[] imageList;
	private String   previewImage;
	private String   selectedImage;

	public ImageDialog(Frame parent, String imageDir, String[] imageList, String title, boolean modal)
	{
		super(parent, title, modal);
		this.imageDir = imageDir;
		this.imageList = imageList;
		this.parent = parent;
		selectedImage = null;
		init();
	}

	public void actionPerformed(java.awt.event.ActionEvent e)
	{
		if(e.getActionCommand().equals("apply"))
		{
			ListSelectionModel sm = ImageList.getSelectionModel();
			if(sm.isSelectionEmpty())
			{
//				SimpleInfoDialog sidAbout = 
				  new SimpleInfoDialog(parent, "Error", true, "No image selected", ImageObserver.ERROR);
				ImageList.requestFocus();
			}
			else
			{
				if(validateControls())
				{
					previewSelectedImage();
				}
			}
		}	
		if(e.getActionCommand().equals("save"))
		{
			ListSelectionModel sm = ImageList.getSelectionModel();
			if(sm.isSelectionEmpty())
			{
//				SimpleInfoDialog sidAbout = 
				  new SimpleInfoDialog(parent, "Error", true, "No image selected", ImageObserver.ERROR);
				ImageList.requestFocus();
			}
			else
			{
				if(validateControls())
				{
					previewSelectedImage();
					selectedImage = previewImage;
					setVisible(false);
				}
			}
		}
		else if(e.getActionCommand().equals("cancel"))
		{
		  setVisible(false);
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
  public void init()
	{
		selectedImage = "";
		Container contentPane = getContentPane();
		contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
		//setBounds(100,100,500,300);
		setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);

		ImageList = new JList(imageList);
		ImageList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		ImageList.clearSelection();
		ListSelectionModel lsm = ImageList.getSelectionModel();

		/* Create the editor kit, document, and stylesheet */
		PreviewPane = new JEditorPane();
		PreviewPane.setEditable(false);
		ImageHtmlKit = new ExtendedHTMLEditorKit();
		ImageHtmlDoc = (HTMLDocument)(ImageHtmlKit.createDefaultDocument());
		ImageHtmlKit.setDefaultCursor(new Cursor(Cursor.TEXT_CURSOR));
		PreviewPane.setCaretPosition(0);
		//PreviewPane.getDocument().addDocumentListener(this);
		//StyleSheet styleSheet = ImageHtmlDoc.getStyleSheet();
		//ImageStyleSheet = styleSheet;
		lsm.addListSelectionListener(new ListSelectionListener() 
		{
			public void valueChanged(ListSelectionEvent e)
			{
				if(!e.getValueIsAdjusting() && validateControls())
				{
					previewSelectedImage();
				}
			}
				
		});

		JScrollPane imageScrollPane = new JScrollPane(ImageList);
		imageScrollPane.setPreferredSize(new Dimension(200,250));
		imageScrollPane.setMaximumSize(new Dimension(200,250));
		imageScrollPane.setAlignmentX(LEFT_ALIGNMENT);
		JPanel centerPanel = new JPanel();
		centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.X_AXIS));
		centerPanel.add(imageScrollPane);
		centerPanel.setBorder(BorderFactory.createTitledBorder("Server Images"));

		/* Set up the text pane */
		PreviewPane.setEditorKit(ImageHtmlKit);
		PreviewPane.setDocument(ImageHtmlDoc);
		PreviewPane.setMargin(new Insets(4, 4, 4, 4));
		JScrollPane previewViewport = new JScrollPane(PreviewPane);
		previewViewport.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		previewViewport.setPreferredSize(new Dimension(250,250));
		centerPanel.add(previewViewport); 

		JPanel controlsPanel = new JPanel();
		controlsPanel.setLayout(new BoxLayout(controlsPanel, BoxLayout.Y_AXIS));
		JPanel altPanel = new JPanel();
		altPanel.setLayout(new BoxLayout(altPanel, BoxLayout.X_AXIS));
		altPanel.add(Box.createHorizontalStrut(10));
		JLabel imageAltTextLabel = new JLabel("Alternate Text:", SwingConstants.LEFT);	  
		altPanel.add(imageAltTextLabel);

		ImageAltText = new JTextField("");
		ImageAltText.addActionListener(this);
		ImageAltText.setPreferredSize(new Dimension(300,25));
		ImageAltText.setMaximumSize(new Dimension(600,25));
		altPanel.add(ImageAltText);
		altPanel.add(Box.createHorizontalStrut(10));
		controlsPanel.add(altPanel);
		controlsPanel.add(Box.createVerticalStrut(5));

		JPanel dimPanel = new JPanel();
		dimPanel.setLayout(new BoxLayout(dimPanel, BoxLayout.X_AXIS));
		dimPanel.add(Box.createHorizontalStrut(10));
		JLabel imageWidthLabel = new JLabel("Width:", SwingConstants.LEFT);	  
		dimPanel.add(imageWidthLabel);
		ImageWidth = new JTextField("");
		ImageWidth.setPreferredSize(new Dimension(40,25));
		ImageWidth.setMaximumSize(new Dimension(40,25));
		dimPanel.add(ImageWidth);
		JLabel imageWidthPixels = new JLabel("pix", SwingConstants.LEFT);	  
		imageWidthPixels.setPreferredSize(new Dimension(20,10));
		dimPanel.add(imageWidthPixels);
		dimPanel.add(Box.createHorizontalStrut(10));
		JLabel imageHeightLabel = new JLabel("Height:", SwingConstants.LEFT);	  
		dimPanel.add(imageHeightLabel);
		ImageHeight = new JTextField("");
		ImageHeight.setPreferredSize(new Dimension(40,25));
		ImageHeight.setMaximumSize(new Dimension(40,25));
		dimPanel.add(ImageHeight);
		JLabel imageHeightPixels = new JLabel("pix", SwingConstants.LEFT);	  
		imageHeightPixels.setPreferredSize(new Dimension(20,10));
		dimPanel.add(imageHeightPixels);
		dimPanel.add(Box.createHorizontalStrut(10));

		JLabel wrapLabel = new JLabel("Wrap:", SwingConstants.LEFT);
		dimPanel.add(wrapLabel);
		WrapList = new JList(Wraps);
		WrapList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		WrapList.getSelectionModel().setSelectionInterval(0,0);
		JScrollPane wrapScrollPane = new JScrollPane(WrapList);
		wrapScrollPane.setAlignmentX(LEFT_ALIGNMENT);
		wrapScrollPane.setPreferredSize(new Dimension(80,40));
		wrapScrollPane.setMaximumSize(new Dimension(80,100));
		dimPanel.add(wrapScrollPane);
		controlsPanel.add(dimPanel);

		/*
		JPanel borderPanel = new JPanel();
		JLabel borderStyleLabel = new JLabel("Style:", SwingConstants.LEFT);
		borderPanel.add(borderStyleLabel);
		BorderList = new JList(Borders);
		BorderList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		BorderList.getSelectionModel().setSelectionInterval(0,0);
		JScrollPane borderScrollPane = new JScrollPane(BorderList);
		borderScrollPane.setAlignmentX(LEFT_ALIGNMENT);
		borderScrollPane.setPreferredSize(new Dimension(80,40));
		borderScrollPane.setMaximumSize(new Dimension(80,100));
		borderPanel.add(borderScrollPane);
		borderPanel.add(Box.createHorizontalStrut(5));
		*/

		dimPanel.add(Box.createHorizontalStrut(5));
		JLabel borderSizeLabel = new JLabel("Border Size:", SwingConstants.LEFT);
		dimPanel.add(borderSizeLabel);
		BorderSizeList = new JList(BorderSizes);
		BorderSizeList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		BorderSizeList.getSelectionModel().setSelectionInterval(0,0);
		JScrollPane borderSizeScrollPane = new JScrollPane(BorderSizeList);
		borderSizeScrollPane.setAlignmentX(LEFT_ALIGNMENT);
		borderSizeScrollPane.setPreferredSize(new Dimension(80,40));
		borderSizeScrollPane.setMaximumSize(new Dimension(80,100));
		dimPanel.add(borderSizeScrollPane);
		dimPanel.add(Box.createHorizontalStrut(10));
		dimPanel.add(Box.createVerticalStrut(10));

		/*
		JLabel borderColorLabel = new JLabel("Color:", SwingConstants.LEFT);
		borderPanel.add(borderColorLabel);
		BorderColorList = new JList(BorderColors);
		BorderColorList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		JScrollPane borderColorScrollPane = new JScrollPane(BorderColorList);
		borderColorScrollPane.setAlignmentX(LEFT_ALIGNMENT);
		borderColorScrollPane.setPreferredSize(new Dimension(80,40));
		borderPanel.add(borderColorScrollPane);
		controlsPanel.add(borderPanel);
		*/

		JPanel buttonPanel= new JPanel();
		buttonPanel.setBorder(new SoftBevelBorder(BevelBorder.LOWERED));
		JButton applyButton = new JButton("Apply");
		applyButton.setActionCommand("apply");
		applyButton.addActionListener(this);

		JButton saveButton = new JButton("Accept");
		saveButton.setActionCommand("save");
		saveButton.addActionListener(this);
		JButton cancelButton = new JButton("Cancel");
		cancelButton.setActionCommand("cancel");
		cancelButton.addActionListener(this);

		buttonPanel.add(applyButton);
		buttonPanel.add(saveButton);
		buttonPanel.add(cancelButton);

		contentPane.add(centerPanel);
		contentPane.add(controlsPanel);
		contentPane.add(buttonPanel);
		this.pack();
		setVisible(true);
    }

    private void previewSelectedImage()
    {
		ListSelectionModel sm = ImageList.getSelectionModel();
		if(!sm.isSelectionEmpty())
		{
			String theImage = imageList[sm.getMinSelectionIndex()];
			try
			{
				// Clear the preview area
				PreviewPane.setText("");
				StringBuffer attrString = new StringBuffer();
				if(!ImageHeight.getText().equals(""))
				{
					attrString.append("HEIGHT=\"" + ImageHeight.getText() + "\" ");
				}
				if(!ImageWidth.getText().equals(""))
				{
					attrString.append("WIDTH=\"" + ImageWidth.getText() + "\" ");
				}
				if(!ImageAltText.getText().equals(""))
				{
					attrString.append("ALT=\"" + ImageAltText.getText() + "\" ");
				}
				if(!WrapList.getSelectionModel().isSelectionEmpty())
				{
					String theWrap = Wraps[WrapList.getSelectionModel().getMinSelectionIndex()];
					if(!theWrap.equals("none"))
					{
					attrString.append("ALIGN=\"" + theWrap + "\" ");
					}
				}
				/*
				if(!BorderList.getSelectionModel().isSelectionEmpty())
				{
					String theBorder = Borders[BorderList.getSelectionModel().getMinSelectionIndex()];
					if(!theBorder.equals("none"))
					{
				*/
				String borderSize = null;
//				String borderColor = null;
				if(!BorderSizeList.getSelectionModel().isSelectionEmpty())
				{
					borderSize = BorderSizes[BorderSizeList.getSelectionModel().getMinSelectionIndex()];
					if(!borderSize.equals("none"))
					{
						attrString.append("BORDER=" + borderSize);
					}
				}
				else
				{
					borderSize = BorderSizes[0];
				}
				/*
						if(!BorderColorList.getSelectionModel().isSelectionEmpty())
						{
							borderColor = BorderColors[BorderColorList.getSelectionModel().getMinSelectionIndex()];						
						}
						else
						{
							borderColor = "gray";
						}
						attrString.append("STYLE=\"border: " + borderColor + " "  + borderSize + "px " + theBorder + "\"");
					}
				}
				*/
				previewImage = "<IMG SRC=" + imageDir + "/" + theImage + " " + attrString.toString() + ">";
				ImageHtmlKit.insertHTML(ImageHtmlDoc, 0, previewImage, 0, 0, HTML.Tag.IMG);
				repaint();
			}
			catch(Exception ex)
			{
				System.err.println("Exception previewing image");
			}
		}
	}

	private boolean validateControls()
	{
		boolean result = true;
		if(!ImageWidth.getText().equals(""))
		{
			try
			{
				Integer.parseInt(ImageWidth.getText());
			}
			catch (NumberFormatException e)
			{
				result = false;
//				SimpleInfoDialog sidAbout = 
				  new SimpleInfoDialog(parent, "Error", true, "Image Width is not an integer", ImageObserver.ERROR);
				ImageWidth.requestFocus();
			}
		}
		if( result && !ImageHeight.getText().equals(""))
		{
			try
			{
				Integer.parseInt(ImageHeight.getText());
			}
			catch (NumberFormatException e)
			{
				result = false;
//				SimpleInfoDialog sidAbout = 
				  new SimpleInfoDialog(parent, "Error", true, "Image Height is not an integer", ImageObserver.ERROR);
				ImageHeight.requestFocus();
			}
		}
		return result;
	}

    public String getSelectedImage()
    {
	  return selectedImage;
    }	
}
