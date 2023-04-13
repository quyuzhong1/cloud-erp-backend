package com.erp.server.wms.controller;


import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.wms.dto.PurchaseReturnOrderDTO;
import com.erp.model.wms.dto.WarehouseReceiveDTO;
import org.apache.ibatis.annotations.Param;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.common.core.controller.BaseController;

/**
 * 采购退货单
 * @author LUO_WG
 * @since 2023-04-07
 */
@RestController
@RequestMapping("/purchaseReturnOrder")
public class PurchaseReturnOrderController extends BaseController {
    /**
     * 列表查询
     * @Author Luo_WG
     * @Date 2023/4/6 18:46
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult<com.common.business.vo.PagingVO<com.erp.model.wms.dto.WarehouseReceiveDTO.PagingViewDTO>>
     **/
    @PostMapping("/paging")
    public ApiResult<PagingVO<PurchaseReturnOrderDTO.PagingViewDTO>> paging(@RequestBody @Validated PagingDTO<PurchaseReturnOrderDTO.PagingParamDTO> dto) {
        PagingVO<PurchaseReturnOrderDTO.PagingViewDTO> pagingVO = null;
        return success(pagingVO);
    }

    /**
     * 新增
     * @Author Luo_WG
     * @Date 2023/4/6 18:46
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @PostMapping("/add")
    public ApiResult add(@RequestBody @Validated PagingDTO<PurchaseReturnOrderDTO.AddDTO> dto) {
        Boolean flag = false;
        return flag == true ? success() : failure();
    }

    /**
     * 修改
     * @Author Luo_WG
     * @Date 2023/4/6 18:46
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @PostMapping("/update")
    public ApiResult update(@RequestBody @Validated PagingDTO<PurchaseReturnOrderDTO.UpdateDTO> dto) {
        Boolean flag = false;
        return flag == true ? success() : failure();
    }

    /**
     * 查询详情
     * @Author Luo_WG
     * @Date 2023/4/6 18:57
     * @param id
     * @return com.common.core.controller.vo.ApiResult<com.erp.model.wms.dto.WarehouseReceiveDTO.ViewDTO>
     **/
    @GetMapping("/view")
    public ApiResult<PurchaseReturnOrderDTO.ViewDTO> view(@Param("id") String id) {
        PurchaseReturnOrderDTO.ViewDTO dto = null;
        return success(dto);
    }

    /**
     * 提交
     * @Author Luo_WG
     * @Date 2023/4/6 18:52
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @PostMapping("/submit")
    public ApiResult submit(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        Boolean flag = false;
        return flag == true ? success() : failure();
    }

    /**
     * 新增提交
     * @Author Luo_WG
     * @Date 2023/4/6 18:52
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @PostMapping("/addAndSubmit")
    public ApiResult addAndSubmit(@RequestBody @Validated PurchaseReturnOrderDTO.AddDTO dto) {
        Boolean flag = false;
        return flag == true ? success() : failure();
    }

    /**
     * 修改提交
     * @Author Luo_WG
     * @Date 2023/4/6 18:52
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @PostMapping("/updateAndSubmit")
    public ApiResult updateAndSubmit(@RequestBody @Validated PurchaseReturnOrderDTO.UpdateDTO dto) {
        Boolean flag = false;
        return flag == true ? success() : failure();
    }

    /**
     * 批量审核
     * @Author Luo_WG
     * @Date 2023/4/6 19:06
     * @param baseApproveParamDTO baseApproveParamDTO
     * @return com.common.core.controller.vo.ApiResult
     **/
    @PostMapping("/approve")
    public ApiResult approve(@RequestBody @Validated BaseApproveParamDTO baseApproveParamDTO) {
        Boolean flag = false;
        return flag == true ? success() : failure();
    }

    /**
     * 批量反审核审核
     * @Author Luo_WG
     * @Date 2023/4/6 19:29
     * @param baseApproveParamDTO baseApproveParamDTO
     * @return com.common.core.controller.vo.ApiResult
     **/
    @PostMapping("/disApprove")
    public ApiResult disApprove(@RequestBody @Validated BaseApproveParamDTO baseApproveParamDTO) {
        Boolean flag = false;
        return flag == true ? success() : failure();
    }

    /**
     * 批量撤销
     * @Author Luo_WG
     * @Date 2023/4/6 19:29
     * @param idsDTO idsDTO
     * @return com.common.core.controller.vo.ApiResult
     **/
    @PostMapping("/withDraw")
    public ApiResult withDraw(@RequestBody @Validated BaseIdsDTO.IdsDTO idsDTO) {
        Boolean flag = false;
        return flag == true ? success() : failure();
    }

    /**
     * 批量作废
     * @Author Luo_WG
     * @Date 2023/4/6 19:29
     * @param remarkDTO idsDTO
     * @return com.common.core.controller.vo.ApiResult
     **/
    @PostMapping("/invalid")
    public ApiResult invalid(@RequestBody @Validated BaseIdsDTO.RemarkDTO remarkDTO) {
        Boolean flag = false;
        return flag == true ? success() : failure();
    }

    /**
     * 批量删除
     * @Author Luo_WG
     * @Date 2023/4/6 19:29
     * @param idsDTO idsDTO
     * @return com.common.core.controller.vo.ApiResult
     **/
    @PostMapping("/delete")
    public ApiResult delete(@RequestBody @Validated BaseIdsDTO.IdsDTO idsDTO) {
        Boolean flag = false;
        return flag == true ? success() : failure();
    }

}
