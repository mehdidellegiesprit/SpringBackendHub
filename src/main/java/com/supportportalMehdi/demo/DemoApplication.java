package com.supportportalMehdi.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;

import static com.supportportalMehdi.demo.constant.FileConstant.USER_FOLDER;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class,
		DataSourceTransactionManagerAutoConfiguration.class,
		HibernateJpaAutoConfiguration.class})
public class DemoApplication {

	@Bean
	public WebMvcConfigurer corsConfigurer() {
		return new WebMvcConfigurer() {
			@Override
			public void addCorsMappings(CorsRegistry registry) {
				registry.addMapping("/**")
//						.allowedOrigins("http://localhost:4200", "https://spring-server-pfe-cc201bffbf5c.herokuapp.com","https://angular-server-pfe-6e7b27eebb55.herokuapp.com") // Remplacez par l'URL de votre frontend
//						.allowedOrigins(
//								"http://localhost:4200",
//								"https://angular-server-pfe-6e7b27eebb55.herokuapp.com",
//								"https://spring-server-pfe-cc201bffbf5c.herokuapp.com"
//						)
						.allowedOrigins("*") // Ajoutez ici les URL de vos frontends
						.allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
						.allowedHeaders("*")
						.allowCredentials(true);
			}
		};
	}


	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
		new File(USER_FOLDER).mkdirs();
	}
//	@Bean
//	public CorsFilter corsFilter() {
//		UrlBasedCorsConfigurationSource urlBasedCorsConfigurationSource = new UrlBasedCorsConfigurationSource();
//		CorsConfiguration corsConfiguration = new CorsConfiguration();
//		corsConfiguration.setAllowCredentials(true);
//		corsConfiguration.setAllowedOrigins(Arrays.asList("http://localhost:4200", "http://localhost:4300"));
//		//corsConfiguration.setAllowedOrigins(Collections.singletonList("http://localhost:4200"));
//		corsConfiguration.setAllowedHeaders(Arrays.asList("Origin", "Access-Control-Allow-Origin", "Content-Type",
//				"Accept", "Jwt-Token", "Authorization", "Origin, Accept", "X-Requested-With",
//				"Access-Control-Request-Method", "Access-Control-Request-Headers"));
//		corsConfiguration.setExposedHeaders(Arrays.asList("Origin", "Content-Type", "Accept", "Jwt-Token", "Authorization",
//				"Access-Control-Allow-Origin", "Access-Control-Allow-Origin", "Access-Control-Allow-Credentials"));
//		corsConfiguration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
//		urlBasedCorsConfigurationSource.registerCorsConfiguration("/**", corsConfiguration);
//		return new CorsFilter(urlBasedCorsConfigurationSource);
//	}
//	//just commentaire
@Bean
public CorsFilter corsFilter() {
	UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
	CorsConfiguration config = new CorsConfiguration();
	config.setAllowCredentials(true);
	config.setAllowedOrigins(Arrays.asList(
			"http://localhost:4200",
			"http://localhost:4300",
			"https://angular-server-pfe-6e7b27eebb55.herokuapp.com",
			"https://spring-server-pfe-cc201bffbf5c.herokuapp.com"
	));
	config.setAllowedHeaders(Arrays.asList(
			"Origin", "Content-Type", "Accept", "Jwt-Token", "Authorization",
			"Access-Control-Allow-Origin", "Access-Control-Request-Method",
			"Access-Control-Request-Headers"
	));
	config.setExposedHeaders(Arrays.asList(
			"Origin", "Content-Type", "Accept", "Jwt-Token", "Authorization",
			"Access-Control-Allow-Origin", "Access-Control-Allow-Credentials"
	));
	config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
	source.registerCorsConfiguration("/**", config);
	return new CorsFilter(source);
}

	@Bean
	public BCryptPasswordEncoder bCryptPasswordEncoder (){
		//TODO l methode hethi fel lekher nejmou n7otouha fel SecurityConfiguration
		return new BCryptPasswordEncoder () ;
	}

}
