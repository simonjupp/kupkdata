import com.sun.tools.javac.code.Attribute;
import junit.framework.TestCase;
import junit.framework.TestResult;
import kupkb_experiments.*;
import org.apache.log4j.Logger;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.BindingSet;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.semanticweb.owlapi.io.RDFXMLOntologyFormat;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import uk.ac.man.ac.uk.kupkb.repository.DefaultKUPKBConfig;
import uk.ac.man.ac.uk.kupkb.repository.KUPKBManager;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.net.URI;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: jupp
 * Date: 02/12/2011
 * Time: 13:43
 * To change this template use File | Settings | File Templates.
 */
public class KUPKBBuildValidator extends TestCase {

    private static final Logger logger = Logger.getLogger(KUPKBBuildValidator.class);


    KUPKBManager kupmanager;

    @Override
    protected void setUp() throws Exception {

        logger.info("Setting up");
        kupmanager = new KUPKBManager(new DefaultKUPKBConfig());
    }

    public void testFullReport () {
        String path = "/Users/jupp/Dropbox/JuppKlein/KUP/datasets";

        randomEntryReport(path);

    }

    public void randomEntryReport(String path) {

        File root = new File( path );

        File[] list = root.listFiles();


        for ( File f : list ) {
            if ( f.isDirectory() ) {
                if (f.getAbsolutePath().contains("waiting")) {
                    break;
                }
                else {
                    randomEntryReport( f.getAbsolutePath() );

                    logger.info( "Dir:" + f.getAbsoluteFile() );
                }
            }
            else {

                logger.info("File:" + f.getAbsoluteFile());
                String filename = f.getName();
                logger.info("File name: " + filename);
                if (f.getName().endsWith(".xls")) {
                    File file = new File(URI.create("file:" + f.getAbsolutePath()));

                    ExperimentSpreadSheetParser parser = new ExperimentSpreadSheetParser(file);

                    KUPExperiment exp = parser.getExperiment();
                    boolean atleast1 = false;

                    for (KUPAnalysis analysis : exp.getAnalysis()) {

                        for (CompoundList compoundList : analysis.getCompoundList()) {

                            for (CompoundList.ListMember member :compoundList.getMembers()) {

                                if (member.getGeneId() != null ) {

                                    if (!member.getGeneId().equals("")) {
                                        logger.info("Got a gene id: " + member.getGeneId());
                                        boolean result = checkMainResults(exp.getExperimentID(), KUPNamespaces.GENEIDPREFIX + member.getGeneId());
                                        if (!result) {
                                            continue;
                                        }
                                        else {
                                            atleast1 = true;
                                            break;
                                        }
                                    }
                                }

                                if (member.getUniprotID() != null) {

                                    if (!member.getUniprotID().equals("")) {

                                        logger.info("Got a uniprot id: " + member.getUniprotID());
                                        String geneid = getGeneIdForUnirpot(KUPNamespaces.UNIPROTURI+ member.getUniprotID());
                                        if (!geneid.equals("")) {
                                            boolean result = checkMainResults(exp.getExperimentID(), geneid);
                                            if (!result) {
                                                continue;
                                            }
                                            else {
                                                atleast1 = true;
                                                break;
                                            }
                                        }
                                    }
                                }

                                if (member.getMicrocosmid() != null) {

                                    if (!member.getMicrocosmid().equals("")) {

                                        logger.info("Got a miRNA id: " + member.getMicrocosmid());
                                        String miranda = getMirandaIdForMiRna(member.getMicrocosmid());
                                        if (!miranda.equals("")) {
                                            boolean result = checkMainResults(exp.getExperimentID(), miranda);
                                            if (!result) {
                                                continue;
                                            }
                                            else {
                                                atleast1 = true;
                                                break;
                                            }
                                        }
                                    }
                                }
                                if (member.getHmdbid() != null) {

                                    if (!member.getHmdbid().equals("")) {

                                        logger.info("Got a hmdb id: " + member.getHmdbid());
                                        boolean result = checkMainResults(exp.getExperimentID(), KUPNamespaces.HMDBURI + member.getHmdbid());
                                        if (!result) {
                                            continue;
                                        }
                                        else {
                                            atleast1 = true;
                                            break;
                                        }

                                    }
                                }
                            }
                        }
                    }
                    if (!atleast1) {
                        logger.warn("Gene id was empty for all compounds!" + exp.getExperimentID());
                        fail();
                    }
                }
            }
        }
    }

    public String getGeneIdForUnirpot (String uniprotid) {

        ValueFactory factory = kupmanager.getValueFactory();

        try {
            String[] queries = KUPKBManager.collectQueries("get_geneid_from_uniprot", "./test_queries");

            for (int i = 0; i < queries.length; i++) {
                String name = queries[i].substring(0, queries[i].indexOf(":"));
                String query = queries[i].substring(name.length() + 2).trim();

                TupleQuery tq = kupmanager.prepareTupleQuery(query);
                tq.setBinding("uniprot", factory.createURI(uniprotid));

                TupleQueryResult queryResult = tq.evaluate();

                while (queryResult.hasNext()) {
                    BindingSet bindingSet = queryResult.next();
                    Value geneid = bindingSet.getValue("geneid");
                    return geneid.stringValue();

                }

            }

        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }


        return "";
    }

    public String getMirandaIdForMiRna (String miRna) {

        ValueFactory factory = kupmanager.getValueFactory();

        try {
            String[] queries = KUPKBManager.collectQueries("get_miranda_from_mirna", "./test_queries");

            for (int i = 0; i < queries.length; i++) {
                String name = queries[i].substring(0, queries[i].indexOf(":"));
                String query = queries[i].substring(name.length() + 2).trim();

                TupleQuery tq = kupmanager.prepareTupleQuery(query);
                tq.setBinding("mirna", factory.createLiteral(miRna));

                TupleQueryResult queryResult = tq.evaluate();

                while (queryResult.hasNext()) {
                    BindingSet bindingSet = queryResult.next();
                    Value geneid = bindingSet.getValue("miranda");
                    return geneid.stringValue();

                }

            }

        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }


        return "";
    }

    public boolean checkMainResults (String expId, String compoundId) {

        logger.info("querying main results with exp: " + expId + " and compound " + compoundId);

        ValueFactory factory = kupmanager.getValueFactory();


        boolean hasResult = false;

        try {
            String[] queries = KUPKBManager.collectQueries("generate_results_table", "./test_queries");

            for (int i = 0; i < queries.length; i++) {
                String name = queries[i].substring(0, queries[i].indexOf(":"));
                String query = queries[i].substring(name.length() + 2).trim();

                TupleQuery tq = kupmanager.prepareTupleQuery(query);
                tq.setBinding("geneid", factory.createURI(compoundId));

                TupleQueryResult queryResult = tq.evaluate();

                if (queryResult.hasNext()) {
                    hasResult = true;

                }

            }

        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        return hasResult;

    }

    public void checkAdvancedSearchResult() {

    }

}
