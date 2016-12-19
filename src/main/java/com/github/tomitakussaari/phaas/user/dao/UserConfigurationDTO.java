package com.github.tomitakussaari.phaas.user.dao;

import com.github.tomitakussaari.phaas.model.ProtectionScheme;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;

import javax.persistence.*;

@Entity
@Table(name = "user_configuration")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserConfigurationDTO {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;
    private String user;
    private String dataProtectionKey;
    private boolean active;
    private ProtectionScheme.PasswordEncodingAlgorithm algorithm;

    public String decryptDataProtectionKey(String encryptionPassword) {
        TextEncryptor decryptor = Encryptors.text(encryptionPassword, salt());
        return decryptor.decrypt(cryptPassword());
    }

    private String salt() {
        return dataProtectionKey.split(":::")[0];
    }

    private String cryptPassword() {
        return dataProtectionKey.split(":::")[1];
    }

}
