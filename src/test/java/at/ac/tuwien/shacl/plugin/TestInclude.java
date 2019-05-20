package at.ac.tuwien.shacl.plugin;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.junit.Test;

import java.io.IOException;

import org.topbraid.shacl.validation.ValidationUtil;

import at.ac.tuwien.shacl.plugin.util.ShaclValidationReport;
import at.ac.tuwien.shacl.plugin.util.ShaclValidationResult;
import at.ac.tuwien.shacl.plugin.util.TestUtil4;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class TestInclude {

    @Test
    public void testExample4() throws IOException {

        // Load the main data model
        Model dataModel   = TestUtil4.getDataModel();
        Model shapesModel = TestUtil4.getShapesModel();

        // Run the validator and print results
        Resource results =
                ValidationUtil.validateModel(dataModel, shapesModel, false);

        results.getModel().write(System.out, "TURTLE");


        ShaclValidationReport report = new ShaclValidationReport(results);

        assertFalse("Model should not conform", report.conforms);
        assertEquals("There should be one violation", 1, report.validationResults.size());

        ShaclValidationResult res = (ShaclValidationResult) report.validationResults.toArray()[0];

        assertEquals("Value has less than 4^^http://www.w3.org/2001/XMLSchema#integer characters", res.resultMessage.toString());
    }

}
