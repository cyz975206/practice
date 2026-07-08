package com.cyz.seal;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 骨架冒烟测试：验证 Spring 上下文能加载（H2 + Flowable + Security + MyBatis-Plus 等自动装配正常）。
 */
@SpringBootTest
class SealApplicationTests {

    @Test
    void contextLoads() {
        // 仅验证上下文能启动；具体用例随各上下文实现补充。
    }
}
