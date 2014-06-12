package Ontogrator_kupkb;/*
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

import com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.util.OWLClassExpressionVisitorAdapter;
import org.semanticweb.owlapi.util.SimpleShortFormProvider;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

import java.io.*;
import java.sql.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Author: Simon Jupp<br>
 * Date: Feb 17, 2011<br>
 * The University of Manchester<br>
 * Bio-Health Informatics Group<br>
 */
public class OWLNestedSetGenerator {

    private static int INDENT = 4;

    private OWLReasonerFactory reasonerFactory;

    private OWLOntology ontology;

    private OWLOntologyManager man;

    private PrintStream out;

    private Map<OWLClass, Set<String>> nodeMap;

    private Connection connect = null;

    private static IRI nodeAnno = IRI.create("http://nodeset#leftRight");

    private PreparedStatement insertOntology1 = null;
    private PreparedStatement insertOntology2 = null;
    private PreparedStatement insertOntology3 = null;

    private int paneNumber;

    private SimpleShortFormProvider sp = new SimpleShortFormProvider();

    int n = 0;
    private Set<IRI> ignore;

    public OWLNestedSetGenerator (OWLOntologyManager manager, OWLReasonerFactory reasonerFactory, int ontologyPane) {

        this.ignore = new HashSet<IRI>();
        this.nodeMap = new HashMap<OWLClass, Set<String>>();
        this.reasonerFactory = reasonerFactory;
        this.man =  manager;
        this.paneNumber = ontologyPane;
        out = System.out;
        connectMysql();

        try {
            insertOntology1 = connect
                        .prepareStatement("insert ignore into ontogrator_kupkb.OntologyEntries values (?, ?, ?, ?, ? , ?, ?)");

            insertOntology2 = connect
                        .prepareStatement("insert ignore into ontogrator_kupkb.OntologyRelations values (?, ?, ?, ?, ?)");

            insertOntology3 = connect
                        .prepareStatement("insert ignore into ontogrator_kupkb.OntologySynonyms values (?, ?, ?)");

        } catch (SQLException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }


    }

