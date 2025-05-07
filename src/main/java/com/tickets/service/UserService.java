package com.tickets.service;

import com.tickets.domain.User;
import com.tickets.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> listarTodosUsers() {
        logger.info("TRANSACTION START: Listando todos os usuários.");
        try {
            List<User> users = userRepository.findAll();
            logger.info("TRANSACTION END: Listagem de {} usuários concluída.", users.size());
            return users;
        } catch (Exception e) {
            logger.error("TRANSACTION ERROR: Erro ao listar todos os usuários.", e);
            throw e;
        }
    }

    public Optional<User> buscarUserPorId(Long id) {
        logger.info("TRANSACTION START: Buscando usuário com ID: {}.", id);
        try {
            Optional<User> user = userRepository.findById(id);
            if (user.isPresent()) {
                logger.info("TRANSACTION END: Usuário com ID {} encontrado: {}.", id, user.get());
            } else {
                logger.warn("TRANSACTION END: Usuário com ID {} não encontrado.", id);
            }
            return user;
        } catch (Exception e) {
            logger.error("TRANSACTION ERROR: Erro ao buscar usuário com ID: {}.", id, e);
            throw e;
        }
    }

    public Optional<User> buscarUserPorEmail(String email) {
        logger.info("TRANSACTION START: Buscando usuário com email: {}.", email);
        try {
            Optional<User> user = userRepository.findByEmail(email);
            if (user.isPresent()) {
                logger.info("TRANSACTION END: Usuário com email {} encontrado: {}.", email, user.get());
            } else {
                logger.warn("TRANSACTION END: Usuário com email {} não encontrado.", email);
            }
            return user;
        } catch (Exception e) {
            logger.error("TRANSACTION ERROR: Erro ao buscar usuário com email: {}.", email, e);
            throw e;
        }
    }

    public User salvarUser(User user) {
        logger.info("TRANSACTION START: Salvando novo usuário: {}.", user);
        try {
            User userSalvo = userRepository.save(user);
            logger.info("TRANSACTION END: Usuário criado com ID: {}.", userSalvo.getIdUser());
            return userSalvo;
        } catch (Exception e) {
            logger.error("TRANSACTION ERROR: Erro ao salvar o usuário: {}.", user, e);
            throw e;
        }
    }

    public void deletarUser(Long id) {
        logger.warn("TRANSACTION START: Deletando usuário com ID: {}.", id);
        try {
            if (userRepository.existsById(id)) {
                userRepository.deleteById(id);
                logger.warn("TRANSACTION END: Usuário com ID {} deletado com sucesso.", id);
            } else {
                logger.warn("TRANSACTION END: Usuário com ID {} não encontrado para deletar.", id);
            }
        } catch (Exception e) {
            logger.error("TRANSACTION ERROR: Erro ao deletar o usuário com ID: {}.", id, e);
            throw e;
        }
    }
}