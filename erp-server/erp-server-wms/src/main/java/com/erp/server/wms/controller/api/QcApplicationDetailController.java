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
import com.erp.server.wms.service.QcApplicationDetailService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.vo.PagingVO;
import com.common.business.dto.base.*;
import com.common.business.dto.ApproveDTO;
import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.wms.dto.QcApplicationDetailDTO;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;
import com.erp.model.wms.entity.QcApplicationDetailEntity;

/**
 * 质检申请单明细表
 *
 * @author will
 * @since 2026-03-20
 */
@Slf4j
@RestController
@LogSystemModule("质检申请单明细表")
@RequestMapping("/qcApplicationDetail")
public class QcApplicationDetailController extends BaseController {

    @Resource
    private QcApplicationDetailService qcApplicationDetailService;

    /**
    * 新增
    * @author will
    * @date:  2026-03-20
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "质检申请单明细表新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated QcApplicationDetailDTO.AddDTO dto) {
        return success(qcApplicationDetailService.add(dto));
    }

    /**
    * 修改
    * @author will
    * @date:  2026-03-20
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "质检申请单明细表修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "wms:qcApplicationDetail:update",
        serviceClass = QcApplicationDetailService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated QcApplicationDetailDTO.UpdateDTO dto) {
        qcApplicationDetailService.update(dto);
        return success();
    }


    /**
    * 获取状态统计
    * @return
    */
    @PostMapping("/tabList")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:qcApplicationDetail:paging",
            tableAlias = ""
    )
    public ApiResult<List<QcApplicationDetailDTO.TabListDTO>> tabList(@RequestBody PermissionsDTO dto) {
       return success(qcApplicationDetailService.tabList(dto));
    }

    /**
    * 列表查询
    * @author will
    * @date: 2026-03-20
    * @param dto
    * @return ApiResult<PagingVO<QcApplicationDetailDTO.ListDTO>>
    */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:qcApplicationDetail:paging",
            tableAlias = ""
    )
    public ApiResult<PagingVO<QcApplicationDetailDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<QcApplicationDetailDTO.PagingParamDTO> dto) {
        return success(qcApplicationDetailService.paging(dto));
    }


    /**
    * 详情
    * @author will
    * @date:  2026-03-20
    * @param id
    * @return ApiResult<QcApplicationDetailDTO.ViewDTO>>
    */
    @GetMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:qcApplicationDetail:view",
            serviceClass = QcApplicationDetailService.class,
            keyIdName = "id")
    @LogViewService
    public ApiResult<QcApplicationDetailDTO.ViewDTO> view(@RequestParam("id") String id) {
        return success(qcApplicationDetailService.view(id));
    }

    /**
    * 导出Excel数据
    * @author will
    * @date:  2026-03-20
    * @param dto
    * @param response
    * @return
    */
    @PostMapping("/export")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:qcApplicationDetail:export",
            tableAlias = ""
    )
    @LogAction(value = LogActionEnum.EXPORT, desc = "质检申请单明细表导出Excel数据")
    public void exportList(@RequestBody @Validated QcApplicationDetailDTO.ExportDTO dto, HttpServletResponse response) {
        qcApplicationDetailService.exportList(dto, response);
    }


}
