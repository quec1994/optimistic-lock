package com.quec1994.config;

import com.quec1994.interceptor.OptimisticLockerPlugin;
import com.zaxxer.hikari.HikariDataSource;
import lombok.AllArgsConstructor;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import javax.sql.DataSource;
import java.util.Properties;

/**
 * <P>ClassName: DataConfiguration
 * <P>Description: 持久层配置类
 *
 * @author quec1994
 * @version V1.0, quec1994, 2020/03/02
 **/
@Configuration
@MapperScan(basePackages = {"com.quec1994.mapper"}, sqlSessionFactoryRef = "sqlSessionFactory")
@AllArgsConstructor
public class DataConfiguration {

    public final MySqlProperties mySqlProperties;

    /**
     * 创建 mysql库数据源
     *
     * @return mysql库数据源
     * @author V1.0, queyzh, 2020/3/2 16:56
     **/
    @Primary
    @Bean(name = "mysqlDataSource")
    public DataSource getMysqlDataSource() {
        final HikariDataSource ds = new HikariDataSource();
        ds.setDriverClassName(mySqlProperties.getDriverClassName());
        ds.setConnectionTestQuery(mySqlProperties.getConnectionTestQuery());
        ds.setConnectionTimeout(mySqlProperties.getConnectionTimeoutMs());
        ds.setIdleTimeout(mySqlProperties.getIdleTimeoutMs());
        ds.setMaxLifetime(mySqlProperties.getMaxLifetimeMs());
        ds.setMaximumPoolSize(mySqlProperties.getMaxPoolSize());
        ds.setMinimumIdle(mySqlProperties.getMinIdle());
        ds.setJdbcUrl(mySqlProperties.getJdbcUrl());
        ds.setUsername(mySqlProperties.getUsername());
        ds.setPassword(mySqlProperties.getPassword());
        return ds;
    }

    /**
     * mybatis 配置参数
     *
     * @return 配置参数实例
     */
    @Bean
    public Properties properties() {
        Properties properties = new Properties();
        // 数据库的列名
        properties.setProperty("versionColumn", "my_version");
        // java Bean 字段名
        properties.setProperty("versionField", "myVersion");
        return properties;
    }

    /**
     * 创建插件实例
     *
     * @return 插件实例
     * @author V1.0, queyzh, 2020/3/2 16:56
     **/
    @Bean
    public Interceptor optimisticLocker() {
        OptimisticLockerPlugin optimisticLockerPlugin = new OptimisticLockerPlugin();
        optimisticLockerPlugin.setProperties(properties());
        return optimisticLockerPlugin;
    }

    /**
     * 配置 mybatis 工厂 Bean
     *
     * @return mybatis 工厂 Bean
     * @throws Exception 工厂 Bean 创建出错时抛出
     * @author V1.0, queyzh, 2020/3/2 16:55
     **/
    @Bean(name = "sqlSessionFactory")
    public SqlSessionFactory sqlSessionFactoryBean() throws Exception {
        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
        sqlSessionFactoryBean.setDataSource(getMysqlDataSource());
        sqlSessionFactoryBean.setPlugins(new Interceptor[]{optimisticLocker()});
        // 扫描mapper.xml
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        sqlSessionFactoryBean.setMapperLocations(resolver.getResources("classpath*:/mysql/**/*.xml"));
        return sqlSessionFactoryBean.getObject();
    }

}