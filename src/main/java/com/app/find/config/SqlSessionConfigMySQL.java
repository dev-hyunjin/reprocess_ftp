package com.app.find.config;

import com.zaxxer.hikari.HikariDataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.mybatis.spring.boot.autoconfigure.SpringBootVFS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

@Configuration
@MapperScan(value = "com.app.find.mapper.mysql", sqlSessionFactoryRef = "mysqlSqlSessionFactory")
@EnableTransactionManagement
public class SqlSessionConfigMySQL {

    @Bean(name = "mysqlDataSource")
    @ConfigurationProperties(prefix = "spring.mysql.datasource")
    public DataSource mysqlDataSource() {
        return DataSourceBuilder.create().type(HikariDataSource.class).build();
    }

    @Bean(name = "mysqlSqlSessionFactory")
    public SqlSessionFactory mysqlSqlSessionFactory(@Qualifier("mysqlDataSource") DataSource mysqlDataSource, ApplicationContext applicationContext) throws Exception {
        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
        sqlSessionFactoryBean.setDataSource(mysqlDataSource);
        sqlSessionFactoryBean.setVfs(SpringBootVFS.class);

        sqlSessionFactoryBean.setMapperLocations(applicationContext.getResources("classpath:sqlmap/mysql/*.xml"));
        return sqlSessionFactoryBean.getObject();
    }

    /* SqlSession */
    @Bean(name = "mysqlSqlSessionTemplate")
    public SqlSessionTemplate gwSqlSessionTemplate(@Autowired @Qualifier("mysqlSqlSessionFactory") SqlSessionFactory gwSqlSessionFactory) {
        return new SqlSessionTemplate(gwSqlSessionFactory);
    }
}
