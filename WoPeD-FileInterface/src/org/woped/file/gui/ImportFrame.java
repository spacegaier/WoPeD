package org.woped.file.gui;

import javax.swing.*;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import javax.swing.table.DefaultTableModel;

import org.apromore.manager.model_portal.EditSessionType;
import org.apromore.manager.model_portal.ExportFormatInputMsgType;

import java.util.StringTokenizer;
import org.apromore.access.*;
import org.woped.translations.Messages;

public class ImportFrame extends JDialog {

	private static final long serialVersionUID = 1L;
	JTextField txtName;
	JTextField txtID;
	JTextField txtDomain;
	JTextField txtOwner;
	JLabel lblPnmlImportFunction;
	JLabel lblImportAPnml;
	JLabel lblFilterBy;
	JLabel lblPnmlName;
	JLabel lblID;
	JLabel lblDomain;
	JLabel lblOwner;
	JLabel lblResults;
	JLabel lblSelectProcess;
	ApromoreAccessObject initAAO;
	ArrayMaker dc;
	String[][] rowData;
	final String[] columnNames = {
			Messages.getString("Apromore.Import.UI.ProcessName"),
			Messages.getString("Apromore.Import.UI.ID"),
			Messages.getString("Apromore.Import.UI.Owner"),
			Messages.getString("Apromore.Import.UI.Domain"),
			Messages.getString("Apromore.Import.UI.Versions") };
	DefaultTableModel tabModel;
	JTable table;
	JScrollPane scrollPane;
	JButton btnFind;
	JComboBox comboBox;
	JButton btnImport;
	JLabel lblSelectVersion;

	public ImportFrame() {
		setModal(true);
		initComponents();
	}

	public ExportFormatInputMsgType getElement() {
		if (table == null)
			return null;
		if (table.getSelectedRow() == -1)
			return null;
		if (comboBox.getSelectedItem() == null)
			return null;

		ExportFormatInputMsgType a = new ExportFormatInputMsgType();
		a.setAnnotationName("");
		a.setFormat("XPDL 2.1");

		a.setOwner(rowData[table.getSelectedRow()][2]);
		a.setProcessId(Integer.valueOf(rowData[table.getSelectedRow()][1]));
		a.setProcessName(rowData[table.getSelectedRow()][0]);
		a.setVersionName(comboBox.getSelectedItem().toString());
		a.setWithAnnotations(true);
		a.setAnnotationName("Initial");
		return a;
	}

	public EditSessionType getEditSession() {
		if (table.getSelectedRow() == -1)
			return null;

		EditSessionType a = new EditSessionType();
		a.setAnnotation("");
		a.setDomain(rowData[table.getSelectedRow()][3]);
		a.setNativeType("PNML");
		a.setProcessId(Integer.valueOf(rowData[table.getSelectedRow()][1]));
		a.setProcessName(rowData[table.getSelectedRow()][0]);
		a.setUsername(rowData[table.getSelectedRow()][2]);
		a.setVersionName(comboBox.getSelectedItem().toString());
		a.setWithAnnotation(false);
		return a;
	}

