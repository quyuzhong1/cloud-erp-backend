package com.erp.server.wms.mapper;
import com.erp.model.wms.dto.FirstMileDeliveryDTO;
import com.erp.model.wms.entity.FirstMileDeliveryDetailEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * <p>
 * 头程发货单明细表 Mapper 接口
 * </p>
 *
 * @author Luo_WG
 * @since 2023-10-30
 */
@Mapper
public interface FirstMileDeliveryDetailMapper extends BaseMapper<FirstMileDeliveryDetailEntity> {

    /**
     * 根据来源详情id查询发货详情
     * @Author Luo_WG
     * @Date 2023/11/15 20:12
     * @param sourceDetailIds
     * @return java.util.List<com.erp.model.wms.entity.FbaDeliveryDetailEntity>
     **/
    List<FirstMileDeliveryDetailEntity> listBySourceDetailIds(@Param("sourceDetailIds") List<String> sourceDetailIds);

    /**
     * 根据主表id分组sku查询发货及待装箱数
     * @Author Luo_WG
     * @Date 2023/11/28 18:40
     * @param mainIds
     * @return java.util.List<com.erp.model.wms.dto.FirstMileDeliveryDTO.GroupSkuDTO>
     **/
    List<FirstMileDeliveryDTO.GroupSkuDTO> listGroupSkuByMainIds(@Param("mainIds") List<String> mainIds);

    /**
     * 根据主表id分组查询发货单已包装发货及待装箱数
     * @Author Luo_WG
     * @Date 2023/11/30 11:30
     * @param mainIds
     * @return java.util.List<com.erp.model.wms.dto.FirstMileDeliveryDTO.GroupSkuDTO>
     **/
    List<FirstMileDeliveryDTO.GroupSkuDTO> listCartonGroupSkuByMainIds(@Param("mainIds") List<String> mainIds);
}
