package org.am.mypotrfolio;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "org.am.mypotrfolio")
public class MyPotrfolioApplication {

    public static void main(String[] args) {
        SpringApplication.run(MyPotrfolioApplication.class, args);
    }

}
