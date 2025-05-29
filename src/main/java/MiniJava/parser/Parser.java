package MiniJava.parser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Stack;

import MiniJava.Log.Log;
import MiniJava.codeGenerator.CodeGenerator;
import MiniJava.errorHandler.ErrorHandler;
import MiniJava.scanner.lexicalAnalyzer;
import MiniJava.scanner.token.Token;

public class Parser {
    private ArrayList<Rule> rules;
    private Stack<Integer> parsStack;
    private ParseTable parseTable;
    private lexicalAnalyzer lexicalAnalyzer;
    private ParserCodeGeneratorFacade cg;

    public Parser() {
        setParsStack(new Stack<>());
        getParsStack().push(0);
        try {
            setParseTable(new ParseTable(Files.readAllLines(Paths.get("src/main/resources/parseTable")).get(0)));
        } catch (Exception e) {
            e.printStackTrace();
        }

        setRules(new ArrayList<>());
        try {
            for (String stringRule : Files.readAllLines(Paths.get("src/main/resources/Rules"))) {
                getRules().add(new Rule(stringRule));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        setCg(new ParserCodeGeneratorFacade());
    }

    public void startParse(java.util.Scanner sc) {
        setLexicalAnalyzer(new lexicalAnalyzer(sc));
        Token lookAhead = getLexicalAnalyzer().getNextToken();
        boolean finish = false;
        Action currentAction;

        while (!finish) {
            try {
                Log.print(lookAhead.toString() + "\t" + getParsStack().peek());

                currentAction = getParseTable().getActionTable(getParsStack().peek(), lookAhead);
                Log.print(currentAction.toString());

                switch (currentAction.action) {
                    case shift:
                        getParsStack().push(currentAction.number);
                        lookAhead = getLexicalAnalyzer().getNextToken();
                        break;

                    case reduce:
                        Rule rule = getRules().get(currentAction.number);
                        for (int i = 0; i < rule.RHS.size(); i++) {
                            getParsStack().pop();
                        }

                        Log.print(getParsStack().peek() + "\t" + rule.LHS);
                        getParsStack().push(getParseTable().getGotoTable(getParsStack().peek(), rule.LHS));
                        Log.print(getParsStack().peek() + "");

                        try {
                            getCg().semanticFunction(rule.semanticAction, lookAhead);
                        } catch (Exception e) {
                            Log.print("Code Genetator Error");
                        }
                        break;

                    case accept:
                        finish = true;
                        break;
                }
                Log.print("");
            } catch (Exception e) {
                handleParseError(e);
            }
        }

        if (!ErrorHandler.hasError)
            getCg().printMemory();
    }

    private void handleParseError(Exception e) {
        e.printStackTrace();
    }

    // Self-encapsulated getters and setters

    private ArrayList<Rule> getRules() {
        return rules;
    }

    private void setRules(ArrayList<Rule> rules) {
        this.rules = rules;
    }

    private Stack<Integer> getParsStack() {
        return parsStack;
    }

    private void setParsStack(Stack<Integer> parsStack) {
        this.parsStack = parsStack;
    }

    private ParseTable getParseTable() {
        return parseTable;
    }

    private void setParseTable(ParseTable parseTable) {
        this.parseTable = parseTable;
    }

    private lexicalAnalyzer getLexicalAnalyzer() {
        return lexicalAnalyzer;
    }

    private void setLexicalAnalyzer(lexicalAnalyzer lexicalAnalyzer) {
        this.lexicalAnalyzer = lexicalAnalyzer;
    }

    private ParserCodeGeneratorFacade getCg() {
        return cg;
    }

    private void setCg(ParserCodeGeneratorFacade cg) {
        this.cg = cg;
    }
}
