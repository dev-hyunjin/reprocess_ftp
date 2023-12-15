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
@MapperScan(value = "com.app.find.mapper.oracle", sqlSessionFactoryRef = "oracleSqlSessionFactory")
@EnableTransactionManagement
public class SqlSessionConfigOracle {

    @Bean(name = "oracleDataSource")
    @ConfigurationProperties(prefix = "spring.oracle.datasource")
    public DataSource oracleDataSource() {
        return DataSourceBuilder.create().type(HikariDataSource.class).build();
    }

    @Bean(name = "oracleSqlSessionFactory")
    public SqlSessionFactory oracleSqlSessionFactory(@Qualifier("oracleDataSource") DataSource oracleDataSource, ApplicationContext applicationContext) throws Exception {
        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
        sqlSessionFactoryBean.setDataSource(oracleDataSource);
        sqlSessionFactoryBean.setVfs(SpringBootVFS.class);

        sqlSessionFactoryBean.setMapperLocations(applicationContext.getResources("classpath:sqlmap/oracle/*.xml"));
        return sqlSessionFactoryBean.getObject();
    }

    /* SqlSession */
    @Bean(name = "oracleSqlSessionTemplate")
    public SqlSessionTemplate gwSqlSessionTemplate(@Autowired @Qualifier("oracleSqlSessionFactory") SqlSessionFactory gwSqlSessionFactory) {
        return new SqlSessionTemplate(gwSqlSessionFactory);
    }
}
