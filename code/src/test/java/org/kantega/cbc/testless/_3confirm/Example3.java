package org.kantega.cbc.testless._3confirm;

import org.kantega.cbyc.Validated;

import java.util.Optional;

public class Example3 {

    public static void main(String[] args) {

        final String digestSubject =
          "Dette er en oppsummering";

        //Vi skal sende ut en oppsummering til en bruker, men
        // 1) Brukeren må finnes
        // 2) Brukeren må ha bekreftet eposten sin
        final Optional<ContactInfo> maybeInfo =
          Database.infoForId("a"); //Prøv med b eller c også

        final Validated<DigestMessage> validatedDigest =
          maybeInfo
            .map(contactInfo -> contactInfo.email)
            .map(emailAddress -> emailAddress.fold(
              unconfirmed -> Validated.<DigestMessage>fail("Epostadressen er ikke bekreftet"),
              confirmed -> Validated.valid(new DigestMessage(confirmed, digestSubject))))
            .orElseGet(() -> Validated.fail("Brukeren finnes ikke i databasen"));

        System.out.println(validatedDigest);

    }
}
