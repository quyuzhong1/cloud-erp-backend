package com.erp.server.scm.controller.feign;

import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.scm.dto.SupplierDTO;
import com.erp.model.scm.dto.SupplierPlantAddrDTO;
import com.erp.model.scm.entity.PurchaseOrderSupplierEntity;
import com.erp.model.scm.entity.SupplierEntity;
import com.erp.server.scm.service.PurchaseOrderSupplierService;
import com.erp.server.scm.service.SupplierService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 供应商feign控制器
 * @CreateTime: 2023-06-19  19:07
 * @Author: zhangchunlin
 */
@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping(value = "/feign/supplier")
public class SupplierFeignController extends BaseController {

    private final SupplierService supplierService;
    private final PurchaseOrderSupplierService purchaseOrderSupplierService;

    /**
     * 批量获取供应商信息
     * @param ids
     * @return
     */
    @PostMapping("/getSupplierSimpleInfo")
    public Map<String, SupplierDTO.SupplierSimpleDTO> getSupplierSimpleInfo(@RequestBody List<String> ids) {
        return supplierService.getSupplierSimpleInfo(ids);
    }

    /**
     * 审核 供应商
     * @Author Luo_WG
     * @Date 2023/7/12 12:31
     * @param dto
     * @return java.lang.Boolean
     **/
    @PostMapping("/supplierApprove")
    public List<BatchResultDTO> supplierApprove(@RequestBody BaseApproveParamDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        List<SupplierEntity> entityList = supplierService.listByIds(dto.getIds());
        for (String id : dto.getIds()) {
            SupplierEntity entity = entityList.stream().filter(v->v.getId().equals(id)).findFirst().orElse(null);
            if(Objects.isNull(entity)){
                resultDTOS.add(BatchResultDTO.fail(id,id,"供应商不存在"));
                continue;
            }
            try {
                resultDTOS.add(supplierService.approve(entity,dto.getType(),dto.getComment(),dto.getIsNeedProcess()));
            }catch (Exception e){
                log.error("产品sku审核失败",e);
                resultDTOS.add(BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage()));
            }
        }
        return resultDTOS;
    }


    /**
     * 根据供应商名称获取供应商
     * @author yl
     * @date 2023-09-22 19:50
     * @param supplierNames
     * @return java.lang.Boolean
     */
    @PostMapping("/listBySupplierByNames")
    public List<SupplierEntity> listBySupplierByNames(@RequestBody List<String> supplierNames) {
        return supplierService.listBySupplierByNames(supplierNames);
    }

    /**
     * 获取采购员对应供应商列表
     */
    @GetMapping("/listByPurchaseUserId")
    public List<SupplierEntity> listByPurchaseUserId(@RequestParam("purchaseUserId") String purchaseUserId) {
        return supplierService.listByPurchaseUserId(purchaseUserId);
    }

    /**
     * 根据用户id获取供应商
     */
    @GetMapping("/getSupplierByUid")
    public SupplierEntity getSupplierByUid(@RequestParam("uid") String uid) {
        return supplierService.getSupplierByUid(uid);
    }

    /**
     * 根据用户id获取供应商
     */
    @GetMapping("/getSupplierById")
    public SupplierEntity getSupplierById(@RequestParam("id") String id) {
        return supplierService.getById(id);
    }

    /**
     * @description: 根据供应商id集合查询
     * @author Will
     * @date: 2024/1/24 18:38
     * @param supplierIdList
     * @return List<SupplierDefaultDTO>
     */
    @PostMapping("/listDefaultBySupplierIdList")
    public List<SupplierDTO.SupplierDefaultDTO> listDefaultBySupplierIdList(@RequestBody List<String> supplierIdList) {
        return supplierService.listDefaultBySupplierIdList(supplierIdList);
    }

    /**
     * 根据采购订单获取供应商信息
     * @param ids
     * @return
     */
    @PostMapping("/getSupplierByOrderIds")
    List<PurchaseOrderSupplierEntity> getSupplierByOrderIds(@RequestBody List<String> ids){
        return purchaseOrderSupplierService.listByPurchaseOrderIds(ids);
    }

    /**
     * 根据供应商编号查询
     */
    @PostMapping("/listByCodes")
    List<SupplierEntity> listByCodes(@RequestBody List<String> codeList){
        return supplierService.listByCodes(codeList);
    }

    /**
     * 根据供应商编号查询
     */
    @PostMapping("/add")
    BatchResultDTO add(@RequestBody SupplierDTO.InsertDTO addDTO){
        SupplierEntity entity = supplierService.add(addDTO);
        return BatchResultDTO.success(entity.getId(), entity.getCode());
    }

     @PostMapping("/updateApproveStatus")
    void updateApproveStatus(SupplierDTO.UpdateApproveStatusDTO updateApproveStatusDTO){
        supplierService.updateApproveStatus(updateApproveStatusDTO);
    }

    /**
     * 获取工厂所在地
     * @author will
     * @date 2025/9/2 14:23
     * @param addPlantAddrDTO
     * @return List<AddDTO>
     */
    @PostMapping("/checkImportPlantAddr")
    List<SupplierPlantAddrDTO.AddDTO> checkImportPlantAddr(@RequestBody SupplierDTO.AddPlantAddrDTO addPlantAddrDTO){
        return supplierService.checkImportPlantAddr(addPlantAddrDTO.getCountylist(),addPlantAddrDTO.getCityList(),addPlantAddrDTO.getPlantAddr(),addPlantAddrDTO.getErrorMsgList(),addPlantAddrDTO.getIsUpdatePart());
    }

    /**
     * 根据供应商类型 获取到已审核的对应供应商
     * 未审核通过的会置为禁用
     *
     * @return
     */
    @GetMapping("/listApproveSupplierByCategoryType")
    public List<SupplierDTO.SupplierSimpleDTO> listApproveSupplierByCategoryType(@RequestParam("categoryType") String categoryType) {
        return supplierService.listApproveSupplierByCategoryType(categoryType);
    }

}