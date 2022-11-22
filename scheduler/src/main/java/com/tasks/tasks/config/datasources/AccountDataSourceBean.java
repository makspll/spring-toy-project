package com.tasks.tasks.config.datasources;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.init.DatabasePopulator;
import org.springframework.jdbc.datasource.init.DatabasePopulatorUtils;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.tasks.tasks.entities.Account;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = "com.tasks.tasks.dao.account", entityManagerFactoryRef = "accountEntityManagerFactory", transactionManagerRef = "accountTransactionManager")
public class AccountDataSourceBean {

    @Autowired
    private Environment env;

    @Bean
    @Qualifier("accountDataSource")
    @Primary
    public DataSource accountDataSource() {

        DataSource dataSource = DataSourceBuilder.create()
                .driverClassName(env.getProperty("spring.account-datasource.driver-class-name"))
                .url(env.getProperty("spring.account-datasource.jdbc-url"))
                .username(env.getProperty("spring.account-datasource.username"))
                .password(env.getProperty("spring.account-datasource.password"))
                .build();

        Resource initSchema = new ClassPathResource("sql/schema-accounts.sql");
        DatabasePopulator databasePopulator = new ResourceDatabasePopulator(initSchema);
        DatabasePopulatorUtils.execute(databasePopulator, dataSource);

        return dataSource;
    }

    @Bean
    @Primary
    public LocalContainerEntityManagerFactoryBean accountEntityManagerFactory(
            EntityManagerFactoryBuilder entityManagerFactoryBuilder,
            @Qualifier("accountDataSource") DataSource dataSource) {
        return entityManagerFactoryBuilder
                .dataSource(dataSource)
                .packages(Account.class)
                .build();
    }

    @Bean
    @Primary
    public PlatformTransactionManager accountTransactionManager(
            @Qualifier("accountEntityManagerFactory") EntityManagerFactory accountEntityManagerFactory) {
        return new JpaTransactionManager(accountEntityManagerFactory);
    }
}
