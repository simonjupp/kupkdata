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

import org.apache.commons.lang.StringUtils;
import org.bridgedb.*;
import org.bridgedb.bio.BioDataSource;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.*;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Author: Simon Jupp<br>
 * Date: Feb 9, 2011<br>
 * The University of Manchester<br>
 * Bio-Health Informatics Group<br>
 *
 * This class converts a KUPExperiment object (see KUPExperiment.java)
 * and converts it into an RDF/XML OWL representation. These OWL files can then
 * be loaded into the KUPKB RDF triple store
 *
 */
public class OntologyBuilder {

    private KUPExperiment experiment;

    private OWLOntologyManager manager;
    private OWLDataFactory factory;
    private OWLOntology ontology;
    private SimpleShortFormProvider shortFormProvider;
    private BidirectionalShortFormProviderAdapter shortFormAdapter;
    private BidirectionalShortFormProviderAdapter shortFormAnnotationAdapter;
    private ShortFormProvider lcAnnoFp;
    private String taxonomy;

    private Map<String, String> speciesToDB;
    private Map<String, String> compoundToList;

    private AnnotationValueShortFormProvider annoSfp;

    private Map<String, String> miRNAid2Acc;

    private static String miRNAMappingFile = "./background_data/miRNAmapping.txt";

    private Set<String> expNames = new HashSet<String>();


    public OntologyBuilder() {
        manager = OWLManager.createOWLOntologyManager();
        factory = manager.getOWLDataFactory();
    }

    public void initialise () {
        shortFormProvider = new SimpleShortFormProvider();
        shortFormAdapter = new BidirectionalShortFormProviderAdapter(manager, manager.getOntologies(), shortFormProvider);

        annoSfp = new AnnotationValueShortFormProvider(
                Collections.singletonList(factory.getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_LABEL.getIRI())), new HashMap<OWLAnnotationProperty, List<String>>(), manager );

