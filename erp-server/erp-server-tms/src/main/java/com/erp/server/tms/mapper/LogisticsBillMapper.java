package com.erp.server.tms.mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.AdvanceQueryContainer;
import com.erp.model.tms.dto.LogisticsBillDTO;
import com.erp.model.tms.dto.TmsFirstMileLogisticDTO;
import com.erp.model.tms.entity.LogisticsBillEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * <p>
 * 物流单 Mapper 接口
 * </p>
 *
 * @author lambda
 * @since 2023-11-09
 */
@Mapper
public interface LogisticsBillMapper extends BaseMapper<LogisticsBillEntity> {

    /**
     * 根据来源id查询物流信息及跟踪号
     * @Author Luo_WG
     * @Date 2023/11/10 9:08
     * @param sourceIdList
     * @return java.util.List<com.erp.model.tms.dto.LogisticsBillDTO.LogisticsBillVo>
     **/
    List<LogisticsBillDTO.LogisticsBillVo> listLogisticsBillVoBySourceIds(@Param("sourceIdList") List<String> sourceIdList);

    /**
     * 统计tab
     *@parms permissionSql
     *@return 
     *@author yl
     *@date 2023-11-15
     */
    List<LogisticsBillDTO.TabListDTO> tabList(@Param("permissionSql") String permissionSql);

    /**
     * 分页
     *@parms params
     *@return
     *@author yl
     *@date 2023-11-16
     */
    IPage<LogisticsBillDTO.PagingVO> paging(Page query, @Param("params")LogisticsBillDTO.PagingParamDTO params,@Param("statusList") List<String> statusList);

    /**
     * 导出
     *@parms dto
     *@return 
     *@author yl
     *@date 2023-11-16
     */
    List<LogisticsBillDTO.PagingVO> listExport(@Param("params")LogisticsBillDTO.ExportDTO dto,@Param("statusList") List<String> statusList);

    /**
     * 获取物流单基础信息 根据跟踪号
     * @param trackNo
     * @return
     */
    LogisticsBillDTO.BaseDTO getBaseByTrackNo(@Param("trackNo") String trackNo);

    /**
     * 根据物流跟踪单号查询物流单详情
     * @Author Luo_WG
     * @Date 2023/12/14 15:45
     * @param transportNoList
     * @return com.erp.model.tms.dto.LogisticsBillDTO.BaseDTO
     **/
    List<LogisticsBillDTO.BaseDTO> listLogisticsBillByTransportNos(@Param("transportNoList") List<String> transportNoList);

    List<String> listSoOutIdByQuery(@Param("params") AdvanceQueryContainer advanceQueryContainer);

    IPage<TmsFirstMileLogisticDTO.PagingVO> firstMilePaging(Page query, @Param("params") TmsFirstMileLogisticDTO.PagingParamDTO params);

    TmsFirstMileLogisticDTO.ViewDTO firstMileView(@Param("id") String id);

    List<TmsFirstMileLogisticDTO.TabListDTO> firstMileTabList(String orderType);

    List<TmsFirstMileLogisticDTO.LogisticStatisticsDTO> statistics(TmsFirstMileLogisticDTO.LogisticStatisticsReq logisticStatisticsReq);

    TmsFirstMileLogisticDTO.StatisticsVO.ReconciliationStatistics reconciliationStatistics(String orderType);

    List<TmsFirstMileLogisticDTO.OverdueDTO> overdueStatistics(String code);

    List<TmsFirstMileLogisticDTO.ExportCostDTO> firstMileFeeCostExport(@Param("params") TmsFirstMileLogisticDTO.PagingParamDTO pagingParamDTO);

    List<TmsFirstMileLogisticDTO.PagingVO> hasWarnPaging(@Param("params") TmsFirstMileLogisticDTO.PagingParamDTO dto);
}
