package model;

import javafx.util.Pair;

import java.util.*;

public class Parser {
    private Grammar grammar;
    private Map<String, Set<String>> firstSet;
    private Map<String, Set<String>> followSet;
    private ParseTable parseTable = new ParseTable();
    private static Stack<List<String>> rules = new Stack<>();
    private Map<Pair<String, List<String>>, Integer> productionsNumbered = new HashMap<>();

    public Parser() {
        this.grammar = new Grammar();
        this.firstSet = new HashMap<>();
        this.followSet = new HashMap<>();
        generateSets();
    }

    private void generateSets() {
        generateFirstSet();
        generateFollowSet();
        createParseTable();
    }

    private void generateFirstSet() {
        for (String nonTerminal : grammar.getNonTerminals()) {
            firstSet.put(nonTerminal, this.firstOf(nonTerminal));
        }
    }

    private Set<String> firstOf(String nonTerminal) {
        if (firstSet.containsKey(nonTerminal))
            return firstSet.get(nonTerminal);
        Set<String> temp = new HashSet<>();
        Set<String> terminals = grammar.getTerminals();
        for (Production production : grammar.getProductionsForNonterminal(nonTerminal))
            for (List<String> rule : production.getRules()) {
                String firstSymbol = rule.get(0);
                if (firstSymbol.equals("ε"))
                    temp.add("ε");
                else if (terminals.contains(firstSymbol))
                    temp.add(firstSymbol);
                else
                    temp.addAll(firstOf(firstSymbol));
            }
        return temp;
    }

    private void generateFollowSet() {
        for (String nonTerminal : grammar.getNonTerminals()) {
            followSet.put(nonTerminal, this.followOf(nonTerminal, nonTerminal));
        }
    }

    private Set<String> followOf(String nonTerminal, String initialNonTerminal) {
        if (followSet.containsKey(nonTerminal))
            return followSet.get(nonTerminal);
        Set<String> temp = new HashSet<>();
        Set<String> terminals = grammar.getTerminals();

        if (nonTerminal.equals(grammar.getStartingSymbol()))
            temp.add("$");

        for (Production production : grammar.getProductionsContainingNonterminal(nonTerminal)) {
            String productionStart = production.getStart();
            for (List<String> rule : production.getRules()){
                List<String> ruleConflict = new ArrayList<>();
                ruleConflict.add(nonTerminal);
                ruleConflict.addAll(rule);
                if (rule.contains(nonTerminal) && !rules.contains(ruleConflict)) {
                    rules.push(ruleConflict);
                    int indexNonTerminal = rule.indexOf(nonTerminal);
                    temp.addAll(followOperation(nonTerminal, temp, terminals, productionStart, rule, indexNonTerminal, initialNonTerminal));

//                    // For cases like: N -> E 36 E, when E is the nonTerminal so we have 2 possibilities: 36 goes in follow(E) and also follow(N)
                    List<String> sublist = rule.subList(indexNonTerminal + 1, rule.size());
                    if (sublist.contains(nonTerminal))
                        temp.addAll(followOperation(nonTerminal, temp, terminals, productionStart, rule, indexNonTerminal + 1 + sublist.indexOf(nonTerminal), initialNonTerminal));

                    rules.pop();
                }
            }
        }

        return temp;
    }

    private Set<String> followOperation(String nonTerminal, Set<String> temp, Set<String> terminals, String productionStart, List<String> rule, int indexNonTerminal, String initialNonTerminal) {
        if (indexNonTerminal == rule.size() - 1) {
            if (productionStart.equals(nonTerminal))
                return temp;
            if (!initialNonTerminal.equals(productionStart)){
                temp.addAll(followOf(productionStart, initialNonTerminal));
            }
        }
        else
        {
            String nextSymbol = rule.get(indexNonTerminal + 1);
            if (terminals.contains(nextSymbol))
                temp.add(nextSymbol);
            else{
                if (!initialNonTerminal.equals(nextSymbol)) {
                    Set<String> fists = new HashSet<>(firstSet.get(nextSymbol));
                    if (fists.contains("ε")) {
                        temp.addAll(followOf(nextSymbol, initialNonTerminal));
                        fists.remove("ε");
                    }
                    temp.addAll(fists);
                }
            }
        }
        return temp;
    }

