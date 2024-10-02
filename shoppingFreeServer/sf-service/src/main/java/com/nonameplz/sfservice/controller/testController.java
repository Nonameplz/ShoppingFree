package com.nonameplz.sfservice.controller;

import com.nonameplz.sfcommon.domain.R;
import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "测试接口")
@RestController
@RequestMapping("/testing")
@RequiredArgsConstructor
public class testController {

    @PostMapping
    @Operation(summary = "test knife4j", description = "测试文档接口")
    public R<String> testKnife4j(
            @Parameter(description = "接收一个字符串")
            @RequestParam(name = "name") String name
    ) {
        return R.ok("Hello " + name);
    }

    @GetMapping("/{str}")
    @Operation(summary = "test Get", description = "测试Get方法")
    public R<String> testGet(
            @Parameter(description = "接收一个字符串")
            @PathVariable String str
    ) {

        return R.ok("This is " + str);
    }
}
