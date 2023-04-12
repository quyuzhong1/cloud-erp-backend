package com.erp.server.wms.controller;


import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.vo.PagingVO;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.wms.dto.PurchaseStorageDTO;
import com.erp.server.wms.service.PurchaseStorageService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.List;

/**
 * <p>
 * 采购入库单 前端控制器
 * </p>
 *
 * @author will
 * @since 2023-04-10
 */
@RestController
@RequestMapping("/purchaseStorage")
public class PurchaseStorageController extends BaseController {

    @Resource
    private PurchaseStorageService purchaseStorageService;


    /**
     * 列表查询
     * @author Will
     * @date: 2023/4/11 19:56
     * @param dto 
     * @return ApiResult<PagingVO<ListDTO>> 
     */
    @PostMapping("/paging")
    public ApiResult<PagingVO<PurchaseStorageDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<PurchaseStorageDTO.SearchParamDTO> dto) {
        return null;
    }

    /**
     * 列表数量
     * @author Will
     * @date: 2023/4/11 20:08
     * @param dto
     * @return ApiResult<List<ListStatusCountDTO>>
     */
    @PostMapping("/listCount")
    public ApiResult<List<PurchaseStorageDTO.ListStatusCountDTO>> listCount(@RequestBody PermissionsDTO dto) {
        return null;
    }

   /**
    * 新增
    * @author Will
    * @date: 2023/4/11 19:58
    * @param dto 
    * @return ApiResult 
    */
    @PostMapping("/add")
    public ApiResult add(@RequestBody @Validated PurchaseStorageDTO.AddDTO dto) {
        return null;
    }

   /**
    * 新增并提交
    * @author Will
    * @date: 2023/4/11 19:59
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/addAndSubmit")
    public ApiResult addAndSubmit(@RequestBody @Validated PurchaseStorageDTO.AddDTO dto) {
        return null;
    }
    
    /**
     * 修改
     * @author Will
     * @date: 2023/4/11 20:02
     * @param dto 
     * @return ApiResult 
     */
    @PostMapping("/update")
    public ApiResult update(@RequestBody @Validated PurchaseStorageDTO.UpdateDTO dto) {
        return null;
    }
    
    /**
     * 修改并提交
     * @author Will
     * @date: 2023/4/11 20:02
     * @param dto 
     * @return ApiResult 
     */
    @PostMapping("/updateAndSubmit")
    public ApiResult updateAndSubmit(@RequestBody @Validated PurchaseStorageDTO.UpdateDTO dto) {
        return null;
    }

    /**
     * 提交
     * @author Will
     * @date: 2023/4/11 20:00
     * @param dto 
     * @return ApiResult 
     */
    @PostMapping("/submit")
    public ApiResult submit(@RequestBody @Valid BaseIdsDTO.IdsDTO dto) {
        return null;
    }

    /**
     * 查看详情
     * @author Will
     * @date: 2023/4/11 20:10
     * @param id
     * @return ApiResult
     */
    @GetMapping("/view")
    public ApiResult<PurchaseStorageDTO.viewDTO> view(@RequestParam("id") String id) {
        return null;
    }


    /**
     * 删除
     * @author Will
     * @date: 2023/4/11 20:09
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/delete")
    public ApiResult delete(@RequestBody @Valid BaseIdsDTO.IdsDTO dto) {
        return null;
    }

    /**
     * 作废
     * @author Will
     * @date: 2023/4/11 20:11
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/invalid")
    public ApiResult invalid(@RequestBody @Validated BaseIdsDTO.RemarkDTO dto) {

        return null;
    }

    /**
     * 批量审核
     * @author Will
     * @date: 2023/4/11 20:11
     * @param baseApproveParamDTO
     * @return ApiResult
     */
    @PostMapping("/approve")
    public ApiResult approve(@RequestBody @Validated BaseApproveParamDTO baseApproveParamDTO) {
        return success();
    }

    /**
     * 批量反审核
     * @author Will
     * @date: 2023/4/11 20:12
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/disApprove")
    public ApiResult unAudit(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        return null;
    }

    /**
     * 取消流程
     * @author Will
     * @date: 2023/4/11 20:24
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/cancelProcess")
    public ApiResult cancelProcess(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        return null;
    }

    /**
     * 导出
     * @author Will
     * @date: 2023/4/11 20:25
     * @param dto
     * @param response
     * @return ApiResult
     */
    @PostMapping(value = "/exportExcel")
    public ApiResult exportExcel(@RequestBody PurchaseStorageDTO.SearchParamDTO dto, HttpServletResponse response) {
        return null;
    }

    /**
     * 下推退货单数据显示
     * @author Will
     * @date: 2023/4/11 20:30
     * @param id
     * @return ApiResult<ViewGeneratePurchaseReturnOrderDTO>
     */
    @GetMapping("/viewGeneratePurchaseReturnOrder")
    public ApiResult<PurchaseStorageDTO.ViewGeneratePurchaseReturnOrderDTO> viewGeneratePurchaseReturnOrder(@RequestParam("id") String id) {
        return null;
    }

    /**
     * 下推退货单数据保存
     * @author Will
     * @date: 2023/4/11 20:33
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/generatePurchaseReturnOrder")
    public ApiResult generatePurchaseReturnOrder(@RequestBody @Validated PurchaseStorageDTO.GeneratePurchaseReturnOrderDTO dto) {
        return null;
    }

}
