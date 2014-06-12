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

/**
 * Author: Simon Jupp<br>
 * Date: Nov 16, 2010<br>
 * The University of Manchester<br>
 * Bio-Health Informatics Group<br>
 */
public class Vlahou2GelLC {

    OWLOntologyManager manager;
    OWLOntology mann_ontology;
    OWLDataFactory factory;

    String uniprotURI = "http://purl.uniprot.org/uniprot/";
    String rdfBaseURI = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
    String rdfsBaseURI = "http://www.w3.org/2000/01/rdf-schema#";
    String taxURI = "http://bio2rdf.org/ns/taxonomy:Taxonomy_of_species";
    String kupkbURI = "http://www.e-lico.eu/data/kupkb/v2/";
    String base = "http://www.e-lico.eu/data/kupkb/experiment/valhouGelLC_";

    IRI lcmsAnalysisIRI = IRI.create(kupkbURI + "GeLC-MSProteinAnalysis");
    IRI analysisOf = IRI.create(kupkbURI + "analysisOf");
    IRI lcmsList = IRI.create(kupkbURI + "GeLC-MSProteinList");
    IRI proteinListMember = IRI.create(kupkbURI + "ProteinListMember");
    IRI efoIRI = IRI.create("http://www.ebi.ac.uk/efo/EFO_0000001");
    IRI has_bio_material = IRI.create(kupkbURI + "bioMaterial");
    IRI has_bio_condition = IRI.create(kupkbURI + "bioCondition");
    IRI has_role = IRI.create("http://purl.obolibrary.org/obo/OBI_0000316");
    IRI analyte = IRI.create(kupkbURI + "analyte");
    IRI adultHuman = IRI.create(kupkbURI + "NormalAdultHuman");
    IRI annotatedWith = IRI.create(kupkbURI + "annotated_with");
    IRI produces = IRI.create(kupkbURI + "produces");
    IRI hasDBRef = IRI.create(kupkbURI + "hasDatabaseRef");
    IRI hasMember = IRI.create(kupkbURI + "hasMember");
    IRI hasQuality = IRI.create(kupkbURI + "hasQuality");
    IRI urine = IRI.create("http://purl.org/obo/owl/MA#MA_0002545");

    OWLNamedIndividual compoundList;

