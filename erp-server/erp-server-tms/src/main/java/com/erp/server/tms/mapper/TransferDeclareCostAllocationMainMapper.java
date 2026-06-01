package com.erp.server.tms.mapper;
import com.erp.model.tms.entity.TransferDeclareCostAllocationMainEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * <p>
 * 中转费用分摊主表 Mapper 接口
 * </p>
 *
 * @author shukai
 * @since 2024-12-07
 */
@Mapper
public interface TransferDeclareCostAllocationMainMapper extends BaseMapper<TransferDeclareCostAllocationMainEntity> {

    List<String> pageMainIdsByReportPeriodStr(@Param("reportPeriodStr") String reportPeriodStr,
                                              @Param("reportStatus") String reportStatus,
                                              @Param("excludeBigTableDone") boolean excludeBigTableDone,
                                              @Param("bigTableDoneCode") String bigTableDoneCode,
                                              @Param("lastId") String lastId,
                                              @Param("batchSize") int batchSize);

    Integer countMainByReportPeriodStr(@Param("reportPeriodStr") String reportPeriodStr,
                                       @Param("reportStatus") String reportStatus,
                                       @Param("excludeBigTableDone") boolean excludeBigTableDone,
                                       @Param("bigTableDoneCode") String bigTableDoneCode);

    List<String> pageMainIdsForReAllocation(@Param("reportPeriodStr") String reportPeriodStr,
                                            @Param("reportStatus") String reportStatus,
                                            @Param("lastId") String lastId,
                                            @Param("batchSize") int batchSize);

    Integer countMainForReAllocation(@Param("reportPeriodStr") String reportPeriodStr,
                                     @Param("reportStatus") String reportStatus);

}
