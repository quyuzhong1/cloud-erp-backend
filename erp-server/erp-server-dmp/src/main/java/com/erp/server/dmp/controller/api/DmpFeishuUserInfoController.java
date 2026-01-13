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
import com.erp.server.dmp.service.DmpFeishuUserInfoService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.vo.PagingVO;
import com.common.business.dto.base.*;
import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.dmp.dto.DmpFeishuUserInfoDTO;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;
import com.erp.model.dmp.entity.DmpFeishuUserInfoEntity;

/**
 * DMP飞书用户信息
 *
 * @author jack
 * @since 2026-01-13
 */
@Slf4j
@RestController
@LogSystemModule("DMP飞书用户信息")
@RequestMapping("/dmpFeishuUserInfo")
public class DmpFeishuUserInfoController extends BaseController {

    @Resource
    private DmpFeishuUserInfoService dmpFeishuUserInfoService;

    /**
    * 新增
    * @author jack
    * @date:  2026-01-13
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "DMP飞书用户信息新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated DmpFeishuUserInfoDTO.AddDTO dto) {
        return success(dmpFeishuUserInfoService.add(dto));
    }

    /**
    * 修改
    * @author jack
    * @date:  2026-01-13
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "DMP飞书用户信息修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "dmp:dmpFeishuUserInfo:update",
        serviceClass = DmpFeishuUserInfoService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated DmpFeishuUserInfoDTO.UpdateDTO dto) {
        dmpFeishuUserInfoService.update(dto);
        return success();
    }


    /**
    * 获取状态统计
    * @return
    */
    @PostMapping("/tabList")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "dmp:dmpFeishuUserInfo:paging",
            tableAlias = ""
    )
    public ApiResult<List<DmpFeishuUserInfoDTO.TabListDTO>> tabList(@RequestBody PermissionsDTO dto) {
       return success(dmpFeishuUserInfoService.tabList(dto));
    }

    /**
    * 列表查询
    * @author jack
    * @date: 2026-01-13
    * @param dto
    * @return ApiResult<PagingVO<DmpFeishuUserInfoDTO.ListDTO>>
    */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "dmp:dmpFeishuUserInfo:paging",
            tableAlias = ""
    )
    public ApiResult<PagingVO<DmpFeishuUserInfoDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<DmpFeishuUserInfoDTO.PagingParamDTO> dto) {
        return success(dmpFeishuUserInfoService.paging(dto));
    }


    /**
    * 详情
    * @author jack
    * @date:  2026-01-13
    * @param id
    * @return ApiResult<DmpFeishuUserInfoDTO.ViewDTO>>
    */
    @GetMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "dmp:dmpFeishuUserInfo:view",
            serviceClass = DmpFeishuUserInfoService.class,
            keyIdName = "id")
    @LogViewService
    public ApiResult<DmpFeishuUserInfoDTO.ViewDTO> view(@RequestParam("id") String id) {
        return success(dmpFeishuUserInfoService.view(id));
    }

    /**
    * 导出Excel数据
    * @author jack
    * @date:  2026-01-13
    * @param dto
    * @param response
    * @return
    */
    @PostMapping("/export")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "dmp:dmpFeishuUserInfo:export",
            tableAlias = ""
    )
    @LogAction(value = LogActionEnum.EXPORT, desc = "DMP飞书用户信息导出Excel数据")
    public void exportList(@RequestBody @Validated DmpFeishuUserInfoDTO.ExportDTO dto, HttpServletResponse response) {
        dmpFeishuUserInfoService.exportList(dto, response);
    }


}
