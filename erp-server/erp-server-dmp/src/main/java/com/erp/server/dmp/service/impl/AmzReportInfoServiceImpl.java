package com.erp.server.dmp.service.impl;


import com.common.business.service.impl.SuperServiceImpl;
import com.erp.model.dmp.entity.AmzReportInfoEntity;
import com.erp.server.dmp.mapper.AmzReportInfoMapper;
import com.erp.server.dmp.service.AmzReportInfoService;
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
public class AmzReportInfoServiceImpl extends SuperServiceImpl<AmzReportInfoMapper, AmzReportInfoEntity> implements AmzReportInfoService {

    @Override
    public AmzReportInfoEntity getByReportId(String reportId, String processingStatus) {
        return lambdaQuery()
                .eq(AmzReportInfoEntity::getReportId, reportId)
                .eq(StringUtils.isNotBlank(processingStatus), AmzReportInfoEntity::getProcessingStatus, processingStatus)
                .last("LIMIT 1")
                .one();
    }
}
