package com.github.tomitakussaari.consumers;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Component
public class ApiConsumersService implements UserDetailsService {

    private final ApiConsumerConfigurationRepository apiConsumerConfigurationRepository;
    private final ApiConsumersRepository apiConsumersRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        ApiConsumersRepository.ApiConsumer apiConsumer = apiConsumersRepository.findByUserName(username);
        if (apiConsumer == null) {
            throw new UsernameNotFoundException("not found: " + username);
        }
        return new PHaasUserDetails(apiConsumer, apiConsumerConfigurationRepository.findByUserName(username));
    }
}
