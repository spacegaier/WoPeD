/*
 *
 * Copyright (C) 2004-2005, see @author in JavaDoc for the author
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *
 * For contact information please visit http://woped.dhbw-karlsruhe.de
 *
 */
package org.woped.core.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jgraph.graph.DefaultPort;
import org.oasisOpen.docs.wsbpel.x20.process.executable.TPartnerLinks;
import org.oasisOpen.docs.wsbpel.x20.process.executable.TVariable;
import org.oasisOpen.docs.wsbpel.x20.process.executable.TVariables;
import org.woped.core.Constants;
import org.woped.core.model.bpel.BpelVariable;
import org.woped.core.model.bpel.BpelVariableList;
import org.woped.core.model.bpel.PartnerlinkList;
import org.woped.core.model.bpel.UddiVariable;
import org.woped.core.model.bpel.UddiVariableList;
import org.woped.core.model.petrinet.AbstractPetriNetElementModel;
import org.woped.core.model.petrinet.EditorLayoutInfo;
import org.woped.core.model.petrinet.ParaphrasingModel;
import org.woped.core.utilities.LoggerManager;

/**
 * @author <a href="mailto:slandes@kybeidos.de">Simon Landes </a> <br>
 *         <br>
 *         The <code>ModelElementContainer</code> stores the whole Model of an
 *         Petri-Net. In the frist view it is an <code>HashMap</code>,
 *         containing for each <code>PetriNetModelElement</code> an Entry with
 *         its ID as key. This Entry is again an <code>HashMap</code>
 *         containing the Element itselfs with the ID
 *         <code>ModelElementContainer.SELF_ID</code>. Additionally it
 *         contains all Elements of which this <code>PetriNetModelElement</code>
 *         is Source. The Methods of the <code>ModelElementContainer</code>
 *         offer a secure and comfort handling with the Model. Therefore the
 *         <code>getIdMap()</code> Method should not be used. <br>
 *         <br>
 *         Created on 29.04.2003
 */

@SuppressWarnings("serial")
public class ModelElementContainer implements Serializable {
	// ! If !=null, stores editor layout info for the editor
	// ! that is to be used to edit the sub-process
	private EditorLayoutInfo editorLayoutInfo = null;

	public EditorLayoutInfo getEditorLayoutInfo() {
		return editorLayoutInfo;
	}

	public void setEditorLayoutInfo(EditorLayoutInfo editorLayoutInfo) {
		this.editorLayoutInfo = editorLayoutInfo;
	}

	// ! Just as we own elements, elements own us
	// ! if we're a simple transition element container
	// ! Again, it is important for navigation to know these things
	// ! The owningElement member may be null (which is in fact the default)
	// ! if we're not owned by an AbstractElementModel instance at all
	private AbstractPetriNetElementModel owningElement = null;

	private BpelVariableList variablesList = new BpelVariableList();
	private PartnerlinkList partnerLinkList = new PartnerlinkList();
	private UddiVariableList uddiVariableList = new UddiVariableList();
	private ParaphrasingModel paraphrasingModel = new ParaphrasingModel();
	
	public void setOwningElement(AbstractPetriNetElementModel element) {
		owningElement = element;
	}

	public AbstractPetriNetElementModel getOwningElement() {
		return owningElement;
	}

	private Map<String, Map<String, Object>> idMap = null;
	private Map<String, ArcModel> arcs = null;
	public static final String SELF_ID = "_#_";

	/**
	 * Constructor for ModelElementContainer.
	 */
	public ModelElementContainer() {
		idMap = new HashMap<String, Map<String, Object>>();
		arcs = new HashMap<String, ArcModel>();
	}

	/**
	 * Returns the idMapper. This is the Main Hashmap containing the whole
	 * Petri-Net. Is mainly used by the Class itselfs. Should not be necessary
	 * to use this Method outside of the Container!
	 *
	 * @return Map
	 */
	public Map<String, Map<String, Object>> getIdMap() {
		
		return idMap;
	}

