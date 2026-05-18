package com.erp.server.wms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.common.core.exception.ServiceException;
import com.common.core.utils.StrUtils;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.wms.dto.AfterSalePackDTO;
import com.erp.model.wms.dto.AfterSalePackDetailDTO;
import com.erp.model.wms.dto.AfterSalesWarehouseLocationSuggestDto;
import com.erp.model.wms.dto.WarehouseLocationMoveDTO;
import com.erp.model.wms.dto.WarehouseLocationMoveDetailDTO;
import com.erp.model.wms.entity.AfterSalesFullBoxTransferDetailEntity;
import com.erp.model.wms.entity.InventoryEntity;
import com.erp.model.wms.entity.WarehouseEntity;
import com.erp.model.wms.entity.WarehouseLocationMoveDetailEntity;
import com.erp.model.wms.enums.WarehouseLocationMoveSyncOperateEnum;
import com.erp.model.wms.enums.inventory.InventoryStatusEnum;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.server.wms.service.AfterSalePackService;
import com.erp.server.wms.service.AfterSalesFullBoxTransferDetailService;
import com.erp.server.wms.service.InventoryService;
import com.erp.server.wms.service.PdaAfterSalesWarehouseMoveService;
import com.erp.server.wms.service.WarehouseLocationMoveDetailService;
import com.erp.server.wms.service.WarehouseLocationMoveService;
import com.erp.server.wms.service.WarehouseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
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
    private WarehouseLocationMoveDetailService warehouseLocationMoveDetailService;
    @Resource
    private AfterSalesFullBoxTransferDetailService afterSalesFullBoxTransferDetailService;
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
        addDTO.setSyncOperate(WarehouseLocationMoveSyncOperateEnum.AFTER_SALES_SHELVING.getCode());

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

        // ── 第一步：校验前端传入的箱唛行去重 ──────────────────────────────────
        // submittedBoxMap：以 mainId 为 key 去重，收集每个箱子的唯一标识
        // mainIdByCode   ：以 boxLabelCode(箱唛号) 为 key 检测跨行重复箱唛
        // submittedDetailKeys：以 mainId+skuNo+移出仓位 为 key 检测同箱内重复明细行
        LinkedHashMap<String, SubmittedBox> submittedBoxMap = new LinkedHashMap<>();
        Map<String, String> mainIdByCode = new HashMap<>(16);
        Set<String> submittedDetailKeys = new LinkedHashSet<>();
        for (AfterSalesWarehouseLocationSuggestDto.PdaFullBoxTransferSubmitLineDto line : dto.getLines()) {
            String mainId = CharSequenceUtil.trim(line.getMainId());
            String code = CharSequenceUtil.trim(line.getCode());
            SubmittedBox submittedBox = submittedBoxMap.get(mainId);
            if (submittedBox == null) {
                submittedBoxMap.put(mainId, new SubmittedBox(mainId, code));
            } else if (CharSequenceUtil.isNotBlank(code) && CharSequenceUtil.isNotBlank(submittedBox.code)
                    && !CharSequenceUtil.equals(submittedBox.code, code)) {
                // 同一 mainId 下前端传了两个不同箱唛号，数据异常
                throw new ServiceException(CharSequenceUtil.format("装箱主键【{}】对应多个箱唛号，请检查提交数据", mainId));
            }
            if (CharSequenceUtil.isNotBlank(code)) {
                String oldMainId = mainIdByCode.putIfAbsent(code, mainId);
                if (CharSequenceUtil.isNotBlank(oldMainId) && !CharSequenceUtil.equals(oldMainId, mainId)) {
                    // 同一箱唛号出现在两个不同 mainId 下，判定为重复提交
                    throw new ServiceException(CharSequenceUtil.format("箱唛号【{}】重复，请勿重复提交", code));
                }
            }
            // 同一箱内同一 SKU+移出仓位 的明细行不允许重复（前端应直接合并数量后提交）
            String detailKey = mainId + "|" + CharSequenceUtil.trim(line.getSkuNo()) + "|"
                    + CharSequenceUtil.trim(line.getOutWarehouseLocationCode());
            if (!submittedDetailKeys.add(detailKey)) {
                throw new ServiceException("箱唛明细重复，请勿重复提交同一箱唛");
            }
        }

        // ── 第二步：批量查询数据库，校验箱唛合法性 ───────────────────────────────
        // 后端重新查询，防止前端绕过状态检查；同时避免在箱唛循环内逐条查询数据库。
        // 校验项：① usageStatus 必须为 false  ② isMoveWarehouse 必须为 false（未移仓）
        List<AfterSalePackDTO.ViewDTO> boxInfoList = new ArrayList<>();
        List<SubmittedBox> submittedBoxes = new ArrayList<>(submittedBoxMap.values());
        List<String> boxCodes = submittedBoxes.stream()
                .map(submittedBox -> submittedBox.code)
                .filter(CharSequenceUtil::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        Map<String, AfterSalePackDTO.ViewDTO> boxInfoByCode = CollUtil.isEmpty(boxCodes)
                ? Collections.emptyMap()
                : afterSalePackService.viewByCodes(boxCodes).stream()
                .collect(Collectors.toMap(boxInfo -> CharSequenceUtil.trim(boxInfo.getCode()), boxInfo -> boxInfo, (a, b) -> a));
        for (SubmittedBox submittedBox : submittedBoxes) {
            AfterSalePackDTO.ViewDTO boxInfo = CharSequenceUtil.isNotBlank(submittedBox.code)
                    ? boxInfoByCode.get(submittedBox.code)
                    : afterSalePackService.view(submittedBox.mainId);
            if (boxInfo == null) {
                throw new ServiceException(CharSequenceUtil.format("箱唛号【{}】不存在", submittedBox.code));
            }
            String mainId = submittedBox.mainId;
            // 校验查询结果与前端提交的 mainId 一致
            if (!CharSequenceUtil.equals(mainId, CharSequenceUtil.trimToEmpty(boxInfo.getId()))) {
                throw new ServiceException(CharSequenceUtil.format("装箱主键与查询结果不一致：提交【{}】查询【{}】", mainId, boxInfo.getId()));
            }
            // 校验查询结果与前端提交的箱唛号一致（防止 code 与 mainId 错位）
            if (CharSequenceUtil.isNotBlank(submittedBox.code) && !CharSequenceUtil.equals(submittedBox.code, CharSequenceUtil.trim(boxInfo.getCode()))) {
                throw new ServiceException(CharSequenceUtil.format("箱唛号与查询结果不一致：提交【{}】查询【{}】", submittedBox.code, boxInfo.getCode()));
            }
            // 校验 usageStatus：isUse 必须为 false
            assertUsageStatusAllowsMove(boxInfo);
            // 校验移仓状态：isMoveWarehouse 为 true 表示已移仓，不允许重复操作
            if (Boolean.TRUE.equals(boxInfo.getIsMoveWarehouse())) {
                String label = CharSequenceUtil.blankToDefault(CharSequenceUtil.trim(boxInfo.getCode()), boxInfo.getId());
                throw new ServiceException(CharSequenceUtil.format("箱唛【{}】已完成移仓，不支持重复移仓", label));
            }
            boxInfoList.add(boxInfo);
        }

        // ── 第三步：以"SKU + 移出仓位 + 移入仓位"为维度汇总所有箱子的装箱明细 ──
        // 多个箱子中相同 SKU 且相同移出仓位的数量累加为一条移仓子项；
        // 不同移出仓位的同一 SKU 保留为独立子项（对应图示 sku01 2A01/sku01 2A03 分两行）。
        // 使用 LinkedHashMap 保持插入顺序，确保生成的子项顺序稳定。
        LinkedHashMap<String, FullBoxTransferAggregate> aggregateMap = new LinkedHashMap<>();
        for (AfterSalePackDTO.ViewDTO boxInfo : boxInfoList) {
            if (CollUtil.isEmpty(boxInfo.getDetailViewDTOList())) {
                throw new ServiceException(CharSequenceUtil.format("箱唛【{}】无装箱明细", CharSequenceUtil.blankToDefault(boxInfo.getCode(), boxInfo.getId())));
            }
            String boxDisplay = CharSequenceUtil.blankToDefault(CharSequenceUtil.trim(boxInfo.getCode()), CharSequenceUtil.trim(boxInfo.getId()));
            for (AfterSalePackDetailDTO.ViewDTO detail : boxInfo.getDetailViewDTOList()) {
                validateQty(detail.getPackQty());
                String skuNo = CharSequenceUtil.trim(detail.getSkuNo());
                String sourceLoc = CharSequenceUtil.trim(detail.getOutWarehouseLocationCode());
                if (CharSequenceUtil.isBlank(skuNo)) {
                    throw new ServiceException(CharSequenceUtil.format("箱唛【{}】存在空 SKU 明细", boxDisplay));
                }
                if (CharSequenceUtil.isBlank(sourceLoc)) {
                    throw new ServiceException(CharSequenceUtil.format("箱唛【{}】SKU【{}】源仓位为空", boxDisplay, skuNo));
                }
                // 聚合 key = skuNo + 移出仓位 + 移入仓位（目标仓位本批次唯一）
                String aggregateKey = skuNo + "|" + sourceLoc + "|" + targetCode;
                FullBoxTransferAggregate aggregate = aggregateMap.computeIfAbsent(aggregateKey,
                        key -> new FullBoxTransferAggregate(skuNo, sourceLoc, targetCode));
                aggregate.addQty(detail.getPackQty());
                aggregate.addBoxDisplay(boxDisplay);
                // 用装箱明细里的 skuId 补充聚合行的 skuId，若多箱同 SKU 的 skuId 不一致则报错
                String detailSkuId = CharSequenceUtil.trim(detail.getSkuId());
                if (CharSequenceUtil.isNotBlank(detailSkuId)) {
                    if (CharSequenceUtil.isBlank(aggregate.skuId)) {
                        aggregate.skuId = detailSkuId;
                    } else if (!CharSequenceUtil.equals(aggregate.skuId, detailSkuId)) {
                        throw new ServiceException(CharSequenceUtil.format("SKU【{}】在箱唛明细中的 skuId 不一致", skuNo));
                    }
                }
            }
        }

        // ── 第四步：批量查询 PLM SKU 信息，校验 skuId 匹配 ───────────────────
        List<String> skuNos = aggregateMap.values().stream()
                .map(aggregate -> aggregate.skuNo)
                .distinct()
                .collect(Collectors.toList());
        List<SkuVO> skuVOList = plmTaskFeign.listBySkuNoList(skuNos);
        Map<String, SkuVO> skuByNo = new HashMap<>(16);
        if (CollUtil.isNotEmpty(skuVOList)) {
            skuByNo.putAll(skuVOList.stream()
                    .filter(vo -> CharSequenceUtil.isNotBlank(vo.getSkuNo()))
                    .collect(Collectors.toMap(vo -> CharSequenceUtil.trim(vo.getSkuNo()), vo -> vo, (a, b) -> a)));
        }

        // ── 第五步：逐聚合行校验库存（合计数量），并组装仓位移动明细 ──────────
        // 注意：此处校验的是聚合后的合计数量，避免多箱同 SKU 逐行通过、整体超库存的问题
        List<WarehouseLocationMoveDetailDTO.AddDTO> detailList = new ArrayList<>();
        for (FullBoxTransferAggregate aggregate : aggregateMap.values()) {
            validateQty(aggregate.qty);

            String skuNo = aggregate.skuNo;
            SkuVO skuVO = skuByNo.get(skuNo);
            if (skuVO == null) {
                throw new ServiceException(CharSequenceUtil.format("未找到 SKU【{}】", skuNo));
            }
            if (CharSequenceUtil.isNotBlank(aggregate.skuId) && !CharSequenceUtil.equals(aggregate.skuId, skuVO.getSkuId())) {
                throw new ServiceException(CharSequenceUtil.format("SKU【{}】与 skuId 不匹配", skuNo));
            }

            assertUsableQtyAtLocation(warehouseId, orgId, skuVO.getSkuId(), aggregate.sourceLoc, aggregate.qty, skuNo);

            // 备注中列出所有来源箱唛，便于事后追溯
            String remark = CharSequenceUtil.format("整箱移仓{}", aggregate.boxDisplay());
            if (CharSequenceUtil.isNotBlank(dto.getRemark())) {
                remark = remark + " " + CharSequenceUtil.trim(dto.getRemark());
            }

            WarehouseLocationMoveDetailDTO.AddDTO detail = new WarehouseLocationMoveDetailDTO.AddDTO();
            detail.setSkuId(skuVO.getSkuId());
            detail.setSkuNo(skuNo);
            detail.setOutWarehouseLocation(aggregate.sourceLoc);
            detail.setInWarehouseLocation(aggregate.targetLoc);
            detail.setQty(aggregate.qty);
            detail.setRemark(remark);
            detailList.add(detail);
        }

        WarehouseLocationMoveDTO.AddDTO addDTO = new WarehouseLocationMoveDTO.AddDTO();
        addDTO.setWarehouseId(warehouseId);
        addDTO.setDetailList(detailList);
        addDTO.setPcShow(false);
        addDTO.setSyncOperate(WarehouseLocationMoveSyncOperateEnum.FULL_BOX_TRANSFER.getCode());

        log.info("售后PDA整箱移仓 warehouseId={} target={} lineCount={}", warehouseId, targetCode, detailList.size());
        String moveId = warehouseLocationMoveService.addAndApprove(addDTO);

        // ── 第六步：保存来源明细到 wms_move_src_detail ──────────────────────────
        // addAndApprove 返回的 moveId 用于关联主单（main_id）；
        // 再查一次刚落库的 warehouse_location_move_detail，用 SKU+移出仓位+移入仓位 匹配出 detail_id，
        // 这样每一条来源明细行就与汇总明细行建立了父子关系，满足事后拆箱查看的需求。
        saveBoxTransferDetails(moveId, targetCode, boxInfoList, skuByNo);

        // ── 第七步：标记箱唛为已移仓（is_move_warehouse = true）────────────────
        // 内部会重新从库查询最新状态（防止卡顿/重复提交的并发窗口），
        // 并使用条件更新（WHERE is_move_warehouse = false）保证原子性。
        List<String> boxIds = boxInfoList.stream()
                .map(b -> CharSequenceUtil.trimToEmpty(b.getId()))
                .filter(CharSequenceUtil::isNotBlank)
                .collect(Collectors.toList());
        afterSalePackService.markBoxesAsMoved(boxIds);

        return moveId;
    }

    /**
     * 在整箱移仓主单落库后，将箱唛维度的原始明细保存到 wms_box_move_detail。
     *
     * @param moveId      刚创建的仓位移动主单 ID（对应 main_id 字段）
     * @param targetCode  移入仓位编码（本批次唯一目标仓位）
     * @param boxInfoList 已通过 usageStatus 校验的装箱单列表（包含装箱明细）
     * @param skuByNo     从 PLM 查询得到的 SKU 信息，key 为 skuNo
     */
    private void saveBoxTransferDetails(String moveId, String targetCode,
                                        List<AfterSalePackDTO.ViewDTO> boxInfoList,
                                        Map<String, SkuVO> skuByNo) {
        // 查询刚保存的移仓子表明细，以"skuNo|移出仓位|移入仓位"为 key 建立快速查找表，
        // 用于将箱唛明细行关联到对应的汇总明细行（detail_id）
        List<WarehouseLocationMoveDetailEntity> savedDetails =
                warehouseLocationMoveDetailService.listByMainIds(Collections.singletonList(moveId));
        Map<String, String> detailIdByKey = savedDetails.stream().collect(
                Collectors.toMap(
                        d -> d.getSkuNo() + "|" + d.getOutWarehouseLocation() + "|" + d.getInWarehouseLocation(),
                        WarehouseLocationMoveDetailEntity::getId,
                        (a, b) -> a
                )
        );

        List<AfterSalesFullBoxTransferDetailEntity> boxDetailList = new ArrayList<>();
        for (AfterSalePackDTO.ViewDTO boxInfo : boxInfoList) {
            // source_code 存箱唛号，source_id 存装箱单主键；
            // 字段设计为通用来源，后续若接入其他来源类型可直接复用
            String sourceCode = CharSequenceUtil.trimToEmpty(boxInfo.getCode());
            String sourceId = CharSequenceUtil.trimToEmpty(boxInfo.getId());
            if (CollUtil.isEmpty(boxInfo.getDetailViewDTOList())) {
                continue;
            }
            for (AfterSalePackDetailDTO.ViewDTO detail : boxInfo.getDetailViewDTOList()) {
                String skuNo = CharSequenceUtil.trimToEmpty(detail.getSkuNo());
                String sourceLoc = CharSequenceUtil.trimToEmpty(detail.getOutWarehouseLocationCode());
                // 关联对应的汇总明细行 ID（detail_id）
                String detailId = detailIdByKey.get(skuNo + "|" + sourceLoc + "|" + targetCode);
                // 取 PLM 返回的 skuId（优先），装箱明细的 skuId 作为兜底
                SkuVO skuVO = skuByNo.get(skuNo);
                String skuId = skuVO != null ? skuVO.getSkuId()
                        : CharSequenceUtil.trimToEmpty(detail.getSkuId());

                AfterSalesFullBoxTransferDetailEntity entity = new AfterSalesFullBoxTransferDetailEntity();
                entity.setMainId(moveId);
                entity.setDetailId(detailId);
                entity.setSourceCode(sourceCode);
                entity.setSourceId(sourceId);
                entity.setSkuId(skuId);
                entity.setSkuNo(skuNo);
                entity.setOutWarehouseLocation(sourceLoc);
                entity.setInWarehouseLocation(targetCode);
                entity.setQty(detail.getPackQty());
                boxDetailList.add(entity);
            }
        }

        if (CollUtil.isNotEmpty(boxDetailList)) {
            boolean saved = afterSalesFullBoxTransferDetailService.saveBatch(boxDetailList);
            if (!saved) {
                throw new ServiceException("整箱移仓箱唛明细保存失败");
            }
        }
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

    /**
     * 校验指定仓位的 SKU 可用库存是否满足本次移仓需求数量。
     * 使用合计后的 needQty 调用，可防止多箱同 SKU 分批通过、整体超库存的情况。
     */
    private void assertUsableQtyAtLocation(String warehouseId, String orgId, String skuId, String location, int needQty, String skuNo) {
        InventoryEntity inv = inventoryService.findInventory(orgId, warehouseId, skuId, location, InventoryStatusEnum.USABLE.getCode());
        if (inv == null || inv.getQty() == null || inv.getQty() < needQty) {
            throw new ServiceException(CharSequenceUtil.format("【{}】sku移出仓位库存不足，不支持整箱移仓", skuNo));
        }
    }

    /**
     * 前端提交的箱唛行经过去重后保存的最小标识：主键 + 箱唛号。
     * 一个 mainId 对应数据库中唯一一个售后装箱单；code 为可选的箱唛号，
     * 优先用 code 查询（更精准），无 code 时退化为按 mainId 查询。
     */
    private static class SubmittedBox {
        private final String mainId;
        private final String code;

        private SubmittedBox(String mainId, String code) {
            this.mainId = mainId;
            this.code = code;
        }
    }

    /**
     * 以"SKU + 移出仓位 + 移入仓位"为维度的移仓数量聚合器。
     * <p>
     * 多个箱子中满足相同三元组的明细会累加到同一个聚合行，
     * 最终对应仓位移动单中的一条子项（{@link WarehouseLocationMoveDetailDTO.AddDTO}）。
     * {@link #boxDisplays} 记录所有来源箱唛，用于生成可追溯的备注信息。
     */
    private static class FullBoxTransferAggregate {
        /** SKU 编码 */
        private final String skuNo;
        /** 移出仓位编码 */
        private final String sourceLoc;
        /** 移入仓位编码（本批次唯一目标仓位） */
        private final String targetLoc;
        /** 参与汇总的箱唛号集合（有序去重，用于备注拼接） */
        private final LinkedHashSet<String> boxDisplays = new LinkedHashSet<>();
        /** SKU 主键，从装箱明细中取得；为空时由 PLM 查询结果填充 */
        private String skuId;
        /** 汇总数量 */
        private int qty;

        private FullBoxTransferAggregate(String skuNo, String sourceLoc, String targetLoc) {
            this.skuNo = skuNo;
            this.sourceLoc = sourceLoc;
            this.targetLoc = targetLoc;
        }

        private void addQty(Integer addQty) {
            this.qty += addQty;
        }

        private void addBoxDisplay(String boxDisplay) {
            this.boxDisplays.add(boxDisplay);
        }

        /** 返回逗号分隔的来源箱唛号字符串，用于备注 */
        private String boxDisplay() {
            return String.join(",", boxDisplays);
        }
    }
}