    private void connectMysql() {

        try {
            Class.forName("com.mysql.jdbc.Driver");
            // Setup the connection with the DB
                connect = DriverManager
                        .getConnection("jdbc:mysql://localhost/ontogrator_kupkb?"
                                + "user=simon&password=plok88");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (SQLException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

    }

    private void printHierarchy(OWLOntology onto, OWLClass clazz) throws OWLException {
        OWLReasoner reasoner = reasonerFactory.createNonBufferingReasoner(onto);
        System.err.println("just classified");
        this.ontology = onto;
        reasoner.flush();
        computeNumbers(reasoner, clazz, 0);

//        printHierarchy(reasoner, clazz, 0);


    }


    public void computeNumbers(OWLReasoner reasoner, OWLClass clazz, int depth) {

        if (!ignore.contains(clazz.getIRI())) {
            if (reasoner.isSatisfiable(clazz)) {
                n++;
                int left = n;

                for (OWLClass child : reasoner.getSubClasses(clazz, true).getFlattened()) {
                    if (!child.equals(clazz)) {
                        computeNumbers(reasoner, child, depth + 1);
                    }
                }
                n++;
                int right = n;
                addAnnotation(clazz, left, right);

                insertClassinDB(reasoner, clazz, left, right, depth);
            }
        }

    }



    private static class RestrictionVisitor extends OWLClassExpressionVisitorAdapter {

        OWLClass target;
        boolean isReal = false;
        public RestrictionVisitor (OWLClass cls) {
            target = cls;

        }

        public boolean getChild() {
            return isReal;
        }
        @Override
        public void visit(OWLObjectSomeValuesFrom desc) {

            if (desc.getProperty().asOWLObjectProperty().getIRI().equals(IRI.create("http://purl.org/obo/owl/OBO_REL#part_of"))) {

                if (!desc.getFiller().isAnonymous()) {

                    if (desc.getFiller() instanceof OWLClass) {
                       isReal = desc.getFiller().asOWLClass().equals(target);
                    }

                }
            }
        }
    }



    private void processMouseAnatomy(OWLOntologyManager man, OWLOntology ontology) {

        for (OWLClass cls : ontology.getClassesInSignature()) {

            for (OWLAxiom ax : ontology.getReferencingAxioms(cls)) {

                if (ax instanceof OWLSubClassOfAxiom) {
                    OWLSubClassOfAxiom sub = (OWLSubClassOfAxiom) ax;

                    OWLClassExpression exp = sub.getSuperClass();
                    if (exp.isAnonymous()) {

                        RestrictionVisitor rv = new RestrictionVisitor(cls);
                        exp.accept(rv);

                        if (rv.isReal) {
                            man.applyChanges(man.addAxiom(ontology, man.getOWLDataFactory().getOWLSubClassOfAxiom(sub.getSubClass(), cls)));
                            man.applyChanges(man.removeAxiom(ontology, man.getOWLDataFactory().getOWLSubClassOfAxiom(cls, man.getOWLDataFactory().getOWLThing())));

                        }

                    }
                }

            }
        }

    }


    private void insertClassinDB(OWLReasoner reasoner, OWLClass clazz, int left, int right, int depth) {

        String id = sp.getShortForm(clazz);
        String label = labelFor(clazz).replace("\"", "").replace("@en", "");
        String definition = "";
        int parent_count = reasoner.getSuperClasses(clazz, true).getFlattened().size();
        int child_count = reasoner.getSubClasses(clazz, true).getFlattened().size();

//        if (parent_count == 1) {
//            if (reasoner.getSuperClasses(clazz, true).getFlattened().contains(man.getOWLDataFactory().getOWLThing())) {
//                parent_count = 0;
//            }
//
//        }
        if (child_count == 1) {
            if (reasoner.getSubClasses(clazz, true).getFlattened().contains(man.getOWLDataFactory().getOWLNothing())) {
                child_count = 0;
            }
        }

        System.err.println("Inserting:" + label + " L:"+left + " R:" + right + " D:" + depth + " pc:" + parent_count + " cc:" + child_count);

        try {
            insertOntology1.setInt(1, paneNumber);
            insertOntology1.setString(2, id);
            insertOntology1.setString(3, id);
            insertOntology1.setString(4, label);
            insertOntology1.setString(5, definition);
            insertOntology1.setInt(6, parent_count);
            insertOntology1.setInt(7, child_count);

            insertOntology1.executeUpdate();
            insertOntology1.clearParameters();


        } catch (SQLException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        try {
            insertOntology2.setInt(1, paneNumber);
            insertOntology2.setString(2,id);
            insertOntology2.setInt(3,left);
            insertOntology2.setInt(4,right);
            insertOntology2.setInt(5,depth);

            insertOntology2.executeUpdate();
            insertOntology2.clearParameters();
        } catch (SQLException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        try {
            insertOntology3.setInt(1, paneNumber);
            insertOntology3.setString(2,id);
            insertOntology3.setString(3,label);

            insertOntology3.executeUpdate();
            insertOntology3.clearParameters();
        } catch (SQLException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }


    }

    public void addAnnotation(OWLClass cls, int left, int right) {

        if (nodeMap.get(cls) == null) {
            HashSet<String> s = new HashSet<String>();
            s.add("Left:" + left + ",Right:" + right);
            nodeMap.put(cls, s);

        }
        else {
            nodeMap.get(cls).add("Left:" + left + ",Right:" + right);
        }

//        OWLAnnotationProperty prop = man.getOWLDataFactory().getOWLAnnotationProperty(nodeAnno);
//        OWLLiteral lit = man.getOWLDataFactory().getOWLLiteral("Left:" + left + ",Right:" + right);
//        OWLAnnotation anno = man.getOWLDataFactory().getOWLAnnotation(prop, lit);
//        System.out.println("Adding axiom: " + man.getOWLDataFactory().getOWLAnnotationAssertionAxiom(cls.getIRI(), anno).toString());
//        man.applyChanges(man.addAxiom(ontology, man.getOWLDataFactory().getOWLAnnotationAssertionAxiom(cls.getIRI(), anno)));

    }

    public void printHierarchy(OWLReasoner reasoner, OWLClass clazz, int level)
            throws OWLException {
        /*
         * Only print satisfiable classes -- otherwise we end up with bottom
         * everywhere
         */
        if (!ignore.contains(clazz.getIRI())) {
            if (reasoner.isSatisfiable(clazz)) {
                for (int i = 0; i < level * INDENT; i++) {
                    out.print(" ");
                }
                out.print(labelFor( clazz ));
                for (String s: lrFor( clazz )) {
                    out.print("(" + s + ")");
                }
                out.println();
                for (OWLClass child : reasoner.getSubClasses(clazz, true).getFlattened()) {
                      if (!child.equals(clazz)) {
                        printHierarchy(reasoner, child, level + 1);
                    }
                }
            }
        }
    }

    private Set<String> lrFor( OWLClass clazz) {

//        Set<String> hs = new HashSet<String>();
//
//        Set<OWLAnnotation> annotations = clazz.getAnnotations(ontology);
//        for (OWLAnnotation anno : annotations) {
//            if (anno.getProperty().equals(nodeAnno)) {
//                hs.add(anno.getValue().toString());
//
//            }
//        }
//
//        return hs;
        return nodeMap.get(clazz);
    }

    private String labelFor( OWLClass clazz) {

        Set<OWLAnnotation> annotations = new HashSet<OWLAnnotation>();
        for (OWLOntology onto : man.getOntologies()) {
            annotations.addAll(clazz.getAnnotations(onto));
        }
        for (OWLAnnotation anno : annotations) {
            if (anno.getProperty().getIRI().equals(OWLRDFVocabulary.RDFS_LABEL.getIRI())) {
                return anno.getValue().toString();

            }
        }   

        if (clazz.getIRI().getFragment() != null) {
            return clazz.getIRI().getFragment();
        }


        return sp.getShortForm(clazz);
        
    }
    
    public static void main(String[] args)  {

        OWLOntologyManager man = OWLManager.createOWLOntologyManager();

        PelletReasonerFactory fact = new PelletReasonerFactory();
        
//        try {
//            man.setSilentMissingImportsHandling(true);

            // ontology set 1 Anatomy and Cells

//            int paneid = 1;
//            IRI rootIRI = man.getOWLDataFactory().getOWLThing().getIRI();
//            OWLOntology onto = man.loadOntology(IRI.create("file:/Users/simon/Documents/e-lico/svn/trunk/Public/kupo/kupo-cells.owl"));
//            IRI iri1 = IRI.create("file:/Users/simon/Documents/e-lico/svn/trunk/Public/kupo/imports/mao.owl");
//            OWLOntology mouseOntology = man.loadOntology(iri1);
//            IRI iri = IRI.create("file:/Users/simon/Documents/e-lico/svn/trunk/Public/kupo/imports/cto.owl");
//            man.loadOntology(iri);
//
//            OWLImportsDeclaration impdec1 = man.getOWLDataFactory().getOWLImportsDeclaration(IRI.create("http://purl.org/obo/owl/adult_mouse_anatomy"));
//            OWLImportsDeclaration impdec2 = man.getOWLDataFactory().getOWLImportsDeclaration(IRI.create("http://purl.org/obo/owl/cell"));
//
//            man.applyChange(new AddImport(onto, impdec1));
//            man.applyChange(new AddImport(onto, impdec2));
//
//
//            Set<IRI> ignore = new HashSet<IRI> ();
//            ignore.add(IRI.create("http://www.geneontology.org/formats/oboInOwl#ObsoleteClass"));
//            ignore.add(IRI.create("http://www.geneontology.org/formats/oboInOwl#Definition"));
//            ignore.add(IRI.create("http://www.geneontology.org/formats/oboInOwl#DbXref"));
//            ignore.add(IRI.create("http://www.geneontology.org/formats/oboInOwl#Subset"));
//            ignore.add(IRI.create("http://www.geneontology.org/formats/oboInOwl#Synonym"));
//            ignore.add(IRI.create("http://www.geneontology.org/formats/oboInOwl#SynonymType"));

            // ontology set 2 KUPKB core

//            int paneid = 2;
//
//            OWLOntology onto = man.loadOntology(IRI.create("file:/Users/simon/Documents/e-lico/svn/trunk/Public/kupkb/kupkb.owl"));
//
//            man.loadOntology(IRI.create("file:/Users/simon/Documents/e-lico/svn/trunk/Public/kupkb/imports/EFO_inferred_v142.owl"));
//            man.loadOntology(IRI.create("file:/Users/simon/Documents/e-lico/svn/trunk/Public/kupkb/imports/bio2rdf.owl"));
//            man.loadOntology(IRI.create("file:/Users/simon/Documents/e-lico/svn/trunk/Public/kupkb/imports/IAO.owl"));
//
//            OWLImportsDeclaration impdec1 = man.getOWLDataFactory().getOWLImportsDeclaration(IRI.create("http://bio2rdf.org/bio2rdf-2008.owl"));
//            OWLImportsDeclaration impdec2 = man.getOWLDataFactory().getOWLImportsDeclaration(IRI.create("http://purl.obolibrary.org/obo/iao.owl"));
////            OWLImportsDeclaration impdec3 = man.getOWLDataFactory().getOWLImportsDeclaration(IRI.create("http://www.ebi.ac.uk/efo/efo.owl"));
//
//            man.applyChange(new AddImport(onto, impdec1));
//            man.applyChange(new AddImport(onto, impdec2));
////            man.applyChange(new AddImport(onto, impdec3));
//
//            IRI rootIRI = IRI.create("http://www.e-lico.eu/data/kupkb/KUPKB_1000001");
//            Set<IRI> ignore = new HashSet<IRI> ();

            // ontology set 3 KUPO experiments and models

//            int paneid = 3;
//            OWLOntology onto = man.loadOntology(IRI.create("file:/Users/simon/Documents/e-lico/svn/trunk/Public/kupo/kupo-core.owl"));
//            man.loadOntology(IRI.create("file:/Users/simon/Documents/e-lico/svn/trunk/Public/kupkb/imports/EFO_inferred_v142.owl"));
//            man.loadOntology(IRI.create("file:/Users/simon/Documents/e-lico/svn/trunk/Public/kupo/imports/PATO.owl"));
////            OWLImportsDeclaration impdec1 = man.getOWLDataFactory().getOWLImportsDeclaration(IRI.create("http://www.ebi.ac.uk/efo/efo.owl"));
//            OWLImportsDeclaration impdec2 = man.getOWLDataFactory().getOWLImportsDeclaration(IRI.create("http://purl.org/obo/owl/quality"));
//
////            man.applyChange(new AddImport(onto, impdec1));
//            man.applyChange(new AddImport(onto, impdec2));
//
//            IRI rootIRI = IRI.create("http://www.e-lico.eu/data/kupo/KUPO_0000001");
//            Set<IRI> ignore = new HashSet<IRI> ();
//            ignore.add(IRI.create("http://www.e-lico.eu/data/kupo/KUPO_0000002"));
//            ignore.add(IRI.create("http://www.e-lico.eu/data/kupo/KUPO_0000003"));


            // ontology set 4 Gene Ontology

//            int paneid = 4;
//            OWLOntology onto = man.loadOntology(IRI.create("http://purl.org/obo/owl/GO"));
//
//            IRI rootIRI = man.getOWLDataFactory().getOWLThing().getIRI();
//            Set<IRI> ignore = new HashSet<IRI> ();
//            ignore.add(IRI.create("http://www.geneontology.org/formats/oboInOwl#ObsoleteClass"));
//            ignore.add(IRI.create("http://www.geneontology.org/formats/oboInOwl#Definition"));
//            ignore.add(IRI.create("http://www.geneontology.org/formats/oboInOwl#DbXref"));
//            ignore.add(IRI.create("http://www.geneontology.org/formats/oboInOwl#Subset"));
//            ignore.add(IRI.create("http://www.geneontology.org/formats/oboInOwl#Synonym"));
//            ignore.add(IRI.create("http://www.geneontology.org/formats/oboInOwl#SynonymType"));


            // Main bit where you run the code...


//            Ontogrator_kupkb.OWLNestedSetGenerator ns = new Ontogrator_kupkb.OWLNestedSetGenerator(man, fact, paneid);

            // special tweak for the mousa anatomy t include the partonomy
//            ns.processMouseAnatomy(man, mouseOntology);

//            ns.setIgnoreSet(ignore);
//
//            ns.printHierarchy(onto, man.getOWLDataFactory().getOWLClass(rootIRI));



//        } catch (OWLOntologyCreationException e) {
//            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//        } catch (OWLException e) {
//            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//        }

        // loading kupkb_experiments.test data
        OWLNestedSetGenerator ns = new OWLNestedSetGenerator(man, fact, 0);
        ns.loadData("/Users/simon/dev/java/projects/kupdata/ontogrator_testingdata_small.xls");


    }


    private void loadData(String s) {

        File file = new File(s);
        InputStream inputStream = null;
        try {

            inputStream = file.toURI().toURL().openStream();
            HSSFWorkbook workbook = new HSSFWorkbook(new BufferedInputStream(inputStream));

            HSSFSheet sheet = workbook.getSheetAt(0);

            int lastRow = sheet.getLastRowNum();
            for (int x = 1; x <=17 ; x++) {
                HSSFRow row = sheet.getRow(x);

                if (row.getCell(0) != null) {
                    int tabid = (int) row.getCell(0).getNumericCellValue();
                    int paneid = (int) row.getCell(1).getNumericCellValue();
                    String docid = String.valueOf(row.getCell(2).getNumericCellValue()).replace(".0", "");
                    String ontologyid = row.getCell(3).getStringCellValue();
                    String geneid = String.valueOf(row.getCell(4).getNumericCellValue()).replace(".0", "");
                    String genesymbol = row.getCell(5).getStringCellValue();
                    String unprotAcc = row.getCell(6).getStringCellValue();
                    String expName = row.getCell(7).getStringCellValue();
                    String expDesc = row.getCell(8).getStringCellValue();
                    String species = row.getCell(9).getStringCellValue();
                    String bioMaterial = row.getCell(10).getStringCellValue();
                    String bioMaterialName = row.getCell(11).getStringCellValue();
                    String quality = row.getCell(12).getStringCellValue();


                    System.out.println(tabid +"\t"+ paneid +"\t"+ docid +"\t"+ ontologyid +"\t"+ geneid
                     +"\t"+ genesymbol +"\t"+ unprotAcc +"\t"+ expName +"\t"+ expDesc +"\t"+ species
                     +"\t"+ bioMaterial+"\t"+ bioMaterialName +"\t"+ quality);

                    Statement stmt= null;
                    try {
                        stmt = connect.createStatement();
                        stmt.execute("call ontogrator_kupkb.InsertHit('" +
                        tabid +"','"+ paneid +"','"+ docid +"','"+ ontologyid +"','"+ geneid
                     +"','"+ genesymbol +"','"+ unprotAcc +"','"+ expName +"','"+ expDesc +"','"+ species
                     +"','"+ bioMaterial +"','" + bioMaterialName +"','"+ quality + "')");


                    } catch (SQLException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }

                }

                Statement s2 = null;
                try {
                    s2 = connect.createStatement();
                    s2.execute("call ontogrator_kupkb.UpdateOntologySubset()");
                } catch (SQLException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }

            }


        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

    }

    private void setIgnoreSet(Set<IRI> ignore) {
        this.ignore = ignore;
    }


}
