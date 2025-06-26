package com.erp.server.srm.controller.api;


import cn.hutool.core.collection.CollUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.srm.dto.CfgSettingDTO;
import com.erp.server.srm.service.CfgSettingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
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
    public ApiResult<Object> update(@RequestBody @Validated CfgSettingDTO.UpdateDTO dto) {
        cfgSettingService.update(dto);
        return success();
    }

    /**
     * 获取供应商订单配置信息
     *
     * @return
     */
    @GetMapping("/view")
    public ApiResult<CfgSettingDTO.ViewDTO> view() {
        CfgSettingDTO.ViewDTO viewDTO = cfgSettingService.view();
        return success(viewDTO);
    }

    /**
     *根据key值查询所有配置信息
     * @author Will
     * @date: 2024/1/17 10:49
     * @param key
     * @return List<ViewDTO>
     */
    @GetMapping("/listByKey")
    public ApiResult<CfgSettingDTO.ViewDTO> listByKey(@RequestParam("key") String key) {
        List<CfgSettingDTO.ViewDTO> list = cfgSettingService.listByKey(key);
        return success(CollUtil.isEmpty(list) ? new CfgSettingDTO.ViewDTO() : list.get(0));
    }
}
