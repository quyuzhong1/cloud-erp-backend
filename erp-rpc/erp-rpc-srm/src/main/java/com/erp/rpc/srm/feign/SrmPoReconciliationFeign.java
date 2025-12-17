package com.erp.rpc.srm.feign;

import com.common.business.config.FeignErrorDecoder;
import com.erp.model.srm.dto.CfgSettingDTO;
import com.erp.model.srm.dto.PoReconciliationDetailDTO;
import com.erp.model.srm.entity.CfgSettingEntity;
import com.erp.model.srm.entity.PoReconciliationDetailEntity;
import com.erp.model.srm.vo.SupplierConfigVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * @description: 对账单
 * @author Will
 * @date: 2024/1/25 9:58
 */
@FeignClient(name = "erp-srm", contextId = "srmPoReconciliationFeign",configuration = {FeignErrorDecoder.class})
public interface SrmPoReconciliationFeign {

    /**
     * @description: 新增对账明细
     * @author Will
     * @date: 2024/1/25 9:58
     * @param addList
     * @return List<SupplierConfigVO>
     */
    @PostMapping("/feign/poReconciliation/add")
    void add(@RequestBody @Validated List<PoReconciliationDetailDTO.AddDTO> addList);

    /**
     * @description: 根据来源明细id集合删除对账明细
     * @author Will
     * @date: 2024/1/25 17:17
     * @param sourceDetailIdList
     */
    @PostMapping("/feign/poReconciliation/deleteDetailBySourceDetailIdList")
    void deleteDetailBySourceDetailIdList(@RequestBody @Validated List<String> sourceDetailIdList);

    /**
     * @description: 根据来源明细id集合查询对账明细
     * @author Will
     * @date: 2024/1/25 17:17
     * @param sourceDetailIdList
     * @return List<PoReconciliationDetailEntity>
     */
    @PostMapping("/feign/poReconciliation/listDetailBySourceDetailIdList")
    List<PoReconciliationDetailEntity> listDetailBySourceDetailIdList(@RequestBody @Validated List<String> sourceDetailIdList);

    /**
     * @description: 更新业务状态
     * @author Will
     * @date: 2024/2/2 17:32
     * @param statusDTO
     */
    @PostMapping("/feign/poReconciliation/updateBusinessStatusBySourceIdList")
    void updateBusinessStatusBySourceIdList(@RequestBody @Validated PoReconciliationDetailDTO.UpdateBusinessStatusDTO statusDTO);
    /**
     * 获取供应商未确认订单明细数量
     * @param supplierId
     * @return
     */
    @GetMapping("/feign/poReconciliation/countSupplierUnConfirmOrderDetail")
    Integer countSupplierUnConfirmOrderDetail(@RequestParam(value = "supplierId") String supplierId);
}
