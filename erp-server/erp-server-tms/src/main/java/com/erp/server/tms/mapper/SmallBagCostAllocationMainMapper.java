package com.erp.server.tms.mapper;
import com.erp.model.tms.entity.SmallBagCostAllocationMainEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * <p>
 * 小包费用分摊主表 Mapper 接口
 * </p>
 *
 * @author shukai
 * @since 2024-12-05
 */
@Mapper
public interface SmallBagCostAllocationMainMapper extends BaseMapper<SmallBagCostAllocationMainEntity> {

    /**
     * 游标分批查询指定核算月份的主表ID（直查主表，前置过滤无效记录）
     *
     * @param reportPeriodStr     核算期间 yyyy-MM
     * @param reportStatus        目标核算状态，非空时过滤 report_status != reportStatus
     * @param excludeBigTableDone 是否排除大表已生成（目标为待确认时为 true）
     * @param bigTableDoneCode    大表已生成状态码
     * @param lastId              游标：上一批最后一条 id（首次传空字符串）
     * @param batchSize           每批条数
     * @return 当前批次的主表ID列表
     */
    List<String> pageMainIdsByReportPeriodStr(@Param("reportPeriodStr") String reportPeriodStr,
                                              @Param("reportStatus") String reportStatus,
                                              @Param("excludeBigTableDone") boolean excludeBigTableDone,
                                              @Param("bigTableDoneCode") String bigTableDoneCode,
                                              @Param("lastId") String lastId,
                                              @Param("batchSize") int batchSize);

    /**
     * 统计指定核算月份可更新核算状态的主表记录数（与游标查询同过滤条件）
     */
    Integer countMainByReportPeriodStr(@Param("reportPeriodStr") String reportPeriodStr,
                                       @Param("reportStatus") String reportStatus,
                                       @Param("excludeBigTableDone") boolean excludeBigTableDone,
                                       @Param("bigTableDoneCode") String bigTableDoneCode);

    /**
     * 游标分批查询指定核算月份待重新分摊的主表ID（report_status 等值匹配）
     */
    List<String> pageMainIdsForReAllocation(@Param("reportPeriodStr") String reportPeriodStr,
                                            @Param("reportStatus") String reportStatus,
                                            @Param("lastId") String lastId,
                                            @Param("batchSize") int batchSize);

    /**
     * 统计指定核算月份待重新分摊的主表记录数
     */
    Integer countMainForReAllocation(@Param("reportPeriodStr") String reportPeriodStr,
                                     @Param("reportStatus") String reportStatus);

}
