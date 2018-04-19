# Validering av data - når Optional ikke strekker til.

Correct by construction er en artikkelserie for javautviklere som tar opp tips og triks for å kunne sove bedre om natten etter at man har releaset software. Dette er første artikkelen i serien.

(Kode for denne artikkelen finner du [her](https://github.com/kantega/correct-by-construction/blob/master/code/src/main/java/org/kantega/cbyc/Validated.java) )

Java 8 førte med seg `Optional` som første med seg en del kontroverser. Nå, en del år senere, har støvet lagt seg, og man har stort sett vendt seg til å unngå null for å håndere spesialtilfellet [0,1] i kardinalitet. Oså for validering viser Optional seg å være nyttig i enkelte tilfeller, fordi man kan bruker flatMap og map for å håndtere tilfellene der man enten ikke har dataene, eller de er ugyldige:
```
    public Optional<String> getAsString(String paramName){...}

    public Optional<String> getAsInt(String paramName){...}

    Optional<User> userO =
        getAsString("username")
            .flatMap(name-> 
                getAsInt("age").flatMap(age->
                    new User(name,age)));
```
Litt vel mye paranteser og innrykk, men vi er sånn rimelig sikre på at userO er korrekt.
Men når man ønsker å rapportere _hvorfor_ det ikke lar seg opprette et objekt er det vanlig å se litt keitete logikk rundt Optional. Dette løses ofte med en orgie av exceptions


```
String username = getAsString("username").orElseThrow(()->new IllegalStateException("username not defined"));
Integer age = getAsInt("age").orElseThrow(()->new IllegalStateException("age not defined);
```

Eller så behandler man Optional som en litt brysom null:
```
    Optional<String> maybeUsername = getAsString("username");

    if(username.isDefined()){
    String username = maybeUsername.get();
    } else {
        throw new IllegalStateException("username not defined");
    }
}
```

Men vi kan gjøre det bedre! 
Ville det ikke være bedre å kombinere Optional med feilrapportering - dog uten å måtte kaste exceptions hele tiden? Kunne man kanskje også samle alle feilene i en bolk dersom man skulle ønske dette? Og kunne man i tillegg unngå alle innrykkene som flatMap krever?
Svaret er selvfølgelig ja. 

La oss lage en klasse for dette. Vi kaller den Validated.

Først lager vi en halvformell definisjon av den nye typen vår, dette guider oss videre når vi skal implementere den:
_`Validated<A>` representer et objekt som __enten__ inneholder et objekt av typen `A` __eller__ inneholder en liste av feilmeldinger._

I Java er det greit å representere denne dualiteten med et grensesnitt `Validated<A>` som har to implementasjoner: `Valid<A>` og `Failed<A>`.

Ok, hvordan skal vi så lage en Validated? Vi trenger i hvertfall to statiske factory metoder, la oss kalle gi dem åpenbare  navn `Validated<T> valid(A a)` og `Validated<T> fail(String msg)`.  Videre kan det være greit å kunne konvertere en Optional til en Validated: `Validated<T> of(Optional<T> o, String onEmpty)`.

La oss se hvordan Validated kan erstatte Optional:
```
Validated<String> username = Validated.of(getAsString("username"),"username not defined"));
Validated<Integer> age = Validated.of(getAsInt("age"),"age not defined));
```
Vi har kvittet oss med exceptions, men username og age er nå pakket inn i en Validated. Med Optional brukte vi flatMap for å pakke ut først den ene og så den andre, men det er knot så la oss se om vi får til å løse dette smooth.
Vi kunne f.eks. laget en statisk metode som tar inn to Validated, og så - dersom begge er Valid - gjør innholdene tilgjengelige for oss samtidig.

```
/**
 *  Accumulates the values of two Validated values. 
 *  If both are Valid, the values are applied to the provided function, returning a Valid with the result of the application.
 *  If either Validated is Fail, the Fail is returned. 
 *  If both are Fail, their messages are append into a new Fail. 
 */
public static <A,B,C> Validated<C> accum(Validated<A> av, Validated<B> bv,BiFunction<A,B,C> combiner){...}
```

Vi utsetter implementasjonen av denne metoden til senere, la oss først se hvordan man bruker den:
```
Validated<String> username = Validated.of(getAsString("username"),"username not defined"));
Validated<Integer> age = Validated.of(getAsString("age"),"age not defined));

Validated<User> user = Validated.accum(username,age,User::new);
```
Ikke så verst!
`user` inneholder nå enten alle feilmeldingene, eller et User objekt.
Men man ser også et par åpenbar ulemper: Man trenger en ny statisk metode for hvert antall Validated objekt man ønsker å kombinere, og det finnes ikke noen standard `@FunctionalInterface` for
funksjoner som tar inn mer enn to argumenter i java.
Det første løser man ganske greit (sjekk koden i medfølgende eksempler), det andre løser man ved å importere et api som støtter dette, feks [functionaljava](http://functionaljava.org) eller [vavr.io](http://vavr.io)

Nå som vi har bestemt oss for hvordan vi løser hvordan vi slår sammen flere validateringer må vi finne ut hvordan vi løser problemet der en validering er avhengig av resultatet fra en annen validering. La oss utvide eksempelet over og anta at getAsString og getAsInt er definert på et Params objekt som lastes inn.

```
interface Param{
Validated<String> getAsString(String name);
Validated<Integer> getAsInt(String name);
}
Validated<Param> params = loadParams();
```

Hvordan skal vi nå få ut username og age?  La oss se på hvordan man bruker en Optional for inspirasjon. 
Dersom man skal manipulere et objekt i en Optional, uten å måtte sjekke om den er defined først, bruker vi map(). map() tar inn en funksjon som endrer innholdet . Denne funksjonen anvendes bare dersom Optional er defined. Så dersom vi kaller map på en Optional som er empty, skjer det ingenting. Dette virker furniftig å ha på Validated også.
Men validated "inneholder" data i både Valid og Failed tilstandene, så vi må bestemme oss for hvilken tilstand map skal gjelde for. Siden det vanligvis ikke er så spennende å endre feilmeldinger bestemmer vi oss for at map skal gjelde Valid og bli ignorert ved Failed.

la oss skrive javadoc og signatur for map:
```
/**
* If the Validated is Valid, then this method return a new Validated with the function applied to its contents. If the Validated is
* Failed, then it has no effect.
*/
<B> Validated<B> map(Function<A,B> function);

```
Så kan vi prøve på params:
```
Validated<Param> params = loadParams();
Validated<Validated<String>> username = params.map(p->p.getAsString("username"));
```

Hmmm. Typen ser riktig ut: Det er en validation av resulaltatet av en validation. Men det er upraktisk at det nøstet sånn.
For å løse opp i dette kan vi se på hvilke tilstander `Validated<Validated<String>>` kan ha.
 1. Den ytre Validated er Fail
 2. Den ytre Validated er Valid som inneholder en Fail
 3. Den ytre Validated er Valid som inneholder en Valid
 
vi har egentlig da bare to tilstander, enten to varianter av Fail, eller en variant av Valid. Dette kan vi utnytte ved å slå de to Fail situasjonene sammen.
La oss definere flatMap, som først mapper og så slår de sammen etterpå (impementasjonen tar vi senere):

```
public <B> Validated<B> flatMap(Function<A,Validated<B>> function);
```
Flatmap kalles ofte "bind" siden det i praksis "binder" to objekter av samme type sammen i rekkefølge, slik at den første blie evaluert først, og så den andre.
La oss sjekke hvordan flatMap brukes.

```
Validated<User> user = 
    params.flatMap(p -> Validated.accum(p.getAsString("username"), p.getAsInt("age"), User::new));
```
Sweet!

Uten noe særlig boilerplate kan vi nå
1) kvitte oss med exceptions
2) samle feilmeldinger
3) nøste valideringer
4) slå sammen valideringer dersom alle er gyldige, eller summere feilmeldingene for eventuelle feil



For å se selve implementasjonen er det greiest å bare se på koden til [implementasjonen](https://github.com/kantega/correct-by-construction/blob/master/code/src/main/java/org/kantega/cbyc/Validated.java)  og [eksempelet](https://github.com/kantega/correct-by-construction/blob/master/code/src/test/java/org/katenga/cbc/validated/ValidatedExample.java)
