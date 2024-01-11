package com.erp.server.srm.controller.api;


import com.erp.model.srm.vo.ConfigVO;
import com.erp.model.srm.vo.SupplierConfigVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.enums.LogActionEnum;
import com.common.business.dto.base.*;
import org.springframework.web.bind.annotation.RestController;

import com.common.core.controller.BaseController;
import com.erp.server.srm.service.CfgSettingService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.srm.dto.CfgSettingDTO;

import java.util.List;

/**
 * 系统配置管理
 *
 * @author zdy
 * @since 2024-01-10
 */
@Slf4j
@RestController
@LogSystemModule("系统配置管理")
@RequestMapping("/cfgSetting")
public class CfgSettingController extends BaseController {

    @Resource
    private CfgSettingService cfgSettingService;

    /**
     * 新增
     *
     * @param dto
     * @return ApiResult<String>
     * @author zdy
     * @date: 2024-01-10
     */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "系统配置管理新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated CfgSettingDTO.AddDTO dto) {
        cfgSettingService.add(dto);
        return success();
    }

    /**
     * 修改
     *
     * @param dto
     * @return ApiResult
     * @author zdy
     * @date: 2024-01-10
     */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "系统配置管理修改")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "srm:cfgSetting:update",
            serviceClass = CfgSettingService.class,
            keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated CfgSettingDTO.UpdateDTO dto) {
        cfgSettingService.update(dto);
        return success();
    }

    /**
     * 获取供应商订单配置信息
     *
     * @return
     */
    @GetMapping("/getConfig")
    public ApiResult<List<ConfigVO>> getConfig() {
        List<ConfigVO> configVOList =cfgSettingService.getConfig();
        return success(configVOList);
    }
}
