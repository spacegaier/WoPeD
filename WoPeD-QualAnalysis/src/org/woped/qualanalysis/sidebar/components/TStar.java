package org.woped.qualanalysis.sidebar.components;

import java.awt.Color;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.jgraph.graph.AttributeMap;
import org.jgraph.graph.DefaultGraphCell;
import org.jgraph.graph.DefaultPort;
import org.jgraph.graph.GraphConstants;
import org.woped.core.controller.IEditor;
import org.woped.core.model.AbstractElementModel;
import org.woped.core.model.ArcModel;
import org.woped.core.model.CreationMap;
import org.woped.core.model.IntPair;
import org.woped.core.model.ModelElementFactory;
import org.woped.core.model.PetriNetModelProcessor;
import org.woped.core.model.petrinet.AbstractPetriNetModelElement;
import org.woped.core.model.petrinet.GroupModel;
import org.woped.core.model.petrinet.NameModel;
import org.woped.core.model.petrinet.OperatorTransitionModel;
import org.woped.core.model.petrinet.PetriNetModelElement;
import org.woped.core.model.petrinet.TransitionModel;
import org.woped.core.model.petrinet.TransitionResourceModel;
import org.woped.core.model.petrinet.TriggerModel;
import org.woped.core.utilities.Utils;

public class TStar {

    private static final Color T_STAR_COLOR = new Color(200, 200, 200);

    private IEditor editor = null;

    private Object[] tStarComponents = new Object[3];

    private TransitionModel tStar = null;

    private static final String T_STAR = "t*";

    public TStar(IEditor editor) {
        this.editor = editor;
    }

    public void updateTStar(AbstractElementModel source, AbstractElementModel sink) {
        if (source == null && sink == null) {
            if (tStar != null) {
                tStar.removeAllChildren();
                deleteCells(tStarComponents);
                tStar = null;
                editor.setTStarEnabled(false);
                editor.getGraph().setEnabled(true);
            }
        } else {
            if (tStar != null) {
                tStar.removeAllChildren();
                deleteCells(tStarComponents);
                tStar = null;
            }
            editor.setTStarEnabled(true);
            int smallestX = 0;
            int biggestX = 0;
            int smallestY = 0;
            int biggestY = 0;
            Object[] cells = editor.getGraph().getGraphLayoutCache().getCells(
                    editor.getGraph().getGraphLayoutCache().getAllViews());
            for (Object cell : cells) {
                if (cell instanceof AbstractElementModel) {
                    Point p = ((AbstractElementModel) cell).getPosition();
                    biggestX = Math.max(p.x, biggestX);
                    biggestY = Math.max(p.y, biggestY);
                    if (smallestX != 0) {
                        smallestX = Math.min(p.x, smallestX);
                    } else {
                        smallestX = p.x;
                    }
                    if (smallestY != 0) {
                        smallestY = Math.min(p.y, smallestY);
                    } else {
                        smallestY = p.y;
                    }
                }
            }
            CreationMap map = new CreationMap();
            map.setId(T_STAR);
            map.setName(T_STAR);
            map.setType(AbstractPetriNetModelElement.TRANS_SIMPLE_TYPE);
            tStar = new TransitionModel(map);
            GraphConstants.setBorderColor(tStar.getAttributes(), T_STAR_COLOR);
            GraphConstants.setForeground(tStar.getNameModel().getAttributes(), T_STAR_COLOR);
            tStar.addPort();
            tStar.addPort();

            Point point;
            int x = 100;
            Point sourceArcPoint = null;
            Point sinkArcPoint = null;
            if (editor.isRotateSelected()) {
                point = new Point(biggestX + x, ((biggestY - smallestY) / 2) + tStar.getHeight());
                tStar.getNameModel().setPosition((biggestX + x + tStar.getWidth()),
                        ((biggestY - smallestY) / 2) + tStar.getHeight());
                sourceArcPoint = new Point(point.x + tStar.getWidth() / 2, source.getPosition().y + source.getHeight()
                        / 2);
                sinkArcPoint = new Point(point.x + tStar.getWidth() / 2, sink.getPosition().y + sink.getHeight() / 2);
            } else {
                point = new Point(((biggestX - smallestX) / 2) + tStar.getWidth(), biggestY + x);
                tStar.getNameModel().setPosition(((biggestX - smallestX) / 2) + tStar.getWidth(),
                        (biggestY + x + tStar.getHeight()));
                sourceArcPoint = new Point(source.getPosition().x + source.getWidth() / 2, point.y + tStar.getHeight()
                        / 2);
                sinkArcPoint = new Point(sink.getPosition().x + sink.getWidth() / 2, point.y + tStar.getHeight() / 2);
            }
            tStar.setPosition(point);
            GroupModel group = editor.getGraph().groupName(tStar, (tStar).getNameModel());
            group.setUngroupable(false);
            group.add(tStar);
            group.add(tStar.getNameModel());
            tStarComponents[0] = group;
            editor.getGraph().getGraphLayoutCache().insert(group);

            CreationMap sinkTStarMap = CreationMap.createMap();
            sinkTStarMap.setArcSourceId(sink.getId());
            sinkTStarMap.setArcTargetId(tStar.getId());
            sinkTStarMap.setEditOnCreation(false);
            List<Point> sinkTStarPointList = new ArrayList<Point>();
            sinkTStarPointList.add(sinkArcPoint);
            sinkTStarMap.setArcPoints(sinkTStarPointList);
            ArcModel sinkTStarArc = createArc(sinkTStarMap, true);
            tStarComponents[1] = sinkTStarArc;

            CreationMap tStarSourceMap = CreationMap.createMap();
            tStarSourceMap.setArcSourceId(tStar.getId());
            tStarSourceMap.setArcTargetId(source.getId());
            tStarSourceMap.setEditOnCreation(false);
            List<Point> tStarSourcePointList = new ArrayList<Point>();
            tStarSourcePointList.add(sourceArcPoint);
            tStarSourceMap.setArcPoints(tStarSourcePointList);
            ArcModel tStarSourceArc = createArc(tStarSourceMap, true);
            tStarComponents[2] = tStarSourceArc;
            editor.getGraph().setEnabled(false);
        }
    }

