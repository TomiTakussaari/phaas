package com.github.tomitakussaari.user;

import lombok.*;
import org.springframework.data.repository.CrudRepository;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Arrays;
import java.util.List;

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
        @NonNull
        private String roles;

        public List<String> roles() {
            return Arrays.asList(roles.split(","));
        }

    }
}
