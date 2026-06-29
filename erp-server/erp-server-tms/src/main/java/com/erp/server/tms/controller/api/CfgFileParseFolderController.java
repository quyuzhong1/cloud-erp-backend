package com.erp.server.tms.controller.api;


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
import com.erp.server.tms.service.CfgFileParseFolderService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.vo.PagingVO;
import com.common.business.dto.base.*;
import com.common.business.dto.ApproveDTO;
import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.tms.dto.CfgFileParseFolderDTO;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;
import com.erp.model.tms.entity.CfgFileParseFolderEntity;

/**
 * 清洗配置-文件夹映射子表
 *
 * @author jack
 * @since 2026-06-29
 */
@Slf4j
@RestController
@LogSystemModule("清洗配置-文件夹映射子表")
@RequestMapping("/cfgFileParseFolder")
public class CfgFileParseFolderController extends BaseController {

    @Resource
    private CfgFileParseFolderService cfgFileParseFolderService;

    /**
    * 新增
    * @author jack
    * @date:  2026-06-29
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "清洗配置-文件夹映射子表新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated CfgFileParseFolderDTO.AddDTO dto) {
        return success(cfgFileParseFolderService.add(dto));
    }

    /**
    * 修改
    * @author jack
    * @date:  2026-06-29
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "清洗配置-文件夹映射子表修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "tms:cfgFileParseFolder:update",
        serviceClass = CfgFileParseFolderService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated CfgFileParseFolderDTO.UpdateDTO dto) {
        cfgFileParseFolderService.update(dto);
        return success();
    }


    /**
    * 获取状态统计
    * @return
    */
    @PostMapping("/tabList")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "tms:cfgFileParseFolder:paging",
            tableAlias = ""
    )
    public ApiResult<List<CfgFileParseFolderDTO.TabListDTO>> tabList(@RequestBody PermissionsDTO dto) {
       return success(cfgFileParseFolderService.tabList(dto));
    }

    /**
    * 列表查询
    * @author jack
    * @date: 2026-06-29
    * @param dto
    * @return ApiResult<PagingVO<CfgFileParseFolderDTO.ListDTO>>
    */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "tms:cfgFileParseFolder:paging",
            tableAlias = ""
    )
    public ApiResult<PagingVO<CfgFileParseFolderDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<CfgFileParseFolderDTO.PagingParamDTO> dto) {
        return success(cfgFileParseFolderService.paging(dto));
    }


    /**
    * 详情
    * @author jack
    * @date:  2026-06-29
    * @param id
    * @return ApiResult<CfgFileParseFolderDTO.ViewDTO>>
    */
    @GetMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "tms:cfgFileParseFolder:view",
            serviceClass = CfgFileParseFolderService.class,
            keyIdName = "id")
    @LogViewService
    public ApiResult<CfgFileParseFolderDTO.ViewDTO> view(@RequestParam("id") String id) {
        return success(cfgFileParseFolderService.view(id));
    }

    /**
    * 导出Excel数据
    * @author jack
    * @date:  2026-06-29
    * @param dto
    * @param response
    * @return
    */
    @PostMapping("/export")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "tms:cfgFileParseFolder:export",
            tableAlias = ""
    )
    @LogAction(value = LogActionEnum.EXPORT, desc = "清洗配置-文件夹映射子表导出Excel数据")
    public void exportList(@RequestBody @Validated CfgFileParseFolderDTO.ExportDTO dto, HttpServletResponse response) {
        cfgFileParseFolderService.exportList(dto, response);
    }


}
