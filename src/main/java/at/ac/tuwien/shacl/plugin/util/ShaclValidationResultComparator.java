package at.ac.tuwien.shacl.plugin.util;

import org.apache.jena.rdf.model.RDFNode;
import org.topbraid.shacl.validation.ValidationResult;
import org.topbraid.shacl.vocabulary.SH;

import java.util.Comparator;
import java.util.Objects;

import at.ac.tuwien.shacl.plugin.syntax.JenaOwlConverter;

public class ShaclValidationResultComparator implements Comparator<ValidationResult> {

    public final static ShaclValidationResultComparator INSTANCE = new ShaclValidationResultComparator();

    @Override
    public int compare(ValidationResult r1, ValidationResult r2) {
        int compareSeverity = compareSeverity(r1.getSeverity(), r2.getSeverity());
        if (compareSeverity != 0)
            return compareSeverity;

        int compareFocusNode = JenaOwlConverter.compareRDFNode(r1.getFocusNode(), r2.getFocusNode());
        if (compareFocusNode != 0)
            return compareFocusNode;

        int compareResultPath = JenaOwlConverter.compareRDFNode(r1.getPath(), r2.getPath());
        if (compareResultPath != 0)
            return compareResultPath;

        int compareShape = JenaOwlConverter.compareRDFNode(r1.getSourceShape(), r2.getSourceShape());
        if (compareShape != 0)
            return compareShape;

        int compareResultMessage = compareString(r1.getMessage(), r2.getMessage());
        if (compareResultMessage != 0)
            return compareResultMessage;

        return JenaOwlConverter.compareRDFNode(r1.getValue(), r2.getValue());
    }

    private static int compareSeverity(RDFNode n1, RDFNode n2) {
        int s1 = getSeverityNumber(n1);
        int s2 = getSeverityNumber(n2);

        // Violation first, then Warning, then Info, then alphabetical from A to Z for custom severities
        if (s1 == 0 && s2 == 0) {
            return JenaOwlConverter.compareRDFNode(n1, n2);
        }
        else {
            return Integer.compare(s1, s2);
        }
    }

    private static int compareString(String n1, String n2) {
        if (Objects.equals(n1, n2)) {
            return 0;
        }
        else if (n1 == null) {
            return -1;
        }
        else if (n2 == null) {
            return 1;
        }
        else {
            return n1.compareTo(n2);
        }
    }

    private static int getSeverityNumber(RDFNode n) {
        if (n != null) {
            if (n.equals(SH.Violation)) {
                return 1;
            } else if (n.equals(SH.Warning)) {
                return 2;
            } else if (n.equals(SH.Info)) {
                return 3;
            }
        }

        return 0;
    }

}
