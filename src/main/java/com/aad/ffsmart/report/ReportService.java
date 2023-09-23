package com.aad.ffsmart.report;

import org.springframework.core.io.buffer.DataBuffer;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Report service interface
 * <p>
 * Contains method signatures for reports logic
 * Implemented in ReportServiceImpl
 *
 * @author Oliver Wortley
 */
public interface ReportService {

    Flux<String> getAllReports();

    Mono<Void> generateReport();

    Flux<DataBuffer> downloadReport(String fileName);
}
