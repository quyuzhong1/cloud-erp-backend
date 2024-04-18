package com.erp.server.wms.controller.api;


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
import com.erp.server.wms.service.WmsDataCompareImportService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.wms.dto.WmsDataCompareImportDTO;

/**
 * 数据对比导入文件信息
 *
 * @author shukai
 * @since 2024-03-20
 */
@Slf4j
@RestController
@LogSystemModule("数据对比导入文件信息")
@RequestMapping("/wmsDataCompareImport")
public class WmsDataCompareImportController extends BaseController {

    @Resource
    private WmsDataCompareImportService wmsDataCompareImportService;

    /**
    * 新增
    * @author shukai
    * @date:  2024-03-20
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "数据对比导入文件信息新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated WmsDataCompareImportDTO.AddDTO dto) {
        return success(wmsDataCompareImportService.add(dto));
    }

    /**
    * 修改
    * @author shukai
    * @date:  2024-03-20
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "数据对比导入文件信息修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "wms:wmsDataCompareImport:update",
        serviceClass = WmsDataCompareImportService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated WmsDataCompareImportDTO.UpdateDTO dto) {
        wmsDataCompareImportService.update(dto);
        return success();
    }



}
