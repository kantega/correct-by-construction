# Testing: Less is More!

Vel, jeg innrømmer at overskriften er noe spekulativ, men les videre så forstår du...

De aller fleste som jobber med software kan være enige om at testing er lurt. Og jeg tror også at alle kan være enige om at det er nyttig å teste mest mulig med minst mulig kode.

Man kan si at testing er en av mange måter å få tilbakemelding om at programmet ditt gjør det du mener det skal gjøre, og ha den effekten du håper at den skal ha. Man har den enkleste formen for testing med enhetstesting. Den er lynrask og kan gi  tilbakemelding med en gang koden bygges.  Så har man integrasjonstesting, der sammensetning av systemer testes, men automatisk. Litt mer krevende er manuell systemtesting, som krever installasjon og manuelle rutiner. Kanskje har man pilotkunder som får tester nye deler av programmet ditt i et produksjonsmiljl før det slippes løs på et større marked?
Og til slutt har man har også produksjon, der sluttbrukerne hver dag tester systemet for deg. 

Det trente øyet ser at alle disse testvariantene ligger på et kontinuum, der de enkleste testene gir raskest tilbakemelding og er enklest å lage, mens "testene" i produksjon kan være adskillig mer krevende å reagere på. 


Pris: `Enhetstest <-- billig -------- dyr --> Produskjon`
Roundtrip: ` <-- rask --------- treg --> `

Det er logisk at mest mulig av testingen bør foregå til venstre. Jo mer vi gjør av den til venstre, jo mindre trenger vi å gjøre til høyre. Det er billigere og raskere og enklere. Men det finnes også noe som er _enda lenger til venstre_ på aksten _før_ man i det hele tatt _kommer_ til testing!

For den aller raskeste tilbakemeldingen får du av kompilatoren. Den tester om programmet ditt er gyldig! (Vel - syntaktisk gyldig da i hvertfall, og semantisk med tanke på programmeringsspråket, men det er ikke så viktig). Og det er her vi skal begynne: Vi skal se på hvordan vi kan lage programmene våre slik at man _ikke trenger_ teste så mye. Tenk deg det! Slippe å vedlikeholde tester, og _samtidig_ sove godt om natta!

La oss lage oss et lite case som ligner litt på det vi ser fra virkeligheten, men samtidig er så enkelt at det ikke blir for mye arbeid. Vi kan f.eks. tenke oss at vi lager en applikasjon som bl.a. skal håndtere kontaktinformasjon. Vi starter med epost.

Hvis vi skal lage et skikkelig enterprisey system bruker vi sikkert jax-rs + jackson. Da kunne vi ha brukt annotasjoner for å være sikre på at data som kommer inn er korrekt:
```
class ContactInfo{

    ...

}
```
og bruke den slik:
```
{...}
```
Vel og bra. Men - vi må lage tester _for alle situasjoner_ der data puttes inn i en ContactInfo objekt. Vi er også avhengig av et rammeverk som gjør dette for oss. Og vi blir ikke varslet dersom vi glemmer å validere. 
La oss se om vi kan gjøre det slik at vi får en _kompileringsfeil_ dersom vi prøver å bruke et uvalidert objekt.

Forrige artikkel handlet om Validering, la oss gjenbruke klassen vi lagde der: Validated. La oss lage en egen domeneklasse for epost, og modde litt op Contact info.
Vi må være helt sikre på at Email ikke endrer seg etter at den er validert, det betyr at vi må gjøre den immutable. Det får vi til ved å sørge for at alt innhold i klassen er _final_, og at alt innholdet er immutable. String er immutable, så da kan vi lage klassen:
```
{...}
```

Nå må vi gjøre det helt sikkert at det ikke går an å opprette en Email som har ugyldig syntaks. Dette får vi til ved å gjøre konstruktoren private, og lage en factory metode som returnerer Validated<Email>. Dette gjør det _umulig_ å få tak i eller bruke et epostobjekt som ikke er validert og gyldig. Når man nå får tak i et Email objekt vet man at det er gyldig, og man trenger ikke sjekke dette flere steder i applikasjonen. Kompilatoren vil varsle deg dersom du prøver opprette en uten å validere først.

Bonus med denne framgangmåten er at det er lett å bruke, siden man ikke trenger rammeverkstøtte.



