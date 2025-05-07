package com.tickets.controller;

import com.tickets.domain.Order;
import com.tickets.domain.User;
import com.tickets.service.OrderService;
import com.tickets.service.UserService;
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
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final OrderService orderService;

    @Autowired
    public UserController(UserService userService, OrderService orderService) {
        this.userService = userService;
        this.orderService = orderService;
    }

    @GetMapping()
    public ResponseEntity<List<User>> listarUsersSimples() {
        List<User> users = userService.listarTodosUsers();
        // Para garantir que a lista de orders não seja serializada, podemos fazer uma cópia
        // dos usuários e setar a lista de orders para null.
        List<User> usersSimples = users.stream()
                .map(user -> {
                    User userSimples = new User();
                    userSimples.setIdUser(user.getIdUser());
                    userSimples.setName(user.getName());
                    userSimples.setEmail(user.getEmail());
                    userSimples.setOrders(null); // Desconsidera a lista de orders
                    return userSimples;
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(usersSimples);
    }


    @GetMapping("/email/{email}")
    public ResponseEntity<User> buscarUserPorEmail(@PathVariable String email) {
        Optional<User> userOptional = userService.buscarUserPorEmail(email);
        return userOptional.map(user -> {
            User userSemOrders = new User();
            userSemOrders.setIdUser(user.getIdUser());
            userSemOrders.setName(user.getName());
            userSemOrders.setEmail(user.getEmail());
            userSemOrders.setOrders(null); // Garante que a lista de orders seja nula
            return ResponseEntity.ok(userSemOrders);
        }).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/detalhe-usuario/{id}")
    public ResponseEntity<?> buscarDetalheUsuarioComOrders(@PathVariable Long id) {
        Optional<User> userOptional = userService.buscarUserPorId(id);
        return userOptional.map(user -> {
            Map<String, Object> detalhe = new HashMap<>();
            detalhe.put("idUser", user.getIdUser());
            detalhe.put("name", user.getName());
            detalhe.put("orders", user.getOrders().stream()
                    .map(order -> {
                        Map<String, Object> orderDetalhe = new HashMap<>();
                        orderDetalhe.put("idOrder", order.getIdOrder());
                        orderDetalhe.put("item", order.getItem());
                        orderDetalhe.put("quantity", order.getQuantity());
                        orderDetalhe.put("value", order.getValue());
                        return orderDetalhe;
                    }).collect(Collectors.toList())); // Correção aqui!
            return ResponseEntity.ok(detalhe);
        }).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<User> criarUser(@RequestBody User user) {
        User novoUser = userService.salvarUser(user);
        return new ResponseEntity<>(novoUser, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> atualizarUser(@PathVariable Long id, @RequestBody User userAtualizado) {
        Optional<User> userExistente = userService.buscarUserPorId(id);
        if (userExistente.isPresent()) {
            userAtualizado.setIdUser(id);
            User userSalvo = userService.salvarUser(userAtualizado);
            return ResponseEntity.ok(userSalvo);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarUser(@PathVariable Long id) {
        if (userService.buscarUserPorId(id).isPresent()) {
            userService.deletarUser(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}