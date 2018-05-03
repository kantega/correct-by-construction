package org.kantega.cbc.testless.stage1;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;

public class RunBeanValidation {

    public static void main(String[] args) {
        ValidatorFactory factory =
          Validation.buildDefaultValidatorFactory();

        Validator validator =
          factory.getValidator();

        ContactInfo contactInfo =
          new ContactInfo("ola.normann@test.org");

        Set<ConstraintViolation<ContactInfo>> constraintViolations =
          validator.validate(contactInfo);

        //Skriver ut et tomt sett
        System.out.println(constraintViolations);


        ContactInfo feilContactInfo =
          new ContactInfo("ola.normann_test");

        Set<ConstraintViolation<ContactInfo>> feilConstraintViolations =
          validator.validate(feilContactInfo);


        //Skriver ut et sett med feil
        System.out.println(feilConstraintViolations);



    }
}
