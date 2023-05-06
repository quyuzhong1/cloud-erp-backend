package com.erp.server.scm.controller.api;


import com.common.business.dto.base.*;
import com.common.business.vo.PagingVO;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.scm.dto.SupplierPhaseDTO;
import com.erp.server.scm.service.SupplierPhaseService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

/**
 * 供应商阶段管理
 *
 * @author admin
 * @since 2023-03-15
 */
@RestController
@RequestMapping("/supplier/phase")
public class SupplierPhaseController extends BaseController {


    @Resource
    private SupplierPhaseService supplierPhaseService;


    /**
     * 供应商阶段分页列表
     *
     * @return
     */
    @PostMapping("/paging")
    public ApiResult<PagingVO<SupplierPhaseDTO.PagingViewDTO>> paging(@RequestBody @Validated PagingDTO<SupplierPhaseDTO.PagingParamDTO> dto) {
        PagingVO<SupplierPhaseDTO.PagingViewDTO> pagingVO = supplierPhaseService.paging(dto);
        return success(pagingVO);
    }

    /**
     * 添加供应商阶段
     *
     * @param dto
     * @return
     */
    @PostMapping("/add")
    public ApiResult add(@RequestBody @Validated SupplierPhaseDTO.AddDTO dto) {
        String id = supplierPhaseService.add(dto);
        return StringUtils.isNotBlank(id) ? success() : failure();
    }

    /**
     * 变更阶段的时候 获取对应的阶段列表
     *
     * @param
     * @return com.common.core.controller.vo.ApiResult<java.util.List < com.common.business.dto.base.BaseDropDownDTO.CommonDTO>>
     * @author yl
     * @date 2023-03-31 14:20
     */
    @PostMapping("/listByChange")
    public ApiResult<List<BaseDropDownDTO.CommonDTO>> listByChange(@RequestBody @Validated SupplierPhaseDTO.ListDTO dto) {
        List<BaseDropDownDTO.CommonDTO> list = supplierPhaseService.listByChange(dto);
        return success(list);
    }

    /**
     * 修改供应商阶段
     *
     * @param dto
     * @return
     */
    @PostMapping("/update")
    public ApiResult update(@RequestBody @Validated SupplierPhaseDTO.UpdateDTO dto) {
        String id = supplierPhaseService.updateSupplierPhase(dto);
        return StringUtils.isNotBlank(id) ? success() : failure();
    }

    /**
     * 修改并审核
     *
     * @param dto
     * @return
     */
    @PostMapping("/updateAndSubmit")
    public ApiResult updateAndSubmit(@RequestBody @Validated SupplierPhaseDTO.UpdateDTO dto) {
        Boolean result = supplierPhaseService.updateAndSubmit(dto);
        return result == true ? success() : failure();
    }

    /**
     * 提交并审核
     *
     * @param dto
     * @return
     */
    @PostMapping("/addAndSubmit")
    public ApiResult addAndSubmit(@RequestBody @Validated SupplierPhaseDTO.AddDTO dto) {
        Boolean result = supplierPhaseService.addAndSubmit(dto);
        return result == true ? success() : failure();
    }

    /**
     * 供应商阶段提交审核
     *
     * @param dto
     * @return
     */
    @PostMapping("/submit")
    public ApiResult submit(@RequestBody @Valid BaseIdsDTO.IdsDTO dto) {
        Boolean result = supplierPhaseService.submit(dto.getIds());
        return result == true ? success() : failure();
    }

    /**
     * 供应商阶段 详情
     *
     * @param dto
     * @return
     */
    @PostMapping("/view")
    public ApiResult<SupplierPhaseDTO.UpdateDTO> view(@RequestBody @Validated BaseIdDTO dto) {
        SupplierPhaseDTO.UpdateDTO supplierPhase = supplierPhaseService.view(dto.getId());
        return success(supplierPhase);
    }


    /**
     * 供应商阶段审核
     *
     * @param dto
     * @return
     */
    @PostMapping("/approve")
    public ApiResult view(@RequestBody @Validated BaseApproveParamDTO dto) {
        Boolean result = supplierPhaseService.approve(dto);
        return result == true ? success() : failure();
    }


    /**
     * 取消流程
     *
     * @param dto
     * @return com.common.core.controller.vo.ApiResult
     * @author yl
     * @date 2023-03-23 17:57
     */
    @PostMapping("/cancelProcess")
    public ApiResult cancelProcess(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        Boolean result = supplierPhaseService.cancelProcess(dto.getIds());
        return result == true ? success() : failure();
    }


    /**
     * 删除供应商阶段
     *
     * @param dto
     * @return
     */
    @PostMapping("/delete")
    public ApiResult delete(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        Boolean result = supplierPhaseService.deleteByIds(dto.getIds());
        return result == true ? success() : failure();
    }

}
