package com.aad.ffsmart.report;

import com.aad.ffsmart.web.ResponseMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static com.aad.ffsmart.web.ResponseHandler.generateResponse;

/**
 * Report REST controller
 * <p>
 * Defines endpoints for /reports path inc. auth and response code/message
 *
 * @author Oliver Wortley
 */
@RestController
@RequestMapping("/reports")
public class ReportController {
    @Autowired
    private ReportService reportService;

    @GetMapping
    @PreAuthorize("hasRole('HEAD_CHEF')")
    public Mono<ResponseEntity<Object>> getAllReports() {
        return generateResponse(ResponseMessage.SUCCESS, HttpStatus.OK, reportService.getAllReports());
    }

    @PostMapping
    @PreAuthorize("hasRole('HEAD_CHEF')")
    public Mono<ResponseEntity<Object>> generateReport() {
        return reportService.generateReport().then(generateResponse(ResponseMessage.SUCCESS, HttpStatus.CREATED));
    }

    @GetMapping(value = "/{fileName}", produces = MediaType.APPLICATION_PDF_VALUE)
    @PreAuthorize("hasRole('HEAD_CHEF')")
    public Mono<ResponseEntity<Flux<DataBuffer>>> downloadReport(@PathVariable String fileName) {
        return Mono.fromCallable(() -> ResponseEntity.ok(
                reportService.downloadReport(fileName)
        ));
    }
}
