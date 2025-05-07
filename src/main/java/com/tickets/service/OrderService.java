package com.tickets.service;

import com.tickets.domain.Order;
import com.tickets.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;

    @Autowired
    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public List<Order> listarTodosOrders() {
        logger.info("Listando todos os pedidos.");
        return orderRepository.findAll();
    }

    public Optional<Order> buscarOrderPorId(Long id) {
        logger.info("Buscando pedido com ID: {}", id);
        return orderRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Order> buscarOrderComUsuario(Long id) {
        return orderRepository.findById(id);
        // Por padrão, o relacionamento ManyToOne é carregado "eager" (ansiosamente)
        // então ao buscar a Order, o User associado também será carregado.
    }

    public Order salvarOrder(Order order) {
        Order orderSalvo = orderRepository.save(order);
        logger.info("Pedido criado com ID: {}", orderSalvo.getIdOrder());
        return orderSalvo;
    }

    public void deletarOrder(Long id) {
        logger.warn("Pedido com ID {} foi deletado.", id);
        orderRepository.deleteById(id);
    }

    public Order atualizarOrder(Long id, Order orderAtualizado) {
        return orderRepository.findById(id)
                .map(order -> {
                    order.setItem(orderAtualizado.getItem());
                    order.setQuantity(orderAtualizado.getQuantity());
                    order.setValue(orderAtualizado.getValue());
                    Order orderSalvo = orderRepository.save(order);
                    logger.info("Pedido com ID {} foi atualizado.", orderSalvo.getIdOrder());
                    return orderSalvo;
                })
                .orElse(null);
    }
}