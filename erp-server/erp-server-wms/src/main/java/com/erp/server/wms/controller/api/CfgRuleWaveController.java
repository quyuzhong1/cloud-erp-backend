package com.erp.server.wms.controller.api;


import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.pickingstrategy.CfgRulePickingDTO;
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
import com.erp.server.wms.service.CfgRuleWaveService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.wms.dto.CfgRuleWaveDTO;

/**
 * 波次规则
 *
 * @author will
 * @since 2024-06-20
 */
@Slf4j
@RestController
@LogSystemModule("波次规则")
@RequestMapping("/cfgRuleWave")
public class CfgRuleWaveController extends BaseController {

    @Resource
    private CfgRuleWaveService cfgRuleWaveService;


    @PostMapping("/paging")
    @WebAdvanceQuery
    public ApiResult<PagingVO<CfgRuleWaveDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<CfgRuleWaveDTO.PagingParamDTO> dto) {
        PagingVO<CfgRuleWaveDTO.ListDTO> pagingVO = cfgRuleWaveService.paging(dto);
        return success(pagingVO);
    }

    /**
    * 新增
    * @author will
    * @date:  2024-06-20
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "波次规则新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated CfgRuleWaveDTO.AddDTO dto) {
        return success(cfgRuleWaveService.add(dto));
    }

    /**
    * 修改
    * @author will
    * @date:  2024-06-20
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "波次规则修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "wms:cfgRuleWave:update",
        serviceClass = CfgRuleWaveService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated CfgRuleWaveDTO.UpdateDTO dto) {
        cfgRuleWaveService.update(dto);
        return success();
    }



}
