package com.erp.server.wms.service.impl;

import com.common.business.service.SuperServiceImpl;
import com.common.core.utils.BeanMapper;
import com.erp.model.wms.dto.QcReportDetailDTO;
import com.erp.model.wms.entity.DictBasicEntity;
import com.erp.model.wms.entity.QcReportDetailEntity;
import com.erp.model.wms.entity.QcReportEntity;
import com.erp.server.wms.mapper.QcReportDetailMapper;
import com.erp.server.wms.service.DictBasicService;
import com.erp.server.wms.service.QcReportDetailService;
import com.erp.server.wms.service.QcReportService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author lambda
 * @since 2023-04-14
 */
@Service
public class QcReportDetailServiceImpl extends SuperServiceImpl<QcReportDetailMapper, QcReportDetailEntity> implements QcReportDetailService {


    @Resource
    private QcReportService qcReportService;

    @Resource
    private DictBasicService dictBasicService;

    /**
     * 质检报告明细暂存
     *
     * @param billId
     * @param reportDetailList
     * @return void
     * @author yl
     * @date 2023-04-19 11:14
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void draft(String billId, List<QcReportDetailDTO.AddDTO> reportDetailList) {
        if (CollectionUtils.isEmpty(reportDetailList)) {
            return;
        }
        List<QcReportDetailEntity> dbList = this.findByMainId(billId);
        List<QcReportDetailEntity> saveOrUpdateList = BeanMapper.copyList(reportDetailList, QcReportDetailEntity.class);
        saveOrUpdateList.stream().forEach(s -> s.setMainId(billId));
        //获取到删除的id
        List<String> deleteIdList = getDeleteIds(reportDetailList, dbList);
        if (CollectionUtils.isNotEmpty(deleteIdList)) {
            this.removeByIds(deleteIdList);
        }
        this.saveOrUpdateBatch(saveOrUpdateList);
    }


    /**
     * 根据质检单id获取 质检报告明细信息
     *
     * @param billId
     * @return java.util.List<com.erp.model.wms.dto.QcReportDetailDTO.ViewDTO>
     * @author yl
     * @date 2023-04-19 12:34
     */
    @Override
    public List<QcReportDetailDTO.ViewDTO> getByMainId(String billId) {
        List<QcReportDetailEntity> list = this.findByMainId(billId);
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyList();
        }
        List<QcReportDetailDTO.ViewDTO> viewList = BeanMapper.copyList(list, QcReportDetailDTO.ViewDTO.class);
        //质检报告的id 集合
        List<String> qcReportIds = viewList.stream().map(QcReportDetailDTO.ViewDTO::getQcReportId).collect(Collectors.toList());
        List<QcReportEntity> qcReportList = qcReportService.listByIds(qcReportIds);

        for (QcReportDetailDTO.ViewDTO item : viewList) {
            QcReportEntity report = qcReportList.stream().filter(q -> q.getId().equals(item.getQcReportId())).
                    findFirst().orElse(null);
            if (report != null) {
                item.setQcReportName(report.getName());
                item.setQcReportContent(report.getContent());
            }
            List<DictBasicEntity> dictList = dictBasicService.getByKeyList(new ArrayList<>());
            String resultDict = item.getResultDict();
            String resultName=dictList.stream().filter(d -> d.getValue().equals(resultDict)).
                    findFirst().flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
            item.setResultName(resultName);
        }

        return viewList;
    }


    /**
     * 根据主表id 获取数据
     *
     * @param billId
     * @return java.util.List<com.erp.model.wms.entity.QcReportDetailEntity>
     * @author yl
     * @date 2023-04-19 11:20
     */
    private List<QcReportDetailEntity> findByMainId(String billId) {
        return lambdaQuery().eq(QcReportDetailEntity::getMainId, billId).list();
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
    private List<String> getDeleteIds(List<QcReportDetailDTO.AddDTO> list, List<QcReportDetailEntity> dbList) {
        List<String> ids = list.stream().filter(g -> StringUtils.isNotBlank(g.getId())).
                map(QcReportDetailDTO.AddDTO::getId).collect(Collectors.toList());
        List<String> dbIds = dbList.stream().map(QcReportDetailEntity::getId).collect(Collectors.toList());
        return dbIds.stream().filter(s -> !ids.contains(s)).collect(Collectors.toList());
    }
}
