package org.woped.qualanalysis.soundness;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.woped.core.controller.IEditor;
import org.woped.core.model.AbstractElementModel;
import org.woped.qualanalysis.service.interfaces.IWorkflowCheck;
import org.woped.qualanalysis.soundness.algorithms.AlgorithmFactory;
import org.woped.qualanalysis.soundness.algorithms.basedonlowlevelpetrinet.sourcesink.ISourceSinkTest;
import org.woped.qualanalysis.soundness.builder.BuilderFactory;
import org.woped.qualanalysis.soundness.datamodel.AbstractNode;
import org.woped.qualanalysis.soundness.datamodel.LowLevelPetriNet;
import org.woped.qualanalysis.soundness.datamodel.PlaceNode;
import org.woped.qualanalysis.soundness.datamodel.TransitionNode;
import org.woped.qualanalysis.structure.StructuralAnalysis;

/**
 * 
 * 
 * @author Patrick Spies, Patrick Kirchgaessner, Joern Liebau, Enrico Moeller, Sebastian Fuss
 * 
 */
public class WorkflowCheckImplement implements IWorkflowCheck {

    private IEditor editor;
    private ISourceSinkTest sourceSinkTest;
    private LowLevelPetriNet lolNetWithTStar;
    private LowLevelPetriNet lolNetWithoutTStar;

    public WorkflowCheckImplement(IEditor editor) {
        this.editor = editor;
        lolNetWithoutTStar = BuilderFactory.createLowLevelPetriNetWithoutTStarBuilder(editor).getLowLevelPetriNet();
        lolNetWithTStar = BuilderFactory.createLowLevelPetriNetWithTStarBuilder(editor).getLowLevelPetriNet();
        sourceSinkTest = AlgorithmFactory.createSourceSinkTest(lolNetWithoutTStar);

    }

    @Override
    public Iterator<AbstractElementModel> getSourcePlacesIterator() {
        Set<AbstractElementModel> sourcePlaces = new HashSet<AbstractElementModel>();
        for (PlaceNode place : sourceSinkTest.getSourcePlaces()) {
            sourcePlaces.add(getAEM(place));
        }
        return sourcePlaces.iterator();
    }

    @Override
    public Iterator<AbstractElementModel> getSinkPlacesIterator() {
        Set<AbstractElementModel> sinkPlaces = new HashSet<AbstractElementModel>();
        for (PlaceNode place : sourceSinkTest.getSinkPlaces()) {
            sinkPlaces.add(getAEM(place));
        }
        return sinkPlaces.iterator();
    }

    @Override
    public Iterator<AbstractElementModel> getSourceTransitionsIterator() {
        Set<AbstractElementModel> sourceTransitions = new HashSet<AbstractElementModel>();
        for (TransitionNode transition : sourceSinkTest.getSourceTransitions()) {
            sourceTransitions.add(getAEM(transition));
        }
        return sourceTransitions.iterator();
    }

    @Override
    public Iterator<AbstractElementModel> getSinkTransitionsIterator() {
        Set<AbstractElementModel> sinkTransitions = new HashSet<AbstractElementModel>();
        for (TransitionNode transition : sourceSinkTest.getSinkTransitions()) {
            sinkTransitions.add(getAEM(transition));
        }
        return sinkTransitions.iterator();
    }

    @Override
    public Iterator<AbstractElementModel> getNotConnectedNodes() {
        Set<Set<AbstractNode>> ccs = AlgorithmFactory.createCcTest(lolNetWithoutTStar).getConnectedComponents();

        if (ccs.size() > 1) {
            return editor.getModelProcessor().getElementContainer().getRootElements().iterator();
        }

        return new HashSet<AbstractElementModel>().iterator();
    }

    @Override
    public Iterator<AbstractElementModel> getNotStronglyConnectedNodes() {
        // delegate
        return new StructuralAnalysis(editor).getNotStronglyConnectedNodes();
    }

    @Override
    public Iterator<Set<AbstractElementModel>> getStronglyConnectedComponents() {
        Set<Set<AbstractElementModel>> sccs = new HashSet<Set<AbstractElementModel>>();
        Set<AbstractElementModel> scc;
        for (Set<AbstractNode> set : AlgorithmFactory.createSccTest(lolNetWithTStar).getStronglyConnectedComponents()) {
            if (set.size() > 0) {
                scc = new HashSet<AbstractElementModel>();

                for (AbstractNode node : set) {
                    scc.add(getAEM(node));
                }
                // remove t*
                scc.remove(null);
                if(scc.size() > 0){
                	sccs.add(scc);
                }
            }
        }

        return sccs.iterator();
    }

    @Override
    public Iterator<Set<AbstractElementModel>> getConnectedComponents() {
        Set<Set<AbstractElementModel>> ccs = new HashSet<Set<AbstractElementModel>>();
        Set<AbstractElementModel> cc;
        for (Set<AbstractNode> set : AlgorithmFactory.createCcTest(lolNetWithoutTStar).getConnectedComponents()) {
            if (set.size() > 0) {
                cc = new HashSet<AbstractElementModel>();

                for (AbstractNode node : set) {
                    cc.add(getAEM(node));
                }
                // remove t*
                cc.remove(null);
                if(cc.size() > 0) {
                	ccs.add(cc);
                }
            }
        }

        return ccs.iterator();
    }

    /**
     * 
     * @param node the AbstractNode to get the referring AbstractElementModel from
     * @return the referred AbstractElementModel
     */
    private AbstractElementModel getAEM(AbstractNode node) {
        return editor.getModelProcessor().getElementContainer().getElementById(node.getOriginId());
    }

}