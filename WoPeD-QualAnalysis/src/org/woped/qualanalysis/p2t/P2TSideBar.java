package org.woped.qualanalysis.p2t;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.BadLocationException;

import org.woped.core.controller.IEditor;
import org.woped.core.model.ModelElementContainer;
import org.woped.gui.translations.Messages;
import org.woped.qualanalysis.paraphrasing.controller.WebServiceThread;

@SuppressWarnings("serial")
/**
 * the sidebar to be used for the displaying of the natural text-presentation
 *
 */
public class P2TSideBar extends JPanel implements ActionListener{
//  in further releases the logic, listeners and the panel should be separated in different classes 
	private IEditor editor = null;
	private JEditorPane textpane = null;
	private org.woped.qualanalysis.p2t.Process2Text naturalTextParser = null;
	private JButton reload = null;
	private WebServiceThread webService;
	private boolean threadInProgress;
	
	/**
	 * 
	 * @param currentEditor	the editor in which the instance of the sidebar is used
	 */
	public P2TSideBar(IEditor currentEditor){
    	super();
    	editor = currentEditor;
    	addComponents();
    	new Thread(){
    		public void run(){
    			getText();
    		}
    	}.start();
    }
	
	/**
	 * Getter for the used parser
	 * @return	org.woped.qualanalysis.p2t.Process2Text 	used parser
	 */
	public org.woped.qualanalysis.p2t.Process2Text getNaturalTextParser() {
		return naturalTextParser;
	}
	
	/**
	 * Setter for the used pareser
	 * @param naturalTextParser 	the parser-instance to be used in this sidebar
	 */
	public void setNaturalTextParser(org.woped.qualanalysis.p2t.Process2Text naturalTextParser) {
		this.naturalTextParser = naturalTextParser;
	}
	/**
	 * Getter for the IEditor
	 * @return IEditor	the editor in which the instance of the sidebar is used
	 */
	public IEditor getEditor() {
		return editor;
	}
	/**
	 * Method to initialize and add the the components to the sidebar
	 */
    private void addComponents(){
    		JLabel header = new JLabel(Messages.getString("P2T.textBandTitle"));
    	this.add(header);
    	 //TODO replace with an reload-image
    		reload = new JButton("Reload");
    		reload.addActionListener(this);
    	this.add(reload);
    	
    	this.add(new HideLabel(Messages.getImageIcon("AnalysisSideBar.Cancel"),Messages.getString("Metrics.SideBar.Hide")));
    	
	    	textpane = new JEditorPane("text/html", "");
	    	textpane.addHyperlinkListener(new HyperlinkListener() {
				@Override
				public void hyperlinkUpdate(HyperlinkEvent hle) {
					if (hle.getEventType() == HyperlinkEvent.EventType.ACTIVATED){
						highlightElement(hle.getDescription());
					}	
				}
			});
			textpane.setAutoscrolls(true);
			textpane.setEditable(false);
	    	textpane.setMinimumSize(new Dimension(150,100));	    	
    	this.add(textpane);
    	
    }
    /**
     * Method to handle the highlighting of the elements and in the text
     * @param ids; id of the element and the text-passage to be highlighted 
     */
    private void highlightElement(String ids) {
    	String[] singleIDs = ids.split(",");
    	for (String id : singleIDs){
    		highlightIDinText(id);
    		id = id.split("_op_")[0]; //ignore the path option
    		highlightIDinProcess(id);
    	}
	}
    
