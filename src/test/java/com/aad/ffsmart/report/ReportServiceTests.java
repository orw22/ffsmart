package com.aad.ffsmart.report;

import com.aad.ffsmart.inventory.InventoryItem;
import com.aad.ffsmart.inventory.InventoryService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.buffer.DataBuffer;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportServiceTests {
    private static final String FILENAME = "ffsmart_report_2023_02_06.pdf";
    private static final String DOWNLOAD_FILENAME = "ffsmart_report_2023_01_29.pdf";
    @Mock
    private InventoryService inventoryService;
    @InjectMocks
    private ReportServiceImpl reportService;

    @DisplayName("Get all reports, expect list of reports returned")
    @Test
    void givenReports_whenGetAllReports_thenReportsReturned() {
        Flux<String> reportFlux = reportService.getAllReports();

        StepVerifier
                .create(reportFlux)
                .consumeNextWith(reportName -> assertEquals(FILENAME, reportName))
                .thenCancel()
                .verify();
    }

    @DisplayName("Generate report, expect report generated and sent")
    @Test
    void givenUser_whenGenerateReport_thenReportGenerated() {
        when(inventoryService.getExpiredItems()).thenReturn(Flux.just(
                new InventoryItem("0", "Bananas 100g", "63d1b3dae8b8e7e8b68300af", "Supplier 1", 10, new Date())));

        reportService.generateReport();
        verify(inventoryService, times(1)).getExpiredItems();
    }

    @DisplayName("Download report, expect successful")
    @Test
    void givenReports_whenDownloadReport_thenSuccess() {
        Flux<DataBuffer> reportFlux = reportService.downloadReport(DOWNLOAD_FILENAME);

        StepVerifier
                .create(reportFlux)
                .consumeNextWith(Assertions::assertNotNull)
                .thenCancel()
                .verify();
    }

}
