package es.uned.dia.audio.model_elements;

import java.awt.Component;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

import org.colos.ejs.model_elements.AbstractModelElement;
import org.colos.ejs.model_elements.ModelElementsCollection;

/**
 * Audio Player Element  
 * 
 * @author Jesús Chacón Sombría <jcsombria@gmail.com>
 */
public class AudioElement extends AbstractModelElement {
	public static final String ICON_FILE = "es/uned/dia/audio/resources/AudioPlayer.png";
	static ImageIcon ELEMENT_ICON = AbstractModelElement.createImageIcon(ICON_FILE);

	private JTextField urlText = new JTextField("http://localhost/axis-cgi/audio/receive.cgi", 4);

	// -------------------------------
	// Implementation of ModelElement
	// -------------------------------

	/**
	 * Obtain the image icon of the element
	 * @return An <i>ImageIcon</i> object
	 */
	public ImageIcon getImageIcon() { return ELEMENT_ICON; }

	/**
	 * Obtain the generic name of the element
	 * @return The <i>String</i> with the generic name of the element
	 */
	public String getGenericName() { return "AudioPlayer"; } 

	/**
	 * Obtain the tooltip of the element
	 * @return The <i>String</i> with the name of the element
	 */
	public String getConstructorName() { return "es.uned.dia.audio.MultipartAudioPlayer"; }

	/**
	 * Obtain the display info String of the element
	 * @return The <i>String</i> with the display info
	 */
	public String getDisplayInfo() { return "(" + urlText.getText() + ")";}

	// -------------------------------
	// Help and edition
	// -------------------------------

	/**
	 * Obtain the tooltip for the element
	 * @return The tooltip <i>String</i>
	 */
	public String getTooltip() {
		return "Audio Player";
	}

	
	/**
	 * Obtain the html page with the help and/or description of the element
	 * @return The tooltip <i>String</i>
	 */
	@Override
	protected String getHtmlPage() { 
		return "es/uned/dia/audio/resources/AudioPlayer.html";
	}

	/**
	 * Obtain the tooltip for the element
	 * @return The tooltip <i>String</i>
	 */
	protected Component createEditor(String name, Component parentComponent, final ModelElementsCollection collection) {    
		JPanel mainPanel = new JPanel();
	    mainPanel.setBorder(BorderFactory.createTitledBorder("Audio settings"));    
		SpringLayout sl_mainPanel = new SpringLayout();
		mainPanel.setLayout(sl_mainPanel);

	    JLabel urlLabel = new JLabel("Streaming URL: ");
		sl_mainPanel.putConstraint(SpringLayout.NORTH, urlLabel, 7, SpringLayout.NORTH, mainPanel);
		sl_mainPanel.putConstraint(SpringLayout.WEST, urlLabel, 5, SpringLayout.WEST, mainPanel);
		sl_mainPanel.putConstraint(SpringLayout.VERTICAL_CENTER, urlText, 0, SpringLayout.VERTICAL_CENTER, urlLabel);
		sl_mainPanel.putConstraint(SpringLayout.WEST, urlText, 7, SpringLayout.EAST, urlLabel);
		sl_mainPanel.putConstraint(SpringLayout.EAST, urlText, -7, SpringLayout.EAST, mainPanel);

	    mainPanel.add(urlLabel);
	    mainPanel.add(urlText);
	    mainPanel.setPreferredSize(new Dimension(400,50));
	    mainPanel.setMaximumSize(new Dimension(400,50));

	    return mainPanel;
	} 

	/**
	 * Save the state of the element into an XML String
	 * @return String the XML String 
	 */
	public String savetoXML() {
		return "<url>" + urlText.getText() + "</url>";
	}

	/**
	 * Load the state of the element from an XML String
	 * @param String the XML String 
	 */
	public void readfromXML(String _inputXML) {
		int begin = _inputXML.indexOf("<url>") + "<url>".length();

		int end = _inputXML.indexOf("</url>");
		try {
			String url = _inputXML.substring(begin, end);
			urlText.setText(url);
		} catch(IndexOutOfBoundsException e) {
			System.out.println("Error restoring state of AudioPlayerElement: invalid XML sequence");	  
		}
	} 

	/**
	 * Obtain the initialization code required to instantiate the element.
	 * @param String the XML String 
	 */
	public String getInitializationCode(String _name) {
		String init = _name + " = new " + getConstructorName() + "(\"" + urlText.getText() + "\");";
		return init;
	}
}
