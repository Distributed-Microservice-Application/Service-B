package com.DMA.Service_B.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import javax.sql.DataSource;
import jakarta.persistence.EntityManagerFactory;
import java.util.HashMap;

@Configuration
@EnableJpaRepositories(
    basePackages = "com.DMA.Service_B.repository.replica",
    entityManagerFactoryRef = "replicaEntityManagerFactory",
    transactionManagerRef = "replicaTransactionManager"
)
public class ReplicaDatabaseConfig {

    @Value("${spring.datasource.replica.url:jdbc:postgresql://localhost:5432/outbox_read}")
    private String replicaUrl;
    
    @Value("${spring.datasource.replica.username:postgres}")
    private String replicaUsername;
    
    @Value("${spring.datasource.replica.password:gerges1020}")
    private String replicaPassword;
    
    @Value("${spring.datasource.replica.driver-class-name:org.postgresql.Driver}")
    private String replicaDriverClassName;

    @Bean(name = "replicaDataSource")
    public DataSource replicaDataSource() {
        return DataSourceBuilder.create()
            .url(replicaUrl)
            .username(replicaUsername)
            .password(replicaPassword)
            .driverClassName(replicaDriverClassName)
            .build();
    }

    @Bean(name = "replicaEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean replicaEntityManagerFactory(
            @Qualifier("replicaDataSource") DataSource dataSource) {
        
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);
        em.setPackagesToScan("com.DMA.Service_B.entity");
        
        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);
        
        HashMap<String, Object> properties = new HashMap<>();
        properties.put("hibernate.hbm2ddl.auto", "update");
        properties.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        properties.put("hibernate.show_sql", "true");
        em.setJpaPropertyMap(properties);
        
        return em;
    }

    @Bean(name = "replicaTransactionManager")
    public JpaTransactionManager replicaTransactionManager(
            @Qualifier("replicaEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}
