package org.kantega.cbc.testless._2validated;

import org.apache.commons.validator.routines.EmailValidator;
import org.kantega.cbyc.Validated;

public class EmailAddress {

    final String value;

    private EmailAddress(String value) {
        this.value = value;
    }

    public static Validated<EmailAddress> of(String value) {
        return
          EmailValidator.getInstance().isValid(value) ?
            Validated.valid(new EmailAddress(value)) :
            Validated.fail("Feil format");
    }

    @Override
    public String toString() {
        return "Email{" +
          "value='" + value + '\'' +
          '}';
    }
}
