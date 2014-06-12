package uk.ac.man.ac.uk.kupkb.repository;

import info.aduna.xml.XMLWriter;
import org.openrdf.model.Graph;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.GraphImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.query.*;
import org.openrdf.query.resultio.sparqlxml.SPARQLResultsXMLWriter;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.config.RepositoryConfig;
import org.openrdf.repository.config.RepositoryConfigException;
import org.openrdf.repository.manager.RepositoryManager;
import org.openrdf.rio.*;
import uk.ac.man.ac.uk.kupkb.io.CSVQueryResultHandler;
import uk.ac.man.ac.uk.kupkb.io.HTMLQueryResultHandler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;/*
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
 * Date: Apr 14, 2011<br>
 * The University of Manchester<br>
 * Bio-Health Informatics Group<br>
 */
public class KUPKBManager {

    private RepositoryManager repositoryManager;

    private static Repository repository;

    private static RepositoryConnection repositoryConnection;

    private static Graph graph;

    private static Resource repositoryNode = null;

    /*
    * Constructors
    */
    public KUPKBManager () throws RepositoryException, RepositoryConfigException {
        new KUPKBManager(new DefaultKUPKBConfig());
    }

    public KUPKBManager (KUPKBConfig config) throws RepositoryConfigException, RepositoryException {

        if (config.isLocal()) {
            parseConfig(config.getConfigFilePath());
            repositoryManager = KUPKBConnection.getLocalConnection(config.getLocalRepositoryConnectionPath());
            // Create a configuration object from the configuration file and add it
            // to the repositoryManager
            RepositoryConfig repositoryConfig = RepositoryConfig.create(graph, repositoryNode);
            repositoryManager.addRepositoryConfig(repositoryConfig);

        }
        else {
            repositoryManager = KUPKBConnection.getHTTPConnection(config.getRemoteRepositoryConnectionURL());

        }

        repository = repositoryManager.getRepository(config.getRepositoryID());
        repositoryConnection = repository.getConnection();
    }

    /*
    * Methods
    */

    public RepositoryManager getRepositoryManager() {
        return repositoryManager;
    }

    public static Repository getRepository() {
        return repository;
    }

    public static RepositoryConnection getRepositoryConnection() {
        return repositoryConnection;
    }

    public TupleQueryResultHandler evaluateQuery(String queryid, String format, PrintWriter pw) throws Exception {
        // process the query file to get the queries
        TupleQueryResultHandler result;
		String[] queries = collectQueries(queryid, "./test_queries");
        for (int i = 0; i < queries.length; i++) {
            final String name = queries[i].substring(0, queries[i].indexOf(":"));
            final String query = queries[i].substring(name.length() + 2).trim();
            System.out.println("Executing query '"+ name + "'");

            // this is done via invoking the respoitory's performTableQuery()
            // method
            // the first argument specifies the query language
            // the second is the actual query string
            // the result is returned in a tabular form with columns, the
            // variables in the projection
            // and each result in a separate row. these are simply enumerated
            // and shown in the console
            executeSingleQuery(query, format, pw);
        } // for

//        int rows = 0;
//        while (result.hasNext()) {
//            BindingSet tuple = (BindingSet) result.next();
//            if (rows == 0) {
//                for (Iterator<Binding> iter = tuple.iterator(); iter.hasNext();) {
//                    System.out.print(iter.next().getName());
//                    System.out.print("\t");
//                }
//                System.out.println();
//                System.out.println("---------------------------------------------");
//            }
//            rows++;
//        }

        return null;
    }

    public TupleQuery prepareTupleQuery(String query) throws RepositoryException, MalformedQueryException {

        return repositoryConnection.prepareTupleQuery(QueryLanguage.SPARQL,
         query);

    }

    public void executeSingleQuery(String query, String format, PrintWriter pw) {
		try {
//			long start = System.nanoTime();
			Query preparedQuery = prepareQuery(query);
			if( preparedQuery == null ) {
				System.out.println( "Unable to parse query: " + query );
                return;
			}

			if (preparedQuery instanceof BooleanQuery) {
				System.out.println("Result: " + ((BooleanQuery) preparedQuery).evaluate());
                return;
			}


            TupleQuery tuple = repositoryConnection.prepareTupleQuery(QueryLanguage.SPARQL, query);
            tuple.setIncludeInferred(true);

            TupleQueryResultHandler handler = new SPARQLResultsXMLWriter(new XMLWriter(pw));
            if (format.equals("html")) {
                handler = new HTMLQueryResultHandler(pw, repositoryConnection.getNamespaces());
            }
            else if (format.equals("csv")) {
                handler = new CSVQueryResultHandler(pw, repositoryConnection.getNamespaces());
            }

            tuple.evaluate(handler);

		} catch (Throwable e) {
			System.out.println("An error occurred during query execution: " + e.getMessage());
		}
	}

