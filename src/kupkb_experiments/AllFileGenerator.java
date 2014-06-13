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
 * Date: Feb 13, 2011<br>
 * The University of Manchester<br>
 * Bio-Health Informatics Group<br>
 *
 * This script reads all the experiment spreadsheets files in the JuppKlien dropbox folder
 * and converts them all to individual OWL files. 
 *
 *
 */
public class AllFileGenerator {

    OntologyBuilder ob;

    public AllFileGenerator() {
        ob = new OntologyBuilder();
        try {
//            ob.loadOntology(IRI.create(KUPNamespaces.KUPKB_IRI.toString()));
//            ob.loadOntology(IRI.create(KUPNamespaces.MAO.toString()));
//            ob.loadOntology(IRI.create(KUPNamespaces.CTO.toString()));
// always leave out            ob.loadOntology(IRI.create(KUPNamespaces.PATO.toString()));
            ob.loadOntology(IRI.create("file:/Users/jupp/dev/ontology_dev/kupkb/ontologies/kupkb/kupkb-merged.owl"));

        } catch (OWLOntologyCreationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        ob.initialise();

    }

    public void walk (String path)  {
        File root = new File( path );

        File[] list = root.listFiles();


        for ( File f : list ) {
            if ( f.isDirectory() ) {
                if (f.getAbsolutePath().contains("waiting")) {
                    break;
                }
                else {
                    walk( f.getAbsolutePath() );
                    System.err.println( "Dir:" + f.getAbsoluteFile() );
                }
            }
            else {
                System.out.println( "File:" + f.getAbsoluteFile() );
                String filename = f.getName();
                String ontologyname = filename.replace(".xls", ".owl");
                System.out.println("File name: " + filename);
                if (f.getName().endsWith(".xls")) {
//                    File file = new File(f.get);

                     ExperimentSpreadSheetParser parser = new ExperimentSpreadSheetParser(f);

                     KUPExperiment exp = parser.getExperiment();

                     OWLOntology onto = ob.generateOWL(exp);
                     OWLOntologyManager man = ob.getOWLManager();
                     try {
                         File outFile = new File("/Users/jupp/Google Drive/JuppKlein/CVDKB/rdf/" + ontologyname);
                         man.saveOntology(onto, new RDFXMLOntologyFormat(), IRI.create(outFile.toURI()));
                         ob.getOWLManager().removeOntology(onto);
                     } catch (OWLOntologyStorageException e) {
                         e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                     }

                }


            }
        }

        for (String name : ob.getAllNames()) {
            System.out.println(name);
        }

    }

    public static void main(String[] args) {

        String path = "/Users/jupp/Google Drive/JuppKlein/CVDKB/Datasets";

        AllFileGenerator all = new AllFileGenerator();
        all.walk(path);
    }
}
