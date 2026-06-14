package com.craftinginterpreters.my.lox;

import java.util.HashMap;
import java.util.Map;

class Environment {
    // 선언만 되고 값이 한 번도 들어가지 않은 변수를 표시하는 표식.
    // 진짜 nil(null)과 구분하기 위해 별도의 유일한 객체를 쓴다.
    static final Object UNINITIALIZED = new Object();

    final Environment enclosing;
    private final Map<String, Object> values = new HashMap<>();

    Environment() {
        enclosing = null;
    }

    Environment(Environment enclosing) {
        this.enclosing = enclosing;
    }

    void define(String name, Object value) {
        values.put(name, value);
    }

    Object get(Token name) {
        if (values.containsKey(name.lexeme)) {
            Object value = values.get(name.lexeme);
            if (value == UNINITIALIZED) {
                throw new RuntimeError(name,
                        "Uninitialized variable '" + name.lexeme + "'.");
            }
            return value;
        }

        if (enclosing != null) {
            return enclosing.get(name);
        }

        throw new RuntimeError(name, "Undefined variable '" + name.lexeme + "'.");
    }

    void assign(Token name, Object value) {
        if (values.containsKey(name.lexeme)) {
            values.put(name.lexeme, value);
            return ;
        }

        if (enclosing != null) {
            enclosing.assign(name, value);
            return ;
        }

        throw new RuntimeError(name, "Undefined variable '" + name.lexeme + "'.");
    }
}