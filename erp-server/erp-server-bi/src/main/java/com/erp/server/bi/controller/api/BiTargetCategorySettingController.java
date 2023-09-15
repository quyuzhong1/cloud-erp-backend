package com.erp.server.bi.controller.api;


import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogViewService;
import com.erp.model.bi.dto.BiTargetSkuSettingDTO;
import com.erp.model.bi.dto.BiTargetYearDTO;
import com.erp.server.bi.service.BiTargetCategorySettingService;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RestController;

import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.bi.dto.BiTargetCategorySettingDTO;

/**
 * 分类 目标设置表
 *
 * @author Lambda
 * @since 2023-09-13
 */
@Slf4j
@RestController
@RequestMapping("/biTargetCategorySetting")
public class BiTargetCategorySettingController extends BaseController {

    @Resource
    private BiTargetCategorySettingService biTargetCategorySettingService;



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
    public ApiResult<PagingVO<BiTargetCategorySettingDTO.PagingViewDTO>> queryByPage(@RequestBody @Validated PagingDTO<BiTargetYearDTO.PagingParamDTO> dto) {
        PagingVO<BiTargetCategorySettingDTO.PagingViewDTO> pagingVO = biTargetCategorySettingService.paging(dto);
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
    public ApiResult<String> add(@RequestBody @Validated BiTargetCategorySettingDTO.AddDTO dto) {
        return success(biTargetCategorySettingService.add(dto));
    }


    /**
     * 详情
     *
     * @param id
     * @return
     */
    @GetMapping("/view")
    @LogViewService
    public ApiResult<BiTargetCategorySettingDTO.ViewDTO> view(@RequestParam(value = "id") String id) {
        BiTargetCategorySettingDTO.ViewDTO view = biTargetCategorySettingService.view(id);
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
//        menuCode = "dmp:biTargetCategorySetting:update",
//        serviceClass = BiTargetCategorySettingService.class,
//        keyIdName = "id")
    public ApiResult update(@RequestBody @Validated BiTargetCategorySettingDTO.UpdateDTO dto) {
        biTargetCategorySettingService.update(dto);
        return success();
    }



}
