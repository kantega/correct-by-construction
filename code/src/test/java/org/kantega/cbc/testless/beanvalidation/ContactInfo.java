package org.kantega.cbc.testless.beanvalidation;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;

public class ContactInfo {

    @Email
    @NotNull
    private String email;

    public ContactInfo(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }
}
