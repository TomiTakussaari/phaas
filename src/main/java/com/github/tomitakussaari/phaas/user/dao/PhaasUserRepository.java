package com.github.tomitakussaari.phaas.user.dao;

import org.springframework.data.repository.CrudRepository;

public interface PhaasUserRepository extends CrudRepository<UserDTO, Integer> {

    UserDTO findByUserName(String userName);


}
