package com.cyz.config;

import com.cyz.common.util.Sm4Util;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Sm4RedisConfig {

    private static final Logger log = LoggerFactory.getLogger(Sm4RedisConfig.class);

    @Value("${sm4.key}")
    private String sm4Key;

    @Value("${spring.data.redis.host}")
    private String encHost;

    @Value("${spring.data.redis.port:6379}")
    private int port;

    @Value("${spring.data.redis.password:}")
    private String encPassword;

    @Value("${spring.data.redis.database:0}")
    private int database;

    @Bean(destroyMethod = "shutdown")
    public RedissonClient redissonClient() {
        String host = resolveValue(encHost, "spring.data.redis.host");
        String password = (encPassword == null || encPassword.isEmpty()) ? null
                : resolveValue(encPassword, "spring.data.redis.password");

        String address = "redis://" + host + ":" + port;

        Config config = new Config();
        config.useSingleServer()
                .setAddress(address)
                .setDatabase(database)
                .setConnectionMinimumIdleSize(5)
                .setConnectionPoolSize(20)
                .setTimeout(5000)
                .setConnectTimeout(5000);
        if (password != null && !password.isEmpty()) {
            config.useSingleServer().setPassword(password);
        }

        log.info("Redis (Redisson) connected to {}:{}", host, port);
        return Redisson.create(config);
    }

    private String resolveValue(String value, String configKey) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        if (Sm4Util.isEncrypted(value, sm4Key)) {
            return Sm4Util.decrypt(value, sm4Key);
        }
        String encrypted = Sm4Util.encrypt(value, sm4Key);
        log.warn("Redis配置信息使用了明文，建议替换为密文：{} => {}", configKey, encrypted);
        return value;
    }
}
