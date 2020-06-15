package com.kenshoo.pl.entity;

import com.google.common.collect.ImmutableSet;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;


@RunWith(MockitoJUnitRunner.class)
public class HierarchyTest {

    /* ----- The Setup -----

      +-+ Account
        |
        +----+ Campaign
        |    |
        |    +----+ AdGroup
        |         |
        |         +----+ Ad
        |
        +----+ Sitelink
             |
             +----+ SitelinkURL
     */

    @Mock EntityType<?> Account;
    @Mock EntityType<?> Campaign;
    @Mock EntityType<?> AdGroup;
    @Mock EntityType<?> Ad;
    @Mock EntityType<?> Sitelink;
    @Mock EntityType<?> SitelinkURL;

    private Hierarchy hierarchy;

    @Before
    public void buildHierarchy() {
        hierarchy = new Hierarchy(Account, ImmutableSet.of(
                Pair.of(Account, Campaign),
                Pair.of(Account, Sitelink),
                Pair.of(Campaign, AdGroup),
                Pair.of(AdGroup, Ad),
                Pair.of(Sitelink, SitelinkURL)
        ));
    }

    @Test
    public void get_the_right_parent() {
        assertThat(hierarchy.getParent(Campaign).get(), is(Account));
        assertThat(hierarchy.getParent(Sitelink).get(), is(Account));
        assertThat(hierarchy.getParent(AdGroup).get(), is(Campaign));
        assertThat(hierarchy.getParent(Ad).get(), is(AdGroup));
        assertThat(hierarchy.getParent(SitelinkURL).get(), is(Sitelink));
    }

    @Test
    public void root_parent_is_missing() {
        assertFalse(hierarchy.getParent(Account).isPresent());
    }

    @Test
    public void get_the_right_children() {
        assertThat(hierarchy.childrenTypes(Account), containsInAnyOrder(Sitelink, Campaign));
        assertThat(hierarchy.childrenTypes(Campaign), containsInAnyOrder(AdGroup));
        assertThat(hierarchy.childrenTypes(Sitelink), containsInAnyOrder(SitelinkURL));
    }

    @Test
    public void leaf_children_is_an_empty_collection() {
        assertThat(hierarchy.childrenTypes(Ad), is(empty()));
    }

}