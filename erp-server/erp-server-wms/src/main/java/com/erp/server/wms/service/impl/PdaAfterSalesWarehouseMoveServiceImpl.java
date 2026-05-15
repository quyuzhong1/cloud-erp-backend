package com.erp.server.wms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.common.core.exception.ServiceException;
import com.common.core.utils.StrUtils;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.wms.dto.AfterSalePackDTO;
import com.erp.model.wms.dto.AfterSalesWarehouseLocationSuggestDto;
import com.erp.model.wms.dto.WarehouseLocationMoveDTO;
import com.erp.model.wms.dto.WarehouseLocationMoveDetailDTO;
import com.erp.model.wms.entity.InventoryEntity;
import com.erp.model.wms.entity.WarehouseEntity;
import com.erp.model.wms.enums.inventory.InventoryStatusEnum;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.server.wms.service.AfterSalePackService;
import com.erp.server.wms.service.InventoryService;
import com.erp.server.wms.service.PdaAfterSalesWarehouseMoveService;
import com.erp.server.wms.service.WarehouseLocationMoveService;
import com.erp.server.wms.service.WarehouseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 售后 PDA：货品上架 / 整箱移仓
 */
@Slf4j
@Service
public class PdaAfterSalesWarehouseMoveServiceImpl implements PdaAfterSalesWarehouseMoveService {

    private static final int QTY_MAX = 999999999;

    @Resource
    private WarehouseLocationMoveService warehouseLocationMoveService;
    @Resource
    private PlmTaskFeign plmTaskFeign;
    @Resource
    private WarehouseService warehouseService;
    @Resource
    private InventoryService inventoryService;
    @Resource
    private AfterSalePackService afterSalePackService;

