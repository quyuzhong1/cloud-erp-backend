package com.erp.rpc.scm.feign;

import com.common.business.config.FeignErrorDecoder;
import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.erp.model.scm.dto.SupplierDTO;
import com.erp.model.scm.dto.SupplierPlantAddrDTO;
import com.erp.model.scm.entity.PurchaseOrderSupplierEntity;
import com.erp.model.scm.entity.SupplierEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

/**
 * @Classname: SupplierFeign

 * @CreateTime: 2023-06-19  19:22
 * @Author: zhangchunlin
 */
@FeignClient(name = "erp-scm", contextId = "supplierFeign",configuration = {FeignErrorDecoder.class})
public interface SupplierFeign {


    /**
     * 批量获取供应商信息
     * 返回的map不会为空，无需判断
     * @param ids
     * @return
     */
    @PostMapping("/feign/supplier/getSupplierSimpleInfo")
    Map<String, SupplierDTO.SupplierSimpleDTO> getSupplierSimpleInfo(@RequestBody List<String> ids);

    /**
     * 审核 供应商
     * @Author Luo_WG
     * @Date 2023/7/12 12:31
     * @param dto
     * @return java.lang.Boolean
     **/
    @PostMapping("/feign/supplier/supplierApprove")
    List<BatchResultDTO> supplierApprove(@RequestBody BaseApproveParamDTO dto);

    /**
     * 获取供应商列表
     * @param singletonList
     * @return
     */
    @PostMapping("/feign/supplier/listBySupplierByNames")
    List<SupplierEntity> listBySupplierByNames(List<String> singletonList);

    /**
     * 获取采购员对应供应商列表
     * @return
     */
    @GetMapping("/feign/supplier/listByPurchaseUserId")
    List<SupplierEntity> listByPurchaseUserId(@RequestParam("purchaseUserId") String purchaseUserId);

    @GetMapping("/feign/supplier/getSupplierByUid")
    SupplierEntity getSupplierByUid(@RequestParam("uid") String uid);

    @GetMapping("/feign/supplier/getSupplierById")
    SupplierEntity getSupplierById(@RequestParam("id") String id);

    /**
     * @description: 根据供应商id集合查询默认数据
     * @author Will
     * @date: 2024/1/24 18:36
     * @param supplierIdList
     * @return List<SupplierDefaultDTO>
     */
    @PostMapping("/feign/supplier/listDefaultBySupplierIdList")
    List<SupplierDTO.SupplierDefaultDTO> listDefaultBySupplierIdList(@RequestBody List<String> supplierIdList);

    /**
     * 根据采购订单获取供应商信息
     * @param ids
     * @return
     */
    @GetMapping("/feign/supplier/getSupplierByOrderIds")
    List<PurchaseOrderSupplierEntity> getSupplierByOrderIds(@RequestBody List<String> ids);

    /**
     * 根据供应商编号查询
     */
    @PostMapping("/feign/supplier/listByCodes")
    List<SupplierEntity> listByCodes(@RequestBody List<String> codeList);

    /**
     * add
     */
    @PostMapping("/feign/supplier/add")
    BatchResultDTO add(@RequestBody SupplierDTO.InsertDTO addDTO);

    @PostMapping("/feign/supplier/updateApproveStatus")
    void updateApproveStatus(SupplierDTO.UpdateApproveStatusDTO updateApproveStatusDTO);

    /**
     * 获取工厂所在地
     * @author will
     * @date 2025/9/2 14:21
     * @param addPlantAddrDTO
     * @return List<AddDTO>
     */
    @PostMapping("/feign/supplier/checkImportPlantAddr")
    List<SupplierPlantAddrDTO.AddDTO> checkImportPlantAddr(@RequestBody SupplierDTO.AddPlantAddrDTO addPlantAddrDTO);
}
