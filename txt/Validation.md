# Correct by Construction: Optional NG - Validering av data

Correct by construction er en artikkelserie for javautviklere som tar opp tips og triks for å kunne sove bedre om natten etter at man har releaset software. Dette er første artikkelen i serien.

Java 8 førte med seg `Optional` som første med seg en del kontroverser. Nå, en del år senere, har støvet lagt seg, og man har stort sett vendt seg til å unngå null for å håndere spesialtilfellet [0,1] i kardinalitet. Oså for validering viser Optional seg å være nyttig i enkelte tilfeller, fordi man kan bruker flatMap og map for å håndtere tilfellene der man enten ikke har dataene, eller de er ugyldige:
```
    Map<String,Object> params = ...;

        public Optional<String> getAsString(String paramName){
            try{
                return Optional.of(String.of(params.get(paramName)));
            }catch(Exception e){
                //Dersom null eller ikke string, returner empty.
                return Optional.empty();
            }
        }
        
        public Optional<String> getAsInt(String paramName){...}
        
        ...
        
        
        Optional<User> userO =
            getAsString("username")
            .flatMap(name-> 
                getAsInt("age").flatMap(age->
                    new User(name,age)));

```
Litt vel mye paranteser og innrykk, men vi er sånn rimelig sikre på at userO er korrekt.
Men når man ønsker å rapportere _hvorfor det ikke_ lar seg opprette et objekt er det vanlig å se litt keitete logikk rundt Optional, men man kan komme seg unna det med en orgie av exceptions


```
String username = getAsString("username").orElseThrow(()->new IllegalStateException("username not defined"));
Integer age = getAsInt("age").orElseThrow(()->new IllegalStateException("age not defined);
```

Men vi kan gjøre det bedre! Ville det ikke være bedre å kombinere options normale bruk, men med feilrapportering - dog uten å måtte kaste exceptions hele tiden? Kunne man kanskje også samle alle feilene i en bolk dersom man skulle ønske dette? Og kunne man i tillegg unngå alle innrykkene som flatMap krever?
Svaret er selvfølgelig ja. La oss lage en klasse for dette. Vi kaller den Validated.
Først må vi definere betydningen av den nye typen vår: 
_`Validated<A>` representer et objekt som __enten__ inneholder et objekt av typen `A` __eller__ inneholder en liste av feilmeldinger._

I Java er det greit å representere disse to tilstandene som subklasser. La oss kalle dem Valid og Failed.
Akkurat som at man ikke vet hvor mange elementer en liste inneholder (utifra typen), eller at man ikke vet om en Optional er defined eller ikke, ved vi heller ikke om 
Validated inneholder feilmeldinger, eller objektet vi er ute etter. (uten å sjekke den runtime)

Men hvordan bruker vi det da? La oss se på hvordan man bruker en Optional for inspirasjon. 
Dersom man skal manipulere et objekt i en optional, uten å måtte sjekke om den er defined først, bruker vi map(). Map tar inn en funksjon som endrer innholdet. Denne funksjonen anvendes bare dersom det er noe å anvende den på. Så dersom vi kaller map på en Optional som er empty, skjer det ingenting. Man trenger å gjøre en test først. Så map er noe vi kan ha på Validated også.
Men validated "inneholder" data i både Valid og Failed tilstandene, så vi må bestemme oss for hvilken tilstand map skal gjelde for. Siden det vanligvis ikke er så spennende å endre feilmeldinger bestemmer vi oss for at map skal gjelde Success, og bli ignorert ved Failed.

la oss skrive javadoc og signatur for map
```
/**
* If the Validated is Valid, then this method return a new Validated with the function applied to its contents. If the Validated is
Failed, then it has no effect.
*/
<B> Validated<B> map(Function<A,B> function);

```

Ok, hvordan skal vi så lage en Validated? Vi trenger i hvertfall to statiske factory metoder, la oss kalle dem det åpenbare `Validated<T> success(A a)` og `Validated<T> fail(String msg)`.  Videre kan det være greit å kunne konvertere en Optional til en Validated: `Validated<T> of(Optional<T> o, String onEmpty)`.

Nå kan vi endre koden vår litt:
```
Validated<String> username = Validated.of(getAsString("username"),"username not defined"));
Validated<Integer> age = Validated.of(getAsInt("age"),"age not defined));
```
Vi har kvittet oss med exceptions, men username og age er nå pakket inn i en Validated. Å bruke både username og age krever at vi på et eller annet vis "kommer oss inn" i begge Validated objektene samtidig, og kombinerer innholdene for å kunne lage en user. La oss se om vi får til å løse dette smooth.

