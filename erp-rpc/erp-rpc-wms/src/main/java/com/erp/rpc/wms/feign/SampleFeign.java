package com.erp.rpc.wms.feign;

import com.common.business.config.FeignErrorDecoder;
import com.common.business.dto.base.*;
import com.common.business.vo.PagingVO;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.wms.dto.*;
import com.erp.model.wms.dto.inventory.InstockForcastDTO;
import com.erp.model.wms.dto.inventory.InventoryQtyDTO;
import com.erp.model.wms.entity.InventoryEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.Valid;
import java.util.List;

/**
 * @Classname: SampleFeign
 * @CreateTime: 2025-09-10
 * @Author: jack
 */
@FeignClient(name = "erp-wms",path = "/feign", contextId = "sampleFeign" ,configuration = {FeignErrorDecoder.class})
public interface SampleFeign {
    // ==================== 样品借用单相关接口 ====================

    /**
     * 样品借用单新增
     */
    @PostMapping("/sampleBorrowInfo/add")
    ApiResult<BaseResultDTO.AddDTO> sampleBorrowInfoAdd(@RequestBody @Valid SampleBorrowInfoDTO.AddDTO dto);

    /**
     * 样品借用单修改
     */
    @PostMapping("/sampleBorrowInfo/update")
    ApiResult<?> sampleBorrowInfoUpdate(@RequestBody @Valid SampleBorrowInfoDTO.UpdateDTO dto);

    /**
     * 样品借用单标签页列表
     */
    @PostMapping("/sampleBorrowInfo/tabList")
    ApiResult<List<SampleBorrowInfoDTO.TabListDTO>> sampleBorrowInfoTabList(@RequestBody PermissionsDTO param);

    /**
     * 样品借用单分页查询
     */
    @PostMapping("/sampleBorrowInfo/paging")
    ApiResult<PagingVO<SampleBorrowInfoDTO.ListDTO>> sampleBorrowInfoPaging(@RequestBody @Valid PagingDTO<SampleBorrowInfoDTO.PagingParamDTO> dto);

    /**
     * 样品借用单新增并提交
     */
    @PostMapping("/sampleBorrowInfo/addAndSubmit")
    ApiResult<BaseResultDTO.AddDTO> sampleBorrowInfoAddAndSubmit(@RequestBody @Valid SampleBorrowInfoDTO.AddDTO dto);

    /**
     * 样品借用单修改并提交
     */
    @PostMapping("/sampleBorrowInfo/updateAndSubmit")
    ApiResult<Void> sampleBorrowInfoUpdateAndSubmit(@RequestBody @Valid SampleBorrowInfoDTO.UpdateDTO dto);

    /**
     * 样品借用单提交审核
     */
    @PostMapping("/sampleBorrowInfo/submit")
    ApiResult<List<BatchResultDTO>> sampleBorrowInfoSubmit(@RequestBody @Valid BaseIdsDTO.IdsDTO dto);

    /**
     * 样品借用单审核通过
     */
    @PostMapping("/sampleBorrowInfo/approve")
    ApiResult<List<BatchResultDTO>> sampleBorrowInfoApprove(@RequestBody @Valid BaseApproveParamDTO dto);

    /**
     * 样品借用单审核不通过
     */
    @PostMapping("/sampleBorrowInfo/disApprove")
    ApiResult<List<BatchResultDTO>> sampleBorrowInfoDisApprove(@RequestBody @Valid BaseIdsDTO.IdsDTO dto);

    /**
     * 样品借用单删除
     */
    @PostMapping("/sampleBorrowInfo/delete")
    ApiResult<List<BatchResultDTO>> sampleBorrowInfoDelete(@RequestBody @Valid BaseIdsDTO.IdsDTO dto);

    /**
     * 样品借用单作废
     */
    @PostMapping("/sampleBorrowInfo/invalid")
    ApiResult<List<BatchResultDTO>> sampleBorrowInfoInvalid(@RequestBody @Valid BaseIdsDTO.RemarkDTO dto);

    /**
     * 样品借用单取消流程
     */
    @PostMapping("/sampleBorrowInfo/cancelProcess")
    ApiResult<List<BatchResultDTO>> sampleBorrowInfoCancelProcess(@RequestBody @Valid BaseIdsDTO.IdsDTO dto);

    /**
     * 样品借用单完成借用
     */
    @PostMapping("/sampleBorrowInfo/finishBorrow")
    ApiResult<List<BatchResultDTO>> sampleBorrowInfoFinishBorrow(@RequestBody @Valid BaseIdsDTO.IdsDTO dto);

