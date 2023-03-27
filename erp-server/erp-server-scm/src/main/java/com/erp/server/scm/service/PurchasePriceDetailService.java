package com.erp.server.scm.service;

import com.common.business.service.SuperService;
import com.erp.model.scm.dto.PurchasePriceDetailDTO;
import com.erp.model.scm.entity.PurchasePriceDetailEntity;
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
     * @param purchasePriceDetailList
     * @return void
     */
    void checkSkuInterval(List<PurchasePriceDetailDTO.AddDTO> purchasePriceDetailList);

    
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
     * 根据价目表id 获取产品明细信息
     * @author yl
     * @date 2023-03-27 9:48
     * @param id
     * @return java.util.List<com.erp.model.scm.dto.PurchasePriceDetailDTO.UpdateDTO>
     */
    List<PurchasePriceDetailDTO.UpdateDTO> getByPurchasePriceId(String id);

    
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
    PurchasePriceDetailDTO.ImportDTO importFile(MultipartFile excelFile);
}
