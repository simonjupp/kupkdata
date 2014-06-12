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

import org.semanticweb.owlapi.model.*;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

/**
 * Author: Simon Jupp<br>
 * Date: Feb 8, 2011<br>
 * The University of Manchester<br>
 * Bio-Health Informatics Group<br>
 */
public enum KUPVocabulary {

    KUPANNOTATION("KUPKB_1000036", EntityType.CLASS, KUPNamespaces.KUPKB.toString()),

    UP("KUPKB_1000091", EntityType.CLASS, KUPNamespaces.KUPKB.toString()),

    DOWN("KUPKB_1000089", EntityType.CLASS, KUPNamespaces.KUPKB.toString()),

    ANALYSIS_OF("analysisOf", EntityType.OBJECT_PROPERTY, KUPNamespaces.KUPKB.toString()),

    HAS_BIO_MATERERIAL("bioMaterial", EntityType.OBJECT_PROPERTY, KUPNamespaces.KUPKB.toString()),

    HAS_BIO_CONDITION("bioCondition", EntityType.OBJECT_PROPERTY, KUPNamespaces.KUPKB.toString()),

    HAS_DISEASE("hasDisease", EntityType.OBJECT_PROPERTY, KUPNamespaces.KUPKB.toString()),

    HAS_ROLE("hasAnnotationRole", EntityType.OBJECT_PROPERTY, KUPNamespaces.KUPKB.toString()),

    ANNOTATED_WITH("annotatedWith", EntityType.OBJECT_PROPERTY, KUPNamespaces.KUPKB.toString()),

    PRODUCES("produces", EntityType.OBJECT_PROPERTY, KUPNamespaces.KUPKB.toString()),

    PRE_AN_TECH("hasPreAnalyticalTechnique", EntityType.OBJECT_PROPERTY, KUPNamespaces.KUPKB.toString()),

    TAXONOMY_PROPERTY("bio2rdf:xTaxonomy", EntityType.OBJECT_PROPERTY, KUPNamespaces.Bio2RDF.toString()),

    TAXONOMY_CLASS("taxonomy:Taxonomy_of_species", EntityType.CLASS, KUPNamespaces.Bio2RDF.toString()),

    HAS_DBREF("hasDatabaseRef", EntityType.OBJECT_PROPERTY, KUPNamespaces.KUPKB.toString()),

    HAS_MEMBER("hasMember", EntityType.OBJECT_PROPERTY, KUPNamespaces.KUPKB.toString()),

    HAS_QUALITY("hasQuality", EntityType.OBJECT_PROPERTY, KUPNamespaces.KUPKB.toString()),

    FOLD_CHANGE("foldChange", EntityType.DATA_PROPERTY, KUPNamespaces.KUPKB.toString()),

    P_VALUE("pValue", EntityType.DATA_PROPERTY, KUPNamespaces.KUPKB.toString()),

    FDR_VALUE("fdrValue", EntityType.DATA_PROPERTY, KUPNamespaces.KUPKB.toString()),
    
    HAS_EXPRESSION("hasExpression", EntityType.OBJECT_PROPERTY, KUPNamespaces.KUPKB.toString());
    
    public static final Set<IRI> ALL_IRIS;

    static {
        ALL_IRIS = new HashSet<IRI>();
        for(KUPVocabulary v : KUPVocabulary.values()) {
            ALL_IRIS.add(v.getIRI());
        }
    }

    private String localName;

    private IRI iri;

    private EntityType entityType;

    KUPVocabulary(String localname, EntityType entityType, String NAMESPACE) {
        this.localName = localname;
        this.entityType = entityType;
        this.iri = IRI.create(NAMESPACE + localname);
    }

    public EntityType getEntityType() {
        return entityType;
    }

    public String getLocalName() {
        return localName;
    }

    public IRI getIRI() {
        return iri;
    }

    public URI getURI() {
        return iri.toURI();
    }

    public static Set<OWLAnnotationProperty> getAnnotationProperties(OWLDataFactory dataFactory) {
        Set<OWLAnnotationProperty> result = new HashSet<OWLAnnotationProperty>();
        for(KUPVocabulary v : values()) {
            if(v.entityType.equals(EntityType.ANNOTATION_PROPERTY)) {
                result.add(dataFactory.getOWLAnnotationProperty(v.iri));
            }
        }
        return result;
    }


    public static Set<OWLObjectProperty> getObjectProperties(OWLDataFactory dataFactory) {
        Set<OWLObjectProperty> result = new HashSet<OWLObjectProperty>();
        for(KUPVocabulary v : values()) {
            if(v.entityType.equals(EntityType.OBJECT_PROPERTY)) {
                result.add(dataFactory.getOWLObjectProperty(v.iri));
            }
        }
        return result;
    }

    public static Set<OWLDataProperty> getDataProperties(OWLDataFactory dataFactory) {
        Set<OWLDataProperty> result = new HashSet<OWLDataProperty>();
        for(KUPVocabulary v : values()) {
            if(v.entityType.equals(EntityType.DATA_PROPERTY)) {
                result.add(dataFactory.getOWLDataProperty(v.iri));
            }
        }
        return result;
    }

    public static Set<OWLClass> getClasses(OWLDataFactory dataFactory) {
        Set<OWLClass> result = new HashSet<OWLClass>();
        for(KUPVocabulary v : values()) {
            if(v.entityType.equals(EntityType.CLASS)) {
                result.add(dataFactory.getOWLClass(v.iri));
            }
        }
        return result;
    }


}