	/**
	 * Method addElement. Adds an <code>PetriNetModelElement</code> to the
	 * Container.
	 *
	 * @param theElement
	 * @throws ElementException
	 */
	public AbstractPetriNetElementModel addElement(AbstractPetriNetElementModel theElement) {
		if (getIdMap().get(theElement.getId()) == null) {
			// if referenceMap does not exits, create it
			Map<String, Object> referenceMap = new HashMap<String, Object>();
			// =>frist time adding element, first add Element itself with
			// SELF_ID to the referenceMap
			referenceMap.put(SELF_ID, theElement);
			// ... and to the idMap
			getIdMap().put(theElement.getId(), referenceMap);

			// Tell the element that it is now owned
			theElement.addOwningContainer(this);

			LoggerManager.debug(Constants.CORE_LOGGER, "Element: "
					+ theElement.getId() + " added");
		} else {
			LoggerManager.debug(Constants.CORE_LOGGER,
					"The Element already exists, did nothing!");
		}
		return theElement;
	}

	/**
	 * Method addReference. Adds an Reference from the
	 * <code>PetriNetModelElement</code> with id <code>sourceId</code> to
	 * the Element with id <code>targetId</code>.
	 *
	 * @param sourceId
	 * @param targetId
	 */
	public void addReference(ArcModel arc) {

		if (getElementById(arc.getSourceId()) == null) {
			// if referenceMap is not setup, then the Element itself was not set
			LoggerManager.warn(Constants.CORE_LOGGER, "Source (ID:"
					+ arc.getSourceId() + ") does not exist");
		} else if (getElementById(arc.getTargetId()) == null) {
			LoggerManager.warn(Constants.CORE_LOGGER, "Target (ID:"
					+ arc.getTargetId() + ") does not exist");
		} else if (containsArc(arc)) {
			LoggerManager.debug(Constants.CORE_LOGGER, "Arc already exists!");
		} else {
			getIdMap().get(arc.getSourceId()).put(arc.getId(), arc);
			arcs.put(arc.getId(), arc);
			LoggerManager.debug(Constants.CORE_LOGGER, "Reference: "
					+ arc.getId() + " (" + arc.getSourceId() + " -> "
					+ arc.getTargetId() + ") added.");
		}

	}

	/**
	 * Check whether a reference from sourceID to targetID exists. Note that
	 * this will check for top-level references, not low-level components of van
	 * der Aalst operators. This means that only actual, visible arcs as present
	 * in the graphical Petri-Net representation will be found
	 *
	 * @param sourceId
	 * @param targetId
	 * @return
	 */
	public boolean hasReference(Object sourceId, Object targetId) {
		return (findArc(sourceId.toString(), targetId.toString()) != null);
	}

	/**
	 * Method removeElement. Removes a <code>PetriNetModelElement</code>
	 * including all its References.
	 *
	 * @param id
	 */
	public void removeElement(Object id) {

		// AT FIRST delete element's connections
		removeArcsFromElement(id);
		// AND THEN remove the element, and all its target information
		removeOnlyElement(id);

	}

	public void removeOnlyElement(Object id) {
		AbstractPetriNetElementModel element = getElementById(id);
		// The element is no longer owned by anybody
		if (element!=null) element.removeOwningContainer(this);
		getIdMap().remove(id);
		LoggerManager.debug(Constants.CORE_LOGGER, "Element (ID:" + id
				+ ") deleted.");
	}

	public void removeTargetArcsFromElement(Object id) {
		// remove all Target Arcs
		Iterator<String> arcsToRemove2 = getOutgoingArcs(id).keySet().iterator();
		// arcsToRemove2.next();
		while (arcsToRemove2.hasNext()) {
			removeArc(arcsToRemove2.next());
		}
	}

	public void removeSourceArcsFromElement(Object id) {
		// remove all Source Arcs
		Iterator<String> arcsToRemove = getIncomingArcs(id).keySet().iterator();
		while (arcsToRemove.hasNext()) {
			removeArc(arcsToRemove.next());
		}
	}

	/**
	 * Method removeRefElements. Removes only all Arcs from a
	 * <code>PetriNetModelElement</code>, not the Element itselfs.
	 *
	 * @param id
	 */
	public void removeArcsFromElement(Object id) {
		removeSourceArcsFromElement(id);
		removeTargetArcsFromElement(id);

		LoggerManager.debug(Constants.CORE_LOGGER,
				"All References from/to (ID:" + id + ") deleted");
	}

	public void removeArc(Object id) {
		if (getArcById(id) != null) {
			// remove the Arc-Model
			removeArc(getArcById(id));
		} else
			LoggerManager.warn(Constants.CORE_LOGGER, "Arc with ID: " + id
					+ " does not exists");
	}

