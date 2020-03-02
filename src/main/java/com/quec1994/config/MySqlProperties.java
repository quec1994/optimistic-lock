package com.quec1994.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * <P>ClassName: MySqlProperties
 * <P>Description: mysql 配置参数
 *
 * @author quec1994
 * @version V1.0, 2020/03/02
 **/
@Component
@ConfigurationProperties(prefix = "db.mysql")
@Data
public class MySqlProperties {
    /**
     * 主数据库连接
     */
    private String jdbcUrl;
    private String username;
    private String password;

    /**
     * 数据库公共配置
     */
    private String driverClassName;
    private String connectionTestQuery;
    private Long connectionTimeoutMs;
    private Long idleTimeoutMs;
    private Long maxLifetimeMs;
    private Integer maxPoolSize;
    private Integer minIdle;

}
