package me.oskar.spl.codegen.x86.program;

public enum OpCode {
    PUSH("push"),
    POP("pop"),
    MOV("mov"),
    CALL("call"),
    RET("ret"),
    CMP("cmp"),
    JE("je"),
    JNE("jne"),
    JG("jg"),
    JGE("jge"),
    JL("jl"),
    JLE("jle"),
    JMP("jmp"),
    LEA("lea"),
    ADD("add"),
    SUB("sub"),
    IMUL("imul"),
    IDIV("idiv"),
    CQO("cqo"),
    XOR("xor");

    private final String mnemonic;

    OpCode(String mnemonic) {
        this.mnemonic = mnemonic;
    }

    @Override
    public String toString() {
        return mnemonic;
    }
}
