# Correct by Construction: Optional NG - Validering av data

Correct by construction er en artikkelserie for javautviklere som tar opp tips og triks for å kunne sove bedre om natten etter at man har releaset software. Dette er første artikkelen i serien.

Java 8 førte med seg `Optional` som første med seg en del kontroverser. Nå, en del år senere, har støvet lagt seg, og man har stort sett vendt seg til å unngå null for å håndere spesialtilfellet [0,1] i kartdinalitet. Oså for validering viser Optional seg å være nyttig i enkelte tilfeller, fordi man kan bruker flatMap og map for å håndtere tilfellene der man enten ikke har dataene, eller de er ugyldige:
```
    Map<String,Object> params = ...;

        public Optional<String> getAsString(String paramName){
            try{
                return Optional.of(String.of(paramName.get(paramName)));
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
Integer age = getAsString("age").orElseThrow(()->new IllegalStateException("age not defined);
```

Men vi kan gjøre det bedre! Ville det ikke være bedre å kombinere options normale bruk, men med feilrapportering - dog uten å måtte kaste exceptions hele tiden? Kunne man kanskje også samle alle feilene i en bolk dersom man skulle ønske dette?



