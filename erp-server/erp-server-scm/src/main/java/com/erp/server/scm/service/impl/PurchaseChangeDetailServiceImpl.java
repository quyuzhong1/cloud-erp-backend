package com.erp.server.scm.service.impl;

import com.common.business.service.SuperServiceImpl;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.MathUtil;
import com.erp.model.scm.dto.PurchaseChangeDetailDTO;
import com.erp.model.scm.entity.PurchaseChangeDetailEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.server.scm.mapper.PurchaseChangeDetailMapper;
import com.erp.server.scm.service.ModuleOperateLogService;
import com.erp.server.scm.service.PurchaseChangeDetailService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author will
 * @since 2023-03-16
 */
@Service
public class PurchaseChangeDetailServiceImpl extends SuperServiceImpl<PurchaseChangeDetailMapper, PurchaseChangeDetailEntity> implements PurchaseChangeDetailService {

    @Resource
    private ModuleOperateLogService moduleOperateLogService;

    @Override
    public void add(List<PurchaseChangeDetailDTO.AddDTO> details, String purchaseChangeId) {
        if (CollectionUtils.isEmpty(details)) {
            return;
        }
        List<PurchaseChangeDetailEntity> list = BeanMapperUtils.copyList(PurchaseChangeDetailEntity.class, details);
        //计算金额
        doOpCalculateAmount(list,purchaseChangeId);
        this.saveBatch(list);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(List<PurchaseChangeDetailDTO.UpdateDTO> details, String purchaseChangeId) {
        if (details == null) {
            details = new ArrayList<>();
        }
        //原明细数据
        List<PurchaseChangeDetailEntity> oldList = this.listByPurchaseChangeIds(Arrays.asList(purchaseChangeId));
        List<String> deleteIds = getDeleteIds(details, oldList);
        if (CollectionUtils.isNotEmpty(deleteIds)) {

            List<PurchaseChangeDetailEntity> removeList = oldList.stream().filter(obj -> deleteIds.contains(obj.getId())).collect(Collectors.toList());
            //操作日志
            List<Pair<String, String>> pairList = removeList.stream().map(obj -> new Pair<>(obj.getPurchaseChangeId(), obj.getSkuNo())).collect(Collectors.toList());
            moduleOperateLogService.batchAddModuleOperateLog("删除了一个SKU【%s】", ModuleTypeEnum.PURCHASE_CHANGE.getCode(), pairList, "编辑操作");
            this.removeByIds(deleteIds);
        }
        List<PurchaseChangeDetailEntity> newList = BeanMapperUtils.copyList(PurchaseChangeDetailEntity.class, details);
        //计算金额
        doOpCalculateAmount(newList,purchaseChangeId);
        this.saveOrUpdateBatch(newList);
    }


    @Override
    public List<PurchaseChangeDetailEntity> listByPurchaseChangeIds(List<String> purchaseChangeIds) {
        return lambdaQuery().in(PurchaseChangeDetailEntity::getPurchaseChangeId, purchaseChangeIds).list();
    }


    /**
     * 查询需要删除的数据
     */
    private List<String> getDeleteIds(List<PurchaseChangeDetailDTO.UpdateDTO> newList, List<PurchaseChangeDetailEntity> oldList) {
        List<String> newIds = newList.stream().filter(g -> StringUtils.isNotBlank(g.getId())).
                map(PurchaseChangeDetailDTO.UpdateDTO::getId).collect(Collectors.toList());
        List<String> oldIds = oldList.stream().map(PurchaseChangeDetailEntity::getId).collect(Collectors.toList());
        return oldIds.stream().filter(s -> !newIds.contains(s)).collect(Collectors.toList());
    }

    /**
     * 更新金额
     */
    private void doOpCalculateAmount(List<PurchaseChangeDetailEntity> list,String purchaseChangeId) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        list.forEach(obj->{
            obj.setPurchaseChangeId(purchaseChangeId);
            obj.setAmount(MathUtil.multiply(obj.getPrice(),obj.getQty()));
        });
    }
}