package com.aad.ffsmart.inventory;

import com.aad.ffsmart.auth.JWTUtil;
import com.aad.ffsmart.user.User;
import com.aad.ffsmart.user.UserRepository;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ParseContext;
import com.jayway.jsonpath.spi.json.JacksonJsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.AutoConfigureDataMongo;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@AutoConfigureDataMongo
class InventoryTests {
    private static ParseContext jsonPath;
    @Autowired
    private WebTestClient webTestClient;
    @Autowired
    private InventoryRepository inventoryRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private InventoryChangeRepository inventoryChangeRepository;
    @Autowired
    private JWTUtil jwtUtil;
    private final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    @BeforeAll
    static void setup() {
        Configuration jacksonConfig = Configuration.builder()
                .mappingProvider(new JacksonMappingProvider())
                .jsonProvider(new JacksonJsonProvider())
                .build();
        jsonPath = JsonPath.using(jacksonConfig);
    }

    @DisplayName("Add inventory integration test")
    @WithMockUser(roles = "HEAD_CHEF")
    @Test
    void givenContext_whenAddInventory_thenInventoryAddedToDatabase() throws ParseException {
        Integer quantityAdded = 10;
        String userId = "63d878b154297b3967ee9503";

        User user = userRepository.findById(userId).block();
        String token = "Bearer " + jwtUtil.generateToken(Objects.requireNonNull(user));

        webTestClient.post()
                .uri("/inventory/insert")
                .header("Authorization", token)
                .body(Mono.just(List.of(new InventoryItem(
                        "2",
                        "Lamb 500g",
                        "63d1b3dae8b8e7e8b68300af",
                        "Supplier 1",
                        quantityAdded,
                        dateFormat.parse("2023-02-10")
                ))), List.class)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .consumeWith(System.out::println)
                .returnResult();

        InventoryItem itemAdded = inventoryRepository
                .findAll("lamb", 0, 100, dateFormat.parse("2023-01-01"), dateFormat.parse("2023-02-20")).blockFirst();

        assert itemAdded != null;
        assertTrue(itemAdded.getQuantity() >= quantityAdded);

        InventoryChange change = inventoryChangeRepository.findAll()
                .sort(Comparator.comparing(InventoryChange::getDate).reversed())
                .blockFirst();
        assert change != null;
        assertEquals(change.getItems().get(0).getQuantity(), quantityAdded);
        assertEquals("2", change.getItems().get(0).getItemId());
    }

    @DisplayName("Remove inventory integration test")
    @WithMockUser(roles = "HEAD_CHEF")
    @Test
    void givenInventory_whenRemoveInventory_thenInventoryRemovedFromDatabase() throws ParseException {
        Integer quantityRemoved = 3;
        Integer initialQuantity = Objects.requireNonNull(inventoryRepository
                .findAll("lamb", 0, 100, dateFormat.parse("2023-01-01"), dateFormat.parse("2023-02-20")).blockFirst()).getQuantity();
        String userId = "63d878b154297b3967ee9503";

        User user = userRepository.findById(userId).block();
        String token = "Bearer " + jwtUtil.generateToken(Objects.requireNonNull(user));

        webTestClient.post()
                .uri("/inventory/remove")
                .header("Authorization", token)
                .body(Mono.just(List.of(new InventoryItem(
                        "2",
                        "Lamb 500g",
                        "63d1b3dae8b8e7e8b68300af",
                        "Supplier 1",
                        quantityRemoved,
                        dateFormat.parse("2023-02-10")
                ))), List.class)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .consumeWith(System.out::println)
                .returnResult();

        InventoryItem itemRemoved = inventoryRepository
                .findAll("lamb", 0, 100, dateFormat.parse("2023-01-01"), dateFormat.parse("2023-02-20")).blockFirst();

        assert itemRemoved != null;
        assertEquals((int) itemRemoved.getQuantity(), initialQuantity - quantityRemoved);

        InventoryChange change = inventoryChangeRepository.findAll()
                .sort(Comparator.comparing(InventoryChange::getDate).reversed())
                .blockFirst();
        assert change != null;
        assertEquals(change.getItems().get(0).getQuantity(), quantityRemoved);
        assertEquals("2", change.getItems().get(0).getItemId());
    }

    @DisplayName("Remove all quantity of item integration test")
    @WithMockUser(roles = "HEAD_CHEF")
    @Test
    void givenItemWithQuantity_whenRemoveAllQuantity_thenInventoryItemDeleted() throws ParseException {
        Integer quantityToRemove = 100;
        String userId = "63d878b154297b3967ee9503";

        User user = userRepository.findById(userId).block();
        String token = "Bearer " + jwtUtil.generateToken(Objects.requireNonNull(user));

        webTestClient.post()
                .uri("/inventory/remove")
                .header("Authorization", token)
                .body(Mono.just(List.of(new InventoryItem(
                        "6",
                        "Milk 2L",
                        "63d1b3dae8b8e7e8b68300af",
                        "Supplier 1",
                        quantityToRemove,
                        dateFormat.parse("2023-02-08")
                ))), List.class)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .consumeWith(System.out::println)
                .returnResult();

        InventoryItem itemRemoved = inventoryRepository
                .findAll("milk", 0, 100, dateFormat.parse("2023-02-08"), dateFormat.parse("2023-02-09")).blockFirst();

        assertNull(itemRemoved);
    }

