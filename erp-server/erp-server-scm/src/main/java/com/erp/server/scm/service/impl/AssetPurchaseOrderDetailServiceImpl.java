package com.erp.server.scm.service.impl;


import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.common.business.enums.AssetPurchaseOrderReceiveEnum;
import com.erp.model.scm.dto.AssetPurchaseOrderDTO;
import com.erp.model.scm.entity.AssetNoticeDetailEntity;
import com.erp.model.scm.entity.AssetPurchaseOrderEntity;
import com.erp.model.scm.dto.PurchasePriceDTO;
import com.erp.model.scm.enums.AssetApproveStatusEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.server.scm.mapper.AssetPurchaseOrderDetailMapper;
import com.erp.server.scm.service.*;
import io.seata.spring.annotation.GlobalTransactional;
import com.common.business.annotation.DistributeLocker;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.scm.entity.AssetPurchaseOrderDetailEntity;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.core.exception.ServiceException;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.scm.dto.AssetPurchaseOrderDetailDTO;
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
    private ModuleOperateLogService moduleOperateLogService;

    @Autowired
    private AssetPurchaseOrderDetailService assetPurchaseOrderDetailService;

    @Autowired
    private AssetNoticeService assetNoticeService;

    @Autowired
    private AssetNoticeDetailService assetNoticeDetailService;

    @Autowired
    private AssetPurchaseOrderService assetPurchaseOrderService;

    @Autowired
    private PurchasePriceService purchasePriceService;


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
        moduleOperateLogService.addModuleOperateLog(msg, null, assetPurchaseOrderDetailEntity.getId(), "新增操作");
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
        moduleOperateLogService.addModuleOperateLogByObj(old, assetPurchaseOrderDetailEntity, null, assetPurchaseOrderDetailEntity.getId() , "", msg);
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
        //只有已审核的采购单才可以结束验收
        List<String> purchaseOrderIdList = oldList.stream().map(obj -> obj.getMainId()).collect(Collectors.toList());
        List<AssetPurchaseOrderEntity> assetPurchaseOrderEntityList = assetPurchaseOrderService.listByIds(purchaseOrderIdList);
        long count = assetPurchaseOrderEntityList.stream().filter(obj -> !obj.getApproveStatus().equals(AssetApproveStatusEnum.APPROVE.getCode())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_95319);
        }

        // 过滤需更新的记录（未结束接收的明细）
        List<AssetPurchaseOrderDetailEntity> toUpdateList = oldList.stream()
                .filter(entity -> !AssetPurchaseOrderReceiveEnum.CLOSE.getCode().equals(entity.getEndReceive()))
                .collect(Collectors.toList());

        if (!toUpdateList.isEmpty()) {
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
                    moduleOperateLogService.addModuleOperateLogByObj(
                            oldList.stream().filter(old -> old.getId().equals(updatedEntity.getId())).findFirst().orElse(null),
                            updatedEntity,
                            ModuleTypeEnum.ASSET_PURCHASE_ORDER.getCode(),
                            updatedEntity.getId(),
                            "模具采购单",
                            msg
                    );
                }
            }
        }

        return Boolean.TRUE;
    }

    @Override
    public void add(AssetPurchaseOrderDTO.AddDTO addDTO, String assetPurchaseOrderId) {
        if (CollectionUtils.isEmpty(addDTO.getAssetPurchaseOrderDetailDTOList())) {
            return;
        }

        //从价表取价
        List<PurchasePriceDTO.PriceDTO> priceDTOList = new ArrayList<>();
        for (AssetPurchaseOrderDetailDTO.AddDTO dto : addDTO.getAssetPurchaseOrderDetailDTOList()) {
            PurchasePriceDTO.PriceDTO priceDTO = new PurchasePriceDTO.PriceDTO();
            priceDTO.setSkuId(dto.getAssetId());
            priceDTO.setSupplierId(addDTO.getAssetPurchaseOrderSupplierDTO().getSupplierId());
            priceDTO.setQty(dto.getPurchaseQty().intValue());
            priceDTO.setPurchaseOrgId(addDTO.getPurchaseOrgId());
            priceDTOList.add(priceDTO);
        }

        List<PurchasePriceDTO.PriceDTO> priceDTOS = purchasePriceService.batchGetPurchasePrice(priceDTOList);
        if (priceDTOS.isEmpty()) {
            throw new ServiceException(ApiError.ERROR_98024);
        }

        List<AssetPurchaseOrderDetailEntity> detailEntityList = new ArrayList<>();
        for (AssetPurchaseOrderDetailDTO.AddDTO addDTO1 : addDTO.getAssetPurchaseOrderDetailDTOList()) {
            AssetPurchaseOrderDetailEntity assetPurchaseOrderDetailEntity = new AssetPurchaseOrderDetailEntity();
            assetPurchaseOrderDetailEntity.setMainId(assetPurchaseOrderId);
            assetPurchaseOrderDetailEntity.setAssetId(addDTO1.getAssetId());
            assetPurchaseOrderDetailEntity.setAssetCode(addDTO1.getAssetCode());
            assetPurchaseOrderDetailEntity.setAssetName(addDTO1.getAssetName());
            assetPurchaseOrderDetailEntity.setCurrency(addDTO1.getCurrency());
            assetPurchaseOrderDetailEntity.setCurrencySymbol(addDTO1.getCurrencySymbol());
            assetPurchaseOrderDetailEntity.setTaxRate(addDTO1.getTaxRate());
            assetPurchaseOrderDetailEntity.setPurchaseQty(addDTO1.getPurchaseQty());
            assetPurchaseOrderDetailEntity.setPlanDeliveryDate(addDTO1.getPlanDeliveryDate());
            assetPurchaseOrderDetailEntity.setIsUrgent(addDTO1.getIsUrgent());
            assetPurchaseOrderDetailEntity.setSourceDetailId(StringUtils.isNotBlank(addDTO1.getSourceDetailId()) ? addDTO1.getSourceDetailId() : null);
            assetPurchaseOrderDetailEntity.setTag(addDTO1.getTag());
            assetPurchaseOrderDetailEntity.setRemark(StringUtils.isNotBlank(addDTO1.getRemark()) ? addDTO1.getRemark() : null);

            for (PurchasePriceDTO.PriceDTO priceDTO : priceDTOS) {
                if (priceDTO.getSkuId().equals(addDTO1.getAssetId())) {
                    assetPurchaseOrderDetailEntity.setTaxPrice(priceDTO.getTaxPrice());
                    assetPurchaseOrderDetailEntity.setTotalAmount(new BigDecimal(priceDTO.getAmount()));
                }
            }

            assetPurchaseOrderDetailEntity.setEndReceive(AssetPurchaseOrderReceiveEnum.WAIT_RECEIVE.getCode());
            detailEntityList.add(assetPurchaseOrderDetailEntity);
        }

        AssetPurchaseOrderDetailEntity assetPurchaseOrderDetailEntity = detailEntityList.stream()
                .filter(obj -> obj.getTotalAmount().compareTo(BigDecimal.ZERO) == 0)
                .findFirst()
                .orElse(null);
        if (Objects.nonNull(assetPurchaseOrderDetailEntity)) {
            throw new ServiceException(ApiError.ERROR_95313,assetPurchaseOrderDetailEntity.getAssetCode());
        }

        super.saveBatch(detailEntityList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(AssetPurchaseOrderDTO.UpdateDTO updateDTO, String assetPurchaseOrderId) {
        if (CollectionUtils.isEmpty(updateDTO.getAssetPurchaseOrderDetailDTOList())) {
            return;
        }

        List<AssetPurchaseOrderDetailDTO.UpdateDTO> detailList = updateDTO.getAssetPurchaseOrderDetailDTOList();

        if (StringUtils.isNotBlank(updateDTO.getSourceId())) {
            // 校验来源是否存在
            if (Objects.isNull(assetNoticeService.getById(updateDTO.getSourceId()))) {
                throw new ServiceException(ApiError.ERROR_95297); // 来源不存在
            }

            // 校验明细条数是否增加
            List<AssetPurchaseOrderDetailEntity> oldList = this.lambdaQuery()
                    .eq(AssetPurchaseOrderDetailEntity::getMainId, assetPurchaseOrderId)
                    .eq(AssetPurchaseOrderDetailEntity::getIsDeleted, Boolean.FALSE)
                    .list();
            if (detailList.size() > oldList.size()) {
                throw new ServiceException(ApiError.ERROR_95311); // 不允许增加明细
            }

            // 校验采购数量是否超过剩余数量
            validatePurchaseQty(detailList, oldList);

            // 从价表取价
            List<PurchasePriceDTO.PriceDTO> priceDTOS = getPurchasePrices(updateDTO, detailList);

            // 构建明细实体
            List<AssetPurchaseOrderDetailEntity> detailEntityList = buildDetailEntities(updateDTO, detailList, priceDTOS);

            // 检查总金额是否为 0
            checkTotalAmount(detailEntityList);

            // 删除旧数据 + 保存新数据
            deleteOldDetails(assetPurchaseOrderId, detailList);
            this.saveOrUpdateBatch(detailEntityList);
        } else {
            // 无来源的订单，直接删除旧数据 + 保存新数据
            deleteOldDetails(assetPurchaseOrderId, detailList);
            List<AssetPurchaseOrderDetailEntity> newList = BeanMapperUtils.copyList(AssetPurchaseOrderDetailEntity.class, detailList);
            this.saveOrUpdateBatch(newList);
        }
    }

    /**
     * 校验采购数量是否超过剩余数量
     */
    private void validatePurchaseQty(List<AssetPurchaseOrderDetailDTO.UpdateDTO> detailList, List<AssetPurchaseOrderDetailEntity> oldList) {
        for (AssetPurchaseOrderDetailEntity assetPurchaseOrderDetailEntity : oldList) {
            List<AssetPurchaseOrderDetailEntity> entityList = assetPurchaseOrderDetailService.lambdaQuery()
                    .eq(AssetPurchaseOrderDetailEntity::getId, assetPurchaseOrderDetailEntity.getSourceDetailId())
                    .eq(AssetPurchaseOrderDetailEntity::getIsDeleted, Boolean.FALSE)
                    .list();

            BigDecimal purchaseQtySum = entityList.stream()
                    .filter(obj -> !obj.getId().equals(assetPurchaseOrderDetailEntity.getId()))
                    .map(AssetPurchaseOrderDetailEntity::getPurchaseQty)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            AssetNoticeDetailEntity assetNoticeDetailEntity = assetNoticeDetailService.lambdaQuery()
                    .eq(AssetNoticeDetailEntity::getId, assetPurchaseOrderDetailEntity.getSourceDetailId())
                    .eq(AssetNoticeDetailEntity::getIsDeleted, Boolean.FALSE)
                    .one();

            if (assetNoticeDetailEntity.getApplyQty().subtract(purchaseQtySum)
                    .compareTo(assetPurchaseOrderDetailEntity.getPurchaseQty()) < 0) {
                throw new ServiceException(ApiError.ERROR_95312); // 采购数量超过剩余数量
            }
        }
    }

    /**
     * 从价表取价
     */
    private List<PurchasePriceDTO.PriceDTO> getPurchasePrices(AssetPurchaseOrderDTO.UpdateDTO updateDTO, List<AssetPurchaseOrderDetailDTO.UpdateDTO> detailList) {
        List<PurchasePriceDTO.PriceDTO> priceDTOList = detailList.stream()
                .map(dto -> {
                    PurchasePriceDTO.PriceDTO priceDTO = new PurchasePriceDTO.PriceDTO();
                    priceDTO.setSkuId(dto.getAssetId());
                    priceDTO.setSupplierId(updateDTO.getAssetPurchaseOrderSupplierDTO().getSupplierId());
                    priceDTO.setQty(dto.getPurchaseQty().intValue());
                    priceDTO.setPurchaseOrgId(updateDTO.getPurchaseOrgId());
                    return priceDTO;
                })
                .collect(Collectors.toList());

        List<PurchasePriceDTO.PriceDTO> priceDTOS = purchasePriceService.batchGetPurchasePrice(priceDTOList);
        if (priceDTOS.isEmpty()) {
            throw new ServiceException(ApiError.ERROR_98024);
        }
        return priceDTOS;
    }

    /**
     * 构建明细实体
     */
    private List<AssetPurchaseOrderDetailEntity> buildDetailEntities(
            AssetPurchaseOrderDTO.UpdateDTO updateDTO,
            List<AssetPurchaseOrderDetailDTO.UpdateDTO> detailList,
            List<PurchasePriceDTO.PriceDTO> priceDTOS) {
        return detailList.stream()
                .map(dto -> {
                    AssetPurchaseOrderDetailEntity entity = new AssetPurchaseOrderDetailEntity();
                    BeanUtils.copyProperties(dto, entity);
                    entity.setMainId(updateDTO.getId());
                    entity.setSourceDetailId(StringUtils.isNotBlank(dto.getSourceDetailId()) ? dto.getSourceDetailId() : null);

                    // 设置价格
                    priceDTOS.stream()
                            .filter(priceDTO -> priceDTO.getSkuId().equals(dto.getAssetId()))
                            .findFirst()
                            .ifPresent(priceDTO -> {
                                entity.setTaxPrice(priceDTO.getTaxPrice());
                                entity.setTotalAmount(new BigDecimal(priceDTO.getAmount()));
                            });

                    entity.setEndReceive(AssetPurchaseOrderReceiveEnum.WAIT_RECEIVE.getCode());
                    return entity;
                })
                .collect(Collectors.toList());
    }

    /**
     * 检查总金额是否为 0
     */
    private void checkTotalAmount(List<AssetPurchaseOrderDetailEntity> detailEntityList) {
        AssetPurchaseOrderDetailEntity invalidEntity = detailEntityList.stream()
                .filter(obj -> obj.getTotalAmount().compareTo(BigDecimal.ZERO) == 0)
                .findFirst()
                .orElse(null);
        if (Objects.nonNull(invalidEntity)) {
            throw new ServiceException(ApiError.ERROR_95313, invalidEntity.getAssetCode());
        }
    }

    /**
     * 删除旧数据
     */
    private void deleteOldDetails(String assetPurchaseOrderId, List<AssetPurchaseOrderDetailDTO.UpdateDTO> newList) {
        List<AssetPurchaseOrderDetailEntity> oldList = this.lambdaQuery()
                .eq(AssetPurchaseOrderDetailEntity::getMainId, assetPurchaseOrderId)
                .list();

        Set<String> newIdSet = newList.stream()
                .filter(g -> StringUtils.isNotBlank(g.getId()))
                .map(AssetPurchaseOrderDetailDTO.UpdateDTO::getId)
                .collect(Collectors.toSet());

        List<String> deleteIds = oldList.stream()
                .map(AssetPurchaseOrderDetailEntity::getId)
                .filter(id -> !newIdSet.contains(id))
                .collect(Collectors.toList());

        if (CollectionUtils.isNotEmpty(deleteIds)) {
            List<AssetPurchaseOrderDetailEntity> removeList = oldList.stream()
                    .filter(obj -> deleteIds.contains(obj.getId()))
                    .collect(Collectors.toList());

            List<Pair<String, String>> pairList = removeList.stream()
                    .map(obj -> new Pair<>(obj.getAssetId(), obj.getMainId()))
                    .collect(Collectors.toList());

            moduleOperateLogService.batchAddModuleOperateLog(
                    "模具采购单删除了一个SKU【%s】",
                    ModuleTypeEnum.ASSET_PURCHASE_ORDER.getCode(),
                    pairList,
                    "编辑操作"
            );

            this.removeByIds(deleteIds);
        }
    }

    @Override
    public void updateKingdeeDetailId(JSONArray list) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        for (Object obj : list) {
            JSONObject jsonObject = JSONUtil.parseObj(obj);
            String detailId = (String) jsonObject.get("detailId");
            String kingdeeDetailId = (String) jsonObject.get("kingdeeDetailId");
            this.lambdaUpdate()
                    .set(AssetPurchaseOrderDetailEntity::getKingdeeDetailId, kingdeeDetailId)
                    .eq(AssetPurchaseOrderDetailEntity::getId, detailId)
                    .update();
        }
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(AssetPurchaseOrderDetailEntity assetPurchaseOrderDetailEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
