package com.erp.server.tms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.AdvanceQueryContainer;
import com.erp.model.tms.dto.LogisticsBillDTO;
import com.erp.model.tms.dto.TmsFirstMileLogisticDTO;
import com.erp.model.tms.dto.TmsFirstMileReconciliationDetailDTO;
import com.erp.model.tms.entity.LogisticsBillEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
     * 根据来源查询物流信息及跟踪号
     * @Author hyj
     * @Date 2024/05/11 14:58
     * @param trackNoList
     * @return java.util.List<com.erp.model.tms.dto.LogisticsBillDTO.LogisticsBillVo>
     **/
    List<LogisticsBillDTO.LogisticsBillVo> listLogisticsBillVoByTrackNo(@Param("trackNoList") List<String> trackNoList);

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
    IPage<LogisticsBillDTO.PagingVO> paging(Page query,@Param("params")LogisticsBillDTO.PagingParamDTO params);
    /**
     * 导出
     *@parms dto
     *@return 
     *@author yl
     *@date 2023-11-16
     */
    List<LogisticsBillDTO.PagingVO> listExport(@Param("params")LogisticsBillDTO.PagingParamDTO dto);
    Page<LogisticsBillDTO.PagingVO> listExport(@Param("page") Page<LogisticsBillDTO.PagingVO> page, @Param("params")LogisticsBillDTO.PagingParamDTO dto);

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

    List<TmsFirstMileLogisticDTO.TabListDTO> firstMileTabList(@Param("orderType") String orderType,@Param("permissionSql") String permissionSql);

    List<TmsFirstMileLogisticDTO.LogisticStatisticsDTO> statistics(@Param("params") TmsFirstMileLogisticDTO.LogisticStatisticsReq logisticStatisticsReq,@Param("permissionSql") String permissionSql);

    TmsFirstMileLogisticDTO.StatisticsVO.ReconciliationStatistics reconciliationStatistics(@Param("orderType") String orderType,@Param("permissionSql") String permissionSql);

    List<TmsFirstMileLogisticDTO.OverdueDTO> overdueStatistics(@Param("orderType") String code,@Param("permissionSql") String permissionSql);

    List<TmsFirstMileLogisticDTO.ExportCostDTO> firstMileFeeCostExport(@Param("params") TmsFirstMileLogisticDTO.PagingParamDTO pagingParamDTO);

    List<TmsFirstMileLogisticDTO.PagingVO> hasWarnPaging(@Param("params") TmsFirstMileLogisticDTO.PagingParamDTO dto);

    IPage<TmsFirstMileReconciliationDetailDTO.ListDTO> waitReconciliationPaging(Page<?> query,
                                                                                @Param("params") TmsFirstMileReconciliationDetailDTO.PagingParamDTO params,
                                                                                @Param("orderType") String orderType,
                                                                                @Param("reconciliationStatus") String reconciliationStatus,
                                                                                @Param("trackStatus") String trackStatus
    );

    List<TmsFirstMileReconciliationDetailDTO.ListDTO> waitReconciliationList(@Param("orderType") String orderType,
                                                                             @Param("reconciliationStatus") String reconciliationStatus,
                                                                             @Param("trackStatus") String trackStatus,
                                                                             @Param("mainIds") List<String> mainIds,
                                                                             @Param("transportNoList") List<String> transportNoList,
                                                                             @Param("logisticsSupplierIdList") List<String> logisticsSupplierIdList,
                                                                              LocalDate startDate, LocalDate endDate);
    /**
     * 根据物流跟踪单号或运单号查询物流单详情
     * @author will
     * @date 2024/7/3 18:03
     * @param logisticsCode
     * @return BaseDTO
     */
    LogisticsBillDTO.BaseDTO getByTrackNoOrTransportNo(@Param("logisticsCode") String logisticsCode);

    List<TmsFirstMileLogisticDTO.WeightAllocationDTO> assembleFirstMileEstimatedList(@Param("ids") List<String> ids);

    List<LogisticsBillEntity> queryToSdy(@Param("startTime") LocalDateTime startTime, @Param("endTime")LocalDateTime endTime, @Param("pageSize")Integer pageSize, @Param("offset")int offset);
}
