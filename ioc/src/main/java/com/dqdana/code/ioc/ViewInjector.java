package com.dqdana.code.ioc;

public interface ViewInjector<T> {
    void inject(T t, Object source);
}