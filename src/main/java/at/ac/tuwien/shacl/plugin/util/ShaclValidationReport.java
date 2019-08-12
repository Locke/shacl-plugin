package at.ac.tuwien.shacl.plugin.util;

import org.apache.jena.rdf.model.Resource;

import org.topbraid.shacl.validation.ResourceValidationResult;
import org.topbraid.shacl.validation.ValidationResult;
import org.topbraid.shacl.vocabulary.SH;

import java.util.Set;

public class ShaclValidationReport {
    public final boolean conforms;
    public final Set<ValidationResult> validationResults;

    public ShaclValidationReport(Set<ValidationResult> validationResults) {
        this.validationResults = validationResults;

        this.conforms = validationResults.isEmpty();
    }

    public ShaclValidationReport(Resource r) {
        this(r.listProperties(SH.result).mapWith(s -> (ValidationResult) new ResourceValidationResult(s.getResource())).toSet());
    }
}
