package controller;

import model.ParseTable;
import model.Parser;
import model.Production;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class Program {
    private Parser parser;

    public Program() {
        this.parser = new Parser();
    }

    public Map<String, Set<String>> getFirstSet() {
        return parser.getFirstSet();
    }

    public List<String> getNonTerminals() {
        return parser.getGrammar().getNonTerminals();
    }

    public Set<String> getTerminals() {
        return parser.getGrammar().getTerminals();
    }

    public List<Production> getProductions() {
        return parser.getGrammar().getProductions();
    }

    public List<Production> getProductionsForNonterminal(String nonTerminal) {
        return parser.getGrammar().getProductionsForNonterminal(nonTerminal);
    }

    public String getStartingSymbol() {
        return parser.getGrammar().getStartingSymbol();
    }

    public Map<String, Set<String>> getFollowSet() {
        return parser.getFollowSet();
    }

    public ParseTable getParseTable() {
        return parser.getParseTable();
    }
}
