package com.erp.server.oms.service;
import com.common.business.dto.PlatformOrderDTO;
import com.erp.model.oms.dto.ListingInfoWithSkuMappingDTO;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.oms.entity.SoMultiChannelDetailEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.oms.dto.SoMultiChannelDetailDTO;
import com.erp.model.oms.entity.SoMultiChannelEntity;
import com.erp.model.plm.vo.SkuInfoSimpleVO;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 多渠道订单明细表 服务类
 * </p>
 *
 * @author Jim
 * @since 2024-05-30
 */
public interface SoMultiChannelDetailService extends SuperService<SoMultiChannelDetailEntity> {

    /**
    * 保存或更新
    * @author Jim
    * @date: 2024-05-30
    * @param dto DTO
    * @return 明细列表
    */

    List<SoMultiChannelDetailEntity> saveOrUpdateEntity(PlatformOrderDTO dto, SoMultiChannelEntity mainEntity, Map<String, List<ListingInfoWithSkuMappingDTO>> listingInfoWithSkuMappingDTOMap, ShopInfoEntity shopInfo, List<SkuInfoSimpleVO> skuList);


    /**
     * 通过主键ID查询列表
     * @author Jim
     * @date: 2024-05-30
     * @param mainId 主键ID
     * @return 明细列表
     */
    List<SoMultiChannelDetailEntity> listByMainId(String mainId);
}
