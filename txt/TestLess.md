# Testing: Less is More!

Vel, jeg innrømmer at overskriften er noe spekulativ, men les videre så forstår du...

(Koden som brukes i eksemplene finnes på https://github.com/kantega/correct-by-construction, jeg anbefaler at du tar en titt.)

De aller fleste som jobber med software kan være enige om at testing er lurt. Og jeg tror også at alle kan være enige om at det er nyttig å teste mest mulig med minst mulig kode.

Man kan si at testing er en av mange måter å få tilbakemelding om at programmet ditt gjør det du mener det skal gjøre, og ha den effekten du håper at den skal ha. Man har den enkleste formen for testing med enhetstesting. Den er lynrask og kan gi  tilbakemelding med en gang koden bygges.  Så har man integrasjonstesting, der sammensetning av systemer testes, men automatisk. Litt mer krevende er manuell systemtesting, som krever installasjon og manuelle rutiner. Kanskje har man pilotkunder som får tester nye deler av programmet ditt i et produksjonsmiljø før det slippes løs på et større marked?
Og til slutt har man har også produksjon, der sluttbrukerne hver dag tester systemet for deg. 

Det trente øyet ser at alle disse testvariantene ligger på et kontinuum, der de enkleste testene gir raskest tilbakemelding og er enklest å lage, mens "testene" i produksjon kan være adskillig mer krevende å reagere på. 

Men vi kan gjøre det enda bedre! For det finnes også noe som kan gjøres  _før_ man i det hele tatt _kommer_ til testing!

For den aller raskeste tilbakemeldingen får du av kompilatoren. Den tester om programmet ditt er gyldig! (Vel - syntaktisk gyldig da i hvertfall, og semantisk med tanke på programmeringsspråket, men det er ikke så viktig). Og det er her vi skal begynne: Vi skal se på hvordan vi kan lage programmene våre slik at man _ikke trenger_ teste så mye. Tenk deg det! Slippe å vedlikeholde tester, og _samtidig_ sove godt om natta!

Denne herlige ideen baserer seg på at man bruker subklasser for å beskrive de ulike tilstandene en entitet eller verdi kan ha. Man begrenser mulighetene til å opprette objekter ved å ha private konstruktorer og bruke statiske metoder som sjekker input før objektet returneres. Cluet er at man ikke vet hvilken subklasse man får tilbake, og at man med noen triks sørger for at man må sjekke for alle mulige tilstander - hvis ikke kompilerer ikke koden.

La oss lage oss et lite case som ligner litt på det vi ser fra virkeligheten, men samtidig er så enkelt at det ikke blir for mye arbeid. Vi kan f.eks. tenke oss at vi lager en applikasjon som bl.a. skal håndtere kontaktinformasjon. Vi starter med epost.

Hvis vi skal lage et skikkelig enterprisey system bruker vi sikkert jax-rs + jackson. Da kunne vi ha brukt annotasjoner for å være sikre på at data som kommer inn er korrekt:
```java
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
```
og bruke den slik:
```java
public class Example1 {

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
```
Vel og bra. Men det er litt styr at man overalt man oppretter en ContactInfo også må gjøre en validering. Når du tenker etter, så bør man gjøre en validering hver gang man _bruker_ et ContactInfo objekt. Man vet jo ikke om objektet er validert fra før! Vi er også avhengig av et rammeverk som gjør dette for oss. Og vi blir ikke varslet dersom vi glemmer å validerei dete hele tatt.

La oss se om vi kan gjøre det slik at vi får en _kompileringsfeil_ dersom vi prøver å bruke et uvalidert objekt.

[Forrige artikkel](https://github.com/kantega/correct-by-construction/blob/master/txt/Validated.md) handlet om Validering, la oss gjenbruke klassen vi lagde der: Validated. La oss lage en egen domeneklasse for epost, og modde litt på caset vårt. Vi modder litt på ContactInfo, og lager klassen EmailAdress.
Vi må være helt sikre på at EmailAdress ikke endrer seg etter at den er validert, det betyr at vi må gjøre den immutable. Det får vi til ved å sørge for at alt innhold i klassen er _final_, og at alt innholdet er immutable. String er immutable, så da kan vi lage klassen:
```java
public class EmailAddress {

    final String value;

    public EmailAddress(String value) {
        this.value = value;
    }
}
```

Nå må vi gjøre det helt sikkert at det ikke går an å opprette en EmailAdress som har ugyldig syntaks. Dette får vi til ved å gjøre konstruktoren private, og lage en factory metode som returnerer Validated<Email>. Dette gjør det _umulig_ å få tak i eller bruke et epostobjekt som ikke er validert og gyldig. Når man nå får tak i et Email objekt vet man at det er gyldig, og man trenger ikke sjekke dette flere steder i applikasjonen. Kompilatoren vil varsle deg dersom du prøver opprette en uten å validere først.

```
public class EmailAddress {

    final String value;

    private EmailAddress(String value) {
        this.value = value;
    }

    public static Validated<EmailAddress> of(String value) {
        return
            EmailValidator.getInstance().isValid(value) ?
                Validated.valid(new EmailAddress(value)) :
                Validated.fail("Feil format");
    }
}
```
Bonus med denne framgangmåten er at det er lett å bruke, siden man ikke trenger rammeverkstøtte.

For å gjøre det litt mer interessant lager vi et tilsvarende for telefonnummer også:
```java
public class Phonenumber {

    final NonEmptyList<Digit> digits;

    private Phonenumber(NonEmptyList<Digit> digits) {
        this.digits = digits;
    }

    public static Validated<Phonenumber> of(String numberAsString) {
        List<Digit> digitList =
        Option.somes(Stream.fromString(numberAsString).map(Digit::fromChar)).toList();
        return 
            digitList.isEmpty() ?
                Validated.fail("Feil format på input, det må være minst ett tall") :
                Validated.valid(new Phonenumber(NonEmptyList.nel(digitList.head(), digitList.tail())));
    }

}
```

Det kan virke som litt pes å lagre tallene i et telefonnummer som en ikke-tom liste med siffer. Men nå er vi sikre at det ikke er en random tekststreng, vi vet at listen ikke er tom, og vi vet at det er gyldige siffer på hver plass. Vi trenger ikke teste det! Dersom vi lager litt mer kompliserte regler for gyldige telefonnummer, så må vi lage en test for at disse stemmer for alle gyldige telefonnummer. Det høres ut som sirkellogikk, så det fikser vi en senere artikkel.

Vi setter nå dette inn i ContactInfo slik vi lærte forrige artikkel:
```java
...
    public static void main(String[] args) {

        Validated<ContactInfo> validatedInfo =
            Validated.accum(
                EmailAddress.of("ola.normann@test.com"),
                Phonenumber.of("12345678"),
                ContactInfo::new
            );
            
        ...
```


Ok, nå har vi gjort to konverteringer, uten at det egentlig går an å skrive en meningsfull test for det. Fordi det ikke _kan_ være feil, kompilatoren sjekker det for oss.

Men dette var enkelt og trivielt. La oss utvide caset vårt litt. 
Epostadressen må jo bekreftes av brukeren. Det øker sannsynligheten for at den stemmer (la oss anta at vi ikke vil eller kan bruke OpenId Connect for pålogging)

Vi endrer EmailAddress til et interface med to implementasjoner: Unconfirmed og Confirmed, og så definerer vi en fold() metode. Denne fungerer akkurat som en visitor, bare at den returnerer en verdi. Vi lar også fold være den _eneste_ måten å hente ut informasjon fra EmailAddress på.

Vi lager to muligheter for å opprette en bekreftet epostadresse på: En for standard forretningslogikk der vi markerer en ubekreftet adresse som bekreftet med hjelp av et "bevis" (i dette tilfellet en timestamp), og en metode vi kan bruke for f.eks. deserialisering. Den sistnevnte gir vi et navn som tydeligjør at den bruker man bare unntaksvis.

```java
public interface EmailAddress {

    //Dette er eneste måten å hente ut tilstanden fra EmailAdress på
    <T> T fold(
        Function<Unconfirmed, T> onUnconfirmed,
        Function<Confirmed, T> onConfirmed
    );

    //Brukes når vi skal opprett en epostadresse fra brukeren
    static Validated<EmailAddress> of(String value) {
        return
            EmailValidator.getInstance().isValid(value) ?
                Validated.valid(new Unconfirmed(value)) :
                Validated.fail("Feil format");
    }
    
    //Brukes kun til deserialisering der vi stoler på datagrunnlaget
    static Validated<EmailAddress> unsafeCreateConfirmed(Instant instant, String value) {
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

        //Brukes når man skal bekrefte en epostadresse. Her bruker vi et tidsstempel som "bevis" på at 
        //bekreftelsen har skjedd. Denne skal gjøre det vanskelig å opprette bekreftede epostadresser
        //når man er litt bevisstløs i gjerningsøyeblikket
        public Confirmed confirm(Instant timestamp){
            return new Confirmed(timestamp, value);
        }
       
        @Override
        public <T> T fold(Function<Unconfirmed, T> onUnconfirmed, Function<Confirmed, T> onConfirmed) {
            return onUnconfirmed.apply(this);
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
    }
}
```

For å hente ut data blir vi nå tvunget til å bruke `fold()` , og da _må_ vi håndtere begge de mulige tilstandene til EmailAddress. Skipper vi det får vi en kompileringsfeil. 


Så dersom vi f.eks. skal sende ut et ukessammendrag på mail til en bruker, så lager vi oss en sammendrags-klasse som inneholder en `EmailAdress.Confirmed`. På denne måten kan vi ikke opprette et sammendragsobjekt uten en bekreftet epostadresse. 

``` 
public class DigestMessage {

    public final EmailAddress.Confirmed confirmedMail;
    public final String subject;

    //Kun en bekreftet epostadresse aksepteres her
    public DigestMessage(EmailAddress.Confirmed confirmedMail, String subject) {
        this.confirmedMail = confirmedMail;
        this.subject = subject;
    }
}
```
For å opprette en `DigestMessage` må nå bruke `EmailAddress.fold()`:

```java
public static void main(String[] args) {

    final String digestSubject =
        "Dette er en oppsummering";

    //Vi skal sende ut en oppsummering til en bruker, men
    // 1) Brukeren må finnes
    // 2) Brukeren må ha bekreftet eposten sin
    final Optional<ContactInfo> maybeInfo =
        Database.infoForId("a"); //Prøv med b eller c også

    final Validated<DigestMessage> validatedDigest =
        maybeInfo
            .map(contactInfo -> contactInfo.email)
            .map(emailAddress -> emailAddress.fold( //her bruker vi fold 
                unconfirmed -> Validated.<DigestMessage>fail("Epostadressen er ikke bekreftet"), //kjøres dersom post er ubekreftet
                confirmed -> Validated.valid(new DigestMessage(confirmed, digestSubject)))) //kjøres dersom bekreftet
            .orElseGet(() -> Validated.fail("Brukeren finnes ikke i databasen"));

    System.out.println(validatedDigest);

}
```


Oi. Mange vil tenke at dette ser veldig ukjent og rotete ut. Du tenker kanskje at exceptions eller casting, eller sågar bare å sette et flagg i Emailaddress hadde vært mer lettvint. Og svaret er nok utvilsom ja. Men da åpner du opp for at man kan havne i en ugyldig tilstand! Da ville det være mulig  å sende ut mail til en ubekreftet adresse. Dette må du dermed skrive tester for å validere. Og siden de testene må sjekke tilstandsendringen over tid - nemlig at en mail først ikke kan sendes ut, og så sendes ut - blir det adskillig mer arbeid å veldikeholde testene, enn å bare legge på et ekstra lag med typesikkerhet.
Kanskje er det verdt å øve seg på å lese slik kode allikevel da? Jeg mener - _mindre testing_?

En bonus med å være så eksplisitt med de ulike tilstandene et objekt kan ha er at man blir tvunget til å tenke gjennom grensetilfellene fra starten: Hva om brukeren ikke finnes (den kan jo bli slettet i mellomtiden). Hva skal systemet gjøre dersom man forsøke sende mail til en ubekreftet adresse? Vær ærlig, dette er problemstillinger vi ofte skyver på til det smeller.






