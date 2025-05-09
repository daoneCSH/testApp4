package com.springboot.testapp4.config;

import jakarta.persistence.EntityManagerFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        basePackages = "com.springboot.testapp4.repository",
        entityManagerFactoryRef = "entityManagerFactory",
        transactionManagerRef = "transactionManager"
)

public class DataSourceConfig {
    private static final String MARIADB_URL = "jdbc:mariadb://localhost:3307/testApp4";
    private static final String MARIADB_USER = "root";
    private static final String MARIADB_PASSWORD = "112233";
    private static final String MARIADB_DRIVER = "org.mariadb.jdbc.Driver";

    private static final String PGSQL_URL = "jdbc:postgresql://localhost:5432/testApp4db";
    private static final String PGSQL_USER = "postgres";
    private static final String PGSQL_PASSWORD = "112233";
    private static final String PGSQL_DRIVER = "org.postgresql.Driver";

    private static final String MYSQL_URL = "jdbc:mysql://localhost:3309/testapp4";
    private static final String MYSQL_USER = "root";
    private static final String MYSQL_PASSWORD = "112233";
    private static final String MYSQL_DRIVER = "com.mysql.cj.jdbc.Driver";

    @Bean
    public DataSource db1() {
        return DataSourceBuilder.create()
                .url(MARIADB_URL)
                .username(MARIADB_USER)
                .password(MARIADB_PASSWORD)
                .driverClassName(MARIADB_DRIVER)
                .build();
    }

    @Bean
    public DataSource db2() {
        return DataSourceBuilder.create()
                .url(PGSQL_URL)
                .username(PGSQL_USER)
                .password(PGSQL_PASSWORD)
                .driverClassName(PGSQL_DRIVER)
                .build();
    }

    @Bean
    public DataSource db3() {
        return DataSourceBuilder.create()
                .url(MYSQL_URL)
                .username(MYSQL_USER)
                .password(MYSQL_PASSWORD)
                .driverClassName(MYSQL_DRIVER)
                .build();
    }

    @Primary
    @Bean
    public DataSource dynamicDataSource() {
        Map<Object, Object> dataSources = new HashMap<>();
        dataSources.put("DB1", db1());
        dataSources.put("DB2", db2());
        dataSources.put("DB3", db3());
        dataSources.put("MARIADB", db1());
        dataSources.put("PGSQL", db2());
        dataSources.put("MYSQL", db3());

        DynamicDataSource routingDataSource = new DynamicDataSource();
        routingDataSource.setDefaultTargetDataSource(db1());
        routingDataSource.setTargetDataSources(dataSources);
        return routingDataSource;
    }

    @Primary
    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dynamicDataSource) {
        LocalContainerEntityManagerFactoryBean emf = new LocalContainerEntityManagerFactoryBean();
        emf.setDataSource(dynamicDataSource);
        emf.setPackagesToScan("com.springboot.testapp4.domain"); // 엔티티 경로
        emf.setJpaVendorAdapter(new HibernateJpaVendorAdapter());

        Map<String, Object> jpaProps = new HashMap<>();
        jpaProps.put("hibernate.hbm2ddl.auto", "update");
        jpaProps.put("hibernate.dialect", "org.hibernate.dialect.MySQLDialect");
        emf.setJpaPropertyMap(jpaProps);

        return emf;
    }

    @Primary
    @Bean
    public PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}
