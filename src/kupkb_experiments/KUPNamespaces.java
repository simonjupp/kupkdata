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

/**
 * Author: Simon Jupp<br>
 * Date: Feb 8, 2011<br>
 * The University of Manchester<br>
 * Bio-Health Informatics Group<br>
 */
public enum KUPNamespaces {

//    OWL2XML("http://www.w3.org/2006/12/owl2-xml#"),

    /**
     * The OWL 2 namespace is here for legacy reasons.
     */
    OWL2("http://www.w3.org/2006/12/owl2#"),

    OWL11XML("http://www.w3.org/2006/12/owl11-xml#"),


    /**
     * The OWL 1.1 namespace is here for legacy reasons.
     */
    OWL11("http://www.w3.org/2006/12/owl11#"),

    OWL("http://www.w3.org/2002/07/owl#"),

    RDFS("http://www.w3.org/2000/01/rdf-schema#"),

    RDF("http://www.w3.org/1999/02/22-rdf-syntax-ns#"),

    XSD("http://www.w3.org/2001/XMLSchema#"),

    XML("http://www.w3.org/XML/1998/namespace"),

    SWRL("http://www.w3.org/2003/11/swrl#"),

    SWRLB("http://www.w3.org/2003/11/swrlb#"),

    SKOS("http://www.w3.org/2004/02/skos/core#"),

    KUPO("http://www.kupkb.org/data/kupo/"),

    KUPKB("http://www.kupkb.org/data/kupkb/"),

    KUPKB_IRI("http://www.kupkb.org/public/kupkb/kupkb.owl"),
    
    KUPO_IRI("http://www.kupkb.org/public/kupo/kupo.owl"),

    MAO("http://purl.org/obo/owl/MA#"),

    GO("http://purl.org/obo/owl/GO"),

    CTO("http://purl.org/obo/owl/CL"),

    PATO("http://purl.org/obo/owl/PATO"),

    OBO_REL("http://purl.org/obo/owl/OBO_REL"),

    Bio2RDF("http://bio2rdf.org/ns/"),

    Bio2RDFTAXON("http://bio2rdf.org/"),

    EFO ("http://www.ebi.ac.uk/efo/"),

    GENEIDPREFIX ("http://bio2rdf.org/geneid:"),

    UNIPROTURI("http://purl.uniprot.org/uniprot/"),

    HMDBURI("http://bio2rdf.org/hmdb:"),

    MIRBASE("http://www.mirbase.org/cgi-bin/mirna_entry.pl?acc="),

    OBO("http://purl.obolibrary.org/obo/");

    String ns;


    KUPNamespaces(String ns) {
        this.ns = ns;
    }


    public String toString() {
        return ns;
    }
}