	public void removeArc(ArcModel arc) {
		if (arc != null) {
			LoggerManager.debug(Constants.CORE_LOGGER, "Reference (ID:"
					+ arc.getId() + ") deleted");
			// remove in arc Map
			arcs.remove(arc.getId());
			// remove Target Entry, (in Source Element's reference Map)
			((Map<String, Object>) getIdMap().get(arc.getSourceId())).remove(arc.getId());
		}

	}

	public void removeAllSourceElements(Object targetId) {
		Iterator<String> transIter = getSourceElements(targetId).keySet().iterator();
		while (transIter.hasNext()) {
			removeElement(transIter.next());
		}
	}

	public void removeAllTargetElements(Object sourceId) {
		Iterator<String> transIter = getTargetElements(sourceId).keySet().iterator();
		while (transIter.hasNext()) {
			removeElement(transIter.next());
		}
	}

	/**
	 * Method getReferenceElements. Returns the all
	 * <code>AbstractElementModel</code>, of which an Element with a special
	 * id is source.
	 *
	 * @param id
	 * @return Map
	 */
	public Map<String, AbstractPetriNetElementModel> getTargetElements(Object id) {

		if ((Map<String, Object>) getIdMap().get(id) != null) {

			Iterator<String> refIter = ((Map<String, Object>) getIdMap().get(id)).keySet().iterator();
			Map<String, AbstractPetriNetElementModel> targetMap = new HashMap<String, AbstractPetriNetElementModel>();
			while (refIter.hasNext()) {
				Object arc = ((Map<String, Object>) getIdMap().get(id)).get(refIter.next());
				if (arc instanceof ArcModel) {
					AbstractPetriNetElementModel aCell = (AbstractPetriNetElementModel) ((DefaultPort) ((ArcModel) arc)
							.getTarget()).getParent();
					targetMap.put(aCell.getId(), aCell);
				}
			}
			return targetMap;
		} else {
			return null;
		}
	}

	public Map<String, Object> getOutgoingArcs(Object id) {

		if ((Map<String, Object>) getIdMap().get(id) != null) {
			Map<String, Object> arcOut = new HashMap<String, Object>(getIdMap().get(id));
			arcOut.remove("_#_");
			return arcOut;
		} else
			return new HashMap<String, Object>();
	}

	public Map<String, ArcModel> getIncomingArcs(Object id) {
		return findSourceArcs(id);
	}

	/**
	 * Method getSourceElements. Returns the all
	 * <code>PetriNetModelElement</code>, of which an Element with a special
	 * id is target.
	 *
	 * @param id
	 * @return Map
	 */
	public Map<String, AbstractPetriNetElementModel> getSourceElements(Object targetId) {

		return findSourceElements(targetId);

	}

	/**
	 * Method getRootElements. Returns a <code>List</code> containing all
	 * <code>PetriNetModelElement</code> without any Reference information.
	 *
	 * @return List
	 */
	public List<AbstractPetriNetElementModel> getRootElements() {

		List<AbstractPetriNetElementModel> rootElements = new ArrayList<AbstractPetriNetElementModel>();
		Iterator<String> allIter = getIdMap().keySet().iterator();
		while (allIter.hasNext()) {
			AbstractPetriNetElementModel element = getElementById(allIter.next());
			rootElements.add(element);
		}

		return rootElements;

	}

	/**
	 * Method findSourceElements. Returns a Map with the Elements that contains
	 * a reference to the Object with a special id.
	 *
	 * @param id
	 * @return List
	 */
	protected Map<String, AbstractPetriNetElementModel> findSourceElements(
			Object targetId) {

		Map<String, AbstractPetriNetElementModel> sourceMap = new HashMap<String, AbstractPetriNetElementModel>();
		Iterator<String> sourceArcIter = findSourceArcs(targetId).keySet().iterator();
		ArcModel tempArc;
		while (sourceArcIter.hasNext()) {
			tempArc = (ArcModel) arcs.get(sourceArcIter.next());
			sourceMap.put(tempArc.getSourceId(), getElementById(tempArc
					.getSourceId()));
		}
		return sourceMap;
	}

	protected Map<String, ArcModel> findSourceArcs(Object id) {

		Iterator<String> arcIter = arcs.keySet().iterator();
		Map<String, ArcModel> sourceArcs = new HashMap<String, ArcModel>();
		ArcModel tempArc;
		while (arcIter.hasNext()) {
			tempArc = (ArcModel) arcs.get(arcIter.next());
			if (tempArc.getTargetId() != null) {
				if (tempArc.getTargetId().equals(id)) {
					sourceArcs.put(tempArc.getId(), tempArc);
				}
			}
		}
		return sourceArcs;

	}

