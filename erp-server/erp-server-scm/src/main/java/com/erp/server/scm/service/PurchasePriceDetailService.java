package com.erp.server.scm.service;

import cn.hutool.json.JSONArray;
import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.dto.base.UpdateStateDTO;
import com.common.business.service.SuperService;
import com.common.business.validator.ValidList;
import com.erp.model.scm.dto.PurchasePriceChangeDTO;
import com.erp.model.scm.dto.PurchasePriceChangeDetailDTO;
import com.erp.model.scm.dto.PurchasePriceDetailDTO;
import com.erp.model.scm.entity.PurchasePriceDetailEntity;
import org.apache.commons.math3.util.Pair;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * 产品采购价格明细表 服务类
 * </p>
 *
 * @author admin
 * @since 2023-03-15
 */
public interface PurchasePriceDetailService extends SuperService<PurchasePriceDetailEntity> {

    
    /**
     * 添加明细
     * @author yl
     * @date 2023-03-24 15:02
     * @param id
     * @param purchasePriceDetailList
     * @return void
     */
    void addPriceDetail(String id, List<PurchasePriceDetailDTO.AddDTO> purchasePriceDetailList);

    /**
     * @description: 报价信息验证
     * @author Will
     * @date: 2024/1/15 16:58
     * @param supplierId
     * @param list
     */
    void checkPurchasePriceDetail (String supplierId,String purchaseOrgId, List<PurchasePriceDetailEntity> list);
    /**
     * @description: 根据skuId查询是否存在符合条件的单价和税率
     * @author Will
     * @date: 2023/3/27 9:37
     * @param dto
     * @return List<PurchasePriceDetailDTO.PurchaseTaxPriceViewDTO>
     */
    List<PurchasePriceDetailDTO.PurchaseTaxPriceViewDTO> getTaxPrice(PurchasePriceDetailDTO.PurchaseTaxPriceSearchDTO dto);
    /**
     * @description: 根据skuId查询是否存在符合条件的单价和税率(返回错误消息)
     * @author Will
     * @date: 2023/3/30 14:51
     * @param dto
     * @return Pair<List<PurchaseTaxPriceViewDTO>>
     */
    Pair<String,List<PurchasePriceDetailDTO.PurchaseTaxPriceViewDTO>> listPurchaseTaxPriceView (PurchasePriceDetailDTO.PurchaseTaxPriceSearchDTO dto);

    /**
     * 根据价目表id 获取产品明细信息
     * @author yl
     * @date 2023-03-27 9:48
     * @param purchasePriceId
     * @return java.util.List<com.erp.model.scm.dto.PurchasePriceDetailDTO.UpdateDTO>
     */
    List<PurchasePriceDetailDTO.ViewDTO> getByPurchasePriceId(String purchasePriceId);

    /**
     * 根据价目表id 获取产品明细信息
     * @author yl
     * @date 2023-03-27 9:48
     * @param dto
     * @return java.util.List<com.erp.model.scm.dto.PurchasePriceDetailDTO.UpdateDTO>
     */
    List<PurchasePriceDetailDTO.ViewDTO> listByPurchasePriceIds(PurchasePriceChangeDetailDTO.SkuChangeParamDTO dto);

    /**
     * @description: 根据价目表明细ids获取产品明细信息
     * @author Will
     * @date: 2023/7/17 12:10
     * @param purchasePriceDetailIds
     * @return List<ViewDTO>
     */
    List<PurchasePriceDetailDTO.ViewDTO> listByPurchasePriceDetailIds(List<String> purchasePriceDetailIds);


    /**
     * 根据价目表id 和详情表id 集合获取对应数据
     * @author yl
     * @date 2023-03-27 9:48
     * @param purchasePriceId
     * @param purchasePriceDetailIds 采购价目详情表id 集合
     * @return java.util.List<com.erp.model.scm.dto.PurchasePriceDetailDTO.UpdateDTO>
     */
    List<PurchasePriceDetailDTO.ViewDTO> getPriceDetail(String purchasePriceId,List<String> purchasePriceDetailIds);

    
    /**
     * 修改产品明细
     * @author yl
     * @date 2023-03-27 11:18
     * @param id
     * @param purchasePriceDetailList
     * @return void
     */
    void updatePriceDetail(String id, List<PurchasePriceDetailDTO.UpdateDTO> purchasePriceDetailList);

    
    /**
     * 下载模板
     * @author yl
     * @date 2023-03-27 16:08
     * @param response
     * @return void
     */
    void downloadTemplate(HttpServletResponse response);

    /**
     * 导入产品信息
     * @author yl
     * @date 2023-03-27 16:51
     * @param excelFile
     * @return com.erp.model.scm.dto.PurchasePriceDetailDTO.ImportDTO
     */
    PurchasePriceDetailDTO.ImportDTO importFile(MultipartFile excelFile,List<String> skuIds,HttpServletResponse response);