    /**
     * 样品借用单查看详情
     */
    @GetMapping("/sampleBorrowInfo/view")
    ApiResult<SampleBorrowInfoDTO.ViewDTO> sampleBorrowInfoView(@RequestParam("id") String id);

    /**
     * 样品借用单编号下拉
     */
    @PostMapping("/sampleBorrowInfo/drop/down")
    ApiResult<List<SampleBorrowInfoDTO.DropDownDTO>> sampleBorrowInfoDropDown(@RequestBody SampleBorrowInfoDTO.SelectDTO dto);

    /**
     * 样品借用单查询SKU
     */
    @PostMapping("/sampleBorrowInfo/listSku")
    ApiResult<PagingVO<SampleBorrowInfoDTO.SkuAvailableQtyDTO>> sampleBorrowInfoListSku(@RequestBody PagingDTO<SampleBorrowInfoDTO.SearchDTO> dto);

    // ==================== 样品领用单相关接口 ====================

    /**
     * 样品领用单新增
     */
    @PostMapping("/sampleRecipient/add")
    ApiResult<BaseResultDTO.AddDTO> sampleRecipientAdd(@RequestBody @Valid SampleRecipientDTO.AddDTO dto);

    /**
     * 样品领用单修改
     */
    @PostMapping("/sampleRecipient/update")
    ApiResult<?> sampleRecipientUpdate(@RequestBody @Valid SampleRecipientDTO.UpdateDTO dto);

    /**
     * 样品领用单标签页列表
     */
    @PostMapping("/sampleRecipient/tabList")
    ApiResult<List<SampleRecipientDTO.TabListDTO>> sampleRecipientTabList(@RequestBody PermissionsDTO param);

    /**
     * 样品领用单分页查询
     */
    @PostMapping("/sampleRecipient/paging")
    ApiResult<PagingVO<SampleRecipientDTO.ListDTO>> sampleRecipientPaging(@RequestBody @Valid PagingDTO<SampleRecipientDTO.PagingParamDTO> dto);

    /**
     * 样品领用单新增并提交
     */
    @PostMapping("/sampleRecipient/addAndSubmit")
    ApiResult<BaseResultDTO.AddDTO> sampleRecipientAddAndSubmit(@RequestBody @Valid SampleRecipientDTO.AddDTO dto);

    /**
     * 样品领用单修改并提交
     */
    @PostMapping("/sampleRecipient/updateAndSubmit")
    ApiResult<Void> sampleRecipientUpdateAndSubmit(@RequestBody @Valid SampleRecipientDTO.UpdateDTO dto);

    /**
     * 样品领用单提交审核
     */
    @PostMapping("/sampleRecipient/submit")
    ApiResult<List<BatchResultDTO>> sampleRecipientSubmit(@RequestBody @Valid BaseIdsDTO.IdsDTO dto);

    /**
     * 样品领用单审核通过
     */
    @PostMapping("/sampleRecipient/approve")
    ApiResult<List<BatchResultDTO>> sampleRecipientApprove(@RequestBody @Valid BaseApproveParamDTO dto);

    /**
     * 样品领用单审核不通过
     */
    @PostMapping("/sampleRecipient/disApprove")
    ApiResult<List<BatchResultDTO>> sampleRecipientDisApprove(@RequestBody @Valid BaseIdsDTO.IdsDTO dto);

    /**
     * 样品领用单删除
     */
    @PostMapping("/sampleRecipient/delete")
    ApiResult<List<BatchResultDTO>> sampleRecipientDelete(@RequestBody @Valid BaseIdsDTO.IdsDTO dto);

    /**
     * 样品领用单作废
     */
    @PostMapping("/sampleRecipient/invalid")
    ApiResult<List<BatchResultDTO>> sampleRecipientInvalid(@RequestBody @Valid BaseIdsDTO.RemarkDTO dto);

    /**
     * 样品领用单取消流程
     */
    @PostMapping("/sampleRecipient/cancelProcess")
    ApiResult<List<BatchResultDTO>> sampleRecipientCancelProcess(@RequestBody @Valid BaseIdsDTO.IdsDTO dto);

    /**
     * 样品领用单完成领用
     */
    @PostMapping("/sampleRecipient/finishRecipient")
    ApiResult<List<BatchResultDTO>> sampleRecipientFinishRecipient(@RequestBody @Valid BaseIdsDTO.IdsDTO dto);

