package com.erp.server.bi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.bi.entity.BiSalesMonitoringEntity;
import com.erp.model.bi.vo.BiSalesMonitoringTableVO;
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
     * 上个月销量（销售额）
     */
    List<BiSalesMonitoringTableVO> listSumSecondMonthSale(@Param("type") Integer type);
    /**
     * 本月销量（销售额）
     */
    List<BiSalesMonitoringTableVO> listSumFirstMonthSale(@Param("type") Integer type);
    /**
     * 全年销量（销售额）
     */
    List<BiSalesMonitoringTableVO> listSumYearSale(@Param("type") Integer type);
}
