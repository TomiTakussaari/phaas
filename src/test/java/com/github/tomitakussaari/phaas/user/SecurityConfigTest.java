package com.github.tomitakussaari.phaas.user;

import com.github.tomitakussaari.phaas.model.DataProtectionScheme;
import com.github.tomitakussaari.phaas.model.PasswordEncodingAlgorithm;
import com.github.tomitakussaari.phaas.user.SecurityConfig.PhaasAuthenticator;
import com.github.tomitakussaari.phaas.util.CryptoHelper;
import com.github.tomitakussaari.phaas.util.PepperSource;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static com.github.tomitakussaari.phaas.user.SecurityConfig.USER_NOT_FOUND_PASSWORD;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SecurityConfigTest {

    private final UsersService usersService = Mockito.mock(UsersService.class);
    private final CryptoHelper mockCryptoHelper = Mockito.mock(CryptoHelper.class);
    private final CryptoHelper cryptoHelper = new CryptoHelper(new PepperSource("foo"));
    private final String userNotFoundCryptedData = cryptoHelper.encryptData(USER_NOT_FOUND_PASSWORD, "data");
    private final UsernamePasswordAuthenticationToken authenticationToken = Mockito.mock(UsernamePasswordAuthenticationToken.class);
    private final String encryptedData = cryptoHelper.encryptData("correct-password", "my-data");
    private final DataProtectionScheme dataProtectionScheme = new DataProtectionScheme(1, PasswordEncodingAlgorithm.SHA256_BCRYPT, encryptedData, cryptoHelper);
    private final PhaasUser phaasuser = Mockito.mock(PhaasUser.class);
    private final PhaasAuthenticator authenticator = new PhaasAuthenticator(usersService, mockCryptoHelper, userNotFoundCryptedData);

    @Test(expected = BadCredentialsException.class)
    public void rejectsInvalidPassword() {
        when(authenticationToken.getCredentials()).thenReturn("wrong-password");
        when(phaasuser.activeProtectionScheme()).thenReturn(dataProtectionScheme);
        authenticator.additionalAuthenticationChecks(phaasuser, authenticationToken);
    }

    @Test
    public void acceptsCorrectPassword() {
        when(authenticationToken.getCredentials()).thenReturn("correct-password");
        when(phaasuser.activeProtectionScheme()).thenReturn(dataProtectionScheme);
        authenticator.additionalAuthenticationChecks(phaasuser, authenticationToken);
    }

    @Test
    public void spendsTimeDecryptingUserNotFoundCryptedDataWhenUserIsNotFound() { //to prevent attackers from checking response times and scanning for available usernames
        when(usersService.loadUserByUsername("foobar")).thenThrow(new UsernameNotFoundException(""));
        try {
            authenticator.retrieveUser("foobar", authenticationToken);
            fail("should have thrown user not found");
        } catch(UsernameNotFoundException expected) {
            verify(mockCryptoHelper).decryptData("userNotFoundPassword", userNotFoundCryptedData);
        }
    }
}