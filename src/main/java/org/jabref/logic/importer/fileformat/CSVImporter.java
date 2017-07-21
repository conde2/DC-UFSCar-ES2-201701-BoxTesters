package org.jabref.logic.importer.fileformat;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.jabref.logic.importer.Importer;
import org.jabref.logic.importer.ParserResult;
import org.jabref.logic.util.FileExtensions;
import org.jabref.model.entry.BibEntry;

public class CSVImporter extends Importer {

    @Override
    public String getName() {
        return "CSV";
    }

    @Override
    public FileExtensions getExtensions() {
        return FileExtensions.CSV;
    }

    @Override
    public String getDescription() {
        return "Importer for CSV format.";
    }

    @Override
    public boolean isRecognizedFormat(BufferedReader reader) throws IOException {
        String buffer;

        return false;
    }

    @Override //ver OvidImporter.java, BibTeXMLImporter.java
    public ParserResult importDatabase(BufferedReader input) throws IOException {
        List<BibEntry> bibitems = new ArrayList<>();

        return new ParserResult(bibitems);
    }


}