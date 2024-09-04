package com.erp.server.mrp.service.impl;


import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.mrp.dto.CfgRuleLogisticsDTO;
import com.erp.model.mrp.dto.CfgRuleLogisticsDetailDTO;
import com.erp.model.mrp.dto.CfgRuleStockUpDTO;
import com.erp.model.mrp.entity.CfgRuleLogisticsDetailEntity;
import com.erp.model.mrp.entity.CfgRuleLogisticsEntity;
import com.erp.model.mrp.enums.CfgRulePlatformTypeEnum;
import com.erp.model.oms.enums.ShopAuthTypeEnum;
import com.erp.model.wms.enums.LogisticsMethodEnum;
import com.erp.server.mrp.mapper.CfgRuleLogisticsMapper;
import com.erp.server.mrp.service.CfgRuleLogisticsDetailService;
import com.erp.server.mrp.service.CfgRuleLogisticsService;
import com.erp.server.mrp.service.OperateLogService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
/**
 * <p>
 * 备货物流（规则设置） 服务实现类
 * </p>
 *
 * @author will
 * @since 2024-08-23
 */
@Slf4j
@Service
public class CfgRuleLogisticsServiceImpl extends SuperServiceImpl<CfgRuleLogisticsMapper, CfgRuleLogisticsEntity> implements CfgRuleLogisticsService {
    @Autowired
    private OperateLogService operateLogService;

    @Autowired
    private CfgRuleLogisticsDetailService cfgRuleLogisticsDetailService;

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(List<CfgRuleLogisticsDTO.UpdateDTO> logisticsList,String stockUpId,Boolean isCustom) {
        if (CollectionUtils.isEmpty(logisticsList)) {
            logisticsList = Collections.EMPTY_LIST;
        }
        List<CfgRuleLogisticsEntity> list = BeanMapperUtils.copyList(CfgRuleLogisticsEntity.class, logisticsList);
        //自定义更新无需删除
        if (!isCustom) {
            //原物流信息
            List<CfgRuleLogisticsEntity> oldList = listByStockUpIdList(Arrays.asList(stockUpId));
            //删除明细
            List<String> deleteIds = getDeleteIds(list, oldList);
            if (CollectionUtils.isNotEmpty(deleteIds)) {
                this.deleteByIdList(deleteIds);
            }
        }
        if (CollectionUtils.isEmpty(list)) {
            return  Boolean.TRUE;
        }
        // 数据处理
        handleData(list,stockUpId);
        log.info("编辑 开始修改备货物流（规则设置）数据，id：【{}】", stockUpId);
        boolean save = super.saveOrUpdateBatch(list);
        if(!save) {
            throw new ServiceException("备货物流（规则设置）保存失败");
        }

        //更新物流明细信息
        list.stream().forEach(obj -> cfgRuleLogisticsDetailService.update(obj.getDetailList(),obj.getId()));
        return Boolean.TRUE;
    }

    @Override
    public List<CfgRuleLogisticsEntity> listByStockUpIdList (List<String> stockUpIdList) {
        if (CollectionUtils.isEmpty(stockUpIdList)) {
            return Collections.EMPTY_LIST;
        }
       return lambdaQuery().in(CfgRuleLogisticsEntity::getStockUpId,stockUpIdList).list();
    }

