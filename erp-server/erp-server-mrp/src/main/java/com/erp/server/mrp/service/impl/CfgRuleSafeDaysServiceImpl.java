package com.erp.server.mrp.service.impl;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.wrapper.FeignQuery;
import com.common.core.exception.ServiceException;
import com.erp.model.mrp.dto.CfgRuleSafeDaysDTO;
import com.erp.model.mrp.entity.CfgRuleSafeDaysEntity;
import com.erp.model.mrp.entity.CfgRuleStockUpEntity;
import com.erp.model.mrp.enums.CfgRulePlatformTypeEnum;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.server.mrp.mapper.CfgRuleSafeDaysMapper;
import com.erp.server.mrp.service.CfgRuleSafeDaysService;
import com.erp.server.mrp.service.OperateLogService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 安全天数明细 服务实现类
 * </p>
 *
 * @author liaohui
 * @since 2025-02-17
 */
@Service
public class CfgRuleSafeDaysServiceImpl extends SuperServiceImpl<CfgRuleSafeDaysMapper, CfgRuleSafeDaysEntity> implements CfgRuleSafeDaysService {

    @Resource
    private OperateLogService operateLogService;

    @Override
    public List<CfgRuleSafeDaysEntity> listByStockUpIdList(List<String> stockUpIdList) {

        return list(Wrappers.<CfgRuleSafeDaysEntity>lambdaQuery().in(CfgRuleSafeDaysEntity::getStockUpId, stockUpIdList));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(List<CfgRuleSafeDaysDTO.UpdateDTO> safeDaysList, CfgRuleStockUpEntity cfgRuleStockUpEntity, String code) {
        if (CollectionUtils.isEmpty(safeDaysList)) {
            safeDaysList = Collections.emptyList();
        }
        List<CfgRuleSafeDaysEntity> oldList = listByStockUpIdListAndType(Collections.singletonList(cfgRuleStockUpEntity.getId()), code);
        List<String> oldIdList = oldList.stream().map(CfgRuleSafeDaysEntity::getId).collect(Collectors.toList());
        List<String> newIds = safeDaysList.stream().map(CfgRuleSafeDaysDTO.UpdateDTO::getId).
                filter(StringUtils::isNotBlank).collect(Collectors.toList());
        List<String> deleteIds = oldIdList.stream().filter(v -> !newIds.contains(v)).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(deleteIds)) {
            this.removeByIds(deleteIds);
        }
        if (CollectionUtils.isEmpty(safeDaysList)) {
            return;
        }
        List<CfgRuleSafeDaysEntity> cfgRuleSafeDaysList = handleData(safeDaysList, cfgRuleStockUpEntity.getId(), code);
        boolean save = super.saveOrUpdateBatch(cfgRuleSafeDaysList);
        if (!save) {
            throw new ServiceException("更多天数（规则设置）保存失败");
        }
        //日志
        addOperateLog(cfgRuleSafeDaysList, cfgRuleStockUpEntity, code);
    }

    private void addOperateLog(List<CfgRuleSafeDaysEntity> cfgRuleSafeDaysList, CfgRuleStockUpEntity stockUpEntity, String code) {
        List<ShopInfoEntity> list = FeignQuery.list(ShopInfoEntity.class);
        StringBuilder sb = new StringBuilder();
        if (CfgRulePlatformTypeEnum.AMAZON.getCode().equals(code)) {
            sb.append("FBA安全天数：<br>");
        } else {
            sb.append("海外仓安全天数：<br>");
        }
        for (CfgRuleSafeDaysEntity entity : cfgRuleSafeDaysList) {
            String shopName = list.stream().filter(v -> entity.getShopIdJson().contains(v.getId())).map(ShopInfoEntity::getName).distinct().collect(Collectors.joining(","));
            String childMsg = CharSequenceUtil.format("•店铺【{}】、安全天数【{}】<br>", shopName, entity.getSafeDays());
            sb.append(childMsg);
        }
        operateLogService.addModuleOperateLog(sb.toString(), ModuleTypeEnum.REPLENISHMENT_SUGGESTION.getCode(), CharSequenceUtil.blankToDefault(stockUpEntity.getRefId(),stockUpEntity.getId()), "备货");
    }

    private List<CfgRuleSafeDaysEntity> handleData(List<CfgRuleSafeDaysDTO.UpdateDTO> safeDaysList, String id, String code) {
        List<CfgRuleSafeDaysEntity> result = new ArrayList<>();
        for (CfgRuleSafeDaysDTO.UpdateDTO dto : safeDaysList) {
            CfgRuleSafeDaysEntity entity = new CfgRuleSafeDaysEntity();
            entity.setId(dto.getId());
            entity.setStockUpId(id);
            entity.setPlatformType(code);
            entity.setSafeDays(dto.getSafeDays());
            entity.setShopIdJson(JSONUtil.parseArray(dto.getShopIdList()));
            result.add(entity);
        }
        return result;
    }

    public List<CfgRuleSafeDaysEntity> listByStockUpIdListAndType(List<String> stockUpIdList, String type) {

        return list(Wrappers.<CfgRuleSafeDaysEntity>lambdaQuery()
                .in(CfgRuleSafeDaysEntity::getStockUpId, stockUpIdList)
                .eq(CfgRuleSafeDaysEntity::getPlatformType, type)
        );
    }
}
