package com.erp.server.bi.controller.api;


import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogViewService;
import com.erp.model.bi.dto.BiTargetCategorySettingDTO;
import com.erp.model.bi.dto.BiTargetStaffSettingDTO;
import com.erp.model.bi.dto.BiTargetYearDTO;
import com.erp.server.bi.service.BiTargetNewProductSettingService;
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
import com.erp.model.bi.dto.BiTargetNewProductSettingDTO;
import org.springframework.web.multipart.MultipartFile;

/**
 * 目标管理-新品
 *
 * @author Lambda
 * @since 2023-09-13
 */
@Slf4j
@RestController
@RequestMapping("/biTargetNewProductSetting")
public class BiTargetNewProductSettingController extends BaseController {

    @Resource
    private BiTargetNewProductSettingService biTargetNewProductSettingService;



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
    public ApiResult<PagingVO<BiTargetNewProductSettingDTO.PagingViewDTO>> queryByPage(@RequestBody @Validated PagingDTO<BiTargetYearDTO.PagingParamDTO> dto) {
        PagingVO<BiTargetNewProductSettingDTO.PagingViewDTO> pagingVO = biTargetNewProductSettingService.paging(dto);
        return success(pagingVO);
    }


    /**
    * 新增
    * @author Lambda
    * @date:  2023-09-13
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    public ApiResult<String> add(@RequestBody @Validated BiTargetNewProductSettingDTO.AddDTO dto) {
        return success(biTargetNewProductSettingService.add(dto));
    }

    /**
     * 详情
     *
     * @param id
     * @return
     */
    @GetMapping("/view")
    @LogViewService
    public ApiResult<BiTargetNewProductSettingDTO.ViewDTO> view(@RequestParam(value = "id") String id) {
        BiTargetNewProductSettingDTO.ViewDTO view = biTargetNewProductSettingService.view(id);
        return success(view);
    }

    /**
    * 修改
    * @author Lambda
    * @date:  2023-09-13
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
//        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
//        tableField = "create_user_id",
//        menuCode = "dmp:biTargetNewProductSetting:update",
//        serviceClass = BiTargetNewProductSettingService.class,
//        keyIdName = "id")
    public ApiResult update(@RequestBody @Validated BiTargetNewProductSettingDTO.UpdateDTO dto) {
        biTargetNewProductSettingService.update(dto);
        return success();
    }


    /**
     * 下载模板
     *
     * @return
     */
    @GetMapping("/downloadTemplate")
    public ApiResult downloadTemplate(HttpServletResponse response) {
        biTargetNewProductSettingService.downloadTemplate(response);
        return success();
    }

    /**
     * 导入新品目标设置
     *
     * @return
     */
    @GetMapping("/importFile")
    public ApiResult<BiTargetNewProductSettingDTO.ImportDTO> importFile(@RequestParam(value = "excelFile") MultipartFile excelFile, HttpServletResponse response) {
        BiTargetNewProductSettingDTO.ImportDTO result = biTargetNewProductSettingService.importFile(excelFile, response);
        return success(result);
    }

}
