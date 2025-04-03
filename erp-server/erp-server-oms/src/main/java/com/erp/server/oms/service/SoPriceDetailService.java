package com.erp.server.oms.service;

import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.dto.base.UpdateStateDTO;
import com.common.business.service.SuperService;
import com.erp.model.oms.dto.SoPriceChangeDTO;
import com.erp.model.oms.dto.SoPriceChangeDetailDTO;
import com.erp.model.oms.dto.SoPriceDetailDTO;
import com.erp.model.oms.entity.SoPriceDetailEntity;
import org.apache.commons.math3.util.Pair;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * 销售价目表明细 服务类
 * </p>
 *
 * @author will
 * @since 2025-03-24
 */
public interface SoPriceDetailService extends SuperService<SoPriceDetailEntity> {

    /**
     * 添加明细
     * @author yl
     * @date 2023-03-24 15:02
     * @param id
     * @param soPriceDetailList
     * @return void
     */
    void addPriceDetail(String id, List<SoPriceDetailDTO.AddDTO> soPriceDetailList);

    /**
     * @description: 报价信息验证
     * @author Will
     * @date: 2024/1/15 16:58
     * @param supplierId
     * @param list
     */
    void checkSoPriceDetail (String supplierId,String soOrgId, List<SoPriceDetailEntity> list);
    /**
     * @description: 根据skuId查询是否存在符合条件的单价和税率
     * @author Will
     * @date: 2023/3/27 9:37
     * @param dto
     * @return List<SoPriceDetailDTO.soTaxPriceViewDTO>
     */
    List<SoPriceDetailDTO.SoTaxPriceViewDTO> getTaxPrice(SoPriceDetailDTO.SoTaxPriceSearchDTO dto);
    /**
     * @description: 根据skuId查询是否存在符合条件的单价和税率(返回错误消息)
     * @author Will
     * @date: 2023/3/30 14:51
     * @param dto
     * @return Pair<List<soTaxPriceViewDTO>>
     */
    Pair<String,List<SoPriceDetailDTO.SoTaxPriceViewDTO>> listSoTaxPriceView (SoPriceDetailDTO.SoTaxPriceSearchDTO dto);

    /**
     * 根据价目表id 获取产品明细信息
     * @author yl
     * @date 2023-03-27 9:48
     * @param soPriceId
     * @return java.util.List<com.erp.model.scm.dto.SoPriceDetailDTO.UpdateDTO>
     */
    List<SoPriceDetailDTO.ViewDTO> getBySoPriceId(String soPriceId);

    /**
     * 根据价目表id 获取产品明细信息
     * @author yl
     * @date 2023-03-27 9:48
     * @param dto
     * @return java.util.List<com.erp.model.scm.dto.SoPriceDetailDTO.UpdateDTO>
     */
    List<SoPriceDetailDTO.ViewDTO> listBySoPriceIds(SoPriceChangeDetailDTO.SkuChangeParamDTO dto);

    /**
     * 采购价目表 点击变更报价 获取到详情
     * @author yl
     * @date 2023-04-06 12:03
     * @param ids
     * @return com.erp.model.scm.dto.PurchasePriceChangeDTO.ViewDTO
     */
    SoPriceChangeDTO.ViewDTO priceChangeDetail(List<String> ids);

    /**
     * @description: 根据价目表明细ids获取产品明细信息
     * @author Will
     * @date: 2023/7/17 12:10
     * @param soPriceDetailIds
     * @return List<ViewDTO>
     */
    List<SoPriceDetailDTO.ViewDTO> listBySoPriceDetailIds(List<String> soPriceDetailIds);
    /**
     * 根据采购价目表id 获取到采购价目变更的明细
     * @author yl
     * @date 2023-04-06 18:54
     * @param dto
     * @return java.util.List<com.erp.model.scm.dto.PurchasePriceChangeDetailDTO.ViewDTO>
     */
    List<SoPriceChangeDetailDTO.ViewDTO> listPriceChangeDetail(SoPriceChangeDetailDTO.SkuChangeParamDTO dto);

    /**
     * 修改产品明细
     * @author yl
     * @date 2023-03-27 11:18
     * @param id
     * @param soPriceDetailList
     * @return void
     */
    void updatePriceDetail(String id, List<SoPriceDetailDTO.UpdateDTO> soPriceDetailList);


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
     * @return com.erp.model.scm.dto.SoPriceDetailDTO.ImportDTO
     */
    SoPriceDetailDTO.ImportDTO importFile(MultipartFile excelFile, List<String> skuIds, HttpServletResponse response);

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
     * @param soOrgId
     * @param skuIdList
     * @return java.util.List<com.erp.model.scm.dto.SoPriceDetailDTO.ViewDTO>
     */
    List<SoPriceDetailDTO.ViewDTO> listCheckSoPriceDetail(String supplierId,String soOrgId,List<String> skuIdList);

    /**
     * 获取根据主表id
     * @author yl
     * @date 2023-05-05 16:41
     * @param id
     * @return java.util.List<java.lang.String>
     */
    List<SoPriceDetailEntity> listDetailByMainId(String id);


    /**
     * 获取根据主表ids
     * @author yl
     * @date 2023-05-05 16:41
     * @param ids
     * @return java.util.List<java.lang.String>
     */
    List<SoPriceDetailEntity> listDetailByMainIds(List<String> ids);

    /**
     * 根据供应商和状态查询价目信息
     * @author yl
     * @date 2023-08-06 9:37
     * @param customerId
     * @return java.util.List<SoPriceDetailEntity>
     */
    List<SoPriceDetailEntity> getByCustomerIdAndStatus(String customerId, String soOrgId, List<String> statusList);

    /**
     * 更新信息
     * @param SoPriceDetailEntity
     * @return
     */
    void updateDetail(SoPriceDetailEntity SoPriceDetailEntity, SoPriceDetailEntity old);

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
     * 批量查询报价信息
     * @author will
     * @date 2025/3/27 12:16
     * @param skuIdList
     * @param customerIdList
     * @param soQtyList
     * @param soOrgIdList
     * @return java.util.List<com.erp.model.oms.dto.SoPriceDetailDTO.SoTaxPriceBatchViewDTO>
     */
    List<SoPriceDetailDTO.SoTaxPriceBatchViewDTO> batchGetTaxPrice(List<String> skuIdList, List<String> customerIdList, List<Integer> soQtyList, List<String> soOrgIdList);
}
