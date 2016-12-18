package com.github.tomitakussaari.user;

import lombok.*;
import org.springframework.data.repository.CrudRepository;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public interface PhaasUserRepository extends CrudRepository<PhaasUserRepository.UserDTO, String> {


    UserDTO findByUserName(String userName);


    @Entity
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    class UserDTO {

        @Id
        @NonNull
        private String userName;
        @NonNull
        private String passwordHash;

        private String roles;

        public List<String> getRoles() {
            return Arrays.asList(roles.split(","));
        }

    }
}
