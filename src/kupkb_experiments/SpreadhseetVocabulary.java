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
 * Date: Feb 10, 2011<br>
 * The University of Manchester<br>
 * Bio-Health Informatics Group<br>
 */
public enum SpreadhseetVocabulary {

    EXPERIMENT_ID("experiment id"),

    COMPOUND_LIST("compound list"),

    COMPOUND_LIST_ID("compound list member id"),

    EXPERIMENT_ASSAY("experiment assay"),

    ANALYSIS_TYPE("experiment analysis"),

    PRE_ANALYTICAL("preanalytical technique"),

    ROLE("role"),

    EXPERIMENT_CONDITION("experiment condition"),

    EXPERIMENT_DESCRIPTION("experiment description"),

    SPECIES("species"),

    BIOMATERIAL("biomaterial"),

    MATURITY("maturity"),

    DISEASE("disease"),

    LATERALITY("laterality"),

    SEVERITY("severity"),

    GENE_SYMBOL("gene symbol"),

    GENE_ID("gene id"),

    ENTREZ_GENE_ID("entrezgene id"),

    PMID("PMID"),

    GEO_ID("GEO acc"),

    FDR("fdr"),

    UNIPROT_ID("uniprot id"),

    UNIPROT_ACC("uniprot accession"),

    HMDB_ID("hmdb id"),

    MICROCOSM("microcosm id"),

    EXPRESSION_STRENGTH("expression strength"),

    DIFFERENTIAL("differential expression analyte/control"),

    RATIO("ratio"),

    P_VALUE("pvalue");


    private String keyName;

    public static final  Set<String> ALLKEYS;

    SpreadhseetVocabulary(String s) {
        this.keyName = s;
    }

    static {
        ALLKEYS = new HashSet<String>();
        for(SpreadhseetVocabulary v : SpreadhseetVocabulary.values()) {
            ALLKEYS.add(v.getKeyName());
        }
    }

    public String getKeyName() {
        return keyName;
    }

    public static boolean isValid(String s) {

        if (ALLKEYS.contains(s.toLowerCase())) {
            return true;
        }
        return false;
    }
}
