package com.aad.ffsmart.report;

import com.aad.ffsmart.web.ResponseMessage;
import com.jayway.jsonpath.JsonPath;
import io.netty.buffer.ByteBufAllocator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.NettyDataBufferFactory;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.aad.ffsmart.report.ReportServiceImpl.REPORTS_BASE_DIR;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class ReportTests {
    private static final String FILENAME = "ffsmart_report_2023_02_01.pdf";
    @Autowired
    private WebTestClient webTestClient;
    @Autowired
    private ReportService reportService;

    private final DataBufferFactory dataBufferFactory = new NettyDataBufferFactory(ByteBufAllocator.DEFAULT);

    @DisplayName("Get all reports integration test")
    @WithMockUser(roles = "HEAD_CHEF")
    @Test
    void givenReports_whenGetAllReports_thenResponseContainsFilename() {
        String[] res = JsonPath.parse(webTestClient.get()
                        .uri("/reports")
                        .exchange()
                        .expectStatus().isOk()
                        .expectBody(String.class)
                        .consumeWith(System.out::println)
                        .returnResult()
                        .getResponseBody())
                .read("$.data", String[].class);

        assertTrue(Arrays.stream(res).toList().contains(FILENAME));
    }

    @DisplayName("Generate report integration test")
    @WithMockUser(roles = "HEAD_CHEF")
    @Test
    void givenContext_whenGenerateReport_thenReportGenerated() {
        webTestClient.mutateWith(SecurityMockServerConfigurers.csrf())
                .post()
                .uri("/reports")
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.message").isEqualTo(ResponseMessage.SUCCESS)
                .consumeWith(System.out::println);

        Set<String> files = Stream.of(Objects.requireNonNull(new File(REPORTS_BASE_DIR).listFiles()))
                .map(File::getName)
                .collect(Collectors.toSet());

        assertTrue(files.contains("ffsmart_report_"
                + new SimpleDateFormat("yyyy_MM_dd").format(new Date()) + ".pdf"));
    }

}
