package org.kantega.cbc.testless._3confirm;

public class DigestMessage {

    public final EmailAddress.Confirmed confirmedMail;
    public final String subject;

    public DigestMessage(EmailAddress.Confirmed confirmedMail, String subject) {
        this.confirmedMail = confirmedMail;
        this.subject = subject;
    }

    @Override
    public String toString() {
        return "DigestMessage{" +
          "confirmedMail=" + confirmedMail +
          ", subject='" + subject + '\'' +
          '}';
    }
}
