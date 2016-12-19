package com.github.tomitakussaari.phaas.user.dao;

import com.github.tomitakussaari.phaas.model.ProtectionScheme;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.security.crypto.encrypt.Encryptors;

import javax.persistence.*;

@Entity
@Table(name = "user_configuration")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class UserConfigurationDTO {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;
    private String user;
    private String dataProtectionKey;
    private boolean active;
    private ProtectionScheme.PasswordEncodingAlgorithm algorithm;


}