    /**
     * 样品领用单查看详情
     */
    @GetMapping("/sampleRecipient/view")
    ApiResult<SampleRecipientDTO.ViewDTO> sampleRecipientView(@RequestParam("id") String id);

    /**
     * 样品领用单SKU列表
     */
    @PostMapping("/sampleRecipient/listSku")
    ApiResult<PagingVO<SampleRecipientDTO.SkuListResponseDTO>> sampleRecipientListSku(@RequestBody @Valid SampleRecipientDTO.SkuListQueryDTO dto);

    /**
     * 样品领用单SKU可用库存
     */
    @PostMapping("/sampleRecipient/skuAvailableStock")
    ApiResult<List<SampleRecipientDTO.SkuAvailableStockDTO>> sampleRecipientSkuAvailableStock(@RequestBody @Valid SampleRecipientDTO.SkuAvailableStockQueryDTO dto);

    /**
     * 样品领用单SKU成本
     */
    @PostMapping("/sampleRecipient/skuCost")
    ApiResult<List<SampleRecipientDTO.SkuDTO>> sampleRecipientSkuCost(@RequestBody @Valid SampleRecipientDTO.SkuCostQueryDTO dto);

    // ==================== 样品退回单相关接口 ====================

    /**
     * 样品退回单新增
     */
    @PostMapping("/sampleBackInfo/add")
    ApiResult<BaseResultDTO.AddDTO> sampleBackInfoAdd(@RequestBody @Valid SampleBackInfoDTO.AddDTO dto);

    /**
     * 样品退回单修改
     */
    @PostMapping("/sampleBackInfo/update")
    ApiResult<?> sampleBackInfoUpdate(@RequestBody @Valid SampleBackInfoDTO.UpdateDTO dto);

    /**
     * 样品退回单标签页列表
     */
    @PostMapping("/sampleBackInfo/tabList")
    ApiResult<List<SampleBackInfoDTO.TabListDTO>> sampleBackInfoTabList(@RequestBody PermissionsDTO param);

    /**
     * 样品退回单分页查询
     */
    @PostMapping("/sampleBackInfo/paging")
    ApiResult<PagingVO<SampleBackInfoDTO.ListDTO>> sampleBackInfoPaging(@RequestBody @Valid PagingDTO<SampleBackInfoDTO.PagingParamDTO> dto);

    /**
     * 样品退回单新增并提交
     */
    @PostMapping("/sampleBackInfo/addAndSubmit")
    ApiResult<BaseResultDTO.AddDTO> sampleBackInfoAddAndSubmit(@RequestBody @Valid SampleBackInfoDTO.AddDTO dto);

    /**
     * 样品退回单修改并提交
     */
    @PostMapping("/sampleBackInfo/updateAndSubmit")
    ApiResult<Void> sampleBackInfoUpdateAndSubmit(@RequestBody @Valid SampleBackInfoDTO.UpdateDTO dto);

    /**
     * 样品退回单提交审核
     */
    @PostMapping("/sampleBackInfo/submit")
    ApiResult<List<BatchResultDTO>> sampleBackInfoSubmit(@RequestBody @Valid BaseIdsDTO.IdsDTO dto);

    /**
     * 样品退回单审核通过
     */
    @PostMapping("/sampleBackInfo/approve")
    ApiResult<List<BatchResultDTO>> sampleBackInfoApprove(@RequestBody @Valid BaseApproveParamDTO dto);

    /**
     * 样品退回单审核不通过
     */
    @PostMapping("/sampleBackInfo/disApprove")
    ApiResult<List<BatchResultDTO>> sampleBackInfoDisApprove(@RequestBody @Valid BaseIdsDTO.IdsDTO dto);

    /**
     * 样品退回单删除
     */
    @PostMapping("/sampleBackInfo/delete")
    ApiResult<List<BatchResultDTO>> sampleBackInfoDelete(@RequestBody @Valid BaseIdsDTO.IdsDTO dto);

    /**
     * 样品退回单作废
     */
    @PostMapping("/sampleBackInfo/invalid")
    ApiResult<List<BatchResultDTO>> sampleBackInfoInvalid(@RequestBody @Valid BaseIdsDTO.RemarkDTO dto);

    /**
     * 样品退回单取消流程
     */
    @PostMapping("/sampleBackInfo/cancelProcess")
    ApiResult<List<BatchResultDTO>> sampleBackInfoCancelProcess(@RequestBody @Valid BaseIdsDTO.IdsDTO dto);

