package org.jabref.logic.importer.util;

import java.util.List;

import org.jabref.logic.groups.DefaultGroupsFactory;
import org.jabref.logic.importer.ParseException;
import org.jabref.logic.l10n.Localization;
import org.jabref.logic.util.MetadataSerializationConfiguration;
import org.jabref.logic.util.strings.QuotedStringTokenizer;
import org.jabref.model.groups.AbstractGroup;
import org.jabref.model.groups.AutomaticKeywordGroup;
import org.jabref.model.groups.AutomaticPersonsGroup;
import org.jabref.model.groups.ExplicitGroup;
import org.jabref.model.groups.GroupHierarchyType;
import org.jabref.model.groups.GroupTreeNode;
import org.jabref.model.groups.KeywordGroup;
import org.jabref.model.groups.RegexKeywordGroup;
import org.jabref.model.groups.SearchGroup;
import org.jabref.model.groups.WordKeywordGroup;
import org.jabref.model.strings.StringUtil;

/**
 * Converts string representation of groups to a parsed {@link GroupTreeNode}.
 */
public class GroupsParser {

    private GroupsParser() {
    }

    public static GroupTreeNode importGroups(List<String> orderedData, Character keywordSeparator)
            throws ParseException {
        try {
            GroupTreeNode cursor = null;
            GroupTreeNode root = null;
            for (String string : orderedData) {
                // This allows to read databases that have been modified by, e.g., BibDesk
                string = string.trim();
                if (string.isEmpty()) {
                    continue;
                }

                int spaceIndex = string.indexOf(' ');
                if (spaceIndex <= 0) {
                    throw new ParseException("Expected \"" + string + "\" to contain whitespace");
                }
                int level = Integer.parseInt(string.substring(0, spaceIndex));
                AbstractGroup group = GroupsParser.fromString(string.substring(spaceIndex + 1), keywordSeparator);
                GroupTreeNode newNode = GroupTreeNode.fromGroup(group);
                if (cursor == null) {
                    // create new root
                    cursor = newNode;
                    root = cursor;
                } else {
                    // insert at desired location
                    while ((level <= cursor.getLevel()) && (cursor.getParent().isPresent())) {
                        cursor = cursor.getParent().get();
                    }
                    cursor.addChild(newNode);
                    cursor = newNode;
                }
            }
            return root;
        } catch (ParseException e) {
            throw new ParseException(Localization
                    .lang("Group tree could not be parsed. If you save the BibTeX library, all groups will be lost."),
                    e);
        }
    }

    /**
     * Re-create a group instance from a textual representation.
     *
     * @param s The result from the group's toString() method.
     * @return New instance of the encoded group.
     * @throws ParseException If an error occurred and a group could not be created,
     *                        e.g. due to a malformed regular expression.
     */
    public static AbstractGroup fromString(String s, Character keywordSeparator)
            throws ParseException {
        if (s.startsWith(MetadataSerializationConfiguration.KEYWORD_GROUP_ID)) {
            return GroupsParser.keywordGroupFromString(s, keywordSeparator);
        }
        if (s.startsWith(MetadataSerializationConfiguration.ALL_ENTRIES_GROUP_ID)) {
            return GroupsParser.allEntriesGroupFromString(s);
        }
        if (s.startsWith(MetadataSerializationConfiguration.SEARCH_GROUP_ID)) {
            return GroupsParser.searchGroupFromString(s);
        }
        if (s.startsWith(MetadataSerializationConfiguration.EXPLICIT_GROUP_ID)) {
            return GroupsParser.explicitGroupFromString(s, keywordSeparator);
        }
        if (s.startsWith(MetadataSerializationConfiguration.LEGACY_EXPLICIT_GROUP_ID)) {
            return GroupsParser.legacyExplicitGroupFromString(s, keywordSeparator);
        }
        if (s.startsWith(MetadataSerializationConfiguration.AUTOMATIC_PERSONS_GROUP_ID)) {
            return GroupsParser.automaticPersonsGroupFromString(s);
        }
        if (s.startsWith(MetadataSerializationConfiguration.AUTOMATIC_KEYWORD_GROUP_ID)) {
            return GroupsParser.automaticKeywordGroupFromString(s);
        }

        throw new ParseException("Unknown group: " + s);
    }

