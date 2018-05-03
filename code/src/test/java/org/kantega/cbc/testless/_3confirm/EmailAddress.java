package org.kantega.cbc.testless._3confirm;

import org.apache.commons.validator.routines.EmailValidator;
import org.kantega.cbyc.Validated;

import java.time.Instant;
import java.util.function.Function;

public interface EmailAddress {

    <T> T fold(
      Function<Unconfirmed, T> onUnconfirmed,
      Function<Confirmed, T> onConfirmed
    );


    static Validated<EmailAddress> of(String value) {
        return
          EmailValidator.getInstance().isValid(value) ?
            Validated.valid(new Unconfirmed(value)) :
            Validated.fail("Feil format");
    }

    static Validated<EmailAddress> validated(Instant instant, String value) {
        return
          EmailValidator.getInstance().isValid(value) ?
            Validated.valid(new Confirmed(instant,value)) :
            Validated.fail("Feil format");
    }

    class Unconfirmed implements EmailAddress {

        public final String value;

        public Unconfirmed(String value) {
            this.value = value;
        }

        public Confirmed confirm(Instant timestamp){
            return new Confirmed(timestamp, value);
        }

        @Override
        public <T> T fold(Function<Unconfirmed, T> onUnconfirmed, Function<Confirmed, T> onConfirmed) {
            return onUnconfirmed.apply(this);
        }

        @Override
        public String toString() {
            return "Unconfirmed{" +
              "value='" + value + '\'' +
              '}';
        }
    }

    class Confirmed implements EmailAddress {

        public final Instant timestamp;
        public final String value;

        private Confirmed(Instant timestamp, String value) {
            this.timestamp = timestamp;
            this.value = value;
        }

        @Override
        public <T> T fold(Function<Unconfirmed, T> onUnconfirmed, Function<Confirmed, T> onConfirmed) {
            return onConfirmed.apply(this);
        }

        @Override
        public String toString() {
            return "Confirmed{" +
              "timestamp=" + timestamp +
              ", value='" + value + '\'' +
              '}';
        }
    }
}
