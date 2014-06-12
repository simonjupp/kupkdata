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

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.semanticweb.owlapi.model.IRI;
import uk.ac.manchester.cs.owl.semspreadsheets.model.*;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.*;

/**
 * Author: Simon Jupp<br>
 * Date: Feb 10, 2011<br>
 * The University of Manchester<br>
 * Bio-Health Informatics Group<br>
 *
 * This class parses the KUPKB spreadsheet template for experiments
 * It create a KUPExperiment object see KUPExperiment.java
 *
 */
public class ExperimentSpreadSheetParser {

    private KUPExperiment experiment;

    private Map<String, List<Cell>> keyValue;

    private List<Cell> roleCell;

    private HSSFSheet sheet;

    private Map<String, Integer> compoundAttributeToColumn;

    private int compoundListStart;

    private Workbook workbook;

    private WorkbookManager workbookManager;

    private OntologyTermValidationManager validationManager;

    public ExperimentSpreadSheetParser  (File file) {


        keyValue = new HashMap<String, List<Cell>>();

        compoundAttributeToColumn = new HashMap<String, Integer>();

        String expId ="exp_" + String.valueOf(System.currentTimeMillis());
        String analysisId = "analysis_" + String.valueOf(System.currentTimeMillis());
        roleCell = new ArrayList<Cell>();


        workbookManager = new WorkbookManager();
        InputStream inputStream = null;
        try {
            workbookManager.loadWorkbook(file);

            workbook = workbookManager.getWorkbook();
            validationManager = workbookManager.getOntologyTermValidationManager();



            inputStream = file.toURI().toURL().openStream();
            HSSFWorkbook workbook = new HSSFWorkbook(new BufferedInputStream(inputStream));

            this.sheet = workbook.getSheetAt(0);

            int lastRow = sheet.getLastRowNum();
            for (int x = 0; x <=lastRow ; x++) {
                HSSFRow row = sheet.getRow(x);
                if (row != null) {
                    firstPass(row);
                }
            }


        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        if (!keyValue.containsKey(SpreadhseetVocabulary.EXPERIMENT_ID.getKeyName())) {
            System.err.println("Didn't find an experiment id in the spreadsheet");
            System.exit(0);
        }
        else {

            Cell c = keyValue.get(SpreadhseetVocabulary.EXPERIMENT_ID.getKeyName()).get(0);
            String texpId = getValueForKey(c);
            if (!texpId.equals("")) {
                expId = texpId;
            }

            experiment = new KUPExperiment(expId);

            if (experiment == null) {
                System.err.println("Can't create experiment");
                System.exit(0);
            }
            else {

                if (keyValue.get(SpreadhseetVocabulary.COMPOUND_LIST.getKeyName()) != null) {
                    String desc = getValueForKey(keyValue.get(SpreadhseetVocabulary.COMPOUND_LIST.getKeyName()).get(0));
                    if (!desc.equals("")) {
                        experiment.setListType(desc);
                    }
                }

                if (keyValue.get(SpreadhseetVocabulary.EXPERIMENT_ASSAY.getKeyName()) != null) {
                    String desc = getValueForKey(keyValue.get(SpreadhseetVocabulary.EXPERIMENT_ASSAY.getKeyName()).get(0));
                    if (!desc.equals("")) {
                        experiment.setAssayType(desc);
                    }
                }

                if (keyValue.get(SpreadhseetVocabulary.PRE_ANALYTICAL.getKeyName()) != null) {
                    String desc = getValueForKey(keyValue.get(SpreadhseetVocabulary.PRE_ANALYTICAL.getKeyName()).get(0));
                    if (!desc.equals("")) {
                        experiment.setPreAnalyticalTechnuique(desc);
                    }
                }

                if (keyValue.get(SpreadhseetVocabulary.ANALYSIS_TYPE.getKeyName()) != null) {
                    String desc = getValueForKey(keyValue.get(SpreadhseetVocabulary.ANALYSIS_TYPE.getKeyName()).get(0));
                    if (!desc.equals("")) {
                        experiment.setAnalysisType(desc);
                    }
                }
            }
        }


        // now go through the analysis roles
        Set<KUPAnnotation> annotations = new HashSet<KUPAnnotation>();
        String uniqueString = "";
        int rolecounter = 0;
        for (Cell currentRole : roleCell) {


            // search from current cell down until you get to the next role
            int rowIndex = currentRole.getRowIndex();
            // first is the Role

            uniqueString = "_" + String.valueOf(System.currentTimeMillis() + rolecounter);
            analysisId = expId + uniqueString;

            KUPAnnotation annotation = new KUPAnnotation(analysisId);
            String roleValue = getValueForKey(currentRole);
            if (roleValue.equals("")) {
                rolecounter++;
                continue;
            }
            annotation.setRole(roleValue);
            rowIndex++;



            // now keep going until you find the next role
            Set<String> qualities = new HashSet<String>();
            Set<String> bioMaterials = new HashSet<String>();

            while (keepGettingRole(rowIndex)) {
                HSSFRow currentRow = sheet.getRow(rowIndex);
                // get the first cell

                Cell cell = currentRow.getCell(0);
                System.err.println(cell.getStringCellValue());
                if (cell.getStringCellValue().toLowerCase().equals(SpreadhseetVocabulary.EXPERIMENT_CONDITION.getKeyName())) {
                    String t = getValueForKey(cell);
                    IRI iri = lookupId(cell, t);
                    if (iri != null) {
                        t = iri.toString();
                    }

                    annotation.setCondition(t);
                    System.out.println("Setting exp condition: " + t);
                }
                else if (cell.getStringCellValue().toLowerCase().equals(SpreadhseetVocabulary.SPECIES.getKeyName())) {
                    String t = getValueForKey(cell);
                    IRI iri = lookupId(cell, t);
                    if (iri != null) {
                        t = iri.toString();
                    }

                    annotation.setTaxonomy(t);
                    System.out.println("Setting taxonomy: " + t);
                }
                else if (cell.getStringCellValue().toLowerCase().equals(SpreadhseetVocabulary.DISEASE.getKeyName())) {
                    String t = getValueForKey(cell);
                    String [] diseaseValues = t.split("\\s*\\|\\s*");
                    Set<String> diseasesSet = new HashSet<String>();
                    for (String s : diseaseValues) {
                        s = s.trim();
                        IRI iri = lookupId(cell, s);
                        if (iri != null) {
                            s = iri.toString();
                        }
                        System.out.println("Setting disease: " + s);
                        diseasesSet.add(s);
                    }
                    annotation.getHasDisease().addAll(diseasesSet);
                }
                else if (cell.getStringCellValue().toLowerCase().equals(SpreadhseetVocabulary.BIOMATERIAL.getKeyName())) {

                    String desc = getValueForKey(keyValue.get(SpreadhseetVocabulary.BIOMATERIAL.getKeyName()).get(rolecounter));
                    String [] values = desc.split("\\s*\\|\\s*");
                    for (String s : values) {
                        s = s.trim();

                        IRI iri = lookupId(cell, s);
                        if (iri != null) {
                            s = iri.toString();
                        }
                        System.out.println("Setting biomaterial: " + s);

                        bioMaterials.add(s);
                    }
                }
                else if (cell.getStringCellValue().toLowerCase().equals(SpreadhseetVocabulary.EXPERIMENT_DESCRIPTION.getKeyName())) {
                    String description = getValueForKey(keyValue.get(SpreadhseetVocabulary.EXPERIMENT_DESCRIPTION.getKeyName()).get(0));
                    if (!description.equals("")) {
                        System.out.println("Setting description: " + description);
                        experiment.setAssayDescription(description);
                    }
                }
                else {
                    String t = getValueForKey(cell);
                    if (!t.equals("")) {
                        IRI iri = lookupId(cell, t);
                        if (iri != null) {
                            t = iri.toString();
                        }
                        System.out.println("Setting quality: " + t);

                        qualities.add(t);
                    }
                    // the rest are qualities

                }
                rowIndex++;
            }
            annotation.getBioMaterial().addAll(bioMaterials);
            annotation.getQualities().addAll(qualities);

            annotations.add(annotation);
            rolecounter++;

        }


        // finally parse the compound lists
        CompoundList comList = new CompoundList(expId + uniqueString);
        for (int r = compoundListStart; r <=sheet.getLastRowNum(); r++) {

            if (sheet.getRow(r) == null) {
                continue;
            }

            CompoundList.ListMember listMember = comList.newListMember();
//            Cell firstCell = sheet.getRow(r).getCell(1);
//            if (firstCell != null) {
//                if (!firstCell.getStringCellValue().equals("")) {

                    for (String key : compoundAttributeToColumn.keySet()) {

                        if (key.equals(SpreadhseetVocabulary.GENE_SYMBOL.getKeyName())) {
                            // get the value in the cell
                            int col = compoundAttributeToColumn.get(key);
                            Cell cell = sheet.getRow(r).getCell(col);
                            if (cell != null) listMember.setGeneSymbol(cell.getStringCellValue());
                        }
                        if (key.equals(SpreadhseetVocabulary.GENE_ID.getKeyName()) || key.equals(SpreadhseetVocabulary.ENTREZ_GENE_ID.getKeyName())) {
                            // get the value in the cell
                            int col = compoundAttributeToColumn.get(key);
                            Cell cell = sheet.getRow(r).getCell(col);
                            if (cell != null) {

                                String s = cell.getStringCellValue();

                                if (s.contains("E")) {
                                    String tmps = s.substring(s.indexOf("E"));
                                    s = s.replace(tmps, "");
                                    s = s.replace(".", "");
                                }
                                else if (s.endsWith(".0")) {
                                    s = s.replace(".0", "");
                                }
                                listMember.setGeneId(s);
                            }
                        }
                        if (key.equals(SpreadhseetVocabulary.UNIPROT_ID.getKeyName()) || key.equals(SpreadhseetVocabulary.UNIPROT_ACC.getKeyName())) {
                            // get the value in the cell
                            int col = compoundAttributeToColumn.get(key);
                            Cell cell = sheet.getRow(r).getCell(col);
                            if (cell != null) listMember.setUniprotID(cell.getStringCellValue());
                        }
                        if (key.equals(SpreadhseetVocabulary.HMDB_ID.getKeyName())) {
                            // get the value in the cell
                            int col = compoundAttributeToColumn.get(key);
                            Cell cell = sheet.getRow(r).getCell(col);
                            if (cell != null) listMember.setHmdbid(cell.getStringCellValue());
                        }
                        if (key.equals(SpreadhseetVocabulary.MICROCOSM.getKeyName())) {
                            // get the value in the cell
                            int col = compoundAttributeToColumn.get(key);
                            Cell cell = sheet.getRow(r).getCell(col);
                            if (cell != null) listMember.setMicrocosmid(cell.getStringCellValue());
                        }
                        if (key.equals(SpreadhseetVocabulary.EXPRESSION_STRENGTH.getKeyName())) {
                            // get the value in the cell
                            int col = compoundAttributeToColumn.get(key);
                            Cell cell = sheet.getRow(r).getCell(col);
                            if (cell != null) listMember.setExpressionStrength(cell.getStringCellValue());
                        }
                        if (key.equals(SpreadhseetVocabulary.DIFFERENTIAL.getKeyName())) {
                            // get the value in the cell
                            int col = compoundAttributeToColumn.get(key);
                            Cell cell = sheet.getRow(r).getCell(col);
                            if (cell != null) listMember.setDifferential(cell.getStringCellValue());
                        }
                        if (key.equals(SpreadhseetVocabulary.RATIO.getKeyName())) {
                            // get the value in the cell
                            int col = compoundAttributeToColumn.get(key);
                            Cell cell = sheet.getRow(r).getCell(col);
                            if (cell != null) listMember.setRatio(cell.getStringCellValue());
                        }
                        if (key.equals(SpreadhseetVocabulary.P_VALUE.getKeyName())) {
                            // get the value in the cell
                            int col = compoundAttributeToColumn.get(key);
                            Cell cell = sheet.getRow(r).getCell(col);
                            if (cell != null) listMember.setPValue(cell.getStringCellValue());
                        }
                        if (key.equals(SpreadhseetVocabulary.FDR.getKeyName())) {
                            // get the value in the cell
                            int col = compoundAttributeToColumn.get(key);
                            Cell cell = sheet.getRow(r).getCell(col);
                            if (cell != null) listMember.setFdrValue(cell.getStringCellValue());
                        }
                    }
                    comList.getMembers().add(listMember);
//                }
//            }
        }

        KUPAnalysis analysis = new KUPAnalysis(analysisId);
        analysis.getCompoundList().add(comList);
        analysis.getAnnotations().addAll(annotations);
        experiment.getAnalysis().add(analysis);

    }

    public IRI lookupId(Cell cell, String value) {

        int colIndex = cell.getColumnIndex();
        int rowIndex = cell.getRowIndex();

        Collection<OntologyTermValidation> validations = validationManager.getContainingValidations(
                new Range(workbook.getSheet(0), colIndex + 1, rowIndex, colIndex + 1, rowIndex));

        for (OntologyTermValidation v : validations) {
            OntologyTermValidationDescriptor desc = v.getValidationDescriptor();
            for (Term t : desc.getTerms()) {
                if (t.getName().toLowerCase().equals(value.toLowerCase())) {
                    if (t.getIRI().toString().contains("e-lico.eu")) {
                        return IRI.create(t.getIRI().toString().replace("e-lico.eu", "kupkb.org"));
                    }
                    return t.getIRI();
                }
            }
        }
        
        return null;
    }


    private boolean keepGettingRole(int rowIndex) {

        HSSFRow currentRow = sheet.getRow(rowIndex);
        if (currentRow == null) {
            return false;
        }
        Cell cell = currentRow.getCell(0);
        if (cell == null) {
            return false;
        }
        else if (cell.getStringCellValue().equals("")) {
            return false;
        }


        return true;  //To change body of created methods use File | Settings | File Templates.
    }

    private String getValueForKey (Cell cell) {
        int colIndex = cell.getColumnIndex();
        int rowIndex = cell.getRowIndex();
//        System.err.println(cell.getStringCellValue() + " col" + colIndex + " row " + rowIndex);
        Cell nextCell = sheet.getRow(rowIndex).getCell(colIndex + 1);

        

        if (nextCell != null) {
            return nextCell.getStringCellValue();
        }
        return "";
    }

    private void firstPass (HSSFRow row) {

        // first pass, looking for experiment ID and where the role and compound list are located
        Iterator i = row.cellIterator();
        while (i.hasNext()) {
            Cell cell = (Cell) i.next();
//            System.out.println(cell.getCellType());
            cell.setCellType(HSSFCell.CELL_TYPE_STRING);

//            if (cell.getCellType() == HSSFCell.CELL_TYPE_NUMERIC) {
//
//            }

            if (SpreadhseetVocabulary.isValid(cell.getStringCellValue())) {
                // put this in the map, and the value of the next cell
                if (keyValue.get(cell.getStringCellValue().toLowerCase()) == null) {
                    keyValue.put(cell.getStringCellValue().toLowerCase(), new ArrayList<Cell>());
                }
                keyValue.get(cell.getStringCellValue().toLowerCase()).add(cell);

                if (cell.getStringCellValue().toLowerCase().equals(SpreadhseetVocabulary.ROLE.getKeyName())) {
                    roleCell.add(cell);
                }
                else if (cell.getStringCellValue().toLowerCase().equals(SpreadhseetVocabulary.GENE_SYMBOL.getKeyName())) {
                    compoundListStart = row.getRowNum() + 1;
                    compoundAttributeToColumn.put(SpreadhseetVocabulary.GENE_SYMBOL.getKeyName(), cell.getColumnIndex());
                }
//                else if (cell.getStringCellValue().toLowerCase().equals(SpreadhseetVocabulary.GENE_SYMBOL.getKeyName()) && cell.getColumnIndex() != 9) {
//                    compoundAttributeToColumn.put(SpreadhseetVocabulary.GENE_SYMBOL.getKeyName(), cell.getColumnIndex());
//                }
                else if (cell.getStringCellValue().toLowerCase().equals(SpreadhseetVocabulary.GENE_ID.getKeyName()) && cell.getColumnIndex() != 9) {
                    compoundAttributeToColumn.put(SpreadhseetVocabulary.GENE_ID.getKeyName(), cell.getColumnIndex());
                }
                else if (cell.getStringCellValue().toLowerCase().equals(SpreadhseetVocabulary.ENTREZ_GENE_ID.getKeyName()) && cell.getColumnIndex() != 9) {
                    compoundAttributeToColumn.put(SpreadhseetVocabulary.ENTREZ_GENE_ID.getKeyName(), cell.getColumnIndex());
                }
                else if (cell.getStringCellValue().toLowerCase().equals(SpreadhseetVocabulary.UNIPROT_ID.getKeyName()) && cell.getColumnIndex() != 9) {
                    compoundAttributeToColumn.put(SpreadhseetVocabulary.UNIPROT_ID.getKeyName(), cell.getColumnIndex());
                }
                else if (cell.getStringCellValue().toLowerCase().equals(SpreadhseetVocabulary.UNIPROT_ACC.getKeyName()) && cell.getColumnIndex() != 9) {
                    compoundAttributeToColumn.put(SpreadhseetVocabulary.UNIPROT_ACC.getKeyName(), cell.getColumnIndex());
                }
                else if (cell.getStringCellValue().toLowerCase().equals(SpreadhseetVocabulary.HMDB_ID.getKeyName()) && cell.getColumnIndex() != 9) {
                    compoundAttributeToColumn.put(SpreadhseetVocabulary.HMDB_ID.getKeyName(), cell.getColumnIndex());
                }
                else if (cell.getStringCellValue().toLowerCase().equals(SpreadhseetVocabulary.MICROCOSM.getKeyName())) {
                    compoundAttributeToColumn.put(SpreadhseetVocabulary.MICROCOSM.getKeyName(), cell.getColumnIndex());
                }
                else if (cell.getStringCellValue().toLowerCase().equals(SpreadhseetVocabulary.EXPRESSION_STRENGTH.getKeyName())) {
                    compoundAttributeToColumn.put(SpreadhseetVocabulary.EXPRESSION_STRENGTH.getKeyName(), cell.getColumnIndex());
                }
                else if (cell.getStringCellValue().toLowerCase().equals(SpreadhseetVocabulary.DIFFERENTIAL.getKeyName())) {
                    compoundAttributeToColumn.put(SpreadhseetVocabulary.DIFFERENTIAL.getKeyName(), cell.getColumnIndex());
                }
                else if (cell.getStringCellValue().toLowerCase().equals(SpreadhseetVocabulary.RATIO.getKeyName())) {
                    compoundAttributeToColumn.put(SpreadhseetVocabulary.RATIO.getKeyName(), cell.getColumnIndex());
                }
                else if (cell.getStringCellValue().toLowerCase().equals(SpreadhseetVocabulary.P_VALUE.getKeyName())) {
                    compoundAttributeToColumn.put(SpreadhseetVocabulary.P_VALUE.getKeyName(), cell.getColumnIndex());
                }
                else if (cell.getStringCellValue().toLowerCase().equals(SpreadhseetVocabulary.FDR.getKeyName())) {
                    compoundAttributeToColumn.put(SpreadhseetVocabulary.FDR.getKeyName(), cell.getColumnIndex());
                }



            }

        }
    }

    public static void main(String[] args) {

        File file = new File(URI.create("file:/Users/simon/Dropbox/JuppKlein/KUP/datasets/March2011/long_danesh_bis_miRNAGlucosePodocyte/long_danesh_bis_miRNAGlucosePodocyte.xls"));
        new ExperimentSpreadSheetParser(file);

    }

    public KUPExperiment getExperiment() {
        return experiment;
    }
}
