package com.erp.server.oms.service;

import com.erp.model.oms.entity.CfgInvoiceSettingDetailEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.oms.dto.CfgInvoiceSettingDetailDTO;
import com.erp.model.oms.entity.SoB2cEntity;

import java.util.List;

/**
 * <p>
 * 发票设置明细 服务类
 * </p>
 *
 * @author hcg
 * @since 2025-04-09
 */
public interface CfgInvoiceSettingDetailService extends SuperService<CfgInvoiceSettingDetailEntity> {

    CfgInvoiceSettingDetailDTO.ViewDTO view(CfgInvoiceSettingDetailDTO.ViewParamsDTO dto);

    BaseResultDTO.AddDTO addOrUpdate(CfgInvoiceSettingDetailDTO.AddOrUpdateDTO dto);

    /**
     * 根据店铺id集合查询
     * @author will
     * @date 2025/4/9 12:20
     * @param shopIdList
     * @return List<CfgInvoiceSettingDetailEntity>
     */
    List<CfgInvoiceSettingDetailEntity> listByShopIdList(List<String> shopIdList);

    List<CfgInvoiceSettingDetailDTO.ViewShopDTO> listShopSelect(String dictplatform);
    /**
     * 查询启用数据
     * @author will
     * @date 2025/4/14 14:10
     * @param dictPlatform
     * @param shopId
     * @return CfgInvoiceSettingDetailEntity
     */
    CfgInvoiceSettingDetailEntity getInvoiceSettingDetail(String dictPlatform, String shopId);
    List<CfgInvoiceSettingDetailEntity> listInvoiceSettingDetail(String dictPlatform, String shopId);

    List<CfgInvoiceSettingDetailDTO.ViewDictPlatformDTO> listDictSelect(CfgInvoiceSettingDetailDTO.ParamsDictPlatformDTO dto);

      /**
        * @description: 级联删除绑定店铺明细
        * @author: hcg
        * @date: 2025/4/18 9:41
        * @param ids
        * @param aTrue
        * @return: void
        **/
    void delateByMainIds(List<String> ids, Boolean aTrue);

    /**
     * 生成发票
     * @param soB2cEntity
     * @param type
     */
    void generateNfeInvoice (SoB2cEntity soB2cEntity, String type);
}
