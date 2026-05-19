# lab-reactor

Maven **aggregator** — `cache-problems` (kök base modül) ve tüm alt modülleri tek seferde derler.

```bash
mvn clean install -DskipTests
```

Modül listesine yeni level eklendiğinde `pom.xml` içindeki `<modules>` güncellenir.
