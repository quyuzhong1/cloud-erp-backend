package com.erp.server.sys.controller.feign;

import com.erp.model.sys.entity.DictGlobalAreaEntity;
import com.erp.server.sys.service.DictGlobalAreaService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 区域
 * @Author Luo_WG
 * @Date 2023/5/30 19:27
 **/
@RestController
@RequestMapping("feign/globalArea")
public class DictGlobalAreaFeignController {

    @Resource
    private DictGlobalAreaService dictGlobalAreaService;

    /**
     * 根据id查询区域
     */
    @PostMapping("/getById")
    public DictGlobalAreaEntity getById(@RequestBody String id) {
        return dictGlobalAreaService.getById(id);
    }
}
