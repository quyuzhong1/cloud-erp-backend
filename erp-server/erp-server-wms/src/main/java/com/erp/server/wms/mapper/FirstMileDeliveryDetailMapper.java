package com.erp.server.wms.mapper;
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
}
