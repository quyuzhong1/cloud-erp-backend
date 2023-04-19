package com.erp.server.wms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.service.SuperServiceImpl;
import com.common.core.utils.BeanMapper;
import com.erp.model.wms.dto.QcReportDTO;
import com.erp.model.wms.dto.QcReportDetailDTO;
import com.erp.model.wms.entity.QcReportEntity;
import com.erp.server.wms.mapper.QcReportMapper;
import com.erp.server.wms.service.QcReportService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    @Transactional(rollbackFor = Exception.class)
    public void updateQcReport(String qcRuleId, List<QcReportDTO.UpdateDTO> qcReportLList) {
        if (CollectionUtils.isEmpty(qcReportLList)) {
            return;
        }
        List<QcReportEntity> dbList = this.findByRuleId(qcRuleId);
        List<QcReportEntity> saveOrUpdateList = BeanMapper.copyList(qcReportLList, QcReportEntity.class);
        saveOrUpdateList.stream().forEach(s -> s.setQcRuleId(qcRuleId));
        //获取到删除的id
        List<String> deleteIdList = getDeleteIds(qcReportLList, dbList);
        if (CollectionUtils.isNotEmpty(deleteIdList)) {
            this.removeByIds(deleteIdList);
        }
        this.saveOrUpdateBatch(saveOrUpdateList);
    }


    /**
     * 根据质检类型 获取待 报告明细
     *
     * @param qcType
     * @return java.util.List<com.erp.model.wms.dto.QcReportDTO.ListDTO>
     * @author yl
     * @date 2023-04-18 16:01
     */
    @Override
    public List<QcReportDTO.ListDTO> getByQcType(String qcType) {
        String approveStatus = ApproveStatusEnum.APPROVE.getStatus();

        return baseMapper.getByQcType(qcType, approveStatus);
    }


    /**
     * 根据质检规则 删除质检报告
     *
     * @param ruleIds
     * @return void
     * @author yl
     * @date 2023-04-18 16:27
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeByRuleIds(List<String> ruleIds) {
        if (CollectionUtils.isNotEmpty(ruleIds)) {
            LambdaQueryWrapper<QcReportEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.in(QcReportEntity::getQcRuleId, ruleIds);
            this.remove(queryWrapper);
        }

    }

    @Override
    public void draft(String billId, List<QcReportDetailDTO.AddDTO> reportDetailList) {

    }


    /**
     * 获取要删除的id 集合
     *
     * @param list
     * @param dbList
     * @return java.util.List<java.lang.String>
     * @author yl
     * @date 2023-04-13 14:59
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
