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

import java.util.HashSet;
import java.util.Set;

/**
 * Author: Simon Jupp<br>
 * Date: Feb 8, 2011<br>
 * The University of Manchester<br>
 * Bio-Health Informatics Group<br>
 */
public class KUPAnnotation {

    private Set<String> bioMaterial = new HashSet<String>();
    private Set<String> hasDisease = new HashSet<String>();
    private String role;
    private Set<String> qualities = new HashSet<String>();
    private String taxonomy;
    private String annotationID;
    private String condition;
    private String laterality;
    private String severity;

    private static String ANNOTATION = "_annotation";

    public KUPAnnotation(String id) {
        this.annotationID = KUPNamespaces.KUPKB.toString() + KUPExperiment.EXPERIMENT + id + ANNOTATION;
        System.out.println("creating annotation URI: " + annotationID);

    }

    public KUPAnnotation(String id, String role, String condition, String taxonomy, Set<String> bioMaterial, Set<String> qualities, Set<String> hasDisease, String laterality, String severity) {

        this.bioMaterial = bioMaterial;
        this.hasDisease = hasDisease;
        this.role = role;
        this.qualities = qualities;
        this.taxonomy = taxonomy;
        this.condition = condition;
        this.laterality = laterality;
        this.severity = severity;

        this.annotationID = KUPNamespaces.KUPKB.toString() + KUPExperiment.EXPERIMENT + id + ANNOTATION;
        System.out.println("creating annotation URI: " + annotationID);

    }


    public Set<String> getBioMaterial() {
        return bioMaterial;
    }

    public Set<String> getHasDisease() {
        return hasDisease;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Set<String> getQualities() {
        return qualities;
    }

    public String getTaxonomy() {
        return taxonomy;
    }

    public void setTaxonomy(String taxa) {
        this.taxonomy = taxa;
    }


    public String getAnnotationID() {
        return annotationID;
    }


    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public String getLaterality() {
        return laterality;
    }

    public void setLaterality(String laterality) {
        this.laterality = laterality;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }
    


}
