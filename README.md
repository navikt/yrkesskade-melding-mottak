# yrkesskade-melding-mottak
Mottaksmodul for innsendte meldinger om yrkesskade, -sykdom og menerstatning

## Lokal kjøring
Applikasjonen kan startes ved å kjøre YrkesskadeMeldingMottakApplication.

Legg til VM argumentene `-DYRKESSKADE_MOTTAK_DB_USERNAME=<brukernavn>`og `-DYRKESSKADE_MOTTAK_DB_PASSWORD=<passord>`

Spring profilen `local` må aktiveres med VM argument `-Dspring.profiles.active=local` eller ved hjelp av Active profile feltet i IntelliJ.

Database og Kafka må kjøre før applikasjonen kan startes

### Database
Det forutsettes at det kjører en database lokalt. Vi bruker Postgres. 

Dette kan enten innstalleres lokalt på maskinen eller startes med docker

```sql
CREATE DATABASE yrkesskade_mottak
```


### Kafka
Det er også nødvendig å starte opp en lokal Kafka som kan gjøres ved å benyttes seg av [navkafka-docker-compose][1].

```bash
docker-compose build
```

Start docker tjenestene
```bash
docker-compose up
```

Topicen vi lytter på må opprettes ved å kjøre en PUT mot [kafka-adminrest](http://localhost:8840/api/v1/apidocs/index.html?url=swagger.json) ved hjelp av følgende kommando:

```bash
curl -X PUT "http://igroup:itest@localhost:8840/api/v1/oneshot" -H  "Accept: application/json" -H  "Content-Type: application/json" --data "./local-setup/kafka_oneshot.json"
```



