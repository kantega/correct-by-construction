package org.kantega.cbc.testless.stage2;

import org.kantega.cbyc.Validated;

public class RunValidation2 {

    public static void main(String[] args) {
        Validated<Email> email1 = Email.of("ola.normann@test.com");
        Validated<Email> email2 = Email.of("ol.normann_test");

        //Skriver ut epost
        System.out.println(email1);

        //Skriver ut en feilmelding
        System.out.println(email2);
    }
}
