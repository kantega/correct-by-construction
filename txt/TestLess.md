# Testing: Less is More!

Vel, jeg innrømmer at overskriften er noe spekulativ, men les videre så forstår du...

De aller fleste som jobber med software kan være enige om at testing er lurt. Og jeg tror også at alle kan være enige om at det er nyttig å teste mest mulig med minst mulig kode.

Man kan si at testing er en av mange måter å få tilbakemelding om at programmet ditt gjør det du mener det skal gjøre, og ha den effekten du håper at den skal ha. Man har den enkleste formen for testing med enhetstesting. Den er lynrask og kan gi  tilbakemelding med en gang koden bygges.  Så har man integrasjonstesting, der sammensetning av systemer testes, men automatisk. Litt mer krevende er manuell systemtesting, som krever installasjon og manuelle rutiner. Kanskje har man pilotkunder som får tester nye deler av programmet ditt i et produksjonsmiljl før det slippes løs på et større marked?
Og til slutt har man har også produksjon, der sluttbrukerne hver dag tester systemet for deg. 

Det trente øyet ser at alle disse testvariantene ligger på et kontinuum, der de enkleste testene gir raskest tilbakemelding og er enklest å lage, mens "testene" i produksjon kan være adskillig mer krevende å reagere på. 


Pris: `Enhetstest <-- billig -------- dyr --> Produskjon`
Roundtrip: ` <-- rask --------- treg --> `

Det er logisk at mest mulig av testingen bør foregå til venstre. Jo mer vi gjør av den til venstre, jo mindre trenger vi å gjøre til høyre. Det er billigere og raskere og enklere. Men det finnes også noe som er _enda lenger til venstre_ på aksten _før_ man i det hele tatt _kommer_ til testing!

For den aller raskeste tilbakemeldingen får du av kompilatoren. Den tester om programmet ditt er gyldig! (Vel - syntaktisk gyldig da i hvertfall). Og det er her vi skal begynne: Vi skal se på hvordan vi kan lage programmene våre slik at man _ikke trenger_ teste så mye. Tenk deg det! Slippe å vedlikeholde tester, og _samtidig_ sove godt om natta!


