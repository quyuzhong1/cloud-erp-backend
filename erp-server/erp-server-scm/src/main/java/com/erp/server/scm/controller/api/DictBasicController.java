package com.erp.server.scm.controller.api;


import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.scm.dto.DictBasicDTO;
import com.erp.server.scm.service.DictBasicService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 字典管理
 *
 * @author Lambda
 * @since 2023-03-16
 */
@RestController
@RequestMapping("/dict")
public class DictBasicController extends BaseController {


    @Resource
    private DictBasicService dictBasicService;


    /**
     * 保存或者修改字典信息
     *
     * @param dto
     * @return
     */
    @PostMapping("/saveOrUpdateBatch")
    public ApiResult<Object> saveOrUpdate(@RequestBody @Validated List<DictBasicDTO> dto) {
        Boolean result = dictBasicService.saveOrUpdateDict(dto);
        return result == true ? success() : failure();
    }


    /**
     * 获取对应字典数据
     *  supplierPayMode  供应商结算方式
     *  supplierCategory 供应商分类
     *  supplierAccountPayment  供应商支付方式
     *  subcontractChangeReason 委外变更原因
     *  purchaseOrderType 采购订单单据类型
     *  executionStatus 执行状态
     * @return
     */
    @GetMapping("/list")
    public ApiResult<List<DictBasicDTO>> list(@RequestParam("key") String key) {
        List<DictBasicDTO> list = dictBasicService.getByKey(key);
        return success(list);
    }

}
