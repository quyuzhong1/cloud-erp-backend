package com.erp.server.wms.mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.wms.entity.WmsDeliveryPlanDetailEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * <p>
 * 发货计划详情表 Mapper 接口
 * </p>
 *
 * @author Luo_WG
 * @since 2023-11-16
 */
@Mapper
public interface WmsDeliveryPlanDetailMapper extends BaseMapper<WmsDeliveryPlanDetailEntity> {
    /**
     * 根据来源id查询
     * @author will
     * @date 2024/10/23 10:02
     * @param idList
     * @return List<WmsDeliveryPlanDetailEntity>
     */
    List<WmsDeliveryPlanDetailEntity> listBySourceIdList(@Param("idList") List<String> idList);
}
