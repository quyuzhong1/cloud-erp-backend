package com.erp.server.dmp.controller.feign;

import com.erp.model.dmp.dto.CfgSettingDTO;
import com.erp.model.dmp.enums.SettingEnum;
import com.erp.server.dmp.service.CfgSettingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
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

    /**
     * 查询仓库是否支持推送仓位
     * @author will
     * @date 2024/8/15 18:24
     * @param warehouseIdList
     * @return CfgSettingDTO.WarehouseLocationSettingDTO
     */
    @PostMapping ("/cfgSetting/isPushKingdeeWarehouseLocation")
    public List<CfgSettingDTO.WarehouseLocationSettingDTO> isPushKingdeeWarehouseLocation(@RequestBody List<String> warehouseIdList) {
        return cfgSettingService.isPushKingdeeWarehouseLocation(warehouseIdList);
    }
}
