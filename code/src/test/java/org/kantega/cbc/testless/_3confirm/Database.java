package org.kantega.cbc.testless._3confirm;

import org.kantega.cbyc.Validated;

import java.time.Instant;
import java.util.Optional;

public class Database {

    public static Optional<ContactInfo> infoForId(String id) {
        return
          id.equals("a") ?
            Validated
              .accum(EmailAddress.of("ola.normann@mail.com"), Phonenumber.of("1234567"), ContactInfo::new)
              .fold(
                f -> Optional.empty(),
                Optional::of) :
            id.equals("b") ?
              Validated
                .accum(EmailAddress.validated(Instant.now(),"ola.normann@mail.com"), Phonenumber.of("1234567"), ContactInfo::new)
                .fold(
                  f -> Optional.empty(),
                  Optional::of) :
              Optional.empty();
    }

}
