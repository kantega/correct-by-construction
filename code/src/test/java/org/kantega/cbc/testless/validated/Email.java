package org.kantega.cbc.testless.validated;

import org.apache.commons.validator.routines.EmailValidator;
import org.kantega.cbyc.Validated;

public class Email {

    final String value;

    private Email(String value) {
        this.value = value;
    }

    public static Validated<Email> of(String value) {
        return
          EmailValidator.getInstance().isValid(value) ?
            Validated.valid(new Email(value)) :
            Validated.fail("Feil format");
    }

    @Override
    public String toString() {
        return "Email{" +
          "value='" + value + '\'' +
          '}';
    }
}
