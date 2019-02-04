package at.ac.tuwien.shacl.plugin.util;

import java.io.InputStream;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.util.FileUtils;
import org.topbraid.shacl.arq.SHACLFunctions;
import org.topbraid.shacl.vocabulary.SH;
import org.topbraid.jenax.util.JenaUtil;
import org.topbraid.jenax.util.SystemTriples;

public class SHACLSystemModel {

    private static Model shaclModel;

    public static Model getSHACLModel() {
        if (shaclModel == null) {

            shaclModel = JenaUtil.createDefaultModel();

            InputStream shaclTTL = SHACLSystemModel.class.getResourceAsStream("/etc/shacl.ttl");
            shaclModel.read(shaclTTL, SH.BASE_URI, FileUtils.langTurtle);

            shaclModel.add(SystemTriples.getVocabularyModel());

            SHACLFunctions.registerFunctions(shaclModel);
        }
        return shaclModel;
    }
}