    private static AbstractGroup automaticPersonsGroupFromString(String string) {
        if (!string.startsWith(MetadataSerializationConfiguration.AUTOMATIC_PERSONS_GROUP_ID)) {
            throw new IllegalArgumentException("KeywordGroup cannot be created from \"" + string + "\".");
        }
        QuotedStringTokenizer tok = new QuotedStringTokenizer(string.substring(MetadataSerializationConfiguration.AUTOMATIC_PERSONS_GROUP_ID
                .length()), MetadataSerializationConfiguration.GROUP_UNIT_SEPARATOR, MetadataSerializationConfiguration.GROUP_QUOTE_CHAR);

        String name = StringUtil.unquote(tok.nextToken(), MetadataSerializationConfiguration.GROUP_QUOTE_CHAR);
        GroupHierarchyType context = GroupHierarchyType.getByNumberOrDefault(Integer.parseInt(tok.nextToken()));
        String field = StringUtil.unquote(tok.nextToken(), MetadataSerializationConfiguration.GROUP_QUOTE_CHAR);
        AutomaticPersonsGroup newGroup = new AutomaticPersonsGroup(name, context, field);
        addGroupDetails(tok, newGroup);
        return newGroup;
    }

    private static AbstractGroup automaticKeywordGroupFromString(String string) {
        if (!string.startsWith(MetadataSerializationConfiguration.AUTOMATIC_KEYWORD_GROUP_ID)) {
            throw new IllegalArgumentException("KeywordGroup cannot be created from \"" + string + "\".");
        }
        QuotedStringTokenizer tok = new QuotedStringTokenizer(string.substring(MetadataSerializationConfiguration.AUTOMATIC_KEYWORD_GROUP_ID
                .length()), MetadataSerializationConfiguration.GROUP_UNIT_SEPARATOR, MetadataSerializationConfiguration.GROUP_QUOTE_CHAR);

        String name = StringUtil.unquote(tok.nextToken(), MetadataSerializationConfiguration.GROUP_QUOTE_CHAR);
        GroupHierarchyType context = GroupHierarchyType.getByNumberOrDefault(Integer.parseInt(tok.nextToken()));
        String field = StringUtil.unquote(tok.nextToken(), MetadataSerializationConfiguration.GROUP_QUOTE_CHAR);
        Character delimiter = tok.nextToken().charAt(0);
        Character hierarchicalDelimiter = tok.nextToken().charAt(0);
        AutomaticKeywordGroup newGroup = new AutomaticKeywordGroup(name, context, field, delimiter, hierarchicalDelimiter);
        addGroupDetails(tok, newGroup);
        return newGroup;
    }

    /**
     * Parses s and recreates the KeywordGroup from it.
     *
     * @param s The String representation obtained from
     *          KeywordGroup.toString()
     */
    private static KeywordGroup keywordGroupFromString(String s, Character keywordSeparator) throws ParseException {
        if (!s.startsWith(MetadataSerializationConfiguration.KEYWORD_GROUP_ID)) {
            throw new IllegalArgumentException("KeywordGroup cannot be created from \"" + s + "\".");
        }
        QuotedStringTokenizer tok = new QuotedStringTokenizer(s.substring(MetadataSerializationConfiguration.KEYWORD_GROUP_ID
                .length()), MetadataSerializationConfiguration.GROUP_UNIT_SEPARATOR, MetadataSerializationConfiguration.GROUP_QUOTE_CHAR);

        String name = StringUtil.unquote(tok.nextToken(), MetadataSerializationConfiguration.GROUP_QUOTE_CHAR);
        GroupHierarchyType context = GroupHierarchyType.getByNumberOrDefault(Integer.parseInt(tok.nextToken()));
        String field = StringUtil.unquote(tok.nextToken(), MetadataSerializationConfiguration.GROUP_QUOTE_CHAR);
        String expression = StringUtil.unquote(tok.nextToken(), MetadataSerializationConfiguration.GROUP_QUOTE_CHAR);
        boolean caseSensitive = Integer.parseInt(tok.nextToken()) == 1;
        boolean regExp = Integer.parseInt(tok.nextToken()) == 1;
        KeywordGroup newGroup;
        if (regExp) {
            newGroup = new RegexKeywordGroup(name, context, field, expression, caseSensitive);
        } else {
            newGroup = new WordKeywordGroup(name, context, field, expression, caseSensitive, keywordSeparator, false);
        }
        addGroupDetails(tok, newGroup);
        return newGroup;
    }

