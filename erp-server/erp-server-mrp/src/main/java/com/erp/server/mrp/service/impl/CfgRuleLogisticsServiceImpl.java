package com.erp.server.mrp.service.impl;


import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.wrapper.FeignQuery;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.MathUtil;
import com.erp.model.mrp.dto.CfgRuleLogisticsDTO;
import com.erp.model.mrp.dto.CfgRuleLogisticsDetailDTO;
import com.erp.model.mrp.entity.CfgRuleLogisticsDetailEntity;
import com.erp.model.mrp.entity.CfgRuleLogisticsEntity;
import com.erp.model.mrp.entity.CfgRuleStockUpEntity;
import com.erp.model.mrp.enums.CfgRulePlatformTypeEnum;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.oms.enums.ShopAuthTypeEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.enums.LogisticsMethodEnum;
import com.erp.server.mrp.mapper.CfgRuleLogisticsMapper;
import com.erp.server.mrp.service.CfgRuleLogisticsDetailService;
import com.erp.server.mrp.service.CfgRuleLogisticsService;
import com.erp.server.mrp.service.CfgRuleStockUpService;
import com.erp.server.mrp.service.OperateLogService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
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

    @Autowired
    private CfgRuleStockUpService cfgRuleStockUpService;
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
        //原物流信息
        List<CfgRuleLogisticsEntity> oldList = listByStockUpIdList(Arrays.asList(stockUpId));
        //自定义更新无需删除
        if (!isCustom) {
            //删除明细
            List<String> deleteIds = getDeleteIds(list, oldList);
            if (CollectionUtils.isNotEmpty(deleteIds)) {
                this.deleteByIdList(deleteIds);
                // 数据处理
                oldList = oldList.stream().filter(obj -> !deleteIds.contains(obj.getId())).collect(Collectors.toList());
            }
        }
        if (CollectionUtils.isEmpty(list)) {
            return  Boolean.TRUE;
        }
        // 数据处理
        handleData(list,stockUpId,oldList,isCustom);
        log.info("编辑 开始修改备货物流（规则设置）数据，id：【{}】", stockUpId);
        boolean save = super.saveOrUpdateBatch(list);
        if(!save) {
            throw new ServiceException("备货物流（规则设置）保存失败");
        }
        //更新物流明细信息
        list.stream().forEach(obj -> cfgRuleLogisticsDetailService.update(obj.getDetailList(),obj.getId()));

        //店铺
        List<ShopInfoEntity> shopInfoList = FeignQuery.list(ShopInfoEntity.class);
        //备货信息
        CfgRuleStockUpEntity stockUpEntity = cfgRuleStockUpService.getById(stockUpId);
        if (ObjectUtil.isEmpty(stockUpEntity)) {
            throw new ServiceException(ApiError.NOT_EXIST_BILL,"规则设置（备货）");
        }
        //日志
        StringBuffer msg = new StringBuffer();
        String title = "本地发FBA：";
        if (StrUtil.equals(stockUpEntity.getPlatformType(), CfgRulePlatformTypeEnum.OVERSEAS.getCode())) {
            title = "本地发海外：";
        }
        msg.append( StrUtil.format("{}<br>",title));
        for (CfgRuleLogisticsEntity logisticsEntity : list) {
            String parentMsg = StrUtil.format("物流方式【{}】、物流时效【{}】、发货频率【{}】<br>",LogisticsMethodEnum.getName(logisticsEntity.getLogisticsMethod()),logisticsEntity.getLogisticsDays(),logisticsEntity.getLogisticsCycleDays());
            msg.append(parentMsg);
            List<CfgRuleLogisticsDetailDTO.UpdateDTO> detailList = logisticsEntity.getDetailList();
            if (CollectionUtils.isEmpty(detailList)) {
                continue;
            }
            for (CfgRuleLogisticsDetailDTO.UpdateDTO updateDTO : detailList) {
                String shopNames = CollectionUtils.isEmpty(updateDTO.getShopIdList()) ? "" : shopInfoList.stream().filter(obj -> updateDTO.getShopIdList().contains(obj.getId())).map(ShopInfoEntity::getName).distinct().collect(Collectors.joining(","));
                String childMsg = StrUtil.format("•区域【{}】、店铺【{}】、时效【{}】<br>", updateDTO.getArea(),StrUtil.equals(ShopAuthTypeEnum.ENUM_ALL.getCode(),updateDTO.getType()) ? "全部店铺": shopNames, updateDTO.getLogisticsDays());
                msg.append(childMsg);
            }
        }
        operateLogService.addModuleOperateLog(msg.toString(), ModuleTypeEnum.REPLENISHMENT_SUGGESTION.getCode(), StrUtil.blankToDefault(stockUpEntity.getRefId(),stockUpEntity.getId()) , "备货");
        return Boolean.TRUE;
    }

    @Override
    public List<CfgRuleLogisticsEntity> listByStockUpIdList (List<String> stockUpIdList) {
        if (CollectionUtils.isEmpty(stockUpIdList)) {
            return Collections.EMPTY_LIST;
        }
       return lambdaQuery().in(CfgRuleLogisticsEntity::getStockUpId,stockUpIdList).orderByAsc(CfgRuleLogisticsEntity::getIndex).list();
    }

    @Override
    public List<CfgRuleLogisticsDTO.ViewDTO> listViewByStockUpIdList (List<String> stockUpIdList) {
        if (CollectionUtils.isEmpty(stockUpIdList)) {
            return Collections.EMPTY_LIST;
        }
        List<CfgRuleLogisticsEntity> list = lambdaQuery().in(CfgRuleLogisticsEntity::getStockUpId, stockUpIdList).orderByAsc(CfgRuleLogisticsEntity::getIndex).list();
        if (CollectionUtils.isEmpty(list)) {
            return Collections.EMPTY_LIST;
        }
        List<CfgRuleLogisticsDTO.ViewDTO> cfgLogisticsViewList = BeanMapperUtils.copyList(CfgRuleLogisticsDTO.ViewDTO.class, list);
        //物流配置明细
        List<String> mainIdList = cfgLogisticsViewList.stream().map(CfgRuleLogisticsDTO.ViewDTO::getId).distinct().collect(Collectors.toList());
        List<CfgRuleLogisticsDetailEntity> cfgRuleLogisticsDetailList = cfgRuleLogisticsDetailService.listByMainIdList(mainIdList);
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
    private void handleData(List<CfgRuleLogisticsEntity> list,String stockUpId,List<CfgRuleLogisticsEntity> oldList,Boolean isCustom) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        String names = list.stream().collect(Collectors.groupingBy(CfgRuleLogisticsEntity::getLogisticsMethod)).entrySet().stream().filter(obj -> obj.getValue().size() > MathUtil.ONE).map(obj -> obj.getKey()).distinct().collect(Collectors.joining(","));
        if (StrUtil.isNotBlank(names)) {
            throw new ServiceException("物流方式【{}】唯一不能添加重复数据",names);
        }
        //排序
        Integer maxIndex = MathUtil.ZERO;
        if (isCustom) {
            maxIndex = oldList.stream().max(Comparator.comparingInt(CfgRuleLogisticsEntity::getIndex)).map(CfgRuleLogisticsEntity::getIndex).orElse(MathUtil.ZERO);
        }
        for (CfgRuleLogisticsEntity  logisticsEntity: list) {
            //排序
            if (ObjectUtil.isEmpty(logisticsEntity.getIndex())) {
                logisticsEntity.setIndex(maxIndex + 1);
            }
            //备货主表id
            logisticsEntity.setStockUpId(stockUpId);
            //相同物流方式赋值id
            CfgRuleLogisticsEntity entity = oldList.stream().filter(obj -> StrUtil.equals(obj.getLogisticsMethod(), logisticsEntity.getLogisticsMethod())).findFirst().orElse(null);
            if (ObjectUtil.isNotEmpty(entity)) {
                logisticsEntity.setId(entity.getId());
                //自定义添加的需要保持原有序号
                if (ObjectUtil.isEmpty(logisticsEntity.getIndex())) {
                    logisticsEntity.setIndex(isCustom ? entity.getIndex() : logisticsEntity.getIndex());
                }
            }
            maxIndex ++;
        }
    }
}
