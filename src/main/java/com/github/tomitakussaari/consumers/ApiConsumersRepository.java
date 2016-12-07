package com.github.tomitakussaari.consumers;

import lombok.*;
import org.springframework.data.repository.CrudRepository;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public interface ApiConsumersRepository extends CrudRepository<ApiConsumersRepository.ApiConsumer, String> {


    ApiConsumer findByUserName(String userName);


    @Entity
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    class ApiConsumer {

        @Id
        @NonNull
        private String userName;
        @NonNull
        private String passwordHash;

        private String roles;

        public List<String> getRoles() {
            return Arrays.asList(roles.split(","));
        }

        public void setRoles(List<String> roles) {
            this.roles = roles.stream().collect(Collectors.joining(","));
        }
    }
}
