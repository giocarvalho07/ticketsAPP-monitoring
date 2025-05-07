package com.tickets.controller;

import com.tickets.domain.Order;
import com.tickets.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    private final OrderService orderService;

    @Autowired
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping()
    public ResponseEntity<List<Order>> listarOrdersSimples() {
        logger.info("REQUEST RECEIVED: GET /orders");
        try {
            List<Order> orders = orderService.listarTodosOrders();
            List<Order> ordersSimples = orders.stream()
                    .map(order -> {
                        Order orderSimples = new Order();
                        orderSimples.setId(order.getIdOrder());
                        orderSimples.setItem(order.getItem());
                        orderSimples.setQuantity(order.getQuantity());
                        orderSimples.setValue(order.getValue());
                        orderSimples.setUser(null);
                        return orderSimples;
                    })
                    .collect(Collectors.toList());
            logger.info("RESPONSE SENT: GET /orders - Status: {}", HttpStatus.OK);
            return ResponseEntity.ok(ordersSimples);
        } catch (Exception e) {
            logger.error("REQUEST ERROR: GET /orders - Error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/detalhe/{id}")
    public ResponseEntity<?> buscarDetalheOrder(@PathVariable Long id) {
        logger.info("REQUEST RECEIVED: GET /orders/detalhe/{}", id);
        try {
            Optional<Order> orderOptional = orderService.buscarOrderComUsuario(id);
            if (orderOptional.isPresent()) {
                Map<String, Object> detalhe = new HashMap<>();
                detalhe.put("idOrder", orderOptional.get().getIdOrder());
                detalhe.put("item", orderOptional.get().getItem());
                detalhe.put("quantity", orderOptional.get().getQuantity());
                detalhe.put("value", orderOptional.get().getValue());
                detalhe.put("userId", orderOptional.get().getUser().getIdUser());
                detalhe.put("userName", orderOptional.get().getUser().getName());
                logger.info("RESPONSE SENT: GET /orders/detalhe/{} - Status: {}, Body: {}", id, HttpStatus.OK, detalhe);
                return ResponseEntity.ok(detalhe);
            } else {
                logger.warn("RESPONSE SENT: GET /orders/detalhe/{} - Status: {}", id, HttpStatus.NOT_FOUND);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("REQUEST ERROR: GET /orders/detalhe/{} - Error: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping
    public ResponseEntity<Order> criarOrder(@RequestBody Order order) {
        logger.info("REQUEST RECEIVED: POST /orders - Body: {}", order);
        try {
            Order novoOrder = orderService.salvarOrder(order);
            logger.info("RESPONSE SENT: POST /orders - Status: {}, Body: {}", HttpStatus.CREATED, novoOrder);
            return new ResponseEntity<>(novoOrder, HttpStatus.CREATED);
        } catch (Exception e) {
            logger.error("REQUEST ERROR: POST /orders - Error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Order> atualizarOrder(@PathVariable Long id, @RequestBody Order orderAtualizado) {
        logger.info("REQUEST RECEIVED: PUT /orders/{} - Body: {}", id, orderAtualizado);
        try {
            Order orderSalvo = orderService.atualizarOrder(id, orderAtualizado);
            if (orderSalvo != null) {
                logger.info("RESPONSE SENT: PUT /orders/{} - Status: {}, Body: {}", id, HttpStatus.OK, orderSalvo);
                return ResponseEntity.ok(orderSalvo);
            } else {
                logger.warn("RESPONSE SENT: PUT /orders/{} - Status: {}", id, HttpStatus.NOT_FOUND);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("REQUEST ERROR: PUT /orders/{} - Error: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarOrder(@PathVariable Long id) {
        logger.warn("REQUEST RECEIVED: DELETE /orders/{}", id);
        try {
            if (orderService.buscarOrderPorId(id).isPresent()) {
                orderService.deletarOrder(id);
                logger.info("RESPONSE SENT: DELETE /orders/{} - Status: {}", id, HttpStatus.NO_CONTENT);
                return ResponseEntity.noContent().build();
            } else {
                logger.warn("RESPONSE SENT: DELETE /orders/{} - Status: {}", id, HttpStatus.NOT_FOUND);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("REQUEST ERROR: DELETE /orders/{} - Error: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}