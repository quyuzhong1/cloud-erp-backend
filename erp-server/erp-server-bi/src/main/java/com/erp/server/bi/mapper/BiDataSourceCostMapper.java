package com.erp.server.bi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.bi.dto.BiDataSourceCostSearchDTO;
import com.erp.model.bi.dto.BiFilterDTO;
import com.erp.model.bi.dto.CompletionRateRankingDTO;
import com.erp.model.bi.entity.BiDataSourceCostEntity;
import com.erp.model.bi.vo.DateCostVO;
import com.erp.model.bi.vo.DeptCostVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author Will
 * @version 1.0

 * @date 2022/12/14 16:38
 */
@Mapper
public interface BiDataSourceCostMapper extends BaseMapper<BiDataSourceCostEntity> {
    /**
     * @description: 分页查询
     * @author Will
     * @date: 2022/12/14 16:44
     * @param query
     * @param params
     * @return IPage<BiDataSourceCostDTO>
     */
    IPage<LinkedHashMap<String,Object>> paging(Page<Object> query, @Param("params") BiDataSourceCostSearchDTO params);
    /**
     * @description: 查询所有成本
     * @author Will
     * @date: 2022/12/16 15:54
     * @param params
     * @return List<LinkedHashMap<Object>>
     */
    List<LinkedHashMap<String,Object>> getAllBiDataSourceCost(@Param("params") BiDataSourceCostSearchDTO params);

    /**
     * 成本分析部门和成本类型
     *
     * @param month
     * @param dto
     * @param dictValues
     * @param groupName
     * @return
     */
    List<DeptCostVO> sumByDeptAndCostType(@Param("month") LocalDateTime month, @Param("params") BiFilterDTO dto, @Param("dictValues") List<String> dictValues,@Param("groupName") String groupName);
    /**
     * 成本分析部门和成本类型
     *
     * @param dto
     * @param dictValues
     * @return
     */
    List<DateCostVO> sumByDateAndCostType(@Param("params") BiFilterDTO dto, @Param("dictValues") List<String> dictValues);

    /**
     * 部门销售额排行榜
     * @param dto
     * @param settleRate
     * @return
     */
    List<CompletionRateRankingDTO.PagingDTO> deptCompletionRateRanking(@Param("params") CompletionRateRankingDTO.SearchDTO dto, @Param("settleRate") String settleRate);

    /**
     * 用户销售额排行榜
     * @param dto
     * @param settleRate
     * @return
     */
    List<CompletionRateRankingDTO.PagingDTO> userCompletionRateRanking(@Param("params") CompletionRateRankingDTO.SearchDTO dto, @Param("settleRate") String settleRate);
}
