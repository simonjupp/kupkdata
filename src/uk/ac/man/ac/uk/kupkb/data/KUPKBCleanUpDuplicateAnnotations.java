package uk.ac.man.ac.uk.kupkb.data;

import org.openrdf.model.Literal;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.query.*;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.config.RepositoryConfigException;
import uk.ac.man.ac.uk.kupkb.repository.DefaultKUPKBConfig;
import uk.ac.man.ac.uk.kupkb.repository.KUPKBManager;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: jupp
 * Date: 06/12/2011
 * Time: 18:11
 * To change this template use File | Settings | File Templates.
 */
public class KUPKBCleanUpDuplicateAnnotations {


    public KUPKBCleanUpDuplicateAnnotations () {





    }

    public Map<String, List<String>> check() {

        String query = "PREFIX owl:<http://www.w3.org/2002/07/owl#>\n" +
                "PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                "PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#>\n" +
                "PREFIX CL:<http://purl.org/obo/owl/CL#>\n" +
                "SELECT * WHERE " +
                "{?s rdf:type owl:Class .\n" +
                "?s rdfs:label ?label" +
                "}";

        HashMap<String, List<String>> valueMap = new HashMap<String, List<String>>();

        try {
            KUPKBManager kupmanager = new KUPKBManager(new DefaultKUPKBConfig());

            ValueFactory factory = kupmanager.getValueFactory();

            TupleQuery tq = kupmanager.prepareTupleQuery(query);

            TupleQueryResult queryResult = tq.evaluate();

            while (queryResult.hasNext()) {
                BindingSet bindings = queryResult.next();

                Value subject = bindings.getValue("s");
                Value label = bindings.getValue("label");


                if (valueMap.containsKey(subject.stringValue())) {

                    String fullLabel = label.stringValue();

                    if (label instanceof Literal) {
                        Literal lit = (Literal) label;
                        fullLabel = lit.toString();
//                        if (lit.getLanguage() != null ) {
//                            fullLabel = lit.toString();
//
//                        }
//                        if (lit.getDatatype() != null) {
//
//
//                        }
                    }

                    valueMap.get(subject.stringValue()).add(fullLabel);

                }
                else {
                    valueMap.put(subject.stringValue(), new ArrayList<String>());
                    valueMap.get(subject.stringValue()).add(label.stringValue());

                }
            }



        } catch (RepositoryConfigException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (RepositoryException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (QueryEvaluationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (MalformedQueryException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        return valueMap;

    }

    public static void main(String[] args) {
        KUPKBCleanUpDuplicateAnnotations cleanup = new KUPKBCleanUpDuplicateAnnotations();
        Map<String, List<String>> valueMap = cleanup.check();

        System.out.println("value map loaded!");

        for (String key : valueMap.keySet()) {
            if (valueMap.get(key).size() > 1) {
                for (String v : valueMap.get(key)) {
                    System.out.println(key + " -> " + v);
                }
            }
        }

        cleanup.printStatementsToRemove(valueMap);

    }

    private void printStatementsToRemove(Map<String, List<String>> valueMap) {

        for (String key : valueMap.keySet()) {
            if (valueMap.get(key).size() > 1) {

                for (String v : valueMap.get(key)) {
                    if (v.startsWith("\"")) {
                        System.out.println("<" + key + ">" + " <http://www.w3.org/2000/01/rdf-schema#label> " + v + " .");
                    }
                }
            }
        }



    }

}
