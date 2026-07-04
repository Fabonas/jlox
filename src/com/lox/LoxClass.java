package com.lox;

import java.util.List;
import java.util.Map;

public class LoxClass implements LoxCallable {
    final String name;
    final LoxClass superclass;
    private final Map<String, LoxFunction> methods;

    LoxClass(String name, LoxClass superclass, Map<String, LoxFunction> methods) {
        this.superclass = superclass;
        this.name = name;
        this.methods = methods;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public Object call(Interpreter inter, List<Object> args) {
        LoxInstance instance = new LoxInstance(this);
        LoxFunction init = findMethod("init");

        if (init != null) init.bind(instance).call(inter, args);

        return instance;
    }

    LoxFunction findMethod(String name) {
        if (methods.containsKey(name)) return methods.get(name);

        if (superclass != null) return superclass.findMethod(name);

        return null;
    }

    @Override
    public int arity() {
        LoxFunction init = findMethod("init");
        if (init == null) return 0;

        return init.arity();
    }
}
