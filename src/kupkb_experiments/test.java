package kupkb_experiments;/*
 * Copyright (C) 2007, University of Manchester
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
 * This class is used for testing the converter of individual spreadsheets into OWL ontology files
 *
 */
public class test {

    public static void main(String[] args) {

        String filename = "taylor-pkd-mouse-metabo_45days";

        File file = new File(URI.create("file:/Users/jupp/Dropbox/JuppKlein/KUP/datasets/November2011/taylor_weiss/" + filename +  ".xls"));


         OntologyBuilder ob = new OntologyBuilder();
         try {
             ob.loadOntology(IRI.create(KUPNamespaces.KUPKB_IRI.toString()));
             ob.loadOntology(IRI.create(KUPNamespaces.MAO.toString()));
             ob.loadOntology(IRI.create(KUPNamespaces.CTO.toString()));
//             ob.loadOntology(IRI.create(KUPNamespaces.PATO.toString()));
         } catch (OWLOntologyCreationException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }
        ob.initialise();

        ExperimentSpreadSheetParser parser = new ExperimentSpreadSheetParser(file);

        KUPExperiment exp = parser.getExperiment();

         OWLOntology onto = ob.generateOWL(exp);
         OWLOntologyManager man = ob.getOWLManager();
         try {
             man.saveOntology(onto, new RDFXMLOntologyFormat(), IRI.create("file:/Users/jupp/Dropbox/JuppKlein/KUP/experiment_ontologies/" + filename + ".owl"));
         } catch (OWLOntologyStorageException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }
 

    }
}
