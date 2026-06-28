package com.craftinginterpreters.my.lox;

import java.util.HashMap;
import java.util.Map;

public class LoxInstance {

    private LoxClass clazz;
    private final Map<String, Object> fields = new HashMap<>();

    public LoxInstance(LoxClass loxClass) {
        this.clazz = loxClass;
    }

    @Override
    public String toString() {
        return clazz.name + " instance";
    }

    /** 메서드는 Class가 소유하지만, 그 클래스의 인스턴스를 통하여 access한다. */
    Object get(Token name) {
        if (fields.containsKey(name.lexeme)) {
            return fields.get(name.lexeme);
        }

        LoxFunction method = clazz.findMethod(name.lexeme);
        if(method != null) return method.bind(this);

        throw new RuntimeError(name, "Undefined property '" + name.lexeme + "'.");
    }

    void set(Token name, Object value) {
        fields.put(name.lexeme, value);
    }
}
