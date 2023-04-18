package com.erp.server.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.wms.dto.QcEffectivenessDTO;
import com.erp.model.wms.entity.QcBillEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * <p>
 * 质检单表 Mapper 接口
 * </p>
 *
 * @author lambda
 * @since 2023-04-14
 */
@Mapper
public interface QcBillMapper extends BaseMapper<QcBillEntity> {
    /**
     * @description: 质检总览查询
     * @author Will
     * @date: 2023/4/17 18:35
     * @param params
     * @return List<ViewQcOverviewDetailDTO>
     */
    List<QcEffectivenessDTO.ViewQcOverviewDetailDTO> listQcBillGroupQcStatus(@Param("params") QcEffectivenessDTO.CommonSearchParamDTO params);
    /**
     * @description: 按日期查询质检趋势
     * @author Will
     * @date: 2023/4/18 12:31
     * @param beginDate
     * @param endDate 
     * @return List<GroupQcTrendDTO> 
     */
    List<QcEffectivenessDTO.GroupQcTrendDTO> listQcBillGroupQcTrend(@Param("type")String type, @Param("beginDate")LocalDate beginDate,@Param("endDate") LocalDate endDate);

}
