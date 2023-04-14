package com.erp.server.wms.service.impl;

import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.business.service.SuperServiceImpl;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.dto.PurchaseStockInDetailDTO;
import com.erp.model.wms.entity.PurchaseStockInDetailEntity;
import com.erp.server.wms.mapper.PurchaseStorageDetailMapper;
import com.erp.server.wms.service.ModuleOperateLogService;
import com.erp.server.wms.service.PurchaseStockInDetailService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 采购入库明细表 服务实现类
 * </p>
 *
 * @author will
 * @since 2023-04-10
 */
@Service
public class PurchaseStockInDetailServiceImpl extends SuperServiceImpl<PurchaseStorageDetailMapper, PurchaseStockInDetailEntity> implements PurchaseStockInDetailService {

    @Resource
    private ModuleOperateLogService moduleOperateLogService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(List<PurchaseStockInDetailDTO.AddDTO> details, String mainId) {
        if (CollectionUtils.isEmpty(details)) {
            return;
        }
        List<PurchaseStockInDetailEntity> list = BeanMapperUtils.copyList(PurchaseStockInDetailEntity.class, details);
        doOpHandleDetails(list,mainId);
        this.saveBatch(list);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(List<PurchaseStockInDetailDTO.UpdateDTO> details, String mainId) {
        if (details == null) {
            details = new ArrayList<>();
        }
        //原明细数据
        List<PurchaseStockInDetailEntity> oldList = this.listByMainId(mainId);
        List<String> deleteIds = getDeleteIds(details, oldList);
        if (CollectionUtils.isNotEmpty(deleteIds)) {
            List<PurchaseStockInDetailEntity> removeList = oldList.stream().filter(obj -> deleteIds.contains(obj.getId())).collect(Collectors.toList());
            //操作日志
            List<Pair<String, String>> pairList = removeList.stream().map(obj -> new Pair<>(obj.getMainId(), obj.getSkuNo())).collect(Collectors.toList());
            moduleOperateLogService.batchAddModuleOperateLog("删除了一个SKU【%s】", ModuleTypeEnum.PURCHASE_STOCK_IN.getCode(),pairList,"编辑操作");
            this.removeByIds(deleteIds);
        }
        List<PurchaseStockInDetailEntity> newList = BeanMapperUtils.copyList(PurchaseStockInDetailEntity.class, details);

        //处理明细id及操作日志
        doOpHandleDetails(newList,mainId);

        //新增或修改明细
        this.saveOrUpdateBatch(newList);
    }

    /**
     * 查询需要删除的数据
     */
    private List<String> getDeleteIds(List<PurchaseStockInDetailDTO.UpdateDTO> newList, List<PurchaseStockInDetailEntity> oldList) {
        List<String> newIds = newList.stream().filter(g -> StringUtils.isNotBlank(g.getId())).
                map(PurchaseStockInDetailDTO.UpdateDTO::getId).collect(Collectors.toList());
        List<String> oldIds = oldList.stream().map(PurchaseStockInDetailEntity
                ::getId).collect(Collectors.toList());
        return oldIds.stream().filter(s -> !newIds.contains(s)).collect(Collectors.toList());
    }

    @Override
    public void removeByMainIds(List<String> mainIds) {
        lambdaUpdate().in(PurchaseStockInDetailEntity::getMainId,mainIds).remove();
    }

    @Override
    public List<PurchaseStockInDetailEntity> listByMainId(String mainId) {
        return lambdaQuery().eq(PurchaseStockInDetailEntity::getMainId,mainId).list();
    }

    @Override
    public List<PurchaseStockInDetailEntity> listDetailBySourceDetailIds(List<String> sourceDetailIds) {
        return lambdaQuery().in(PurchaseStockInDetailEntity::getSourceDetailId,sourceDetailIds).list();
    }


    /**
     * 处理明细中的数据id
     */
    private void doOpHandleDetails (List<PurchaseStockInDetailEntity> newList, String mainId) {

        //仓库信息
        for (PurchaseStockInDetailEntity entity : newList) {
            //操作日志
            if (StringUtils.isBlank(entity.getId())) {
                moduleOperateLogService.addModuleOperateLog(String.format("新增了一条SKU【%s】明细",entity.getSkuNo()), ModuleTypeEnum.PURCHASE_STOCK_IN.getCode(),mainId,"编辑操作");
            } else {
                PurchaseStockInDetailEntity old = this.getById(entity.getId());
                if (ObjectUtils.isEmpty(old)) {
                    throw new ServiceException(ApiError.ERROR_98002);
                }
                moduleOperateLogService.addModuleOperateLogByObj(old,entity, ModuleTypeEnum.PURCHASE_STOCK_IN.getCode(),mainId,"",String.format("【%s】",old.getSkuNo()));
            }
        }
    }
}
