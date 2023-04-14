package com.erp.server.wms.service.impl;

import com.common.business.service.SuperServiceImpl;
import com.common.core.utils.BeanMapper;
import com.erp.model.wms.dto.QcReportDTO;
import com.erp.model.wms.entity.QcReportEntity;
import com.erp.server.wms.mapper.QcReportMapper;
import com.erp.server.wms.service.QcReportService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

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


    /**
     * 根据质检规则id获取质检报告信息
     *
     * @param qcRuleId
     * @return java.util.List<com.erp.model.wms.dto.QcReportDTO.UpdateDTO>
     * @author yl
     * @date 2023-04-13 14:26
     */
    @Override
    public List<QcReportDTO.UpdateDTO> getByQcRuleId(String qcRuleId) {
        List<QcReportEntity> list = findByRuleId(qcRuleId);
        return BeanMapper.copyList(list, QcReportDTO.UpdateDTO.class);
    }

    /**
     * 修改质检报告
     *
     * @param qcRuleId
     * @param qcReportLList
     * @return void
     * @author yl
     * @date 2023-04-13 14:53
     */
    @Override
    public void updateQcReport(String qcRuleId, List<QcReportDTO.UpdateDTO> qcReportLList) {
        if (CollectionUtils.isEmpty(qcReportLList)) {
            return;
        }
        //这是要添加的
        List<QcReportDTO.UpdateDTO> addList = qcReportLList.stream().filter(c -> StringUtils.isBlank(c.getId())).collect(Collectors.toList());

        List<QcReportEntity> dbList = this.findByRuleId(qcRuleId);

        List<QcReportEntity> saveOrUpdateList = BeanMapper.copyList(qcReportLList, QcReportEntity.class);
        saveOrUpdateList.stream().forEach(s->s.setQcRuleId(qcRuleId));
        //获取到删除的id
        List<String> deleteIdList = getDeleteIds(addList, dbList);
        if (CollectionUtils.isNotEmpty(deleteIdList)) {
            this.removeByIds(deleteIdList);
        }
        this.saveOrUpdateBatch(saveOrUpdateList);
    }

    
    /**
     * 获取要删除的id 集合
     * @author yl
     * @date 2023-04-13 14:59
     * @param list
     * @param dbList
     * @return java.util.List<java.lang.String>
     */
    private List<String> getDeleteIds(List<QcReportDTO.UpdateDTO> list, List<QcReportEntity> dbList) {
        List<String> ids = list.stream().filter(g -> StringUtils.isNotBlank(g.getId())).
                map(QcReportDTO.UpdateDTO::getId).collect(Collectors.toList());
        List<String> dbIds = dbList.stream().map(QcReportEntity::getId).collect(Collectors.toList());
       return dbIds.stream().filter(s -> !ids.contains(s)).collect(Collectors.toList());
    }


    private List<QcReportEntity> findByRuleId(String qcRuleId) {
        return lambdaQuery().eq(QcReportEntity::getQcRuleId, qcRuleId).list();

    }
}
