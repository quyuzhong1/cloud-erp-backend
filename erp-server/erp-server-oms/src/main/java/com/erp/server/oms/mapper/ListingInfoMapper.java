package com.erp.server.oms.mapper;

import com.erp.model.oms.dto.ListingInfoDTO;
import com.erp.model.oms.dto.SkuMappingDTO;
import com.erp.model.oms.entity.ListingInfoEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 对应平台sku 表 Mapper 接口
 * </p>
 *
 * @author Lambda
 * @since 2023-08-18
 */
@Mapper
public interface ListingInfoMapper extends BaseMapper<ListingInfoEntity> {

    List<ListingInfoDTO.ListDTO> listByType(@Param("type") String type);

    /**
     * 根据erp产品sku查询映射关系
     * @Author Luo_WG
     * @Date 2023/11/28 11:49
     * @param skuIdList
     * @return java.util.List<com.erp.model.oms.dto.SkuMappingDTO.listMappingSkuDTO>
     **/
    List<SkuMappingDTO.MappingSkuViewDTO> listMappingSkuByParam(@Param("skuIdList") List<String> skuIdList);
}
