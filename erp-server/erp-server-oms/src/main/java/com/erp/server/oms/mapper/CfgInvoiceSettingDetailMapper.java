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
     * 根据店铺id集合查询
     * @author will
     * @date 2025/4/9 12:20
     * @param shopIdList
     * @return List<CfgInvoiceSettingDetailEntity>
     */
    List<CfgInvoiceSettingDetailEntity> listByShopIdList(@Param("shopIdList") List<String> shopIdList);
    /**
     * 查询启用数据
     * @author will
     * @date 2025/4/14 14:10
     * @param dictPlatform
     * @param shopId
     * @return CfgInvoiceSettingDetailEntity
     */
    List<CfgInvoiceSettingDetailEntity> getInvoiceSettingDetail(@Param("dictPlatform")String dictPlatform,@Param("shopId") String shopId);


    /**
     * 查询发票设置详情，按平台分组，并在SQL中处理ratio乘以100
     * @param mainId 主ID
     * @param dictKey 字典Key
     * @param platformValues 平台值列表
     * @return 按平台分组的详情列表
     */
    CfgInvoiceSettingDetailDTO.ViewDTO selectDetailsByMainIdGroupByPlatformWithRatioAdjusted(
            @Param("mainId") String mainId,
            @Param("dictKey") String dictKey,
            @Param("platformValues") List<String> platformValues
    );
}