    @SuppressWarnings("unchecked")
    private void deleteCells(Object[] toDelete) {
        Vector<Object> result = new Vector<Object>();
        for (int i = 0; i < toDelete.length; i++) {
            if (toDelete[i] instanceof GroupModel && !((GroupModel) toDelete[i]).isUngroupable()) {
                GroupModel tempGroup = (GroupModel) toDelete[i];
                Object cell = tempGroup;
                while (cell instanceof GroupModel) {
                    cell = ((GroupModel) cell).getMainElement();
                }
                if (cell instanceof AbstractElementModel && !((AbstractElementModel) cell).isReadOnly()) {
                    result.add(tempGroup);
                    for (int j = 0; j < tempGroup.getChildCount(); j++) {
                        result.add(tempGroup.getChildAt(j));
                    }
                }
            } else {
                result.add(toDelete[i]);
            }
        }
        HashSet<Object> uniqueResult = new HashSet<Object>();
        for (int i = 0; i < result.size(); i++) {
            uniqueResult.add(result.get(i));
            if (result.get(i) instanceof AbstractElementModel
                    && ((AbstractElementModel) result.get(i)).getPort() != null) {
                Iterator edges = ((AbstractElementModel) result.get(i)).getPort().edges();
                while (edges.hasNext()) {
                    uniqueResult.add(edges.next());
                }
            }
        }
        deleteOnlyCells(uniqueResult.toArray());
    }

    public void deleteOnlyCells(Object[] toDelete) {
        toDelete = Utils.sortArcsFirst(toDelete);
        Vector<Object> allPorts = new Vector<Object>();
        Vector<Object> allCells = new Vector<Object>();
        for (int i = 0; i < toDelete.length; i++) {
            if (toDelete[i] instanceof ArcModel) {
                allPorts.add(toDelete[i]);
                editor.getModelProcessor().removeArc(((ArcModel) toDelete[i]).getId());
            } else
                if (toDelete[i] instanceof TriggerModel) {
                    TransitionModel owner = (TransitionModel) editor.getModelProcessor().getElementContainer()
                            .getElementById(((TriggerModel) toDelete[i]).getOwnerId());
                    if (owner != null) {
                        if (owner.getToolSpecific().getTrigger().getTriggertype() == TriggerModel.TRIGGER_RESOURCE
                                && owner.getToolSpecific().getTransResource() != null) {
                            owner.getToolSpecific().removeTransResource();
                        }
                        owner.getToolSpecific().removeTrigger();
                    }
                    allPorts.add(toDelete[i]);
                } else
                    if (toDelete[i] instanceof TransitionResourceModel) {
                        TransitionModel owner = (TransitionModel) editor.getModelProcessor().getElementContainer()
                                .getElementById(((TransitionResourceModel) toDelete[i]).getOwnerId());
                        if (owner != null) {
                            owner.getToolSpecific().removeTransResource();
                        }
                        allPorts.add(toDelete[i]);
                    } else
                        if (toDelete[i] instanceof NameModel) {
                            allPorts.add(toDelete[i]);
                        } else
                            if (toDelete[i] instanceof GroupModel) {
                                allPorts.add(toDelete[i]);
                            } else
                                if (toDelete[i] instanceof AbstractElementModel) {
                                    AbstractElementModel element = (AbstractElementModel) toDelete[i];
                                    // if there are trigger, delete their jgraph model
                                    if (toDelete[i] instanceof TransitionModel) {
                                        if (((TransitionModel) toDelete[i]).getToolSpecific().getTrigger() != null) {
                                            DefaultGraphCell cell = ((TransitionModel) editor.getModelProcessor()
                                                    .getElementContainer().getElementById(element.getId()))
                                                    .getToolSpecific().getTrigger();
                                            if (cell != null) {
                                                deleteCells(new Object[] { cell });
                                            }
                                        }
                                    }
                                    allPorts.add(element.getPort());
                                    allPorts.add(toDelete[i]);
                                    editor.getModelProcessor().getElementContainer().removeOnlyElement(element.getId());

                                }
        }
        Vector<Object> allDeletedObjects = new Vector<Object>();
        allDeletedObjects.addAll(allPorts);
        allDeletedObjects.addAll(allCells);
        editor.getGraph().getModel().remove(allDeletedObjects.toArray());
        editor.updateNet();
    }

