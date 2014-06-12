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

import org.bridgedb.*;
import org.bridgedb.bio.BioDataSource;

import java.util.Map;
import java.util.Set;

/**
 * Author: Simon Jupp<br>
 * Date: Feb 16, 2011<br>
 * The University of Manchester<br>
 * Bio-Health Informatics Group<br>
 *
 * This file is not part of the KUPKB build process
 *
 */
public class ProteinInteractionMapper {


    public ProteinInteractionMapper() {

        String id = "Q3KP35";
        IDMapper idm = getIDMApper("Hs_Derby_20100601.bridge");
        Xref ref = new Xref(id, BioDataSource.ENTREZ_GENE);
        try {
//            Set<String> attr = idm.getAttributes(ref);

            Map<Xref, String> map = ((AttributeMapper)idm).freeAttributeSearch(id, "Symbol", 20);//idm.mapID(ref, DataSource.getBySystemCode("Symbol"));

            if (map.keySet().isEmpty()) {
                System.err.println("Couldn't find an identifiers for " + id);
            }

            for (Xref x : map.keySet()) {

                System.err.println("Found - " + x.getId() + " -> " + x.getDataSource() + " for " + id  );
                if (x.getDataSource().toString().equals("Entrez Gene")) {
                    Set<String> ids = ((AttributeMapper) idm).getAttributes(x, "Symbol");
                    for (String s : ids) {
                        if (s.equals(id)) {
                            System.err.println(x.getId() + " - " + x.getDataSource());
//                            return x.getId();
                        }
                    }
                }
            }

        } catch (IDMapperException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }


    }

    public IDMapper getIDMApper (String taxon) {

        try {
            Class.forName("org.bridgedb.rdb.IDMapperRdb");
            return BridgeDb.connect("idmapper-pgdb:bridgedb-1.0.2/data/gene_database/" +  taxon);

        } catch (ClassNotFoundException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IDMapperException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return null;
    }

    public static void main(String[] args) {


        ProteinInteractionMapper pm = new ProteinInteractionMapper();

    }

}
