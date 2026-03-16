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
import com.erp.server.plm.service.SkuStdRetailPriceService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.vo.PagingVO;
import com.common.business.dto.base.*;
import com.common.business.dto.ApproveDTO;
import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.plm.dto.SkuStdRetailPriceDTO;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;
import com.erp.model.plm.entity.SkuStdRetailPriceEntity;

/**
 * sku标准零售价表
 *
 * @author shukai
 * @since 2026-03-16
 */
@Slf4j
@RestController
@LogSystemModule("sku标准零售价表")
@RequestMapping("/skuStdRetailPrice")
public class SkuStdRetailPriceController extends BaseController {

    @Resource
    private SkuStdRetailPriceService skuStdRetailPriceService;

    /**
    * 新增
    * @author shukai
    * @date:  2026-03-16
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "sku标准零售价表新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated SkuStdRetailPriceDTO.AddDTO dto) {
        return success(skuStdRetailPriceService.add(dto));
    }

    /**
    * 修改
    * @author shukai
    * @date:  2026-03-16
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "sku标准零售价表修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "plm:skuStdRetailPrice:update",
        serviceClass = SkuStdRetailPriceService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated SkuStdRetailPriceDTO.UpdateDTO dto) {
        skuStdRetailPriceService.update(dto);
        return success();
    }


    /**
    * 获取状态统计
    * @return
    */
    @PostMapping("/tabList")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "plm:skuStdRetailPrice:paging",
            tableAlias = ""
    )
    public ApiResult<List<SkuStdRetailPriceDTO.TabListDTO>> tabList(@RequestBody PermissionsDTO dto) {
       return success(skuStdRetailPriceService.tabList(dto));
    }

    /**
    * 列表查询
    * @author shukai
    * @date: 2026-03-16
    * @param dto
    * @return ApiResult<PagingVO<SkuStdRetailPriceDTO.ListDTO>>
    */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "plm:skuStdRetailPrice:paging",
            tableAlias = ""
    )
    public ApiResult<PagingVO<SkuStdRetailPriceDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<SkuStdRetailPriceDTO.PagingParamDTO> dto) {
        return success(skuStdRetailPriceService.paging(dto));
    }


    /**
    * 详情
    * @author shukai
    * @date:  2026-03-16
    * @param id
    * @return ApiResult<SkuStdRetailPriceDTO.ViewDTO>>
    */
    @GetMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "plm:skuStdRetailPrice:view",
            serviceClass = SkuStdRetailPriceService.class,
            keyIdName = "id")
    @LogViewService
    public ApiResult<SkuStdRetailPriceDTO.ViewDTO> view(@RequestParam("id") String id) {
        return success(skuStdRetailPriceService.view(id));
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
            menuCode = "plm:skuStdRetailPrice:export",
            tableAlias = ""
    )
    @LogAction(value = LogActionEnum.EXPORT, desc = "sku标准零售价表导出Excel数据")
    public void exportList(@RequestBody @Validated SkuStdRetailPriceDTO.ExportDTO dto, HttpServletResponse response) {
        skuStdRetailPriceService.exportList(dto, response);
    }


}
