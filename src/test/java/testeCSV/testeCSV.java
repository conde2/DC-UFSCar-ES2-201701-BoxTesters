package testeCSV;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import org.jabref.logic.importer.fileformat.CSVImporter;
import org.jabref.logic.util.FileExtensions;

import org.junit.Test;

import static org.junit.Assert.*;


public class testeCSV {

    @Test
    public void testImportDatabaseBufferedReader() throws IOException {
        CSVImporter dummy = new CSVImporter();
        BufferedReader in = new BufferedReader(new FileReader("lixo"));
        assertEquals(in, dummy.isRecognizedFormat(in));
    }

    @Test
    public void testGetName() {
        CSVImporter dummy = new CSVImporter();
        assertEquals(dummy.getName(), "CSV");
    }

    @Test
    public void testGetExtensions() {
        CSVImporter dummy = new CSVImporter();
        assertEquals(FileExtensions.CSV, dummy.getExtensions());
    }

    @Test
    public void testGetDescription() {
        CSVImporter dummy = new CSVImporter();
        assertEquals("Importer for CSV format.", dummy.getDescription());
    }

}