	/**
	 * Method getElementById. Returns the a ModelElement with a special id
	 * itselfs
	 *
	 * @param id
	 * @return ModelElement
	 */
	public AbstractPetriNetElementModel getElementById(Object id) {

		if (getIdMap().get(id) != null) {
			return (AbstractPetriNetElementModel) ((Map<String, Object>) getIdMap().get(id))
					.get(ModelElementContainer.SELF_ID);
			
			
		} else {
			LoggerManager.debug(Constants.CORE_LOGGER, "Requested Element (ID:"
					+ id + ") does not exists");
			return null;
		}

	}
	
	public void removeAllHighlighting(){
		Map<String, Map<String, Object>> map = getIdMap();
		for(String id:map.keySet())
			((AbstractPetriNetElementModel) map.get(id).get(ModelElementContainer.SELF_ID)).setHighlighted(false);
		for(String arc:arcs.keySet())
			((ArcModel)arcs.get(arc)).setHighlighted(false);
	}

	public ArcModel getArcById(Object id) {

		if (arcs.get(id) != null) {
			return (ArcModel) arcs.get(id);
		} else {
			LoggerManager.debug(Constants.CORE_LOGGER, " Requested Arc (ID:"
					+ id + ") does not exists");
			return null;
		}
	}


	public boolean containsArc(ArcModel arc) {

		Iterator<String> arcIter = getSourceElements(arc.getTargetId()).keySet()
				.iterator();
		while (arcIter.hasNext()) {
			if (arcIter.next().equals(arc.getSourceId())) {
				return true;
			}
		}
		return false;
	}

	public boolean containsElement(Object id) {
		return getIdMap().containsKey(id);
	}

	/**
	 * Returns the arcs.
	 *
	 * @return Map
	 */
	public Map<String, ArcModel> getArcMap() {
		return arcs;
	}

	/**
	 * Sets the arcs.
	 *
	 * @param arcs
	 *            The arcs to set
	 */
	public void setArcMap(Map<String, ArcModel> arcs) {
		this.arcs = arcs;
	}

	public Map<String, AbstractPetriNetElementModel> getElementsByType(int type) {
		Map<String, AbstractPetriNetElementModel> elements = new HashMap<String, AbstractPetriNetElementModel>();
		Iterator<String> elementsIter = getIdMap().keySet().iterator();
		AbstractPetriNetElementModel element;
		// try {
		while (elementsIter.hasNext()) {
			element = getElementById(elementsIter.next());
			if (element != null && element.getType() == type) {
				elements.put(element.getId(), element);
			}
		}
		// } catch (Exception e) {
		// e.printStackTrace();
		// }
		return elements;
	}

	public ArcModel findArc(String sourceId, String targetId) {
		Iterator<String> iter = arcs.keySet().iterator();
		while (iter.hasNext()) {
			ArcModel arc = (ArcModel) arcs.get(iter.next());
			if (arc.getSourceId().equals(sourceId)
					&& arc.getTargetId().equals(targetId)) {
				return arc;
			}
		}
		return null;
	}

	/* Bpel extension */


	/**
	 * @return
	 */
	public BpelVariableList getVariableList()
	{
		return this.variablesList;
	}

	/**
	 *
	 * @return
	 */
	public PartnerlinkList getPartnerlinkList()
	{
		return this.partnerLinkList;
	}

	/**
	 * @return
	 */
	public TPartnerLinks getTPartnerLinkList() {
		return this.partnerLinkList.getBpelCode();
	}

	/**
	 * Returns a list of TVariables. This method is used by the bpel-generator
	 * to produce BPEL-Code.
	 *
	 * @return
	 */
	public TVariables getTVariablesList() {
		return this.variablesList.getBpelCode();
	}

	/**
	 * Returns a list of PartnerLinkTypes. This method is used by the
	 * BPEL-generator to pruduce the global datamodel of partnerlinks
	 *
	 * @return
	 */
	public String[] getPartnerLinkList() {
		return this.partnerLinkList.getPartnerlinkNameArray();
	}

	/**
	 * Insert a partnerlink to a consisting list of partnerlinks
	 *
	 * @param name
	 * @param namespace
	 * @param partnerLinkType
	 * @param partnerRole
	 * @param WsdlUrl
	 */
	public void setPartnerLink(String name, String namespace,
			String partnerLinkType, String partnerRole, String WsdlUrl) {
		this.partnerLinkList.setPartnerLink(name, namespace, partnerLinkType,
				partnerRole, WsdlUrl);
	}

