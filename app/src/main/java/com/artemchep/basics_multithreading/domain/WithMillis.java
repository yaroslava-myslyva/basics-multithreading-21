package com.artemchep.basics_multithreading.domain;

import androidx.annotation.NonNull;

// Do not modify this file
public class WithMillis<T> {

    @NonNull
    public final T value;

    public final long elapsedMillis;

    public WithMillis(@NonNull T value) {
        this(value, 0L);
    }

    public WithMillis(@NonNull T value, long elapsedMillis) {
        this.value = value;
        this.elapsedMillis = elapsedMillis;
    }

}
