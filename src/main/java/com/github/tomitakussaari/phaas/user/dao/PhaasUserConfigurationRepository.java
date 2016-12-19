package com.github.tomitakussaari.phaas.user.dao;

import org.springframework.data.repository.CrudRepository;

import java.util.List;


public interface PhaasUserConfigurationRepository extends CrudRepository<UserConfigurationDTO, Integer> {

    List<UserConfigurationDTO> findByUser(String userName);
}
