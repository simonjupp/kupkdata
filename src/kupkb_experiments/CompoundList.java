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
public class CompoundList {

    // variables

    private String compoundListID;

    private Set<ListMember> members = new HashSet<ListMember>();

    // constuctor

    public CompoundList(String id) {

        this.compoundListID = KUPNamespaces.KUPKB.toString() + KUPExperiment.EXPERIMENT + id + "_list";
        System.out.println("creating compound list URI: " + compoundListID);
    }

    // methods

    public String getCompoundListID() {
        return compoundListID;
    }

    public void setCompoundListID(String compoundListID) {
        this.compoundListID = compoundListID;
    }

    public Set<ListMember> getMembers() {
        return members;
    }

    public void addMembers(String id, String geneSymbol, String geneId, String unprotID, String hmdbid, String microcosmid
                           , String expressionStrength, String differential, String ratio, String pValue) {

        members.add(new ListMember(id, geneSymbol, geneId, unprotID, hmdbid, microcosmid
                           , expressionStrength, differential, ratio, pValue));
    }

    public ListMember newListMember () {
        return new ListMember();
    }


    // innner class

    public class ListMember {

        private String id;
        private String geneSymbol;
        private String geneId;
        private String uniprotID;
        private String hmdbid;
        private String microcosmid;
        private String expressionStrength;
        private String differential;
        private String ratio;
        private String pValue;

        private String fdrValue;

        public ListMember(String id, String geneSymbol, String geneId, String unprotID, String hmdbid, String microcosmid, String expressionStrength, String differential, String ratio, String pValue) {
            this.id = id;
            this.geneSymbol = geneSymbol;
            this.geneId = geneId;
            this.uniprotID = unprotID;
            this.hmdbid = hmdbid;
            this.microcosmid = microcosmid;
            this.expressionStrength = expressionStrength;
            this.differential = differential;
            this.ratio = ratio;
            this.pValue = pValue;
        }

        public ListMember() {

        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getGeneSymbol() {
            return geneSymbol;
        }

        public void setGeneSymbol(String geneSymbol) {
            this.geneSymbol = geneSymbol;
        }

        public String getGeneId() {
            return geneId;
        }

        public void setGeneId(String geneId) {
            this.geneId = geneId;
        }

        public String getUniprotID() {
            return uniprotID;
        }

        public void setUniprotID(String uniprotID) {
            this.uniprotID = uniprotID;
        }

        public String getHmdbid() {
            return hmdbid;
        }

        public void setHmdbid(String hmdbid) {
            this.hmdbid = hmdbid;
        }

        public String getMicrocosmid() {
            return microcosmid;
        }

        public void setMicrocosmid(String microcosmid) {
            this.microcosmid = microcosmid;
        }

        public String getExpressionStrength() {
            return expressionStrength;
        }

        public void setExpressionStrength(String expressionStrength) {
            this.expressionStrength = expressionStrength;
        }

        public String getDifferential() {
            return differential;
        }

        public void setDifferential(String differential) {
            this.differential = differential;
        }

        public String getRatio() {
            return ratio;
        }

        public void setRatio(String ratio) {
            this.ratio = ratio;
        }

        public String getPValue() {
            return pValue;
        }

        public void setPValue(String pValue) {
            this.pValue = pValue;
        }


        public void setFdrValue(String fdrValue) {
            this.fdrValue = fdrValue;
        }

        public String getFdrValue() {
            return fdrValue;
        }

        
    }

}
