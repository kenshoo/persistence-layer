package com.kenshoo.pl.entity;

import com.google.common.collect.ImmutableList;
import org.jooq.lambda.Seq;
import static java.util.Collections.emptyList;


public class FeatureSet {

    private final ImmutableList<Feature> features;

    public final static FeatureSet EMPTY = new FeatureSet(emptyList());

    public FeatureSet(Iterable<Feature> features) {
        this.features = ImmutableList.copyOf(features);
    }

    public FeatureSet(Feature... features) {
        this(Seq.of(features));
    }

    public boolean isEnabled(Feature feature) {
        return features.contains(feature);
    }
}
