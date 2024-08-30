package com.erp.server.mrp.controller.api;


import com.common.business.annotation.DataPermission;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.enums.DataAttributeEnum;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.mrp.dto.LabelInfoDTO;
import com.erp.server.mrp.service.LabelInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 标签信息表
 *
 * @author will
 * @since 2024-08-30
 */
@Slf4j
@RestController
@LogSystemModule("标签信息表")
@RequestMapping("/labelInfo")
public class LabelInfoController extends BaseController {

    @Resource
    private LabelInfoService labelInfoService;

    /**
    * 新增
    * @author will
    * @date:  2024-08-30
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "标签信息表新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated LabelInfoDTO.AddDTO dto) {
        return success(labelInfoService.add(dto));
    }

    /**
    * 修改
    * @author will
    * @date:  2024-08-30
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "标签信息表修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "mrp:labelInfo:update",
        serviceClass = LabelInfoService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated LabelInfoDTO.UpdateDTO dto) {
        labelInfoService.update(dto);
        return success();
    }



}
