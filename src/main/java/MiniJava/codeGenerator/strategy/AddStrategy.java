package MiniJava.codeGenerator.strategy;

import MiniJava.codeGenerator.Address;
import MiniJava.codeGenerator.Operation;
import MiniJava.codeGenerator.varType;
import MiniJava.errorHandler.ErrorHandler;
import MiniJava.scanner.token.Token;

public class AddStrategy implements SemanticStrategy {
    private final CodeGeneratorContext context;

    public AddStrategy(CodeGeneratorContext context) {
        this.context = context;
    }

    @Override
    public void execute(Token next) {
        Address temp = new Address(context.getMemory().getTemp(), varType.Int);
        Address s2 = context.getSs().pop();
        Address s1 = context.getSs().pop();

        if (s1.varType != varType.Int || s2.varType != varType.Int) {
            ErrorHandler.printError("In add two operands must be integer");
        }
        context.getMemory().add3AddressCode(Operation.ADD, s1, s2, temp);
        context.getSs().push(temp);
    }
} 