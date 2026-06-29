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
import com.erp.server.tms.service.CfgFileParseFileService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.vo.PagingVO;
import com.common.business.dto.base.*;
import com.common.business.dto.ApproveDTO;
import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.tms.dto.CfgFileParseFileDTO;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;
import com.erp.model.tms.entity.CfgFileParseFileEntity;

/**
 * 清洗配置-文件识别规则子表
 *
 * @author jack
 * @since 2026-06-29
 */
@Slf4j
@RestController
@LogSystemModule("清洗配置-文件识别规则子表")
@RequestMapping("/cfgFileParseFile")
public class CfgFileParseFileController extends BaseController {

    @Resource
    private CfgFileParseFileService cfgFileParseFileService;

    /**
    * 新增
    * @author jack
    * @date:  2026-06-29
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "清洗配置-文件识别规则子表新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated CfgFileParseFileDTO.AddDTO dto) {
        return success(cfgFileParseFileService.add(dto));
    }

    /**
    * 修改
    * @author jack
    * @date:  2026-06-29
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "清洗配置-文件识别规则子表修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "tms:cfgFileParseFile:update",
        serviceClass = CfgFileParseFileService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated CfgFileParseFileDTO.UpdateDTO dto) {
        cfgFileParseFileService.update(dto);
        return success();
    }


    /**
    * 获取状态统计
    * @return
    */
    @PostMapping("/tabList")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "tms:cfgFileParseFile:paging",
            tableAlias = ""
    )
    public ApiResult<List<CfgFileParseFileDTO.TabListDTO>> tabList(@RequestBody PermissionsDTO dto) {
       return success(cfgFileParseFileService.tabList(dto));
    }

    /**
    * 列表查询
    * @author jack
    * @date: 2026-06-29
    * @param dto
    * @return ApiResult<PagingVO<CfgFileParseFileDTO.ListDTO>>
    */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "tms:cfgFileParseFile:paging",
            tableAlias = ""
    )
    public ApiResult<PagingVO<CfgFileParseFileDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<CfgFileParseFileDTO.PagingParamDTO> dto) {
        return success(cfgFileParseFileService.paging(dto));
    }


    /**
    * 详情
    * @author jack
    * @date:  2026-06-29
    * @param id
    * @return ApiResult<CfgFileParseFileDTO.ViewDTO>>
    */
    @GetMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "tms:cfgFileParseFile:view",
            serviceClass = CfgFileParseFileService.class,
            keyIdName = "id")
    @LogViewService
    public ApiResult<CfgFileParseFileDTO.ViewDTO> view(@RequestParam("id") String id) {
        return success(cfgFileParseFileService.view(id));
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
            menuCode = "tms:cfgFileParseFile:export",
            tableAlias = ""
    )
    @LogAction(value = LogActionEnum.EXPORT, desc = "清洗配置-文件识别规则子表导出Excel数据")
    public void exportList(@RequestBody @Validated CfgFileParseFileDTO.ExportDTO dto, HttpServletResponse response) {
        cfgFileParseFileService.exportList(dto, response);
    }


}