    private void createParseTable() {
        numberingProductions();
        List<String> rowSymbols = new LinkedList<>();
        rowSymbols.addAll(grammar.getNonTerminals());
        rowSymbols.addAll(grammar.getTerminals());
        rowSymbols.add("$");

        List<String> columnSymbols = new LinkedList<>(grammar.getTerminals());
        columnSymbols.add("$");

        // M(a, a) = pop
        // M($, $) = acc

        parseTable.put(new Pair<>("$", "$"), new Pair<>(Collections.singletonList("acc"), -1));
        for (String terminal: grammar.getTerminals())
            parseTable.put(new Pair<>(terminal, terminal), new Pair<>(Collections.singletonList("pop"), -1));


         /*
          1) M(A, a) = (α, i), if:
                a) a ∈ first(α)
                b) a != ε
                c) A -> α production with index i

           2) M(A, b) = (α, i), if:
                a) ε ∈ first(α)
                b) whichever b ∈ follow(A)
                c) A -> α production with index i
        */

        productionsNumbered.forEach((key, value) -> {
            String rowSymbol = key.getKey();
            List<String> rule = key.getValue();
            Pair<List<String>, Integer> parseTableValue = new Pair<>(rule, value);
            for (String columnSymbol : columnSymbols){
                Pair<String, String> parseTableKey = new Pair<>(rowSymbol, columnSymbol);
                if (rule.get(0).equals(columnSymbol) && !columnSymbol.equals("ε"))
                    parseTable.put(parseTableKey, parseTableValue);
                else if (rule.get(0).equals("ε")) {
                    List<String> followRowSymbol = new ArrayList<>(followSet.get(rowSymbol));
                    for (String aFollowRowSymbol : followRowSymbol) {
                        String b = String.valueOf(aFollowRowSymbol);
                        if (b.equals("ε")) {
                            parseTableKey = new Pair<>(rowSymbol, "$");
                        } else {
                            parseTableKey = new Pair<>(rowSymbol, b);
                        }

                        if (!parseTable.containsKey(parseTableKey)) {
                            parseTable.put(parseTableKey, parseTableValue);
                        }
                    }


                }
            }
        });

        System.out.println(parseTable);
    }

    private void numberingProductions() {
        int index = 1;
        for (Production production: grammar.getProductions())
            for (List<String> rule: production.getRules())
                productionsNumbered.put(new Pair<>(production.getStart(), rule), index++);
        System.out.println(productionsNumbered);
    }

    private List<Pair<Integer, Production>> getIndexedProductionsForNonTerminal(String nonTerminal) {
        List<Pair<Integer, Production>> indexedProductionsForNonTerminal = new LinkedList<>();

        for (Pair<Integer, Production> indexedProduction : getIndexedProductions()) {
            Production production = indexedProduction.getValue();
            if (production.getStart().equals(nonTerminal)) {
                indexedProductionsForNonTerminal.add(indexedProduction);
            }
        }

        return indexedProductionsForNonTerminal;
    }

    private List<Pair<Integer, Production>> getIndexedProductions() {
        List<Pair<Integer, Production>> indexedProductions = new LinkedList<>();

        int i = 1;
        for (Production production : grammar.getProductions()) {
            for (List<String> rule : production.getRules()) {
                Production newProduction = new Production(production.getStart(), Collections.singletonList(rule));
                indexedProductions.add(new Pair<>(i, newProduction));
                i++;
            }
        }

        return indexedProductions;
    }

    public Grammar getGrammar() {
        return grammar;
    }

    public Map<String, Set<String>> getFirstSet() {
        return firstSet;
    }

    public Map<String, Set<String>> getFollowSet() {
        return followSet;
    }

    public ParseTable getParseTable() {
        return parseTable;
    }
}
