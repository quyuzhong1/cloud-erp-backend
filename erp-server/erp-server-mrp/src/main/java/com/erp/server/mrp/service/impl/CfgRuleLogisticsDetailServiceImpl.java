package com.erp.server.mrp.service.impl;


import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.mrp.dto.CfgRuleLogisticsDetailDTO;
import com.erp.model.mrp.entity.CfgRuleLogisticsDetailEntity;
import com.erp.server.mrp.mapper.CfgRuleLogisticsDetailMapper;
import com.erp.server.mrp.service.CfgRuleLogisticsDetailService;
import com.erp.server.mrp.service.OperateLogService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 备货物流明细（规则设置） 服务实现类
 * </p>
 *
 * @author will
 * @since 2024-08-23
 */
@Slf4j
@Service
public class CfgRuleLogisticsDetailServiceImpl extends SuperServiceImpl<CfgRuleLogisticsDetailMapper, CfgRuleLogisticsDetailEntity> implements CfgRuleLogisticsDetailService {
    @Autowired
    private OperateLogService operateLogService;

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(List<CfgRuleLogisticsDetailDTO.UpdateDTO> detailList,String mainId) {
        if (CollectionUtils.isEmpty(detailList)) {
            detailList = Collections.EMPTY_LIST;
        }
        List<CfgRuleLogisticsDetailEntity> list = BeanMapperUtils.copyList(CfgRuleLogisticsDetailEntity.class, detailList);
        //原物流信息
        List<CfgRuleLogisticsDetailEntity> oldList = listByMainIdList(Arrays.asList(mainId));
        //删除明细
        List<String> deleteIds = getDeleteIds(list, oldList);
        if (CollectionUtils.isNotEmpty(deleteIds)) {
            this.removeByIds(deleteIds);
        }
        // 数据处理
        handleData(list,mainId);
        log.info("编辑 开始修改备货物流明细（规则设置）数据，id：【{}】", mainId);
        boolean save = super.updateBatchById(list);
        if(!save) {
            throw new ServiceException("备货物流明细（规则设置）保存失败");
        }
        return Boolean.TRUE;
    }

    @Override
    public void deleteByMainIdList(List<String> mainIdList) {
        if (CollectionUtils.isEmpty(mainIdList)) {
            return;
        }
        lambdaUpdate().in(CfgRuleLogisticsDetailEntity::getMainId,mainIdList).remove();
    }

    /**
     * 查询需要删除的数据
     */
    private List<String> getDeleteIds(List<CfgRuleLogisticsDetailEntity> newList, List<CfgRuleLogisticsDetailEntity> oldList) {
        List<String> newIds = newList.stream().filter(g -> StringUtils.isNotBlank(g.getId())).
                map(CfgRuleLogisticsDetailEntity::getId).collect(Collectors.toList());
        List<String> oldIds = oldList.stream().map(CfgRuleLogisticsDetailEntity::getId).collect(Collectors.toList());
        return oldIds.stream().filter(s -> !newIds.contains(s)).collect(Collectors.toList());
    }

    /**
     * 根据主表id集合查询
     * @author will
     * @date 2024/8/27 11:50
     * @param mainIdList
     * @return List<CfgRuleLogisticsDetailEntity>
     */
    private List<CfgRuleLogisticsDetailEntity> listByMainIdList(List<String> mainIdList) {
        if (CollectionUtils.isEmpty(mainIdList)) {
            return Collections.EMPTY_LIST;
        }
       return lambdaQuery().in(CfgRuleLogisticsDetailEntity::getMainId,mainIdList).list();
    }

    /**
    * 新增修改处理数据
    */
    private void handleData(List<CfgRuleLogisticsDetailEntity> list,String mainId) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        for (CfgRuleLogisticsDetailEntity detailEntity : list) {
            //主表id
            detailEntity.setMainId(mainId);
            //店铺id
            JSONArray shopIdJson = JSONUtil.parseArray(detailEntity.getShopIdList());
            detailEntity.setShopIdJson(shopIdJson);
        }
    }
}
