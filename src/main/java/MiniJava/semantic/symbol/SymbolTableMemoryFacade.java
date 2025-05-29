package MiniJava.semantic.symbol;

import MiniJava.codeGenerator.Memory;

public class SymbolTableMemoryFacade {
    private Memory mem;

    public SymbolTableMemoryFacade(Memory mem) {
        this.mem = mem;
    }

    public int getDataAddress() {
        return mem.getDataAddress();
    }
}