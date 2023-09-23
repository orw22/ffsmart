package com.aad.ffsmart.report;

import com.aad.ffsmart.inventory.InventoryItem;
import com.aad.ffsmart.inventory.InventoryService;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import io.netty.buffer.ByteBufAllocator;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.NettyDataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Report service implementation
 * <p>
 * For generating PDF health and safety reports containing the expired items currently in the fridge
 * Utilises OpenPDF library
 *
 * @author Oliver Wortley
 */
@Service
@Slf4j
public class ReportServiceImpl implements ReportService {
    public static final String REPORTS_BASE_DIR = "C:/ffsmart/reports/";
    public static final int BUFFER_SIZE = 8096;
    @Autowired
    private InventoryService inventoryService;
    @Autowired
    private JavaMailSender javaMailSender;

    private final DateFormat formatter = new SimpleDateFormat("yyyy_MM_dd");

    private final DataBufferFactory dataBufferFactory = new NettyDataBufferFactory(ByteBufAllocator.DEFAULT);

    private void writeTableHeader(PdfPTable table) {
        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(Color.LIGHT_GRAY);
        cell.setPadding(4);

        Font font = FontFactory.getFont(FontFactory.HELVETICA);
        font.setColor(Color.BLACK);
        font.setSize(10);

        cell.setPhrase(new Phrase("Item ID", font));
        table.addCell(cell);

        cell.setPhrase(new Phrase("Item name", font));
        table.addCell(cell);

        cell.setPhrase(new Phrase("Expiry date", font));
        table.addCell(cell);

        cell.setPhrase(new Phrase("Quantity", font));
        table.addCell(cell);
    }

    private void writeTableData(PdfPTable table, List<InventoryItem> expiredItems) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        expiredItems.forEach(item -> {
            table.addCell(item.getId());
            table.addCell(item.getItemName());
            table.addCell(dateFormat.format(item.getExpiryDate()));
            table.addCell(item.getQuantity().toString());
        });
    }

    public Flux<String> getAllReports() {
        return Mono.just(Stream.of(Objects.requireNonNull(new File(REPORTS_BASE_DIR).listFiles()))
                        .filter(file -> !file.isDirectory())
                        .map(File::getName)
                        .collect(Collectors.toSet()))
                .flatMapMany(Flux::fromIterable)
                .sort(Comparator.comparing(s -> {
                    if (((String) s).startsWith("ffsmart_report")) {
                        return Integer.valueOf(((String) s).substring(15).replace("_", "").replace(".pdf", ""));
                    } else {
                        return -1;
                    }
                }).reversed());
    }

    private void sendReport(String fileName) throws MessagingException {
        MimeMessage msg = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(msg, true);

        helper.setTo("n1124638@ntu.ac.uk");
        helper.setSubject("FFsmart Health and Safety Report");
        helper.setText("Hi,\n\nHere is the latest health and safety report for FFsmart, detailing the expired items currently in the fridge.\n\nBest,\n\nThe FFsmart API");

        helper.addAttachment(fileName, new FileSystemResource(new File(REPORTS_BASE_DIR + fileName)));

        javaMailSender.send(msg);
    }

    public Mono<Void> generateReport() {
        Document document = new Document(PageSize.A4);
        String fileName = "ffsmart_report_" + formatter.format(new Date()) + ".pdf";

        try {
            PdfWriter.getInstance(document, new FileOutputStream(REPORTS_BASE_DIR + fileName, false));
        } catch (FileNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Generate report failed");
        }

        document.open();
        Font font = FontFactory.getFont(FontFactory.HELVETICA_BOLD);
        font.setSize(12);
        font.setColor(Color.BLACK);

        Paragraph p = new Paragraph("Expired items in FFsmart as of " + new SimpleDateFormat("yyyy-MM-dd").format(new Date()), font);
        p.setAlignment(Element.ALIGN_CENTER);

        document.add(p);

        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100f);
        table.setWidths(new float[]{1.5f, 3.5f, 2.5f, 1.5f});
        table.setSpacingBefore(8);

        writeTableHeader(table);

        return inventoryService.getExpiredItems().collectList().flatMap(expiredItems -> {
            writeTableData(table, expiredItems);

            document.add(table);
            document.close();
            try {
                sendReport(fileName);
            } catch (MessagingException e) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Send report failed");
            }

            return Mono.empty();
        });
    }

    public Flux<DataBuffer> downloadReport(String fileName) {
        return DataBufferUtils.read(
                new File(REPORTS_BASE_DIR + fileName).toPath(),
                dataBufferFactory,
                BUFFER_SIZE
        );
    }
}
