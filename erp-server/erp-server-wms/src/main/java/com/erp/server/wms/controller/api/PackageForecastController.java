package com.erp.server.wms.controller.api;


import com.erp.model.wms.dto.SoOutstockDTO;
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
import com.erp.server.wms.service.PackageForecastService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.wms.dto.PackageForecastDTO;

import java.util.List;

/**
 * 组包预报表
 *
 * @author Lambda
 * @since 2024-01-26
 */
@Slf4j
@RestController
@LogSystemModule("组包预报表")
@RequestMapping("/packageForecast")
public class PackageForecastController extends BaseController {

    @Resource
    private PackageForecastService packageForecastService;

    /**
     * 获取 tab列表
     *
     * @return
     */
    @PostMapping("/tabList")
    public ApiResult<List<PackageForecastDTO.TabListDTO>> tabList(@RequestBody PermissionsDTO dto) {
        List<PackageForecastDTO.TabListDTO> tabList = packageForecastService.tabList(dto);
        return success(tabList);
    }


    /**
     * 分页
     *
     * @return
     */
    @PostMapping("/paging")
    public ApiResult<List<PackageForecastDTO.PagingViewDTO>> tabList(@RequestBody @Validated PagingDTO<SoOutstockDTO.PagingParamDTO> dto) {
        PagingVO<PackageForecastDTO.PagingViewDTO> pagingVO = packageForecastService.paging(dto);
        return success(pagingVO);
    }



    /**
    * 修改
    * @author Lambda
    * @date:  2024-01-26
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "组包预报表修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "wms:packageForecast:update",
        serviceClass = PackageForecastService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated PackageForecastDTO.UpdateDTO dto) {
        packageForecastService.update(dto);
        return success();
    }



}
