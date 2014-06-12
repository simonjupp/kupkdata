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

import kupkb_experiments.KUPVocabulary;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.AnnotationValueShortFormProvider;
import org.semanticweb.owlapi.util.BidirectionalShortFormProviderAdapter;
import org.semanticweb.owlapi.util.SimpleShortFormProvider;
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
public class EuReGene2CSV extends DefaultHandler {

    String currentTag = "";
    String currentAnnotation = "";
    String currentGeneId = "";
    String currentAnatomy = "";

    Map<String, String> termToStrength;
    Map<String, String> emapToMao;

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
    String kupkbURI = "http://www.kupkb.org/data/kupkb/";
    String base = "http://www.kupkb.org/data/kupkb/experiment/euregene_";

    IRI mRNAISHAnalaysis = IRI.create("http://www.kupkb.org/data/kupkb/KUPKB_1000005");
    IRI analysisOf = IRI.create("http://www.kupkb.org/data/kupkb/analysisOf");
    IRI geneList = IRI.create("http://www.kupkb.org/data/kupkb/KUPKB_1000027");
    IRI geneListMember = IRI.create("http://www.kupkb.org/data/kupkb/KUPKB_1000032");
    IRI efoIRI = IRI.create("http://www.kupkb.org/data/kupkb/KUPKB_1000036");
    IRI has_bio_material = IRI.create("http://www.kupkb.org/data/kupkb/bioMaterial");
    IRI has_bio_condition = IRI.create("http://www.kupkb.org/data/kupkb/bioCondition");
    IRI has_role = IRI.create("http://www.kupkb.org/data/kupkb/hasAnnotationRole");
    IRI analyte = IRI.create("http://www.kupkb.org/data/kupo/KUPO_0300008");
    IRI normal = IRI.create("http://www.kupkb.org/data/kupo/KUPO_0300007");
    IRI adult = IRI.create("http://purl.org/obo/owl/PATO#PATO_0001701");
    IRI annotatedWith = IRI.create("http://www.kupkb.org/data/kupkb/annotatedWith");
    IRI produces = IRI.create("http://www.kupkb.org/data/kupkb/produces");
    IRI hasDBRef = IRI.create("http://www.kupkb.org/data/kupkb/hasDatabaseRef");
    IRI hasMember = IRI.create("http://www.kupkb.org/data/kupkb/hasMember");
    IRI hasStrength = IRI.create("http://www.kupkb.org/data/kupkb/hasExpression");



    BidirectionalShortFormProviderAdapter shortFrom;

    StringBuilder sb;

    OWLNamedIndividual experimentAssayType;

