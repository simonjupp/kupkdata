package uk.ac.man.ac.uk.kupkb.io;

import org.openrdf.model.Namespace;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.query.BindingSet;
import org.openrdf.query.TupleQueryResultHandler;
import org.openrdf.query.TupleQueryResultHandlerException;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;

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
public class HTMLQueryResultHandler implements TupleQueryResultHandler {

    PrintWriter pw;
    RepositoryResult<Namespace> namespaces;
    Map<String, String> prefixes;

    public HTMLQueryResultHandler(PrintWriter pw,  RepositoryResult<Namespace> namespaces) {
        this.pw = pw;
        this.namespaces = namespaces;
        prefixes = new HashMap<String, String>();

        try {

            while(namespaces.hasNext()) {
                Namespace n = (Namespace) namespaces.next();
                prefixes.put(n.getName(), n.getPrefix());
            }

        } catch (RepositoryException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

    }

    boolean odd = true;



    public void startQueryResult(List<String> list) throws TupleQueryResultHandlerException {
        pw.println("<table>");
        pw.println("</tr>");
        for (String str : list) {
            pw.print("<th>");
            pw.println(str);
            pw.print("</th>");
        }
        pw.println("</tr>");
    }

    public void endQueryResult() throws TupleQueryResultHandlerException {
        pw.println("</table>");
    }

    public void handleSolution(BindingSet bindingSet) throws TupleQueryResultHandlerException {
        if (odd) {
            pw.println("<tr>");
            odd = false;
        }
        else {
            pw.println("<tr class=\"odd\">");
            odd = true;
        }

        for (String name : bindingSet.getBindingNames()) {
            pw.print("<td>");

            String value = bindingSet.getValue(name).stringValue();


            if (value.endsWith(".gif")) {
                String tmp = value.replace(".gif", ".png");
                pw.print("<a href=\"" + tmp + "\"><img width=\"300\" height=\"240\" src=\"" + tmp + "\"/></a>");
            }
            else if (value.startsWith("http")) {
                URIImpl uri = new URIImpl(value);

                pw.print("<a href=\"" + value + "\">" + prefixes.get(uri.getNamespace()) + ":" + uri.getLocalName() + "</a>");
            }
            else {
                pw.print(value);
            }




            pw.print("</td>");
        }

        pw.println("</tr>");

    }
}
