package com.erp.server.dmp.service.impl;


import com.common.business.service.impl.SuperServiceImpl;
import com.erp.model.dmp.entity.DmpAmzReportInfoEntity;
import com.erp.server.dmp.mapper.DmpAmzReportInfoMapper;
import com.erp.server.dmp.service.DmpAmzReportInfoService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
/**
 * <p>
 * 亚马逊报告请求记录 服务实现类
 * </p>
 *
 * @author Jim
 * @since 2024-01-18
 */
@Slf4j
@Service
public class DmpAmzReportInfoServiceImpl extends SuperServiceImpl<DmpAmzReportInfoMapper, DmpAmzReportInfoEntity> implements DmpAmzReportInfoService {

    @Override
    public DmpAmzReportInfoEntity getByReportId(String reportId, String processingStatus) {
        return lambdaQuery()
                .eq(DmpAmzReportInfoEntity::getReportId, reportId)
                .eq(StringUtils.isNotBlank(processingStatus), DmpAmzReportInfoEntity::getProcessingStatus, processingStatus)
                .last("LIMIT 1")
                .one();
    }
}
