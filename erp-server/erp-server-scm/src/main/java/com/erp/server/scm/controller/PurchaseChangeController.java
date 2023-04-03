package com.erp.server.scm.controller;


import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.scm.dto.ListStatusCountDTO;
import com.erp.model.scm.dto.PurchaseChangeDTO;
import com.erp.server.scm.service.PurchaseChangeService;
import org.apache.ibatis.annotations.Param;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 采购变更管理
 *
 * @author will
 * @since 2023-03-16
 */
@RestController
@RequestMapping("/purchaseChange")
public class PurchaseChangeController extends BaseController {

    @Resource
    private PurchaseChangeService purchaseChangeService;

    /**
     * 分页查询
     * @author Will
     * @date: 2023/3/15 16:47
     * @param dto
     * @return ApiResult<PagingVO<List<ScmSalesDemandDTO>>>
     */
    @PostMapping("/paging")
    public ApiResult<PagingVO<PurchaseChangeDTO.ListDTO>> queryByPage(@RequestBody @Validated PagingDTO<PurchaseChangeDTO.SearchParamDTO> dto) {
        PagingVO<PurchaseChangeDTO.ListDTO> pagingVO = purchaseChangeService.paging(dto);
        return success(pagingVO);
    }

    /**
     * 查询数量
     * @author Will
     * @date: 2023/3/15 17:34
     * @return ApiResult
     */
    @GetMapping("/listCount")
    public ApiResult<List<ListStatusCountDTO.PurchaseChangeCountDTO>> listCount() {
        List<ListStatusCountDTO.PurchaseChangeCountDTO> list = purchaseChangeService.listCount();
        return success(list);
    }

    /**
     * 新增
     * @author Will
     * @date: 2023/3/15 17:34
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/add")
    public ApiResult add(@RequestBody @Validated PurchaseChangeDTO.AddDTO dto) {
        purchaseChangeService.add(dto);
        return success();
    }

    /**
     * 修改
     * @author Will
     * @date: 2023/3/15 17:34
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/update")
    public ApiResult update(@RequestBody @Validated PurchaseChangeDTO.UpdateDTO dto) {
        Boolean flag = purchaseChangeService.update(dto);
        return flag == true ? success() : failure();
    }

    /**
     * 新增并提交
     * @author Will
     * @date: 2023/3/15 17:34
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/addAndSubmit")
    public ApiResult addAndSubmit(@RequestBody @Validated PurchaseChangeDTO.AddDTO dto) {
        Boolean flag = purchaseChangeService.addAndSubmit(dto);
        return flag == true ? success() : failure();
    }

    /**
     * 修改并提交
     * @author Will
     * @date: 2023/3/15 17:34
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/updateAndSubmit")
    public ApiResult updateAndSubmit(@RequestBody @Validated PurchaseChangeDTO.UpdateDTO dto) {
        Boolean flag = purchaseChangeService.updateAndSubmit(dto);
        return flag == true ? success() : failure();
    }

    /**
     * 查询详情
     * @author Will
     * @date: 2023/3/15 17:44
     * @param id
     * @return ApiResult<ScmPurchaseChangeDTO>
     */
    @GetMapping("/view")
    public ApiResult<PurchaseChangeDTO.ViewDTO> view(@Param("id") String id) {
        PurchaseChangeDTO.ViewDTO dto = purchaseChangeService.view(id);
        return success(dto);
    }

    /**
     * 取消流程
     * @author Will
     * @date: 2023/3/15 17:59
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/cancelProcess")
    public ApiResult cancelProcess(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        Boolean result = purchaseChangeService.cancelProcess(dto.getIds());
        return result == true ? success() : failure();
    }

    /**
     * 批量作废
     * @author Will
     * @date: 2023/3/15 17:50
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/invalid")
    public ApiResult invalid(@RequestBody @Validated BaseIdsDTO.RemarkDTO dto) {
        Boolean flag = purchaseChangeService.invalid(dto.getIds(),dto.getRemark());
        return flag == true ? success() : failure();
    }


    /**
     * 批量提交
     * @author Will
     * @date: 2023/3/15 17:47
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/submit")
    public ApiResult submit(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        Boolean flag = purchaseChangeService.submit(dto.getIds());
        return flag == true ? success() : failure();
    }

    /**
     * 批量审核
     * @author Will
     * @date: 2023/3/15 17:54
     * @param baseApproveParamDTO
     * @return ApiResult
     */
    @PostMapping("/approve")
    public ApiResult approve(@RequestBody @Validated BaseApproveParamDTO baseApproveParamDTO) {
        purchaseChangeService.approve(baseApproveParamDTO);
        return success();
    }

    /**
     * 导出
     * @author Will
     * @date: 2023/3/15 18:01
     * @param dto
     * @param response
     * @return ApiResult
     */
    @PostMapping(value = "/exportExcel")
    public ApiResult exportExcel(@RequestBody PurchaseChangeDTO.SearchParamDTO dto, HttpServletResponse response) {
        Boolean flag = purchaseChangeService.exportExcel(dto, response);
        return flag == true ? success() : failure();
    }

}
