package com.erbol.ems.config;

import com.erbol.ems.category.Category;
import com.erbol.ems.category.CategoryRepository;
import com.erbol.ems.user.Administrator;
import com.erbol.ems.user.UserRepository;
import com.erbol.ems.user.UserType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private static final String DEFAULT_ADMIN_EMAIL = "admin@ems.local";
    private static final String DEFAULT_ADMIN_PASSWORD = "Admin123!";
    private static final String DEFAULT_ADMIN_NAME = "Default Administrator";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CategoryRepository categoryRepository;

    public DataSeeder(UserRepository userRepository,
                      PasswordEncoder passwordEncoder,
                      CategoryRepository categoryRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.categoryRepository = categoryRepository;
    }

    @Override
    public void run(String... args) {
        seedDefaultAdmin();
        seedDefaultCategories();
    }

    private void seedDefaultAdmin() {
        if (userRepository.existsByEmail(DEFAULT_ADMIN_EMAIL)) {
            log.info("Default administrator already exists, skipping seed.");
            return;
        }

        Administrator admin = new Administrator(
                DEFAULT_ADMIN_NAME,
                DEFAULT_ADMIN_EMAIL,
                passwordEncoder.encode(DEFAULT_ADMIN_PASSWORD)
        );
        userRepository.save(admin);

        log.info("Seeded default administrator: email={}, type={}",
                DEFAULT_ADMIN_EMAIL, UserType.ADMIN);
    }

    private void seedDefaultCategories() {
        if (categoryRepository.count() > 0) {
            log.info("Categories already exist, skipping seed.");
            return;
        }

        List<Category> defaults = List.of(
                new Category("Technology", "IT conferences, hackathons, workshops"),
                new Category("Music", "Concerts, festivals, music events"),
                new Category("Business", "Networking events, startup meetups"),
                new Category("Education", "Lectures, courses, training"),
                new Category("Sports", "Sporting events and tournaments"),
                new Category("Arts", "Exhibitions, theater, cultural events")
        );
        categoryRepository.saveAll(defaults);
        log.info("Seeded {} default categories.", defaults.size());
    }
}