    /**
     * 批量更改禁用状态
     * @author yl
     * @date 2023-03-28 10:03
     * @param dto
     * @return java.lang.Boolean
     */
    Boolean updateDisabled(UpdateStateDTO.BatchUpdateDTO dto);

    
    /**
     * 根据供应商、组织、SKU查询
     * @author yl
     * @date 2023-04-06 9:37
     * @param supplierId
     * @param purchaseOrgId
     * @param skuIdList
     * @return java.util.List<com.erp.model.scm.dto.PurchasePriceDetailDTO.ViewDTO>
     */
    List<PurchasePriceDetailDTO.ViewDTO> listCheckPurchasePriceDetail(String supplierId,String purchaseOrgId,List<String> skuIdList);

    /**
     * 查询供应商的
     * @Author Luo_WG
     * @Date 2024/1/9 14:20
     * @param supplierIdList
     * @param detailIds
     * @param skuIdList
     * @return java.util.List<com.erp.model.scm.dto.PurchasePriceDetailDTO.AddDTO>
     **/
    List<PurchasePriceDetailDTO.AddDTO> listBySupplierId(List<String> supplierIdList,List<String> detailIds,List<String> skuIdList);

    /**
     * 采购价目表 点击变更报价 获取到详情
     * @author yl
     * @date 2023-04-06 12:03
     * @param ids
     * @return com.erp.model.scm.dto.PurchasePriceChangeDTO.ViewDTO
     */
    PurchasePriceChangeDTO.ViewDTO priceChangeDetail(List<String> ids);

    /**
     * 根据采购价目表id 获取到采购价目变更的明细
     * @author yl
     * @date 2023-04-06 18:54
     * @param dto
     * @return java.util.List<com.erp.model.scm.dto.PurchasePriceChangeDetailDTO.ViewDTO>
     */
    List<PurchasePriceChangeDetailDTO.ViewDTO> listPriceChangeDetail(PurchasePriceChangeDetailDTO.SkuChangeParamDTO dto);
    /**
     * @description: 更新明细金蝶id
     * @author Will
     * @date: 2023/5/4 18:36
     * @param list
     */
    void updateKingdeeDetailId(JSONArray list);

    
    /**
     * 获取根据主表id
     * @author yl
     * @date 2023-05-05 16:41
     * @param id
     * @return java.util.List<java.lang.String>
     */
    List<PurchasePriceDetailEntity> listDetailByMainId(String id);


    /**
     * 获取根据主表ids
     * @author yl
     * @date 2023-05-05 16:41
     * @param ids
     * @return java.util.List<java.lang.String>
     */
    List<PurchasePriceDetailEntity> listDetailByMainIds(List<String> ids);

    /**
     * 根据供应商和状态查询价目信息
     * @author yl
     * @date 2023-08-06 9:37
     * @param supplierId
     * @return java.util.List<PurchasePriceDetailEntity>
     */
    List<PurchasePriceDetailEntity> getBySupplierIdAndStatus(String supplierId,String purchaseOrgId,List<String> statusList);

    /**
     * 更新信息
     * @param purchasePriceDetailEntity
     * @return
     */
    void updateDetail(PurchasePriceDetailEntity purchasePriceDetailEntity, PurchasePriceDetailEntity old);
    /**
     * @description: 批量查询报价
     * @author Will
     * @date: 2023/9/14 14:10
     * @param list
     * @return List<PurchaseTaxPriceBatchViewDTO>
     */
    List<PurchasePriceDetailDTO.PurchaseTaxPriceBatchViewDTO> batchGetTaxPrice(ValidList<PurchasePriceDetailDTO.PurchaseTaxPriceSearchDTO> list);
    /**
     * @description: 更新明细备注
     * @author Will
     * @date: 2023/9/21 15:26
     * @param ids
     * @param remark
     */
    void updateDetailRemark(List<String> ids, String remark);

    /**
     * 禁用
     * @author Will
     * @date: 2024/1/15 15:01
     * @param dto
     * @return Boolean
     */
    Boolean disabled(BaseIdsDTO.IdsDTO dto);
    /**
     * 启用
     * @author Will
     * @date: 2024/1/15 15:02
     * @param dto
     * @return Boolean
     */
    Boolean enable(BaseIdsDTO.IdsDTO dto);

    /**
     * 根据组织/供应商/sku/数量获取采购单价
     * @param skuIdList
     * @param supplierIdList
     * @param purchaseQtyList
     * @param purchaseOrgIdList
     * @return
     */
    List<PurchasePriceDetailDTO.PurchaseTaxPriceBatchViewDTO> batchGetTaxPrice(List<String> skuIdList, List<String> supplierIdList, List<Integer> purchaseQtyList, List<String> purchaseOrgIdList);
}
