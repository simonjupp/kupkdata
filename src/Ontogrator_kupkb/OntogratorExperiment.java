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

/**
 * Author: Simon Jupp<br>
 * Date: Feb 22, 2011<br>
 * The University of Manchester<br>
 * Bio-Health Informatics Group<br>
 */
public class OntogratorExperiment {

    private int tabid;
    private int paneid;
    private int documentid;
    private String ontologyID;
    private String geneid;
    private String geneSymbol;
    private String uniprotID;
    private String experiment_name;
    private String description;
    private String species;
    private String bioMaterial;
    private String expressionStrength;

    public int getTabid() {
        return tabid;
    }

    public void setTabid(int tabid) {
        this.tabid = tabid;
    }

    public int getPaneid() {
        return paneid;
    }

    public void setPaneid(int paneid) {
        this.paneid = paneid;
    }

    public int getDocumentid() {
        return documentid;
    }

    public void setDocumentid(int documentid) {
        this.documentid = documentid;
    }

    public String getOntologyID() {
        return ontologyID;
    }

    public void setOntologyID(String ontologyID) {
        this.ontologyID = ontologyID;
    }

    public String getGeneid() {
        return geneid;
    }

    public void setGeneid(String geneid) {
        this.geneid = geneid;
    }

    public String getGeneSymbol() {
        return geneSymbol;
    }

    public void setGeneSymbol(String geneSymbol) {
        this.geneSymbol = geneSymbol;
    }

    public String getUniprotID() {
        return uniprotID;
    }

    public void setUniprotID(String uniprotID) {
        this.uniprotID = uniprotID;
    }

    public String getExperiment_name() {
        return experiment_name;
    }

    public void setExperiment_name(String experiment_name) {
        this.experiment_name = experiment_name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSpecies() {
        return species;
    }

    public void setSpecies(String species) {
        this.species = species;
    }

    public String getBioMaterial() {
        return bioMaterial;
    }

    public void setBioMaterial(String bioMaterial) {
        this.bioMaterial = bioMaterial;
    }

    public String getExpressionStrength() {
        return expressionStrength;
    }

    public void setExpressionStrength(String expressionStrength) {
        this.expressionStrength = expressionStrength;
    }

    public String toString () {
        final String TAB = "\t";
        return tabid + TAB + paneid + TAB + documentid + TAB + ontologyID + TAB + geneid + TAB + geneSymbol
                + TAB + uniprotID + TAB + experiment_name + TAB + description + TAB + species + TAB + bioMaterial + TAB + expressionStrength;

    }

    public OntogratorExperiment(int tabid, int paneid, int documentid, String ontologyID, String geneid, String geneSymbol, String uniprotID, String experiment_name, String description, String species, String bioMaterial, String expressionStrength) {

        this.tabid = tabid;
        this.paneid = paneid;
        this.documentid = documentid;
        this.ontologyID = ontologyID;
        this.geneid = geneid;
        this.geneSymbol = geneSymbol;
        this.uniprotID = uniprotID;
        this.experiment_name = experiment_name;
        this.description = description;
        this.species = species;
        this.bioMaterial = bioMaterial;
        this.expressionStrength = expressionStrength;
    }

    public OntogratorExperiment () {

    }



}
