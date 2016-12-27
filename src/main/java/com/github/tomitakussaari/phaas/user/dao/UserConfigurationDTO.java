package com.github.tomitakussaari.phaas.user.dao;

import com.github.tomitakussaari.phaas.model.DataProtectionScheme;
import com.github.tomitakussaari.phaas.model.PasswordEncodingAlgorithm;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "user_configuration")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public final class UserConfigurationDTO implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;
    private String user;
    private String dataProtectionKey;
    private boolean active;
    private PasswordEncodingAlgorithm algorithm;

    public DataProtectionScheme toProtectionScheme() {
        return new DataProtectionScheme(getId(), getAlgorithm(), getDataProtectionKey());
    }

}
