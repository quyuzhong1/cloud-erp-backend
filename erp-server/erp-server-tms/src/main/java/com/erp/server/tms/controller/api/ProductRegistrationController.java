package com.erp.server.tms.controller.api;


import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.vo.PagingVO;
import com.erp.model.tms.dto.LogisticsBillDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import javax.annotation.Resource;
import javax.validation.Valid;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.enums.LogActionEnum;
import com.common.business.dto.base.*;
import org.springframework.web.bind.annotation.RestController;

import com.common.core.controller.BaseController;
import com.erp.server.tms.service.ProductRegistrationService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.tms.dto.ProductRegistrationDTO;

import java.util.List;

/**
 * 产品备案表
 *
 * @author lrp
 * @since 2024-03-14
 */
@Slf4j
@RestController
@LogSystemModule("产品备案表")
@RequestMapping("/productRegistration")
public class ProductRegistrationController extends BaseController {

    @Resource
    private ProductRegistrationService productRegistrationService;

    /**
     * tabList
     * @author lrp
     * @date:  2024-03-14
     * @return ApiResult<String>
     */
    @GetMapping("/tabList")
    public ApiResult<List<ProductRegistrationDTO.TabListDTO>> tabList() {
        return success(productRegistrationService.tabList());
    }

    /**
     * 分页
     * @author lrp
     * @date:  2024-03-14
     * @return ApiResult<String>
     */
    @PostMapping("/paging")
    @WebAdvanceQuery
    public ApiResult<PagingVO<ProductRegistrationDTO.PagingVO>> paging(@RequestBody @Valid PagingDTO<ProductRegistrationDTO.PagingParamDTO> dto) {
        PagingVO<ProductRegistrationDTO.PagingVO> pagingVO = productRegistrationService.paging(dto);
        return success(pagingVO);
    }


    /**
     * 详情
     * @author lrp
     * @date:  2024-03-14
     * @return ApiResult<String>
     */
    @GetMapping("/view")
    public ApiResult<ProductRegistrationDTO.ViewVO> view(@Param("id") String id) {
        return success(productRegistrationService.view(id));
    }


    /**
    * 新增备案
    * @author lrp
    * @date:  2024-03-14
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "产品备案表新增")
    public ApiResult<List<BatchResultDTO>> add(@RequestBody @Validated ProductRegistrationDTO.AddDTO dto) {
        return success(productRegistrationService.add(dto));
    }

}
