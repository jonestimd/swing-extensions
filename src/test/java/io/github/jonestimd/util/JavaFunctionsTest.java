package io.github.jonestimd.util;

import java.util.function.Function;

import org.junit.Test;

import static org.fest.assertions.Assertions.*;

public class JavaFunctionsTest {
    @Test
    public void nullGardReturnsNullForNullObject() throws Exception {
        Function<Object, String> nullSafe = JavaFunctions.nullGuard(Object::toString);

        assertThat(nullSafe.apply(null)).isNull();
        assertThat(nullSafe.apply(100)).isEqualTo("100");
    }
}