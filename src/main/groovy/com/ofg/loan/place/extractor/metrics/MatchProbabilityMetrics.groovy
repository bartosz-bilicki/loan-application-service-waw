package com.ofg.loan.place.extractor.metrics

import com.codahale.metrics.Meter
import com.codahale.metrics.MetricRegistry
import com.ofg.loan.place.extractor.PlaceExtractor.PlaceResolutionProbability

class MatchProbabilityMetrics {

    private final Map<PlaceResolutionProbability, Meter> probabilityMeters = [:]

    MatchProbabilityMetrics(MetricRegistry metricRegistry) {
        registerProbabilityMetrics(metricRegistry)
    }

    void update(PlaceResolutionProbability probability) {
        probabilityMeters[probability].mark()
    }

    private void registerProbabilityMetrics(MetricRegistry metricRegistry) {
        PlaceResolutionProbability.values().each { probability ->
            probabilityMeters[probability] = metricRegistry.meter("loan.places.analyzed.probability.$probability")
        }
    }
}
