package com.erp.server.wms.controller;


import com.common.business.dto.base.BaseIdDTO;
import com.common.business.dto.base.BaseIdsDTO;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.wms.dto.QcBillDTO;
import com.erp.server.wms.service.QcBillService;
import org.apache.commons.lang3.StringUtils;
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
     * 暂存
     *
     * @param dto
     * @return
     */
    @PostMapping("/draft")
    public ApiResult add(@RequestBody @Validated QcBillDTO.SaveOrUpdateDTO dto) {
        String id = qcBillService.draft(dto);
        return StringUtils.isNotBlank(id) ? success() : failure();
    }


    /**
     * 详情
     *
     * @param dto
     * @return
     */
    @PostMapping("/view")
    public ApiResult<QcBillDTO.ViewDTO> view(@RequestBody @Validated BaseIdDTO dto) {
        return success();
    }

    /**
     * 完成质检
     *
     * @param dto
     * @return
     */
    @PostMapping("/finish")
    public ApiResult finish(@RequestBody @Validated QcBillDTO.SaveOrUpdateDTO dto) {
        String id = qcBillService.draft(dto);
        return StringUtils.isNotBlank(id) ? success() : failure();
    }


    /**
     * 操作完成质检
     *
     * @param dto
     * @return
     */
    @PostMapping("/actionFinish")
    public ApiResult actionFinish(@RequestBody @Validated BaseIdsDTO dto) {

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
        String id = qcBillService.draft(dto);
        return StringUtils.isNotBlank(id) ? success() : failure();
    }


    /**
     * 操作完成免检
     *
     * @param dto
     * @return
     */
    @PostMapping("/actionExemption")
    public ApiResult actionExemption(@RequestBody @Validated BaseIdsDTO dto) {

        return success();
    }


    /**
     * 取消
     *
     * @param dto
     * @return
     */
    @PostMapping("/cancel")
    public ApiResult cancel(@RequestBody @Validated QcBillDTO.SaveOrUpdateDTO dto) {
        String id = qcBillService.draft(dto);
        return StringUtils.isNotBlank(id) ? success() : failure();
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
