package edu.sjsu.cmpe281.config;

import javax.sql.DataSource;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.orm.hibernate5.LocalSessionFactoryBuilder;
import org.springframework.orm.hibernate5.HibernateTransactionManager;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;

@Configuration
@EntityScan("edu.sjsu.cmpe281.model")
@EnableTransactionManagement
public class RDSConfig {
    @Value("${rekopot.rds.driver}") String driverClassName;
    @Value("${rekopot.rds.url}") String url;
    @Value("${rekopot.rds.username}") String username;
    @Value("${rekopot.rds.password}") String password;
    @Bean(name = "dataSource")
    public DataSource getDataSource() {
	DataSource dataSource = DataSourceBuilder
		.create()
		.username(username)
		.password(password)
		.url(url)
		.driverClassName(driverClassName)
		.build();
	return dataSource;
    }
    @Bean(name = "sessionFactory")
    public SessionFactory getSessionFactory(DataSource dataSource) {
	LocalSessionFactoryBuilder sessionBuilder = new LocalSessionFactoryBuilder(dataSource);
	sessionBuilder.scanPackages("edu.sjsu.cmpe281.model");
	return sessionBuilder.buildSessionFactory();
    }
    @Bean(name = "transactionManager")
    public HibernateTransactionManager getTransactionManager(
	    SessionFactory sessionFactory) {
	HibernateTransactionManager transactionManager = new HibernateTransactionManager(
		sessionFactory);
	return transactionManager;
    }
    @Bean
    public DataSourceInitializer dataSourceInitializer(final DataSource dataSource) {
	final DataSourceInitializer initializer = new DataSourceInitializer();
	initializer.setDataSource(dataSource);
	return initializer;
    }
}
