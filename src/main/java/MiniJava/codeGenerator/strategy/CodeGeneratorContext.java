package MiniJava.codeGenerator.strategy;

import MiniJava.codeGenerator.Address;
import MiniJava.codeGenerator.Memory;
import MiniJava.semantic.symbol.SymbolTable;

import java.util.Stack;

public class CodeGeneratorContext {
    private final Memory memory;
    private final Stack<Address> ss;
    private final SymbolTable symbolTable;
    private final Stack<String> symbolStack;

    public CodeGeneratorContext(Memory memory, Stack<Address> ss, SymbolTable symbolTable, Stack<String> symbolStack) {
        this.memory = memory;
        this.ss = ss;
        this.symbolTable = symbolTable;
        this.symbolStack = symbolStack;
    }

    public Memory getMemory() {
        return memory;
    }

    public Stack<Address> getSs() {
        return ss;
    }

    public SymbolTable getSymbolTable() {
        return symbolTable;
    }

    public Stack<String> getSymbolStack() {
        return symbolStack;
    }
} 