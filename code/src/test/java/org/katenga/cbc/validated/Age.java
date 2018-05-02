package org.katenga.cbc.validated;

import org.kantega.cbyc.Validated;

public class Age {

    public final int value;

    private Age(int value) {
        this.value = value;
    }

    /**
     * The only way to create an Age is through this method, thereby assuring that it is valid if it exists.
     * @param value
     * @return
     */
    public static Validated<Age> toAge(int value) {
        return Validated.validate(value, v -> (v >= 0 && v < 150), "The age must be in the range [0,150)").map(Age::new);
    }

    @Override
    public String toString() {
        return "Age{" +
          "value=" + value +
          '}';
    }
}
