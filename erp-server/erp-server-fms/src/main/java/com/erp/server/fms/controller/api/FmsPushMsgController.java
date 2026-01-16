package com.erp.server.fms.controller.api;


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
import com.erp.server.fms.service.FmsPushMsgService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.vo.PagingVO;
import com.common.business.dto.base.*;
import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.fms.dto.FmsPushMsgDTO;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;
import com.erp.model.fms.entity.FmsPushMsgEntity;

/**
 * 本地推送消息表
 *
 * @author will
 * @since 2025-12-30
 */
@Slf4j
@RestController
@LogSystemModule("本地推送消息表")
@RequestMapping("/fmsPushMsg")
public class FmsPushMsgController extends BaseController {

    @Resource
    private FmsPushMsgService fmsPushMsgService;

    /**
    * 新增
    * @author will
    * @date:  2025-12-30
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "本地推送消息表新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated FmsPushMsgDTO.AddDTO dto) {
        return success(fmsPushMsgService.add(dto));
    }

    /**
    * 修改
    * @author will
    * @date:  2025-12-30
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "本地推送消息表修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "fms:fmsPushMsg:update",
        serviceClass = FmsPushMsgService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated FmsPushMsgDTO.UpdateDTO dto) {
        fmsPushMsgService.update(dto);
        return success();
    }


    /**
    * 获取状态统计
    * @return
    */
    @PostMapping("/tabList")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "fms:fmsPushMsg:paging",
            tableAlias = ""
    )
    public ApiResult<List<FmsPushMsgDTO.TabListDTO>> tabList(@RequestBody PermissionsDTO dto) {
       return success(fmsPushMsgService.tabList(dto));
    }

    /**
    * 列表查询
    * @author will
    * @date: 2025-12-30
    * @param dto
    * @return ApiResult<PagingVO<FmsPushMsgDTO.ListDTO>>
    */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "fms:fmsPushMsg:paging",
            tableAlias = ""
    )
    public ApiResult<PagingVO<FmsPushMsgDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<FmsPushMsgDTO.PagingParamDTO> dto) {
        return success(fmsPushMsgService.paging(dto));
    }


    /**
    * 详情
    * @author will
    * @date:  2025-12-30
    * @param id
    * @return ApiResult<FmsPushMsgDTO.ViewDTO>>
    */
    @GetMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "fms:fmsPushMsg:view",
            serviceClass = FmsPushMsgService.class,
            keyIdName = "id")
    @LogViewService
    public ApiResult<FmsPushMsgDTO.ViewDTO> view(@RequestParam("id") String id) {
        return success(fmsPushMsgService.view(id));
    }

    /**
    * 导出Excel数据
    * @author will
    * @date:  2025-12-30
    * @param dto
    * @param response
    * @return
    */
    @PostMapping("/export")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "fms:fmsPushMsg:export",
            tableAlias = ""
    )
    @LogAction(value = LogActionEnum.EXPORT, desc = "本地推送消息表导出Excel数据")
    public void exportList(@RequestBody @Validated FmsPushMsgDTO.ExportDTO dto, HttpServletResponse response) {
        fmsPushMsgService.exportList(dto, response);
    }


}