	/**
	 * Insert a partnerlink to a consisting list of partnerlinks Attention:
	 * Parameters: name, namespace, partnerLinkType, partnerRole, myRole
	 *
	 * @param name
	 * @param namespace
	 * @param partnerLinkType
	 * @param partnerRole
	 * @param myRole
	 * @param WsdlUrl
	 */
	public void addPartnerLink(String name, String namespace,
			String partnerLinkType, String partnerRole, String myRole,
			String WsdlUrl) {
		this.partnerLinkList.addPartnerLink(name, namespace, partnerLinkType,
				partnerRole, myRole, WsdlUrl);
	}

	/**
	 * Insert a partnerlink to a consisting list of partnerlinks Attention:
	 * Parameters: name, namespace, partnerLinkType, myRole (without
	 * partnerRole)
	 *
	 * @param name
	 * @param namespace
	 * @param partnerLinkType
	 * @param myRole
	 * @param WsdlUrl
	 */
	public void addPartnerLinkWithoutPartnerRole(String name, String namespace,
			String partnerLinkType, String myRole, String WsdlUrl) {
		this.partnerLinkList.addPartnerLinkWithoutPartnerRole(name, namespace,
				partnerLinkType, myRole, WsdlUrl);
	}

	/**
	 * @edit by Alexander Ro�wog
	 *
	 * Insert a partnerlink to a consisting list of partnerlinks
	 * Attention: Parameters: name, namespace, partnerLinkType, partnerRole
	 *
	 * @param name
	 * @param namespace
	 * @param partnerLinkType
	 * @param partnerRole
	 * @param WsdlUrl
	 */
	public void addPartnerLinkWithoutMyRole(String name, String namespace,
			String partnerLinktType, String partnerRole, String WsdlUrl) {
		this.partnerLinkList.addPartnerLinkWithoutMyRole(name, namespace,
				partnerLinktType, partnerRole, WsdlUrl);
	}

	public boolean existWsdlUrl(String WsdlUrl){

		String[] urls = this.partnerLinkList.getWsdlUrls();
		if(urls==null)return false;
		for (int i=0;i<urls.length-1;i++){
			if (urls[i].equals(WsdlUrl))return true;
		}

		return false;
	}

	public boolean existPLName(String newname){
		String[] names = this.partnerLinkList.getPartnerlinkNameArray();
		if(names==null)return false;
		for(int i=0;i<names.length-1;i++){
			if(names[i].equals(newname))return true;
		}
		return false;
	}

	/**
	 *
	 * @param arg
	 */
	public void addVariable(TVariable arg) {
	    this.variablesList.addVariable(arg);
	}

	/**
	 *
	 * @param name
	 * @param type
	 */
	public void addVariable(String name, String type) {
		this.variablesList.addVariable(name, type);
	}

	/**
	 *
	 * @param name
	 * @param namespace
	 * @param type
	 */
	public void addWSDLVariable(String name, String namespace, String type) {
		this.variablesList.addWSDLVariable(name, namespace, type);
	}

	/**
	 *
	 * @param Name
	 * @return
	 */
	public BpelVariable findBpelVariableByName(String Name)
	{
		return this.variablesList.findBpelVaraibleByName(Name);
	}

	/**
	 *
	 * @return
	 */
	public String[] getBpelVariableNameList()
	{
		return this.variablesList.getVariableNameArray();
	}

	/**
	 *
	 * @return
	 */
	public HashSet<BpelVariable> getBpelVariableList()
	{
		return this.variablesList.getBpelVariableList();
	}
	
	/**
	 *@Param: Name, URL
	 *
	 */
	
	public void addUddiVariable(String name, String url)
	{
		this.uddiVariableList.addVariable(name,url);
	}
	
	/**
	 *
	 * @return
	 */
	public String[] getUddiVariableNameList()
	{
		return this.uddiVariableList.getVariableNameArray();
	}
	
	/**
	 * @param: Name
	 * @return
	 */

	public UddiVariable findUddiVariableByName(String name)
	{
		return this.uddiVariableList.findUddiVariableByName(name);
	}
	
	public ParaphrasingModel getParaphrasingModel(){
		return this.paraphrasingModel;
	}
}