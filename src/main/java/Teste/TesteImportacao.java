package Teste;

import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;

import org.jabref.logic.importer.ImportFormatPreferences;
import org.jabref.logic.importer.ImportFormatReader;
import org.jabref.logic.xmp.XMPPreferences;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.Answers;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(Parameterized.class)
public class TesteImportacao {

  private ImportFormatReader reader;

    private final int count;
    public final String format;
    private final Path file;

    //Criação da classe para teste de importação que recebe os três parâmetros
    public TesteImportacao(String resource, String format, int count) throws URISyntaxException {
        this.format = format;
        this.count = count;
        this.file = Paths.get(TesteImportacao.class.getResource(resource).toURI());
    }

    @Before
    public void setUp() {
        reader = new ImportFormatReader();
        ImportFormatPreferences importFormatPreferences = mock(ImportFormatPreferences.class, Answers.RETURNS_DEEP_STUBS);
        when(importFormatPreferences.getEncoding()).thenReturn(StandardCharsets.UTF_8);
        reader.resetImportFormats(importFormatPreferences, mock(XMPPreferences.class));
    }

    @Test
    //Teste para verificar se o formato é desconhecido
    public void testImportUnknownFormat() throws Exception {
        ImportFormatReader.UnknownFormatImport unknownFormat = reader.importUnknownFormat(file);
        assertEquals(count, unknownFormat.parserResult.getDatabase().getEntryCount());
    }

    @Test
    //Teste para verificar correspondência do arquivo
    public void testImportFormatFromFile() throws Exception {
        assertEquals(count, reader.importFromFile(format, file).getDatabase().getEntries().size());
    }

    @Parameterized.Parameters(name = "{index}: {1}")
    public static Collection<Object[]> importFormats() {
        Collection<Object[]> result = new ArrayList<>();
        //Importando os items bibliográficos na base corrente
        result.add(new Object[] {"itensBibliograficos/IsiImporterTestMedline.isi", "isi", 1});
        result.add(new Object[] {"itensBibliograficos/Endnote.pattern.E.enw", "refer", 1});
        result.add(new Object[] {"itensBibliograficos/RisImporterTestScience.ris", "ris", 1});
        result.add(new Object[] {"itensBibliograficos/BiblioscapeImporterTestJournalArticle.txt", "txt", 1});
        result.add(new Object[] {"itensBibliograficos/BibTeXMLImporterTestBooklet.xml", "msbib", 1});

        return result;
    }
}