package at.ac.tuwien.shacl.plugin.util;

import org.apache.jena.ontology.OntDocumentManager;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.util.FileUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import at.ac.tuwien.shacl.plugin.events.ShaclValidation;

/**
 * Provides convenience methods for test execution.
 */
public class TestUtil4 {

    private static final String example4aIRI = "http://www.example.org/names";
    private static final String example4bIRI = "http://www.example.org/persons";
    private static final String example4DataIRI = "http://www.example.org/";

    private static final URL example4aUrl = ShaclValidation.class.getClassLoader().getResource("example4a.ttl");
    private static final URL example4bUrl = ShaclValidation.class.getClassLoader().getResource("example4b.ttl");
    private static final URL example4DataUrl = ShaclValidation.class.getClassLoader().getResource("example4-data.owl.ttl");


    public static Model getDataModel() throws IOException {
        try (InputStream in = example4DataUrl.openStream()) {
            Model dataModel = ModelFactory.createDefaultModel();
            dataModel.read(in, example4DataIRI, FileUtils.langTurtle);

            return dataModel;
        }
    }

    public static Model getShapesModel() throws IOException {
        OntDocumentManager dm = new OntDocumentManager();

        OntModelSpec spec = new OntModelSpec(OntModelSpec.OWL_MEM);
		spec.setDocumentManager(dm);

        try {
            OntModel model4a = loadOntModel(spec, example4aUrl, example4aIRI);
            dm.addModel(example4aIRI, model4a);
            return loadOntModel(spec, example4bUrl, example4bIRI);
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        }
    }

    private static OntModel loadOntModel(OntModelSpec spec, URL url, String base) throws IOException {
        OntModel model = ModelFactory.createOntologyModel(spec);
        try (InputStream in = url.openStream()) {
            model.read(in, base, FileUtils.langTurtle);
            return model;
        }
    }

}
