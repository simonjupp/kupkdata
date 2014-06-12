package oldscripts;/*
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

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Author: Simon Jupp<br>
 * Date: Nov 16, 2010<br>
 * The University of Manchester<br>
 * Bio-Health Informatics Group<br>
 */
public class garonne {

    OWLOntologyManager manager;
    OWLOntology garonne_ontology;
    OWLDataFactory factory;

    String bio2rdfURI = "http://bio2rdf.org/geneid:";
    String rdfBaseURI = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
    String rdfsBaseURI = "http://www.w3.org/2000/01/rdf-schema#";
    String taxURI = "http://bio2rdf.org/ns/taxonomy:Taxonomy_of_species";
    String kupkbURI = "http://www.e-lico.eu/data/kupkb/";
    String kupoURI = "http://www.e-lico.eu/data/kupo/";
    String base = "http://www.e-lico.eu/data/kupkb/GSE694_DOUCET_GARONNE_";
    String maoBase = "http://purl.org/obo/owl/MA#";

    IRI sageAnalysisIRI = IRI.create(kupkbURI + "KUPKB_1000004"); // SAGE analysis
    IRI analysisOf = IRI.create(kupkbURI + "analysisOf");
    IRI mRNAlist = IRI.create(kupkbURI + "KUPKB_1000027"); // mRNA list
    IRI proteinListMember = IRI.create(kupkbURI + "KUPKB_1000032");// mRNA list member
    IRI annotationURI = IRI.create(kupkbURI + "KUPKB_1000036");
    IRI has_bio_material = IRI.create(kupkbURI + "bioMaterial");
    IRI has_bio_condition = IRI.create(kupkbURI + "bioCondition");
    IRI has_role = IRI.create("http://purl.obolibrary.org/obo/OBI_0000316");
    IRI analyte = IRI.create(kupoURI + "KUPO_0300008");
//    IRI control = IRI.create(kupoURI + "KUPO_0300009");
    IRI normalCondition = IRI.create(kupoURI + "KUPO_0300007");
    IRI annotatedWith = IRI.create(kupkbURI + "annotated_with");
    IRI taxonomyIRI = IRI.create("http://bio2rdf.org/ns/bio2rdf:xTaxonomy");
    IRI humanIRI = IRI.create("http://bio2rdf.org/taxon:9606");
    IRI produces = IRI.create(kupkbURI + "produces");
    IRI hasDBRef = IRI.create(kupkbURI + "hasDatabaseRef");
    IRI hasMember = IRI.create(kupkbURI + "hasMember");
    IRI hasQuality = IRI.create(kupkbURI + "hasQuality");
    IRI hasExpression = IRI.create(kupkbURI + "hasExpression");
    IRI adultURI = IRI.create("http://www.ebi.ac.uk/efo/EFO_0001272");


    OWLNamedIndividual compoundList;

