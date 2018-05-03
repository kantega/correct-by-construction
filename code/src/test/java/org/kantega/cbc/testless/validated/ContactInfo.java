package org.kantega.cbc.testless.validated;

public class ContactInfo {


    public final Email email;
    public final Phonenumber phonenumber;

    public ContactInfo(Email email, Phonenumber phonenumber) {
        this.email = email;
        this.phonenumber = phonenumber;
    }

    @Override
    public String toString() {
        return "ContactInfo{" +
          "email=" + email +
          ", phonenumber=" + phonenumber +
          '}';
    }
}
