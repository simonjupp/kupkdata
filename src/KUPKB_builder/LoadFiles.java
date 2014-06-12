package KUPKB_builder;

import org.openrdf.model.*;
import org.openrdf.model.impl.GraphImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.repository.config.RepositoryConfig;
import org.openrdf.repository.config.RepositoryConfigException;
import org.openrdf.repository.manager.LocalRepositoryManager;
import org.openrdf.repository.manager.RepositoryManager;
import org.openrdf.rio.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
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
 * Date: Apr 4, 2011<br>
 * The University of Manchester<br>
 * Bio-Health Informatics Group<br>
 */
public class LoadFiles {

    private RepositoryManager repositoryManager;

    public static String PARAM_CONFIG = "config";

    private Repository repository;

    private RepositoryConnection repositoryConnection;

    private Map<String, String> namespacePrefixes = new HashMap<String, String>();


    public LoadFiles(String reposId, String templateFile) {

        File configFile = new File(templateFile);
        // Parse the configuration file, assuming it is in Turtle format
        final Graph graph;
        Resource repositoryNode = null;

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

            repositoryManager = new LocalRepositoryManager(new File("./kupkbdb"));

            repositoryManager.initialize();

            // Create a configuration object from the configuration file and add it
            // to the repositoryManager
            RepositoryConfig repositoryConfig = RepositoryConfig.create(graph, repositoryNode);
            repositoryManager.addRepositoryConfig(repositoryConfig);

        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        // Get the repository to use
        try {
            repository = repositoryManager.getRepository(reposId);
        } catch (RepositoryConfigException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (RepositoryException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        try {
            repositoryConnection = repository.getConnection();
        } catch (RepositoryException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        // print out namespaces as a test
        try {
            iterateNamespaces();
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

    }

    /**
	 * Parse the given RDF file and return the contents as a Graph
	 *
	 * @param configurationFile The file containing the RDF data
	 * @param format
     * @param defaultNamespace
     * @return The contents of the file as an RDF graph
     * @throws Exception
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
	 * Two approaches for finding the total number of explicit statements in a
	 * repository.
	 *
	 * @return The number of explicit statements
	 */
	private long numberOfExplicitStatements() throws Exception {

		// This call should return the number of explicit statements.
		long explicitStatements = repositoryConnection.size();

		// Another approach is to get an iterator to the explicit statements
		// (by setting the includeInferred parameter to false) and then counting
		// them.
		RepositoryResult<Statement> statements = repositoryConnection.getStatements(null, null, null, false);
		explicitStatements = 0;

		while (statements.hasNext()) {
			statements.next();
			explicitStatements++;
		}
		statements.close();
		return explicitStatements;
	}

    	/**
	 * Iterates and collects the list of the namespaces, used in URIs in the
	 * repository
	 */
	public void iterateNamespaces() throws Exception {
		System.out.println("===== Namespace List ==================================");

		System.out.println("Namespaces collected in the repository:");
		RepositoryResult<Namespace> iter = repositoryConnection.getNamespaces();

		while (iter.hasNext()) {
			Namespace namespace = iter.next();
			String prefix = namespace.getPrefix();
			String name = namespace.getName();
			namespacePrefixes.put(name, prefix);
			System.out.println(prefix + ":\t" + name);
		}
		iter.close();
	}

    public void loadFiles () {
        // load the default files in files_to_load.txt
    }

    public void loadFiles (File preloadFile) throws RepositoryException, IOException {

        // Invent a context for the graph loaded from the file.
        URI context = new URIImpl( preloadFile.toURI().toString() );
        boolean loaded = false;
        // Try all formats
		for( RDFFormat rdfFormat : allFormats ) {

            System.out.println("Trying to load " + rdfFormat);
            try {
				repositoryConnection.add(new BufferedReader(new FileReader(preloadFile), 1024 * 1024),
						"http://kupkb.org/kupkb", rdfFormat, context);
				//repositoryConnection.commit();
				System.out.println("Loaded file '" + preloadFile.getName() + "' (" + rdfFormat.getName() + ")." );
				loaded = true;
				break;
			}
			catch (UnsupportedRDFormatException e) {
				// Format not supported, so try the next format in the list.
                System.err.println("Unsupported format");
			}
			catch (RDFParseException e) {
				// Can't parse the file, so it is probably in another format. Try the next format.
                System.err.println("RDF parsing error");
			}

        }

        if( ! loaded )
			System.out.println( "Failed to load '" + preloadFile.getName() + "'." );
    }

    // A list of RDF file formats used in loadFile().
    private static final RDFFormat allFormats[] = new RDFFormat[] {
        RDFFormat.N3,
        RDFFormat.RDFXML,
        RDFFormat.TURTLE};


    public List<String> getFilePathsFromFile (File fileContainingFilePaths) throws IOException {

        List<String> filePathsToLoad = new ArrayList<String>();

        BufferedReader inp = new BufferedReader(new FileReader(fileContainingFilePaths));
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
			if (line.startsWith("/")) {
                filePathsToLoad.add(line);
			}
		}

        return filePathsToLoad;
    }

    /**
     * Shutdown the repository and flush unwritten data.
     */
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

    /**
     * Auxiliary method, printing an RDF value in a "fancy" manner. In case of
     * URI, qnames are printed for better readability
     *
     * @param value The value to beautify
     */
    public String beautifyRDFValue(Value value) throws Exception {
        if (value instanceof URI) {
            URI u = (URI) value;
            String namespace = u.getNamespace();
            String prefix = namespacePrefixes.get( namespace );
            if (prefix == null) {
                prefix = u.getNamespace();
            } else {
                prefix += ":";
            }
            return prefix + u.getLocalName();
        } else {
            return value.toString();
        }
    }

    /**
     * Auxiliary method, nicely format an RDF statement.
     *
     * @param statement The statement to be formatted.
     * @return The beautified statement.
     */
    public String beautifyStatement(Statement statement) throws Exception {
        return	beautifyRDFValue(statement.getSubject()) + " " +
                beautifyRDFValue(statement.getPredicate()) + " " +
                beautifyRDFValue(statement.getObject());
    }



    public static void main(String[] args) {

//        String serverURL = "http://rpc295.cs.man.ac.uk:8083/openrdf-sesame";

        if (args.length == 0) {
            System.out.println("Please provide a repository id");
            System.exit(0);
        }
        else if (args.length > 0) {


//            String reposId = "kupkb-111011";
            String configFile = "./kup_bigowlim.ttl";
            String reposId = args[0];
            LoadFiles lf = new LoadFiles(reposId, configFile);

            String filesToLoad = "./files_to_load";


            try {
                for (String s : lf.getFilePathsFromFile(new File(filesToLoad))) {

                    File f = new File(s);
                    if (f.exists()) {
                        System.out.println("Loading file " + f.getAbsolutePath());
                        try {
                            lf.loadFiles(f);
                        } catch (RepositoryException e) {
                            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                        } catch (Exception e) {
                            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                        }
                    }
                    else {
                        System.out.println("File doesn't exist: " + s);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }

            lf.shutdown();

        }

//        LoadFiles lf = new LoadFiles(serverURL, reposId);

//        String configFile = "/Users/simon/Library/Application Support/Aduna/OpenRDF Sesame console/templates/kup_bigowlim.ttl";



    }

}
