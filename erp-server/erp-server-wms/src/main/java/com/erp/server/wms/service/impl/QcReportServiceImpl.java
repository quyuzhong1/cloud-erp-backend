package com.erp.server.wms.service.impl;

import com.common.business.service.SuperServiceImpl;
import com.common.core.utils.BeanMapper;
import com.erp.model.wms.dto.QcReportDTO;
import com.erp.model.wms.entity.QcReportEntity;
import com.erp.server.wms.mapper.QcReportMapper;
import com.erp.server.wms.service.QcReportService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 质检报告 服务实现类
 * </p>
 *
 * @author lambda
 * @since 2023-04-13
 */
@Service
public class QcReportServiceImpl extends SuperServiceImpl<QcReportMapper, QcReportEntity> implements QcReportService {


    /**
     * 添加质检报告
     *
     * @param ruleId
     * @param reportList
     * @return void
     * @author yl
     * @date 2023-04-13 10:31
     */
    @Override
    public void addQcReport(String ruleId, List<QcReportDTO.AddDTO> reportList) {
        if (CollectionUtils.isEmpty(reportList)) {
            return;
        }
        List<QcReportEntity> list = BeanMapper.copyList(reportList, QcReportEntity.class);
        list.forEach(r -> r.setQcRuleId(ruleId));
        this.saveBatch(list);
    }
}