    @Override
    public List<CfgRuleLogisticsDTO.ViewDTO> listViewByStockUpIdList (List<String> stockUpIdList) {
        if (CollectionUtils.isEmpty(stockUpIdList)) {
            return Collections.EMPTY_LIST;
        }
        List<CfgRuleLogisticsEntity> list = lambdaQuery().in(CfgRuleLogisticsEntity::getStockUpId, stockUpIdList).list();
        if (CollectionUtils.isEmpty(list)) {
            return Collections.EMPTY_LIST;
        }
        List<CfgRuleLogisticsDTO.ViewDTO> cfgLogisticsViewList = BeanMapperUtils.copyList(CfgRuleLogisticsDTO.ViewDTO.class, list);
        //物流配置明细
        List<String> mainIdList = cfgLogisticsViewList.stream().map(CfgRuleLogisticsDTO.ViewDTO::getId).distinct().collect(Collectors.toList());
        List<CfgRuleLogisticsDetailEntity> cfgRuleLogisticsDetailList = cfgRuleLogisticsDetailService.listByMainIdList(mainIdList);
        if (CollectionUtils.isEmpty(cfgRuleLogisticsDetailList)) {
            return cfgLogisticsViewList;
        }
        for (CfgRuleLogisticsDTO.ViewDTO viewDTO : cfgLogisticsViewList) {
            List<CfgRuleLogisticsDetailEntity> detailList = cfgRuleLogisticsDetailList.stream().filter(obj -> StrUtil.equals(obj.getMainId(), viewDTO.getId())).collect(Collectors.toList());
            viewDTO.setLogisticsMethodName(LogisticsMethodEnum.getName(viewDTO.getLogisticsMethod()));
            if (CollectionUtils.isNotEmpty(detailList)) {
                List<CfgRuleLogisticsDetailDTO.ViewDTO> cfgLogisticsDetailViewList = BeanMapperUtils.copyList(CfgRuleLogisticsDetailDTO.ViewDTO.class, detailList);
                for (CfgRuleLogisticsDetailDTO.ViewDTO detailViewDTO : cfgLogisticsDetailViewList) {
                    List<String> shopIdList = JSONUtil.parseArray(detailViewDTO.getShopIdJson()).stream().filter(obj -> ObjectUtil.isNotEmpty(obj))
                            .map(obj -> obj.toString()).collect(Collectors.toList());
                    detailViewDTO.setShopIdList(shopIdList);
                }
                viewDTO.setDetailList(cfgLogisticsDetailViewList);
            }
        }
        return cfgLogisticsViewList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByStockUpId(String stockUpId) {
        List<CfgRuleLogisticsEntity> cfgRuleLogisticsList = listByStockUpIdList(Arrays.asList(stockUpId));
        if (CollectionUtils.isEmpty(cfgRuleLogisticsList)) {
            return;
        }
        //根据id删除
        List<String> idList = cfgRuleLogisticsList.stream().map(CfgRuleLogisticsEntity::getId).distinct().collect(Collectors.toList());
        deleteByIdList(idList);
    }

    @Override
    public CfgRuleLogisticsDTO.LogisticsResultDTO getLogisticsMaxPriority(String stockUpId, CfgRuleStockUpDTO.StrategyDTO dto) {
        // 获取外层最高优先级数据
        CfgRuleLogisticsEntity entity = getOne(Wrappers.<CfgRuleLogisticsEntity>lambdaQuery()
                .eq(CfgRuleLogisticsEntity::getStockUpId, stockUpId)
                .orderByAsc(CfgRuleLogisticsEntity::getIndex)
                .last("LIMIT 1")
        );
        if (ObjectUtil.isEmpty(entity)) {
            return null;
        }
        //获取对应明细数据
        List<CfgRuleLogisticsDetailEntity> cfgRuleLogisticsDetails = cfgRuleLogisticsDetailService.listByMainIdList(Collections.singletonList(entity.getId()));
        CfgRuleLogisticsDetailEntity detail = null;
        //amazon 取值店铺
        if (CfgRulePlatformTypeEnum.AMAZON.getCode().equals(dto.getPlatformType())) {
            //先获取区域加店铺 获取不到则获取区域加全部店铺 再获取不到则取外层数据
            detail = cfgRuleLogisticsDetails.stream()
                    .filter(v -> v.getArea().equals(dto.getArea()))
                    .filter(v -> v.getShopIdJson().contains(dto.getShopId()))
                    .findFirst().orElseGet(() -> cfgRuleLogisticsDetails.stream().filter(v -> v.getArea().equals(dto.getArea()))
                            .filter(v -> v.getType().equals(ShopAuthTypeEnum.ENUM_ALL.getCode()))
                            .findFirst().orElse(null)
                    );
        } else if (CfgRulePlatformTypeEnum.OVERSEAS.getCode().equals(dto.getPlatformType())) {
            //先获取海外仓 再获取不到则取外层数据
            detail = cfgRuleLogisticsDetails.stream()
                    .filter(v -> v.getWarehouseId().equals(dto.getWarehouseId()))
                    .findFirst().orElse(null);
        }
        CfgRuleLogisticsDTO.LogisticsResultDTO resultDTO = new CfgRuleLogisticsDTO.LogisticsResultDTO();
        if (ObjectUtils.isEmpty(detail)) {
            resultDTO.buildLogisticsResult(entity);
        }else {
            resultDTO.buildLogisticsResult(detail, entity);
        }
        return resultDTO;
    }

    /**
     * 根据id删除
     * @author will
     * @date 2024/8/27 11:37
     * @param idList
     */
    private void deleteByIdList (List<String> idList) {
        if (CollectionUtils.isEmpty(idList)) {
            return;
        }
        //删除物流信息
        this.removeByIds(idList);

        //删除物流明细数据
        cfgRuleLogisticsDetailService.deleteByMainIdList(idList);
    }


    /**
     * 查询需要删除的数据
     */
    private List<String> getDeleteIds(List<CfgRuleLogisticsEntity> newList, List<CfgRuleLogisticsEntity> oldList) {
        List<String> newIds = newList.stream().filter(g -> StringUtils.isNotBlank(g.getId())).
                map(CfgRuleLogisticsEntity::getId).collect(Collectors.toList());
        List<String> oldIds = oldList.stream().map(CfgRuleLogisticsEntity::getId).collect(Collectors.toList());
        return oldIds.stream().filter(s -> !newIds.contains(s)).collect(Collectors.toList());
    }

    /**
    * 新增修改处理数据
    */
    private void handleData(List<CfgRuleLogisticsEntity> list,String stockUpId) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        for (CfgRuleLogisticsEntity logisticsEntity : list) {
            //备货主表id
            logisticsEntity.setStockUpId(stockUpId);
        }
    }
}
