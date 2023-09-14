package com.erp.server.bi.controller.api;


import com.common.business.annotation.DataPermission;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogViewService;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.bi.dto.BiTargetShopSettingDTO;
import com.erp.model.bi.dto.BiTargetSkuSettingDTO;
import com.erp.model.bi.dto.BiTargetYearDTO;
import com.erp.server.bi.service.BiTargetSkuSettingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * sku 目标设置表
 *
 * @author Lambda
 * @since 2023-09-13
 */
@Slf4j
@RestController
@RequestMapping("/biTargetSkuSetting")
public class BiTargetSkuSettingController extends BaseController {

    @Resource
    private BiTargetSkuSettingService biTargetSkuSettingService;



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
    public ApiResult<PagingVO<BiTargetSkuSettingDTO.PagingViewDTO>> queryByPage(@RequestBody @Validated PagingDTO<BiTargetYearDTO.PagingParamDTO> dto) {
        PagingVO<BiTargetSkuSettingDTO.PagingViewDTO> pagingVO = biTargetSkuSettingService.paging(dto);
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
    @LogAction(value = LogActionEnum.INSERT, desc = "SKU目标设置添加")
    public ApiResult<String> add(@RequestBody @Validated BiTargetSkuSettingDTO.AddDTO dto) {
        return success(biTargetSkuSettingService.add(dto));
    }

    /**
     * 详情
     *
     * @param id
     * @return
     */
    @GetMapping("/view")
    @LogViewService
    public ApiResult<BiTargetSkuSettingDTO.ViewDTO> view(@RequestParam(value = "id") String id) {
        BiTargetSkuSettingDTO.ViewDTO view = biTargetSkuSettingService.view(id);
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
//        menuCode = "dmp:biTargetSkuSetting:update",
//        serviceClass = BiTargetSkuSettingService.class,
//        keyIdName = "id")
    public ApiResult update(@RequestBody @Validated BiTargetSkuSettingDTO.UpdateDTO dto) {
        biTargetSkuSettingService.update(dto);
        return success();
    }



}
