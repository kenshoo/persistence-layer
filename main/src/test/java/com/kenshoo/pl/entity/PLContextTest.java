package com.kenshoo.pl.entity;

import org.junit.Test;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


public class PLContextTest {

    @Test
    public void generated_feature_set_does_not_change_when_feature_predicate_changes() {
        MutableFeaturePredicate predicate = new MutableFeaturePredicate();
        PLContext plContext = newContext().withFeaturePredicate(predicate).build();
        FeatureSet generatedFeatureSet = plContext.generateFeatureSet();
        predicate.add(Feature.ForTest);
        assertTrue(predicate.test(Feature.ForTest));
        assertFalse(generatedFeatureSet.isEnabled(Feature.ForTest));
    }

    @Test
    public void generated_feature_set_includes_feature_when_predicate_is_true() {
        PLContext plContext = newContext().withFeaturePredicate(__ -> true).build();
        FeatureSet generatedFeatureSet = plContext.generateFeatureSet();
        assertTrue(generatedFeatureSet.isEnabled(Feature.ForTest));
    }

    private PLContext.Builder newContext() {
        return new PLContext.Builder(null);
    }

    class MutableFeaturePredicate implements Predicate<Feature> {

        List<Feature> features = new ArrayList<>(1);

        @Override
        public boolean test(Feature feature) {
            return features.contains(feature);
        }

        public void add(Feature feature) {
            features.add(feature);
        }
    }
}
