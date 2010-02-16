package org.woped.qualanalysis.sidebar.assistant.pages;

import org.woped.qualanalysis.sidebar.SideBar;
import org.woped.qualanalysis.sidebar.assistant.components.BeginnerPanel;
import org.woped.translations.Messages;

/**
 * shows freechoice details
 * 
 * @author Lennart Oess, Arthur Vetter, Jens Tessen, Heiko Herzog
 * 
 */
@SuppressWarnings("serial")
public class FreechoicePage extends BeginnerPanel {

	private int numFreeChoiceViolations;

	public FreechoicePage(BeginnerPanel previous, SideBar sideBar) {
		super(previous, sideBar, Messages
				.getString("AnalysisSideBar.Beginner.FreeChoiceAnalysis"));

		numFreeChoiceViolations = qualanalysisService
				.getNumFreeChoiceViolations();

		if (numFreeChoiceViolations != 0) {
			status = false;
		}
	}

	@Override
	public void addComponents() {
		createEntry("Analysis.Tree.NumFreechoiceViolations",
				qualanalysisService.getFreeChoiceViolationsIterator(),
				numFreeChoiceViolations,
				"AnalysisSideBar.Beginner.Help.FreeChoiceViolations");
		createEmptyEntry();
	}
}