package com.DMA.Service_B.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import javax.sql.DataSource;
import jakarta.persistence.EntityManagerFactory;
import java.util.HashMap;

@Configuration
@EnableJpaRepositories(
    basePackages = "com.DMA.Service_B.repository.leader",
    entityManagerFactoryRef = "leaderEntityManagerFactory",
    transactionManagerRef = "leaderTransactionManager"
)
public class DatabaseConfig {
    
    @Value("${spring.datasource.leader.url:jdbc:postgresql://localhost:5432/outbox_write}")
    private String leaderUrl;
    
    @Value("${spring.datasource.leader.username:postgres}")
    private String leaderUsername;
    
    @Value("${spring.datasource.leader.password:gerges1020}")
    private String leaderPassword;
    
    @Value("${spring.datasource.leader.driver-class-name:org.postgresql.Driver}")
    private String leaderDriverClassName;
    
    @Primary
    @Bean(name = "leaderDataSource")
    public DataSource leaderDataSource() {
        return DataSourceBuilder.create()
            .url(leaderUrl)
            .username(leaderUsername)
            .password(leaderPassword)
            .driverClassName(leaderDriverClassName)
            .build();
    }

    @Primary
    @Bean(name = "leaderEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean leaderEntityManagerFactory(
            @Qualifier("leaderDataSource") DataSource dataSource) {
        
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);
        em.setPackagesToScan("com.DMA.Service_B.entity");
        
        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);
        
        HashMap<String, Object> properties = new HashMap<>();
        properties.put("hibernate.hbm2ddl.auto", "update");
        properties.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        properties.put("hibernate.show_sql", "false");
        properties.put("hibernate.format_sql", "true");
        em.setJpaPropertyMap(properties);
        
        return em;
    }

    @Primary
    @Bean(name = "leaderTransactionManager")
    public JpaTransactionManager leaderTransactionManager(
            @Qualifier("leaderEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}
