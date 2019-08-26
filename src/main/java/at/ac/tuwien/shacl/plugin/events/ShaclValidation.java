package at.ac.tuwien.shacl.plugin.events;

import java.util.Observable;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;

import org.topbraid.shacl.validation.ResourceValidationReport;
import org.topbraid.shacl.validation.ValidationReport;
import org.topbraid.shacl.validation.ValidationUtil;
import org.topbraid.shacl.util.ModelPrinter;

/**
 *
 */
public class ShaclValidation extends Observable {

    public void runValidation2(Model shaclModel, Model dataModel) {
        // Run the validator
        // NOTE: ValidationUtil should offer an interface to ValidationEngine.getValidationResults
        Resource results = ValidationUtil.validateModel(dataModel, shaclModel, true);

        // Print violations
        System.out.println("--- ************* ---");
        System.out.println(ModelPrinter.get().print(results.getModel()));


        ValidationReport report = new ResourceValidationReport(results);

        this.setChanged();
        this.notifyObservers(report);
    }

}
