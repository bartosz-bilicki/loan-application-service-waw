package com.ofg.loan.place.extractor

import com.ofg.loan.place.model.Tweet

interface PropagationWorker {
    void collectAndPropagate(long pairId, List<Tweet> tweets)
}