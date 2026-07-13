package com.erp.server.dmp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.dmp.entity.DmpLogisticsTrackWebhookRecordEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;

@Mapper
public interface DmpLogisticsTrackWebhookRecordMapper extends BaseMapper<DmpLogisticsTrackWebhookRecordEntity> {

    int skipCoveredRecords(@Param("platformCode") String platformCode,
                           @Param("timeoutMinutes") Integer timeoutMinutes,
                           @Param("remark") String remark);

    int recoverTimeoutIngRecords(@Param("platformCode") String platformCode,
                                 @Param("timeoutMinutes") Integer timeoutMinutes,
                                 @Param("remark") String remark);

    List<DmpLogisticsTrackWebhookRecordEntity> claimLatestWaitRecords(@Param("platformCode") String platformCode,
                                                                      @Param("limit") Integer limit);

    int updateStatusByIds(@Param("ids") Collection<String> ids,
                          @Param("platformCode") String platformCode,
                          @Param("fromStatus") String fromStatus,
                          @Param("status") String status,
                          @Param("remark") String remark);
}
