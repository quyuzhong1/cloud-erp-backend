package com.erp.server.dmp.controller.feign;

import com.erp.model.dmp.dto.PlatformTaskDTO;
import com.erp.model.dmp.dto.ThirdWarehouseTaskDTO;
import com.erp.model.dmp.entity.CfgSettingEntity;
import com.erp.model.dmp.entity.DmpPullTaskEntity;
import com.erp.model.dmp.enums.SettingEnum;
import com.erp.server.dmp.service.CfgSettingService;
import com.erp.server.dmp.service.DmpPullTaskService;
import com.erp.server.dmp.service.PlatformApiTaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * 查询Setting
 *
 * @Author Jim
 * @Date 2023/12/01
 **/

@Slf4j
@RestController
@RequestMapping("feign/dmp")
public class DmpSettingFeignController {
    @Resource
    private CfgSettingService cfgSettingService;

    @PostMapping ("/cfgSetting/list")
    public Map<SettingEnum, String> list(@RequestBody String type) {
        return cfgSettingService.getMap(type);
    }
}
