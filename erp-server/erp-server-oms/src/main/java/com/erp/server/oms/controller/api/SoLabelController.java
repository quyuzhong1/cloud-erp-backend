package com.erp.server.oms.controller.api;


import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.enums.LogActionEnum;
import com.common.business.dto.base.*;
import org.springframework.web.bind.annotation.RestController;

import com.common.core.controller.BaseController;
import com.erp.server.oms.service.SoLabelService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.oms.dto.SoLabelDTO;

import java.util.List;

/**
 * B2B订单面单表
 *
 * @author zdy
 * @since 2025-02-24
 */
@Slf4j
@RestController
@LogSystemModule("B2B订单面单表")
@RequestMapping("/soLabel")
public class SoLabelController extends BaseController {

    @Resource
    private SoLabelService soLabelService;

    /**
    * 新增
    * @author zdy
    * @date:  2025-02-24
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "B2B订单面单表新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated SoLabelDTO.AddDTO dto) {
        return success(soLabelService.add(dto));
    }

    /**
    * 修改
    * @author zdy
    * @date:  2025-02-24
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "B2B订单面单表修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "oms:soLabel:update",
        serviceClass = SoLabelService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated SoLabelDTO.UpdateDTO dto) {
        soLabelService.update(dto);
        return success();
    }


    /**
     * 批量获取物流单信息
     * @param dto
     * @return
     */
    @PostMapping("/listLogisticsLabel")
    public ApiResult<List<SoLabelDTO.PrintLabelDTO>> listLogisticsLabel(@RequestBody @Validated BaseIdsDTO.IdsDTO dto){
        return success(soLabelService.listLogisticsLabel(dto.getIds()));
    }

    /**
     * 批量预览/打印物流单
     * @param dto
     * @return
     */
    @PostMapping("/printLogisticsLabel")
    public ApiResult<String> printLogisticsLabel(@RequestBody @Validated BaseIdsDTO.IdsDTO dto){
        return success(soLabelService.printLogisticsLabel(dto.getIds()));
    }

    /**
     * 转换物流标签成URL
     * @return
     */
    @PostMapping("/changeLogisticsLabelToUrl")
    public ApiResult changeLogisticsLabelToUrl(){
        soLabelService.changeLogisticsLabelToUrl();
        return success();
    }
}
