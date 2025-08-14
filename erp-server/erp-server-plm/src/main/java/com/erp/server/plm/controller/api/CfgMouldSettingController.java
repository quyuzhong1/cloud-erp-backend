package com.erp.server.plm.controller.api;


import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.plm.dto.CfgMouldSettingDTO;
import com.erp.model.plm.entity.CfgMouldSettingEntity;
import com.erp.server.plm.service.CfgMouldSettingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * 模具配置
 *
 * @author liaohui
 * @since 2024-12-03
 */
@Slf4j
@RestController
@LogSystemModule("模具配置")
@RequestMapping("/cfgMouldSetting")
public class CfgMouldSettingController extends BaseController {

    @Resource
    private CfgMouldSettingService cfgMouldSettingService;

    /**
    * 新增
    * @author liaohui
    * date:  2024-12-03
    * @param dto 参数
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "新增")
    public ApiResult<String> add(@RequestBody @Validated CfgMouldSettingDTO.AddDTO dto) {
        cfgMouldSettingService.add(dto);
        return success();
    }

    /**
     * 模具类型
     * @author liaohui
     * date:  2024-12-03
     * @return ApiResult<String>
     */
    @PostMapping("/mouldList")
    public ApiResult<List<CfgMouldSettingEntity>> mouldList() {
        return success(cfgMouldSettingService.mouldList());
    }

    /**
     * 文档类型
     * @author liaohui
     * date:  2024-12-03
     * @return ApiResult<String>
     */
    @PostMapping("/docList")
    public ApiResult<List<CfgMouldSettingEntity>> docList() {
        return success(cfgMouldSettingService.docList());
    }


}
