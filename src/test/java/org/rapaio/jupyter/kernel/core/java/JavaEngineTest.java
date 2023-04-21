package org.rapaio.jupyter.kernel.core.java;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class JavaEngineTest {

    @Test
    void validBuildTest() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> JavaEngine.builder().build());
        assertEquals("Timeout must be specified.", e.getMessage());
    }

    @Test
    void buildTest() throws Exception {
        JavaEngine engine = JavaEngine.builder()
                .withTimeoutMillis(-1L)
                .build();

        var result = engine.eval("int x = 3;int y = 4;");
        System.out.println(result);
    }
}
