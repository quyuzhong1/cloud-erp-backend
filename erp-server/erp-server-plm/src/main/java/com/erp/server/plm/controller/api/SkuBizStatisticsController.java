package com.erp.server.plm.controller.api;


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
import com.erp.server.plm.service.SkuBizStatisticsService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.vo.PagingVO;
import com.common.business.dto.base.*;
import com.common.business.dto.ApproveDTO;
import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.plm.dto.SkuBizStatisticsDTO;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;
import com.erp.model.plm.entity.SkuBizStatisticsEntity;

/**
 * sku业务统计表
 *
 * @author shukai
 * @since 2026-03-16
 */
@Slf4j
@RestController
@LogSystemModule("sku业务统计表")
@RequestMapping("/skuBizStatistics")
public class SkuBizStatisticsController extends BaseController {

    @Resource
    private SkuBizStatisticsService skuBizStatisticsService;

    /**
    * 新增
    * @author shukai
    * @date:  2026-03-16
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "sku业务统计表新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated SkuBizStatisticsDTO.AddDTO dto) {
        return success(skuBizStatisticsService.add(dto));
    }

    /**
    * 修改
    * @author shukai
    * @date:  2026-03-16
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "sku业务统计表修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "plm:skuBizStatistics:update",
        serviceClass = SkuBizStatisticsService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated SkuBizStatisticsDTO.UpdateDTO dto) {
        skuBizStatisticsService.update(dto);
        return success();
    }


    /**
    * 获取状态统计
    * @return
    */
    @PostMapping("/tabList")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "plm:skuBizStatistics:paging",
            tableAlias = ""
    )
    public ApiResult<List<SkuBizStatisticsDTO.TabListDTO>> tabList(@RequestBody PermissionsDTO dto) {
       return success(skuBizStatisticsService.tabList(dto));
    }

    /**
    * 列表查询
    * @author shukai
    * @date: 2026-03-16
    * @param dto
    * @return ApiResult<PagingVO<SkuBizStatisticsDTO.ListDTO>>
    */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "plm:skuBizStatistics:paging",
            tableAlias = ""
    )
    public ApiResult<PagingVO<SkuBizStatisticsDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<SkuBizStatisticsDTO.PagingParamDTO> dto) {
        return success(skuBizStatisticsService.paging(dto));
    }


    /**
    * 详情
    * @author shukai
    * @date:  2026-03-16
    * @param id
    * @return ApiResult<SkuBizStatisticsDTO.ViewDTO>>
    */
    @GetMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "plm:skuBizStatistics:view",
            serviceClass = SkuBizStatisticsService.class,
            keyIdName = "id")
    @LogViewService
    public ApiResult<SkuBizStatisticsDTO.ViewDTO> view(@RequestParam("id") String id) {
        return success(skuBizStatisticsService.view(id));
    }

    /**
    * 导出Excel数据
    * @author shukai
    * @date:  2026-03-16
    * @param dto
    * @param response
    * @return
    */
    @PostMapping("/export")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "plm:skuBizStatistics:export",
            tableAlias = ""
    )
    @LogAction(value = LogActionEnum.EXPORT, desc = "sku业务统计表导出Excel数据")
    public void exportList(@RequestBody @Validated SkuBizStatisticsDTO.ExportDTO dto, HttpServletResponse response) {
        skuBizStatisticsService.exportList(dto, response);
    }


}
