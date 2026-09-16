package edu.kit.informatik;

import edu.kit.informatik.model.Role;
import edu.kit.informatik.model.User;
import edu.kit.informatik.repositories.UserRepository;

import java.sql.Timestamp;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AdminSeedConfiguration {

    @Bean
    @ConditionalOnProperty(
        name = "app.seed-admin.enabled",
        havingValue = "true"
    )
    CommandLineRunner seedAdmin(
        UserRepository userRepository,
        PasswordEncoder passwordEncoder,
        @Value("${app.seed-admin.email}") String email,
        @Value("${app.seed-admin.password}") String password
    ) {
        return args -> {
            if (userRepository.existsByEmail(email)) {
                return;
            }

            User admin = new User("admin", "admin", email, "admin",
                true, Role.ADMIN, new Timestamp(0), new Timestamp(0));
            
            admin.setPassword(passwordEncoder.encode(password));
            
            userRepository.save(admin);
        };
    }
}