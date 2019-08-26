package at.ac.tuwien.shacl.plugin.ui;

import java.awt.BorderLayout;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import org.protege.editor.core.ui.view.View;
import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.model.OWLWorkspace;
import org.protege.editor.owl.model.inference.OWLReasonerManager;
import org.protege.editor.owl.model.inference.ReasonerStatus;
import org.protege.editor.owl.model.selection.OWLSelectionModelListener;

import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.topbraid.shacl.validation.ValidationReport;
import org.topbraid.shacl.validation.ValidationResult;

import at.ac.tuwien.shacl.plugin.events.ErrorNotifier;
import at.ac.tuwien.shacl.plugin.events.ShaclValidationRegistry;
import at.ac.tuwien.shacl.plugin.syntax.JenaOwlConverter;
import at.ac.tuwien.shacl.plugin.util.ShaclValidationResultComparator;

/**
 * Panel for the constraint violations.
 */
public class ShaclConstraintViolationPanel extends JPanel {
    /**
     * 
     */
    private static final long serialVersionUID = 1093799641840761261L;

    private final OWLWorkspace owlWorkspace;
    private final View view;

    private ValidationReport lastReport = null;
    private OWLEntity lastSelection = null;

    /**
     * Table view showing the constraint violations.
     */
    private JTable table;

    // TODO link table selection with events
    /**
     * Defines behavior when object gets notified about a SHACL validation result. Shows the constraint violations of
     * the result Jena model in the table view.
     */
    private final Observer shaclObserver = new Observer() {
        /**
         * Called, when the SHACL validator was executed, and the results were returned.
         *
         * @param o observable notifying the observer
         * @param arg result model fetched from Jena
         */
        @Override
        public void update(Observable o, Object arg) {
            if (arg instanceof ValidationReport) {
                lastReport = (ValidationReport) arg;
                updateTable();
            }
            else {
                // TODO: log internal error
            }
        }
    };

