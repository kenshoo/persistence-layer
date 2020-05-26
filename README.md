## Persistence Layer (Java)

Is Hibernate too slow for you?

Do you need high performance for bulk operations?

Do you have a flow of business rules to run upon persitence?

If so, then PL may be the library you need.

PL is a Java mutation layer for business entites where you can define a flow to be executed upon every mutation.
The flow can contain enrichments and validations.
PL was designed to be fast for bulk operations and do every SQL operation in bulks, including fetching of all fields required by the flow.


## Compatibility
* Java 8 or greater
* MySQL Database
* Each PL release version specifies the JOOQ version it was built with. E.g. release "0.1.40-jooq-3.10.4" was built with JOOQ 3.10.4. If you need a very specific JOOQ version, we can add it to our build process.

## Code Samples

### Retrieve auto generated ID
After defining and wiring the required infra (as described in the Book below), you can execute PL commands as follows:
```java
var cmd = new CreateCampaignCommand();

cmd.set(Campaign.NAME         , "bla bla");
cmd.set(Campaign.DAILY_BUDGET , 150);
cmd.set(Campaign.STATUS       , Status.ToPause);

var results = campaignPersistence.update(asList(cmd));
var ids = seq(results).map(r -> r.getIdentifier().get(Campaign.ID)).toList();
```

### Update the list of cities of the campaign (one to many relation)

We want to completely replace the existing cities of our campaign:

```java
var campaignCmd = new UpdateCampaignCommand(someId);

campaignCmd.addChild(new UpsertCityCommand("London"));
campaignCmd.addChild(new UpsertCityCommand("Tel Aviv"));
campaignCmd.addChild(new UpsertCityCommand("Miami"));
campaignCmd.add(new DeletionOfOther(CityEntity.INSTANCE));

campaignPersistence.update(asList(campaignCmd));
```
* The campaign ID is automatically populated into the campaign_cities table.
* All other cities of this campaign are deleted from the DB.

### Define a Simple Validator

```java
class CampaignBudgetValidator implements FieldValidator<Campaign, Integer> {

    @Override
    public EntityField<Campaign, Integer> validatedField() {
        return Campaign.BUDGET;
    }

    @Override
    public ValidationError validate(Integer newBudgetValue) {
        return newBudgetValue > 5000
            ? new ValidationError("budget is too big", Campaign.BUDGET)
            : null;
        }
    }
}
```
See the book for more details:
* How to add validators to the flow.
* How to define more complex validators.

### Entity Definition and Annotations

```java
public class Campaign extends AbstractEntityType<Campaign> {

    public static final Campaign INSTANCE = new Campaign();

    private Campaign() { super("campaigns"); }

    @Override
    public DataTable getPrimaryTable() { return CampaignJooq.TABLE; }
    
    @Immutable
    public static final EntityField<Campaign, Integer> ID = INSTANCE.field(CampaignJooq.TABLE.id.identity(true));

    @Required
    public static final EntityField<Campaign, Integer> BUDGET = INSTANCE.field(CampaignJooq.TABLE.budget);
    
    @Required(RELATION)
    public static final EntityField<Campaign, Integer> ACCOUNT_ID = INSTANCE.field(CampaignJooq.TABLE.account_id);
    
    public static final EntityField<Campaign, Integer> BIDDING_STRATEGY = INSTANCE.field(CampaignBiddingStrategy.TABLE.strategy_type);
}
```

There are multiple features in this examples:
* ID field is auto incremented by the database (by JOOQ expression ```identity.true()```).
* ID field cannot be changed by an UPDATE command.
* BUDGET field must be provided upon creation.
* ACCOUNT_ID must be provided upon creation and the referenced Account entity must exist in DB.
* BIDDING_STRATEGY is a field from another JOOQ table (usages of the campaign entity should not care about it). This requires that the "secondary" table refer to the campaign table by a one-to-one relation.

## The Book

The book is work in progress so it only convers the basic features. Until it is finished, advanced features can be found in the tests (PersitenceLayerTest and PersistenceLayerOneToManyTest) in this repo.

* [Persistence Layer for Smarties](https://docs.google.com/document/d/e/2PACX-1vRLFhNPYwOhqYsm9cTL6UDGCwexuscKrVVuLhdZLrbaGsCB3QG5NY28zyh1uO8QzBhe3XItwc24iSCE/pub#h.g4yh5us7ub8z)

## Download

From maven
```xml
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

