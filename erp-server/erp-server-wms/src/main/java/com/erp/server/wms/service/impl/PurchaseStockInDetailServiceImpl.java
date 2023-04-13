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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

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

        doOpHandleDataId(list,mainId);
        this.saveBatch(list);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(List<PurchaseStockInDetailDTO.UpdateDTO> details, String purchaseStockInId) {

    }

    @Override
    public void removeByMainIds(List<String> mainIds) {

    }

    @Override
    public List<PurchaseStockInDetailEntity> listByMainId(String mainId) {
        return null;
    }


    /**
     * 处理明细中的数据id
     */
    private void doOpHandleDataId (List<PurchaseStockInDetailEntity> newList, String salesDemandId) {

        //仓库信息
        for (PurchaseStockInDetailEntity entity : newList) {
            //操作日志
            if (StringUtils.isBlank(entity.getId())) {
                moduleOperateLogService.addModuleOperateLog(String.format("新增了一条SKU【%s】明细",entity.getSkuNo()), ModuleTypeEnum.PURCHASE_STOCK_IN.getCode(),salesDemandId,"编辑操作");
            } else {
                PurchaseStockInDetailEntity old = this.getById(entity.getId());
                if (ObjectUtils.isEmpty(old)) {
                    throw new ServiceException(ApiError.ERROR_98002);
                }
                moduleOperateLogService.addModuleOperateLogByObj(old,entity, ModuleTypeEnum.PURCHASE_STOCK_IN.getCode(),salesDemandId,"",String.format("【%s】",old.getSkuNo()));
            }
        }
    }
}
