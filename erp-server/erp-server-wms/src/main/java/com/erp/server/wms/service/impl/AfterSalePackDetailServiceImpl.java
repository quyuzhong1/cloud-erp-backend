package com.erp.server.wms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import com.common.business.annotation.DistributeLocker;
import com.common.business.enums.AfterSalePackStatusEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.dto.AfterSalePackDTO;
import com.erp.model.wms.dto.AfterSalePackDetailDTO;
import com.erp.model.wms.dto.WarehouseLocationMoveDTO;
import com.erp.model.wms.dto.WarehouseLocationMoveDetailDTO;
import com.erp.model.wms.entity.AfterSalePackDetailEntity;
import com.erp.model.wms.entity.AfterSalePackEntity;
import com.erp.model.wms.entity.WarehouseLocationEntity;
import com.erp.model.wms.enums.WarehouseLocationMoveSyncOperateEnum;
import com.erp.rpc.plm.feign.ProductDetailFeign;
import com.erp.server.wms.mapper.AfterSalePackDetailMapper;
import com.erp.server.wms.service.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <p>
 * 售后装箱明细表 服务实现类
 * </p>
 *
 * @author lei.nie
 * @since 2026-05-12
 */
@Slf4j
@Service
public class AfterSalePackDetailServiceImpl extends SuperServiceImpl<AfterSalePackDetailMapper, AfterSalePackDetailEntity> implements AfterSalePackDetailService {

    @Resource
    private OperateLogService operateLogService;

    @Resource
    private AfterSalePackService afterSalePackService;

    @Resource
    private ProductDetailFeign productDetailFeign;

    @Resource
    private WarehouseLocationMoveService warehouseLocationMoveService;

    @Resource
    private WarehouseLocationService warehouseLocationService;

