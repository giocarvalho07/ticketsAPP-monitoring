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
        logger.info("TRANSACTION START: Listando todos os pedidos.");
        try {
            List<Order> orders = orderRepository.findAll();
            logger.info("TRANSACTION END: Listagem de {} pedidos concluída.", orders.size());
            return orders;
        } catch (Exception e) {
            logger.error("TRANSACTION ERROR: Erro ao listar todos os pedidos.", e);
            throw e; // É importante relançar a exceção para que a camada superior a trate.
        }
    }

    public Optional<Order> buscarOrderPorId(Long id) {
        logger.info("TRANSACTION START: Buscando pedido com ID: {}.", id);
        try {
            Optional<Order> order = orderRepository.findById(id);
            if (order.isPresent()) {
                logger.info("TRANSACTION END: Pedido com ID {} encontrado: {}.", id, order.get());
            } else {
                logger.warn("TRANSACTION END: Pedido com ID {} não encontrado.", id);
            }
            return order;
        } catch (Exception e) {
            logger.error("TRANSACTION ERROR: Erro ao buscar pedido com ID: {}.", id, e);
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public Optional<Order> buscarOrderComUsuario(Long id) {
        logger.info("TRANSACTION START: Buscando pedido com ID {} e seu usuário.", id);
        try {
            Optional<Order> order = orderRepository.findById(id);
            if (order.isPresent()) {
                logger.info("TRANSACTION END: Pedido com ID {} e usuário {} encontrado.", id, order.get().getUser().getName());
            } else {
                logger.warn("TRANSACTION END: Pedido com ID {} não encontrado.", id);
            }
            return order;
        } catch (Exception e) {
            logger.error("TRANSACTION ERROR: Erro ao buscar pedido com ID {} e usuário.", id, e);
            throw e;
        }
    }

    public Order salvarOrder(Order order) {
        logger.info("TRANSACTION START: Salvando novo pedido: {}.", order);
        try {
            Order orderSalvo = orderRepository.save(order);
            logger.info("TRANSACTION END: Pedido criado com ID: {}.", orderSalvo.getIdOrder());
            return orderSalvo;
        } catch (Exception e) {
            logger.error("TRANSACTION ERROR: Erro ao salvar o pedido: {}.", order, e);
            throw e;
        }
    }

    public void deletarOrder(Long id) {
        logger.warn("TRANSACTION START: Deletando pedido com ID: {}.", id);
        try {
            if (orderRepository.existsById(id)) {
                orderRepository.deleteById(id);
                logger.warn("TRANSACTION END: Pedido com ID {} deletado com sucesso.", id);
            } else {
                logger.warn("TRANSACTION END: Pedido com ID {} não encontrado para deletar.", id);
            }
        } catch (Exception e) {
            logger.error("TRANSACTION ERROR: Erro ao deletar o pedido com ID: {}.", id, e);
            throw e;
        }
    }

    public Order atualizarOrder(Long id, Order orderAtualizado) {
        logger.info("TRANSACTION START: Atualizando pedido com ID {}. Dados: {}", id, orderAtualizado);
        try {
            return orderRepository.findById(id)
                    .map(order -> {
                        order.setItem(orderAtualizado.getItem());
                        order.setQuantity(orderAtualizado.getQuantity());
                        order.setValue(orderAtualizado.getValue());
                        Order orderSalvo = orderRepository.save(order);
                        logger.info("TRANSACTION END: Pedido com ID {} atualizado com sucesso. Novos dados: {}", orderSalvo.getIdOrder(), orderSalvo);
                        return orderSalvo;
                    })
                    .orElseGet(() -> {
                        logger.warn("TRANSACTION END: Pedido com ID {} não encontrado para atualizar.", id);
                        return null;
                    });
        } catch (Exception e) {
            logger.error("TRANSACTION ERROR: Erro ao atualizar o pedido com ID: {} e dados: {}.", id, orderAtualizado, e);
            throw e;
        }
    }
}