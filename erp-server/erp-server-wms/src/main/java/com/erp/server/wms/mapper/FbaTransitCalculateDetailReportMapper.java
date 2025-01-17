package com.erp.server.wms.mapper;

import com.erp.model.wms.entity.FbaTransitCalculateDetailReportEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;


/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author zdy
 * @since 2024-12-12
 */
@Mapper
public interface FbaTransitCalculateDetailReportMapper extends BaseMapper<FbaTransitCalculateDetailReportEntity> {
    /**
     * 查询在途货件数据列表
     * @param reportMonth
     * @param shipmentCode
     * @param asin
     * @param msku
     * @return
     */
    List<FbaTransitCalculateDetailReportEntity> listTransitDetail(@Param("reportMonth") LocalDate reportMonth, @Param("shipmentCode") String shipmentCode, @Param("asin") String asin, @Param("msku") String msku);
}
