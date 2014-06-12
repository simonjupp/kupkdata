package kupkb_experiments;

import junit.framework.TestCase;
import org.apache.log4j.Logger;
import uk.ac.man.ac.uk.kupkb.repository.DefaultKUPKBConfig;
import uk.ac.man.ac.uk.kupkb.repository.KUPKBManager;

import java.io.File;

/**
 * Created by IntelliJ IDEA.
 * User: jupp
 * Date: 02/12/2011
 * Time: 12:55
 * To change this template use File | Settings | File Templates.
 */
public class KUPKBBuildValidator extends TestCase   {

    private static final Logger logger = Logger.getLogger(KUPKBBuildValidator.class);


    KUPKBManager kupmanager;

    @Override
    protected void setUp() throws Exception {

        logger.info("Setting up");
        kupmanager = new KUPKBManager(new DefaultKUPKBConfig());
    }

    public void fullReport () {


    }

    public void randomEntryReport() {

    }

    public void checkRandomIndividualFile (File file) {

        logger.info("Checking random individual from file " + file.getAbsolutePath());

        ExperimentSpreadSheetParser parser = new ExperimentSpreadSheetParser(file);

        logger.info("File parsing ok for " + file.getAbsolutePath());

        KUPExperiment exp = parser.getExperiment();

        logger.info("Got KUPExperiment " + exp.getExperimentID());

        logger.warn("What next?");



    }

    public void checkFullFile (File file) {

    }

    public void testSuggestQuery1 () {

    }

    public void checkSuggestQuery2 () {

    }

    public void checkMainResults () {

    }

    public void checkAdvancedSearchResult() {

    }

}
