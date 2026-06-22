package com.craftinginterpreters.my.lox;

import java.util.*;

interface LoxCallable {

    int arity();

    Object call(Interpreter interpreter, List<Object> arguments);
}