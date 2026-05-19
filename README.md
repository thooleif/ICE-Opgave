# Funktionelle krav

Systemet er en personlig fitness app, der hjælper brugeren med at sætte et vægtmål, få en kost og træningsplan, og holde styr på sin udvikling fra uge til uge. Tanken er at brugeren skal kunne logge ind, fortælle systemet lidt om sig selv, og derefter få en plan der passer til vedkommendes krop og målsætning. Hvis udviklingen ikke går som forventet, skal systemet selv kunne reagere på det.

## Domæne

De centrale entiteter i systemet er brugeren, brugerens profil med fysiske data, et fitnessmål, en træningspræference, en makroplan, samt en historik af ugentlige vejninger. En bruger ejer præcis én profil, ét mål, én træningspræference og én makroplan ad gangen, mens vejningerne samles op over tid og udgør grundlaget for at vurdere fremgang. Makroplanen er afhængig af de tre andre, fordi den beregnes ud fra fysiske data, valgt mål og hvor meget brugeren træner.

## Bruger og profil

Brugeren skal kunne oprette en konto og logge ind igen senere. Når brugeren er logget ind, skal vedkommende kunne udfylde en profil med alder, køn, højde, vægt, erfaringsniveau og eventuelle skader. Profilen skal kunne opdateres løbende, så systemet altid arbejder med aktuelle tal.

## Mål og træningspræferencer

Brugeren skal kunne sætte et fitnessmål bestående af en målvægt og en deadline, og angive om det handler om vægttab, muskelopbygning, vedligehold eller recomp. Derudover skal brugeren kunne angive sine træningspræferencer, herunder fokusområde, antal træningsdage om ugen og varighed pr. session. Systemet skal kunne foreslå en passende ugestruktur ud fra disse oplysninger.

## Makroplan og automatisk justering

Systemet skal kunne beregne en daglig makroplan med kalorier, protein, kulhydrater og fedt ud fra brugerens profil, mål og træningspræferencer. Brugeren skal kunne se hvordan planen er udregnet, så det er gennemskueligt hvor tallene kommer fra. Hvis ugentlige vejninger viser at brugeren ikke bevæger sig i den ønskede retning, skal systemet selv justere makroplanen, så kalorierne enten skrues op eller ned afhængigt af målet. Brugeren skal informeres når en justering finder sted, og hvorfor.

## Madforslag

Systemet skal kunne foreslå konkrete måltider tilpasset brugerens mål. Hvis brugeren er i kalorieunderskud, skal forslagene være mættende og kaloriefattige med højt proteinindhold. Hvis brugeren er i overskud og har brug for at få mere mad i sig, skal forslagene være kalorietætte og nemme at få ned. Forslagene skal passe til de makroer brugeren har tilbage på dagen, så de hjælper med at ramme målet i stedet for blot at være tilfældige opskrifter.

## Vægttracking og fremgang

Brugeren skal kunne registrere en ugentlig vejning, som gemmes sammen med datoen i en personlig historik. Ud fra historikken skal systemet kunne vise den samlede vægtændring og den gennemsnitlige ugentlige udvikling, så brugeren kan se om vedkommende er på rette vej. Det er også disse tal der danner grundlaget for at systemet kan justere makroplanen automatisk.

## Brugerflade

Systemet skal have en grafisk brugerflade, så brugeren kan navigere mellem login, profil, mål, makroplan, vejninger, fremgang og madforslag uden at skulle bruge en terminal. Brugerfladen skal være ren og overskuelig, så det er tydeligt hvor brugeren befinder sig, og hvad næste skridt er.
