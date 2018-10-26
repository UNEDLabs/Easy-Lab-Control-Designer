package org.colos.ejs.model_elements;

import javax.swing.text.JTextComponent;

import org.colos.ejs.osejs.edition.SearchResult;

public class ModelElementSearch extends SearchResult {

  private ModelElementsCollection mCollection;
  private ModelElement mElement;
  private String mElementName;
  
  public ModelElementSearch (ModelElementsCollection collection, ModelElement element, String elementName, String anInformation, String aText, JTextComponent aComponent, int aLineNumber, int aCaretPosition) {
    super (anInformation,aText,aComponent,aLineNumber,aCaretPosition);
    this.mElementName = elementName;
    this.mElement = element;
    this.mCollection = collection;
  }

  public final String getElementName() { return mElementName; }
  
  public final ModelElement getElement() { return mElement; }

  @Override
  public String toString () {
    return information+"("+lineNumber+"): "+textFound;
  }

  @Override
  public void show () {
   String name = mCollection.getEJS().getModelEditor().getElementsEditor().selectElement(mElement);
    // Now, make the text visible
    mElement.showEditor(name, mCollection.getEJS().getMainFrame(), mCollection);
    if (containerTextComponent!=null) {
      containerTextComponent.requestFocusInWindow();
      containerTextComponent.setCaretPosition(caretPosition);
    }
  }
  
  public ModelElementsCollection getCollection() { return mCollection; }

}
