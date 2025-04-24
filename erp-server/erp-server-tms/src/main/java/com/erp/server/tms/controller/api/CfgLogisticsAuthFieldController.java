package com.erp.server.tms.controller.api;


import com.common.business.dto.base.BaseResultDTO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.tms.dto.CfgLogisticsAuthFieldDTO;
import com.erp.server.tms.handler.LogisticsRegistry;
import com.erp.server.tms.service.CfgLogisticsAuthFieldService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 物流商管理
 *
 * @author lambda
 * @since 2023-11-09
 */
@Slf4j
@RestController
@LogSystemModule("物流商授权字段配置表")
@RequestMapping("/cfgLogisticsAuthField")
public class CfgLogisticsAuthFieldController extends BaseController {

    @Resource
    private CfgLogisticsAuthFieldService cfgLogisticsAuthFieldService;

    @Resource
    private LogisticsRegistry logisticsRegistry;

    /**
     * 新增
     *
     * @param dto
     * @return ApiResult<String>
     * @author lambda
     * @date: 2023-11-09
     */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "物流商授权字段配置表新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated CfgLogisticsAuthFieldDTO.AddDTO dto) {
        return success(cfgLogisticsAuthFieldService.add(dto));
    }

    /**
     * 修改
     *
     * @param dto
     * @return ApiResult
     * @author lambda
     * @date: 2023-11-09
     */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "物流商授权字段配置表修改")
    public ApiResult<String> update(@RequestBody @Validated CfgLogisticsAuthFieldDTO.UpdateDTO dto) {
        cfgLogisticsAuthFieldService.update(dto);
        return success();
    }

    /**
     * 根据平台获取对应授权字段
     *
     * @return
     */
    @GetMapping("/listByPlatform")
    public ApiResult<List<CfgLogisticsAuthFieldDTO.ListDTO>> listByLogisticsPlatform(@RequestParam(value = "platform") String platform) {
        List<CfgLogisticsAuthFieldDTO.ListDTO> list = cfgLogisticsAuthFieldService.listByLogisticsPlatform(platform);
        return success(list);
    }


}