Vi kunne f.eks. laget en statisk metode som tar inn to Validated, og så - dersom begge er Valid - gjør innholdene tilgjengelige for oss samtidig.

```
If both the provided Validated objects are Valid, the contents are applied to the function, and the result is put in a new Valid.
Else the messages are accumulated and returned in a new Failed.
public static <A,B,C> Validated<C> accum(Validated<A> av, Validated<B> bv,FiFunction<A,B,C> combiner){...}
```
Vi utsetter implementasjonen av denne metoden til senere, la oss først se hvordan man bruker den:
```
Validated<String> username = Validated.of(getAsString("username"),"username not defined"));
Validated<Integer> age = Validated.of(getAsString("age"),"age not defined));

Validated<User> user = Validated.accum(username,age,User::new);
```
Ikke så verst.
Ulempene åpenbarer seg når man vil binde flere sammen; For det første man må lage en statisk metode for hvert antall Validated man vil kunne binde sammen, for det andre har ikke java et standard grensesnitt for funksjoner som tar inn mer enn to argumenter.
Det sistnevnte kan man løse ved å enten bruke grensesnitt fra bibliotek som støtter dette, feks. vavr.io eller functionaljava.com, eller så nøster man funksjoner: Function<A,Function<B,Function<C,D>>>. Det siste ser helt grusomt ut og støtter heller ikke metodereferanser, så jeg anbefaler å bruke et bibliotek som støtter det. 

Nå som vi har bestemt oss for hvordan vi løser hvordan vi binder sammen flere validateringer må vi finne ut hvordan vi løser problemet der en validering er avhengig av resultatet fra en annen validering. La oss utvide eksempelet over og anta at getAsString og getAsInt er definert på et Params objekt som lastes inn.

```
interface Param{
Validated<String> getAsString(String name);
Validated<Integer> getAsInt(String name);
}
Validated<Param> params = loadParams();
```

Hvordan skal vi nå få ut username og age?
Vi kan prøve med map
```
Validated<Param> params = loadParams();
Validated<Validated<String>> username = params.map(p->p.getAsString("username"));
```

Hmmm. Typen ser riktig ut: Det er en validation av resulaltaten av en validation. Men det er upraktisk.
Kanskje vi kan utnytte at den indre validation bare er bare definert dersom den ytre er Valid? Det betyr at maks en av dem er Failed og da kan vi slå den sammen. La oss definere flatMap, som først mapper og så slår de sammen etterpå:

```
public <B> Validated<B> flatMap(Function<A,Validated<B>> function);
```
Flatmap kalles ofte "bind" siden det i praksis "binder" to objekter av samme type sammen i rekkefølge, slik at den første blie evaluert først, og så den andre.
La oss sjekke koden;

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

Nå må vi bare implementere metodekroppene, la oss begynne med den som kan virke mest urfordrende: accum().

La oss se på spesifikasjonen: Dersom begge Validation er Valid skal verdiene inngå som argumenter til funksjonen som sendes inn. Dersom en av Validation er Fail skal denne returneres. Dersom begge er Fail skal filmeldingene legges sammen og legges i en ny Fail. Vi trenger altså en måte å hente ut innholdet i Validation på.




En måte å gjøre dette på er gjennom en _fold_. Dette fungerer på samme måte som en visitor:
```
public <T> T fold(Function<List<String>,T> onFail,Function<A,T> onValid);
```
Vi lager en funksjon som tar inn to funksjoner. onFail blir brukt dersom Validation objektet er en Fail, og onValid dersom det er en Valid. Vi kan nå bruke denne i accum slik:
```
public static <A,B,C> Validated<C> accum(Validated<A> av, Validated<B> bv,FiFunction<A,B,C> combiner){
    return av.fold(
        aFailMsgs -> vb.fold(
            bFailMsgs -> Validation.fail(aFailMsgs.append(bFailMsgs)), //a og b er Fail, legg sammen feilmeldiner og lag ny Fail
            __ -> Validation.fail(aFailMsgs)), //a er Fail men b er Valid, returner a sin feilmelding bare
        aValidValue -> vb.fold(
            bFailMsgs -> Validation.fail(bFailMsgs), // a er valid med b er Fail, returner b sin feilmelding 
            bValidValue -> Validation.valid(combiner.apply(aValidValue,bValidValue))) //begger er Valid. Apply funksjon og legg i en Valid
    );
}
```

Hmm, det var ganske knotete, __men__ vi kan være sikre på at dette stemmer, fordi det tross alt kompilerer. Men det blir uhåndterlig dersom man 
