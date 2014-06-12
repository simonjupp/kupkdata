package uk.ac.man.ac.uk.kupkb.io;

import au.com.bytecode.opencsv.CSVWriter;
import org.openrdf.model.Namespace;
import org.openrdf.query.BindingSet;
import org.openrdf.query.TupleQueryResultHandler;
import org.openrdf.query.TupleQueryResultHandlerException;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;/*
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
public class CSVQueryResultHandler implements TupleQueryResultHandler {

    PrintWriter pw;

    RepositoryResult<Namespace> namespaces;

    Map<String, String> prefixes;

    CSVWriter writer;

    String [] cols;

    public CSVQueryResultHandler(PrintWriter pw,  RepositoryResult<Namespace> namespaces) {
        this.pw = pw;
        this.namespaces = namespaces;
        prefixes = new HashMap<String, String>();

        writer = new CSVWriter(pw);
        try {
            while(namespaces.hasNext()) {
                Namespace n = (Namespace) namespaces.next();
                prefixes.put(n.getName(), n.getPrefix());
            }

        } catch (RepositoryException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

    }

    public void startQueryResult(List<String> list) throws TupleQueryResultHandlerException {
        cols = new String [list.size()];
        int col = 0;
        for (String s : list) {
            cols[col] = s;
            col++;
        }
        writer.writeNext(cols);
    }

    public void endQueryResult() throws TupleQueryResultHandlerException {
        try {
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    public void handleSolution(BindingSet bindingSet) throws TupleQueryResultHandlerException {
        String [] tcols = new String [cols.length];
        int col = 0;

        for (String name : cols) {

            if (bindingSet.hasBinding(name)) {
                String value = bindingSet.getValue(name).stringValue();
                if (value.startsWith("http")) {
                    int i = value.lastIndexOf("/");
                    tcols[col] = value.substring(i + 1);
                }
                else {

                    tcols[col] = value;
                }
            }
            else {
                tcols[col] = "";
            }
            col++;
        }
        writer.writeNext(tcols);

    }
}
