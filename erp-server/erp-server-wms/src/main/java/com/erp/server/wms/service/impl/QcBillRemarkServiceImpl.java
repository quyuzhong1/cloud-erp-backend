package com.erp.server.wms.service.impl;

import com.common.business.service.SuperServiceImpl;
import com.common.core.utils.BeanMapper;
import com.erp.model.wms.dto.QcRemarkDTO;
import com.erp.model.wms.entity.QcBillRemarkEntity;
import com.erp.server.wms.mapper.QcBillRemarkMapper;
import com.erp.server.wms.service.QcBillRemarkService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 质检单备注表 服务实现类
 * </p>
 *
 * @author lambda
 * @since 2023-04-14
 */
@Service
public class QcBillRemarkServiceImpl extends SuperServiceImpl<QcBillRemarkMapper, QcBillRemarkEntity> implements QcBillRemarkService {


    /**
     * 质检备注 暂存
     *
     * @param billId
     * @param remarkList
     * @return void
     * @author yl
     * @date 2023-04-19 11:23
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(String billId, List<QcRemarkDTO.AddDTO> remarkList) {
        if (CollectionUtils.isEmpty(remarkList)) {
            return;
        }
        List<QcBillRemarkEntity> dbList = this.findByMainId(billId);
        List<QcBillRemarkEntity> saveOrUpdateList = BeanMapper.copyList(remarkList, QcBillRemarkEntity.class);
        saveOrUpdateList.stream().forEach(s -> s.setMainId(billId));
        //获取到删除的id
        List<String> deleteIdList = getDeleteIds(remarkList, dbList);
        if (CollectionUtils.isNotEmpty(deleteIdList)) {
            this.removeByIds(deleteIdList);
        }
        this.saveOrUpdateBatch(saveOrUpdateList);
    }


    /**
     * 获取到质检备注的信息
     *
     * @param billId
     * @return java.util.List<com.erp.model.wms.dto.QcRemarkDTO.AddDTO>
     * @author yl
     * @date 2023-04-19 14:01
     */
    @Override
    public List<QcRemarkDTO.AddDTO> getByMainId(String billId) {
        List<QcBillRemarkEntity> list = this.findByMainId(billId);
        List<QcRemarkDTO.AddDTO> resultList = BeanMapper.copyList(list, QcRemarkDTO.AddDTO.class);
        return resultList;
    }


    /**
     * 根据质检单id集合 查询备注信息
     *
     * @param billIdList
     * @return java.util.List<com.erp.model.wms.entity.QcBillRemarkEntity>
     * @author yl
     * @date 2023-04-19 19:31
     */
    @Override
    public List<QcBillRemarkEntity> getByMainIdList(List<String> billIdList) {
        if (CollectionUtils.isEmpty(billIdList)) {
            return Collections.emptyList();
        }

        return this.lambdaQuery().in(QcBillRemarkEntity::getMainId,billIdList).orderByDesc(QcBillRemarkEntity::getCreateTime).list();
    }


    /**
     * 获取到删除id
     *
     * @param remarkList
     * @param dbList
     * @return java.util.List<java.lang.String>
     * @author yl
     * @date 2023-04-19 11:27
     */
    private List<String> getDeleteIds(List<QcRemarkDTO.AddDTO> remarkList, List<QcBillRemarkEntity> dbList) {
        List<String> ids = remarkList.stream().filter(g -> StringUtils.isNotBlank(g.getId())).
                map(QcRemarkDTO.AddDTO::getId).collect(Collectors.toList());
        List<String> dbIds = dbList.stream().map(QcBillRemarkEntity::getId).collect(Collectors.toList());
        return dbIds.stream().filter(s -> !ids.contains(s)).collect(Collectors.toList());
    }


    /**
     * 根据主表id 获取对应数据
     *
     * @param billId
     * @return
     */
    private List<QcBillRemarkEntity> findByMainId(String billId) {
        return lambdaQuery().eq(QcBillRemarkEntity::getMainId, billId).list();
    }
}
