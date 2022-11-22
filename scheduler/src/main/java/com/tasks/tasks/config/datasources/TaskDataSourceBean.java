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

import com.tasks.tasks.entities.Task;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = "com.tasks.tasks.dao.task", entityManagerFactoryRef = "taskEntityManagerFactory", transactionManagerRef = "taskTransactionManager")
public class TaskDataSourceBean {

    @Autowired
    private Environment env;

    @Bean
    @Qualifier("taskDataSource")
    public DataSource taskDataSource() {

        DataSource dataSource = DataSourceBuilder.create()
                .driverClassName(env.getProperty("spring.task-datasource.driver-class-name"))
                .url(env.getProperty("spring.task-datasource.jdbc-url"))
                .username(env.getProperty("spring.task-datasource.username"))
                .password(env.getProperty("spring.task-datasource.password"))
                .build();

        Resource initSchema = new ClassPathResource("sql/schema-tasks.sql");
        DatabasePopulator databasePopulator = new ResourceDatabasePopulator(initSchema);
        DatabasePopulatorUtils.execute(databasePopulator, dataSource);

        return dataSource;
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean taskEntityManagerFactory(
            EntityManagerFactoryBuilder entityManagerFactoryBuilder,
            @Qualifier("taskDataSource") DataSource dataSource) {
        return entityManagerFactoryBuilder
                .dataSource(dataSource)
                .packages(Task.class)
                .build();
    }

    @Bean
    public PlatformTransactionManager taskTransactionManager(
            @Qualifier("taskEntityManagerFactory") EntityManagerFactory taskEntityManagerFactory) {
        return new JpaTransactionManager(taskEntityManagerFactory);
    }
}