    @DisplayName("Get all inventory with no params integration test")
    @WithMockUser(roles = "CHEF")
    @Test
    void givenNoParams_whenGetAllInventory_thenInventoryReturned() {
        InventoryItem firstItem = inventoryRepository.findAll().sort(Comparator.comparing(InventoryItem::getExpiryDate).reversed()).blockFirst();

        assert firstItem != null;
        webTestClient.get()
                .uri("/inventory")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data[0].id").isEqualTo(firstItem.getId())
                .jsonPath("$.data[0].itemId").isEqualTo(firstItem.getItemId())
                .jsonPath("$.data[0].quantity").isEqualTo(firstItem.getQuantity())
                .consumeWith(System.out::println);
    }

    @DisplayName("Get all inventory with all params integration test")
    @WithMockUser(roles = "CHEF")
    @Test
    void givenAllParams_whenGetAllInventory_thenInventoryReturned() throws ParseException {
        InventoryItem firstItem = inventoryRepository
                .findAll("banana", 1, 100, dateFormat.parse("2023-01-01"), dateFormat.parse("2023-06-01"))
                .sort(Comparator.comparing(InventoryItem::getExpiryDate).reversed()).blockFirst();

        assert firstItem != null;
        webTestClient.get()
                .uri(uriBuilder ->
                        uriBuilder
                                .path("/inventory")
                                .queryParam("itemName", "banana")
                                .queryParam("minQuantity", 1)
                                .queryParam("maxQuantity", 100)
                                .queryParam("expiryDateFrom", "2023-01-01")
                                .queryParam("expiryDateTo", "2023-06-01")
                                .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data[0].id").isEqualTo(firstItem.getId())
                .jsonPath("$.data[0].itemId").isEqualTo(firstItem.getItemId())
                .jsonPath("$.data[0].quantity").isEqualTo(firstItem.getQuantity())
                .consumeWith(System.out::println);
    }

    @DisplayName("Update inventory by id integration test")
    @WithMockUser(roles = "CHEF")
    @Test
    void givenInventory_whenUpdateInventoryById_thenInventoryUpdated() throws ParseException {
        String inventoryId = "63d2a74d838bd336e18f3ddf";
        Integer newQuantity = 75;

        webTestClient.put()
                .uri("/inventory/" + inventoryId)
                .body(Mono.just(new InventoryItem(
                        inventoryId, "0", "Bananas 100g", "63d1b3dae8b8e7e8b68300af",
                        "Supplier 1", newQuantity, dateFormat.parse("2023-06-01"))), User.class)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .consumeWith(System.out::println);

        Mono<InventoryItem> itemMono = inventoryRepository.findById(inventoryId);

        StepVerifier
                .create(itemMono)
                .consumeNextWith(item -> {
                    assertEquals(inventoryId, item.getId());
                    assertEquals(newQuantity, item.getQuantity());
                })
                .verifyComplete();
    }

    @DisplayName("Get inventory change history integration test")
    @WithMockUser(roles = "CHEF")
    @Test
    void givenInventoryChanges_whenGetInventoryChangeHistory_thenInventoryChangesReturned() {
        InventoryChange firstChange = inventoryChangeRepository.findAll()
                .sort(Comparator.comparing(InventoryChange::getDate).reversed()).blockFirst();

        assert firstChange != null;
        webTestClient.get()
                .uri("/inventory/change-history")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data[0].id").isEqualTo(firstChange.getId())
                .jsonPath("$.data[0].userId").isEqualTo(firstChange.getUserId())
                .jsonPath("$.data[0].items").isNotEmpty()
                .consumeWith(System.out::println);
    }

    @DisplayName("Get expired items integration test")
    @WithMockUser(roles = "HEAD_CHEF")
    @Test
    void givenExpiredItems_whenGetExpiredItems_thenItemsReturned() {
        InventoryItem[] expiredItems = inventoryRepository.findExpired().collectList().block().toArray(new InventoryItem[0]);

        String json = webTestClient.get()
                .uri("/inventory/expired-items")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .consumeWith(System.out::println)
                .returnResult()
                .getResponseBody();

        InventoryItem[] res = jsonPath.parse(json).read("$.data", InventoryItem[].class);

        assertArrayEquals(expiredItems, res);
        for (InventoryItem i : res) {
            assertTrue(i.getExpiryDate().before(new Date()));
        }
    }

    @DisplayName("Remove expired items integration test")
    @WithMockUser(roles = "HEAD_CHEF")
    @Test
    void givenExpiredItems_whenRemoveExpiredItems_thenItemsDeleted() {
        webTestClient.delete()
                .uri("/inventory/expired-items")
                .exchange()
                .expectStatus().isNoContent();

        List<InventoryItem> expiredItems = inventoryRepository.findExpired().collectList().block();

        assert expiredItems != null;
        assertTrue(expiredItems.isEmpty());
    }
}
