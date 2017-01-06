package com.github.tomitakussaari.phaas.user.dao;

import lombok.*;
import lombok.experimental.Accessors;
import org.hibernate.annotations.NaturalId;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

@Entity
@Table(name = "user")
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@Getter
@Setter
public final class UserDTO implements Serializable {

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

    private String sharedSecretForSigningCommunication;

    public List<String> roles() {
        return Arrays.asList(roles.split(","));
    }
}
