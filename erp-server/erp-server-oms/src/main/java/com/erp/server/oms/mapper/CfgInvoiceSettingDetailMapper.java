package com.erp.server.oms.mapper;
import com.erp.model.oms.dto.CfgInvoiceSettingDetailDTO;
import com.erp.model.oms.entity.CfgInvoiceSettingDetailEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * <p>
 * 发票设置明细 Mapper 接口
 * </p>
 *
 * @author hcg
 * @since 2025-04-09
 */
@Mapper
public interface CfgInvoiceSettingDetailMapper extends BaseMapper<CfgInvoiceSettingDetailEntity> {
    /**
      * @description:查询发票设置明细关联的店铺（name、value）
      * @author: hcg
      * @date: 2025/4/9 14:46
      * @param:
      * @return: List<CfgInvoiceSettingDetailDTO.ViewDetailShop>
      **/
    List<CfgInvoiceSettingDetailDTO.ViewDetailShop> selectDetailShop();
    /**
      * @description:根据主键id查询明细，根据平台value关联查询平台name
      * @author: hcg
      * @date: 2025/4/9 14:47
      * @param: mainId
      * @return: List<CfgInvoiceSettingDetailDTO.ViewDTO>
      **/
    List<CfgInvoiceSettingDetailDTO.ViewDTO> selectDetailDict(String mainId);
    /**
     * 根据店铺id集合查询
     * @author will
     * @date 2025/4/9 12:20
     * @param shopIdList
     * @return List<CfgInvoiceSettingDetailEntity>
     */
    List<CfgInvoiceSettingDetailEntity> listByShopIdList(@Param("shopIdList") List<String> shopIdList);
}
