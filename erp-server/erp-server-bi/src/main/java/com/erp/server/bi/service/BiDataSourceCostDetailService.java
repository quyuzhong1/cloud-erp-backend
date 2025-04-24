package com.erp.server.bi.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.model.bi.dto.BiDataSourceCostDTO;
import com.erp.model.bi.dto.BiFilterDTO;
import com.erp.model.bi.entity.BiDataSourceCostDetailEntity;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Will
 * @version 1.0

 * @date 2022/12/15 18:14
 */
public interface BiDataSourceCostDetailService  extends IService<BiDataSourceCostDetailEntity> {
    /**
     * 根据costId 查询记录，并进行行列转换
     *
     * @param costIds
     * @param dictValues
     * @return
     */
    HashMap<String, Map<String, BigDecimal>> convertListByCostIds(List<String> costIds, List<String> dictValues);
    /**
     * @description:
     * @author Will
     * @date: 2022/12/16 15:14
     * @param costIds
     * @return List<BiDataSourceCostDetailEntity>
     */
    List<BiDataSourceCostDetailEntity> listByCostIds(List<String> costIds);
    /**
     * @description: 根据成本id删除明细
     * @author Will
     * @date: 2022/12/27 14:06
     * @param costId
     */
    void removeByCostId(String costId);

    /**
     * 获取成本根据类型
     * @param costType
     * @return
     */
    BigDecimal yearByCostType(String year,String costType,BiFilterDTO dto);

    /**
     * 获取月度的
     * @param yearMonthStr
     * @param costType
     * @param dto
     * @return
     */
    BigDecimal monthByCostType(String yearMonthStr, String costType, BiFilterDTO dto);

    /**
     * 获取月份的目标值
     * @param dto
     * @return
     */
    List<BiDataSourceCostDTO.DataValueDTO> listGrossMonth(BiFilterDTO dto);

    /**
     * 获取年度毛利值
     * @author yl
     * @date 2023-09-22 14:39
     * @param dto
     * @return java.util.List<com.erp.model.bi.dto.BiDataSourceCostDTO.DataValueDTO>
     */
    List<BiDataSourceCostDTO.DataValueDTO> listGrossYear(BiDataSourceCostDTO.GrossProfitDTO dto);

    /**
     * 获取季度值
     * @param dto
     * @return
     */
    List<BiDataSourceCostDTO.DataValueDTO> listGrossQuarter(BiDataSourceCostDTO.GrossProfitDTO dto);
}
