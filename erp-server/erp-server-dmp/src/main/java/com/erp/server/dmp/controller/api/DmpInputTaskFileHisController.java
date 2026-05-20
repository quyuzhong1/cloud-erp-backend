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
import com.erp.server.dmp.service.DmpInputTaskFileHisService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.vo.PagingVO;
import com.common.business.dto.base.*;
import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.dmp.dto.DmpInputTaskFileHisDTO;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;
import com.erp.model.dmp.entity.DmpInputTaskFileHisEntity;

/**
 * 拉取任务文件存储归档
 *
 * @author shukai
 * @since 2026-01-26
 */
@Slf4j
@RestController
@LogSystemModule("拉取任务文件存储归档")
@RequestMapping("/dmpInputTaskFileHis")
public class DmpInputTaskFileHisController extends BaseController {

    @Resource
    private DmpInputTaskFileHisService dmpInputTaskFileHisService;

    /**
    * 新增
    * @author shukai
    * @date:  2026-01-26
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "拉取任务文件存储归档新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated DmpInputTaskFileHisDTO.AddDTO dto) {
        return success(dmpInputTaskFileHisService.add(dto));
    }

    /**
    * 修改
    * @author shukai
    * @date:  2026-01-26
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "拉取任务文件存储归档修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "dmp:dmpInputTaskFileHis:update",
        serviceClass = DmpInputTaskFileHisService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated DmpInputTaskFileHisDTO.UpdateDTO dto) {
        dmpInputTaskFileHisService.update(dto);
        return success();
    }


    /**
    * 获取状态统计
    * @return
    */
    @PostMapping("/tabList")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "dmp:dmpInputTaskFileHis:paging",
            tableAlias = ""
    )
    public ApiResult<List<DmpInputTaskFileHisDTO.TabListDTO>> tabList(@RequestBody PermissionsDTO dto) {
       return success(dmpInputTaskFileHisService.tabList(dto));
    }

    /**
    * 列表查询
    * @author shukai
    * @date: 2026-01-26
    * @param dto
    * @return ApiResult<PagingVO<DmpInputTaskFileHisDTO.ListDTO>>
    */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "dmp:dmpInputTaskFileHis:paging",
            tableAlias = ""
    )
    public ApiResult<PagingVO<DmpInputTaskFileHisDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<DmpInputTaskFileHisDTO.PagingParamDTO> dto) {
        return success(dmpInputTaskFileHisService.paging(dto));
    }


    /**
    * 详情
    * @author shukai
    * @date:  2026-01-26
    * @param id
    * @return ApiResult<DmpInputTaskFileHisDTO.ViewDTO>>
    */
    @GetMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "dmp:dmpInputTaskFileHis:view",
            serviceClass = DmpInputTaskFileHisService.class,
            keyIdName = "id")
    @LogViewService
    public ApiResult<DmpInputTaskFileHisDTO.ViewDTO> view(@RequestParam("id") String id) {
        return success(dmpInputTaskFileHisService.view(id));
    }

    /**
    * 导出Excel数据
    * @author shukai
    * @date:  2026-01-26
    * @param dto
    * @param response
    * @return
    */
    @PostMapping("/export")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "dmp:dmpInputTaskFileHis:export",
            tableAlias = ""
    )
    @LogAction(value = LogActionEnum.EXPORT, desc = "拉取任务文件存储归档导出Excel数据")
    public void exportList(@RequestBody @Validated DmpInputTaskFileHisDTO.ExportDTO dto, HttpServletResponse response) {
        dmpInputTaskFileHisService.exportList(dto, response);
    }


}
