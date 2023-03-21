package com.erp.server.scm.controller;


import com.common.business.dto.base.BaseIdDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.scm.dto.SupplierVisitDTO;
import com.erp.server.scm.service.SupplierVisitService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 供应商管理
 *
 * @author admin
 * @since 2023-03-15
 */
@RestController
@RequestMapping("/supplier/visit")
public class SupplierVisitController extends BaseController {


    @Resource
    private SupplierVisitService supplierVisitService;


    /**
     * 供应商 拜访分页列表
     *
     * @return
     */
    @PostMapping("/paging")
    public ApiResult<PagingVO<SupplierVisitDTO.PagingViewDTO>> paging(@RequestBody @Validated PagingDTO<BaseIdDTO> dto) {
        PagingVO<SupplierVisitDTO.PagingViewDTO> pagingVO = supplierVisitService.paging(dto);
        return success(pagingVO);
    }

    /**
     * 保存或者修改供应商拜访
     *
     * @param dto
     * @return
     */
    @PostMapping("/add")
    public ApiResult add(@RequestBody @Validated SupplierVisitDTO.AddDTO dto) {
       Boolean  result= supplierVisitService.add(dto);
        return result==true?success():failure();
    }

}
