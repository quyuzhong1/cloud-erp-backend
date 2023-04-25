package com.erp.server.wms.controller;


import com.common.business.dto.base.BaseIdDTO;
import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.validator.AddGroup;
import com.common.business.vo.PagingVO;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.wms.dto.PurchaseReturnOrderDTO;
import com.erp.model.wms.dto.PurchaseStockInDTO;
import com.erp.model.wms.dto.QcInfoDTO;
import com.erp.model.wms.dto.QcResultDTO;
import com.erp.server.wms.service.QcInfoService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.List;

/**
 * 质检单
 *
 * @author lambda
 * @since 2023-04-14
 */
@RestController
@RequestMapping("/qcBill")
public class QcInfoController extends BaseController {


    @Resource
    private QcInfoService qcInfoService;


    /**
     * 分页
     *
     * @param dto
     * @return
     */
    @PostMapping("/paging")
    public ApiResult<PagingVO<QcInfoDTO.PagingViewDTO>> paging(@RequestBody @Validated PagingDTO<QcInfoDTO.PagingParamDTO> dto) {
        PagingVO<QcInfoDTO.PagingViewDTO> pagingVO = qcInfoService.paging(dto);
        return success(pagingVO);
    }

    /**
     * 获取 质检tab 列表(待质检以及数量的列表)
     *
     * @return
     */
    @GetMapping("/tabList")
    public ApiResult<List<QcInfoDTO.TabListDTO>> tabList() {
        List<QcInfoDTO.TabListDTO> list = qcInfoService.tabList();
        return success(list);
    }


    /**
     * 暂存
     *
     * @param dto
     * @return
     */
    @PostMapping("/draft")
    public ApiResult draft(@RequestBody QcInfoDTO.SaveOrUpdateDTO dto) {
        Boolean result = qcInfoService.draft(dto);
        return result ? success() : failure();
    }

    /**
     * 保存
     *
     * @param dto
     * @return
     */
    @PostMapping("/add")
    public ApiResult add(@RequestBody @Validated({AddGroup.class}) QcInfoDTO.SaveOrUpdateDTO dto) {
        Boolean result = qcInfoService.add(dto);
        return result ? success() : failure();
    }


    /**
     * 详情
     *
     * @param dto
     * @return
     */
    @PostMapping("/view")
    public ApiResult<QcInfoDTO.ViewDTO> view(@RequestBody @Validated BaseIdDTO dto) {
        QcInfoDTO.ViewDTO view = qcInfoService.view(dto.getId());
        return success(view);
    }

    /**
     * 完成质检
     *
     * @param dto
     * @return
     */
    @PostMapping("/finish")
    public ApiResult finish(@RequestBody @Validated({AddGroup.class}) QcInfoDTO.SaveOrUpdateDTO dto) {
        Boolean result = qcInfoService.finish(dto);
        return result ? success() : failure();
    }

    /**
     * 免检
     *
     * @param dto
     * @return
     */
    @PostMapping("/exemption")
    public ApiResult exemption(@RequestBody @Validated QcInfoDTO.SaveOrUpdateDTO dto) {
        Boolean result = qcInfoService.exemption(dto);
        return result ? success() : failure();
    }


    /**
     * 批量完成质检
     *
     * @param dto
     * @return
     */
    @PostMapping("/batchFinish")
    public ApiResult actionFinish(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        Boolean result = qcInfoService.batchFinish(dto.getIds());
        return result ? success() : failure();
    }


    /**
     * 批量免检
     *
     * @param dto
     * @return
     */
    @PostMapping("/batchExemption")
    public ApiResult batchExemption(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        Boolean result = qcInfoService.batchExemption(dto.getIds());
        return result ? success() : failure();
    }


    /**
     * 批量取消质检
     *
     * @param dto
     * @return
     */
    @PostMapping("/batchCancel")
    public ApiResult batchCancel(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        Boolean result = qcInfoService.batchCancel(dto.getIds());
        return result ? success() : failure();
    }


    /**
     * 删除
     *
     * @param dto
     * @return
     */
    @PostMapping("/delete")
    public ApiResult delete(@RequestBody @Valid BaseIdsDTO.IdsDTO dto) {
        Boolean result = qcInfoService.delete(dto.getIds());
        return result ? success() : failure();
    }


    /**
     * 撤销
     *
     * @param dto
     * @return
     */
    @PostMapping("/cancelProcess")
    public ApiResult cancelProcess(@RequestBody @Valid BaseIdsDTO.IdsDTO dto) {
        Boolean result = qcInfoService.cancelProcess(dto.getIds());
        return result ? success() : failure();
    }


    /**
     * 导出质检单
     */
    @PostMapping("/exportQcBill")
    public ApiResult exportWarehouse(@RequestBody @Valid QcInfoDTO.ExportDTO dto, HttpServletResponse response) {
        qcInfoService.exportQcBill(dto, response);
        return success();
    }

    /**
     * 分配质检员
     *
     * @return
     */
    @PostMapping("/assign")
    public ApiResult assign(@RequestBody @Valid QcInfoDTO.AssignDTO dto) {
        Boolean result = qcInfoService.assign(dto);
        return result ? success() : failure();
    }

    /**
     * 批量更新处理措施
     *
     * @return
     */
    @PostMapping("/updateHandleMode")
    public ApiResult updateHandleMode(@RequestBody @Valid QcResultDTO.UpdateHandleModeDTO dto) {
        Boolean result = qcInfoService.updateHandleMode(dto);
        return result ? success() : failure();
    }

    /**
     * 下推退货单数据显示
     * @author yl
     * @date: 2023/4/11 20:30
     * @param dto
     * @return ApiResult<ViewGeneratePurchaseReturnOrderDTO>
     */
    @PostMapping("/viewGeneratePurchaseReturnOrder")
    public ApiResult<List<PurchaseReturnOrderDTO.ViewGeneratePurchaseReturnOrderDTO>> viewGeneratePurchaseReturnOrder(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<PurchaseReturnOrderDTO.ViewGeneratePurchaseReturnOrderDTO> list = qcInfoService.viewGeneratePurchaseReturnOrder(dto.getIds());
        return success(list);
    }


    /**
     * 下推退货单数据保存
     * @author yl
     * @date 2023-04-24 9:25
     * @param dto
     * @return com.common.core.controller.vo.ApiResult<java.util.List<com.erp.model.wms.dto.PurchaseReturnOrderDTO.ViewGeneratePurchaseReturnOrderDTO>>
     */
    @PostMapping("/generatePurchaseReturnOrder")
    public ApiResult generatePurchaseReturnOrder(@RequestBody @Validated PurchaseStockInDTO.ListGeneratePurchaseReturnOrderDTO dto) {
        Boolean flag = qcInfoService.generatePurchaseReturnOrder(dto);
        return flag?success():failure();
    }


}