    /**
     * 样品退回单查看详情
     */
    @GetMapping("/sampleBackInfo/view")
    ApiResult<SampleBackInfoDTO.ViewDTO> sampleBackInfoView(@RequestParam("id") String id);

    // ==================== 样品归还单相关接口 ====================

    /**
     * 样品归还单新增
     */
    @PostMapping("/sampleReturnInfo/add")
    ApiResult<BaseResultDTO.AddDTO> sampleReturnInfoAdd(@RequestBody @Valid SampleReturnInfoDTO.AddDTO dto);

    /**
     * 样品归还单修改
     */
    @PostMapping("/sampleReturnInfo/update")
    ApiResult<?> sampleReturnInfoUpdate(@RequestBody @Valid SampleReturnInfoDTO.UpdateDTO dto);

    /**
     * 样品归还单标签页列表
     */
    @PostMapping("/sampleReturnInfo/tabList")
    ApiResult<List<SampleReturnInfoDTO.TabListDTO>> sampleReturnInfoTabList(@RequestBody PermissionsDTO param);

    /**
     * 样品归还单分页查询
     */
    @PostMapping("/sampleReturnInfo/paging")
    ApiResult<PagingVO<SampleReturnInfoDTO.ListDTO>> sampleReturnInfoPaging(@RequestBody @Valid PagingDTO<SampleReturnInfoDTO.PagingParamDTO> dto);

    /**
     * 样品归还单新增并提交
     */
    @PostMapping("/sampleReturnInfo/addAndSubmit")
    ApiResult<BaseResultDTO.AddDTO> sampleReturnInfoAddAndSubmit(@RequestBody @Valid SampleReturnInfoDTO.AddDTO dto);

    /**
     * 样品归还单修改并提交
     */
    @PostMapping("/sampleReturnInfo/updateAndSubmit")
    ApiResult<Void> sampleReturnInfoUpdateAndSubmit(@RequestBody @Valid SampleReturnInfoDTO.UpdateDTO dto);

    /**
     * 样品归还单提交审核
     */
    @PostMapping("/sampleReturnInfo/submit")
    ApiResult<List<BatchResultDTO>> sampleReturnInfoSubmit(@RequestBody @Valid BaseIdsDTO.IdsDTO dto);

    /**
     * 样品归还单审核通过
     */
    @PostMapping("/sampleReturnInfo/approve")
    ApiResult<List<BatchResultDTO>> sampleReturnInfoApprove(@RequestBody @Valid BaseApproveParamDTO dto);

    /**
     * 样品归还单审核不通过
     */
    @PostMapping("/sampleReturnInfo/disApprove")
    ApiResult<List<BatchResultDTO>> sampleReturnInfoDisApprove(@RequestBody @Valid BaseIdsDTO.IdsDTO dto);

    /**
     * 样品归还单删除
     */
    @PostMapping("/sampleReturnInfo/delete")
    ApiResult<List<BatchResultDTO>> sampleReturnInfoDelete(@RequestBody @Valid BaseIdsDTO.IdsDTO dto);

    /**
     * 样品归还单作废
     */
    @PostMapping("/sampleReturnInfo/invalid")
    ApiResult<List<BatchResultDTO>> sampleReturnInfoInvalid(@RequestBody @Valid BaseIdsDTO.RemarkDTO dto);

    /**
     * 样品归还单取消流程
     */
    @PostMapping("/sampleReturnInfo/cancelProcess")
    ApiResult<List<BatchResultDTO>> sampleReturnInfoCancelProcess(@RequestBody @Valid BaseIdsDTO.IdsDTO dto);

    /**
     * 样品归还单查看详情
     */
    @GetMapping("/sampleReturnInfo/view")
    ApiResult<SampleReturnInfoDTO.ViewDTO> sampleReturnInfoView(@RequestParam("id") String id);

    // ==================== 样品报废单相关接口 ====================

    /**
     * 样品报废单新增
     */
    @PostMapping("/sampleScrapInfo/add")
    ApiResult<BaseResultDTO.AddDTO> sampleScrapInfoAdd(@RequestBody @Valid SampleScrapInfoDTO.AddDTO dto);

    /**
     * 样品报废单修改
     */
    @PostMapping("/sampleScrapInfo/update")
    ApiResult<?> sampleScrapInfoUpdate(@RequestBody @Valid SampleScrapInfoDTO.UpdateDTO dto);

    /**
     * 样品报废单标签页列表
     */
    @PostMapping("/sampleScrapInfo/tabList")
    ApiResult<List<SampleScrapInfoDTO.TabListDTO>> sampleScrapInfoTabList(@RequestBody PermissionsDTO param);

