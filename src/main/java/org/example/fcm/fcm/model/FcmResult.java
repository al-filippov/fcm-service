package org.example.fcm.fcm.model;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class FcmResult<T> {
    private final List<T> clusterCentres;
    private final Map<T, List<Double>> result;

    public FcmResult(final List<T> clusterCentres,
                     final Map<T, List<Double>> result) {
        this.clusterCentres = clusterCentres;
        this.result = result;
    }

    public List<T> getClusterCentres() {
        return Collections.unmodifiableList(clusterCentres);
    }

    public Set<T> getDataPoints() {
        return Collections.unmodifiableSet(result.keySet());
    }

    public List<Double> getDataPointDegrees(final T dataPoint) {
        return Collections.unmodifiableList(
                Optional.ofNullable(result.get(dataPoint)).orElse(Collections.emptyList()));
    }

    public Map<T, List<Double>> getResult() {
        return result;
    }
}