    /**
     * Hanles the highlighting of the elements in the text
     * @param id; the ID of the element to be set highlighted
     */
    private void highlightIDinProcess(String id){
    	ModelElementContainer mec = editor.getModelProcessor().getElementContainer();
    	mec.getElementById(id).setHighlighted(true);
    }
    
    
    /**
     * 
     * Highlights passages linked to the given id within the displayed text
     * @param id, the id of the element of which the corresponding text is to be highlighted
     */
    public void highlightIDinText(String id){
    	clean();
    	//is there a linked text
    	if (naturalTextParser != null){
    		//get the text(s) of the id
    		String[] textsToHighlight = naturalTextParser.getLinkedTexts(id);
			for (String find : textsToHighlight){
				
				for (int index = 0; index + find.length() < textpane.getText().length(); index++) {
					String match = null;
				
					try {
						match = textpane.getText(index, find.length());
					} catch (BadLocationException e1) {
						break; //the end of the displayed Text is reached
					}
					//if the text is found
					if (find.equals(match)) {
						javax.swing.text.DefaultHighlighter.DefaultHighlightPainter highlightPainter = null;
						highlightPainter = new javax.swing.text.DefaultHighlighter.DefaultHighlightPainter(Color.YELLOW);
		
						try {
							textpane.getHighlighter().addHighlight(index,index + find.length(), highlightPainter);
						} catch (BadLocationException e) {
							// ignore
						}
					}
				}
			}
    	}
    }
    

	/**
	 * @author original by Mathias Gruschinske
	 * label with mouse listener and icon to hide the sidebar
	 */
	class HideLabel extends JLabel {

		private static final long serialVersionUID = 1L;

		public HideLabel(Icon icon, String toolTip) {
			super(icon);

			this.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
			this.setToolTipText(toolTip);

			this.addMouseListener(new MouseListener() {

				public void mouseReleased(MouseEvent e) {
				}

				public void mousePressed(MouseEvent e) {
				}

				public void mouseEntered(MouseEvent arg0) {
					setCursor(new Cursor(Cursor.HAND_CURSOR));
				}

				public void mouseExited(MouseEvent arg0) {
					setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
				}

				public void mouseClicked(MouseEvent e) {
					editor.hideP2TBar();
					editor.repaint();
				}
			});
		}
	}

	/**
	 * If the reload-button is pressed and the webservice thread is not in progress it will be startet
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == reload){
			new WebServiceThread(this);			
			if(JOptionPane.showConfirmDialog(null, Messages.getString("Paraphrasing.Load.Question.Content"), Messages.getString("Paraphrasing.Load.Question.Title"), JOptionPane.YES_NO_OPTION)  == JOptionPane.YES_OPTION){
				if(this.getThreadInProgress() == false){
					getText();
				}				
				else{
					JOptionPane.showMessageDialog(null, Messages.getString("Paraphrasing.Webservice.ThreadInProgress.Message"),
							Messages.getString("Paraphrasing.Webservice.Error.Title"), JOptionPane.INFORMATION_MESSAGE);
				}
			}
		}
		
	}
	/**
	 * Starts the webservice to get the description of the Petri-Net 
	 */
	private void getText(){
		this.textpane.setText(Messages.getString("P2T.loading"));
		clean();
		
		this.setThreadInProgress(true);
		webService = new WebServiceThread(this);
		webService.start();
		while(!webService.getIsFinished()){
			try {
				Thread.sleep(500);
			} catch (InterruptedException e1) {
				//ignore
			}
		}
		this.textpane.setText(naturalTextParser.getHtmlText());
		setThreadInProgress(false);
		webService = null;
	}
	/**
	 * Setter of the thread-in-progress flag
	 * @param boolen b; sets the in-progress-state-flag of the webservice
	 */
	private void setThreadInProgress(boolean b) {
		threadInProgress = b;
		
	}
	/**
	 * Getter of the Thread in 
	 * @return boolean; returns true if the webservice is still supposed to be in progress
	 */
	private boolean getThreadInProgress() {
		return threadInProgress;
	}
	
	/**
	 * Removes the highlights from the elements and the text. Afterwards those are repainted. 
	 */
	public void clean() {
		textpane.getHighlighter().removeAllHighlights();
		ModelElementContainer mec = editor.getModelProcessor().getElementContainer();
    	mec.removeAllHighlighting();
		editor.getGraph().refreshNet();
		editor.getGraph().repaint();
	}

}
