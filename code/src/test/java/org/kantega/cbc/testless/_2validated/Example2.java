package org.kantega.cbc.testless._2validated;

import org.kantega.cbyc.Validated;

public class Example2 {

    public static void main(String[] args) {

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
