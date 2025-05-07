package com.tickets.controller;

import com.tickets.domain.Order;
import com.tickets.service.OrderService;
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

    private final OrderService orderService;

    @Autowired
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping()
    public ResponseEntity<List<Order>> listarOrdersSimples() {
        List<Order> orders = orderService.listarTodosOrders();
        List<Order> ordersSimples = orders.stream()
                .map(order -> {
                    Order orderSimples = new Order();
                    orderSimples.setId(order.getIdOrder()); // Usando o getIdOrder corretamente agora
                    orderSimples.setItem(order.getItem());
                    orderSimples.setQuantity(order.getQuantity());
                    orderSimples.setValue(order.getValue());
                    orderSimples.setUser(null); // Desconsidera o usuário associado
                    return orderSimples;
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(ordersSimples);
    }

    @GetMapping("/detalhe/{id}")
    public ResponseEntity<?> buscarDetalheOrder(@PathVariable Long id) {
        Optional<Order> orderOptional = orderService.buscarOrderComUsuario(id);
        return orderOptional.map(order -> {
            Map<String, Object> detalhe = new HashMap<>();
            detalhe.put("idOrder", order.getIdOrder());
            detalhe.put("item", order.getItem());
            detalhe.put("quantity", order.getQuantity());
            detalhe.put("value", order.getValue());
            detalhe.put("userId", order.getUser().getIdUser());
            detalhe.put("userName", order.getUser().getName());
            return ResponseEntity.ok(detalhe);
        }).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Order> criarOrder(@RequestBody Order order) {
        Order novoOrder = orderService.salvarOrder(order);
        return new ResponseEntity<>(novoOrder, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Order> atualizarOrder(@PathVariable Long id, @RequestBody Order orderAtualizado) {
        Order orderSalvo = orderService.atualizarOrder(id, orderAtualizado);
        if (orderSalvo != null) {
            return ResponseEntity.ok(orderSalvo);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarOrder(@PathVariable Long id) {
        if (orderService.buscarOrderPorId(id).isPresent()) {
            orderService.deletarOrder(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}