	public void initComponents() {

		setTitle(Messages.getString("Apromore.Import.UI.Title"));

		Dimension frameSize = new Dimension(800, 600);
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int top = (screenSize.height - frameSize.height) / 2;
		int left = (screenSize.width - frameSize.width) / 2;
		setSize(frameSize);
		setLocation(left, top);
		setResizable(false);

		getContentPane().setLayout(null);

		lblPnmlImportFunction = new JLabel(
				Messages.getString("Apromore.Import.UI.Headline"));
		lblPnmlImportFunction.setFont(new Font("Tahoma", Font.BOLD, 14));
		lblPnmlImportFunction.setBounds(10, 11, 166, 20);
		getContentPane().add(lblPnmlImportFunction);

		lblImportAPnml = new JLabel(
				Messages.getString("Apromore.Import.UI.ImportDescription"));
		lblImportAPnml.setBounds(10, 36, 333, 14);
		getContentPane().add(lblImportAPnml);

		lblFilterBy = new JLabel(
				Messages.getString("Apromore.Import.UI.FilterBy"));
		lblFilterBy.setFont(new Font("Tahoma", Font.BOLD, 11));
		lblFilterBy.setBounds(10, 61, 228, 14);
		getContentPane().add(lblFilterBy);

		lblPnmlName = new JLabel(Messages.getString("Apromore.Import.UI.Name"));
		lblPnmlName.setBounds(10, 90, 86, 14);
		getContentPane().add(lblPnmlName);

		txtName = new JTextField();
		txtName.setBounds(135, 87, 86, 20);
		getContentPane().add(txtName);
		txtName.setColumns(10);

		lblID = new JLabel(Messages.getString("Apromore.Import.UI.ID"));
		lblID.setBounds(10, 125, 86, 14);
		getContentPane().add(lblID);

		txtID = new JTextField();
		txtID.setBounds(135, 122, 86, 20);
		getContentPane().add(txtID);
		txtID.setColumns(10);

		lblDomain = new JLabel(Messages.getString("Apromore.Import.UI.Domain"));
		lblDomain.setBounds(315, 87, 46, 14);
		getContentPane().add(lblDomain);

		txtDomain = new JTextField();
		txtDomain.setBounds(440, 84, 86, 20);
		getContentPane().add(txtDomain);
		txtDomain.setColumns(10);

		lblOwner = new JLabel(Messages.getString("Apromore.Import.UI.Owner"));
		lblOwner.setBounds(315, 122, 46, 14);
		getContentPane().add(lblOwner);

		txtOwner = new JTextField();
		txtOwner.setColumns(10);
		txtOwner.setBounds(440, 119, 86, 20);
		getContentPane().add(txtOwner);

		lblResults = new JLabel(
				Messages.getString("Apromore.Import.UI.Results"));
		lblResults.setFont(new Font("Tahoma", Font.BOLD, 14));
		lblResults.setBounds(10, 202, 166, 20);
		getContentPane().add(lblResults);

		lblSelectProcess = new JLabel(
				Messages.getString("Apromore.Import.UI.SelectProcess"));
		lblSelectProcess.setBounds(10, 227, 444, 14);
		getContentPane().add(lblSelectProcess);

		initAAO = new ApromoreAccessObject();
		if (!initAAO.IsOnline()) {
			dispose();
			return;
		}
		dc = new ArrayMaker();
		rowData = ArrayMaker.run(initAAO.getList());

		tabModel = new DefaultTableModel(rowData, columnNames);

		table = new JTable(tabModel);
		table.setShowVerticalLines(false);

		table.setBounds(10, 261, 588, 169);

		scrollPane = new JScrollPane(table);
		scrollPane.setBounds(10, 252, 764, 194);
		getContentPane().add(scrollPane);

		btnFind = new JButton(Messages.getString("Apromore.Import.UI.Filter"));

		btnFind.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
				try {
					if (txtID.getText().equals(""))
						tabModel.setDataVector(ArrayMaker.run(
								initAAO.getList(), txtName.getText(), 0,
								txtOwner.getText(), txtDomain.getText()),
								columnNames);
					else
						tabModel.setDataVector(ArrayMaker.run(
								initAAO.getList(), txtName.getText(),
								Integer.parseInt(txtID.getText()),
								txtOwner.getText(), txtDomain.getText()),
								columnNames);
				} catch (Exception e) {
					Object[] options = { "OK" };
					JOptionPane.showOptionDialog(
							null,
							Messages.getString("Apromore.Import.UI.Error.IDFieldNotInteger"),
							Messages.getString("Apromore.Import.UI.Error.IDFieldNotIntegerTitle"),

							JOptionPane.DEFAULT_OPTION,
							JOptionPane.WARNING_MESSAGE,

							null, options, options[0]);
				}
				tabModel.fireTableStructureChanged();

			}
		});

		btnFind.setBounds(10, 164, 89, 23);
		getContentPane().add(btnFind);

		lblSelectVersion = new JLabel();
		lblSelectVersion.setForeground(Color.RED);
		lblSelectVersion.setBounds(10, 491, 391, 14);
		lblSelectVersion.setVisible(false);
		getContentPane().add(lblSelectVersion);

		comboBox = new JComboBox();
		comboBox.setBounds(196, 485, 54, 20);
		comboBox.setVisible(false);
		getContentPane().add(comboBox);

		btnImport = new JButton(Messages.getString("Apromore.Import.UI.Import"));
		btnImport.setBounds(10, 516, 89, 23);
		btnImport.setVisible(false);
		getContentPane().add(btnImport);

		final JButton btnSelectRow = new JButton(
				Messages.getString("Apromore.Import.UI.Select"));
		btnSelectRow.setBounds(10, 457, 89, 23);
		getContentPane().add(btnSelectRow);
		btnSelectRow.setVisible(true);
		btnSelectRow.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					lblSelectVersion.setText(Messages
							.getString("Apromore.Import.UI.SelectVersionMsg"));
					lblSelectVersion.setVisible(false);
					comboBox.setVisible(false);

					int row = table.getSelectedRow();

					comboBox.removeAllItems();
					String versionen = "" + table.getModel().getValueAt(row, 4);
					StringTokenizer st = new StringTokenizer(versionen, ";");
					String[] comboItems = new String[st.countTokens()];
					int i = 0;
					while (st.hasMoreTokens()) {
						comboItems[i] = st.nextToken().trim();
						i++;
					}
					for (int j = i - 1; j >= 0; j--) {
						comboBox.addItem(comboItems[j]);
					}
					if (i > 1) {
						lblSelectVersion.setVisible(true);
						comboBox.setVisible(true);
					} else {
						lblSelectVersion.setText(Messages
								.getString("Apromore.Import.UI.ImportThisID")
								+ " "
								+ table.getModel().getValueAt(row, 1)
								+ "?");
						lblSelectVersion.setVisible(true);
					}
					btnImport.setVisible(true);

				} catch (Exception ex) {
					Object[] options = { "OK" };
					JOptionPane.showOptionDialog(
							null,
							Messages.getString("Apromore.Import.UI.Error.NoRowSelected"),
							Messages.getString("Apromore.Import.UI.Error.NoRowSelectedTitle"),

							JOptionPane.DEFAULT_OPTION,
							JOptionPane.WARNING_MESSAGE,

							null, options, options[0]);
				}
			}
		});

		btnImport.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				setVisible(false);
				dispose();
			}
		});

		setVisible(true);

	}

}