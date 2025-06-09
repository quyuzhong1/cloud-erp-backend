package com.erp.server.mrp.service.impl;


import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.mrp.dto.CfgRuleLogisticsDetailDTO;
import com.erp.model.mrp.entity.CfgRuleLogisticsDetailEntity;
import com.erp.model.oms.enums.ShopAuthTypeEnum;
import com.erp.server.mrp.mapper.CfgRuleLogisticsDetailMapper;
import com.erp.server.mrp.service.CfgRuleLogisticsDetailService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
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

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(List<CfgRuleLogisticsDetailDTO.UpdateDTO> detailList,String mainId) {
        if (CollectionUtils.isEmpty(detailList)) {
            detailList = Collections.emptyList();
        }
        List<CfgRuleLogisticsDetailEntity> list = BeanMapperUtils.copyList(CfgRuleLogisticsDetailEntity.class, detailList);
        //原物流信息
        List<CfgRuleLogisticsDetailEntity> oldList = listByMainIdList(Arrays.asList(mainId));
        //删除明细
        List<String> deleteIds = getDeleteIds(list, oldList);
        if (CollectionUtils.isNotEmpty(deleteIds)) {
            this.removeByIds(deleteIds);
        }
        if (CollectionUtils.isEmpty(list)) {
            return  Boolean.TRUE;
        }
        // 数据处理
        handleData(list,mainId);
        log.info("编辑 开始修改备货物流明细（规则设置）数据，id：【{}】", mainId);
        boolean save = super.saveOrUpdateBatch(list);
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
    @Override
    public List<CfgRuleLogisticsDetailEntity> listByMainIdList(List<String> mainIdList) {
        if (CollectionUtils.isEmpty(mainIdList)) {
            return Collections.emptyList();
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

            if (CharSequenceUtil.equals(detailEntity.getType(), ShopAuthTypeEnum.ENUM_PART.getCode()) && ObjectUtil.isEmpty(detailEntity.getShopIdList())) {
                throw new ServiceException("指定店铺时店铺不能为空");
            }
            //主表id
            detailEntity.setMainId(mainId);
            //店铺id
            JSONArray shopIdJson = JSONUtil.parseArray(detailEntity.getShopIdList());
            detailEntity.setShopIdJson(shopIdJson);
        }
    }
}
