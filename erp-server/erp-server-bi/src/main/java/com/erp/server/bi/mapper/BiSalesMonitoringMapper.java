package com.erp.server.bi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.bi.dto.BiSalesMonitoringTableDTO;
import com.erp.model.bi.entity.BiSalesMonitoringEntity;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2022/12/29 16:25
 */
@Mapper
public interface BiSalesMonitoringMapper extends BaseMapper<BiSalesMonitoringEntity> {
    List<BiSalesMonitoringTableDTO> listBiSalesMonitoringTable();
}
