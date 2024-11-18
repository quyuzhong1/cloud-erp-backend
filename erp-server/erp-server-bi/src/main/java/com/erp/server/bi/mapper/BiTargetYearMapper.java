package com.erp.server.bi.mapper;
import com.erp.model.bi.dto.BiTargetYearDTO;
import com.erp.model.bi.entity.BiTargetYearEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * <p>
 * 年度目标表 Mapper 接口
 * </p>
 *
 * @author Lambda
 * @since 2023-09-13
 */
@Mapper
public interface BiTargetYearMapper extends BaseMapper<BiTargetYearEntity> {

    /**
     * 根据年获取到所有的目标设置值
     * @param year
     * @return
     */
    List<BiTargetYearDTO.YearMonthValueDTO> allTargetValue(@Param("year") Integer year, @Param("metrics")String  metrics,@Param("metricsName") String metricsName);

    
    /**
     * 获取部门 年月目标值
     * @author yl
     * @date 2023-09-18 14:50
     * @param year
     * @param metrics
     * @param deptIdList
     * @return java.util.List<com.erp.model.bi.dto.BiTargetYearDTO.YearMonthValueDTO>
     */
    List<BiTargetYearDTO.YearMonthValueDTO> listDeptTargetValue(@Param("year")Integer year,@Param("metrics") String metrics,@Param("metricsName") String metricsName,@Param("deptIdList") List<String> deptIdList);


    /**
     * 获取员个目标值
     * @param year
     * @param metrics
     * @param staffIdList
     * @return
     */
    List<BiTargetYearDTO.YearMonthValueDTO> listStaffTargetValue(@Param("year")Integer year, @Param("metrics")String metrics,@Param("metricsName") String metricsName,@Param("staffIdList") List<String> staffIdList);

    /**
     * 获取店铺 目标值
     * @param year
     * @param metrics
     * @param shopIdList
     * @return
     */
    List<BiTargetYearDTO.YearMonthValueDTO> listShopTargetValue(@Param("year")Integer year, @Param("metrics")String metrics,@Param("metricsName") String metricsName,@Param("shopIdList") List<String> shopIdList);
}
