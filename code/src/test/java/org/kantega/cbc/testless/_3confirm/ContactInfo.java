package org.kantega.cbc.testless._3confirm;

public class ContactInfo {


    public final EmailAddress email;
    public final Phonenumber phonenumber;

    public ContactInfo(EmailAddress email, Phonenumber phonenumber) {
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
