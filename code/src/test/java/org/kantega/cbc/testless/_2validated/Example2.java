package org.kantega.cbc.testless._2validated;

import org.kantega.cbyc.Validated;

public class Example2 {

    public static void main(String[] args) {
        Validated<EmailAddress> email1 = EmailAddress.of("ola.normann@test.com");
        Validated<EmailAddress> email2 = EmailAddress.of("ol.normann_test");

        //Skriver ut epost
        System.out.println(email1);

        //Skriver ut en feilmelding
        System.out.println(email2);


        Validated<ContactInfo> validatedInfo =
          Validated.accum(
            EmailAddress.of("ola.normann@test.com"),
            Phonenumber.of("12345678"),
            ContactInfo::new
          );

        //Skriver ut contactinfo
        System.out.println(validatedInfo);

        Validated<ContactInfo> invalidInfo =
          Validated.accum(
            EmailAddress.of("ola.normann_test.com"),
            Phonenumber.of("abcdefghij"),
            ContactInfo::new
          );

        //Skriver ut feilmelding
        System.out.println(invalidInfo);
    }
}
