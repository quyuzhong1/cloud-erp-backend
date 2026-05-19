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
import com.erp.model.plm.entity.ProductDetailEntity;
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
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    private static final String OPERATION_ADD = "add";
    private static final String OPERATION_REDUCE = "reduce";
    private static final String OPERATION_REMOVE = "remove";

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

    @Resource
    private PdaAfterSalesWarehouseMoveService pdaAfterSalesWarehouseMoveService;

    /**
     * 修改
     */
    @DistributeLocker(keyName = "addOrUpdateDTO.id")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(AfterSalePackDetailDTO.UpdateDTO addOrUpdateDTO) {
        checkUpdateParam(addOrUpdateDTO);
        AfterSalePackDetailEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(() -> new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, "售后装箱明细单"));
        AfterSalePackEntity afterSalePackEntity = afterSalePackService.getById(old.getMainId());
        if (afterSalePackEntity == null) {
            throw new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, "售后装箱单");
        }
        // 已经发生了移仓，拆箱时移入仓位不能为空
        if (Boolean.TRUE.equals(afterSalePackEntity.getIsMoveWarehouse()) && StrUtil.isBlank(addOrUpdateDTO.getInWarehouseLocationCode())) {
            throw new ServiceException("移入仓位不能为空");
        }
        // 箱唛状态不等于已封箱，不可操作
        if (!AfterSalePackStatusEnum.SEALED_BOX.getCode().equals(afterSalePackEntity.getPackStatus())) {
            throw new ServiceException("箱唛状态不等于已封箱，不可操作");
        }
        // 箱唛已被使用，不可操作
        if (Boolean.TRUE.equals(afterSalePackEntity.getIsUse())) {
            throw new ServiceException("箱唛已被使用，不可操作");
        }
        // 查询仓位是否可用
        Map<String, WarehouseLocationEntity> warehouseLocationMap = getWarehouseLocationMap(addOrUpdateDTO);
        Integer moveQty = updatePackQty(addOrUpdateDTO, old);
        refreshPackSummary(old.getMainId());
        // 如果拆箱前已经发生了移仓，需要记录移仓流水信息
        addWarehouseLocationMove(addOrUpdateDTO, old, afterSalePackEntity, warehouseLocationMap, moveQty);
        // 记录主单操作日志
        log.info("编辑 开始记录售后装箱明细单日志数据，id：【{}】", old.getId());
        String msg = buildUnboxOperateLog(addOrUpdateDTO, afterSalePackEntity, moveQty);
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.AFTER_SALE_PACK.getName(), afterSalePackEntity.getId(), "拆箱");
        return Boolean.TRUE;
    }

    @Override
    public AfterSalePackDetailDTO.ViewDTO view(String id) {
        AfterSalePackDetailEntity afterSalePackDetailEntity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到售后装箱明细单数据"));
        return BeanMapperUtils.map(AfterSalePackDetailDTO.ViewDTO.class, afterSalePackDetailEntity);
    }

    @Override
    public List<AfterSalePackDetailDTO.ViewDTO> listByCode(String code) {
        if (StrUtil.isBlank(code)) {
            throw new ServiceException("箱唛不能为空");
        }
        AfterSalePackEntity afterSalePackEntity = afterSalePackService.lambdaQuery()
                .eq(AfterSalePackEntity::getCode, code)
                .one();
        if (afterSalePackEntity == null) {
            throw new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, "售后装箱单");
        }
        // 箱唛已经被使用，不可操作
        if (Boolean.TRUE.equals(afterSalePackEntity.getIsUse())) {
            throw new ServiceException("箱唛已被其它单据使用，不可重复使用");
        }
        // 箱唛状态不等于已封箱，不可操作
        if (!AfterSalePackStatusEnum.SEALED_BOX.getCode().equals(afterSalePackEntity.getPackStatus())) {
            throw new ServiceException("箱唛状态不等于已封箱，不可使用");
        }
        // 箱唛未发生移仓，不可使用
        if (Boolean.FALSE.equals(afterSalePackEntity.getIsMoveWarehouse())) {
            throw new ServiceException("未识别箱唛移仓记录，不可使用");
        }
        List<AfterSalePackDetailEntity> detailList = lambdaQuery()
                .eq(AfterSalePackDetailEntity::getMainId, afterSalePackEntity.getId())
                .list();
        if (CollectionUtils.isEmpty(detailList)) {
            throw new ServiceException("未找到售后装箱明细数据");
        }
        Map<String, ProductDetailEntity> productMap = getProductMap(detailList);
        Map<String, WarehouseLocationEntity> warehouseLocationMap = getWarehouseLocationMap(detailList);
        return detailList.stream()
                .collect(Collectors.groupingBy(this::getSkuGroupKey, LinkedHashMap::new, Collectors.toList()))
                .values()
                .stream()
                .map(list -> buildSkuViewDTO(afterSalePackEntity, list, productMap, warehouseLocationMap))
                .collect(Collectors.toList());
    }

    private void checkUpdateParam(AfterSalePackDetailDTO.UpdateDTO addOrUpdateDTO) {
        if (addOrUpdateDTO == null || StrUtil.isBlank(addOrUpdateDTO.getId())) {
            throw new ServiceException("售后装箱明细id不能为空");
        }
        String operation = addOrUpdateDTO.getOperation();
        if (!OPERATION_ADD.equals(operation) && !OPERATION_REDUCE.equals(operation) && !OPERATION_REMOVE.equals(operation)) {
            throw new ServiceException("操作类型错误");
        }
        if (!OPERATION_REMOVE.equals(operation) && (addOrUpdateDTO.getUpdateQty() == null || addOrUpdateDTO.getUpdateQty() <= 0)) {
            throw new ServiceException("新增或者减少数量时，更新数量必填且不能为0");
        }
        if (StrUtil.isBlank(addOrUpdateDTO.getOutWarehouseLocationCode())) {
            throw new ServiceException("拣货仓位不能为空");
        }
        // 如果拣货仓位和移入仓位都不为空，两个仓位不能相同
        if (StringUtils.isNotBlank(addOrUpdateDTO.getInWarehouseLocationCode()) && addOrUpdateDTO.getOutWarehouseLocationCode().equals(addOrUpdateDTO.getInWarehouseLocationCode())) {
            throw new ServiceException("拣货仓位和移入仓位不能相同");
        }
    }

    private Map<String, WarehouseLocationEntity> getWarehouseLocationMap(AfterSalePackDetailDTO.UpdateDTO addOrUpdateDTO) {
        List<String> warehouseLocationCodes = new ArrayList<>();
        warehouseLocationCodes.add(addOrUpdateDTO.getOutWarehouseLocationCode());
        if (StringUtils.isNotBlank(addOrUpdateDTO.getInWarehouseLocationCode())) {
            warehouseLocationCodes.add(addOrUpdateDTO.getInWarehouseLocationCode());
        }
        List<WarehouseLocationEntity> warehouseLocationEntityList = warehouseLocationService.lambdaQuery()
                .in(WarehouseLocationEntity::getCode, warehouseLocationCodes)
                .eq(WarehouseLocationEntity::getDisabled, false)
                .list();
        Map<String, WarehouseLocationEntity> warehouseLocationMap = warehouseLocationEntityList.stream().collect(Collectors.toMap(WarehouseLocationEntity::getCode, Function.identity(), (v1, v2) -> v1));
        if (warehouseLocationMap.get(addOrUpdateDTO.getOutWarehouseLocationCode()) == null) {
            throw new ServiceException("拣货仓位不存在或已禁用");
        }
        if (StringUtils.isNotBlank(addOrUpdateDTO.getInWarehouseLocationCode()) && warehouseLocationMap.get(addOrUpdateDTO.getInWarehouseLocationCode()) == null) {
            throw new ServiceException("移入仓位不存在或已禁用");
        }
        return warehouseLocationMap;
    }

    private void addWarehouseLocationMove(AfterSalePackDetailDTO.UpdateDTO addOrUpdateDTO,
                                          AfterSalePackDetailEntity old,
                                          AfterSalePackEntity afterSalePackEntity,
                                          Map<String, WarehouseLocationEntity> warehouseLocationMap,
                                          Integer moveQty) {
        if (!Boolean.TRUE.equals(afterSalePackEntity.getIsMoveWarehouse())) {
            return;
        }
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
        String moveId = warehouseLocationMoveService.addAndApprove(addDTO);
        pdaAfterSalesWarehouseMoveService.saveMoveCartonDetails(moveId,
                addOrUpdateDTO.getInWarehouseLocationCode(),
                CollUtil.newArrayList(buildMoveSourceBoxInfo(addOrUpdateDTO, old, afterSalePackEntity, moveQty)),
                Collections.emptyMap());
    }

    private AfterSalePackDTO.ViewDTO buildMoveSourceBoxInfo(AfterSalePackDetailDTO.UpdateDTO addOrUpdateDTO,
                                                            AfterSalePackDetailEntity old,
                                                            AfterSalePackEntity afterSalePackEntity,
                                                            Integer moveQty) {
        AfterSalePackDetailDTO.ViewDTO detail = new AfterSalePackDetailDTO.ViewDTO();
        detail.setId(old.getId());
        detail.setMainId(old.getMainId());
        detail.setSkuId(old.getSkuId());
        detail.setSkuNo(old.getSkuNo());
        detail.setOutWarehouseLocationCode(addOrUpdateDTO.getOutWarehouseLocationCode());
        detail.setInWarehouseLocationCode(addOrUpdateDTO.getInWarehouseLocationCode());
        detail.setPackQty(moveQty);
        AfterSalePackDTO.ViewDTO boxInfo = new AfterSalePackDTO.ViewDTO();
        boxInfo.setId(afterSalePackEntity.getId());
        boxInfo.setCode(afterSalePackEntity.getCode());
        boxInfo.setDetailViewDTOList(CollUtil.newArrayList(detail));
        return boxInfo;
    }

    private String buildUnboxOperateLog(AfterSalePackDetailDTO.UpdateDTO addOrUpdateDTO,
                                        AfterSalePackEntity afterSalePackEntity,
                                        Integer qty) {
        String userName = UserContext.getDefaultLoginUser().getUserName();
        String operationName = getOperationName(addOrUpdateDTO.getOperation());
        if (Boolean.TRUE.equals(afterSalePackEntity.getIsMoveWarehouse())) {
            return StrUtil.format("用户【{}】执行[{}]拆箱行为：从[移出仓位：{}]移仓至[移入仓位{}]数量为[{}]，并再次封箱",
                    userName, operationName, addOrUpdateDTO.getOutWarehouseLocationCode(), addOrUpdateDTO.getInWarehouseLocationCode(), qty);
        }
        return StrUtil.format("用户【{}】执行[{}]拆箱行为：从[拣货仓位：{}]{}数量为[{}]，并再次封箱",
                userName, operationName, addOrUpdateDTO.getOutWarehouseLocationCode(), getQuantityAction(addOrUpdateDTO.getOperation()), qty);
    }

    private String getOperationName(String operation) {
        if (OPERATION_ADD.equals(operation)) {
            return "增加数量";
        }
        if (OPERATION_REDUCE.equals(operation)) {
            return "减少数量";
        }
        return "移除sku";
    }

    private String getQuantityAction(String operation) {
        if (OPERATION_ADD.equals(operation)) {
            return "增加";
        }
        if (OPERATION_REDUCE.equals(operation)) {
            return "减少";
        }
        return "移除";
    }

    private Integer updatePackQty(AfterSalePackDetailDTO.UpdateDTO addOrUpdateDTO, AfterSalePackDetailEntity old) {
        Integer oldPackQty = Optional.ofNullable(old.getPackQty()).orElse(0);
        if (OPERATION_REMOVE.equals(addOrUpdateDTO.getOperation())) {
            if (!super.removeById(old.getId())) {
                throw new ServiceException("售后装箱明细单删除失败");
            }
            return oldPackQty;
        }
        Integer updateQty = addOrUpdateDTO.getUpdateQty();
        log.info("编辑 开始修改售后装箱明细单数据，id：【{}】", old.getId());
        if (OPERATION_ADD.equals(addOrUpdateDTO.getOperation())) {
            old.setPackQty(oldPackQty + updateQty);
        } else {
            if (oldPackQty < updateQty) {
                throw new ServiceException("减少数量不能大于已出库数量");
            }
            old.setPackQty(oldPackQty - updateQty);
        }
        boolean save = super.updateById(old);
        if (!save) {
            throw new ServiceException("售后装箱明细单保存失败");
        }
        return updateQty;
    }

    private void refreshPackSummary(String mainId) {
        List<AfterSalePackDetailEntity> detailList = lambdaQuery()
                .eq(AfterSalePackDetailEntity::getMainId, mainId)
                .list();
        AfterSalePackEntity updateEntity = new AfterSalePackEntity();
        updateEntity.setId(mainId);
        updateEntity.setSkuSpeciesQty((int) detailList.stream()
                .map(AfterSalePackDetailEntity::getSkuNo)
                .filter(StrUtil::isNotBlank)
                .distinct()
                .count());
        updateEntity.setTotalQty(detailList.stream()
                .map(AfterSalePackDetailEntity::getPackQty)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .sum());
        if (!afterSalePackService.updateById(updateEntity)) {
            throw new ServiceException("售后装箱单保存失败");
        }
    }

    private String getSkuGroupKey(AfterSalePackDetailEntity detail) {
        if (StrUtil.isNotBlank(detail.getSkuId())) {
            return detail.getSkuId();
        }
        return StrUtil.isNotBlank(detail.getSkuNo()) ? detail.getSkuNo() : detail.getId();
    }

    private AfterSalePackDetailDTO.ViewDTO buildSkuViewDTO(AfterSalePackEntity afterSalePackEntity,
                                                           List<AfterSalePackDetailEntity> detailList,
                                                           Map<String, ProductDetailEntity> productMap,
                                                           Map<String, WarehouseLocationEntity> warehouseLocationMap) {
        AfterSalePackDetailEntity first = detailList.get(0);
        AfterSalePackDetailDTO.ViewDTO viewDTO = BeanMapperUtils.map(AfterSalePackDetailDTO.ViewDTO.class, first);
        viewDTO.setId(StrUtil.isNotBlank(first.getSkuId()) ? first.getSkuId() : first.getId());
        viewDTO.setPackQty(sum(detailList, AfterSalePackDetailEntity::getPackQty));
        viewDTO.setActualQty(sum(detailList, AfterSalePackDetailEntity::getActualQty));
        viewDTO.setDiffQty(sum(detailList, AfterSalePackDetailEntity::getDiffQty));
        ProductDetailEntity productDetail = productMap.get(first.getSkuId());
        if (productDetail != null) {
            viewDTO.setProductName(productDetail.getName());
            viewDTO.setUnitId(productDetail.getUnitId());
            viewDTO.setUnit(productDetail.getUnitName());
            viewDTO.setUnitName(productDetail.getUnitName());
        }
        viewDTO.setDetailDTOList(detailList.stream()
                .map(detail -> buildBoxDetailDTO(afterSalePackEntity, detail, warehouseLocationMap))
                .collect(Collectors.toList()));
        return viewDTO;
    }

    private AfterSalePackDTO.DetailDTO buildBoxDetailDTO(AfterSalePackEntity afterSalePackEntity,
                                                         AfterSalePackDetailEntity detail,
                                                         Map<String, WarehouseLocationEntity> warehouseLocationMap) {
        AfterSalePackDTO.DetailDTO detailDTO = new AfterSalePackDTO.DetailDTO();
        detailDTO.setId(detail.getId());
        detailDTO.setMainId(detail.getMainId());
        detailDTO.setCode(afterSalePackEntity.getCode());
        detailDTO.setSkuId(detail.getSkuId());
        detailDTO.setSkuNo(detail.getSkuNo());
        detailDTO.setPackQty(detail.getPackQty());
        detailDTO.setActualQty(detail.getActualQty());
        detailDTO.setDiffQty(detail.getDiffQty());
        detailDTO.setOutWarehouseLocationId(detail.getOutWarehouseLocationId());
        WarehouseLocationEntity outWarehouseLocation = warehouseLocationMap.get(detail.getOutWarehouseLocationId());
        if (outWarehouseLocation != null) {
            detailDTO.setOutWarehouseLocationCode(outWarehouseLocation.getCode());
            detailDTO.setOutWarehouseLocationName(outWarehouseLocation.getName());
        }
        detailDTO.setInWarehouseLocationId(detail.getInWarehouseLocationId());
        WarehouseLocationEntity inWarehouseLocation = warehouseLocationMap.get(detail.getInWarehouseLocationId());
        if (inWarehouseLocation != null) {
            detailDTO.setInWarehouseLocationCode(inWarehouseLocation.getCode());
            detailDTO.setInWarehouseLocationName(inWarehouseLocation.getName());
        }
        return detailDTO;
    }

    private Map<String, ProductDetailEntity> getProductMap(List<AfterSalePackDetailEntity> detailList) {
        List<String> skuIdList = detailList.stream()
                .map(AfterSalePackDetailEntity::getSkuId)
                .filter(StrUtil::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(skuIdList)) {
            return Collections.emptyMap();
        }
        List<ProductDetailEntity> productList = Optional.ofNullable(productDetailFeign.listByIds(skuIdList)).orElse(Collections.emptyList());
        return productList.stream().collect(Collectors.toMap(ProductDetailEntity::getId, Function.identity(), (v1, v2) -> v1));
    }

    private Map<String, WarehouseLocationEntity> getWarehouseLocationMap(List<AfterSalePackDetailEntity> detailList) {
        List<String> warehouseLocationIds = detailList.stream()
                .flatMap(detail -> Stream.of(detail.getOutWarehouseLocationId(), detail.getInWarehouseLocationId()))
                .filter(StrUtil::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(warehouseLocationIds)) {
            return Collections.emptyMap();
        }
        return warehouseLocationService.listByIds(warehouseLocationIds).stream()
                .collect(Collectors.toMap(WarehouseLocationEntity::getId, Function.identity(), (v1, v2) -> v1));
    }

    private Integer sum(List<AfterSalePackDetailEntity> detailList, Function<AfterSalePackDetailEntity, Integer> mapper) {
        return detailList.stream()
                .map(mapper)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .sum();
    }

}
