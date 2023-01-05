package com.erp.server.bi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.bi.dto.BiSalesMonitoringSearchDTO;
import com.erp.model.bi.dto.BiSalesMonitoringTableDTO;
import com.erp.model.bi.entity.BiSalesMonitoringEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2022/12/29 16:25
 */
@Mapper
public interface BiSalesMonitoringMapper extends BaseMapper<BiSalesMonitoringEntity> {
    /**
     * @description: 查询订单数据
     * @author Will
     * @date: 2023/1/5 10:11
     * @param dto
     * @return List<BiSalesMonitoringTableDTO>
     */
    List<BiSalesMonitoringTableDTO> listBiSalesMonitoringTable(@Param("dto") BiSalesMonitoringSearchDTO dto);
}
