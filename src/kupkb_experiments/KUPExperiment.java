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

import org.coode.owlapi.manchesterowlsyntax.ManchesterOWLSyntaxOntologyFormat;
import org.semanticweb.owlapi.model.*;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Author: Simon Jupp<br>
 * Date: Feb 8, 2011<br>
 * The University of Manchester<br>
 * Bio-Health Informatics Group<br>
 *
 * This class represents an experiment in the KUPKB
 *
 */
public class KUPExperiment {

    private String baseOntologyURI;
    private String listType;
    private String assayType;


    private String assayDescription;
    private String preAnalyticalTechnuique;
    private String analysisType;

    private Set<KUPAnalysis> analysises = new HashSet<KUPAnalysis>();


    private String outputFileURI;

    private String experimentID;

    final static String EXPERIMENT = "experiment/";

    public String getExperimentID() {
        return experimentID;
    }

    public KUPExperiment(String experimentID) {
        this.experimentID = experimentID;
        this.baseOntologyURI = KUPNamespaces.KUPKB.toString() +  EXPERIMENT + experimentID.replace(" ", "_").toLowerCase();
        System.out.println("creating experiment with URI: " + baseOntologyURI);
    }

    public static String get_Set_as_String (Set<String> set) {
        StringBuilder builder  = new StringBuilder();
        for (String s: set) {
            String tmp = s.replace(":", "_").replace(" ", "_").toLowerCase();
            builder.append("_");
            builder.append(tmp);
        }
        return builder.toString();
    }

    public  void setListType(String s) {
        listType = s;
    }

    public String getListType () {
        return this.listType;
    }

    public Set<KUPAnalysis> getAnalysis() {
        return analysises;
    }

    public String getBaseOntologyURI() {
        return baseOntologyURI;
    }

    public void setBaseOntologyURI(String baseOntologyURI) {
        this.baseOntologyURI = baseOntologyURI;
    }

    public String getAssayType() {
        return assayType;
    }

    public void setAssayType(String assayType) {
        this.assayType = assayType;
    }

    public String getPreAnalyticalTechnuique() {
        return preAnalyticalTechnuique;
    }

    public void setPreAnalyticalTechnuique(String preAnalyticalTechnuique) {
        this.preAnalyticalTechnuique = preAnalyticalTechnuique;
    }

    public String getAnalysisType() {
        return analysisType;
    }

    public void setAnalysisType(String analysisType) {
        this.analysisType = analysisType;
    }

    public String getOutputFileURI() {
        return outputFileURI;
    }

    public void setOutputFileURI(String outputFileURI) {
        this.outputFileURI = outputFileURI;
    }

    public String getAssayDescription() {
        return assayDescription;
    }

    public void setAssayDescription(String assayDescription) {
        this.assayDescription = assayDescription;
    }


    public static void main(String[] args) {

        String experimentID = "test12";

        KUPExperiment exp = new KUPExperiment(experimentID);

        exp.setAssayDescription("This is an experiment...wahoo!");
        exp.setListType("KUPKB_1000030");
        exp.setAssayType("KUPKB_1000020");
        exp.setPreAnalyticalTechnuique(null);
        exp.setAnalysisType("KUPKB_1000006");

        KUPAnalysis analysis = new KUPAnalysis(experimentID + "_KUPO_0001126");

        String annotationId = experimentID + "_KUPO_0001126";

        KUPAnnotation annotation = new KUPAnnotation(annotationId, "KUPO_0300009", "KUPO_0300006",  "taxon:9606", Collections.singleton("KUPO_0001126"),
                Collections.singleton("EFO_0001272"), Collections.singleton("KUPO_0100006"), null, "PATO_0000395");

        CompoundList comList = new CompoundList(exp.getExperimentID() + "_KUPO_0001126");
//        comList.addMembers("list_member_01", "AGT", null, null, null, null, null, "Down", null, null);
//        comList.addMembers("list_member_02", "AZGP1", null, null, null, null, null, "Up", null, null);
//        comList.addMembers("list_member_03", "DDAH2", null, null, null, null, null, "Up", null, null);

        analysis.getCompoundList().add(comList);

        analysis.getAnnotations().add(annotation);

        exp.getAnalysis().add(analysis);

        OntologyBuilder ob = new OntologyBuilder();
        try {
            ob.loadOntology(IRI.create(KUPNamespaces.KUPKB_IRI.toString()));
            ob.initialise();
        } catch (OWLOntologyCreationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        OWLOntology onto = ob.generateOWL(exp);
        OWLOntologyManager man = ob.getOWLManager();
        try {
            man.saveOntology(onto, new ManchesterOWLSyntaxOntologyFormat(), IRI.create("file:/Users/simon/tmp/exp_test.owl"));
        } catch (OWLOntologyStorageException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }


    }



}