    public garonne() {

        manager = OWLManager.createOWLOntologyManager();
        factory = manager.getOWLDataFactory();
        try {
            manager.loadOntology(IRI.create("file:/Users/simon/Documents/e-lico/svn/trunk/software/ontologies/kupkb/kupkb.owl"));
//            manager.loadOntology(IRI.create("file:/Users/simon/Dropbox/BHIGJupp/kupkb-v2.owl"));
            garonne_ontology = manager.createOntology(IRI.create("http://www.e-lico.eu/data/kupkb/experiment/GSE694_DOUCET_GARONNE"));


            OWLNamedIndividual experimentAssayType = factory.getOWLNamedIndividual(IRI.create(base + "assay"));
            OWLAxiom ax = factory.getOWLClassAssertionAxiom(factory.getOWLClass(IRI.create("http://www.e-lico.eu/data/kupkb/KUPKB_1000018")), experimentAssayType);
            manager.addAxiom(garonne_ontology, ax);


            Map<String, String> fileMap = new HashMap<String, String>();
            fileMap.put("glom.csv", "MA_0001657");
            fileMap.put("ccd.csv", "MA_0002600");
            fileMap.put("dct.csv", "MA_0001666");
            fileMap.put("pct.csv", "MA_0001669");
            fileMap.put("pst.csv", "MA_0002614");
            fileMap.put("omcd.csv", "MA_0002599");
            fileMap.put("ctal.csv", "MA_0002615");
            fileMap.put("mtal.csv", "MA_0002628");

            // read files
            for (String file : fileMap.keySet()) {

                String maID = fileMap.get(file);

                IRI analysisIDIRI = IRI.create(base  + maID + "_analysis");
                IRI compoundListIRI = IRI.create(base + maID + "_list");
                IRI experimentalFactorIRI = IRI.create(base + maID + "_factor");

                OWLIndividual analysisID = factory.getOWLNamedIndividual(analysisIDIRI);
                OWLAxiom ax1 = factory.getOWLClassAssertionAxiom(factory.getOWLClass(sageAnalysisIRI),
                        analysisID);

                manager.addAxiom(garonne_ontology, ax1);

                // relate to an experiment type
                OWLAxiom ax2 = factory.getOWLObjectPropertyAssertionAxiom(factory.getOWLObjectProperty(analysisOf),
                        analysisID, experimentAssayType);
                manager.addAxiom(garonne_ontology, ax2);

                // relate to annotation
                OWLIndividual experimentFactor = factory.getOWLNamedIndividual(experimentalFactorIRI);
                OWLAxiom ax3 = factory.getOWLClassAssertionAxiom(factory.getOWLClass(annotationURI),
                        experimentFactor);
                manager.addAxiom(garonne_ontology, ax3);

                OWLAxiom ax4 = factory.getOWLObjectPropertyAssertionAxiom(factory.getOWLObjectProperty(annotatedWith),
                        analysisID, experimentFactor);
                manager.addAxiom(garonne_ontology, ax4);

                // describe annotation
                OWLAxiom ax5 = factory.getOWLObjectPropertyAssertionAxiom(factory.getOWLObjectProperty(has_bio_material),
                        experimentFactor, factory.getOWLNamedIndividual(IRI.create(maoBase + maID)));

                OWLAxiom ax6 = factory.getOWLObjectPropertyAssertionAxiom(factory.getOWLObjectProperty(has_bio_condition),
                        experimentFactor, factory.getOWLNamedIndividual(normalCondition));

                OWLAxiom ax7 = factory.getOWLObjectPropertyAssertionAxiom(factory.getOWLObjectProperty(has_role),
                        experimentFactor, factory.getOWLNamedIndividual(analyte));

                OWLAxiom ax7b = factory.getOWLObjectPropertyAssertionAxiom(factory.getOWLObjectProperty(taxonomyIRI),
                        experimentFactor, factory.getOWLNamedIndividual(humanIRI));

                OWLAxiom ax7c = factory.getOWLObjectPropertyAssertionAxiom(factory.getOWLObjectProperty(hasQuality),
                        experimentFactor, factory.getOWLNamedIndividual(adultURI));

                manager.addAxiom(garonne_ontology, ax5);
                manager.addAxiom(garonne_ontology, ax6);
                manager.addAxiom(garonne_ontology, ax7);
                manager.addAxiom(garonne_ontology, ax7b);
                manager.addAxiom(garonne_ontology, ax7c);

                // create the compound list
                compoundList = factory.getOWLNamedIndividual(compoundListIRI);
                OWLAxiom ax8 = factory.getOWLClassAssertionAxiom(factory.getOWLClass(mRNAlist),
                        compoundList);
                manager.addAxiom(garonne_ontology, ax8);
                // add produces relation
                OWLAxiom ax9 = factory.getOWLObjectPropertyAssertionAxiom(factory.getOWLObjectProperty(produces),
                        analysisID, compoundList);
                manager.addAxiom(garonne_ontology, ax9);

                // read file

                 Pattern p = Pattern.compile("^[0-9]+$");
                try {
                    BufferedReader input =  new BufferedReader(new FileReader(new File("/Users/simon/dev/kupo/kupdb/version2/experiments/doucet_garrone_data/" + file)));
                    String line = null; //not declared within while loop
                    while (( line = input.readLine()) != null){
                        if (!line.isEmpty()) {

                            String [] split = line.split(",");
                            if (split.length >1) {

                                String geneid = split[0];
                                Matcher m = p.matcher(geneid);
                               boolean b = m.matches();

                                if (m.matches()) {
                                    IRI compoundListMemberIRI = IRI.create(base + "listmember_" + geneid);
                                    OWLIndividual compoundListMember = factory.getOWLNamedIndividual(compoundListMemberIRI);
                                    OWLAxiom ax10 = factory.getOWLClassAssertionAxiom(factory.getOWLClass(proteinListMember),
                                            compoundListMember);
                                    manager.addAxiom(garonne_ontology, ax10);

                                    // relate compound list to a member
                                    OWLAxiom ax11 = factory.getOWLObjectPropertyAssertionAxiom(factory.getOWLObjectProperty(hasMember),
                                            compoundList, compoundListMember);
                                    manager.addAxiom(garonne_ontology, ax11);

                                    // relate to gene id
                                    OWLAxiom ax12 = factory.getOWLObjectPropertyAssertionAxiom(factory.getOWLObjectProperty(hasDBRef),
                                            compoundListMember, factory.getOWLNamedIndividual(IRI.create(bio2rdfURI + geneid)));
                                    manager.addAxiom(garonne_ontology, ax12);

                                    // relate expression value
                                    OWLAxiom ax13 = factory.getOWLObjectPropertyAssertionAxiom(factory.getOWLObjectProperty(hasExpression),
                                            compoundListMember, factory.getOWLNamedIndividual(IRI.create("http://www.e-lico.eu/data/kupkb/KUPKB_1000063")));
                                    manager.addAxiom(garonne_ontology, ax13);
                                    
                                }
                            }
                        }
                    }

                } catch (FileNotFoundException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                } catch (IOException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }




            }



        } catch (OWLOntologyCreationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }




        try {
            manager.saveOntology(garonne_ontology, IRI.create("file:/Users/simon/Documents/e-lico/svn/trunk/software/ontologies/kupkb/garonnev3.owl"));
        } catch (OWLOntologyStorageException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }


    }

    public static void main(String[] args) {

        new garonne();



    }


}