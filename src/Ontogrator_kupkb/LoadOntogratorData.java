package Ontogrator_kupkb;

import kupkb_experiments.*;
import org.openrdf.model.Value;
import org.openrdf.query.*;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.http.HTTPRepository;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.AnnotationValueShortFormProvider;
import org.semanticweb.owlapi.util.BidirectionalShortFormProviderAdapter;
import org.semanticweb.owlapi.util.ShortFormProvider;
import org.semanticweb.owlapi.util.SimpleShortFormProvider;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

import java.io.File;
import java.net.URI;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.util.*;/*
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

/**
 * Author: Simon Jupp<br>
 * Date: Feb 18, 2011<br>
 * The University of Manchester<br>
 * Bio-Health Informatics Group<br>
 */
public class LoadOntogratorData {

    private Repository myRepository;
    private RepositoryConnection sesame_connection;

    public List<OntogratorExperiment> getOntogratorExperimentSet() {
        return ontogratorExperimentSet;
    }

    private List<OntogratorExperiment> ontogratorExperimentSet;

    private AnnotationValueShortFormProvider annoSfp;
    private BidirectionalShortFormProviderAdapter shortFormAnnotationAdapter;
    private BidirectionalShortFormProviderAdapter shortFormAdapter;

    private ShortFormProvider shortFormProvider;

    private OWLOntologyManager manager;
    private OWLDataFactory factory;

    public LoadOntogratorData() {


        manager = OWLManager.createOWLOntologyManager();
        factory = manager.getOWLDataFactory();

        manager.setSilentMissingImportsHandling(true);

        try {
            manager.loadOntology(IRI.create(KUPNamespaces.KUPKB_IRI.toString()));
            manager.loadOntology(IRI.create("file:/Users/simon/Documents/e-lico/svn/trunk/Public/kupo/imports/mao.owl"));
            manager.loadOntology(IRI.create("file:/Users/simon/Documents/e-lico/svn/trunk/Public/kupo/imports/cto.owl"));
        } catch (OWLOntologyCreationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        shortFormProvider = new SimpleShortFormProvider();
        shortFormAdapter = new BidirectionalShortFormProviderAdapter(manager, manager.getOntologies(), shortFormProvider);

        annoSfp = new AnnotationValueShortFormProvider(
                Collections.singletonList(factory.getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_LABEL.getIRI())), new HashMap<OWLAnnotationProperty, List<String>>(), manager );
        shortFormAnnotationAdapter = new BidirectionalShortFormProviderAdapter(manager, manager.getOntologies(), annoSfp);


        String sesameServer = "http://rpc295.cs.man.ac.uk:8083/openrdf-sesame";
        String repositoryID = "kupkb-3.1";


        ontogratorExperimentSet = new ArrayList<OntogratorExperiment>();


        this.myRepository = new HTTPRepository(sesameServer, repositoryID);

        try {

            myRepository.initialize();

            sesame_connection = myRepository.getConnection();

            /// test connection:

//             String query = "SELECT * WHERE {?s ?p ?o} LIMIT 10";

//            TupleQuery tp = sesame_connection.prepareTupleQuery(QueryLanguage.SPARQL, query);
//            TupleQueryResult rs = tp.evaluate();
//            while (rs.hasNext()) {
//                BindingSet bSet = rs.next();
//                System.out.println(bSet.toString());
//
//            }

        } catch (RepositoryException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }


    }

    private Connection mysql_connection = null;


    private void addHits (OntogratorExperiment exp) {

        Statement stmt= null;
        try {
            stmt = mysql_connection.createStatement();
            stmt.execute("call ontogrator_kupkb.InsertHit('" +
                    exp.getTabid() +"','"+ exp.getPaneid() +"','"+ exp.getDocumentid() +"','"+ exp.getOntologyID() +"','"+ exp.getGeneid()
                    +"','"+ exp.getGeneSymbol() +"','"+ exp.getUniprotID() +"','"+ exp.getExperiment_name()
                    +"','"+ exp.getDescription() +"','"+ exp.getSpecies()
                    +"','"+ exp.getBioMaterial() +"','" + exp.getBioMaterial() +"','"+ exp.getExpressionStrength() + "')");


        } catch (SQLException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }


    }

