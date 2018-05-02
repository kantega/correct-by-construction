package org.katenga.cbc.validated;

public class User {

    public final String username;
    public final Age age;

    public User(String username, Age age) {
        this.username = username;
        this.age = age;
    }

    @Override
    public String toString() {
        return "User{" +
          "username='" + username + '\'' +
          ", age=" + age +
          '}';
    }
}
