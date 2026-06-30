package com.erp.server.dmp.controller.feign;

import com.erp.model.dmp.entity.DmpBasicSystemEntity;
import com.erp.server.dmp.service.DmpBasicSystemService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * DMP 基础系统 Feign 控制器。
 *
 * @author jack
 * @since 2026-06-29
 */
@RestController
@RequestMapping("/feign/dmp/basicSystem")
public class DmpBasicSystemFeignController {

    @Resource
    private DmpBasicSystemService dmpBasicSystemService;

    /**
     * 根据 ID 查询基础系统。
     *
     * @param id 基础系统 ID
     * @return 基础系统
     */
    @GetMapping("/getById")
    public DmpBasicSystemEntity getById(@RequestParam("id") String id) {
        return dmpBasicSystemService.getById(id);
    }
}