    public Vlahou2GelLC() {

        manager = OWLManager.createOWLOntologyManager();
        factory = manager.getOWLDataFactory();
        try {
            manager.loadOntology(IRI.create("file:/Users/simon/Documents/e-lico/svn/trunk/software/ontologies/kupkb/kupkp-dev.owl"));
            mann_ontology = manager.createOntology(IRI.create("http://www.e-lico.eu/data/kupkb/experiment/VLAHOUGELLC"));


            OWLNamedIndividual experimentAssayType = factory.getOWLNamedIndividual(IRI.create(base + "assay"));
            OWLAxiom ax = factory.getOWLClassAssertionAxiom(factory.getOWLClass(IRI.create(kupkbURI + "LC-MS_MSProteinAssay")), experimentAssayType);
            manager.addAxiom(mann_ontology, ax);


            IRI analysisIDIRI = IRI.create(base  + "analysis");
            IRI compoundListIRI = IRI.create(base + "list");
            IRI experimentalFactorIRI = IRI.create(base + "factor");

            OWLIndividual analysisID = factory.getOWLNamedIndividual(analysisIDIRI);
            OWLAxiom ax1 = factory.getOWLClassAssertionAxiom(factory.getOWLClass(lcmsAnalysisIRI),
                    analysisID);

            manager.addAxiom(mann_ontology, ax1);

            // relate to an experiment type
            OWLAxiom ax2 = factory.getOWLObjectPropertyAssertionAxiom(factory.getOWLObjectProperty(analysisOf),
                    analysisID, experimentAssayType);
            manager.addAxiom(mann_ontology, ax2);

            // relate to factor
            OWLIndividual experimentFactor = factory.getOWLNamedIndividual(experimentalFactorIRI);
            OWLAxiom ax3 = factory.getOWLClassAssertionAxiom(factory.getOWLClass(efoIRI),
                    experimentFactor);
            manager.addAxiom(mann_ontology, ax3);

            OWLAxiom ax4 = factory.getOWLObjectPropertyAssertionAxiom(factory.getOWLObjectProperty(annotatedWith),
                    analysisID, experimentFactor);
            manager.addAxiom(mann_ontology, ax4);

            // describe factor
            OWLAxiom ax5 = factory.getOWLObjectPropertyAssertionAxiom(factory.getOWLObjectProperty(has_bio_material),
                    experimentFactor, factory.getOWLNamedIndividual(urine));

            OWLAxiom ax6 = factory.getOWLObjectPropertyAssertionAxiom(factory.getOWLObjectProperty(has_bio_condition),
                    experimentFactor, factory.getOWLNamedIndividual(adultHuman));

            OWLAxiom ax7 = factory.getOWLObjectPropertyAssertionAxiom(factory.getOWLObjectProperty(has_role),
                    experimentFactor, factory.getOWLNamedIndividual(analyte));
            manager.addAxiom(mann_ontology, ax5);
            manager.addAxiom(mann_ontology, ax6);
            manager.addAxiom(mann_ontology, ax7);

            // create the compound list
            compoundList = factory.getOWLNamedIndividual(compoundListIRI);
            OWLAxiom ax8 = factory.getOWLClassAssertionAxiom(factory.getOWLClass(lcmsList),
                    compoundList);
            manager.addAxiom(mann_ontology, ax8);
            // add produces relation
            OWLAxiom ax9 = factory.getOWLObjectPropertyAssertionAxiom(factory.getOWLObjectProperty(produces),
                    analysisID, compoundList);
            manager.addAxiom(mann_ontology, ax9);


        } catch (OWLOntologyCreationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }


        try {
            BufferedReader input =  new BufferedReader(new FileReader(new File("/Users/simon/dev/kupo/kupdb/version2/experiments/vlahou/Control_Urine_Protein_Vlahou_GelLC.csv")));
            String line = null; //not declared within while loop
            while (( line = input.readLine()) != null){
                if (!line.isEmpty()) {

                    String [] split = line.split(",");
                    if (split.length >2) {

                        String unirpotAcc = split[1];
                        if (unirpotAcc.contains("-")) {
                            String dash = unirpotAcc.substring(unirpotAcc.lastIndexOf("-"));
                            unirpotAcc = unirpotAcc.replace(dash, "");
                        }

                        if (! (unirpotAcc.equals("") | unirpotAcc.equals("UniProt/SwissProt"))) {
                            System.out.println(unirpotAcc);
                            IRI compoundListMemberIRI = IRI.create(base + "listmember_" + unirpotAcc);
                            OWLIndividual compoundListMember = factory.getOWLNamedIndividual(compoundListMemberIRI);
                            OWLAxiom ax10 = factory.getOWLClassAssertionAxiom(factory.getOWLClass(proteinListMember),
                                    compoundListMember);
                            manager.addAxiom(mann_ontology, ax10);

                            // relate compound list to a member
                            OWLAxiom ax11 = factory.getOWLObjectPropertyAssertionAxiom(factory.getOWLObjectProperty(hasMember),
                                    compoundList, compoundListMember);
                            manager.addAxiom(mann_ontology, ax11);

                            // relate to gene id
                            OWLAxiom ax12 = factory.getOWLObjectPropertyAssertionAxiom(factory.getOWLObjectProperty(hasDBRef),
                                    compoundListMember, factory.getOWLNamedIndividual(IRI.create(uniprotURI + unirpotAcc)));
                            manager.addAxiom(mann_ontology, ax12);

                            // relate expression value
                            OWLAxiom ax13 = factory.getOWLObjectPropertyAssertionAxiom(factory.getOWLObjectProperty(hasQuality),
                                    compoundListMember, factory.getOWLNamedIndividual(IRI.create("http://purl.org/obo/owl/PATO#PATO_0000467")));
                            manager.addAxiom(mann_ontology, ax13);


                        }

                    }


                }
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }



        try {
            manager.saveOntology(mann_ontology, IRI.create("file:/Users/simon/Documents/e-lico/svn/trunk/software/ontologies/kupkb/vlahouGelLC.owl"));
        } catch (OWLOntologyStorageException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }


    }

    public static void main(String[] args) {

        new Vlahou2GelLC();



    }


}