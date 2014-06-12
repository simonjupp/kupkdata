package kupkb_experiments;/*
 * Copyright (C) 2011, University of Manchester
 *
 * Modifications to the initial code base are copyright of their
 * respective authors, or their employers as appropriate.  Authorship
 * of the modifications may be determined from the ChangeLog placed at
 * the end of this file.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.

 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.

 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

import org.semanticweb.owlapi.io.RDFXMLOntologyFormat;
import org.semanticweb.owlapi.model.*;

import java.io.File;
import java.net.URI;

/**
 * Author: Simon Jupp<br>
 * Date: Feb 10, 2011<br>
 * The University of Manchester<br>
 * Bio-Health Informatics Group<br>
 *
 * This class takes an experiment spreadsheet as input and converts the spreadsheet into an OWL ontology.
 * It has two arguments, the first is the path to the spreadsheets excel file and the second is the path to the
 * output ontology file you want to create
 *
 */
public class Run {
    public static void main(String[] args) {

        System.setProperty("entityExpansionLimit", "1000000000");
        File file = new File(args[0]);

        ExperimentSpreadSheetParser parser = new ExperimentSpreadSheetParser(file);

        KUPExperiment exp = parser.getExperiment();

        OntologyBuilder ob = new OntologyBuilder();
        try {
//            ob.loadOntology(IRI.create(KUPNamespaces.KUPKB_IRI.toString()));
//            ob.loadOntology(IRI.create(KUPNamespaces.MAO.toString()));
//            ob.loadOntology(IRI.create(KUPNamespaces.CTO.toString()));
            ob.loadOntology(IRI.create("file:/Users/jupp/dev/ontology_dev/kupo/kupkb/kupkb-merged.owl"));
            ob.initialise();
        } catch (OWLOntologyCreationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        OWLOntology onto = ob.generateOWL(exp);
        OWLOntologyManager man = ob.getOWLManager();
        try {
            man.saveOntology(onto, new RDFXMLOntologyFormat(), IRI.create(args[1]));
        } catch (OWLOntologyStorageException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }


    }

}
