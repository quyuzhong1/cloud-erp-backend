package com.erp.server.mrp.service.impl;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.wrapper.FeignQuery;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.MathUtil;
import com.erp.model.mrp.dto.CfgRuleOverseasInstockDaysDTO;
import com.erp.model.mrp.entity.CfgRuleExpireTimeEntity;
import com.erp.model.mrp.entity.CfgRuleOverseasInstockDaysEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.entity.WarehouseEntity;
import com.erp.server.mrp.mapper.CfgRuleOverseasInstockDaysMapper;
import com.erp.server.mrp.service.CfgRuleOverseasInstockDaysService;
import com.erp.server.mrp.service.OperateLogService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 海外仓入库天数明细 服务实现类
 * </p>
 *
 * @author liaohui
 * @since 2025-02-13
 */
@Service
@Slf4j
public class CfgRuleOverseasInstockDaysServiceImpl extends SuperServiceImpl<CfgRuleOverseasInstockDaysMapper, CfgRuleOverseasInstockDaysEntity> implements CfgRuleOverseasInstockDaysService {

    @Resource
    private OperateLogService operateLogService;

    @Override
    public List<CfgRuleOverseasInstockDaysDTO.ViewDTO> listViewByExpireTimeIdList(List<String> expireTimeIds) {
        List<CfgRuleOverseasInstockDaysEntity> list = list(Wrappers.<CfgRuleOverseasInstockDaysEntity>lambdaQuery().in(CfgRuleOverseasInstockDaysEntity::getExpireTimeId, expireTimeIds));
        return BeanMapperUtils.copyList(CfgRuleOverseasInstockDaysDTO.ViewDTO.class, list);
    }

    @Override
    public void update(List<CfgRuleOverseasInstockDaysDTO.UpdateDTO> overseasInstockDaysList, CfgRuleExpireTimeEntity cfgRuleExpireTime) {
        if (CollectionUtils.isEmpty(overseasInstockDaysList)) {
            overseasInstockDaysList = Collections.emptyList();
        }
        List<CfgRuleOverseasInstockDaysEntity> list = BeanMapperUtils.copyList(CfgRuleOverseasInstockDaysEntity.class, overseasInstockDaysList);
        //原信息
        List<CfgRuleOverseasInstockDaysEntity> oldList = list(Wrappers.<CfgRuleOverseasInstockDaysEntity>lambdaQuery()
                .eq(CfgRuleOverseasInstockDaysEntity::getExpireTimeId, cfgRuleExpireTime.getId()));
        //删除明细
        List<String> deleteIds = getDeleteIds(list, oldList);
        if (CollectionUtils.isNotEmpty(deleteIds)) {
            this.removeByIds(deleteIds);
            // 数据处理
            oldList = oldList.stream().filter(obj -> !deleteIds.contains(obj.getId())).collect(Collectors.toList());

        }
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        handleData(list, cfgRuleExpireTime.getId());
        boolean save = super.saveOrUpdateBatch(list);
        if (!save) {
            throw new ServiceException("时效海外仓入库时间（规则设置）保存失败");
        }
        List<WarehouseEntity> warehouseList = FeignQuery.list(WarehouseEntity.class);
        //日志
        StringBuilder msg = getMsg(list, warehouseList, oldList);
        operateLogService.addModuleOperateLog(msg.toString(), ModuleTypeEnum.REPLENISHMENT_SUGGESTION.getCode(), CharSequenceUtil.blankToDefault(cfgRuleExpireTime.getRefId(), cfgRuleExpireTime.getId()), "备货");
    }

    private StringBuilder getMsg(List<CfgRuleOverseasInstockDaysEntity> list, List<WarehouseEntity> warehouseList, List<CfgRuleOverseasInstockDaysEntity> oldList) {
        Map<String, String> warehouseMap = warehouseList.stream()
                .collect(Collectors.toMap(WarehouseEntity::getId, WarehouseEntity::getName, (o1, o2) -> o1));
        StringBuilder sb = new StringBuilder();
        for (CfgRuleOverseasInstockDaysEntity entity : list) {
            CfgRuleOverseasInstockDaysEntity old = oldList.stream().filter(v -> v.getId().equals(entity.getId())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(old)) {
                sb.append("新增了:").append(warehouseMap.get(entity.getWarehouseId())).append(entity.getInstockDays()).append("天<br>");
            } else {
                sb.append(warehouseMap.get(old.getWarehouseId())).append(old.getInstockDays()).append("天").append("更新为")
                        .append(warehouseMap.get(entity.getWarehouseId())).append(entity.getInstockDays()).append("天<br>");
            }
        }
        return sb;
    }


    /**
     * 查询需要删除的数据
     */
    private List<String> getDeleteIds(List<CfgRuleOverseasInstockDaysEntity> newList, List<CfgRuleOverseasInstockDaysEntity> oldList) {
        List<String> newIds = newList.stream().map(CfgRuleOverseasInstockDaysEntity::getId).
                filter(StringUtils::isNotBlank).collect(Collectors.toList());
        List<String> oldIds = oldList.stream().map(CfgRuleOverseasInstockDaysEntity::getId).collect(Collectors.toList());
        return oldIds.stream().filter(s -> !newIds.contains(s)).collect(Collectors.toList());
    }

    /**
     * 新增修改处理数据
     */
    private void handleData(List<CfgRuleOverseasInstockDaysEntity> list,String expireTimeId) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        //排序
        int maxIndex = MathUtil.ZERO;
        for (CfgRuleOverseasInstockDaysEntity overseasInstockDaysEntity: list) {
            //排序
            if (ObjectUtil.isEmpty(overseasInstockDaysEntity.getIndex())) {
                overseasInstockDaysEntity.setIndex(maxIndex + 1);
            }
            //备货主表id
            overseasInstockDaysEntity.setExpireTimeId(expireTimeId);

            maxIndex ++;
        }

    }

}
