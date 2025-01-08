package com.erp.server.sys.controller.feign;

import com.common.core.controller.BaseController;
import com.erp.model.sys.entity.SysPostEntity;
import com.erp.model.sys.entity.SysPostUserEntity;
import com.erp.server.sys.service.CfgCountryPartitionService;
import com.erp.server.sys.service.DictPartitionService;
import com.erp.server.sys.service.SysPostService;
import com.erp.server.sys.service.SysPostUserService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("feign/partition")
public class SysPartitionFeignContriller extends BaseController {
    @Resource
    private CfgCountryPartitionService cfgCountryPartitionService;
    /**
     * 通过国家获取分区
     */
    @PostMapping("/getPartitionByCountry")
    public String getPartitionByCountry(@RequestBody String country){
        return cfgCountryPartitionService.getPartitionByCountry(country);
    }
}
