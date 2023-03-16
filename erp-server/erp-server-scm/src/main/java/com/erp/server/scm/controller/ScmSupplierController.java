package com.erp.server.scm.controller;


import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.UpdateStateDTO;
import com.common.business.vo.PagingVO;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.scm.dto.SupplierDTO;
import com.erp.model.scm.dto.SupplierPagingParamDTO;
import com.erp.model.scm.dto.SupplierPagingViemDTO;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 供应商管理
 *
 * @author yl
 * @since 2023-03-15
 */
@RestController
@RequestMapping("/supplier")
public class ScmSupplierController extends BaseController {


    /**
     * 供应商分页列表
     *
     * @return
     */
    @PostMapping("/paging")
    public ApiResult<PagingVO<SupplierPagingViemDTO>> paging(@RequestBody @Validated PagingDTO<SupplierPagingParamDTO> dto) {
        return success();
    }


    /**
     * 保存或者修改供应商
     *
     * @param dto
     * @return
     */
    @PostMapping("/saveOrUpdate")
    public ApiResult saveOrUpdate(@RequestBody @Validated SupplierDTO dto) {
        return success();
    }


    /**
     * 供应商详情
     *
     * @param dto
     * @return
     */
    @PostMapping("/view")
    public ApiResult view(@RequestBody @Validated BaseIdDTO dto) {
        SupplierDTO supplier = new SupplierDTO();
        return success(supplier);
    }


    /**
     * 删除供应商
     * @param dto
     * @return
     */
    @PostMapping("/delete")
    public ApiResult delete(@RequestBody @Validated BaseIdDTO dto) {
        return success();
    }


    /**
     * 启用供应商
     * @param dto
     * @return
     */
    @PostMapping("/updateStatus")
    public ApiResult start(@RequestBody @Validated UpdateStateDTO dto) {
        return success();
    }

    /**
     * 审核
     * @param dto
     * @return
     */
    @PostMapping("/audit")
    public ApiResult audit(@RequestBody @Validated BaseApproveParamDTO dto) {
        return success();
    }







}
