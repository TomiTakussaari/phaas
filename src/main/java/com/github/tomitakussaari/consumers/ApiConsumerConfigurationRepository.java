package com.github.tomitakussaari.consumers;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.repository.CrudRepository;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.util.List;


public interface ApiConsumerConfigurationRepository extends CrudRepository<ApiConsumerConfigurationRepository.ApiConsumerConfiguration, Integer> {


    List<ApiConsumerConfiguration> findByUserName(String userName);

    @Entity
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    class ApiConsumerConfiguration {
        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        private Integer id;
        private String userName;
        private String enc_key;
        private boolean active;
        private String algorithm;

    }
}
