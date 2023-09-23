package com.aad.ffsmart.report;

import com.aad.ffsmart.exception.GlobalErrorAttributes;
import com.aad.ffsmart.exception.GlobalExceptionHandler;
import com.aad.ffsmart.web.ResponseMessage;
import com.aad.ffsmart.web.WebFluxTestSecurityConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.mockito.Mockito.*;

@WebFluxTest(ReportController.class)
@Import(WebFluxTestSecurityConfig.class)
class ReportControllerTests {
    @Autowired
    WebTestClient webTestClient;

    @MockBean
    private ReportService reportService;

    @MockBean
    private GlobalErrorAttributes globalErrorAttributes;

    @MockBean
    private GlobalExceptionHandler globalExceptionHandler;

    @DisplayName("Get all reports, expect status Ok")
    @WithMockUser(roles = "HEAD_CHEF")
    @Test
    void givenReports_whenGetAllReports_thenStatusOk() {
        when(reportService.getAllReports()).thenReturn(Flux.just());

        webTestClient.get()
                .uri("/reports")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .consumeWith(System.out::println);
        verify(reportService, times(1)).getAllReports();
    }

    @DisplayName("Get all reports with role delivery driver, expect status Forbidden")
    @WithMockUser(roles = "DELIVERY_DRIVER")
    @Test
    void givenReportsAndRoleDeliveryDriver_whenGetAllReports_thenStatusForbidden() {
        when(reportService.getAllReports()).thenReturn(Flux.just());

        webTestClient.get()
                .uri("/reports")
                .exchange()
                .expectStatus().isForbidden();
        verify(reportService, never()).getAllReports();
    }

    @DisplayName("Generate report, expect status Created")
    @WithMockUser(roles = "HEAD_CHEF")
    @Test
    void givenRoleHeadChef_whenGenerateReport_thenStatusCreated() {
        when(reportService.generateReport()).thenReturn(Mono.empty());

        webTestClient.mutateWith(SecurityMockServerConfigurers.csrf())
                .post()
                .uri("/reports")
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.message").isEqualTo(ResponseMessage.SUCCESS)
                .consumeWith(System.out::println);

        verify(reportService, times(1)).generateReport();
    }

    @DisplayName("Download report, expect status Ok and Content-Type PDF")
    @WithMockUser(roles = "HEAD_CHEF")
    @Test
    void givenRoleHeadChef_whenDownloadReport_thenStatusOkAndContentTypePDF() {
        when(reportService.downloadReport(anyString())).thenReturn(Flux.just());

        webTestClient.get()
                .uri("/reports/a1.pdf")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_PDF);
        verify(reportService, times(1)).downloadReport(anyString());
    }
}
