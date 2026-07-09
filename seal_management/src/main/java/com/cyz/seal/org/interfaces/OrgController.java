package com.cyz.seal.org.interfaces;

import com.cyz.seal.common.result.Result;
import com.cyz.seal.org.application.OrgService;
import com.cyz.seal.org.domain.Org;
import com.cyz.seal.org.interfaces.dto.OrgCreateRequest;
import com.cyz.seal.org.interfaces.dto.OrgUpdateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 部门 / 机构 REST（本法人实体范围）。路径 /api/org/orgs。
 */
@RestController
@RequestMapping("/api/org/orgs")
@RequiredArgsConstructor
public class OrgController {

    private final OrgService orgService;

    @PostMapping
    public Result<Org> create(@Valid @RequestBody OrgCreateRequest req) {
        return Result.ok(orgService.create(req));
    }

    @PutMapping("/{id}")
    public Result<Org> update(@PathVariable Long id, @Valid @RequestBody OrgUpdateRequest req) {
        return Result.ok(orgService.update(id, req));
    }

    @GetMapping
    public Result<List<Org>> list() {
        return Result.ok(orgService.listTree());
    }

    @GetMapping("/{id}")
    public Result<Org> get(@PathVariable Long id) {
        return Result.ok(orgService.getById(id));
    }

    @PutMapping("/{id}/disable")
    public Result<Void> disable(@PathVariable Long id) {
        orgService.disable(id);
        return Result.ok();
    }
}
