package com.okhanzhin.summarization.service.service;

import com.okhanzhin.task.tracker.common.model.request.SummarizationRequest;

/**
 *
 * @author okhanzhin on 24.09.2026
 */
public interface SummaryGeneratorService {

    String generate(SummarizationRequest request);
}
