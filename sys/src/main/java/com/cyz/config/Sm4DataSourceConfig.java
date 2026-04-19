package com.cyz.config;

import com.cyz.common.util.Sm4Util;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

@Configuration
public class Sm4DataSourceConfig {

    private static final Logger log = LoggerFactory.getLogger(Sm4DataSourceConfig.class);

    @Value("${sm4.key}")
    private String sm4Key;

    @Value("${spring.datasource.url}")
    private String encUrl;

    @Value("${spring.datasource.username}")
    private String encUsername;

    @Value("${spring.datasource.password}")
    private String encPassword;

    @Value("${spring.datasource.driver-class-name}")
    private String driverClassName;

    @Bean
    @Primary
    public DataSource dataSource() {
        HikariDataSource ds = new HikariDataSource();

        String url = resolveValue(encUrl, sm4Key, "spring.datasource.url");
        String username = resolveValue(encUsername, sm4Key, "spring.datasource.username");
        String password = resolveValue(encPassword, sm4Key, "spring.datasource.password");

        ds.setJdbcUrl(url);
        ds.setUsername(username);
        ds.setPassword(password);
        ds.setDriverClassName(driverClassName);
        return ds;
    }

    /**
     * 判断配置值是否为密文，密文则解密，明文则直接使用并输出警告日志
     */
    private String resolveValue(String value, String key, String configKey) {
        if (Sm4Util.isEncrypted(value, key)) {
            return Sm4Util.decrypt(value, key);
        }
        String encrypted = Sm4Util.encrypt(value, key);
        log.warn("数据库连接信息使用了明文配置，建议替换为密文：{} => {}", configKey, encrypted);
        return value;
    }
}