    private Query prepareQuery( String query ) throws Exception {

			try {
				return repositoryConnection.prepareQuery(QueryLanguage.SPARQL, query );
			} catch (UnsupportedQueryLanguageException e) {
				// Can't use this query language, so try the next one.
			} catch (MalformedQueryException e) {
				// The query is probably not in this language. Try the next language.
			}
		// Can't prepare this query in any language
		return null;
	}

    private void parseConfig (String pathToConfig) {

        File configFile = new File(pathToConfig);
        // Parse the configuration file, assuming it is in Turtle format

        try {
            graph = parseFile(configFile, RDFFormat.TURTLE, "http://example.org#");

            // Look for the subject of the first matching statement for
            // "?s type Repository"
            Iterator<Statement> iter = graph.match(null, RDF.TYPE, new URIImpl(
                    "http://www.openrdf.org/config/repository#Repository"));
            if (iter.hasNext()) {
                Statement st = iter.next();
                repositoryNode = st.getSubject();
            }
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

    }

    /**
     * Parse the given RDF file and return the contents as a Graph
     *
     * @param configurationFile The file containing the RDF data
     * @return The contents of the file as an RDF graph
     */
    private Graph parseFile(File configurationFile, RDFFormat format, String defaultNamespace) throws Exception {
        final Graph graph = new GraphImpl();
        RDFParser parser = Rio.createParser(format);
        RDFHandler handler = new RDFHandler() {
            public void endRDF() throws RDFHandlerException {
            }

            public void handleComment(String arg0) throws RDFHandlerException {
            }

            public void handleNamespace(String arg0, String arg1) throws RDFHandlerException {
            }

            public void handleStatement(Statement statement) throws RDFHandlerException {
                graph.add(statement);
            }

            public void startRDF() throws RDFHandlerException {
            }
        };
        parser.setRDFHandler(handler);
        parser.parse(new FileReader(configurationFile), defaultNamespace);
        return graph;
    }

    /**
     * Parse the query file and return the queries defined there for further
     * evaluation. The file can contain several queries; each query starts with
     * an id enclosed in square brackets '[' and ']' on a single line; the text
     * in between two query ids is treated as a SeRQL query. Each line starting
     * with a '#' symbol will be considered as a single-line comment and
     * ignored. Query file syntax example:
     *
     * #some comment [queryid1] <query line1> <query line2> ... <query linen>
     * #some other comment [nextqueryid] <query line1> ... <EOF>
     *
     * @param queryFile
     * @return an array of strings containing the queries. Each string starts
     *         with the query id followed by ':', then the actual query string
     */
    public static String[] collectQueries(String queryid, String queryFile) throws Exception {
        List<String> queries = new ArrayList<String>();
        BufferedReader inp = new BufferedReader(new FileReader(queryFile));
        String nextLine = null;

        for (;;) {
            String line = nextLine;
            nextLine = null;
            if (line == null) {
                line = inp.readLine();
            }
            if (line == null) {
                break;
            }
            line = line.trim();
            if (line.length() == 0) {
                continue;
            }
            if (line.startsWith("#")) {
                continue;
            }
            if (line.startsWith("^[") && line.endsWith("]") && line.contains(queryid)) {
                StringBuffer buff = new StringBuffer(line.substring(2, line.length() - 1));
                buff.append(": ");

                for(;;) {
                    line = inp.readLine();
                    if (line == null) {
                        break;
                    }
                    line = line.trim();
                    if (line.length() == 0) {
                        continue;
                    }
                    if (line.startsWith("#")) {
                        continue;
                    }
                    if (line.startsWith("^[")) {
                        nextLine = line;
                        break;
                    }
                    buff.append(line);
                    buff.append(System.getProperty("line.separator"));
                }

                queries.add(buff.toString());
            }
        }

        String[] result = new String[queries.size()];
        for (int i = 0; i < queries.size(); i++) {
            result[i] = queries.get(i);
        }
        return result;
    }

    public ValueFactory getValueFactory() {
        return repositoryConnection.getValueFactory();
    }

    public void shutdown() {
        System.out.println("===== Shutting down ==========");
        if (repository != null) {
            try {
                repositoryConnection.close();
                repository.shutDown();
                repositoryManager.shutDown();
            } catch (Exception e) {
                System.out.println("An exception occurred during shutdown: " + e.getMessage());
            }
        }

    }
}
