package com.github.tomitakussaari.phaas.user.dao;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.hibernate.annotations.NaturalId;

import javax.persistence.*;
import java.util.Arrays;
import java.util.List;

@Entity
@Data
@Table(name = "user")
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;
    @NonNull
    @NaturalId
    private String userName;
    @NonNull
    private String passwordHash;
    @NonNull
    private String roles;

    public List<String> roles() {
        return Arrays.asList(roles.split(","));
    }
}
