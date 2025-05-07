package com.tickets.controller;


import com.tickets.domain.User;
import com.tickets.service.OrderService;
import com.tickets.service.UserService;
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
@RequestMapping("/users")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;
    private final OrderService orderService;

    @Autowired
    public UserController(UserService userService, OrderService orderService) {
        this.userService = userService;
        this.orderService = orderService;
    }

    @GetMapping()
    public ResponseEntity<List<User>> listarUsersSimples() {
        logger.info("REQUEST RECEIVED: GET /users");
        try {
            List<User> users = userService.listarTodosUsers();
            List<User> usersSimples = users.stream()
                    .map(user -> {
                        User userSimples = new User();
                        userSimples.setIdUser(user.getIdUser());
                        userSimples.setName(user.getName());
                        userSimples.setEmail(user.getEmail());
                        userSimples.setOrders(null);
                        return userSimples;
                    })
                    .collect(Collectors.toList());
            logger.info("RESPONSE SENT: GET /users - Status: {}", HttpStatus.OK);
            return ResponseEntity.ok(usersSimples);
        } catch (Exception e) {
            logger.error("REQUEST ERROR: GET /users - Error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    @GetMapping("/email/{email}")
    public ResponseEntity<User> buscarUserPorEmail(@PathVariable String email) {
        logger.info("REQUEST RECEIVED: GET /users/email/{}", email);
        try {
            Optional<User> userOptional = userService.buscarUserPorEmail(email);
            if (userOptional.isPresent()) {
                User userSemOrders = new User();
                userSemOrders.setIdUser(userOptional.get().getIdUser());
                userSemOrders.setName(userOptional.get().getName());
                userSemOrders.setEmail(userOptional.get().getEmail());
                userSemOrders.setOrders(null);
                logger.info("RESPONSE SENT: GET /users/email/{} - Status: {}", email, HttpStatus.OK);
                return ResponseEntity.ok(userSemOrders);
            } else {
                logger.warn("RESPONSE SENT: GET /users/email/{} - Status: {}", email, HttpStatus.NOT_FOUND);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("REQUEST ERROR: GET /users/email/{} - Error: {}", email, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/detalhe-usuario/{id}")
    public ResponseEntity<?> buscarDetalheUsuarioComOrders(@PathVariable Long id) {
        logger.info("REQUEST RECEIVED: GET /users/detalhe-usuario/{}", id);
        try {
            Optional<User> userOptional = userService.buscarUserPorId(id);
            if (userOptional.isPresent()) {
                Map<String, Object> detalhe = new HashMap<>();
                detalhe.put("idUser", userOptional.get().getIdUser());
                detalhe.put("name", userOptional.get().getName());
                detalhe.put("orders", userOptional.get().getOrders().stream()
                        .map(order -> {
                            Map<String, Object> orderDetalhe = new HashMap<>();
                            orderDetalhe.put("idOrder", order.getIdOrder());
                            orderDetalhe.put("item", order.getItem());
                            orderDetalhe.put("quantity", order.getQuantity());
                            orderDetalhe.put("value", order.getValue());
                            return orderDetalhe;
                        }).collect(Collectors.toList()));
                logger.info("RESPONSE SENT: GET /users/detalhe-usuario/{} - Status: {}", id, HttpStatus.OK);
                return ResponseEntity.ok(detalhe);
            } else {
                logger.warn("RESPONSE SENT: GET /users/detalhe-usuario/{} - Status: {}", id, HttpStatus.NOT_FOUND);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("REQUEST ERROR: GET /users/detalhe-usuario/{} - Error: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    @PostMapping
    public ResponseEntity<User> criarUser(@RequestBody User user) {
        logger.info("REQUEST RECEIVED: POST /users - Body: {}", user);
        try {
            User novoUser = userService.salvarUser(user);
            logger.info("RESPONSE SENT: POST /users - Status: {}, Body: {}", HttpStatus.CREATED, novoUser);
            return new ResponseEntity<>(novoUser, HttpStatus.CREATED);
        } catch (Exception e) {
            logger.error("REQUEST ERROR: POST /users - Error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> atualizarUser(@PathVariable Long id, @RequestBody User userAtualizado) {
        logger.info("REQUEST RECEIVED: PUT /users/{} - Body: {}", id, userAtualizado);
        try {
            Optional<User> userExistente = userService.buscarUserPorId(id);
            if (userExistente.isPresent()) {
                userAtualizado.setIdUser(id);
                User userSalvo = userService.salvarUser(userAtualizado);
                logger.info("RESPONSE SENT: PUT /users/{} - Status: {}, Body: {}", id, HttpStatus.OK, userSalvo);
                return ResponseEntity.ok(userSalvo);
            } else {
                logger.warn("RESPONSE SENT: PUT /users/{} - Status: {}", id, HttpStatus.NOT_FOUND);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("REQUEST ERROR: PUT /users/{} - Error: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarUser(@PathVariable Long id) {
        logger.warn("REQUEST RECEIVED: DELETE /users/{}", id);
        try {
            if (userService.buscarUserPorId(id).isPresent()) {
                userService.deletarUser(id);
                logger.info("RESPONSE SENT: DELETE /users/{} - Status: {}", id, HttpStatus.NO_CONTENT);
                return ResponseEntity.noContent().build();
            } else {
                logger.warn("RESPONSE SENT: DELETE /users/{} - Status: {}", id, HttpStatus.NOT_FOUND);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("REQUEST ERROR: DELETE /users/{} - Error: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}