    /**
     * 修改
     */
    @DistributeLocker(keyName = "addOrUpdateDTO.getId()")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(AfterSalePackDetailDTO.UpdateDTO addOrUpdateDTO) {
        AfterSalePackDetailEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(() -> new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, "售后装箱明细单"));
        AfterSalePackEntity afterSalePackEntity = afterSalePackService.getById(old.getMainId());
        if (afterSalePackEntity == null) {
            throw new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, "售后装箱单");
        }
        // 箱唛状态不等于已封箱，不可操作
        if (!AfterSalePackStatusEnum.SEALED_BOX.getCode().equals(afterSalePackEntity.getPackStatus())) {
            throw new ServiceException("箱唛状态不等于已封箱，不可操作");
        }
        // 箱唛已被使用，不可操作
        if (afterSalePackEntity.getIsUse()) {
            throw new ServiceException("箱唛已被使用，不可操作");
        }
        // 查询仓位是否可用
        List<String> warehouseLocationCodes = Arrays.asList(addOrUpdateDTO.getOutWarehouseLocationCode(), addOrUpdateDTO.getInWarehouseLocationCode());
        List<WarehouseLocationEntity> warehouseLocationEntityList = warehouseLocationService.lambdaQuery()
                .in(WarehouseLocationEntity::getCode, warehouseLocationCodes)
                .eq(WarehouseLocationEntity::getDisabled, false)
                .list();
        if (warehouseLocationCodes.size() != warehouseLocationEntityList.size()) {
            throw new ServiceException("仓位不存在或已禁用");
        }
        Map<String, WarehouseLocationEntity> warehouseLocationMap = warehouseLocationEntityList.stream().collect(Collectors.toMap(WarehouseLocationEntity::getCode, Function.identity(), (v1, v2) -> v1));
        Integer moveQty = 0;
        // 操作类型不为空，表示移除sku操作
        if ("remove".equals(addOrUpdateDTO.getOperation())) {
            moveQty = old.getPackQty();
            super.removeById(old.getId());
        } else {
            if (addOrUpdateDTO.getUpdateQty() == null || addOrUpdateDTO.getUpdateQty() == 0) {
                throw new ServiceException("新增或者减少数量时，更新数量必填且不能为0");
            }
            moveQty = addOrUpdateDTO.getUpdateQty();
            log.info("编辑 开始修改售后装箱明细单数据，id：【{}】", old.getId());
            if ("add".equals(addOrUpdateDTO.getOperation())) {
                old.setPackQty(old.getPackQty() + addOrUpdateDTO.getUpdateQty());
            } else {
                if (old.getPackQty() < addOrUpdateDTO.getUpdateQty()) {
                    throw new ServiceException("减少数量不能大于已出库数量");
                }
                old.setPackQty(old.getPackQty() - addOrUpdateDTO.getUpdateQty());
            }
            boolean save = super.updateById(old);
            if (!save) {
                throw new ServiceException("售后装箱明细单保存失败");
            }
        }
        // 如果拆箱前已经发生了移仓，需要记录移仓流水信息
        if (afterSalePackEntity.getIsMoveWarehouse()) {
            WarehouseLocationMoveDetailDTO.AddDTO detail = new WarehouseLocationMoveDetailDTO.AddDTO();
            detail.setSkuId(old.getSkuId());
            detail.setSkuNo(old.getSkuNo());
            detail.setOutWarehouseLocation(addOrUpdateDTO.getOutWarehouseLocationCode());
            detail.setInWarehouseLocation(addOrUpdateDTO.getInWarehouseLocationCode());
            detail.setQty(moveQty);
            WarehouseLocationMoveDTO.AddDTO addDTO = new WarehouseLocationMoveDTO.AddDTO();
            addDTO.setWarehouseId(CharSequenceUtil.trim(warehouseLocationMap.get(addOrUpdateDTO.getOutWarehouseLocationCode()).getWarehouseId()));
            addDTO.setDetailList(CollUtil.newArrayList(detail));
            addDTO.setPcShow(false);
            addDTO.setSyncOperate(WarehouseLocationMoveSyncOperateEnum.UNBOX_TRANSFER.getCode());
            log.info("拆箱移位 warehouseId={} source={} target={} skuNo={} qty={}",
                    addDTO.getWarehouseId(), addOrUpdateDTO.getOutWarehouseLocationCode(), addOrUpdateDTO.getInWarehouseLocationCode(), old.getSkuNo(), moveQty);
            warehouseLocationMoveService.addAndApprove(addDTO);
        }
        // 记录主单操作日志
        log.info("编辑 开始记录售后装箱明细单日志数据，id：【{}】", old.getId());
        String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), afterSalePackEntity.getId(), "售后装箱明细单");
        operateLogService.addModuleOperateLogByObj(old, old, ModuleTypeEnum.AFTER_SALE_PACK.getCode(), afterSalePackEntity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    public AfterSalePackDetailDTO.ViewDTO view(String id) {
        AfterSalePackDetailEntity afterSalePackDetailEntity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到售后装箱明细单数据"));
        return BeanMapperUtils.map(AfterSalePackDetailDTO.ViewDTO.class, afterSalePackDetailEntity);
    }

    @Override
    public List<AfterSalePackDetailDTO.ViewDTO> listByCode(String code) {
        AfterSalePackDTO.ViewDTO afterSalePackViewDTO = afterSalePackService.viewByCode(code);
        List<AfterSalePackDetailEntity> list = lambdaQuery()
                .eq(AfterSalePackDetailEntity::getMainId, afterSalePackViewDTO.getId())
                .list();
        if (CollectionUtils.isEmpty(list)) {
            throw new ServiceException("未找到售后装箱明细数据");
        }
        // 取出所有不为空的skuId数据
        List<String> skuIdList = list.stream()
                .map(AfterSalePackDetailEntity::getSkuId)
                .filter(StrUtil::isNotBlank)
                .collect(Collectors.toList());
        return Collections.emptyList();
    }

}
