package com.erp.server.scm.controller.feign;

import com.common.business.enums.ApproveStatusEnum;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.scm.dto.PurchasePriceDTO;
import com.erp.model.scm.entity.PurchasePriceEntity;
import com.erp.model.scm.dto.PurchaseSkuOrgRefDTO;
import com.erp.model.scm.entity.PurchaseSkuOrgRefEntity;
import com.erp.server.scm.service.PurchasePriceService;
import com.erp.server.scm.service.PurchaseSkuOrgRefService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * 采购价目表
 * @Author Luo_WG
 * @Date 2023/6/30 19:44
 **/
@RestController
@RequestMapping("feign/purchasePrice")
public class PurchasePriceFeignController {

    @Resource
    private PurchasePriceService purchasePriceService;
    @Resource
    private PurchaseSkuOrgRefService purchaseSkuOrgRefService;

    /**
     * 根据供应商Ids查询最新的sku价格信息
     * @param ids ids
     * @return java.util.List<com.erp.model.scm.entity.PurchaseOrderEntity>
     * @Author Luo_WG
     * @Date 2023/4/13 11:20
     **/
    @PostMapping("/listSupplierSkuPrice")
    public List<PurchasePriceDTO.SupplierSkuPrice> listSupplierSkuPrice(@RequestBody List<String> ids) {
        return purchasePriceService.listSupplierSkuPrice(ids);
    }

    /**
     * 根据供应商Ids查询所有sku价格信息
     * @author Will
     * @date: 2023/10/27 10:05
     * @param ids
     * @return List<SupplierSkuPrice>
     */
    @PostMapping("/listAllSupplierSkuPrice")
    public List<PurchasePriceDTO.SupplierSkuPrice> listAllSupplierSkuPrice(@RequestBody List<String> ids) {
        return purchasePriceService.listAllSupplierSkuPrice(ids);
    }

    /**
     * 批量获取列表采购单价
     * @param list
     * @return
     */
    @PostMapping("/batchGetPurchasePrice")
    public List<PurchasePriceDTO.PriceDTO> batchGetPurchasePrice(@RequestBody List<PurchasePriceDTO.PriceDTO> list) {
        return purchasePriceService.batchGetPurchasePrice(list);
    }

     @PostMapping("/listByCodes")
    public List<PurchasePriceEntity> listByCodes(@RequestBody List<String> codes) {
        return purchasePriceService.listByCodes(codes);
    }

     @PostMapping("/updateApproveStatus")
    public void updateApproveStatus(@RequestBody PurchasePriceDTO.UpdateApprovalStatusDTO updateApprovalStatusDTO) {
        purchasePriceService.updateApproveStatus(updateApprovalStatusDTO);
     }
    /**
     * 根据sku获取采购组织关系
     */
    @PostMapping("/getBySkuIdList")
    public List<PurchaseSkuOrgRefEntity> getBySkuIdList(@RequestBody @Validated PurchaseSkuOrgRefDTO.QuerySkuDTO querySkuDTO) {
        return purchaseSkuOrgRefService.getBySkuIdList(querySkuDTO.getSkuIdList());
    }

    /**
     * 根据供应商Ids查询最新的sku价格信息
     *
     **/
    @PostMapping("/listSkuPrice")
    public List<PurchasePriceDTO.SupplierSkuPrice> listSkuPrice() {
        return purchasePriceService.listSkuPrice();
    }
}
