package com.erp.server.oms.controller.api;


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
import com.erp.server.oms.service.KolSocialMediaService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.oms.dto.KolSocialMediaDTO;
import java.util.List;

/**
 * 达人社媒数据表
 *
 * @author wuhaotian
 * @since 2025-12-01
 */
@Slf4j
@RestController
@LogSystemModule("达人社媒数据表")
@RequestMapping("/kolSocialMedia")
public class KolSocialMediaController extends BaseController {

    @Resource
    private KolSocialMediaService kolSocialMediaService;

    /**
    * 新增
    * @author wuhaotian
    * @date:  2025-12-01
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "达人社媒数据表新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated KolSocialMediaDTO.AddDTO dto) {
        return success(kolSocialMediaService.add(dto));
    }

    /**
    * 批量新增
    * @author wuhaotian
    * @date:  2025-12-03
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/batchAdd")
    @LogAction(value = LogActionEnum.CUSTOM_BATCH_INSERT, desc = "达人社媒数据表批量新增")
    public ApiResult<?> batchAdd(@RequestBody @Validated KolSocialMediaDTO.BatchAddDTO dto) {
        List<BatchResultDTO> resultDTOS = kolSocialMediaService.batchAdd(dto);
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
    * 修改
    * @author wuhaotian
    * @date:  2025-12-01
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "达人社媒数据表修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "oms:kolSocialMedia:update",
        serviceClass = KolSocialMediaService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated KolSocialMediaDTO.UpdateDTO dto) {
        kolSocialMediaService.update(dto);
        return success();
    }



}
