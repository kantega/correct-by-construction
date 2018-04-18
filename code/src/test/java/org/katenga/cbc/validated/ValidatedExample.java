package org.katenga.cbc.validated;

import org.kantega.cbyc.Validated;

public class ValidatedExample {

    public static void main(String[] args) {
        var settings = Settings.empty();

        var usernameV = settings.getAsString("username");
        var ageV = settings.getAsInt("age");

        var userV = Validated.accum(usernameV,ageV,User::new);

        //Prints out a Fail
        System.out.println(userV);

        var settings2 = settings.with("age",35).with("username", "Ola");
        var usernameV2 = settings2.getAsString("username");
        var ageV2 = settings2.getAsInt("age");

        var userV2 = Validated.accum(usernameV2,ageV2,User::new);


        //Prints out a Valid user
        System.out.println(userV2);
    }
}
