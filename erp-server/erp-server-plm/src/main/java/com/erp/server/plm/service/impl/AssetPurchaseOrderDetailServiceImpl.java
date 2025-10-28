package com.erp.server.plm.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.enums.AssetPurchaseOrderReceiveEnum;
import com.erp.model.plm.dto.AssetPurchaseOrderDTO;
import com.erp.model.plm.entity.AssetNoticeDetailEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.server.plm.service.*;
import io.seata.spring.annotation.GlobalTransactional;
import com.common.business.annotation.DistributeLocker;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.plm.entity.AssetPurchaseOrderDetailEntity;
import com.erp.server.plm.mapper.AssetPurchaseOrderDetailMapper;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.core.exception.ServiceException;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.plm.dto.AssetPurchaseOrderDetailDTO;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author wtr
 * @since 2025-10-16
 */
@Slf4j
@Service
public class AssetPurchaseOrderDetailServiceImpl extends SuperServiceImpl<AssetPurchaseOrderDetailMapper, AssetPurchaseOrderDetailEntity> implements AssetPurchaseOrderDetailService {

    @Autowired
    private OperateLogService operateLogService;

    @Autowired
    private AssetPurchaseOrderDetailService assetPurchaseOrderDetailService;

    @Autowired
    private AssetNoticeDetailService assetNoticeDetailService;


    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(AssetPurchaseOrderDetailDTO.AddDTO addDTO) {
        AssetPurchaseOrderDetailEntity assetPurchaseOrderDetailEntity = new AssetPurchaseOrderDetailEntity();
        BeanMapperUtils.copy(addDTO, assetPurchaseOrderDetailEntity);

        // 数据处理
        handleData(assetPurchaseOrderDetailEntity);

        log.info("开始新增");
        boolean save = super.save(assetPurchaseOrderDetailEntity);
        if(!save) {
            throw new ServiceException("保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "" , assetPurchaseOrderDetailEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addSysLogBySave(msg, null, assetPurchaseOrderDetailEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(assetPurchaseOrderDetailEntity.getId(), assetPurchaseOrderDetailEntity.getId());
    }

    /**
    * 修改
    */
    @DistributeLocker(keyName = "addOrUpdateDTO.getId()")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(AssetPurchaseOrderDetailDTO.UpdateDTO addOrUpdateDTO) {
        AssetPurchaseOrderDetailEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, ""));
        AssetPurchaseOrderDetailEntity assetPurchaseOrderDetailEntity =  BeanMapperUtils.map(AssetPurchaseOrderDetailEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(assetPurchaseOrderDetailEntity);
        log.info("编辑 开始修改数据，id：【{}】", old.getId());
        boolean save = super.updateById(assetPurchaseOrderDetailEntity);
        if(!save) {
            throw new ServiceException("保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录日志数据，id：【{}】", assetPurchaseOrderDetailEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), assetPurchaseOrderDetailEntity.getId(), "");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addSysLogByUpdate(old, assetPurchaseOrderDetailEntity, null, assetPurchaseOrderDetailEntity.getId() , "", msg);
        return Boolean.TRUE;
    }

    @Override
    public Boolean endReceive(List<String> idList, String remark) {
        if (CollectionUtils.isEmpty(idList)) {
            throw new IllegalArgumentException("ID列表不能为空");
        }

        List<AssetPurchaseOrderDetailEntity> oldList = this.listByIds(idList);
        if (CollectionUtils.isEmpty(oldList)) {
            throw new ServiceException(ApiError.ERROR_95298);
        }

        // 过滤需更新的记录（未结束接收的明细）
        List<AssetPurchaseOrderDetailEntity> toUpdateList = oldList.stream()
                .filter(entity -> !AssetPurchaseOrderReceiveEnum.CLOSE.getCode().equals(entity.getEndReceive()))
                .collect(Collectors.toList());

        // 批量更新状态
        boolean updateResult = this.lambdaUpdate()
                .set(AssetPurchaseOrderDetailEntity::getEndReceive, AssetPurchaseOrderReceiveEnum.CLOSE.getCode())
                .set(AssetPurchaseOrderDetailEntity::getEndReceiveTime, LocalDate.now())
                .set(AssetPurchaseOrderDetailEntity::getRemark, remark)
                .in(AssetPurchaseOrderDetailEntity::getId, toUpdateList.stream().map(AssetPurchaseOrderDetailEntity::getId).collect(Collectors.toList()))
                .eq(AssetPurchaseOrderDetailEntity::getIsDeleted, Boolean.FALSE)
                .update();

        // 记录操作日志
        if (updateResult) {
            for (AssetPurchaseOrderDetailEntity updatedEntity : toUpdateList) {
                String msg = StrUtil.format("SKU【{}】结束验收，结束原因：{}", updatedEntity.getAssetCode(), remark);
                operateLogService.addSysLogByUpdate(
                        oldList.stream().filter(old -> old.getId().equals(updatedEntity.getId())).findFirst().orElse(null),
                        updatedEntity,
                        ModuleTypeEnum.ASSET_PURCHASE_ORDER.getCode(),
                        updatedEntity.getId(),
                        "资产采购单",
                        msg
                );
            }
        }

        return updateResult;
    }

    @Override
    public void add(AssetPurchaseOrderDTO.AddDTO addDTO, String assetPurchaseOrderId) {
        if (CollectionUtils.isEmpty(addDTO.getAssetPurchaseOrderDetailDTOList())) {
            return;
        }

        List<AssetPurchaseOrderDetailEntity> detailEntityList = new ArrayList<>();
        for (AssetPurchaseOrderDetailDTO.AddDTO addDTO1 : addDTO.getAssetPurchaseOrderDetailDTOList()) {
            AssetPurchaseOrderDetailEntity assetPurchaseOrderDetailEntity = new AssetPurchaseOrderDetailEntity();
            BeanMapperUtils.copy(addDTO1, assetPurchaseOrderDetailEntity);
            assetPurchaseOrderDetailEntity.setMainId(assetPurchaseOrderId);
            assetPurchaseOrderDetailEntity.setTotalAmount(assetPurchaseOrderDetailEntity.getPurchaseQty().multiply(assetPurchaseOrderDetailEntity.getTaxPrice()));
            assetPurchaseOrderDetailEntity.setEndReceive(AssetPurchaseOrderReceiveEnum.WAIT_RECEIVE.getCode());
            detailEntityList.add(assetPurchaseOrderDetailEntity);
        }
        super.saveBatch(detailEntityList);
    }

    @Override
    public void update(AssetPurchaseOrderDTO.UpdateDTO updateDTO, String assetPurchaseOrderId) {
        if (CollectionUtils.isEmpty(updateDTO.getAssetPurchaseOrderDetailDTOList())) {
            return;
        }

        //明细条数不允许增加
        List<AssetPurchaseOrderDetailEntity> list = this.lambdaQuery()
                .eq(AssetPurchaseOrderDetailEntity::getMainId, assetPurchaseOrderId)
                .eq(AssetPurchaseOrderDetailEntity::getIsDeleted, Boolean.FALSE)
                .list();
        if (updateDTO.getAssetPurchaseOrderDetailDTOList().size() > list.size()) {
            throw new ServiceException(ApiError.ERROR_95311);
        }

        //申请数量不允许超过剩余数量
        for (AssetPurchaseOrderDetailEntity assetPurchaseOrderDetailEntity : list) {
            List<AssetPurchaseOrderDetailEntity> entityList = assetPurchaseOrderDetailService.lambdaQuery()
                    .eq(AssetPurchaseOrderDetailEntity::getId, assetPurchaseOrderDetailEntity.getSourceDetailId())
                    .eq(AssetPurchaseOrderDetailEntity::getIsDeleted, Boolean.FALSE)
                    .list();

            BigDecimal purchaseQtySum = entityList.stream()
                    .filter(obj -> !obj.getId().equals(assetPurchaseOrderDetailEntity.getId()))
                    .map(obj -> obj.getPurchaseQty())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            //获取通知单的申请数量
            AssetNoticeDetailEntity assetNoticeDetailEntity = assetNoticeDetailService.lambdaQuery()
                    .eq(AssetNoticeDetailEntity::getId, assetPurchaseOrderDetailEntity.getSourceDetailId())
                    .eq(AssetNoticeDetailEntity::getIsDeleted, Boolean.FALSE)
                    .one();
            //申请数量-除了当前单的已采购数量 > 传入的采购数量 才可以保存
            if (assetNoticeDetailEntity.getApplyQty().subtract(purchaseQtySum).compareTo(assetPurchaseOrderDetailEntity.getPurchaseQty()) < 0) {
                throw new ServiceException(ApiError.ERROR_95312);
            }
        }

        List<AssetPurchaseOrderDetailEntity> assetPurchaseOrderDetailEntities = new ArrayList<>();

        for (AssetPurchaseOrderDetailDTO.UpdateDTO dto : updateDTO.getAssetPurchaseOrderDetailDTOList()) {
            AssetPurchaseOrderDetailEntity assetPurchaseOrderDetailEntity = new AssetPurchaseOrderDetailEntity();
            BeanMapperUtils.copy(dto, assetPurchaseOrderDetailEntity);
            assetPurchaseOrderDetailEntity.setMainId(assetPurchaseOrderId);
            assetPurchaseOrderDetailEntities.add(assetPurchaseOrderDetailEntity);
        }
        super.updateBatchById(assetPurchaseOrderDetailEntities);
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(AssetPurchaseOrderDetailEntity assetPurchaseOrderDetailEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
