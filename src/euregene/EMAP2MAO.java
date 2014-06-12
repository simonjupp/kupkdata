package euregene;/*
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
import org.semanticweb.owlapi.util.AnnotationValueShortFormProvider;
import org.semanticweb.owlapi.util.BidirectionalShortFormProviderAdapter;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.*;
import java.util.*;

/**
 * Author: Simon Jupp<br>
 * Date: Nov 4, 2010<br>
 * The University of Manchester<br>
 * Bio-Health Informatics Group<br>
 */
public class EMAP2MAO extends DefaultHandler {

    String currentTag = "";
    String currentAnnotation = "";
    String currentGeneId = "";
    String currentAnatomy = "";

    Map<String, String> termToStrength;

    boolean stage = false;

    Map<String, Set<String>> mgi_to_geneid;

    OWLOntologyManager manager;
    OWLOntology mouse_anatomy;
    OWLOntology euregene_ontology;
    OWLDataFactory factory;

    String bio2rdfURI = "http://bio2rdf.org/geneid:";
    String rdfBaseURI = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
    String rdfsBaseURI = "http://www.w3.org/2000/01/rdf-schema#";
    String taxURI = "http://bio2rdf.org/ns/taxonomy:Taxonomy_of_species";
    String kupkbURI = "http://www.kupkb.org/data/kupkb/v2/";
    String base = "http://www.kupkb.org/data/kupkb/experiment/euregene_";

    IRI mRNAISHAnalaysis = IRI.create("http://www.kupkb.org/data/kupkb/v2/InSituGeneExpressionAnalysis");
    IRI analysisOf = IRI.create("http://www.kupkb.org/data/kupkb/v2/analysisOf");
    IRI geneList = IRI.create("http://www.kupkb.org/data/kupkb/v2/GeneList");
    IRI geneListMember = IRI.create("http://www.kupkb.org/data/kupkb/v2/GeneListMember");
    IRI efoIRI = IRI.create("http://www.ebi.ac.uk/efo/EFO_0000001");
    IRI has_bio_material = IRI.create("http://www.kupkb.org/data/kupkb/v2/bioMaterial");
    IRI has_bio_condition = IRI.create("http://www.kupkb.org/data/kupkb/v2/bioCondition");
    IRI has_role = IRI.create("http://purl.obolibrary.org/obo/OBI_0000316");
    IRI analyte = IRI.create("http://www.kupkb.org/data/kupkb/v2/analyte");
    IRI adultMouse = IRI.create("http://www.kupkb.org/data/kupkb/v2/NormalAdultMouse");
    IRI annotatedWith = IRI.create("http://www.kupkb.org/data/v2/kupkb/annotated_with");
    IRI produces = IRI.create("http://www.kupkb.org/data/kupkb/v2/produces");
    IRI hasDBRef = IRI.create("http://www.kupkb.org/data/kupkb/v2/hasDatabaseRef");
    IRI hasMember = IRI.create("http://www.kupkb.org/data/kupkb/v2/hasMember");
    IRI hasStrength = IRI.create("http://www.kupkb.org/data/kupkb/v2/hasStrength");



    BidirectionalShortFormProviderAdapter shortFrom;

    StringBuilder sb;

    OWLNamedIndividual experimentAssayType;

    public class MyVisitor implements OWLAnnotationValueVisitor {

        public void visit(IRI iri) {
        }

        public void visit(OWLAnonymousIndividual individual) {
        }

        public void visit(OWLLiteral literal) {
            System.out.println(literal.getLiteral());
        }
    }