    @SuppressWarnings("unchecked")
    public ArcModel createArc(CreationMap map, boolean insertIntoCache) {
        ArcModel arc = null;
        String sourceId = map.getArcSourceId();
        String targetId = map.getArcTargetId();
        List points = map.getArcPoints();
        Point2D[] pointArray = new Point2D[points.size()];
        for (int i = 0; i < points.size(); i++) {
            if (points.get(i) instanceof Point2D) {
                pointArray[i] = (Point2D) points.get(i);
            } else
                if (points.get(i) instanceof IntPair) {
                    pointArray[i] = new Point2D.Double(((IntPair) points.get(i)).getX1(), ((IntPair) points.get(i))
                            .getX2());
                }
        }
        AbstractElementModel source = editor.getModelProcessor().getElementContainer().getElementById(sourceId);
        source = source != null ? source : tStar;
        AbstractElementModel target = editor.getModelProcessor().getElementContainer().getElementById(targetId);
        target = target != null ? target : tStar;
        if (editor.getGraph().isValidConnection(source, target)) {
            if (!editor.getModelProcessor().getElementContainer().hasReference(sourceId, targetId)) {

                String id = map.getArcId();
                // if the id isn't set or the id set already belongs to an
                // existing arc then fetch a new one
                if (id == null | editor.getModelProcessor().getElementContainer().getArcById(id) != null) {
                    id = editor.getModelProcessor().getNexArcId();
                }
                // if aalst source or target -> update Model
                arc = ModelElementFactory.createArcModel(id, (DefaultPort) source.getChildAt(0), (DefaultPort) target
                        .getChildAt(0));
                arc.setPoints(pointArray);
                GraphConstants.setSelectable(arc.getAttributes(), false);
                GraphConstants.setLineColor(arc.getAttributes(), T_STAR_COLOR);

                OperatorTransitionModel operatorModel;
                if (source.getType() == PetriNetModelElement.TRANS_OPERATOR_TYPE) {
                    operatorModel = (OperatorTransitionModel) source;
                    operatorModel.addElement(target);
                    operatorModel.registerOutgoingConnection((PetriNetModelProcessor) editor.getModelProcessor(),
                            target);
                } else
                    if (target.getType() == PetriNetModelElement.TRANS_OPERATOR_TYPE) {
                        operatorModel = (OperatorTransitionModel) target;
                        operatorModel.addElement(source);
                        operatorModel.registerIncomingConnection((PetriNetModelProcessor) editor.getModelProcessor(),
                                source);

                    }
                editor.getModelProcessor().getElementContainer().addReference(arc);

                editor.getGraph().connect(arc, insertIntoCache);
                for (int i = 0; i < pointArray.length; ++i) {
                    arc.addPoint(pointArray[i]);
                    Map<ArcModel, AttributeMap> arcMap = new HashMap<ArcModel, AttributeMap>();
                    arcMap.put(arc, arc.getAttributes());
                    editor.getGraph().getModel().edit(arcMap, null, null, null);
                }
                arc.setProbability(map.getArcProbability());
                arc.setDisplayOn(map.getDisplayArcProbability());
                if (map.getArcLabelPosition() != null) {
                    arc.setLabelPosition(new Point2D.Double(map.getArcLabelPosition().getX(), map.getArcLabelPosition()
                            .getY()));
                }
            }
        }
        return arc;
    }
}