    public void updateOntologySubstes() {
        Statement s2 = null;
        try {
            s2 = mysql_connection.createStatement();
            s2.execute("call ontogrator_kupkb.UpdateOntologySubset()");
        } catch (SQLException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

    }



    private void connectMysql() {

        try {
            Class.forName("com.mysql.jdbc.Driver");
            // Setup the connection with the DB
            mysql_connection = DriverManager
                    .getConnection("jdbc:mysql://localhost/ontogrator_kupkb?"
                            + "user=simon&password=plok88");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (SQLException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

    }

    public void closeConnections() {
        try {
            mysql_connection.close();
            sesame_connection.close();
            myRepository.shutDown();
        } catch (RepositoryException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (SQLException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }


    }

    public class GeneRecord {

        String geneid;
        String symbol;

        public String getGeneid() {
            return geneid;
        }

        public void setGeneid(String geneid) {
            this.geneid = geneid;
        }

        public String getSymbol() {
            return symbol;
        }

        public void setSymbol(String symbol) {
            this.symbol = symbol;
        }

        public String getUniprot() {
            return uniprot;
        }

        public void setUniprot(String uniprot) {
            this.uniprot = uniprot;
        }

        String uniprot;

        public GeneRecord(String geneid, String symbol, String uniprot) {
            this.geneid = geneid;
            this.symbol = symbol;
            this.uniprot = uniprot;
        }

        public GeneRecord() {

        }

    }

    public GeneRecord getGeneRecordByGeneid(String geneid) {

        String query = "PREFIX skos:<http://www.w3.org/2004/02/skos/core#>\n" +
                "PREFIX bio2rdf:<http://bio2rdf.org/ns/bio2rdf:>\n" +
                "PREFIX uniprot:<http://bio2rdf.org/ns/uniprot:>\n" +
                "PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#>\n" +
                "PREFIX geneid:<http://bio2rdf.org/ns/geneid:>\n" +
                "SELECT DISTINCT * WHERE \n" +
                "{\n" +
                "<http://bio2rdf.org/geneid:" + geneid + "> skos:notation ?entrezid . \n" +
                "<http://bio2rdf.org/geneid:" + geneid +  "> bio2rdf:symbol ?genesymbol . \n" +
                "<http://bio2rdf.org/geneid:" + geneid +  "> uniprot:xProtein ?proteinid . \n" +
                "?proteinid rdfs:label ?uniprot \n" +
                "}";

//        System.err.println("querying for gene:" + query);

        try {
            TupleQuery nameQuery = sesame_connection.prepareTupleQuery(QueryLanguage.SPARQL,
                    query);

            TupleQueryResult nameResult = nameQuery.evaluate();

            while (nameResult.hasNext()) {
                BindingSet bSet = nameResult.next();
                Value entrez = bSet.getValue("entrezid");
                Value symbol = bSet.getValue("genesymbol");
                Value uniprot = bSet.getValue("uniprot");

//                System.err.println("got result:" + entrez.stringValue() +" "+ symbol.stringValue()+" "+ uniprot.stringValue());
                nameResult.close();
                return new GeneRecord(geneid, symbol.stringValue(), uniprot.stringValue());

            }

        } catch (RepositoryException e) {
            closeConnections();
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (MalformedQueryException e) {
            closeConnections();
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (QueryEvaluationException e) {
            closeConnections();
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        return null;
    }

    public GeneRecord getGeneRecordByUniprot(String uniprotid) {

        String query = "PREFIX skos:<http://www.w3.org/2004/02/skos/core#>\n" +
                "PREFIX bio2rdf:<http://bio2rdf.org/ns/bio2rdf:>\n" +
                "PREFIX uniprot:<http://bio2rdf.org/ns/uniprot:>\n" +
                "PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#>\n" +
                "SELECT DISTINCT * WHERE \n" +
                "{\n" +
                "?geneid skos:notation ?entrezid . \n" +
                "?geneid bio2rdf:symbol ?genesymbol . \n" +
                "?geneid uniprot:xProtein ?proteinid . \n" +
                "?proteinid rdfs:label \"" + uniprotid + "\" \n" +
                "}";

//        System.err.println("querying: " + query);

        try {
            TupleQuery nameQuery = sesame_connection.prepareTupleQuery(QueryLanguage.SPARQL,
                    query);

            TupleQueryResult nameResult = nameQuery.evaluate();

            while (nameResult.hasNext()) {
                BindingSet bSet = nameResult.next();
                Value entrez = bSet.getValue("entrezid");
                Value symbol = bSet.getValue("genesymbol");

                nameResult.close();
                return new GeneRecord(entrez.stringValue(), symbol.stringValue(), uniprotid);

            }

        } catch (RepositoryException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (MalformedQueryException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (QueryEvaluationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        return null;
    }


    public Set<String> getGOannotations (String uniprotid) {

        Set<String> results = new HashSet<String>();

        String query = "PREFIX skos:<http://www.w3.org/2004/02/skos/core#>\n" +
                "PREFIX bio2rdf:<http://bio2rdf.org/ns/bio2rdf:>\n" +
                "PREFIX uniprot:<http://bio2rdf.org/ns/uniprot:>\n" +
                "PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#>\n" +
                "PREFIX geneid:<http://bio2rdf.org/ns/geneid:>\n" +
                "SELECT DISTINCT ?go WHERE \n" +
                "{\n" +
                "<http://purl.uniprot.org/uniprot/" + uniprotid + "> <http://purl.uniprot.org/core/classifiedWith> ?go \n" +
                "}";

//        System.err.println("querying for gene:" + query);

        try {
            TupleQuery nameQuery = sesame_connection.prepareTupleQuery(QueryLanguage.SPARQL,
                    query);

            TupleQueryResult nameResult = nameQuery.evaluate();

            while (nameResult.hasNext()) {
                BindingSet bSet = nameResult.next();
                Value go = bSet.getValue("go");

//                System.err.println("got GO result:" + go.stringValue());
                nameResult.close();
                OWLClass tc = manager.getOWLDataFactory().getOWLClass(IRI.create(go.stringValue()));
                results.add(shortFormProvider.getShortForm(tc));

            }

        } catch (RepositoryException e) {
            closeConnections();
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (MalformedQueryException e) {
            closeConnections();
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (QueryEvaluationException e) {
            closeConnections();
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        return results;


    }

    public static void main(String[] args) {

        String path = "/Users/simon/Desktop/tmp/OntogratorExp";

        LoadOntogratorData lod = new LoadOntogratorData();
        lod.walk(path);

        lod.connectMysql();

        Calendar cal = Calendar.getInstance();
        DateFormat df = DateFormat.getDateTimeInstance(DateFormat.FULL,
                DateFormat.MEDIUM);


        int x = 0;
        for (OntogratorExperiment exp : lod.getOntogratorExperimentSet()) {

            if (x ==10) {
                System.out.println("Still going: " + df.format(cal.getTime()));
                System.out.println("Exp:" + exp.getExperiment_name());
                x=0;
            }
            //          System.out.println(exp.toString());
            lod.addHits(exp);
            lod.updateOntologySubstes();
            x++;
        }
        lod.closeConnections();



    }

    public String getLabel(String id) {

        OWLEntity e = shortFormAdapter.getEntity(id);
        return annoSfp.getShortForm(e);
    }

    public void walk(String path) {


        File root = new File( path );

        File[] list = root.listFiles();

        int docid = 15109;


        for ( File f : list ) {
            if ( f.isDirectory() ) {
                walk( f.getAbsolutePath() );
                System.err.println( "Dir:" + f.getAbsoluteFile() );
            }
            else {
                System.err.println( "File:" + f.getAbsoluteFile() );
                String filename = f.getName();
                System.err.println("File name: " + filename);
//                if (f.getName().equals("doucet_cheval_CCD_list.xls")) {
                if (f.getName().endsWith(".xls")) {
                    File file = new File(URI.create("file:" + f.getAbsolutePath()));

                    ExperimentSpreadSheetParser parser = new ExperimentSpreadSheetParser(file);

                    KUPExperiment exp = parser.getExperiment();

                    for (KUPAnalysis anal : exp.getAnalysis()) {

                        for (CompoundList cl : anal.getCompoundList()) {

                            for (CompoundList.ListMember memb : cl.getMembers()) {


                                GeneRecord gr = null;

                                if (memb.getGeneId() != null) {
                                    //do sparql query and get the gene symbol and uniprot id...
                                    gr = getGeneRecordByGeneid(memb.getGeneId());
                                }
                                else {
                                    gr = getGeneRecordByUniprot(memb.getUniprotID());
                                }

                                if (gr == null) continue;

                                String geneid = gr.getGeneid();
                                String genesymbol = gr.getSymbol();
                                String uniprotid = gr.getUniprot();

                                // for this gene get all the annotations

                                for (KUPAnnotation anno : anal.getAnnotations()) {

                                    // only ant the analytes
                                    if (!anno.getRole().equals("KUPO_0300008")) {
                                        continue;
                                    }

                                    for (String bio : anno.getBioMaterial()) {

                                        OntogratorExperiment ontExp = new OntogratorExperiment();
                                        ontExp.setTabid(1);
                                        ontExp.setDocumentid(docid);
                                        ontExp.setPaneid(1);
                                        ontExp.setGeneid(geneid);
                                        ontExp.setGeneSymbol(genesymbol);
                                        ontExp.setUniprotID(uniprotid);
                                        ontExp.setExperiment_name(exp.getExperimentID());
                                        ontExp.setDescription(exp.getAssayDescription());
                                        ontExp.setSpecies(getLabel(anno.getTaxonomy())); // get human readable
                                        ontExp.setOntologyID(bio);
                                        ontExp.setBioMaterial(getLabel(bio));
                                        if (memb.getExpressionStrength() !=null) {
                                            ontExp.setExpressionStrength(memb.getExpressionStrength());
                                        }
                                        else if (memb.getDifferential() != null) {
                                            ontExp.setExpressionStrength(memb.getDifferential());
                                        }


                                        ontogratorExperimentSet.add(ontExp);
                                    }

                                    if (anno.getTaxonomy() != null) {
                                        OntogratorExperiment ontExp = new OntogratorExperiment();
                                        ontExp.setTabid(1);
                                        ontExp.setDocumentid(docid);
                                        ontExp.setGeneid(geneid);
                                        ontExp.setPaneid(3);
                                        ontExp.setOntologyID(anno.getTaxonomy());
                                        ontogratorExperimentSet.add(ontExp);
                                    }


                                    if (anno.getCondition() != null) {
                                        OntogratorExperiment ontExp = new OntogratorExperiment();
                                        ontExp.setTabid(1);
                                        ontExp.setDocumentid(docid);
                                        ontExp.setGeneid(geneid);
                                        ontExp.setPaneid(3);
                                        ontExp.setOntologyID(anno.getCondition());
                                        ontogratorExperimentSet.add(ontExp);
                                    }

                                    for (String disease : anno.getHasDisease()) {
                                        if (!disease.equals("")) {
                                            OntogratorExperiment ontExp = new OntogratorExperiment();
                                            ontExp.setTabid(1);
                                            ontExp.setDocumentid(docid);
                                            ontExp.setGeneid(geneid);
                                            ontExp.setPaneid(3);
                                            ontExp.setOntologyID(disease);
                                            ontogratorExperimentSet.add(ontExp);
                                        }
                                    }

                                    for (String qualities : anno.getQualities()) {
                                        if (!qualities.equals("")) {
                                            OntogratorExperiment ontExp = new OntogratorExperiment();
                                            ontExp.setTabid(1);
                                            ontExp.setDocumentid(docid);
                                            ontExp.setGeneid(geneid);
                                            ontExp.setPaneid(3);
                                            ontExp.setOntologyID(qualities);
                                            ontogratorExperimentSet.add(ontExp);
                                        }
                                    }
                                }

                                // expression strength
                                if (memb.getExpressionStrength() !=null) {

                                    OntogratorExperiment ontExp = new OntogratorExperiment();
                                    ontExp.setTabid(1);
                                    ontExp.setDocumentid(docid);
                                    ontExp.setGeneid(geneid);
                                    ontExp.setPaneid(3);
                                    OWLEntity cls = shortFormAnnotationAdapter.getEntity(memb.getExpressionStrength());
                                    if (cls != null) {
                                        ontExp.setOntologyID(shortFormProvider.getShortForm(cls));
                                        ontogratorExperimentSet.add(ontExp);
                                    }
                                }

                                if (memb.getDifferential() != null) {

                                    if (!memb.getDifferential().equals("")) {
                                        OWLEntity cls = shortFormAnnotationAdapter.getEntity(memb.getDifferential());
                                        String s = shortFormProvider.getShortForm(cls);

                                        if (!s.equals("")) {
                                            OntogratorExperiment ontExp = new OntogratorExperiment();
                                            ontExp.setTabid(1);
                                            ontExp.setDocumentid(docid);
                                            ontExp.setGeneid(geneid);
                                            ontExp.setPaneid(3);
                                            ontExp.setOntologyID(s);
                                            ontogratorExperimentSet.add(ontExp);
                                        }
                                    }
                                }


                                // go resulst
                                for (String go : getGOannotations(uniprotid)) {
                                    OntogratorExperiment ontExp = new OntogratorExperiment();
                                    ontExp.setTabid(1);
                                    ontExp.setDocumentid(docid);
                                    ontExp.setGeneid(geneid);
                                    ontExp.setPaneid(4);
                                    ontExp.setOntologyID(go);
                                    ontogratorExperimentSet.add(ontExp);
                                }

//                                OWLEntity expID = shortFormAnnotationAdapter.getEntity(memb.getExpressionStrength());
//                                if (expID != null) {
//                                    String expIdShort = shortFormProvider.getShortForm(expID);
//                                    if (!expIdShort.equals("")) {
//                                        OntogratorExperiment ontExp = new OntogratorExperiment();
//                                        ontExp.setTabid(1);
//                                        ontExp.setDocumentid(docid);
//                                        ontExp.setGeneid(geneid);
//                                        ontExp.setPaneid(3);
//                                        ontExp.setOntologyID(expIdShort);
//                                        ontogratorExperimentSet.add(ontExp);
//                                    }
//                                }

                                if (!exp.getAnalysisType().equals("")) {
                                    OntogratorExperiment ontExp = new OntogratorExperiment();
                                    ontExp.setTabid(1);
                                    ontExp.setDocumentid(docid);
                                    ontExp.setGeneid(geneid);
                                    ontExp.setPaneid(2);
                                    ontExp.setOntologyID(exp.getAnalysisType());
                                    ontogratorExperimentSet.add(ontExp);
                                }

                                if (!exp.getAssayType().equals("")) {
                                    OntogratorExperiment ontExp = new OntogratorExperiment();
                                    ontExp.setTabid(1);
                                    ontExp.setDocumentid(docid);
                                    ontExp.setGeneid(geneid);
                                    ontExp.setPaneid(2);
                                    ontExp.setOntologyID(exp.getAssayType());
                                    ontogratorExperimentSet.add(ontExp);
                                }

                                docid++;

                            }
                        }
                    }


                }


            }
        }




    }




}

