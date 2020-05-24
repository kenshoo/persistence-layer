## Persistence Layer (Java)

Is Hibernate too slow for you?

Do you need high performance for bulk operations?

Do you have a flow of business rules to run upon persitence?

If so, then PL may be the library you need.

PL is a Java mutation layer for business entites where you can define a flow to be executed upon every mutation.
The flow can contain enrichments and validations.
PL was designed to be fast for bulk operations and do every SQL operation in bulks, including fetching of all fields required by the flow.

```
Only MySQL is currently supported.
```

## Usage

After defining and wirnig the required infra (as described in the Book below), you can execute PL commands as follows:
```java
var cmd = new CreateCampaignCmd(someId);

cmd.set(Campaign.NAME         , "bla bla");
cmd.set(Campaign.DAILY_BUDGET , 150);
cmd.set(Campaign.STATUS       , Status.ToPause);

var results = campaignPersistence.update(asList(cmd));
```

## The Book

The book is work in progress so it only convers the basic features. Until it is finished, advanced features can be found in the tests (PersitenceLayerTest and PersistenceLayerOneToManyTest) in this repo.

* [Persistence Layer for Smarties](https://docs.google.com/document/d/e/2PACX-1vRLFhNPYwOhqYsm9cTL6UDGCwexuscKrVVuLhdZLrbaGsCB3QG5NY28zyh1uO8QzBhe3XItwc24iSCE/pub#h.g4yh5us7ub8z)

## Download

From maven
```
<dependency>
    <groupId>com.kenshoo</groupId>
    <artifactId>persistence-layer</artifactId>
    <version>0.1.40-jooq-3.10.4</version>
</dependency>
```

## Licensing

PL is licensed under the Apache License, Version 2.0.

## Credits

PL was originally created at Kenshoo by Victor Bronstein.

