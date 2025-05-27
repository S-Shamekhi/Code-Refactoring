package MiniJava.codeGenerator.strategy;

import MiniJava.scanner.token.Token;

public interface SemanticStrategy {
    void execute(Token next);
} 