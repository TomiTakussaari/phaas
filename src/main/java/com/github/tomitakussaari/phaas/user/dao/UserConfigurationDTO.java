package com.github.tomitakussaari.phaas.user.dao;

import com.github.tomitakussaari.phaas.model.DataProtectionScheme;
import com.github.tomitakussaari.phaas.model.PasswordEncodingAlgorithm;
import com.github.tomitakussaari.phaas.util.CryptoHelper;
import lombok.*;
import lombok.experimental.Accessors;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "user_configuration")
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@Getter
public final class UserConfigurationDTO implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;
    private String user;
    private String dataProtectionKey;
    private boolean active;
    private PasswordEncodingAlgorithm algorithm;

    public DataProtectionScheme toProtectionScheme(CryptoHelper helper) {
        return new DataProtectionScheme(getId(), getAlgorithm(), getDataProtectionKey(), helper);
    }

}
