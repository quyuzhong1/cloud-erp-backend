package com.erp.server.wms.controller;


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

/**
 * <p>
 * 质检单表 前端控制器
 * </p>
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
     * @param dto
     * @return
     */
    @PostMapping("/draft")
    public ApiResult add(@RequestBody @Validated QcBillDTO.SaveOrUpdateDTO dto) {
        String id = qcBillService.draft(dto);
        return StringUtils.isNotBlank(id) ? success() : failure();
    }


}
