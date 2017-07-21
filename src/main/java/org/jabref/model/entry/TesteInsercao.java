package org.jabref.model.entry;

import java.util.*;
import java.util.Map;


import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TesteInsercao {

    private BibEntry keywordEntry;
    private BibEntry emptyEntry;

    @Before
    public void setUp() {
        keywordEntry = new BibEntry();
        keywordEntry.setType(BibtexEntryTypes.ARTICLE);
        keywordEntry.setField("keywords", "Foo, Bar");
        keywordEntry.setChanged(false);

        emptyEntry = new BibEntry();
        emptyEntry.setType("article");
        emptyEntry.setChanged(false);
    }


    //testes da bibtexkey


    @Test
    public void testeBibMenosDoisCaracter() {
        keywordEntry.setField("bibtexkey", "a"); //menos de dois caracteres
        Map<String, String> fieldBib = keywordEntry.getFieldMap();
        assertEquals("E", fieldBib.get("bibtexkey"));
    }


    @Test
    public void testeBibNumeroInicio() {
        keywordEntry.setField("bibtexkey", "9AAA"); //iniciando com numero
        Map<String, String> fieldBib = keywordEntry.getFieldMap();
        assertEquals("2017ES2", fieldBib.get("bibtexkey"));
    }


    @Test
    public void testeBibCaracterEspecial() {
        keywordEntry.setField("bibtexkey", "#AAA"); //caractere especial
        Map<String, String> fieldBib = keywordEntry.getFieldMap();
        assertEquals("#AAA", fieldBib.get("bibtexkey"));
    }

    //teste da inseção de autores

    @Test
    public void testeAutorPontoeVirgula() {
        BibEntry objeto = new BibEntry();
        objeto.setField("author", "Coelho; Paulo"); //ponto e virgula
        assertEquals(Optional.of("Coelho; Paulo"), objeto.getField("author"));
    }

    @Test
    public void testeAutorPontorFinal() {
        BibEntry objeto = new BibEntry();
        objeto.setField("author", "Monteiro. Lobato"); //ponto final
        assertEquals(Optional.of("Monteiro. Lobato"), objeto.getField("author"));

    }

    @Test
    public void testeAutorCaracterEspecialMeio() {
        BibEntry objeto = new BibEntry();
        objeto.setField("author", "Machado de #assis"); //caractere especial no meio
        assertEquals(Optional.empty(), objeto.getField("author"));
    }

    @Test
    public void testeAutorCaracterNumericoFinal() {
        BibEntry objeto = new BibEntry();
        objeto.setField("author", "Guimarães rosa 73"); //caractere numerico
        assertEquals(Optional.empty(), objeto.getField("author"));
    }


    @Test
    public void testaCitacao() {
        BibEntry be = new BibEntry();
        Assert.assertFalse(be.hasCiteKey());
        be.setField("author", "Paulo Coelho");
        be.setCiteKey("PauloCoelho99");
        Assert.assertTrue(be.hasCiteKey());
        assertEquals(Optional.of("PauloCoelho99"), be.getCiteKeyOptional());
        assertEquals(Optional.of("Paulo Coelho"), be.getField("author"));
        be.clearField("author");
        assertEquals(Optional.empty(), be.getField("author"));
    }

    @Test
    public void testaGrupoeProcura() {
        BibEntry objeto = new BibEntry();
        objeto.setGroupHit(true);
        Assert.assertTrue(objeto.isGroupHit());
        objeto.setGroupHit(false);
        Assert.assertFalse(objeto.isGroupHit());
        objeto.setSearchHit(true);
        Assert.assertTrue(objeto.isSearchHit());
        objeto.setSearchHit(false);
        Assert.assertFalse(objeto.isSearchHit());

    }


}