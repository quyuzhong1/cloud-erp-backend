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
import com.erp.server.dmp.service.AdsErpFirstMileInTransitDiffService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.vo.PagingVO;
import com.common.business.dto.base.*;
import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.dmp.dto.AdsErpFirstMileInTransitDiffDTO;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;
import com.erp.model.dmp.entity.doris.AdsErpFirstMileInTransitDiffEntity;

/**
 * 平台在途报告
 *
 * @author Jim
 * @since 2025-11-13
 */
@Slf4j
@RestController
@LogSystemModule("平台在途报告")
@RequestMapping("/adsErpFirstMileInTransitDiff")
public class AdsErpFirstMileInTransitDiffController extends BaseController {

    @Resource
    private AdsErpFirstMileInTransitDiffService adsErpFirstMileInTransitDiffService;

    /**
    * 新增
    * @author Jim
    * @date:  2025-11-13
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "平台在途报告新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated AdsErpFirstMileInTransitDiffDTO.AddDTO dto) {
        return success(adsErpFirstMileInTransitDiffService.add(dto));
    }

    /**
    * 修改
    * @author Jim
    * @date:  2025-11-13
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "平台在途报告修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "dmp:adsErpFirstMileInTransitDiff:update",
        serviceClass = AdsErpFirstMileInTransitDiffService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated AdsErpFirstMileInTransitDiffDTO.UpdateDTO dto) {
        adsErpFirstMileInTransitDiffService.update(dto);
        return success();
    }


    /**
    * 获取状态统计
    * @return
    */
    @PostMapping("/tabList")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "dmp:adsErpFirstMileInTransitDiff:paging",
            tableAlias = ""
    )
    public ApiResult<List<AdsErpFirstMileInTransitDiffDTO.TabListDTO>> tabList(@RequestBody PermissionsDTO dto) {
       return success(adsErpFirstMileInTransitDiffService.tabList(dto));
    }

    /**
    * 列表查询
    * @author Jim
    * @date: 2025-11-13
    * @param dto
    * @return ApiResult<PagingVO<AdsErpFirstMileInTransitDiffDTO.ListDTO>>
    */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "dmp:adsErpFirstMileInTransitDiff:paging",
            tableAlias = ""
    )
    public ApiResult<PagingVO<AdsErpFirstMileInTransitDiffDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<AdsErpFirstMileInTransitDiffDTO.PagingParamDTO> dto) {
        return success(adsErpFirstMileInTransitDiffService.paging(dto));
    }


    /**
    * 详情
    * @author Jim
    * @date:  2025-11-13
    * @param id
    * @return ApiResult<AdsErpFirstMileInTransitDiffDTO.ViewDTO>>
    */
    @GetMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "dmp:adsErpFirstMileInTransitDiff:view",
            serviceClass = AdsErpFirstMileInTransitDiffService.class,
            keyIdName = "id")
    @LogViewService
    public ApiResult<AdsErpFirstMileInTransitDiffDTO.ViewDTO> view(@RequestParam("id") String id) {
        return success(adsErpFirstMileInTransitDiffService.view(id));
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
            menuCode = "dmp:adsErpFirstMileInTransitDiff:export",
            tableAlias = ""
    )
    @LogAction(value = LogActionEnum.EXPORT, desc = "平台在途报告导出Excel数据")
    public void exportList(@RequestBody @Validated AdsErpFirstMileInTransitDiffDTO.ExportDTO dto, HttpServletResponse response) {
        adsErpFirstMileInTransitDiffService.exportList(dto, response);
    }


}
