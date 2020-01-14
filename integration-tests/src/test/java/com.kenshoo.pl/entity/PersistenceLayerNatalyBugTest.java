package com.kenshoo.pl.entity;

import com.google.common.collect.ImmutableSet;
import com.kenshoo.jooq.AbstractDataTable;
import com.kenshoo.jooq.DataTable;
import com.kenshoo.jooq.DataTableUtils;
import com.kenshoo.jooq.TestJooqConfig;
import com.kenshoo.pl.entity.annotation.Id;
import com.kenshoo.pl.entity.annotation.Required;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.SQLDataType;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import java.util.Set;

import static com.kenshoo.pl.entity.annotation.RequiredFieldType.RELATION;
import static com.kenshoo.pl.entity.spi.FieldValueSupplier.fromValues;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class PersistenceLayerNatalyBugTest {

    private static boolean tablesCreated = false;
    private static DSLContext staticDSLContext;

    private DSLContext dslContext = TestJooqConfig.create();
    private PLContext plContext;
    private PersistenceLayer<KeywordEntityType, KeywordEntityType.Key> kwPersistenceLayer;

    private static final int AGENCY_ID = 1;
    private static final int ACCOUNT_ID = 2;
    private static final int CAMPAIGN_ID = 3;
    private static final int PROFILE_ID = 4;
    private static final int KEYWORD_ID = 5;

    private static final String AGENCY_NAME = "agencyName";
    private static final String ACCOUNT_NAME = "accountName";

    private static final Set<DataTable> ALL_TABLES = ImmutableSet.of(
        AgencyTable.INSTANCE,
        AccountTable.INSTANCE,
        CampaignTable.INSTANCE,
        ProfileTable.INSTANCE,
        KeywordTable.INSTANCE);

    @Before
    public void populateTables() {

        kwPersistenceLayer = new PersistenceLayer<>(dslContext);

        staticDSLContext = dslContext;
        plContext = new PLContext.Builder(dslContext).build();

        if (!tablesCreated) {
            ALL_TABLES.forEach(table -> DataTableUtils.createTable(dslContext, table));
        }

        dslContext.insertInto(AgencyTable.INSTANCE)
                  .set(AgencyTable.INSTANCE.id, AGENCY_ID)
                  .set(AgencyTable.INSTANCE.name, AGENCY_NAME)
                  .execute();

        dslContext.insertInto(AccountTable.INSTANCE)
                  .set(AccountTable.INSTANCE.id, ACCOUNT_ID)
                  .set(AccountTable.INSTANCE.name, ACCOUNT_NAME)
                  .set(AccountTable.INSTANCE.agency_id, AGENCY_ID)
                  .set(AccountTable.INSTANCE.profile_id, PROFILE_ID)
                  .execute();

        dslContext.insertInto(CampaignTable.INSTANCE)
                  .set(CampaignTable.INSTANCE.id, CAMPAIGN_ID)
                  .set(CampaignTable.INSTANCE.account_id, ACCOUNT_ID)
                  .execute();

        dslContext.insertInto(ProfileTable.INSTANCE)
                  .set(ProfileTable.INSTANCE.id, PROFILE_ID)
                  .execute();

        tablesCreated = true;
    }

    @After
    public void clearTables() {
        ALL_TABLES.forEach(table -> dslContext.truncate(table).execute());
    }

    @AfterClass
    public static void dropTables() {
        ALL_TABLES.forEach(table -> staticDSLContext.dropTableIfExists(table).execute());
    }

    @Test
    public void testNatalyBug() {
        final CreateEntityCommand<KeywordEntityType> cmd = new CreateEntityCommand<>(KeywordEntityType.INSTANCE);
        cmd.set(KeywordEntityType.ID, KEYWORD_ID);
        cmd.set(KeywordEntityType.CAMPAIGN_ID, CAMPAIGN_ID);
        cmd.set(KeywordEntityType.PROFILE_ID, PROFILE_ID);
        cmd.set(KeywordEntityType.NAME, fromValues(AgencyEntityType.NAME, AccountEntityType.NAME, (x, y) -> x + " " + y));

        final CreateResult<KeywordEntityType, KeywordEntityType.Key> res =
            kwPersistenceLayer.create(singletonList(cmd), kwFlowConfig(), KeywordEntityType.Key.DEFINITION);

        assertThat("Create failed due to: " + res.getErrors(cmd),
                   res.hasErrors(), is(false));

        final Record rec = dslContext.selectFrom(KeywordTable.INSTANCE).fetchOne();
        if (rec != null) {
            assertThat("Incorrect keyword name: ",
                       rec.get(KeywordTable.INSTANCE.name), equalTo(AGENCY_NAME + " " + ACCOUNT_NAME));
        } else {
            fail("Could not find the created keyword");
        }
    }

    private ChangeFlowConfig<KeywordEntityType> kwFlowConfig() {
        return ChangeFlowConfigBuilderFactory.newInstance(plContext, KeywordEntityType.INSTANCE)
                                             .with(new FeatureSet(Feature.FindSecondaryTablesOfParents))
                                             .build();
    }

    public static class AgencyTable extends AbstractDataTable<AgencyTable> {

        public static final AgencyTable INSTANCE = new AgencyTable("agency");

        final TableField<Record, Integer> id = createPKField("id", SQLDataType.INTEGER);
        final TableField<Record, String> name = createField("name", SQLDataType.VARCHAR(20));

        public AgencyTable(String name) {
            super(name);
        }

        public AgencyTable(AgencyTable aliased, String alias) {
            super(aliased, alias);
        }

        @Override
        public AgencyTable as(String alias) {
            return new AgencyTable(this, alias);
        }
    }

    public static class AccountTable extends AbstractDataTable<AccountTable> {

        public static final AccountTable INSTANCE = new AccountTable("account");

        final TableField<Record, Integer> id = createPKField("id", SQLDataType.INTEGER);
        final TableField<Record, String> name = createPKField("name", SQLDataType.VARCHAR(20));
        final TableField<Record, Integer> agency_id = createFKField("agency_id", AgencyTable.INSTANCE.id);
        final TableField<Record, Integer> profile_id = createFKField("profile_id", ProfileTable.INSTANCE.id);

        public AccountTable(String name) {
            super(name);
        }

        public AccountTable(AccountTable aliased, String alias) {
            super(aliased, alias);
        }

        @Override
        public AccountTable as(String alias) {
            return new AccountTable(this, alias);
        }
    }

    public static class ProfileTable extends AbstractDataTable<ProfileTable> {

        public static final ProfileTable INSTANCE = new ProfileTable("profile");

        final TableField<Record, Integer> id = createPKField("id", SQLDataType.INTEGER);

        public ProfileTable(String name) {
            super(name);
        }

        public ProfileTable(ProfileTable aliased, String alias) {
            super(aliased, alias);
        }

        @Override
        public ProfileTable as(String alias) {
            return new ProfileTable(this, alias);
        }
    }

    public static class CampaignTable extends AbstractDataTable<CampaignTable> {

        public static final CampaignTable INSTANCE = new CampaignTable("Campaign");

        final TableField<Record, Integer> id = createPKField("id", SQLDataType.INTEGER);
        final TableField<Record, Integer> account_id = createFKField("account_id", AccountTable.INSTANCE.id);

        public CampaignTable(String name) {
            super(name);
        }

        public CampaignTable(CampaignTable aliased, String alias) {
            super(aliased, alias);
        }

        @Override
        public CampaignTable as(String alias) {
            return new CampaignTable(this, alias);
        }
    }

    public static class KeywordTable extends AbstractDataTable<KeywordTable> {

        public static final KeywordTable INSTANCE = new KeywordTable("keyword");

        final TableField<Record, Integer> id = createPKField("id", SQLDataType.INTEGER);
        final TableField<Record, String> name = createField("name", SQLDataType.VARCHAR(100));
        final TableField<Record, Integer> campaign_id = createFKField("campaign_id", CampaignTable.INSTANCE.id);
        final TableField<Record, Integer> profile_id = createFKField("profile_id", ProfileTable.INSTANCE.id);

        public KeywordTable(String name) {
            super(name);
        }

        public KeywordTable(KeywordTable aliased, String alias) {
            super(aliased, alias);
        }

        @Override
        public KeywordTable as(String alias) {
            return new KeywordTable(this, alias);
        }
    }

    public static class AgencyEntityType extends AbstractEntityType<AgencyEntityType> {
        public static final AgencyEntityType INSTANCE = new AgencyEntityType();

        @Id
        public static final EntityField<AgencyEntityType, Integer> ID = INSTANCE.field(AgencyTable.INSTANCE.id);
        public static final EntityField<AgencyEntityType, String> NAME = INSTANCE.field(AgencyTable.INSTANCE.name);

        public AgencyEntityType() {
            super("Agency");
        }

        public static class Key extends SingleUniqueKeyValue<AgencyEntityType, Integer> {
            public static final SingleUniqueKey<AgencyEntityType, Integer> DEFINITION = new SingleUniqueKey<AgencyEntityType, Integer>(AgencyEntityType.ID) {
                @Override
                protected AgencyEntityType.Key createValue(Integer value) {
                    return new AgencyEntityType.Key(value);
                }
            };

            public Key(int id) {
                super(DEFINITION, id);
            }
        }

        @Override
        public DataTable getPrimaryTable() {
            return AgencyTable.INSTANCE;
        }
    }

    public static class AccountEntityType extends AbstractEntityType<AccountEntityType> {
        public static final AccountEntityType INSTANCE = new AccountEntityType();

        @Id
        public static final EntityField<AccountEntityType, Integer> ID = INSTANCE.field(AccountTable.INSTANCE.id);
        public static final EntityField<AccountEntityType, String> NAME = INSTANCE.field(AccountTable.INSTANCE.name);
        @Required(RELATION)
        public static final EntityField<AccountEntityType, Integer> AGENCY_ID = INSTANCE.field(AccountTable.INSTANCE.agency_id);
        @Required(RELATION)
        public static final EntityField<AccountEntityType, Integer> PROFILE_ID = INSTANCE.field(AccountTable.INSTANCE.profile_id);

        public AccountEntityType() {
            super("Account");
        }

        public static class Key extends SingleUniqueKeyValue<AccountEntityType, Integer> {
            public static final SingleUniqueKey<AccountEntityType, Integer> DEFINITION = new SingleUniqueKey<AccountEntityType, Integer>(AccountEntityType.ID) {
                @Override
                protected AccountEntityType.Key createValue(Integer value) {
                    return new AccountEntityType.Key(value);
                }
            };

            public Key(int id) {
                super(DEFINITION, id);
            }
        }

        @Override
        public DataTable getPrimaryTable() {
            return AccountTable.INSTANCE;
        }
    }

    public static class KeywordEntityType extends AbstractEntityType<KeywordEntityType> {
        public static final KeywordEntityType INSTANCE = new KeywordEntityType();

        @Id
        public static final EntityField<KeywordEntityType, Integer> ID = INSTANCE.field(KeywordTable.INSTANCE.id);
        public static final EntityField<KeywordEntityType, String> NAME = INSTANCE.field(KeywordTable.INSTANCE.name);
        @Required(RELATION)
        public static final EntityField<KeywordEntityType, Integer> CAMPAIGN_ID = INSTANCE.field(KeywordTable.INSTANCE.campaign_id);
        @Required(RELATION)
        public static final EntityField<KeywordEntityType, Integer> PROFILE_ID = INSTANCE.field(KeywordTable.INSTANCE.profile_id);

        public KeywordEntityType() {
            super("keyword");
        }

        public static class Key extends SingleUniqueKeyValue<KeywordEntityType, Integer> {
            public static final SingleUniqueKey<KeywordEntityType, Integer> DEFINITION = new SingleUniqueKey<KeywordEntityType, Integer>(KeywordEntityType.ID) {
                @Override
                protected KeywordEntityType.Key createValue(Integer value) {
                    return new KeywordEntityType.Key(value);
                }
            };

            public Key(int id) {
                super(DEFINITION, id);
            }
        }

        @Override
        public DataTable getPrimaryTable() {
            return KeywordTable.INSTANCE;
        }
    }
}
