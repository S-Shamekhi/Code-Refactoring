package MiniJava.codeGenerator.strategy;

import MiniJava.codeGenerator.Address;
import MiniJava.codeGenerator.varType;
import MiniJava.semantic.symbol.Symbol;
import MiniJava.scanner.token.Token;

public class PidStrategy implements SemanticStrategy {
    private final CodeGeneratorContext context;

    public PidStrategy(CodeGeneratorContext context) {
        this.context = context;
    }

    @Override
    public void execute(Token next) {
        if (context.getSymbolStack().size() > 1) {
            String methodName = context.getSymbolStack().pop();
            String className = context.getSymbolStack().pop();
            try {
                Symbol s = context.getSymbolTable().get(className, methodName, next.value);
                varType t = varType.Int;
                switch (s.type) {
                    case Bool:
                        t = varType.Bool;
                        break;
                    case Int:
                        t = varType.Int;
                        break;
                }
                context.getSs().push(new Address(s.address, t));
            } catch (Exception e) {
                context.getSs().push(new Address(0, varType.Non));
            }
            context.getSymbolStack().push(className);
            context.getSymbolStack().push(methodName);
        } else {
            context.getSs().push(new Address(0, varType.Non));
        }
        context.getSymbolStack().push(next.value);
    }
} 