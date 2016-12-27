package com.github.tomitakussaari.phaas.user.dao;

import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface UserRepository extends CrudRepository<UserDTO, Integer> {

    Optional<UserDTO> findByUserName(String userName);

}
