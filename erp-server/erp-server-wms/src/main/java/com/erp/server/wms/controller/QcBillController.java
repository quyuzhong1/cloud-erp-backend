package com.erp.server.wms.controller;


import com.common.business.dto.base.BaseIdDTO;
import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.validator.AddGroup;
import com.common.business.vo.PagingVO;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.wms.dto.QcBillDTO;
import com.erp.model.wms.enums.QcBillStatusEnum;
import com.erp.server.wms.service.QcBillService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

/**
 * 质检单
 *
 * @author lambda
 * @since 2023-04-14
 */
@RestController
@RequestMapping("/qcBill")
public class QcBillController extends BaseController {


    @Resource
    private QcBillService qcBillService;


    /**
     * 分页
     *
     * @param dto
     * @return
     */
    @PostMapping("/paging")
    public ApiResult<PagingVO<QcBillDTO.PagingViewDTO>> paging(@RequestBody @Validated PagingDTO<QcBillDTO.PagingParamDTO> dto) {
        PagingVO<QcBillDTO.PagingViewDTO> pagingVO = qcBillService.paging(dto);
        return success(pagingVO);
    }


    /**
     * 暂存
     *
     * @param dto
     * @return
     */
    @PostMapping("/draft")
    public ApiResult draft(@RequestBody QcBillDTO.SaveOrUpdateDTO dto) {
        Boolean result = qcBillService.draft(dto);
        return result ? success() : failure();
    }

    /**
     * 保存
     *
     * @param dto
     * @return
     */
    @PostMapping("/add")
    public ApiResult add(@RequestBody @Validated({AddGroup.class}) QcBillDTO.SaveOrUpdateDTO dto) {
        Boolean result = qcBillService.add(dto);
        return result ? success() : failure();
    }


    /**
     * 详情
     *
     * @param dto
     * @return
     */
    @PostMapping("/view")
    public ApiResult<QcBillDTO.ViewDTO> view(@RequestBody @Validated BaseIdDTO dto) {
        QcBillDTO.ViewDTO view = qcBillService.view(dto.getId());
        return success(view);
    }

    /**
     * 完成质检
     *
     * @param dto
     * @return
     */
    @PostMapping("/finish")
    public ApiResult finish(@RequestBody @Validated({AddGroup.class}) QcBillDTO.SaveOrUpdateDTO dto) {
        Boolean result = qcBillService.finish(dto);
        return result ? success() : failure();
    }


    /**
     * 操作完成质检
     *
     * @param dto
     * @return
     */
    @PostMapping("/actionFinish")
    public ApiResult actionFinish(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {

        return success();
    }


    /**
     * 免检
     *
     * @param dto
     * @return
     */
    @PostMapping("/exemption")
    public ApiResult exemption(@RequestBody @Validated QcBillDTO.SaveOrUpdateDTO dto) {
        return success();
    }


    /**
     * 操作完成免检
     *
     * @param dto
     * @return
     */
    @PostMapping("/actionExemption")
    public ApiResult actionExemption(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {

        return success();
    }


    /**
     * 操作 取消质检
     *
     * @param dto
     * @return
     */
    @PostMapping("/actionCancel")
    public ApiResult actionCancel(@RequestBody @Validated BaseIdsDTO dto) {

        return success();
    }


    /**
     * 删除
     *
     * @param dto
     * @return
     */
    public ApiResult delete(@RequestBody @Valid BaseIdsDTO.IdsDTO dto) {
        return success();
    }

    /**
     * 导出
     * 质检单
     */
    @PostMapping("/exportQcBill")
    public ApiResult exportWarehouse(@RequestBody @Valid QcBillDTO.ExportDTO dto, HttpServletResponse response) {
        qcBillService.exportQcBill(dto, response);
        return success();
    }

    /**
     * 分配质检员
     *
     * @return
     */
    @PostMapping("/assign")
    public ApiResult assign(@RequestBody @Valid QcBillDTO.AssignDTO dto) {
        return success();

    }


}
