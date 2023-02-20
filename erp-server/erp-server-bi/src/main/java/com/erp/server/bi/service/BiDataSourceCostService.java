package com.erp.server.bi.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.common.business.dto.base.PagingDTO;
import com.erp.common.business.vo.PagingVO;
import com.erp.model.bi.dto.BiDataSourceCostSearchDTO;
import com.erp.model.bi.dto.BiFilterDTO;
import com.erp.model.bi.vo.*;
import com.erp.model.bi.entity.BiDataSourceCostEntity;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2022/12/14 16:37
 */
public interface BiDataSourceCostService
        extends IService<BiDataSourceCostEntity> {
    /**
     * @description: 分页查询
     * @author Will
     * @date: 2022/12/14 16:43
     * @param dto
     * @return PagingVO<LinkedHashMap<String,Object>>
     */
    PagingVO<LinkedHashMap<String,Object>> paging(PagingDTO<BiDataSourceCostSearchDTO> dto);

    /**
     * 统计销售毛利润
     * @param dto
     * @return
     */
    TargetSaleSumVO sumSalesProfit(BiFilterDTO dto);

    /**
     * 统计毛利率
     * @param dto
     * @return
     */
    TargetSaleSumVO sumSalesRatio(BiFilterDTO dto);

    /**
     * 统计主营收入
     * @param dto
     * @return
     */
    TargetSaleSumVO sumMainRevenue(BiFilterDTO dto);

    /**
     * 销售成本统计
     * @param dto
     * @return
     */
    TargetSaleSumVO sumSalesCost(BiFilterDTO dto);

    /**
     * 导出
     */
    void exportExcel(BiDataSourceCostSearchDTO dto, HttpServletResponse response);

    /**
     * 导入
     */
    Boolean importExcel(MultipartFile excelFile, HttpServletResponse response);
    /**
     * @description: 编辑
     * @author Will
     * @date: 2022/12/21 17:02
     * @param list
     */
    void updateBiDataSourceCost(List<LinkedHashMap<String, Object>> list);

    /**
     * 根据成本参数查询
     */
    BiDataSourceCostEntity getByCostParam(BiDataSourceCostEntity entity);

    /**
     * 获取部分成本利率分析图形
     * @param dto
     * @return
     */
    List<SeriesVO> getDeptCostProfit(BiFilterDTO dto);

    /**
     * 获取最新月份
     * @return
     */
    BiDataSourceCostEntity getMaxMonth();

    /**
     * 根据指定条件汇总成本数据
     * @param dto
     * @return
     */
    List<DeptCostVO> sumCostByCondition(BiFilterDTO dto, String groupName);

    /**
     * 获取利润top20% 店铺
     * @param dto
     * @return
     */
    List<SeriesVO> getShopCostProfit(BiFilterDTO dto);

    /**
     * 获取利润top20的平台
     * @param dto
     * @return
     */
    List<SeriesVO> getPlatformCostProfit(BiFilterDTO dto);

    /**
     * 获取平台毛利率占比分析
     * @param dto
     * @return
     */
    List<PieChartVO> getPlatformCostPercent(BiFilterDTO dto);

    /**
     * 月度成本利润分析
     * @param dto
     * @return
     */
    List<SeriesVO> getMonthCostProfit(BiFilterDTO dto);

    /**
     * 季度成本分析
     * @param dto
     * @return
     */
    List<SeriesVO> getQuarterCostProfit(BiFilterDTO dto);

    /**
     * 年度成本分析
     * @param dto
     * @return
     */
    List<SeriesVO> getYearCostProfit(BiFilterDTO dto);

    /**
     * 获取事业部成本利润分析排名
     * @param dto
     * @return
     */
    List<CostProfitAnalyzeRankVO> getDeptCostProfitRank(BiFilterDTO dto);

    /**
     * 获取平台成本利润分析排名
     * @param dto
     * @return
     */
    List<CostProfitAnalyzeRankVO> getPlatformCostProfitRank(BiFilterDTO dto);

    /**
     * 获取站点成本利润分析排名
     * @param dto
     * @return
     */
    List<CostProfitAnalyzeRankVO> getShopCostProfitRank(BiFilterDTO dto);

    /**
     * 获取人员成本利润分析排名
     * @param dto
     * @return
     */
    List<CostProfitAnalyzeRankVO> getUserCostProfitRank(BiFilterDTO dto);
}
