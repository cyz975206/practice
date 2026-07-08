package com.cyz.seal;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 印章管理系统启动类。
 *
 * <p>按领域上下文切分包（{@code com.cyz.seal.&lt;context&gt;}），每个上下文内部六边形分层
 *（domain / application / infrastructure / interfaces），详见 docs/backend-architecture.md §4。
 * 多法人行级隔离见 ADR-0002；分布式部署见 ADR-0009。
 */
@SpringBootApplication
@MapperScan("com.cyz.seal.**.infrastructure.persistence.mapper")
public class SealApplication {

    public static void main(String[] args) {
        SpringApplication.run(SealApplication.class, args);
    }
}
