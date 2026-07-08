package com.cyz.seal.common.health;

import com.cyz.seal.common.result.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 系统探活端点（骨架，证明 Web → Security → Result 链路通）。
 */
@RestController
@RequestMapping("/api/system")
public class HealthController {

    @GetMapping("/ping")
    public Result<String> ping() {
        return Result.ok("pong");
    }
}
