package com.erp.server.dmp.controller.api;


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
import com.erp.server.dmp.service.AdsErpInventoryDiffKingdeeService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.vo.PagingVO;
import com.common.business.dto.base.*;
import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.dmp.dto.AdsErpInventoryDiffKingdeeDTO;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;
import com.erp.model.dmp.entity.doris.AdsErpInventoryDiffKingdeeEntity;

/**
 * 金蝶库存差异
 *
 * @author Jim
 * @since 2025-11-13
 */
@Slf4j
@RestController
@LogSystemModule("金蝶库存差异")
@RequestMapping("/adsErpInventoryDiffKingdee")
public class AdsErpInventoryDiffKingdeeController extends BaseController {

    @Resource
    private AdsErpInventoryDiffKingdeeService adsErpInventoryDiffKingdeeService;

    /**
    * 新增
    * @author Jim
    * @date:  2025-11-13
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "金蝶库存差异新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated AdsErpInventoryDiffKingdeeDTO.AddDTO dto) {
        return success(adsErpInventoryDiffKingdeeService.add(dto));
    }

    /**
    * 修改
    * @author Jim
    * @date:  2025-11-13
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "金蝶库存差异修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "dmp:adsErpInventoryDiffKingdee:update",
        serviceClass = AdsErpInventoryDiffKingdeeService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated AdsErpInventoryDiffKingdeeDTO.UpdateDTO dto) {
        adsErpInventoryDiffKingdeeService.update(dto);
        return success();
    }


    /**
    * 获取状态统计
    * @return
    */
    @PostMapping("/tabList")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "dmp:adsErpInventoryDiffKingdee:paging",
            tableAlias = ""
    )
    public ApiResult<List<AdsErpInventoryDiffKingdeeDTO.TabListDTO>> tabList(@RequestBody PermissionsDTO dto) {
       return success(adsErpInventoryDiffKingdeeService.tabList(dto));
    }

    /**
    * 列表查询
    * @author Jim
    * @date: 2025-11-13
    * @param dto
    * @return ApiResult<PagingVO<AdsErpInventoryDiffKingdeeDTO.ListDTO>>
    */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "dmp:adsErpInventoryDiffKingdee:paging",
            tableAlias = ""
    )
    public ApiResult<PagingVO<AdsErpInventoryDiffKingdeeDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<AdsErpInventoryDiffKingdeeDTO.PagingParamDTO> dto) {
        return success(adsErpInventoryDiffKingdeeService.paging(dto));
    }


    /**
    * 详情
    * @author Jim
    * @date:  2025-11-13
    * @param id
    * @return ApiResult<AdsErpInventoryDiffKingdeeDTO.ViewDTO>>
    */
    @GetMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "dmp:adsErpInventoryDiffKingdee:view",
            serviceClass = AdsErpInventoryDiffKingdeeService.class,
            keyIdName = "id")
    @LogViewService
    public ApiResult<AdsErpInventoryDiffKingdeeDTO.ViewDTO> view(@RequestParam("id") String id) {
        return success(adsErpInventoryDiffKingdeeService.view(id));
    }

    /**
    * 导出Excel数据
    * @author Jim
    * @date:  2025-11-13
    * @param dto
    * @param response
    * @return
    */
    @PostMapping("/export")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "dmp:adsErpInventoryDiffKingdee:export",
            tableAlias = ""
    )
    @LogAction(value = LogActionEnum.EXPORT, desc = "金蝶库存差异导出Excel数据")
    public void exportList(@RequestBody @Validated AdsErpInventoryDiffKingdeeDTO.ExportDTO dto, HttpServletResponse response) {
        adsErpInventoryDiffKingdeeService.exportList(dto, response);
    }


}