    /**
     * 仅当 {@link AfterSalePackDTO.ViewDTO#getIsUse()} 为 false 时允许整箱移仓。
     */
    private static void assertUsageStatusAllowsMove(AfterSalePackDTO.ViewDTO boxInfo) {
        Boolean u = boxInfo.getIsUse();
        if (Boolean.TRUE.equals(u)) {
            throw new ServiceException("该箱唛已被占用(usageStatus=true)，不支持整箱移仓");
        }
        if (!Boolean.FALSE.equals(u)) {
            throw new ServiceException(CharSequenceUtil.format("箱唛占用状态非法(usageStatus={})，仅允许为 false 时整箱移仓", u));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String submitGoodsShelving(AfterSalesWarehouseLocationSuggestDto.PdaGoodsShelvingSubmitDto dto) {
        validateQty(dto.getQty());

        String targetCode = CharSequenceUtil.trim(dto.getTargetWarehouseLocationCode());
        String sourceCode = CharSequenceUtil.trimToEmpty(dto.getSourceWarehouseLocationCode());

        String skuNo = CharSequenceUtil.trim(dto.getSkuNo());
        List<SkuVO> skuVOList = plmTaskFeign.listBySkuNoList(CollUtil.newArrayList(skuNo));
        if (CollUtil.isEmpty(skuVOList)) {
            throw new ServiceException("未查询到 SKU 信息，请检查 sku 编码");
        }
        SkuVO skuVO = skuVOList.stream()
                .filter(vo -> CharSequenceUtil.isNotBlank(vo.getSkuNo()) && skuNo.equals(CharSequenceUtil.trim(vo.getSkuNo())))
                .findFirst()
                .orElseThrow(() -> new ServiceException(CharSequenceUtil.format("未找到 SKU【{}】", skuNo)));
        if (CharSequenceUtil.isNotBlank(dto.getSkuId()) && !CharSequenceUtil.equals(dto.getSkuId(), skuVO.getSkuId())) {
            throw new ServiceException(CharSequenceUtil.format("SKU【{}】与 skuId 不匹配", skuNo));
        }

        WarehouseLocationMoveDetailDTO.AddDTO detail = new WarehouseLocationMoveDetailDTO.AddDTO();
        detail.setSkuId(skuVO.getSkuId());
        detail.setSkuNo(skuNo);
        detail.setOutWarehouseLocation(sourceCode);
        detail.setInWarehouseLocation(targetCode);
        detail.setQty(dto.getQty());

        WarehouseLocationMoveDTO.AddDTO addDTO = new WarehouseLocationMoveDTO.AddDTO();
        addDTO.setWarehouseId(CharSequenceUtil.trim(dto.getWarehouseId()));
        addDTO.setDetailList(CollUtil.newArrayList(detail));
        addDTO.setPcShow(false);

        log.info("售后PDA货品上架 warehouseId={} source={} target={} skuNo={} qty={}",
                addDTO.getWarehouseId(), sourceCode, targetCode, skuNo, dto.getQty());
        return warehouseLocationMoveService.addAndApprove(addDTO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String submitFullBoxTransfer(AfterSalesWarehouseLocationSuggestDto.PdaFullBoxTransferSubmitDto dto) {
        String warehouseId = CharSequenceUtil.trim(dto.getWarehouseId());
        String targetCode = CharSequenceUtil.trim(dto.getTargetWarehouseLocationCode());
        WarehouseEntity warehouse = warehouseService.getById(warehouseId);
        if (warehouse == null) {
            throw new ServiceException("仓库不存在");
        }
        String orgId = warehouse.getOrgId();

        LinkedHashMap<String, AfterSalesWarehouseLocationSuggestDto.PdaFullBoxTransferSubmitLineDto> unique =
                new LinkedHashMap<>();
        for (AfterSalesWarehouseLocationSuggestDto.PdaFullBoxTransferSubmitLineDto line : dto.getLines()) {
            String key = CharSequenceUtil.trim(line.getMainId()) + "|" + CharSequenceUtil.trim(line.getSkuNo()) + "|"
                    + CharSequenceUtil.trim(line.getWarehouseLocationCode());
            if (unique.containsKey(key)) {
                throw new ServiceException("明细中存在相同的 装箱主键+SKU+源仓位 行，请合并数量后提交");
            }
            unique.put(key, line);
        }

        LinkedHashSet<String> distinctMainIds = new LinkedHashSet<>();
        for (AfterSalesWarehouseLocationSuggestDto.PdaFullBoxTransferSubmitLineDto line : unique.values()) {
            distinctMainIds.add(CharSequenceUtil.trim(line.getMainId()));
        }
        for (String mainId : distinctMainIds) {
            String labelCode = unique.values().stream()
                    .filter(l -> mainId.equals(CharSequenceUtil.trim(l.getMainId())))
                    .map(l -> CharSequenceUtil.trim(l.getCode()))
                    .filter(CharSequenceUtil::isNotBlank)
                    .findFirst()
                    .orElse("");
            AfterSalePackDTO.ViewDTO boxInfo = CharSequenceUtil.isNotBlank(labelCode)
                    ? afterSalePackService.viewByCode(labelCode)
                    : afterSalePackService.view(mainId);
            if (!CharSequenceUtil.equals(mainId, CharSequenceUtil.trimToEmpty(boxInfo.getId()))) {
                throw new ServiceException(CharSequenceUtil.format("装箱主键与查询结果不一致：提交【{}】查询【{}】", mainId, boxInfo.getId()));
            }
            assertUsageStatusAllowsMove(boxInfo);
        }

        List<String> skuNos = unique.values().stream()
                .map(l -> CharSequenceUtil.trim(l.getSkuNo()))
                .distinct()
                .collect(Collectors.toList());
        List<SkuVO> skuVOList = plmTaskFeign.listBySkuNoList(skuNos);
        Map<String, SkuVO> skuByNo = new HashMap<>(16);
        if (CollUtil.isNotEmpty(skuVOList)) {
            skuByNo.putAll(skuVOList.stream()
                    .filter(vo -> CharSequenceUtil.isNotBlank(vo.getSkuNo()))
                    .collect(Collectors.toMap(vo -> CharSequenceUtil.trim(vo.getSkuNo()), vo -> vo, (a, b) -> a)));
        }

        List<WarehouseLocationMoveDetailDTO.AddDTO> detailList = new ArrayList<>();
        for (AfterSalesWarehouseLocationSuggestDto.PdaFullBoxTransferSubmitLineDto line : unique.values()) {
            validateQty(line.getPackQty());

            String skuNo = CharSequenceUtil.trim(line.getSkuNo());
            SkuVO skuVO = skuByNo.get(skuNo);
            if (skuVO == null) {
                throw new ServiceException(CharSequenceUtil.format("未找到 SKU【{}】", skuNo));
            }
            if (CharSequenceUtil.isNotBlank(line.getSkuId()) && !CharSequenceUtil.equals(line.getSkuId(), skuVO.getSkuId())) {
                throw new ServiceException(CharSequenceUtil.format("SKU【{}】与 skuId 不匹配", skuNo));
            }

            String sourceLoc = CharSequenceUtil.trim(line.getWarehouseLocationCode());
            assertUsableQtyAtLocation(warehouseId, orgId, skuVO.getSkuId(), sourceLoc, line.getPackQty(), skuNo);

            String mainId = CharSequenceUtil.trim(line.getMainId());
            String display = CharSequenceUtil.blankToDefault(CharSequenceUtil.trim(line.getCode()), mainId);
            String remark = CharSequenceUtil.format("整箱移仓{}", display);
            if (CharSequenceUtil.isNotBlank(dto.getRemark())) {
                remark = remark + " " + CharSequenceUtil.trim(dto.getRemark());
            }

            WarehouseLocationMoveDetailDTO.AddDTO detail = new WarehouseLocationMoveDetailDTO.AddDTO();
            detail.setSkuId(skuVO.getSkuId());
            detail.setSkuNo(skuNo);
            detail.setOutWarehouseLocation(sourceLoc);
            detail.setInWarehouseLocation(targetCode);
            detail.setQty(line.getPackQty());
            detail.setRemark(remark);
            detailList.add(detail);
        }

        WarehouseLocationMoveDTO.AddDTO addDTO = new WarehouseLocationMoveDTO.AddDTO();
        addDTO.setWarehouseId(warehouseId);
        addDTO.setDetailList(detailList);
        addDTO.setPcShow(false);

        log.info("售后PDA整箱移仓 warehouseId={} target={} lineCount={}", warehouseId, targetCode, detailList.size());
        return warehouseLocationMoveService.addAndApprove(addDTO);
    }

    private void validateQty(Integer qty) {
        if (!StrUtils.isDigit(String.valueOf(qty)) || Objects.isNull(qty)) {
            throw new ServiceException("移动数量只能是数字");
        }
        if (qty <= 0) {
            throw new ServiceException("移动数量不允许为0");
        }
        if (qty > QTY_MAX) {
            throw new ServiceException(CharSequenceUtil.format("移动数量最大值为{}", QTY_MAX));
        }
    }

    private void assertUsableQtyAtLocation(String warehouseId, String orgId, String skuId, String location, int needQty, String skuNo) {
        InventoryEntity inv = inventoryService.lambdaQuery()
                .eq(InventoryEntity::getWarehouseId, warehouseId)
                .eq(InventoryEntity::getOrgId, orgId)
                .eq(InventoryEntity::getSkuId, skuId)
                .eq(InventoryEntity::getDictInventoryStatus, InventoryStatusEnum.USABLE.getCode())
                .eq(InventoryEntity::getWarehouseLocation, location)
                .last("limit 1")
                .one();
        if (inv == null || inv.getQty() == null || inv.getQty() < needQty) {
            throw new ServiceException(CharSequenceUtil.format("【{}】sku移出仓位库存不足，不支持整箱移仓", skuNo));
        }
    }
}