    public EMAP2MAO() {


        mgi_to_geneid = new HashMap<String, Set<String>>();
//        parseMGI();

        manager = OWLManager.createOWLOntologyManager();
        factory = manager.getOWLDataFactory();
        try {
            mouse_anatomy = manager.loadOntology(IRI.create("file:/Users/simon/dev/kupo/workflow/populous/ontologies/mao.owl"));

            for (OWLClass cls : mouse_anatomy.getClassesInSignature()) {
                for (OWLAnnotation anno : cls.getAnnotations(mouse_anatomy, factory.getOWLAnnotationProperty(IRI.create(rdfsBaseURI + "label")))) {
                    anno.getValue().accept(new MyVisitor());
                }
            }
            System.exit(0);
            manager.loadOntology(IRI.create("file:/Users/simon/Documents/e-lico/svn/trunk/software/ontologies/kupkb/kupkp-dev.owl"));
            euregene_ontology = manager.createOntology(IRI.create("http://www.kupkb.org/data/kupkb/experiment/EUREGENE"));

            List<OWLAnnotationProperty> rdfsLabelList = new ArrayList<OWLAnnotationProperty>();
            rdfsLabelList.add(factory.getOWLAnnotationProperty(IRI.create(rdfsBaseURI + "label")));

            AnnotationValueShortFormProvider anno = new AnnotationValueShortFormProvider(rdfsLabelList, new HashMap(), manager );
            shortFrom = new BidirectionalShortFormProviderAdapter(manager, manager.getOntologies(), anno);

            experimentAssayType = factory.getOWLNamedIndividual(IRI.create(base + "assay"));
            OWLAxiom ax = factory.getOWLClassAssertionAxiom(factory.getOWLClass(IRI.create("http://www.kupkb.org/data/kupkb/v2/mRNAISHAssay")), experimentAssayType);
            manager.addAxiom(euregene_ontology, ax);

        } catch (OWLOntologyCreationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }


        SAXParserFactory spf = SAXParserFactory.newInstance();
        //get a new instance of parser
        SAXParser sp = null;

        //parse the file and also register this class for call backs

        // ERG_1877.xml

        File dataFolder = new File("/Users/simon/Documents/e-lico/data/EuReGene/AllAnnotations/");
        File [] listOfFiles = dataFolder.listFiles();
        for (int i = 0; i < listOfFiles.length; i++) {
//        for (int i = 0; i < 10; i++) {
            termToStrength = new HashMap<String, String>();
//            System.out.println("Parsing:" + listOfFiles[i].toString());
            try {
                sp = spf.newSAXParser();
                sp.parse(listOfFiles[i], this);
//                processData();
            } catch (ParserConfigurationException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (SAXException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }

//        try {
//            manager.saveOntology(euregene_ontology, IRI.create("file:/Users/simon/dev/kupo/kupdb/version2/experiments/euregene/euregene.owl"));
//        } catch (OWLOntologyStorageException e) {
//            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//        }

    }


    public void processData() {

        for (String term : termToStrength.keySet()) {

            String name = term.substring(term.lastIndexOf(".") + 1);
            OWLEntity e = shortFrom.getEntity(name);
            if (e != null) {

//                System.out.println(" -> " + e.getIRI().toString());
                IRI analysisIDIRI = IRI.create(base + e.getIRI().getFragment() + "_analysis");
                IRI compoundListIRI = IRI.create(base + e.getIRI().getFragment() + "_list");
                IRI experimentalFactorIRI = IRI.create(base + e.getIRI().getFragment() + "_factor");

                // if it doesn't exist create all the necessary gubbins
                if (!euregene_ontology.containsEntityInSignature(analysisIDIRI)) {
                    // type the analysis type
                    OWLIndividual analysisID = factory.getOWLNamedIndividual(analysisIDIRI);
                    OWLAxiom ax = factory.getOWLClassAssertionAxiom(factory.getOWLClass(mRNAISHAnalaysis),
                            analysisID);

                    manager.addAxiom(euregene_ontology, ax);

                    // relate to an experiment type
                    OWLAxiom ax2 = factory.getOWLObjectPropertyAssertionAxiom(factory.getOWLObjectProperty(analysisOf),
                            analysisID, experimentAssayType);
                    manager.addAxiom(euregene_ontology, ax2);

                    // relate to factor
                    OWLIndividual experimentFactor = factory.getOWLNamedIndividual(experimentalFactorIRI);
                    OWLAxiom ax3 = factory.getOWLClassAssertionAxiom(factory.getOWLClass(efoIRI),
                            experimentFactor);
                    manager.addAxiom(euregene_ontology, ax3);

                    OWLAxiom ax4 = factory.getOWLObjectPropertyAssertionAxiom(factory.getOWLObjectProperty(annotatedWith),
                            analysisID, experimentFactor);
                    manager.addAxiom(euregene_ontology, ax4);

                    // describe factor
                    OWLAxiom ax5 = factory.getOWLObjectPropertyAssertionAxiom(factory.getOWLObjectProperty(has_bio_material),
                            experimentFactor, factory.getOWLNamedIndividual(e.getIRI()));

                    OWLAxiom ax6 = factory.getOWLObjectPropertyAssertionAxiom(factory.getOWLObjectProperty(has_bio_condition),
                            experimentFactor, factory.getOWLNamedIndividual(adultMouse));

                    OWLAxiom ax7 = factory.getOWLObjectPropertyAssertionAxiom(factory.getOWLObjectProperty(has_role),
                            experimentFactor, factory.getOWLNamedIndividual(analyte));
                    manager.addAxiom(euregene_ontology, ax5);
                    manager.addAxiom(euregene_ontology, ax6);
                    manager.addAxiom(euregene_ontology, ax7);


                    // create the compound list
                    OWLIndividual compoundList = factory.getOWLNamedIndividual(compoundListIRI);
                    OWLAxiom ax8 = factory.getOWLClassAssertionAxiom(factory.getOWLClass(geneList),
                            compoundList);
                    manager.addAxiom(euregene_ontology, ax8);
                    // add produces relation
                    OWLAxiom ax9 = factory.getOWLObjectPropertyAssertionAxiom(factory.getOWLObjectProperty(produces),
                            analysisID, compoundList);
                    manager.addAxiom(euregene_ontology, ax9);

                }

                // create the compound list member
                if (mgi_to_geneid.containsKey(currentGeneId)) {
                    for (String geneid : mgi_to_geneid.get(currentGeneId)) {
                        IRI compoundListMemberIRI = IRI.create(base + e.getIRI().getFragment() + "_listmember_" + geneid);
                        OWLIndividual compoundListMember = factory.getOWLNamedIndividual(compoundListMemberIRI);
                        OWLAxiom ax10 = factory.getOWLClassAssertionAxiom(factory.getOWLClass(geneListMember),
                                compoundListMember);
                        manager.addAxiom(euregene_ontology, ax10);

                        // relate compound list to a member
                        OWLAxiom ax11 = factory.getOWLObjectPropertyAssertionAxiom(factory.getOWLObjectProperty(hasMember),
                                factory.getOWLNamedIndividual(compoundListIRI), compoundListMember);
                        manager.addAxiom(euregene_ontology, ax11);

                        // relate to gene id
                        OWLAxiom ax12 = factory.getOWLObjectPropertyAssertionAxiom(factory.getOWLObjectProperty(hasDBRef),
                                compoundListMember, factory.getOWLNamedIndividual(IRI.create(bio2rdfURI + geneid)));
                        manager.addAxiom(euregene_ontology, ax12);

                        // relate expression value
                        OWLAxiom ax13 = factory.getOWLObjectPropertyAssertionAxiom(factory.getOWLObjectProperty(hasStrength),
                                compoundListMember, factory.getOWLNamedIndividual(IRI.create(kupkbURI + termToStrength.get(term).replace(" ", "_"))));
                        manager.addAxiom(euregene_ontology, ax13);
                    }

                }
            }
        }
    }

    public void parseMGI() {

        try {
            BufferedReader input =  new BufferedReader(new FileReader(new File("/Users/simon/dev/kupo/kupdb/version2/experiments/euregene/mgi_to_geneid.txt")));
            String line = null; //not declared within while loop
            while (( line = input.readLine()) != null){
                if (!line.isEmpty()) {

                    String [] split = line.split("\t");
//                    System.out.println("mgi:"  + split[0]);
//                    System.out.println("entrez:"  + split[1]);
                    if (!split[1].isEmpty()) {

                        if (mgi_to_geneid.containsKey(split[0])) {
                            if (!mgi_to_geneid.get(split[0]).contains(split[1])) {
                                mgi_to_geneid.get(split[0]).add(split[1]);
                            }
                        }
                        else {
                            Set<String> sp = new HashSet<String>();
                            sp.add(split[1]);
                            mgi_to_geneid.put(split[0], sp);
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

    @Override
    public void startElement(String uri, String localName, String qName,
		Attributes attributes) throws SAXException {
        super.startElement(uri, localName, qName, attributes);    //To change body of overridden methods use File | Settings | File Templates.

//        System.out.println("qname: " + qName);
        currentTag = qName;
        sb = new StringBuilder();

    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
//        super.endElement(s, s1, qName);    //To change body of overridden methods use File | Settings | File Templates.
//        System.out.println("\n\n");

        currentTag = qName;

        if (currentTag.equals("stage")) {
            if (sb.toString().equals("TS28")) {
                stage = true;
            }
            else {
                stage = false;
            }
        }

        if (stage) {

            if (currentTag.equals("annotation_id")) {
                // create new individual
                currentAnnotation = sb.toString();
//                System.out.println(sb.toString());
            }
            else if (currentTag.equals("gene_id")) {
                currentGeneId = sb.toString();
//                if (mgi_to_geneid.containsKey(sb.toString())) {
//                    for (String s : mgi_to_geneid.get(sb.toString())) {
//                        System.out.println(sb.toString() + " --> " + s);
//                    }
//                }
            }
            else if (currentTag.equals("cmpt_name")) {
                currentAnatomy = sb.toString();
            }
            else if (currentTag.equals("EMAP")) {
                String name = currentAnatomy.substring(currentAnatomy.lastIndexOf(".") + 1);

//                System.out.println(name);
            }
            else if (currentTag.equals("strength")){
                termToStrength.put(currentAnatomy, sb.toString());
            }

        }

    }



    @Override
    public void characters(char[] chars, int i, int i1) throws SAXException {
        super.characters(chars, i, i1);    //To change body of overridden methods use File | Settings | File Templates.
        String t = new String(chars, i, i1);
        sb.append(t);


    }

    public static void main(String[] args) {
        new EMAP2MAO();
    }

}