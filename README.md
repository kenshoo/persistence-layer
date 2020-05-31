## Persistence Layer (Java)

Is Hibernate too slow for you?

Do you need high performance for bulk operations?

Do you have a flow of business rules to run upon persitence?

If so, then PL may be the library you need.

PL is a Java mutation layer for business entities where you can define a flow to be executed upon every mutation.
The flow can contain enrichments and validations.
PL was designed to be fast for bulk operations and do every SQL operation in bulks, including fetching of all fields required by the flow.

## How fast is it

At Kenshoo, after migrating our heavy bulk operations to PL, we got a performance boost factor of at least 2, and even up to 50 in some cases.

## Why is it faster

Persisting in bulks is always faster than persisting one entity at a time within a loop.  
So first of all, working in bulks with Hibernate is kind of a hidden feature. It exists, but few developers are aware of it.  
The PL interface, on the other hand, only accepts collections of commands to encourage you to work effectively.  
But let's suppose you are a "performance driven" developer and you do know how to pre-fetch a bulk of entities using Hibernate and now you want to validate the changes and persist them.  
Each validator may require a different set of fields to fetch.  
**Hibernate could be either eager or lazy. Neither of them is good.** Being eager may fetch too much, but being lazy is much worse as it shall query the DB multiple times within the loop.  
PL precalculates the fields required by the flow components and fetches exactly what is needed in a single query.  
It also precalculates which validators are really required by your commands and there are more optimizations along the way. Oh, and it does not use reflection.

## Compatibility
* Java 8 or greater
* MySQL Database (5.6 or greater)
* PL depends on [JOOQ](https://www.jooq.org). Each PL release version specifies the JOOQ version it was built with. E.g. release "0.1.40-jooq-3.10.4" was built with JOOQ 3.10.4. If you need a very specific JOOQ version, we can add it to our build process.

## [Try it Online](https://repl.it/@GalKoren2/PersistenceLayer#Main.java)

The above link is a **repl.it** online project where you can edit and execute code samples by only using your browser.

## Code Samples

### Retrieve auto generated ID
After defining and wiring the required infra (as described in the Book below), you can execute PL commands as follows:
```java
var cmd = new CreateCampaignCommand();

cmd.set(Campaign.NAME, "bla bla");
cmd.set(Campaign.DAILY_BUDGET, 150);
cmd.set(Campaign.STATUS, Status.ToPause);

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
See the wiki for more details:
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

## [The Wiki](https://github.com/kenshoo/persistence-layer/wiki)

There you can find a full tutorial with theory and examples.

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

