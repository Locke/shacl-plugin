package at.ac.tuwien.shacl.plugin.util;

import org.apache.jena.ontology.OntDocumentManager;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.ontology.impl.OntModelImpl;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFLib;
import org.apache.jena.riot.system.StreamRDFWrapper;
import org.apache.jena.util.FileUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import at.ac.tuwien.shacl.plugin.events.ShaclValidation;

/**
 * Provides convenience methods for test execution.
 */
public class TestUtil4 {

    private static final URL example4aUrl = ShaclValidation.class.getClassLoader().getResource("example4a.ttl");
    private static final URL example4bUrl = ShaclValidation.class.getClassLoader().getResource("example4b.ttl");
    private static final URL example4DataUrl = ShaclValidation.class.getClassLoader().getResource("example4-data.owl.ttl");


    public static Model getDataModel() throws IOException {
        try (InputStream in = example4DataUrl.openStream()) {
            Model dataModel = ModelFactory.createDefaultModel();
            dataModel.read(in, null, FileUtils.langTurtle);

            return dataModel;
        }
    }

    public static Model getShapesModel() throws IOException {
        OntDocumentManager dm = new OntDocumentManager();

        OntModelSpec spec = new OntModelSpec(OntModelSpec.OWL_MEM);
		spec.setDocumentManager(dm);

        try {
            OntModelWithIRI model4a = loadOntModel(spec, example4aUrl);
            dm.addModel(model4a.getBaseIRI(), model4a);
            return loadOntModel(spec, example4bUrl);
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        }
    }

    private static OntModelWithIRI loadOntModel(OntModelSpec spec, URL url) throws IOException {
        OntModelWithIRI model = new OntModelWithIRI(spec);
        try (InputStream in = url.openStream()) {
            StreamRDFWrapperWithIRI sink = new StreamRDFWrapperWithIRI(StreamRDFLib.graph(model.getGraph()));

            RDFParser.source(in).lang(RDFLanguages.nameToLang(FileUtils.langTurtle)).parse(sink);

            model.setBaseIRI(sink.getBaseIRI());

            model.loadImports();
            model.rebind();

            return model;
        }
    }

    private static class OntModelWithIRI extends OntModelImpl {
        private String baseIRI = null;

        public OntModelWithIRI(OntModelSpec spec, Model model) {
            super(spec, model);
        }

        public OntModelWithIRI(OntModelSpec spec) {
            super(spec);
        }

        public void setBaseIRI(String baseIRI) {
            this.baseIRI = baseIRI;
        }

        public String getBaseIRI() {
            return baseIRI;
        }
    }

    private static class StreamRDFWrapperWithIRI extends StreamRDFWrapper {
        private String baseIRI = null;

        public StreamRDFWrapperWithIRI(StreamRDF other) {
            super(other);
        }

        @Override
        public void base(String base) {
            other.base(base);
            this.baseIRI = base;
        }

        public String getBaseIRI() {
            return baseIRI;
        }
    }

}