    private static ExplicitGroup explicitGroupFromString(String input, Character keywordSeparator) throws ParseException {
        if (!input.startsWith(MetadataSerializationConfiguration.EXPLICIT_GROUP_ID)) {
            throw new IllegalArgumentException("ExplicitGroup cannot be created from \"" + input + "\".");
        }
        QuotedStringTokenizer tok = new QuotedStringTokenizer(input.substring(MetadataSerializationConfiguration.EXPLICIT_GROUP_ID.length()),
                MetadataSerializationConfiguration.GROUP_UNIT_SEPARATOR, MetadataSerializationConfiguration.GROUP_QUOTE_CHAR);

        String name = tok.nextToken();
        try {
            int context = Integer.parseInt(tok.nextToken());
            ExplicitGroup newGroup = new ExplicitGroup(name, GroupHierarchyType.getByNumberOrDefault(context), keywordSeparator);
            addGroupDetails(tok, newGroup);
            return newGroup;
        } catch (NumberFormatException exception) {
            throw new ParseException("Could not parse context in " + input);
        }
    }

    private static ExplicitGroup legacyExplicitGroupFromString(String input, Character keywordSeparator) throws ParseException {
        if (!input.startsWith(MetadataSerializationConfiguration.LEGACY_EXPLICIT_GROUP_ID)) {
            throw new IllegalArgumentException("ExplicitGroup cannot be created from \"" + input + "\".");
        }
        QuotedStringTokenizer tok = new QuotedStringTokenizer(input.substring(MetadataSerializationConfiguration.LEGACY_EXPLICIT_GROUP_ID.length()),
                MetadataSerializationConfiguration.GROUP_UNIT_SEPARATOR, MetadataSerializationConfiguration.GROUP_QUOTE_CHAR);

        String name = tok.nextToken();
        try {
            int context = Integer.parseInt(tok.nextToken());
            ExplicitGroup newGroup = new ExplicitGroup(name, GroupHierarchyType.getByNumberOrDefault(context), keywordSeparator);
            GroupsParser.addLegacyEntryKeys(tok, newGroup);
            return newGroup;
        } catch (NumberFormatException exception) {
            throw new ParseException("Could not parse context in " + input);
        }
    }

    /**
     * Called only when created fromString.
     * JabRef used to store the entries of an explicit group in the serialization, e.g.
     *  ExplicitGroup:GroupName\;0\;Key1\;Key2\;;
     * This method exists for backwards compatibility.
     */
    private static void addLegacyEntryKeys(QuotedStringTokenizer tok, ExplicitGroup group) {
        while (tok.hasMoreTokens()) {
            String key = StringUtil.unquote(tok.nextToken(), MetadataSerializationConfiguration.GROUP_QUOTE_CHAR);
            group.addLegacyEntryKey(key);
        }
    }

    private static AbstractGroup allEntriesGroupFromString(String s) {
        if (!s.startsWith(MetadataSerializationConfiguration.ALL_ENTRIES_GROUP_ID)) {
            throw new IllegalArgumentException("AllEntriesGroup cannot be created from \"" + s + "\".");
        }
        return DefaultGroupsFactory.getAllEntriesGroup();
    }

    /**
     * Parses s and recreates the SearchGroup from it.
     *
     * @param s The String representation obtained from
     *          SearchGroup.toString(), or null if incompatible
     */
    private static AbstractGroup searchGroupFromString(String s) {
        if (!s.startsWith(MetadataSerializationConfiguration.SEARCH_GROUP_ID)) {
            throw new IllegalArgumentException("SearchGroup cannot be created from \"" + s + "\".");
        }
        QuotedStringTokenizer tok = new QuotedStringTokenizer(s.substring(MetadataSerializationConfiguration.SEARCH_GROUP_ID.length()),
                MetadataSerializationConfiguration.GROUP_UNIT_SEPARATOR, MetadataSerializationConfiguration.GROUP_QUOTE_CHAR);

        String name = tok.nextToken();
        int context = Integer.parseInt(tok.nextToken());
        String expression = tok.nextToken();
        boolean caseSensitive = Integer.parseInt(tok.nextToken()) == 1;
        boolean regExp = Integer.parseInt(tok.nextToken()) == 1;
        // version 0 contained 4 additional booleans to specify search
        // fields; these are ignored now, all fields are always searched
        return new SearchGroup(StringUtil.unquote(name, MetadataSerializationConfiguration.GROUP_QUOTE_CHAR),
                GroupHierarchyType.getByNumberOrDefault(context), StringUtil.unquote(expression, MetadataSerializationConfiguration.GROUP_QUOTE_CHAR), caseSensitive, regExp
        );
    }

    private static void addGroupDetails(QuotedStringTokenizer tokenizer, AbstractGroup group) {
        if (tokenizer.hasMoreTokens()) {
            group.setExpanded(Integer.parseInt(tokenizer.nextToken()) == 1);
            group.setColor(tokenizer.nextToken());
            group.setIconName(tokenizer.nextToken());
            group.setDescription(tokenizer.nextToken());
        }
    }
}