package com.cyz.seal.org.interfaces;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cyz.seal.common.result.Result;
import com.cyz.seal.org.application.LegalEntityService;
import com.cyz.seal.org.domain.LegalEntity;
import com.cyz.seal.org.interfaces.dto.LegalEntityCreateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 法人实体 REST（骨架 CRUD，定下 controller 约定）。路径 /api/org/legal-entities。
 */
@RestController
@RequestMapping("/api/org/legal-entities")
@RequiredArgsConstructor
public class LegalEntityController {

    private final LegalEntityService legalEntityService;

    @PostMapping
    public Result<LegalEntity> create(@Valid @RequestBody LegalEntityCreateRequest req) {
        return Result.ok(legalEntityService.create(req));
    }

    @GetMapping("/{id}")
    public Result<LegalEntity> get(@PathVariable Long id) {
        return Result.ok(legalEntityService.getById(id));
    }

    @GetMapping
    public Result<Page<LegalEntity>> page(@RequestParam(defaultValue = "1") long current,
                                          @RequestParam(defaultValue = "10") long size) {
        return Result.ok(legalEntityService.page(new Page<>(current, size)));
    }
}
