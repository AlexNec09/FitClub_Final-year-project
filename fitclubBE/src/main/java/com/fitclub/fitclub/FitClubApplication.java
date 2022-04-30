package com.fitclub.fitclub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class FitClubApplication {

    public static void main(String[] args) {
        SpringApplication.run(FitClubApplication.class, args);
    }


//    //To be removed
//    @Bean
//    @Profile("dev")
//    CommandLineRunner run(UserService userService) {
//        return (args) -> {
//            IntStream.rangeClosed(1, 15).mapToObj(i -> {
//                User user = new User();
//                user.setUsername("user" + i);
//                user.setDisplayName("display" + i);
//                user.setPassword("P4ssword");
//                return user;
//            }).forEach(userService::save);
//        };
//    }
}
