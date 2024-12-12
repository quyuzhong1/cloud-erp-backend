package com.erp.server.wms.mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.wms.dto.FirstMileDeliveryDetailDTO;
import com.erp.model.wms.entity.FirstMileDeliveryDetailEntity;
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
     * 根据明细id找来源
     * @author will
     * @date 2024/11/20 18:02
     * @param firstMileDetailIdList
     * @return List<listFirstMileDTO>
     */
    List<FirstMileDeliveryDetailDTO.listFirstMileDTO> listFirstMileSource(@Param("firstMileDetailIdList")List<String> firstMileDetailIdList);

//    /**
//     * 根据主表id分组sku查询发货及待装箱数
//     * @Author Luo_WG
//     * @Date 2023/11/28 18:40
//     * @param mainId
//     * @return java.util.List<com.erp.model.wms.dto.FirstMileDeliveryDTO.GroupSkuDTO>
//     **/
//    List<FirstMileDeliveryDTO.GroupSkuDTO> listGroupSkuByMainId(@Param("mainId") String mainId);

//    /**
//     * 根据主表id分组查询发货单已包装发货及待装箱数
//     * @Author Luo_WG
//     * @Date 2023/11/30 11:30
//     * @param sourceId
//     * @return java.util.List<com.erp.model.wms.dto.FirstMileDeliveryDTO.GroupSkuDTO>
//     **/
//    List<FirstMileDeliveryDTO.GroupSkuDTO> listCartonGroupSkuBySourceId(@Param("sourceId") String sourceId, @Param("boxSpecNo") Integer boxSpecNo);

}
