package com.erp.server.wms.controller.api;


import com.common.business.vo.PagingVO;
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
    @GetMapping("/tabList")
    public ApiResult<List<PackageForecastDTO.TabListDTO>> tabList() {
        List<PackageForecastDTO.TabListDTO> tabList = packageForecastService.tabList();
        return success(tabList);
    }


    /**
     * 分页
     *
     * @return
     */
    @PostMapping("/paging")
    public ApiResult<PagingVO<PackageForecastDTO.PagingViewDTO>> paging(@RequestBody @Validated PagingDTO<PackageForecastDTO.PagingParamDTO> dto) {
        PagingVO<PackageForecastDTO.PagingViewDTO> pagingVO = packageForecastService.paging(dto);
        return success(pagingVO);
    }


    /**
     * 详情
     */
    @GetMapping("/view")
    public ApiResult<PackageForecastDTO.ViewDTO> view(@RequestParam("id") String id) {
        PackageForecastDTO.ViewDTO viewDTO = packageForecastService.view(id);
        return success(viewDTO);
    }






    /**
    * 修改
    * @author Lambda
    * @date:  2024-01-26
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    public ApiResult update(@RequestBody @Validated PackageForecastDTO.UpdateDTO dto) {
        Boolean result = packageForecastService.update(dto);
        return result ? success() : failure();
    }



}