    public EuReGene2CSV () {


        mgi_to_geneid = new HashMap<String, Set<String>>();
        emapToMao = new HashMap<String, String>();

        parseMGI();
        parseEMAP2MAO();

        manager = OWLManager.createOWLOntologyManager();
        manager.setSilentMissingImportsHandling(true);
        factory = manager.getOWLDataFactory();
        try {
            mouse_anatomy = manager.loadOntology(IRI.create("file:/Users/simon/Documents/e-lico/svn/trunk/Public/kupo/imports/mao.owl"));
            manager.loadOntology(IRI.create("file:/Users/simon/Documents/e-lico/svn/trunk/Public/kupo/kupo-emap.owl"));
            manager.loadOntology(IRI.create("file:/Users/simon/Documents/e-lico/svn/trunk/Public/kupkb/kupkb.owl"));
            euregene_ontology = manager.createOntology(IRI.create("http://www.kupkb.org/data/kupkb/experiment/EUREGENE"));

            List<OWLAnnotationProperty> rdfsLabelList = new ArrayList<OWLAnnotationProperty>();
            rdfsLabelList.add(factory.getOWLAnnotationProperty(IRI.create(rdfsBaseURI + "label")));

            AnnotationValueShortFormProvider anno = new AnnotationValueShortFormProvider(rdfsLabelList, new HashMap(), manager );
            shortFrom = new BidirectionalShortFormProviderAdapter(manager, manager.getOntologies(), anno);

            experimentAssayType = factory.getOWLNamedIndividual(IRI.create(base + "assay"));
            OWLAxiom ax = factory.getOWLClassAssertionAxiom(factory.getOWLClass(IRI.create("http://www.kupkb.org/data/kupkb/KUPKB_1000019")), experimentAssayType);
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
                processData();
            } catch (ParserConfigurationException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (SAXException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }

        try {
//            manager.saveOntology(euregene_ontology, IRI.create("file:/Users/simon/dev/kupo/kupdb/version2/experiments/euregene/euregene.owl"));
            manager.saveOntology(euregene_ontology, IRI.create("file:/Users/simon/Dropbox/JuppKlein/KUP/experiment_ontologies/euregene.owl"));        } catch (OWLOntologyStorageException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

    }


    public void processData() {

        for (String term : termToStrength.keySet()) {

            String name = term.substring(term.lastIndexOf(".") + 1);
            OWLEntity e = shortFrom.getEntity(name);

            if (e == null) {

                if (emapToMao.containsKey(name)) {
                    e = shortFrom.getEntity(emapToMao.get(name));

                    if (e != null) {
//                        System.out.println("emap term to mao found: " + term + "->" + e.getIRI().getFragment());

                    }
                }

            }

            if (e != null) {

                SimpleShortFormProvider spf = new SimpleShortFormProvider();

                System.out.println(" -> " + e.getIRI().toString());
                IRI analysisIDIRI = IRI.create(base + spf.getShortForm(e) + "_analysis");
                IRI compoundListIRI = IRI.create(base + spf.getShortForm(e) + "_list");
                IRI experimentalFactorIRI = IRI.create(base + spf.getShortForm(e) + "_annotation");

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

                    OWLAxiom ax5a = factory.getOWLObjectPropertyAssertionAxiom(factory.getOWLObjectProperty(KUPVocabulary.TAXONOMY_PROPERTY.getIRI()),
                            experimentFactor, factory.getOWLNamedIndividual(IRI.create("http://bio2rdf.org/taxon:10090")));

                    OWLAxiom ax6 = factory.getOWLObjectPropertyAssertionAxiom(factory.getOWLObjectProperty(KUPVocabulary.HAS_QUALITY.getIRI()),
                            experimentFactor, factory.getOWLNamedIndividual(adult));

                    OWLAxiom ax6a = factory.getOWLObjectPropertyAssertionAxiom(factory.getOWLObjectProperty(KUPVocabulary.HAS_BIO_CONDITION.getIRI()),
                            experimentFactor, factory.getOWLNamedIndividual(normal));

                    OWLAxiom ax7 = factory.getOWLObjectPropertyAssertionAxiom(factory.getOWLObjectProperty(has_role),
                            experimentFactor, factory.getOWLNamedIndividual(analyte));
                    manager.addAxiom(euregene_ontology, ax5);
                    manager.addAxiom(euregene_ontology, ax5a);
                    manager.addAxiom(euregene_ontology, ax6);
                    manager.addAxiom(euregene_ontology, ax6a);
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
                        IRI compoundListMemberIRI = IRI.create(base + spf.getShortForm(e) + "_listmember_" + geneid);
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
//                        System.err.println("looking up term: " + termToStrength.get(term));
                            OWLAxiom ax13 = factory.getOWLObjectPropertyAssertionAxiom(factory.getOWLObjectProperty(hasStrength),
                                    compoundListMember, factory.getOWLNamedIndividual(IRI.create(termToStrength.get(term))));
                            manager.addAxiom(euregene_ontology, ax13);
                    }
                    
                }
            }
        }
    }

    public void parseEMAP2MAO () {
        try {
            BufferedReader input =  new BufferedReader(new FileReader(new File("/Users/simon/dev/kupo/kupdb/version2/experiments/euregene/emap_MAO_Mappings-v2.txt")));
            String line = null; //not declared within while loop
            while (( line = input.readLine()) != null){
                if (!line.isEmpty()) {

                    String [] split = line.split("\t");
                    if (split.length >= 2) {
                        if (!split[1].isEmpty()) {
                            System.err.println("putting:" + split[0] + "->" + split[1]);
                            emapToMao.put(split[0], split[1]);
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
            else if (currentTag.equals("strength")){
//                System.err.println(currentAnatomy + " -> " + sb.toString());
                if (sb.toString().equals("not detected")) {
                    termToStrength.put(currentAnatomy, "http://www.kupkb.org/data/kupkb/KUPKB_1000068");
                }
                else if (sb.toString().equals("possible")) {
                    termToStrength.put(currentAnatomy, "http://www.kupkb.org/data/kupkb/KUPKB_1000069");
                }
                else if (sb.toString().equals("weak")) {
                    termToStrength.put(currentAnatomy, "http://www.kupkb.org/data/kupkb/KUPKB_1000064");
                }
                else if (sb.toString().equals("strong")) {
                    termToStrength.put(currentAnatomy, "http://www.kupkb.org/data/kupkb/KUPKB_1000066");
                }
                else if (sb.toString().equals("present")) {
                    termToStrength.put(currentAnatomy, "http://www.kupkb.org/data/kupkb/KUPKB_1000063");
                }
                else if (sb.toString().equals("medium")) {
                    termToStrength.put(currentAnatomy, "http://www.kupkb.org/data/kupkb/KUPKB_1000065");
                }
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
        new EuReGene2CSV();
    }

}