    /**
     * 样品报废单分页查询
     */
    @PostMapping("/sampleScrapInfo/paging")
    ApiResult<PagingVO<SampleScrapInfoDTO.ListDTO>> sampleScrapInfoPaging(@RequestBody @Valid PagingDTO<SampleScrapInfoDTO.PagingParamDTO> dto);

    /**
     * 样品报废单新增并提交
     */
    @PostMapping("/sampleScrapInfo/addAndSubmit")
    ApiResult<BaseResultDTO.AddDTO> sampleScrapInfoAddAndSubmit(@RequestBody @Valid SampleScrapInfoDTO.AddDTO dto);

    /**
     * 样品报废单修改并提交
     */
    @PostMapping("/sampleScrapInfo/updateAndSubmit")
    ApiResult<Void> sampleScrapInfoUpdateAndSubmit(@RequestBody @Valid SampleScrapInfoDTO.UpdateDTO dto);

    /**
     * 样品报废单提交审核
     */
    @PostMapping("/sampleScrapInfo/submit")
    ApiResult<List<BatchResultDTO>> sampleScrapInfoSubmit(@RequestBody @Valid BaseIdsDTO.IdsDTO dto);

    /**
     * 样品报废单审核通过
     */
    @PostMapping("/sampleScrapInfo/approve")
    ApiResult<List<BatchResultDTO>> sampleScrapInfoApprove(@RequestBody @Valid BaseApproveParamDTO dto);

    /**
     * 样品报废单审核不通过
     */
    @PostMapping("/sampleScrapInfo/disApprove")
    ApiResult<List<BatchResultDTO>> sampleScrapInfoDisApprove(@RequestBody @Valid BaseIdsDTO.IdsDTO dto);

    /**
     * 样品报废单删除
     */
    @PostMapping("/sampleScrapInfo/delete")
    ApiResult<List<BatchResultDTO>> sampleScrapInfoDelete(@RequestBody @Valid BaseIdsDTO.IdsDTO dto);

    /**
     * 样品报废单作废
     */
    @PostMapping("/sampleScrapInfo/invalid")
    ApiResult<List<BatchResultDTO>> sampleScrapInfoInvalid(@RequestBody @Valid BaseIdsDTO.RemarkDTO dto);

    /**
     * 样品报废单取消流程
     */
    @PostMapping("/sampleScrapInfo/cancelProcess")
    ApiResult<List<BatchResultDTO>> sampleScrapInfoCancelProcess(@RequestBody @Valid BaseIdsDTO.IdsDTO dto);

    /**
     * 样品报废单查看详情
     */
    @GetMapping("/sampleScrapInfo/view")
    ApiResult<SampleScrapInfoDTO.ViewDTO> sampleScrapInfoView(@RequestParam("id") String id);

    // ==================== 样品台账相关接口 ====================

    /**
     * 样品台账标签页列表
     */
    @PostMapping("/sampleLedger/tabList")
    ApiResult<List<SampleLedgerDTO.TabListDTO>> sampleLedgerTabList(@RequestBody PermissionsDTO param);

    /**
     * 样品台账分页查询
     */
    @PostMapping("/sampleLedger/paging")
    ApiResult<PagingVO<SampleLedgerDTO.ListDTO>> sampleLedgerPaging(@RequestBody @Valid PagingDTO<SampleLedgerDTO.PagingParamDTO> dto);

    /**
     * 样品台账查看详情
     */
    @GetMapping("/sampleLedger/view")
    ApiResult<SampleLedgerDTO.ViewDTO> sampleLedgerView(@RequestParam("id") String id);

    /**
     * 样品台账流程列表
     */
    @PostMapping("/sampleLedger/flowList")
    ApiResult<List<SampleLedgerFlowDTO.ListDTO>> sampleLedgerFlowList(@RequestBody @Validated PagingDTO<SampleLedgerFlowDTO.PagingParamDTO> param);

    /**
     * 样品台账流程详情
     */
    @GetMapping("/sampleLedger/flowDetail")
    ApiResult<SampleLedgerFlowDTO.ViewDTO> sampleLedgerFlowDetail(@RequestParam("id") String id);

    /**
     * 样品台账SKU列表
     */
    @PostMapping("/sampleLedger/listSku")
    ApiResult<PagingVO<SampleLedgerDTO.SkuAvailableQtyDTO>> sampleLedgerListSku(@RequestBody @Valid PagingDTO<SampleLedgerDTO.SearchDTO> pagingDTO);
}