        lcAnnoFp = new ShortFormProvider() {

            public String getShortForm(OWLEntity owlEntity) {
                return annoSfp.getShortForm(owlEntity).toLowerCase();
            }

            public void dispose() {
            }
        };

//        shortFormAnnotationAdapter = new BidirectionalShortFormProviderAdapter(manager, manager.getOntologies(), annoSfp);
        shortFormAnnotationAdapter = new BidirectionalShortFormProviderAdapter(manager, manager.getOntologies(), lcAnnoFp);

    }

    public OWLOntologyManager getOWLManager() {
        return manager;
    }

    public OWLClass getRelatedClass(OWLClass cls, OWLObjectProperty prop) {

        RestrictionVisitor restrictionVisitor = new RestrictionVisitor(prop);

//        OWLOntology kupkb = manager.getOntology(IRI.create(KUPNamespaces.KUPKB_IRI.toString()));
        for (OWLOntology o : manager.getOntologies()) {
            for (OWLClassExpression oce : cls.getSuperClasses(o)) {

                oce.accept(restrictionVisitor);
            }
        }

        return restrictionVisitor.getRelatedClass();

    }


    public Map<String, String> getSpeciesToDBMap() {

        if (speciesToDB != null) {
            return speciesToDB;
        }
        speciesToDB = new HashMap<String, String>();
        speciesToDB.put("http://bio2rdf.org/taxon:9606", "Hs_Derby_20100601.bridge");
        speciesToDB.put("http://bio2rdf.org/taxon:10090", "Mm_Derby_20100601.bridge");
        speciesToDB.put("http://bio2rdf.org/taxon:10116", "Rn_Derby_20100601.bridge");
        speciesToDB.put("http://bio2rdf.org/taxon:9989", "Rn_Derby_20100601.bridge");
        speciesToDB.put("taxon:9606", "Hs_Derby_20100601.bridge");
        speciesToDB.put("taxon:10090", "Mm_Derby_20100601.bridge");
        speciesToDB.put("taxon:10116", "Rn_Derby_20100601.bridge");
        speciesToDB.put("taxon:9989", "Rn_Derby_20100601.bridge");
        speciesToDB.put("Human", "Hs_Derby_20100601.bridge");
        speciesToDB.put("Mouse", "Mm_Derby_20100601.bridge");
        speciesToDB.put("Rat", "Rn_Derby_20100601.bridge");
        speciesToDB.put("Rodent", "Rn_Derby_20100601.bridge");

        return speciesToDB;
    }


    public IDMapper getIDMApper () {

        try {
            System.out.println(taxonomy);
            Class.forName("org.bridgedb.rdb.IDMapperRdb");
            IDMapper idm = BridgeDb.connect("idmapper-pgdb:bridgedb-1.0.2/data/gene_database/" +  getSpeciesToDBMap().get(taxonomy));
            return idm;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IDMapperException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return null;
    }

    public void loadOntology (IRI ontologyIRI) throws OWLOntologyCreationException {

        System.out.println("Loading ontology " + ontologyIRI.toQuotedString() + "...");
        manager.setSilentMissingImportsHandling(true);
        manager.loadOntology(ontologyIRI);


    }

    public OWLClass getOWLClassFromString(String s) {

        if (s.toLowerCase().equals("up")) {
            return manager.getOWLDataFactory().getOWLClass(KUPVocabulary.UP.getIRI());
        }
        else if (s.toLowerCase().equals("down")) {
            return manager.getOWLDataFactory().getOWLClass(KUPVocabulary.DOWN.getIRI());
        }

        for (OWLOntology o : manager.getOntologies()) {
            if (o.containsClassInSignature(IRI.create(s))) {
                return manager.getOWLDataFactory().getOWLClass(IRI.create(s));
            }
        }

        OWLEntity e1 = shortFormAdapter.getEntity(s);
        if (e1 == null) {
            e1 = shortFormAnnotationAdapter.getEntity(s);
        }

        if (e1 == null) {
            e1 = shortFormAnnotationAdapter.getEntity(s.toLowerCase());
        }

        if (e1 instanceof OWLClass) {
            return e1.asOWLClass();
        }
        else if (e1 instanceof OWLNamedIndividual) {
            return manager.getOWLDataFactory().getOWLClass(e1.getIRI());
        }
        else {
            return null;
        }
    }

    public OWLNamedIndividual getOWLIndividualFromString(String s) {

        OWLEntity e1 = shortFormAdapter.getEntity(s);
        if (e1 == null) {
            e1 = shortFormAnnotationAdapter.getEntity(s);
        }

        if (e1 instanceof OWLNamedIndividual) {
            return e1.asOWLNamedIndividual();
        }
        else {
            return null;
        }
    }

    public Set<String> getAllNames() {
        return expNames;
    }

    public OWLOntology generateOWL(KUPExperiment exp) {

        this.experiment = exp;

        expNames.add(exp.getExperimentID());

        Set<OWLAxiom> axioms = new HashSet<OWLAxiom>();

        try {
            ontology = manager.createOntology(IRI.create(experiment.getBaseOntologyURI()));
            System.out.println("New ontology created: " + ontology.getOntologyID().toString());
        }
        catch (OWLOntologyCreationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }


        // create the assay individual and type it

        OWLNamedIndividual experimentAssay = factory.getOWLNamedIndividual(IRI.create(experiment.getBaseOntologyURI() + "_assay"));

        System.err.println("assay description:" + experiment.getAssayDescription());
        if (experiment.getAssayDescription() != null) {
            OWLAnnotation expannotation = factory.getOWLAnnotation(
                    factory.getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_COMMENT.getIRI()),
                    factory.getOWLLiteral(experiment.getAssayDescription()));
            axioms.add(factory.getOWLAnnotationAssertionAxiom(experimentAssay.getIRI(), expannotation));
        }

//            OWLClass assayType = factory.getOWLClass(IRI.create(kupkb_experiments.KUPNamespaces.KUPKB + experiment.getAssayType()));
        OWLClass c1 = getOWLClassFromString(experiment.getAssayType());
        if (c1 == null) {
            System.err.println("Program terminated, could not resolve assay type for :" + experiment.getAssayType());
            System.exit(0);
        }

        OWLAxiom ax = factory.getOWLClassAssertionAxiom(c1, experimentAssay);
        axioms.add(ax);
        System.out.println("Creating assay type axiom: " + ax.toString());

        for (KUPAnalysis analysis : experiment.getAnalysis()) {

            // create the analysis individual
            OWLIndividual analysisID = factory.getOWLNamedIndividual(IRI.create(analysis.getAnalysisID()));
//                OWLClass analysisType = factory.getOWLClass(IRI.create(kupkb_experiments.KUPNamespaces.KUPKB + experiment.getAnalysisType()));

            OWLClass analysisType = getOWLClassFromString(experiment.getAnalysisType());
            if (analysisType == null) {
                System.err.println("Program terminated, could not resolve analysis type for :" + experiment.getAnalysisType());
                System.exit(0);
            }

            OWLAxiom ax1 = factory.getOWLClassAssertionAxiom(analysisType, analysisID);
            axioms.add(ax1);
            System.out.println("Creating analysis type axiom: " + ax1.toString());

            OWLAxiom ax2 = factory.getOWLObjectPropertyAssertionAxiom(
                    factory.getOWLObjectProperty(KUPVocabulary.ANALYSIS_OF.getIRI()),
                    analysisID, experimentAssay);
            axioms.add(ax2);
            System.out.println("Creating analysis to assay relation axiom: " + ax2.toString());


            // get the annotations for this analysis
            for (KUPAnnotation annotation : analysis.getAnnotations()) {

                // relate to annotation
                OWLIndividual experimentFactor = factory.getOWLNamedIndividual(IRI.create(annotation.getAnnotationID()));
                OWLClass annotationType = factory.getOWLClass(KUPVocabulary.KUPANNOTATION.getIRI());

                OWLAxiom ax3 = factory.getOWLClassAssertionAxiom(annotationType,
                        experimentFactor);
                axioms.add(ax3);
                System.out.println("Creating annotation type axiom: " + ax3.toString());

                OWLAxiom ax4 = factory.getOWLObjectPropertyAssertionAxiom(factory.getOWLObjectProperty(KUPVocabulary.ANNOTATED_WITH.getIRI()),
                        analysisID, experimentFactor);
                axioms.add(ax4);
                System.out.println("Creating analysis to assay annotation axiom: " + ax4.toString());

                // pre analytical technuique
                if (experiment.getPreAnalyticalTechnuique() != null) {
                    OWLClass cls = getOWLClassFromString(experiment.getPreAnalyticalTechnuique());

                    if (cls == null) {
                        System.err.println("Program terminated, could not resolve pre analytical technique for :" + experiment.getPreAnalyticalTechnuique());
                        System.exit(0);
                    }

                    OWLAxiom ax4a = factory.getOWLObjectPropertyAssertionAxiom(factory.getOWLObjectProperty(KUPVocabulary.PRE_AN_TECH.getIRI()),
                            experimentFactor, factory.getOWLNamedIndividual(cls.getIRI()));
                    axioms.add(ax4a);
                    System.out.println("Creating pre-analytical technique : " + ax4a.toString());
                }


                // describe annotation
                for (String bioMaterial : annotation.getBioMaterial()) {
                    System.out.println("looking up: " + bioMaterial);
                    OWLClass entity = getOWLClassFromString(bioMaterial);


                    if (entity != null) {
                        // we need to pun this individual
                        OWLAxiom tmpAx = factory.getOWLObjectPropertyAssertionAxiom(factory.getOWLObjectProperty(KUPVocabulary.HAS_BIO_MATERERIAL.getIRI()),
                                experimentFactor, factory.getOWLNamedIndividual(entity.getIRI()));
                        System.out.println("Creating bioMaterial annotation axiom: " + tmpAx.toString());
                        axioms.add(tmpAx);
                    }
                    else if (bioMaterial.startsWith("MA")) {
                        // we need to pun this individual
                        OWLAxiom tmpAx = factory.getOWLObjectPropertyAssertionAxiom(factory.getOWLObjectProperty(KUPVocabulary.HAS_BIO_MATERERIAL.getIRI()),
                                experimentFactor, factory.getOWLNamedIndividual(IRI.create(KUPNamespaces.MAO + bioMaterial)));
                        System.out.println("Creating bioMaterial annotation axiom: " + tmpAx.toString());
                        axioms.add(tmpAx);
                    }
                    else if (bioMaterial.startsWith("CL")) {
                        // we need to pun this individual
                        OWLAxiom tmpAx = factory.getOWLObjectPropertyAssertionAxiom(factory.getOWLObjectProperty(KUPVocabulary.HAS_BIO_MATERERIAL.getIRI()),
                                experimentFactor, factory.getOWLNamedIndividual(IRI.create(KUPNamespaces.CTO + bioMaterial)));
                        System.out.println("Creating bioMaterial annotation axiom: " + tmpAx.toString());
                        axioms.add(tmpAx);
                    }
                    else if (entity == null) {
                        System.err.println("Program terminated, could not resolve bio material for :" + bioMaterial);
                        System.exit(0);
                    }
                    else {
                        System.err.println("Something went wrong at: " + bioMaterial);
                        System.exit(0);
                    }


                }

                for (String disease : annotation.getHasDisease()) {
                    OWLClass entity = getOWLClassFromString(disease);
                    if (entity != null) {
                        OWLAxiom tmpAx = factory.getOWLObjectPropertyAssertionAxiom(factory.getOWLObjectProperty(KUPVocabulary.HAS_DISEASE.getIRI()),
                                experimentFactor, factory.getOWLNamedIndividual(entity.getIRI()));
                        System.out.println("Creating disease annotation axiom: " + tmpAx.toString());
                        axioms.add(tmpAx);
                    }
                    else {
                        if (!disease.equals("")) {
                            System.err.println("Something went wrong creating: " + disease);
                            System.exit(0);
                        }
                    }
                }

                for (String quality : annotation.getQualities()) {
                    OWLClass entity = getOWLClassFromString(quality);
                    if (entity != null) {
                        OWLAxiom tmpAx = factory.getOWLObjectPropertyAssertionAxiom(factory.getOWLObjectProperty(KUPVocabulary.HAS_QUALITY.getIRI()),
                                experimentFactor, factory.getOWLNamedIndividual(entity.getIRI()));
                        System.out.println("Creating qualities annotation axiom: " + tmpAx.toString());
                        axioms.add(tmpAx);
                    }
                    else {
                        if (!quality.equals("")) {
                            System.err.println("Something went wrong creating: " + quality);
                            System.exit(0);
                        }
                    }

                }

                // bio condition
                if (!annotation.getCondition().equals("")) {
                    OWLClass cs = getOWLClassFromString(annotation.getCondition());
                    if (cs == null) {
                        System.err.println("Something went wrong creating: " + annotation.getCondition());
                        System.exit(0);
                    }
                    OWLAxiom ax6 = factory.getOWLObjectPropertyAssertionAxiom(factory.getOWLObjectProperty(KUPVocabulary.HAS_BIO_CONDITION.getIRI()),
                            experimentFactor, factory.getOWLNamedIndividual(cs.getIRI()));
                    System.out.println("Creating condition axiom: " + ax6.toString());
                    axioms.add(ax6);
                }

                // role
                if (!annotation.getRole().equals("")) {
                    OWLClass cls = getOWLClassFromString(annotation.getRole());
                    if (cls == null) {
                        System.err.println("Something went wrong creating: " + annotation.getRole());
                        System.exit(0);
                    }

                    OWLAxiom ax7 = factory.getOWLObjectPropertyAssertionAxiom(factory.getOWLObjectProperty(KUPVocabulary.HAS_ROLE.getIRI()),
                            experimentFactor, factory.getOWLNamedIndividual(cls.getIRI()));
                    System.out.println("Creating role axiom: " + ax7.toString());
                    axioms.add(ax7);
                }

                // taxonomy
                if (!annotation.getTaxonomy().equals("")) {
                    OWLClass cls = getOWLClassFromString(annotation.getTaxonomy());
                    if (cls == null) {
                        cls = factory.getOWLClass(IRI.create(KUPNamespaces.Bio2RDFTAXON + annotation.getTaxonomy()));
                    }
                    OWLAxiom ax8 = factory.getOWLObjectPropertyAssertionAxiom(factory.getOWLObjectProperty(KUPVocabulary.TAXONOMY_PROPERTY.getIRI()),
                            experimentFactor, factory.getOWLNamedIndividual(cls.getIRI()));
                    taxonomy = annotation.getTaxonomy();
                    System.out.println("Creating taxonomy axiom: " + ax8.toString());
                    axioms.add(ax8);
                }


            }

            // create the compound list
            for (CompoundList comList : analysis.getCompoundList()) {

                OWLNamedIndividual compoundList = factory.getOWLNamedIndividual(IRI.create(comList.getCompoundListID()));
                OWLClass listType = getOWLClassFromString(experiment.getListType());

                OWLAxiom ax10 = factory.getOWLClassAssertionAxiom(listType,
                        compoundList);
                System.out.println("Creating compound list axiom: " + ax10.toString());
                axioms.add(ax10);

                // add produces relation
                OWLAxiom ax11 = factory.getOWLObjectPropertyAssertionAxiom(factory.getOWLObjectProperty(KUPVocabulary.PRODUCES.getIRI()),
                        analysisID, compoundList);
                System.out.println("Creating compound list relation axiom: " + ax11.toString());
                axioms.add(ax11);

                long randomint = System.currentTimeMillis();
                for (CompoundList.ListMember member  : comList.getMembers()) {

                    IRI id = getCompoundIDasIRI(member);
                    // work out what the id is
                    if (id != null) {
                        String compoundIdShortForm = shortFormProvider.getShortForm(factory.getOWLClass(id));

                        OWLObjectProperty hasMemberP =  factory.getOWLObjectProperty(KUPVocabulary.HAS_MEMBER.getIRI());
                        OWLIndividual compoundListMember = factory.getOWLNamedIndividual(IRI.create(experiment.getBaseOntologyURI() + "_listmember_" + randomint + "_" + compoundIdShortForm));
                        OWLClass listMemberClass = getRelatedClass(listType,hasMemberP);
                        OWLAxiom ax13 = factory.getOWLClassAssertionAxiom(listMemberClass, compoundListMember);
                        System.out.println("Creating compound list member axiom: " + ax13.toString());
                        axioms.add(ax13);

                        // relate compound list to a member
                        OWLAxiom ax14 = factory.getOWLObjectPropertyAssertionAxiom(hasMemberP,
                                compoundList, compoundListMember);
                        System.out.println("Creating compound list member relation axiom: " + ax14.toString());
                        axioms.add(ax14);

                        // relate to gene id
                        OWLAxiom ax15 = factory.getOWLObjectPropertyAssertionAxiom(factory.getOWLObjectProperty(KUPVocabulary.HAS_DBREF.getIRI()),
                                compoundListMember, factory.getOWLNamedIndividual(id));
                        System.out.println("Creating compound list member relation to compound axiom: " + ax15.toString());
                        axioms.add(ax15);

                        // get expression
                        if (member.getExpressionStrength() != null) {

                            if (!member.getExpressionStrength().equals("")) {
                                // hack to handle lowercase up and down

                                String expressionString = member.getExpressionStrength();
                                if (expressionString.equals("up") || expressionString.equals("down")) {
                                    expressionString = StringUtils.capitalize(expressionString);
                                }
                                OWLEntity expression = getOWLClassFromString(expressionString);
                                if (expression == null) {
                                    System.err.println("ERROR: Couldn't find class for " + expressionString);
                                    System.exit(0);
                                }
                                else {
                                    OWLAxiom ax16 = factory.getOWLObjectPropertyAssertionAxiom(factory.getOWLObjectProperty(KUPVocabulary.HAS_EXPRESSION.getIRI()),
                                            compoundListMember, factory.getOWLNamedIndividual(expression.getIRI()));
                                    System.out.println("Adding has Expression axiom: " + ax16.toString());
                                    axioms.add(ax16);

                                }

                            }
                        }

                        if (member.getDifferential() != null) {
                            if (!member.getDifferential().equals("")) {
                                OWLEntity expression = getOWLClassFromString(member.getDifferential());
                                if (expression == null) {
                                    System.err.println("ERROR: Couldn't find class for " + member.getDifferential());
                                    System.exit(0);
                                }
                                else {
                                    OWLAxiom ax17 = factory.getOWLObjectPropertyAssertionAxiom(factory.getOWLObjectProperty(KUPVocabulary.HAS_EXPRESSION.getIRI()),
                                            compoundListMember, factory.getOWLNamedIndividual(expression.getIRI()));
                                    System.out.println("Adding has Expression axiom: " + ax17.toString());
                                    axioms.add(ax17);

                                }
                            }
                        }

                        if (member.getRatio() != null) {
                            if (!member.getRatio().equals("")) {
                                OWLAxiom ax18 = factory.getOWLDataPropertyAssertionAxiom(factory.getOWLDataProperty(KUPVocabulary.FOLD_CHANGE.getIRI()),
                                        compoundListMember, member.getRatio());
                                System.out.println("Adding ratio axiom: " + ax18.toString());
                                axioms.add(ax18);
                            }
                        }

                        if (member.getPValue() != null) {
                            if (!member.getPValue().equals("")) {
                                OWLAxiom ax19 = factory.getOWLDataPropertyAssertionAxiom(factory.getOWLDataProperty(KUPVocabulary.P_VALUE.getIRI()),
                                        compoundListMember, member.getPValue());
                                System.out.println("Adding pValue axiom: " + ax19.toString());
                                axioms.add(ax19);
                            }
                        }

                        if (member.getFdrValue() != null) {
                            if (!member.getFdrValue().equals("")) {
                                OWLAxiom ax20 = factory.getOWLDataPropertyAssertionAxiom(factory.getOWLDataProperty(KUPVocabulary.FDR_VALUE.getIRI()),
                                        compoundListMember, member.getFdrValue());
                                System.out.println("Adding fdr axiom: " + ax20.toString());
                                axioms.add(ax20);
                            }
                        }


                    }


                }
            }
        }

        manager.addAxioms(ontology, axioms);

        return ontology;
    }

    private IRI getCompoundIDasIRI(CompoundList.ListMember member) {


        if (member.getGeneId() != null) {

            if (!member.getGeneId().equals(""))
                return IRI.create(KUPNamespaces.GENEIDPREFIX + member.getGeneId());

        }

        if (member.getGeneSymbol() != null) {
            if (!member.getGeneSymbol().equals("")) {
                String sT = getGeneId(member.getGeneSymbol());
                if (sT != null) {
                    if (!sT.equals(""))
                        return IRI.create(KUPNamespaces.GENEIDPREFIX + sT);
                }
            }


        }

        if (member.getUniprotID() != null) {
            if (!member.getUniprotID().equals(""))
                return IRI.create(KUPNamespaces.UNIPROTURI + member.getUniprotID());

        }

        if (member.getHmdbid() != null) {
            if (!member.getHmdbid().equals(""))
                return IRI.create(KUPNamespaces.HMDBURI + member.getHmdbid());

        }

        if (member.getMicrocosmid() != null) {
            String acc = getmiRNAAccession(member.getMicrocosmid());
            if (!acc.equals(""))
                return IRI.create(KUPNamespaces.MIRBASE + acc);
        }


        return null;
    }

    private Map<String, String> getmiRNAmapping() {

        if (miRNAid2Acc == null) {

            miRNAid2Acc = new HashMap<String, String>();
            String thisLine;
            try {
                BufferedReader br = new BufferedReader(new FileReader(miRNAMappingFile), 1024 * 1024);
                while ((thisLine = br.readLine()) != null) { // while loop begins here

                    if (!thisLine.equals("")) {
                        String [] split = thisLine.split("\t");
                        miRNAid2Acc.put(split[0].toLowerCase(), split[1]);
                    }

                }

            } catch (FileNotFoundException e) {
                System.err.println("Error loading miRNAmapping file from " + miRNAMappingFile);
                System.exit(0);
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }

        }

        return miRNAid2Acc;

    }

    private String getmiRNAAccession(String miRNAid) {

        Map<String, String> mapping = getmiRNAmapping();
        if (mapping.containsKey(miRNAid.toLowerCase())) {
            return mapping.get(miRNAid.toLowerCase());
        }
        else {
            return "";
        }

    }

    private String getGeneId(String geneSymbol) {

        if (geneSymbol.equals("")) return null;

        System.err.println("searching bdb: " + geneSymbol);
//              Class.forName("org.bridgedb.webservice.bridgerest.BridgeRest");
//            IDMapper mapper = BridgeDb.connect ("idmapper-bridgerest:http://webservice.bridgedb.org/Human");

        IDMapper idm = getIDMApper();
        Xref ref = new Xref(geneSymbol, BioDataSource.ENTREZ_GENE);
        try {
//            Set<String> attr = ((AttributeMapper)idm).getAttributes(ref, "Symbol");

            Map<Xref, String> map = ((AttributeMapper)idm).freeAttributeSearch(geneSymbol, "Symbol", 20);//idm.mapID(ref, DataSource.getBySystemCode("Symbol"));

            if (map.keySet().isEmpty()) {
                System.out.println("Couldn't find an identifiers for " + geneSymbol);
                return null;
            }

            for (Xref x : map.keySet()) {

                System.out.println("Found - " + x.getId() + " -> " + x.getDataSource() + " for " + geneSymbol);
                if (x.getDataSource().toString().equals("Entrez Gene")) {
                    Set<String> ids = ((AttributeMapper) idm).getAttributes(x, "Symbol");
                    for (String s : ids) {
                        if (s.equals(geneSymbol)) {
                            System.out.println(x.getId() + " - " + x.getDataSource());
                            return x.getId();
                        }
                    }
                }
            }

        } catch (IDMapperException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return null;
    }



    private static class RestrictionVisitor extends OWLClassExpressionVisitorAdapter {


        private OWLClass relatedClass;
        private OWLObjectProperty property;

        public RestrictionVisitor(OWLObjectProperty prop) {
            this.property = prop;
        }


        public void visit(OWLObjectSomeValuesFrom desc) {
            if (desc.getProperty().equals(property)) {
                relatedClass = desc.getFiller().asOWLClass();
            }
        }

        public OWLClass getRelatedClass() {
            return relatedClass;
        }
    }


}
