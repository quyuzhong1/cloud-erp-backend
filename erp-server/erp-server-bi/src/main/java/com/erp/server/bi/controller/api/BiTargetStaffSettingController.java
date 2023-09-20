package com.erp.server.bi.controller.api;


import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.enums.LogActionEnum;
import com.erp.model.bi.dto.BiTargetYearDTO;
import com.erp.server.bi.service.BiTargetStaffSettingService;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RestController;

import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.bi.dto.BiTargetStaffSettingDTO;
import org.springframework.web.multipart.MultipartFile;

/**
 * 目标管理-人员
 *
 * @author Lambda
 * @since 2023-09-13
 */
@Slf4j
@RestController
@LogSystemModule("目标管理")
@RequestMapping("/biTargetStaffSetting")
public class BiTargetStaffSettingController extends BaseController {

    @Resource
    private BiTargetStaffSettingService biTargetStaffSettingService;


    /**
     * 分页
     *
     * @param dto
     * @return ApiResult<String>
     * @author Lambda
     * @date: 2023-09-13
     */
    @PostMapping("/paging")
//    @DataPermission(operationType = DataAttributeEnum.LIST,
//            tableField = "create_user_id",
//            menuCode = "bi:biTargetStaffSetting:paging",
//            tableAlias = ""
//    )
    public ApiResult<PagingVO<BiTargetStaffSettingDTO.PagingViewDTO>> queryByPage(@RequestBody @Validated PagingDTO<BiTargetYearDTO.PagingParamDTO> dto) {
        PagingVO<BiTargetStaffSettingDTO.PagingViewDTO> pagingVO = biTargetStaffSettingService.paging(dto);
        return success(pagingVO);
    }


    /**
     * 分页
     *
     * @param dto
     * @return ApiResult<String>
     * @author Lambda
     * @date: 2023-09-13
     */
    @PostMapping("/pagingTotal")
//    @DataPermission(operationType = DataAttributeEnum.LIST,
//            tableField = "create_user_id",
//            menuCode = "bi:biTargetStaffSetting:paging",
//            tableAlias = ""
//    )
    public ApiResult<BiTargetYearDTO.PagingTotalDTO> pagingTotal(@RequestBody @Validated PagingDTO<BiTargetYearDTO.PagingParamDTO> dto) {
        return success(null);
    }
    /**
     * 新增
     *
     * @param dto
     * @return ApiResult<String>
     * @author Lambda
     * @date: 2023-09-13
     */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "人员目标设置添加")
    public ApiResult<String> add(@RequestBody @Validated BiTargetStaffSettingDTO.AddDTO dto) {
        return success(biTargetStaffSettingService.add(dto));
    }

    /**
     * 详情
     *
     * @param id
     * @return
     */
    @GetMapping("/view")
    @LogViewService
    public ApiResult<BiTargetStaffSettingDTO.ViewDTO> view(@RequestParam(value = "id") String id) {
        BiTargetStaffSettingDTO.ViewDTO view = biTargetStaffSettingService.view(id);
        return success(view);
    }

    /**
     * 修改
     *
     * @param dto
     * @return ApiResult
     * @author Lambda
     * @date: 2023-09-13
     */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "人员目标设置修改")
//        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
//        tableField = "create_user_id",
//        menuCode = "dmp:biTargetStaffSetting:update",
//        serviceClass = BiTargetStaffSettingService.class,
//        keyIdName = "id")
    public ApiResult update(@RequestBody @Validated BiTargetStaffSettingDTO.UpdateDTO dto) {
        biTargetStaffSettingService.update(dto);
        return success();
    }

    /**
     * 下载模板
     *
     * @return
     */
    @GetMapping("/downloadTemplate")
    public ApiResult downloadTemplate(HttpServletResponse response) {
        biTargetStaffSettingService.downloadTemplate(response);
        return success();
    }

    /**
     * 导入人员目标设置
     *
     * @return
     */
    @GetMapping("/importFile")
    @LogAction(value = LogActionEnum.IMPORT, desc = "人员目标设置导入")
    public ApiResult<BiTargetStaffSettingDTO.ImportDTO> importFile(@RequestParam(value = "excelFile") MultipartFile excelFile, HttpServletResponse response) {
        BiTargetStaffSettingDTO.ImportDTO result = biTargetStaffSettingService.importFile(excelFile, response);
        return success(result);
    }


    /**
     * 分页列表修改
     *
     * @return
     */
    @PostMapping("/listUpdate")
    @LogAction(value = LogActionEnum.UPDATE, desc = "人员目标列表修改")
    public ApiResult listUpdate(@RequestBody BiTargetStaffSettingDTO.RemoveDTO dto) {
        Boolean result = biTargetStaffSettingService.delete(dto);
        return result ? success() : failure();
    }

    /**
     * 分页列表删除
     *
     * @return
     */
    @PostMapping("/remove")
    @LogAction(value = LogActionEnum.DELETE, desc = "人员目标设置删除")
    public ApiResult remove(@RequestBody BiTargetStaffSettingDTO.RemoveDTO dto) {
        Boolean result = biTargetStaffSettingService.delete(dto);
        return result ? success() : failure();
    }


}
