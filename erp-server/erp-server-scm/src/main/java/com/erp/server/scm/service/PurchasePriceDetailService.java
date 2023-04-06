package com.erp.server.scm.service;

import com.common.business.dto.base.UpdateStateDTO;
import com.common.business.service.SuperService;
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
     *  检查sku 区间报价
     * @author yl
     * @date 2023-03-24 14:01
     * @param purchasePriceDetailList 参数的
     * @param  supplierPriceDetailList  供应商已有的
     * @param  supplierPriceChangeDetailList 供应商变更的
     * @return void
     */
    void checkSkuInterval(List<PurchasePriceDetailDTO.AddDTO> purchasePriceDetailList,List<PurchasePriceDetailDTO.AddDTO> supplierPriceDetailList,List<PurchasePriceDetailDTO.AddDTO> supplierPriceChangeDetailList);

    
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
     * 查询供应商的
     * @author yl
     * @date 2023-04-06 9:37
     * @param supplierId
     * @return java.util.List<com.erp.model.scm.dto.PurchasePriceDetailDTO.AddDTO>
     */
    List<PurchasePriceDetailDTO.AddDTO> getBySupplierId(String supplierId);
}
