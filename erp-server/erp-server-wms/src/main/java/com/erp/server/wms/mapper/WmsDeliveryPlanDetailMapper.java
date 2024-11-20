package com.erp.server.wms.mapper;
import com.erp.model.wms.dto.ReportOrderDataDTO;
import com.erp.model.wms.entity.WmsDeliveryPlanDetailEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;

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
     * 查询虚拟仓报表数据
     * @author will
     * @date 2024/11/20 10:35
     * @return List<ViewDTO>
     */
    List<ReportOrderDataDTO.ViewDTO> ListAllVirtualDeliveryPlanDetail();
}
