package com.tickets.service;

import com.tickets.domain.User;
import com.tickets.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> listarTodosUsers() {
        return userRepository.findAll();
    }

    public Optional<User> buscarUserPorId(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> buscarUserPorEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public User salvarUser(User user) {
        return userRepository.save(user);
    }

    public void deletarUser(Long id) {
        userRepository.deleteById(id);
    }
}