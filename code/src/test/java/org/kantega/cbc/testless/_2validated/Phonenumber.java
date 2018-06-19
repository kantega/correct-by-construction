package org.kantega.cbc.testless._2validated;

import fj.Digit;
import fj.data.List;
import fj.data.NonEmptyList;
import fj.data.Option;
import fj.data.Stream;
import org.kantega.cbyc.Validated;

public class Phonenumber {

    final NonEmptyList<Digit> digits;

    private Phonenumber(NonEmptyList<Digit> digits) {
        this.digits = digits;
    }

    public static Validated<Phonenumber> of(String numberAsString) {

        List<Digit> digitList = Option.somes(Stream.fromString(numberAsString).map(Digit::fromChar)).toList();

        return digitList.isEmpty() ?
          Validated.invalid("Feil format på input, det må være minst ett tall") :
          Validated.valid(new Phonenumber(NonEmptyList.nel(digitList.head(), digitList.tail())));
    }

    @Override
    public String toString() {
        return "Phonenumber{" +
          "digits=" + digits +
          '}';
    }
}
