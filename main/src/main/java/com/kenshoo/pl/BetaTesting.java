package com.kenshoo.pl;

import java.util.Set;
import static com.google.common.collect.Sets.newConcurrentHashSet;

/**
 * Enables the user (KS, or anyone else in the outer world) to be a Beta tester by
 * enabling new features at its own risk.
 * This enables us having gradual rollouts.
 */
public class BetaTesting {

    public enum Feature {
        // Add toggles in here
        FindSecondaryTablesOfParents
    }

    private static final Set<Feature> enabled = newConcurrentHashSet();

    public static boolean isEnabled(Feature feature) {
        return enabled.contains(feature);
    }

    public static void enable(Feature feature) {
        enabled.add(feature);
    }

    public static void disable(Feature feature) {
        enabled.remove(feature);
    }

}
