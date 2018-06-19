package org.katenga.cbc.validated;

import org.kantega.cbyc.Validated;

public class RunValidatedExample {

    public static void main(String[] args) {
        var settings = Settings.empty().with("age", 235);

        var username = settings.getAsString("username");
        var age = settings.getAsInt("age").flatMap(Age::toAge);

        var user = Validated.accum(username, age, User::new);

        //Prints out a Fail with two messages
        System.out.println(user);

        var settings2 = settings.with("age", 35).with("username", "Ola");
        var username2 = settings2.getAsString("username");
        var age2 = settings2.getAsInt("age").flatMap(Age::toAge);

        var user2 = Validated.accum(username2, age2, User::new);


        //Prints out a Valid user
        System.out.println(user2);

        //100%-, sure thing-, guaranteed-, cannot invalid-,
        //valid user.
        var validUser =
          user2.orElse(new User("unknown",Age.zero));
    }
}