    private final Observer errorObserver = new Observer() {
        @Override
        public void update(Observable o, Object arg) {
            String msg;

            if (arg instanceof String) {
                msg = (String) arg;
            }
            else if (arg == null) {
                msg = "Unexpected error occurred.";
            }
            else {
                msg = "Unexpected error occurred: " + arg.toString();
            }

            JOptionPane.showMessageDialog(owlWorkspace,
                    msg,
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    };

    private final OWLSelectionModelListener selectionObserver = new OWLSelectionModelListener() {
        @Override
        public void selectionChanged() throws Exception {
            lastSelection = owlWorkspace.getOWLSelectionModel().getSelectedEntity();
            updateTable();
        }
    };

    private void updateTable() {
        // clear table
        ((DefaultTableModel) table.getModel()).setRowCount(0);

        if (lastReport == null || lastReport.results().isEmpty()) {
            updateHeaderText(0, 0);
            return;
        }

        List<ValidationResult> validationResults = filterResults(lastReport, lastSelection);

        validationResults.sort(ShaclValidationResultComparator.INSTANCE);

        // update table with result data
        for (ValidationResult res : validationResults) {
            Vector<String> row = toRow(res);

            ((DefaultTableModel) table.getModel()).addRow(row);
        }

        int numAllResults = lastReport.results().size();
        int numDisplayedResults = validationResults.size();

        updateHeaderText(numAllResults, numDisplayedResults);
    }

    private void updateHeaderText(int numAllResults, int numDisplayedResults) {
        if (view == null)
            return;

        String text;

        if (lastReport == null) {
            text = "unknown";
        }
        else if (lastReport.results().isEmpty()) {
            text = "none";
        }
        else {
            if (numAllResults == numDisplayedResults)
                text = Integer.toString(numAllResults);
            else
                text = numDisplayedResults + "/" + numAllResults;
        }

        view.setHeaderText(text);
    }

    private List<ValidationResult> filterResults(ValidationReport report, OWLEntity selection) {
        if (selection == null) {
            return report.results();
        }
        else {
            Stream<ValidationResult> results = report.results().stream();

            if (selection.isOWLNamedIndividual()) {
                OWLNamedIndividual selectedIndividual = selection.asOWLNamedIndividual();
                String selectedIndividualIRI = selectedIndividual.getIRI().toString();

                results = results
                        .filter(row -> row.getFocusNode() != null && row.getFocusNode().isURIResource())
                        .filter(row -> row.getFocusNode().asResource().getURI().equals(selectedIndividualIRI));
            }
            else if (selection.isOWLClass()) {
                OWLClass selectedClass = selection.asOWLClass();

                // don't filter if owl:Thing is selected
                if (!selectedClass.isTopEntity()) {
                    Set<String> instanceIRIs = getInstanceIRIs(selectedClass);

                    results = results
                            .filter(row -> row.getFocusNode() != null && row.getFocusNode().isURIResource())
                            .filter(row -> instanceIRIs.contains(row.getFocusNode().asResource().getURI()));
                }
            }
            else {
                // TODO: filter on row.resultPath for object / data properties
                // NOTE: (currently) not needed, as those can not be selected in the current tab layout
            }

            return results.collect(Collectors.toList());
        }
    }

    private Set<String> getInstanceIRIs(OWLClass selectedClass) {
        OWLModelManager modelManager = owlWorkspace.getOWLModelManager();
        OWLReasonerManager reasonerManager = modelManager.getOWLReasonerManager();
        OWLReasoner reasoner = modelManager.getReasoner();

        /*
        InferredOntologyLoader.warnUserIfReasonerIsNotConfigured should not be called again,
        the user should already be aware that no reasoner is active
        after the message from the "Validate" button.
        */

        if (reasoner != null &&
            (reasonerManager.getReasonerStatus() == ReasonerStatus.INITIALIZED ||
             reasonerManager.getReasonerStatus() == ReasonerStatus.OUT_OF_SYNC)) {
            // direct = false -> retrieve all instances, not only direct instances
            NodeSet<OWLNamedIndividual> instances = reasoner.getInstances(selectedClass, false);

            return instances.getFlattened().stream()
                    .map(i -> i.getIRI().toString())
                    .collect(Collectors.toSet());
        }
        else {
            return Collections.emptySet();
        }
    }

    private static Vector<String> toRow(ValidationResult res) {
        Vector<String> row = new Vector<>();

        row.add(JenaOwlConverter.getQName(res.getSeverity()));
        row.add(JenaOwlConverter.getQName(res.getSourceShape()));
        row.add(res.getMessage());
        row.add(JenaOwlConverter.getQName(res.getFocusNode()));
        row.add(JenaOwlConverter.getQName(res.getPath()));
        row.add(JenaOwlConverter.getQName(res.getValue()));

        return row;
    }

    public ShaclConstraintViolationPanel() {
        this(null);
    }

    public ShaclConstraintViolationPanel(ShaclConstraintViolationViewComponent parent) {
        if (parent != null) {
            this.owlWorkspace = parent.getOWLWorkspace();
            this.view = parent.getView();
        }
        else {
            this.owlWorkspace = null;
            this.view = null;
        }

        this.init();
    }

    protected void init() {
        String[] headers = { "Severity", "SourceShape", "Message", "FocusNode", "Path", "Value" };
        String[][] data = {};

        TableModel tableModel = new DefaultTableModel(data, headers);
        table = new JTable(tableModel) {
            /**
             * 
             */
            private static final long serialVersionUID = 1L;

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table.setAutoscrolls(true);

        JScrollPane scroll = new JScrollPane(table);

        this.setLayout(new BorderLayout());
        this.add(scroll, BorderLayout.CENTER);

        this.initObservers();
    }

    /**
     * Register to all services this class wants to subscribe.
     */
    private void initObservers() {
        // register to events from shacl validation
        ShaclValidationRegistry.addObserver(shaclObserver);

        // register to error events emitted by this project
        ErrorNotifier.register(errorObserver);

        // register to selection changes in Protégé
        if (owlWorkspace != null) {
            owlWorkspace.getOWLSelectionModel().addListener(selectionObserver);
        }
    }

    /**
     * Defines behavior on disposal of panel.
     */
    public void dispose() {
        ShaclValidationRegistry.removeObserver(shaclObserver);
        ErrorNotifier.unregister(errorObserver);

        if (owlWorkspace != null) {
            owlWorkspace.getOWLSelectionModel().removeListener(selectionObserver);
        }
    }

    public DefaultTableModel getTableModel() {
        return ((DefaultTableModel) table.getModel());
    }

    public JTable getTable() {
        return this.table;
    }
}
