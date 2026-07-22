package com.erp.server.wms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.annotation.DistributeLocker;
import com.common.business.config.DocNoGenHelper;
import com.common.business.dto.base.ApproveOneDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.ApproveTypeEnum;
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.vo.PagingVO;
import com.common.business.wrapper.FeignQuery;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.MessageUtils;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.oms.enums.BillTypeEnum;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.sys.entity.SysAccountingCompanyEntity;
import com.erp.model.sys.entity.SysDepartmentEntity;
import com.erp.model.wms.dto.SoReturnInstockDTO;
import com.erp.model.wms.dto.SoReturnInstockDetailDTO;
import com.erp.model.wms.dto.SoReturnPrestockDTO;
import com.erp.model.wms.dto.SoReturnPrestockDetailDTO;
import com.erp.model.wms.entity.*;
import com.erp.model.wms.enums.InstockTypeEnum;
import com.erp.model.wms.enums.InventoryDirectionEnum;
import com.erp.model.wms.enums.PrestockClaimStatusEnum;
import com.erp.model.wms.enums.PrestockSourceTypeEnum;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.wms.constant.WmsConstant;
import com.erp.server.wms.convert.OtherInStockConverter;
import com.erp.server.wms.mapper.SoReturnPrestockMapper;
import com.erp.server.wms.service.*;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 预入库单 ServiceImpl
 *
 * @author auto
 * @since 2026-06-30
 */
@Slf4j
@Service
public class SoReturnPrestockServiceImpl
        extends SuperServiceImpl<SoReturnPrestockMapper, SoReturnPrestockEntity>
        implements SoReturnPrestockService {

    private static final String SO_RETURN_PRESTOCK_HEADLESS_LOCK_KEY = "SO_RETURN_PRESTOCK_HEADLESS";

    private static final String SO_RETURN_PRESTOCK_LINK_LOCK_KEY = "SO_RETURN_PRESTOCK_LINK";

    /**
     * 单次确认关联最多允许生成的退货入库单数量（按售后单/店铺分组数）。
     * confirmLinkAfterSale/confirmLinkShop 在同一个事务内循环调用 SoReturnInstockService#add 落库，
     * 分组数过多会导致长事务、锁等待时间变长；超过阈值直接拒绝并提示分批操作，而不做架构级两阶段重构。
     */
    private static final int LINK_GROUP_MAX_SIZE = 50;

    /**
     * 确认关联循环耗时告警阈值（毫秒），超过该阈值仅记录警告日志，便于后续评估是否需要拆分事务，不阻断业务
     */
    private static final long LINK_LOOP_WARN_THRESHOLD_MS = 3000L;

    /**
     * 强制关闭未认领预入库单时，单批处理的预入库单数量上限。
     * 用于控制 in 语句参数规模与批量更新粒度，避免单次事务锁范围过大。
     */
    private static final int FORCE_CLOSE_BATCH_SIZE = 500;

    /**
     * so_return_prestock_detail.product_name 列长度上限（varchar(255)）
     */
    private static final int PRODUCT_NAME_MAX_LENGTH = 255;

    /**
     * so_return_prestock_detail.product_image_url 列长度上限（varchar(500)）
     */
    private static final int PRODUCT_IMAGE_URL_MAX_LENGTH = 500;

    @Resource
    private DocNoGenHelper docNoGenHelper;

    @Resource
    private SoReturnPrestockDetailService soReturnPrestockDetailService;

    @Resource
    private SoReturnInstockService soReturnInstockService;

    @Resource
    private SoReturnInstockDetailService soReturnInstockDetailService;

    @Resource
    private WarehouseService warehouseService;

    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private OtherInstockService otherInstockService;

    @Resource
    private OtherInstockDetailService otherInstockDetailService;

    @Resource
    private OperateLogService operateLogService;

    /**
     * 自注入代理：forceCloseUnclaimedPrestock 需要让每个批次的 forceCloseBatch / forceCloseSingle
     * 在独立事务中提交，且 forceCloseSingle 上的 {@code @DistributeLocker} 必须通过 Spring 代理调用才能生效；
     * 使用 @Lazy 避免 Bean 初始化阶段的循环依赖。
     */
    @Lazy
    @Resource
    private SoReturnPrestockService self;

    // ===================== 分页查询 =====================

    @Override
    public PagingVO<SoReturnPrestockDTO.PagingView> paging(PagingDTO<SoReturnPrestockDTO.PagingParam> dto) {
        Page<SoReturnPrestockDTO.PagingView> page = new Page<>(dto.getPage(), dto.getPageSize());
        IPage<SoReturnPrestockDTO.PagingView> result = baseMapper.paging(page, dto.getParams());
        List<SoReturnPrestockDTO.PagingView> records = result.getRecords();
        List<String> ids = records.stream().map(SoReturnPrestockDTO.PagingView::getId).collect(Collectors.toList());
        // 查询详情行
        List<SoReturnPrestockDetailEntity> detailEntities = soReturnPrestockDetailService.listByMainIds(ids);
        Map<String, List<SoReturnPrestockDetailEntity>> detailMap = detailEntities.stream().collect(Collectors.groupingBy(SoReturnPrestockDetailEntity::getMainId));
        // 批量反查认领后生成的退货入库单（当前页全部明细行只发一次查询，不逐行查）
        List<String> detailIds = detailEntities.stream().map(SoReturnPrestockDetailEntity::getId).collect(Collectors.toList());
        Map<String, SoReturnInstockEntity> returnInstockByPrestockDetailId = listReturnInstockByPrestockDetailIds(detailIds);
        // 翻译枚举名称
        records.forEach(v -> {
            v.setTypeName(BillTypeEnum.getName(v.getType()));
            v.setClaimStatusName(PrestockClaimStatusEnum.getName(v.getClaimStatus()));
            v.setSourceTypeName(PrestockSourceTypeEnum.getName(v.getSourceType()));
            List<SoReturnPrestockDetailEntity> details = detailMap.getOrDefault(v.getId(), Collections.emptyList());
            v.setDetailList(details.stream().map(d -> convertDetailToView(d, returnInstockByPrestockDetailId)).collect(Collectors.toList()));
        });
        return new PagingVO<>(result);
    }

    // ===================== 新增 =====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String add(SoReturnPrestockDTO.Add dto) {
        // 物流单号唯一性校验
        checkLogisticCodeUnique(dto.getReturnLogisticCode(), null);
        SoReturnPrestockEntity entity = buildAndPersist(dto, dto.getReturnLogisticCode(), PrestockSourceTypeEnum.MANUAL.getStatus());
        // 操作日志
        operateLogService.addModuleOperateLog(String.format("新增了一个预入库单【%s】", entity.getCode()),
                ModuleTypeEnum.SO_RETURN_PRESTOCK.getCode(), entity.getId(), "新增操作");
        return entity.getId();
    }

    // ===================== 详情 =====================

    @Override
    public SoReturnPrestockDTO.View view(String id) {
        SoReturnPrestockEntity entity = getByIdOrThrow(id);
        SoReturnPrestockDTO.View view = new SoReturnPrestockDTO.View();
        view.setId(entity.getId());
        view.setVersion(entity.getVersion());
        view.setCode(entity.getCode());
        view.setType(entity.getType());
        view.setTypeName(BillTypeEnum.getName(entity.getType()));
        view.setReturnLogisticCode(entity.getReturnLogisticCode());
        view.setClaimStatus(entity.getClaimStatus());
        view.setClaimStatusName(PrestockClaimStatusEnum.getName(entity.getClaimStatus()));
        view.setSourceType(entity.getSourceType());
        view.setSourceTypeName(PrestockSourceTypeEnum.getName(entity.getSourceType()));
        view.setThirdCode(entity.getThirdCode());
        view.setInventoryOrgId(entity.getInventoryOrgId());
        view.setInventoryOrgName(entity.getInventoryOrgName());
        view.setWarehouseId(entity.getWarehouseId());
        view.setWarehouseName(entity.getWarehouseName());
        view.setDictReturnType(entity.getDictReturnType());
        view.setReceivedTime(entity.getReceivedTime());
        view.setOperateTime(entity.getOperateTime());
        view.setRemark(entity.getRemark());
        view.setCreateUserName(entity.getCreateUserName());
        view.setCreateTime(entity.getCreateTime());

        // 查询详情行
        List<SoReturnPrestockDetailEntity> detailEntities =
                soReturnPrestockDetailService.listByMainId(id);
        // 批量反查认领后生成的退货入库单（本单全部明细行只发一次查询，不逐行查）
        List<String> detailIds = detailEntities.stream().map(SoReturnPrestockDetailEntity::getId).collect(Collectors.toList());
        Map<String, SoReturnInstockEntity> returnInstockByPrestockDetailId = listReturnInstockByPrestockDetailIds(detailIds);
        view.setDetailList(detailEntities.stream()
                .map(d -> convertDetailToView(d, returnInstockByPrestockDetailId))
                .collect(Collectors.toList()));
        return view;
    }

    // ===================== 确认关联售后单（预入库单维度批量） =====================

    @Override
    @DistributeLocker(businessType = SO_RETURN_PRESTOCK_LINK_LOCK_KEY, keyName = "dto.mainId")
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    public Boolean confirmLinkAfterSale(SoReturnPrestockDetailDTO.ConfirmLinkAfterSale dto) {
        SoReturnPrestockEntity main = getByIdOrThrow(dto.getMainId());
        if (PrestockClaimStatusEnum.FORCE_CLOSE.getStatus().equals(main.getClaimStatus())) {
            throw new ServiceException(ApiError.SO_RETURN_PRESTOCK_FORCE_CLOSE);
        }

        // 售后订单类型（补/换/赠）不允许关联售后单，只能走关联店铺
        if (BillTypeEnum.AFTER_SALES.getCode().equals(main.getType())) {
            throw new ServiceException(ApiError.SO_RETURN_PRESTOCK_AFTER_SALE_TYPE_LINK_FORBIDDEN);
        }

        // B2C售后单平台字典值为必填（B2B售后单无平台概念，允许为空，故不能在DTO层统一加@NotBlank）
        if (BillTypeEnum.B2C.getCode().equals(main.getType())) {
            boolean anyDictPlatformBlank = dto.getAfterSaleList().stream()
                    .anyMatch(item -> CharSequenceUtil.isBlank(item.getDictPlatform()));
            if (anyDictPlatformBlank) {
                throw new ServiceException(ApiError.SO_RETURN_PRESTOCK_B2C_PLATFORM_REQUIRED);
            }
        }

        // 仅在未关联的明细行范围内进行匹配，避免覆盖已关联售后单或已强制关闭的行
        List<SoReturnPrestockDetailEntity> unlinked = soReturnPrestockDetailService.listByMainId(dto.getMainId())
                .stream()
                .filter(d -> PrestockClaimStatusEnum.UNLINKED.getStatus().equals(d.getClaimStatus()))
                .collect(Collectors.toList());
        if (CollUtil.isEmpty(unlinked)) {
            throw new ServiceException(ApiError.SO_RETURN_PRESTOCK_ALL_LINKED);
        }

        // 预入库单未关联明细：按 SKU 聚合实际收货数量（receivedQty），作为该 SKU 的可认领上限
        Map<String, Integer> prestockReceivedQtyMap = unlinked.stream()
                .collect(Collectors.groupingBy(SoReturnPrestockDetailEntity::getSkuNo,
                        Collectors.summingInt(d -> Objects.nonNull(d.getReceivedQty()) ? d.getReceivedQty() : 0)));
        // 本次勾选的售后单退货明细：按 SKU 聚合退货数量（认领数量）
        Map<String, Integer> afterSaleQtyMap = dto.getAfterSaleList().stream()
                .collect(Collectors.groupingBy(SoReturnPrestockDetailDTO.AfterSaleItem::getSkuNo,
                        Collectors.summingInt(i -> Objects.nonNull(i.getReturnQty()) ? i.getReturnQty() : 0)));

        // 逐 SKU 比较勾选退货数量（认领数量）与预入库单实际收货数量：
        // - SKU 不在预入库单未关联明细中 → 属于异常包裹/源头售后单有误，引导改走「关联店铺」
        // - 勾选退货数量 > 实际收货数量 → 认领失败
        // - 勾选退货数量 == 实际收货数量 → 该 SKU 全部关联；勾选退货数量 < 实际收货数量 → 仅关联勾选部分（部分关联）
        for (Map.Entry<String, Integer> entry : afterSaleQtyMap.entrySet()) {
            Integer receivedQty = prestockReceivedQtyMap.get(entry.getKey());
            if (Objects.isNull(receivedQty)) {
                throw new ServiceException(ApiError.SO_RETURN_PRESTOCK_LINK_AFTER_SALE_SKU_TYPE_EXCEEDS, entry.getKey());
            }
            if (entry.getValue() > receivedQty) {
                throw new ServiceException(ApiError.SO_RETURN_PRESTOCK_LINK_AFTER_SALE_SKU_RECEIVE_QTY_EXCEEDS, entry.getKey());
            }
        }

        // 逐条售后单明细分配到同 SKU 的未关联明细行（按需拆行）；预校验已保证同 SKU 数量充足。
        // 此处仅在内存中变更明细行并收集"本次关联行→售后单明细"映射，统一在生成退货入库单、回写单号后落库
        Map<String, Deque<SoReturnPrestockDetailEntity>> skuRowQueue = unlinked.stream()
                .collect(Collectors.groupingBy(SoReturnPrestockDetailEntity::getSkuNo,
                        Collectors.toCollection(LinkedList::new)));
        List<LinkedDetailPair> linkedPairs = new ArrayList<>();
        // 拆行仅在内存中进行，产生的新明细行统一收集到此处，最后作为「新增」一次性插入；
        // 避免「先 save 拆行新行、随后又对同一新行乐观锁 updateById」在同事务内触发 version 冲突
        List<SoReturnPrestockDetailEntity> newRows = new ArrayList<>();
        for (SoReturnPrestockDetailDTO.AfterSaleItem item : dto.getAfterSaleList()) {
            allocateAfterSaleItem(skuRowQueue.get(item.getSkuNo()), item, linkedPairs, newRows);
        }

        // 关联售后单会回填 skuId；回填后仍为空则禁止进入退货入库/库存联动，要求运营先维护映射
        for (LinkedDetailPair pair : linkedPairs) {
            if (CharSequenceUtil.isBlank(pair.detail.getSkuId())) {
                throw new ServiceException(ApiError.SO_RETURN_PRESTOCK_SKU_REQUIRED,
                        CharSequenceUtil.emptyToDefault(pair.detail.getSkuNo(), pair.detail.getId()));
            }
        }

        // 联动处理：为本次已关联的 SKU 按售后单分组生成《退货入库单》，并把生成的入库单号回写到对应明细行（内存）
        generateReturnInstock(main, linkedPairs);

        // 拆行新增行统一插入（含可能已被关联/回写退货入库单号的最终状态）；新行为纯插入，无需乐观锁
        if (CollUtil.isNotEmpty(newRows)) {
            soReturnPrestockDetailService.saveBatch(newRows, 500);
        }
        // 落库本次关联的「已存在」明细行（含关联信息 + 退货入库单回写）；拆行新增行已在上一步插入，跳过以免重复且避免乐观锁冲突
        Set<SoReturnPrestockDetailEntity> newRowSet = Collections.newSetFromMap(new IdentityHashMap<>());
        newRowSet.addAll(newRows);
        for (LinkedDetailPair pair : linkedPairs) {
            if (newRowSet.contains(pair.detail)) {
                continue;
            }
            if (!soReturnPrestockDetailService.updateById(pair.detail)) {
                throw new ServiceException(ApiError.SO_RETURN_PRESTOCK_DETAIL_MODIFIED);
            }
        }

        // 平账联动：反向冲抵预入库单创建时生成的原整单普通(增库存)其他入库单，
        // 并为本次仍未关联的 SKU 重新生成普通(增库存)其他入库单占位（均直接已审核）；
        // 需在本次关联明细行落库后调用，以便按最新关联状态计算未关联占位明细
        reconcileOtherInstockForPrestock(main, resolveWarehousingDept());

        // 刷新主表关联状态（全部已关联→LINKED，混合→PARTIAL）
        refreshMainClaimStatus(main.getId());

        // 操作日志
        String afterSaleCodes = dto.getAfterSaleList().stream()
                .map(SoReturnPrestockDetailDTO.AfterSaleItem::getAfterSaleCode)
                .filter(CharSequenceUtil::isNotBlank)
                .distinct()
                .collect(Collectors.joining("、"));
        operateLogService.addModuleOperateLog(
                String.format("预入库单【%s】确认关联售后单【%s】", main.getCode(),
                        CharSequenceUtil.emptyToDefault(afterSaleCodes, "-")),
                ModuleTypeEnum.SO_RETURN_PRESTOCK.getCode(), main.getId(), "关联操作");
        return true;
    }

    /**
     * 将一条售后单明细数量分配到同 SKU 的未关联预入库单明细行队列。
     * <p>整行数量 ≤ 剩余待分配量时整行关联；否则拆行：当前行保留剩余待分配量并关联，
     * 拆出的剩余数量作为新未关联行重新入队，供后续同 SKU 售后单明细继续分配。
     * 关联的明细行仅在内存中变更，配对信息收集到 collector，由调用方统一落库。</p>
     */
    private void allocateAfterSaleItem(Deque<SoReturnPrestockDetailEntity> rows,
                                       SoReturnPrestockDetailDTO.AfterSaleItem item, List<LinkedDetailPair> collector,
                                       List<SoReturnPrestockDetailEntity> newRows) {
        int remaining = Objects.nonNull(item.getReturnQty()) ? item.getReturnQty() : 0;
        while (remaining > 0 && Objects.nonNull(rows) && !rows.isEmpty()) {
            SoReturnPrestockDetailEntity row = rows.pollFirst();
            int rowQty = Objects.nonNull(row.getReceivedQty()) ? row.getReceivedQty() : 0;
            if (rowQty <= remaining) {
                applyAfterSaleToDetail(row, item);
                collector.add(new LinkedDetailPair(row, item));
                remaining -= rowQty;
            } else {
                // 拆行：当前行关联 remaining，剩余 rowQty-remaining 拆为新未关联行并回队
                // 新行仅在内存中拆分并收集，统一由调用方最后插入，避免同事务内对新行乐观锁更新
                SoReturnPrestockDetailEntity leftover = splitDetailInMemory(row, remaining);
                newRows.add(leftover);
                applyAfterSaleToDetail(row, item);
                collector.add(new LinkedDetailPair(row, item));
                rows.addFirst(leftover);
                remaining = 0;
            }
        }
    }

    /**
     * 联动生成《退货入库单》：将本次已关联的明细行按售后单（退货单）分组，每个售后单生成一张退货入库单
     * （复用 {@link SoReturnInstockService#add}，由其按 soReturnId + type 反查客户/组织/价格等信息并落库、生成单号），
     * 生成后直接提交并审核通过（不走审批流，置为已审核并触发退货入库库存联动）。
     * 上游预入库单明细不记录下游退货入库单的关联指针，改为下游退货入库单明细通过
     * {@code prestock_detail_id} 回指本预入库单明细行（见 {@link #buildInstockDetailAdd}）。
     */
    private void generateReturnInstock(SoReturnPrestockEntity main, List<LinkedDetailPair> linkedPairs) {
        if (CollUtil.isEmpty(linkedPairs)) {
            return;
        }
        if (CharSequenceUtil.isBlank(main.getWarehouseId())) {
            throw new ServiceException(ApiError.SO_RETURN_PRESTOCK_WAREHOUSE_REQUIRED_FOR_INSTOCK);
        }
        LocalDate billDate = LocalDate.now();
        // 按售后单 ID 分组，保持关联顺序便于排查
        Map<String, List<LinkedDetailPair>> pairsByAfterSale = linkedPairs.stream()
                .collect(Collectors.groupingBy(p -> p.item.getAfterSaleId(), LinkedHashMap::new, Collectors.toList()));
        if (pairsByAfterSale.size() > LINK_GROUP_MAX_SIZE) {
            throw new ServiceException(ApiError.SO_RETURN_PRESTOCK_LINK_AFTER_SALE_GROUP_EXCEEDS, pairsByAfterSale.size());
        }
        long loopStart = System.currentTimeMillis();
        for (List<LinkedDetailPair> pairs : pairsByAfterSale.values()) {
            SoReturnPrestockDetailDTO.AfterSaleItem head = pairs.get(0).item;
            SoReturnInstockDTO.Add add = new SoReturnInstockDTO.Add();
            add.setType(main.getType());
            add.setSoReturnId(head.getAfterSaleId());
            add.setSoReturnCode(head.getAfterSaleCode());
            add.setPlatformOrderCode(head.getPlatformOrderCode());
            add.setShopId(head.getShopId());
            add.setBillDate(billDate);
            add.setWarehouseId(main.getWarehouseId());
            add.setReturnLogisticCode(main.getReturnLogisticCode());
            add.setThirdCode(main.getThirdCode());
            // 来源为本预入库单，便于溯源
            add.setSourceId(main.getId());
            add.setSourceCode(main.getCode());
            add.setSourceType(SourceTypeEnum.SO_RETURN_PRESTOCK.getCode());
            add.setDetailList(pairs.stream()
                    .map(p -> buildInstockDetailAdd(main, p.detail,
                            CharSequenceUtil.emptyToDefault(p.item.getReturnType(), main.getDictReturnType()),
                            CharSequenceUtil.emptyToDefault(p.item.getReturnReason(), ""),
                            CharSequenceUtil.emptyToDefault(p.item.getDetailId(), "")))
                    .collect(Collectors.toList()));

            // 新增并拿到落库后的内存实体（含 id、单号），后续提交/审核均基于该实体，避免"写入后再查询"
            SoReturnInstockEntity instock = soReturnInstockService.addReturnEntity(add);
            // 关联售后单生成的退货入库单直接提交并审核通过（不走审批流），置为已审核并触发退货入库库存联动
            approveReturnInstock(instock);
        }
        long loopCost = System.currentTimeMillis() - loopStart;
        if (loopCost > LINK_LOOP_WARN_THRESHOLD_MS) {
            log.warn("[预入库单确认关联售后单]生成退货入库单耗时过长：预入库单={}，分组数={}，耗时={}ms",
                    main.getCode(), pairsByAfterSale.size(), loopCost);
        }
    }

    /**
     * 由已关联的预入库单明细行组装退货入库单明细入参：数量取本行关联数量（应退=签收兜底=实退）。
     *
     * @param returnTypeDict   退货类型字典值（关联售后单时优先取售后单明细，兜底主表；关联店铺时取主表）
     * @param returnReasonDict 退货原因字典值（关联售后单时取售后单明细，关联店铺时为空）
     * @param soReturnDetailId 售后单明细 ID（关联店铺场景为空，仅关联售后单时用于溯源）
     */
    private SoReturnInstockDetailDTO.Add buildInstockDetailAdd(SoReturnPrestockEntity main,
                                                               SoReturnPrestockDetailEntity detail, String returnTypeDict, String returnReasonDict, String soReturnDetailId) {
        int qty = Objects.nonNull(detail.getReceivedQty()) ? detail.getReceivedQty() : 0;
        SoReturnInstockDetailDTO.Add da = new SoReturnInstockDetailDTO.Add();
        da.setSkuId(detail.getSkuId());
        da.setSkuNo(detail.getSkuNo());
        da.setMustQty(qty);
        da.setReceiveQty(qty);
        da.setRealQty(qty);
        da.setWarehouseId(main.getWarehouseId());
        da.setReturnTypeDict(returnTypeDict);
        da.setReturnReasonDict(returnReasonDict);
        da.setSoReturnDetailId(soReturnDetailId);
        // 下游记录来源：回指本预入库单明细行，取代此前由预入库单明细反向记录 return_instock_id/code 的方向；
        // 使用专用字段 prestockDetailId，不复用 sourceDetailId（后者已被 SoReturnReceiveServiceImpl 按
        // "签收单明细id"语义读取，复用会导致同一字段承载两种互不相关的语义）
        da.setPrestockDetailId(detail.getId());
        // 透传海外仓/预入库明细的不良品标识，避免经预入库关联后再生成退货入库时丢失
        da.setDefectiveProductFlag(Boolean.TRUE.equals(detail.getDefectiveProductFlag()));
        // 预入库单不存在退货签收单，签收数量兜底为 0 会误触发"实退总数量不能大于签收数量"校验；
        // 预入库单明细的 receivedQty 即已确认的到货数量，此处关闭签收数量校验
        da.setIsCheckReceiveQty(false);
        return da;
    }

    /**
     * 本次关联的预入库单明细行与其来源售后单明细的配对，用于关联落库与按售后单分组生成退货入库单
     */
    private static class LinkedDetailPair {
        private final SoReturnPrestockDetailEntity detail;
        private final SoReturnPrestockDetailDTO.AfterSaleItem item;

        LinkedDetailPair(SoReturnPrestockDetailEntity detail, SoReturnPrestockDetailDTO.AfterSaleItem item) {
            this.detail = detail;
            this.item = item;
        }
    }

    /**
     * 将勾选的售后单明细信息写入预入库单明细行并置为已关联。
     * <p>本域"售后单"即 OMS 退货单，故 afterSale 与 soReturn 同源；原销售单 ID/单号取自候选售后单列表
     * 出参（B2B=so_return.source_id/source_code，B2C=so_b2c_return.so_id/so_code）；销售组织/部门
     * 不在候选售后单列表出参中，留空由后续认领流程补齐。</p>
     */
    private void applyAfterSaleToDetail(SoReturnPrestockDetailEntity row,
                                        SoReturnPrestockDetailDTO.AfterSaleItem item) {
        // 三无包裹创建的预入库单明细 sku_id 可能为空，关联售后单时用售后单明细带回的 skuId 回填，
        // 保证后续生成退货入库单及库存联动时 sku_id 非空
        if (CharSequenceUtil.isBlank(row.getSkuId()) && CharSequenceUtil.isNotBlank(item.getSkuId())) {
            row.setSkuId(item.getSkuId());
        }
        row.setAfterSaleId(item.getAfterSaleId())
                .setAfterSaleCode(CharSequenceUtil.emptyToDefault(item.getAfterSaleCode(), ""))
                .setPlatformOrderCode(CharSequenceUtil.emptyToDefault(item.getPlatformOrderCode(), ""))
                .setDictPlatform(CharSequenceUtil.emptyToDefault(item.getDictPlatform(), ""))
                .setSoId(CharSequenceUtil.emptyToDefault(item.getSoId(), ""))
                .setSoCode(CharSequenceUtil.emptyToDefault(item.getSoCode(), ""))
                .setSoReturnId(item.getAfterSaleId())
                .setSoReturnCode(CharSequenceUtil.emptyToDefault(item.getAfterSaleCode(), ""))
                .setShopId(CharSequenceUtil.emptyToDefault(item.getShopId(), ""))
                .setShopName(CharSequenceUtil.emptyToDefault(item.getShopName(), ""))
                .setSalesOrgId("").setSalesOrgName("")
                .setSalesDeptId("").setSalesDeptName("")
                .setSellerId("").setSellerName("")
                // 已认领数量与关联店铺口径一致：取本行分配/拆行后收敛的实际收货数量，
                // 否则会停留在预入库单创建时初始化的 0
                .setClaimedQty(Objects.nonNull(row.getReceivedQty()) ? row.getReceivedQty() : 0)
                .setClaimStatus(PrestockClaimStatusEnum.LINKED.getStatus());
    }

    // ===================== 关联店铺（批量） =====================

    @Override
    @DistributeLocker(businessType = SO_RETURN_PRESTOCK_LINK_LOCK_KEY, keyName = "dto.ids")
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    public List<BatchResultDTO> linkShop(SoReturnPrestockDetailDTO.LinkShop dto) {
        // 入参 ids 为预入库单主表 so_return_prestock 的 ID 列表
        List<String> distinctIds = CollUtil.isEmpty(dto.getIds()) ? Collections.emptyList()
                : dto.getIds().stream().filter(CharSequenceUtil::isNotBlank).distinct().collect(Collectors.toList());
        if (CollUtil.isEmpty(distinctIds)) {
            throw new ServiceException(ApiError.COMMON_PARAM_REQUIRED, "预入库单");
        }

        // 批量查询主表（MyBatis-Plus 逻辑删除自动过滤已软删数据）
        List<SoReturnPrestockEntity> mains = listByIds(distinctIds);
        if (CollUtil.isEmpty(mains) || mains.size() != distinctIds.size()) {
            throw new ServiceException(ApiError.SO_RETURN_PRESTOCK_NOT_FOUND_OR_DELETED);
        }

        // B2B 与 B2C 的"关联店铺"客户解析口径不同（B2B 店铺id即客户id，B2C 需按店铺反查客户），
        // 且前端选店铺的候选来源也不同，故不允许一次批量混合两种类型，需分别关联
        Set<String> billTypes = mains.stream().map(SoReturnPrestockEntity::getType)
                .filter(CharSequenceUtil::isNotBlank).collect(Collectors.toSet());
        if (billTypes.contains(BillTypeEnum.B2B.getCode()) && billTypes.contains(BillTypeEnum.B2C.getCode())) {
            throw new ServiceException(ApiError.SO_RETURN_PRESTOCK_BILL_TYPE_MIXED_FORBIDDEN);
        }

        // 关联对象必填校验（入参已移除注解校验，改由此处按类型强制）：B2B 必须传客户id，B2C 必须传店铺id
        if (billTypes.contains(BillTypeEnum.B2B.getCode())) {
            if (CharSequenceUtil.isBlank(dto.getCustomerId())) {
                throw new ServiceException(ApiError.SO_RETURN_PRESTOCK_LINK_CUSTOMER_REQUIRED);
            }
        } else if (CharSequenceUtil.isBlank(dto.getShopId())) {
            throw new ServiceException(ApiError.SO_RETURN_PRESTOCK_LINK_SHOP_REQUIRED);
        }

        // 批量查询这些主表下的所有明细行，按主表 ID 分组
        List<SoReturnPrestockDetailEntity> allDetails = soReturnPrestockDetailService.listByMainIds(distinctIds);
        Map<String, List<SoReturnPrestockDetailEntity>> detailMap = allDetails.stream()
                .collect(Collectors.groupingBy(SoReturnPrestockDetailEntity::getMainId));

        // 仓储部门为固定兜底部门，批量入口下会被多张单反复用到，事务内一次性预取，避免循环内逐单发起 Feign 调用
        SysDepartmentEntity warehousingDept = resolveWarehousingDept();

        // 逐张预入库单独立处理：多张预入库单即使关联同一店铺，也各自成单（各自生成退货入库单与平账其他入库单），
        // 不合并为"一个关联单"。仅关联尚未关联的明细行，避免覆盖已关联售后单/店铺的行
        List<BatchResultDTO> results = new ArrayList<>(mains.size());
        for (SoReturnPrestockEntity main : mains) {
            // 已关联的预入库单不可重复关联
            if (PrestockClaimStatusEnum.LINKED.getStatus().equals(main.getClaimStatus())) {
                results.add(BatchResultDTO.fail(main.getId(), main.getCode(), "该预入库单已关联，不可重复关联"));
                continue;
            }
            // 已强制关闭的预入库单不可再关联
            if (PrestockClaimStatusEnum.FORCE_CLOSE.getStatus().equals(main.getClaimStatus())) {
                results.add(BatchResultDTO.fail(main.getId(), main.getCode(), "该预入库单已强制关闭，不可再关联"));
                continue;
            }
            List<SoReturnPrestockDetailEntity> unlinked = detailMap.getOrDefault(main.getId(), Collections.emptyList())
                    .stream()
                    .filter(d -> PrestockClaimStatusEnum.UNLINKED.getStatus().equals(d.getClaimStatus()))
                    .collect(Collectors.toList());
            if (CollUtil.isEmpty(unlinked)) {
                results.add(BatchResultDTO.fail(main.getId(), main.getCode(), "无可关联的明细行（均已关联或已强制关闭）"));
                continue;
            }
            // 空 skuId 明细不允许进入关联店铺→退货入库/库存联动，需运营先维护内部SKU映射
            SoReturnPrestockDetailEntity blankSkuDetail = unlinked.stream()
                    .filter(d -> CharSequenceUtil.isBlank(d.getSkuId()))
                    .findFirst().orElse(null);
            if (Objects.nonNull(blankSkuDetail)) {
                results.add(BatchResultDTO.fail(main.getId(), main.getCode(),
                        MessageUtils.getMessage(ApiError.SO_RETURN_PRESTOCK_SKU_REQUIRED,
                                CharSequenceUtil.emptyToDefault(blankSkuDetail.getSkuNo(), blankSkuDetail.getId()))));
                continue;
            }
            // 整单关联：每条未关联明细行默认整行数量全部关联到本次选定的同一店铺（不拆行）
            List<LinkedShopPair> linkedPairs = new ArrayList<>(unlinked.size());
            for (SoReturnPrestockDetailEntity detail : unlinked) {
                int claimQty = Objects.nonNull(detail.getReceivedQty()) ? detail.getReceivedQty() : 0;
                SoReturnPrestockDetailDTO.ShopItem item = buildShopItemFromLinkShop(dto, detail.getId(), claimQty);
                applyShopToDetail(main.getType(), detail, item, claimQty);
                linkedPairs.add(new LinkedShopPair(detail, item));
            }

            // 已关联 SKU 生成《退货入库单》，本单一店铺归为一张，直接置为已审核状态；单号回写到明细行（内存）
            generateReturnInstockByShop(main, linkedPairs, true);

            // 统一落库本单本次关联的明细行（含店铺/销售组织部门销售员信息 + 退货入库单回写）
            for (LinkedShopPair pair : linkedPairs) {
                if (!soReturnPrestockDetailService.updateById(pair.detail)) {
                    throw new ServiceException(ApiError.SO_RETURN_PRESTOCK_DETAIL_MODIFIED);
                }
            }

            // 平账：反向冲抵当前有效的普通(增库存)其他入库单；整单关联无剩余未关联行，故不再生成未关联占位单（均已审核）
            reconcileOtherInstockForPrestock(main, warehousingDept);

            // 联动刷新主表关联状态（全部已关联→LINKED，混合→PARTIAL）与操作时间
            refreshMainClaimStatus(main.getId());
            // 操作日志
            String linkTargetName = BillTypeEnum.B2B.getCode().equals(main.getType())
                    ? CharSequenceUtil.emptyToDefault(dto.getCustomerName(), dto.getCustomerId())
                    : CharSequenceUtil.emptyToDefault(dto.getShopName(), dto.getShopId());
            operateLogService.addModuleOperateLog(
                    String.format("预入库单【%s】关联%s【%s】", main.getCode(),
                            BillTypeEnum.B2B.getCode().equals(main.getType()) ? "客户" : "店铺",
                            CharSequenceUtil.emptyToDefault(linkTargetName, "-")),
                    ModuleTypeEnum.SO_RETURN_PRESTOCK.getCode(), main.getId(), "关联操作");
            results.add(BatchResultDTO.success(main.getId(), main.getCode()));
        }
        return results;
    }

    /**
     * 由批量关联店铺入参构建单条明细的店铺项：整单关联场景下认领数量默认取整行实际收货数量，
     * 并携带前端选定店铺后带出的销售组织/部门/销售员信息。
     */
    private SoReturnPrestockDetailDTO.ShopItem buildShopItemFromLinkShop(
            SoReturnPrestockDetailDTO.LinkShop dto, String detailId, int claimedQty) {
        SoReturnPrestockDetailDTO.ShopItem item = new SoReturnPrestockDetailDTO.ShopItem();
        item.setDetailId(detailId);
        item.setClaimedQty(claimedQty);
        item.setShopId(CharSequenceUtil.emptyToDefault(dto.getShopId(), ""));
        item.setShopName(CharSequenceUtil.emptyToDefault(dto.getShopName(), ""));
        item.setCustomerId(CharSequenceUtil.emptyToDefault(dto.getCustomerId(), ""));
        item.setCustomerName(CharSequenceUtil.emptyToDefault(dto.getCustomerName(), ""));
        item.setDictPlatform(CharSequenceUtil.emptyToDefault(dto.getDictPlatform(), ""));
        item.setSalesOrgId(CharSequenceUtil.emptyToDefault(dto.getSalesOrgId(), ""));
        item.setSalesOrgName(CharSequenceUtil.emptyToDefault(dto.getSalesOrgName(), ""));
        item.setSalesDeptId(CharSequenceUtil.emptyToDefault(dto.getSalesDeptId(), ""));
        item.setSalesDeptName(CharSequenceUtil.emptyToDefault(dto.getSalesDeptName(), ""));
        item.setSellerId(CharSequenceUtil.emptyToDefault(dto.getSellerId(), ""));
        item.setSellerName(CharSequenceUtil.emptyToDefault(dto.getSellerName(), ""));
        return item;
    }

    /**
     * 关联店铺后的其他入库平账处理，分两步（均直接已审核，不走审批流）：
     * <ol>
     *   <li>反向平账：定位本预入库单当前仍有效的普通(增库存)其他入库单
     *       （sourceType=SO_RETURN_PRESTOCK、sourceId=预入库单ID、inventoryDirection=ORDINARY、已审核），
     *       参照 {@code OtherInstockServiceImpl#generateOpposite} 就地复制其主/明细并将库存方向改为「退货」冲减占位库存，
     *       通过 {@link OtherInstockService#addAndApprove} 生成后直接已审核。以「反向单 remark = 原普通单单号」为标记做幂等，
     *       避免同一张普通单被重复反向（支持多次部分关联）。</li>
     *   <li>未关联占位：为本预入库单当前仍未关联的明细行重新生成一张普通(增库存)其他入库单占位，
     *       确保剩余未关联 SKU 的库存仍被正确占用；无剩余未关联行（如整单关联）时不生成。</li>
     * </ol>
     * 该方法需在本次关联明细行落库后调用，以便按最新的关联状态计算未关联占位明细。
     *
     * @param warehousingDept 已预取的仓储兜底部门，供生成未关联占位其他入库单使用，避免在此方法内重复发起 Feign 调用
     */
    private void reconcileOtherInstockForPrestock(SoReturnPrestockEntity main, SysDepartmentEntity warehousingDept) {
        // 1. 反向平账：冲抵当前仍有效（未被反向过）的普通(增库存)其他入库单
        List<OtherInstockEntity> ordinaryList = otherInstockService.lambdaQuery()
                .eq(OtherInstockEntity::getSourceId, main.getId())
                .eq(OtherInstockEntity::getSourceType, SourceTypeEnum.SO_RETURN_PRESTOCK.getCode())
                .eq(OtherInstockEntity::getInventoryDirection, InventoryDirectionEnum.ORDINARY.getCode())
                .eq(OtherInstockEntity::getApproveStatus, ApproveStatusEnum.APPROVE.getStatus())
                .list();
        if (CollUtil.isNotEmpty(ordinaryList)) {
            // 已生成的反向(退货)单，其 remark 记录了被反向的原普通单单号，用于幂等去重
            List<OtherInstockEntity> reversedList = otherInstockService.lambdaQuery()
                    .eq(OtherInstockEntity::getSourceId, main.getId())
                    .eq(OtherInstockEntity::getSourceType, SourceTypeEnum.SO_RETURN_PRESTOCK.getCode())
                    .eq(OtherInstockEntity::getInventoryDirection, InventoryDirectionEnum.RETURN_GOODS.getCode())
                    .list();
            Set<String> reversedCodes = reversedList.stream().map(OtherInstockEntity::getRemark)
                    .filter(CharSequenceUtil::isNotBlank).collect(Collectors.toSet());
            // 一次性批量查询本单所有待反向普通单的明细，避免循环内逐单查库（N+1）
            List<String> ordinaryIds = ordinaryList.stream().map(OtherInstockEntity::getId).collect(Collectors.toList());
            Map<String, List<OtherInstockDetailEntity>> ordinaryDetailMap = otherInstockDetailService.listByMainIds(ordinaryIds)
                    .stream().collect(Collectors.groupingBy(OtherInstockDetailEntity::getMainId));
            for (OtherInstockEntity ordinary : ordinaryList) {
                if (reversedCodes.contains(ordinary.getCode())) {
                    // 该普通单已被反向过，跳过
                    continue;
                }
                // 复制原普通入库单主表，方向改为退货，重置单号/审核信息，明细整单复制
                OtherInstockEntity opposite = OtherInStockConverter.INSTANCE.copy(ordinary);
                opposite.setApproveStatus(ApproveStatusEnum.WAIT_SUBMIT.getStatus());
                opposite.setSyncKingdeeId("");
                opposite.setInventoryDirection(InventoryDirectionEnum.RETURN_GOODS.getCode());
                List<OtherInstockDetailEntity> dbDetailList = ordinaryDetailMap.getOrDefault(ordinary.getId(), Collections.emptyList());
                opposite.setDetailEntityList(OtherInStockConverter.INSTANCE.copyDetailList(dbDetailList));
                // remark 记录被反向的原普通单单号，作为幂等标记
                opposite.setRemark(ordinary.getCode());
                // addAndApprove 内部 save → submit(无流程) → approve，最终为已审核状态
                otherInstockService.addAndApprove(opposite, false);
            }
        }

        // 2. 未关联占位：为当前仍未关联的明细行重新生成普通(增库存)其他入库单
        List<SoReturnPrestockDetailEntity> unlinkedList = soReturnPrestockDetailService.listByMainId(main.getId()).stream()
                .filter(d -> !Boolean.TRUE.equals(d.getIsDeleted()))
                .filter(d -> !PrestockClaimStatusEnum.LINKED.getStatus().equals(d.getClaimStatus()))
                .collect(Collectors.toList());
        generateOrdinaryOtherInstockForDetails(main, unlinkedList, warehousingDept);
    }

    /**
     * 预取系统内部自动入库使用的仓储兜底部门（{@link WmsConstant#DEFAULT_WAREHOUSING_DEPT_ID}）。
     * 部门为固定 ID，批量关联场景下由调用方在进入明细循环前调用一次并向下复用，避免事务内逐单发起 Feign 调用。
     */
    private SysDepartmentEntity resolveWarehousingDept() {
        List<SysDepartmentEntity> deptList = sysUserFeign.getDeptByIds(
                Collections.singletonList(WmsConstant.DEFAULT_WAREHOUSING_DEPT_ID));
        if (CollectionUtils.isEmpty(deptList)) {
            throw new ServiceException(ApiError.SO_RETURN_PRESTOCK_WAREHOUSING_DEPT_NOT_FOUND);
        }
        return deptList.get(0);
    }

    /**
     * 为预入库单指定的明细行生成一张普通(增库存)其他入库单占位并直接已审核，写法参照
     * {@code generateOtherInstockForPrestock}（预入库单创建时生成占位入库的范式）。
     * 明细行为空时不生成；仅对已解析出内部 skuId 的行组单，空 skuId 行与创建路径一致跳过，避免进入库存核心。
     *
     * @param dept 已由调用方预取的仓储兜底部门，作为占位单归属部门，避免在此方法内重复发起 Feign 调用
     */
    private void generateOrdinaryOtherInstockForDetails(SoReturnPrestockEntity main,
                                                        List<SoReturnPrestockDetailEntity> details, SysDepartmentEntity dept) {
        if (CollUtil.isEmpty(details)) {
            return;
        }
        List<SoReturnPrestockDetailEntity> validDetails = details.stream()
                .filter(d -> CharSequenceUtil.isNotBlank(d.getSkuId()))
                .collect(Collectors.toList());
        if (validDetails.isEmpty()) {
            log.warn("[预入库单未关联占位其他入库单]全部明细行均未解析到内部SKU，跳过生成：预入库单={}，原行数={}",
                    main.getCode(), details.size());
            return;
        }
        if (validDetails.size() < details.size()) {
            log.warn("[预入库单未关联占位其他入库单]部分明细行未解析到内部SKU，未计入本次库存联动：预入库单={}，总行数={}，已联动行数={}",
                    main.getCode(), details.size(), validDetails.size());
        }
        Map<String, SkuVO> skuVOMap = listSkuVOMap(validDetails.stream()
                .map(SoReturnPrestockDetailEntity::getSkuId).collect(Collectors.toList()));

        OtherInstockEntity entity = new OtherInstockEntity();
        entity.setBillDate(Objects.nonNull(main.getReceivedTime())
                ? main.getReceivedTime().toLocalDate() : LocalDate.now());
        entity.setInventoryDirection(InventoryDirectionEnum.ORDINARY.getCode());
        entity.setWarehouseId(main.getWarehouseId());
        entity.setWarehouseName(main.getWarehouseName());
        entity.setOrgId(main.getInventoryOrgId());
        entity.setOrgName(main.getInventoryOrgName());
        entity.setDeptId(dept.getId());
        entity.setDeptName(dept.getName());
        entity.setType(InstockTypeEnum.THREE_NO_PRODUCT_PRE_INSTOCK.getCode());
        entity.setReturnLogisticCode(main.getReturnLogisticCode());
        entity.setThirdCode(CharSequenceUtil.emptyToDefault(main.getThirdCode(), ""));
        entity.setSourceType(SourceTypeEnum.SO_RETURN_PRESTOCK.getCode());
        entity.setSourceId(main.getId());
        entity.setSourceCode(main.getCode());
        entity.setRemark(CharSequenceUtil.format("预入库单【{}】关联店铺后未关联SKU自动占位", main.getCode()));

        List<OtherInstockDetailEntity> detailEntityList = new ArrayList<>();
        for (SoReturnPrestockDetailEntity d : validDetails) {
            SkuVO skuVO = skuVOMap.getOrDefault(d.getSkuId(), new SkuVO());
            OtherInstockDetailEntity detailEntity = new OtherInstockDetailEntity();
            detailEntity.setSkuId(d.getSkuId());
            detailEntity.setSkuNo(d.getSkuNo());
            detailEntity.setActualQty(Objects.nonNull(d.getReceivedQty()) ? d.getReceivedQty() : 0);
            detailEntity.setUnit(skuVO.getUnitName());
            detailEntity.setRemark(d.getRemark());
            detailEntity.setPrestockDetailId(d.getId());
            detailEntity.setDefectiveProductFlag(Boolean.TRUE.equals(d.getDefectiveProductFlag()));
            detailEntityList.add(detailEntity);
        }
        entity.setDetailEntityList(detailEntityList);
        otherInstockService.addAndApprove(entity, false);
    }

    /**
     * 将新生成的退货入库单直接置为已审核状态：先提交（不启动审批流），再直接结束审核（跳过工作流），
     * 由 {@code approveEnd} 更新为已审核并触发退货入库库存联动。
     */
    private void approveReturnInstock(SoReturnInstockEntity instock) {
        if (Objects.isNull(instock) || CharSequenceUtil.isBlank(instock.getId())) {
            throw new ServiceException(ApiError.SO_RETURN_PRESTOCK_INSTOCK_GENERATE_FAILED);
        }
        // 直接使用新增返回的内存实体推进提交/审核，避免"写入后再查询"在同一事务未提交或读写分离场景下查不到数据。
        // 落库审核状态默认 waitSubmit，内存实体未回填，补齐以通过 submit 的状态校验
        if (CharSequenceUtil.isBlank(instock.getApproveStatus())) {
            instock.setApproveStatus(ApproveStatusEnum.WAIT_SUBMIT.getStatus());
        }
        // 提交但不启动审批流（isNeedProcess=false），单据转为审核中
        BatchResultDTO submitResult = soReturnInstockService.submit(instock, Boolean.FALSE);
        if (!Boolean.TRUE.equals(submitResult.getSuccess())) {
            throw new ServiceException(ApiError.SO_RETURN_PRESTOCK_INSTOCK_SUBMIT_FAILED, submitResult.getMsg());
        }
        // submit 已把 DB 状态更新为 approveIng，同步内存实体后直接结束审核为通过（不经过工作流），置为已审核并更新库存
        instock.setApproveStatus(ApproveStatusEnum.APPROVE_ING.getStatus());
        soReturnInstockService.approveEnd(
                new ApproveOneDTO(instock.getId(), ApproveTypeEnum.PASS.getStatus(), "预入库单关联自动审核通过"),
                instock);
    }

    // ===================== 确认关联店铺（明细维度，逐行选店铺） =====================

    @Override
    @DistributeLocker(businessType = SO_RETURN_PRESTOCK_LINK_LOCK_KEY, keyName = "dto.mainId")
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO confirmLinkShop(SoReturnPrestockDetailDTO.ConfirmLinkShop dto) {
        SoReturnPrestockEntity main = getByIdOrThrow(dto.getMainId());
        if (PrestockClaimStatusEnum.FORCE_CLOSE.getStatus().equals(main.getClaimStatus())) {
            throw new ServiceException(ApiError.SO_RETURN_PRESTOCK_FORCE_CLOSE);
        }

        // 本单全部明细，按 ID 建索引，便于按 detailId 定位并校验归属
        Map<String, SoReturnPrestockDetailEntity> detailMap = soReturnPrestockDetailService.listByMainId(dto.getMainId())
                .stream().collect(Collectors.toMap(SoReturnPrestockDetailEntity::getId, d -> d, (a, b) -> a));

        // 逐行处理：仅在内存中变更明细行并收集"本次关联行→店铺项"配对，统一在生成退货入库单、回写单号后落库。
        // 未选择店铺的行不会出现在 shopList 中，天然保持未关联
        List<LinkedShopPair> linkedPairs = new ArrayList<>();
        for (SoReturnPrestockDetailDTO.ShopItem item : dto.getShopList()) {
            // 未选择关联对象（B2C 店铺id / B2B 客户id 均为空）→ 保持未关联，跳过
            if (CharSequenceUtil.isBlank(resolveLinkTargetKey(main.getType(), item))) {
                continue;
            }
            SoReturnPrestockDetailEntity detail = detailMap.get(item.getDetailId());
            if (Objects.isNull(detail) || Boolean.TRUE.equals(detail.getIsDeleted())
                    || !dto.getMainId().equals(detail.getMainId())) {
                return BatchResultDTO.fail(main.getId(), main.getCode(),
                        "详情行不存在或不属于当前预入库单：" + item.getDetailId());
            }
            if (PrestockClaimStatusEnum.LINKED.getStatus().equals(detail.getClaimStatus())) {
                return BatchResultDTO.fail(detail.getId(), detail.getSkuNo(), "该行已关联，请先解除关联");
            }
            if (PrestockClaimStatusEnum.FORCE_CLOSE.getStatus().equals(detail.getClaimStatus())) {
                return BatchResultDTO.fail(detail.getId(), detail.getSkuNo(), "该行已强制关闭，不可再关联");
            }
            if (CharSequenceUtil.isBlank(detail.getSkuId())) {
                return BatchResultDTO.fail(detail.getId(),
                        CharSequenceUtil.emptyToDefault(detail.getSkuNo(), detail.getId()),
                        MessageUtils.getMessage(ApiError.SO_RETURN_PRESTOCK_SKU_REQUIRED,
                                CharSequenceUtil.emptyToDefault(detail.getSkuNo(), detail.getId())));
            }
            int receivedQty = Objects.nonNull(detail.getReceivedQty()) ? detail.getReceivedQty() : 0;
            int claimQty = Objects.nonNull(item.getClaimedQty()) ? item.getClaimedQty() : receivedQty;
            if (claimQty <= 0) {
                return BatchResultDTO.fail(detail.getId(), detail.getSkuNo(), "认领数量必须大于0");
            }
            // 认领数量不能超过数据库中该行实际收货数量，否则不允许关联
            if (claimQty > receivedQty) {
                return BatchResultDTO.fail(detail.getId(), detail.getSkuNo(),
                        "认领数量不能超过当前行实际收货数量：" + receivedQty);
            }
            // 认领数量 < 实际收货数量：按收货数量拆行，剩余收货数量拆为新未关联行，当前行仅保留认领数量并关联；
            // 认领数量 == 实际收货数量：整行关联
            if (claimQty < receivedQty) {
                splitDetail(detail, claimQty);
            } else {
                detail.setReceivedQty(claimQty);
            }
            applyShopToDetail(main.getType(), detail, item, claimQty);
            linkedPairs.add(new LinkedShopPair(detail, item));
        }
        if (CollUtil.isEmpty(linkedPairs)) {
            return BatchResultDTO.fail(main.getId(), main.getCode(), "无可关联的明细行（未选择店铺）");
        }

        // 联动处理：关联相同店铺的行合并生成一张《退货入库单》，直接置为已审核状态，并把入库单号回写到对应明细行（内存）
        generateReturnInstockByShop(main, linkedPairs, true);

        // 统一落库本次关联的明细行（含店铺信息 + 退货入库单回写）
        for (LinkedShopPair pair : linkedPairs) {
            if (!soReturnPrestockDetailService.updateById(pair.detail)) {
                throw new ServiceException(ApiError.SO_RETURN_PRESTOCK_DETAIL_MODIFIED);
            }
        }

        // 平账联动：反向冲抵原整单普通(增库存)其他入库单，并为本次仍未关联的 SKU 重新生成普通占位单（均已审核）
        reconcileOtherInstockForPrestock(main, resolveWarehousingDept());

        // 刷新主表关联状态（全部已关联→LINKED，部分→PARTIAL）
        refreshMainClaimStatus(main.getId());

        // 操作日志
        String shopNames = linkedPairs.stream()
                .map(p -> BillTypeEnum.B2B.getCode().equals(main.getType())
                        ? CharSequenceUtil.emptyToDefault(p.item.getCustomerName(), p.item.getCustomerId())
                        : CharSequenceUtil.emptyToDefault(p.item.getShopName(), p.item.getShopId()))
                .filter(CharSequenceUtil::isNotBlank)
                .distinct()
                .collect(Collectors.joining("、"));
        operateLogService.addModuleOperateLog(
                String.format("预入库单【%s】确认关联%s【%s】", main.getCode(),
                        BillTypeEnum.B2B.getCode().equals(main.getType()) ? "客户" : "店铺",
                        CharSequenceUtil.emptyToDefault(shopNames, "-")),
                ModuleTypeEnum.SO_RETURN_PRESTOCK.getCode(), main.getId(), "关联操作");
        return BatchResultDTO.success(main.getId(), main.getCode(), "已完成关联");
    }

    /**
     * 将店铺项信息写入预入库单明细行并置为已关联。
     * <p>关联店铺不对应具体售后单/销售单/订单，故售后单、销售单、退货单、平台订单号等字段清空；
     * 平台字典值取自店铺所属平台，销售组织/部门由前端选店铺后带出。</p>
     */
    private void applyShopToDetail(String type, SoReturnPrestockDetailEntity detail,
                                   SoReturnPrestockDetailDTO.ShopItem item, int claimQty) {
        boolean isB2b = BillTypeEnum.B2B.getCode().equals(type);
        // 明细实体无独立客户字段，B2B 按模块约定将客户id/客户名记录到 shopId/shopName（与关联售后单 B2B 口径一致）
        String shopId = isB2b ? item.getCustomerId() : item.getShopId();
        String shopName = isB2b ? item.getCustomerName() : item.getShopName();
        detail.setShopId(CharSequenceUtil.emptyToDefault(shopId, ""))
                .setShopName(CharSequenceUtil.emptyToDefault(shopName, ""))
                .setDictPlatform(CharSequenceUtil.emptyToDefault(item.getDictPlatform(), ""))
                .setSalesOrgId(CharSequenceUtil.emptyToDefault(item.getSalesOrgId(), ""))
                .setSalesOrgName(CharSequenceUtil.emptyToDefault(item.getSalesOrgName(), ""))
                .setSalesDeptId(CharSequenceUtil.emptyToDefault(item.getSalesDeptId(), ""))
                .setSalesDeptName(CharSequenceUtil.emptyToDefault(item.getSalesDeptName(), ""))
                .setSellerId(CharSequenceUtil.emptyToDefault(item.getSellerId(), ""))
                .setSellerName(CharSequenceUtil.emptyToDefault(item.getSellerName(), ""))
                .setAfterSaleId("").setAfterSaleCode("")
                .setPlatformOrderCode("")
                .setSoId("").setSoCode("")
                .setSoReturnId("").setSoReturnCode("")
                .setClaimedQty(claimQty)
                .setClaimStatus(PrestockClaimStatusEnum.LINKED.getStatus());
    }

    /**
     * 联动生成《退货入库单》：将本次已关联的明细行按店铺分组，每个店铺生成一张退货入库单
     * （复用 {@link SoReturnInstockService#add}）。上游预入库单明细不记录下游退货入库单的关联指针，
     * 改为下游退货入库单明细通过 {@code prestock_detail_id} 回指本预入库单明细行（见 {@link #buildInstockDetailAdd}）。
     *
     * @param autoApprove 是否生成后直接提交并审核通过（批量整单关联店铺场景要求已审核状态）
     */
    private void generateReturnInstockByShop(SoReturnPrestockEntity main, List<LinkedShopPair> linkedPairs, boolean autoApprove) {
        if (CollUtil.isEmpty(linkedPairs)) {
            return;
        }
        if (CharSequenceUtil.isBlank(main.getWarehouseId())) {
            throw new ServiceException(ApiError.SO_RETURN_PRESTOCK_WAREHOUSE_REQUIRED_FOR_INSTOCK);
        }
        LocalDate billDate = LocalDate.now();
        // 按关联目标分组（B2C 按店铺id、B2B 按客户id），保持关联顺序便于排查
        Map<String, List<LinkedShopPair>> pairsByShop = linkedPairs.stream()
                .collect(Collectors.groupingBy(p -> resolveLinkTargetKey(main.getType(), p.item), LinkedHashMap::new, Collectors.toList()));
        if (pairsByShop.size() > LINK_GROUP_MAX_SIZE) {
            throw new ServiceException(ApiError.SO_RETURN_PRESTOCK_LINK_SHOP_GROUP_EXCEEDS, pairsByShop.size());
        }
        boolean isB2b = BillTypeEnum.B2B.getCode().equals(main.getType());
        // B2C 场景下解析客户id需按店铺反查归属客户；分组循环前一次性批量预取本次涉及的全部店铺信息，
        // 避免 resolveLinkCustomerId 在循环内逐组发起单条 Feign 查询（B2B 无需查店铺，返回空 Map）
        Map<String, ShopInfoEntity> shopInfoMap = isB2b ? Collections.emptyMap()
                : listShopInfoMap(pairsByShop.values().stream()
                        .map(pairs -> pairs.get(0).item.getShopId())
                        .collect(Collectors.toList()));
        long loopStart = System.currentTimeMillis();
        for (List<LinkedShopPair> pairs : pairsByShop.values()) {
            SoReturnPrestockDetailDTO.ShopItem head = pairs.get(0).item;
            // 关联生成退货入库单仍需客户id（生成明细时需按客户查平台SKU映射）：
            // B2B 前端直接传客户id；B2C 前端传店铺id，需按已预取的店铺信息反查归属客户，
            // 与 SoReturnInstockServiceImpl B2C 分支口径一致
            String customerId = resolveLinkCustomerId(main.getType(), head.getShopId(), head.getShopName(), head.getCustomerId(), shopInfoMap);
            SoReturnInstockDTO.Add add = new SoReturnInstockDTO.Add();
            add.setType(main.getType());
            // B2B 无店铺概念（前端传的是客户id），退货入库单不落 shopId
            add.setShopId(isB2b ? "" : head.getShopId());
            add.setCustomerId(customerId);
            add.setSalesOrgId(CharSequenceUtil.emptyToDefault(head.getSalesOrgId(), ""));
            add.setSalesDeptId(CharSequenceUtil.emptyToDefault(head.getSalesDeptId(), ""));
            add.setSellerId(CharSequenceUtil.emptyToDefault(head.getSellerId(), ""));
            add.setBillDate(billDate);
            add.setWarehouseId(main.getWarehouseId());
            add.setReturnLogisticCode(main.getReturnLogisticCode());
            add.setThirdCode(main.getThirdCode());
            // 来源为本预入库单，便于溯源
            add.setSourceId(main.getId());
            add.setSourceCode(main.getCode());
            add.setSourceType(SourceTypeEnum.SO_RETURN_PRESTOCK.getCode());
            add.setDetailList(pairs.stream()
                    .map(p -> buildInstockDetailAdd(main, p.detail, main.getDictReturnType(), "", ""))
                    .collect(Collectors.toList()));

            // 新增并拿到落库后的内存实体（含 id、单号），后续提交/审核均基于该实体，避免"写入后再查询"
            SoReturnInstockEntity instock = soReturnInstockService.addReturnEntity(add);
            // 批量整单关联场景：生成后直接提交并审核通过
            if (autoApprove) {
                approveReturnInstock(instock);
            }
        }
        long loopCost = System.currentTimeMillis() - loopStart;
        if (loopCost > LINK_LOOP_WARN_THRESHOLD_MS) {
            log.warn("[预入库单确认关联店铺]生成退货入库单耗时过长：预入库单={}，分组数={}，耗时={}ms",
                    main.getCode(), pairsByShop.size(), loopCost);
        }
    }

    /**
     * 解析关联对象的客户id并做必填校验。关联生成退货入库单及其明细时必须带客户id，缺失会导致下游抛出含义模糊的“客户id不能为空”。
     * <ul>
     *   <li>B2B：前端直接传客户id（无店铺-客户关联环节），customerId 必填；</li>
     *   <li>B2C：前端传店铺id，shopId 必填，按已预取的店铺信息反查归属客户，店铺不存在或未绑定客户时抛出明确异常。</li>
     * </ul>
     *
     * @param type        预入库单单据类型（{@link BillTypeEnum}）
     * @param shopInfoMap 调用方在分组循环前批量预取的 shopId -> ShopInfoEntity 映射（见 {@link #listShopInfoMap}），
     *                    避免本方法在循环内逐次发起单条 Feign 查询；B2B 场景可传空 Map
     */
    private String resolveLinkCustomerId(String type, String shopId, String shopName, String customerId,
                                         Map<String, ShopInfoEntity> shopInfoMap) {
        // B2B：前端直接传客户id
        if (BillTypeEnum.B2B.getCode().equals(type)) {
            if (CharSequenceUtil.isBlank(customerId)) {
                throw new ServiceException(ApiError.SO_RETURN_PRESTOCK_LINK_CUSTOMER_REQUIRED);
            }
            return customerId;
        }
        // B2C：前端传店铺id，按已预取的店铺信息反查归属客户
        if (CharSequenceUtil.isBlank(shopId)) {
            throw new ServiceException(ApiError.SO_RETURN_PRESTOCK_LINK_SHOP_REQUIRED);
        }
        ShopInfoEntity shopInfo = shopInfoMap.get(shopId);
        String shopDesc = CharSequenceUtil.emptyToDefault(shopName, shopId);
        if (Objects.isNull(shopInfo)) {
            throw new ServiceException(ApiError.SO_RETURN_PRESTOCK_LINK_SHOP_NOT_FOUND, shopDesc);
        }
        if (CharSequenceUtil.isBlank(shopInfo.getCustomerId())) {
            throw new ServiceException(ApiError.SO_RETURN_PRESTOCK_LINK_SHOP_CUSTOMER_REQUIRED, shopDesc);
        }
        return shopInfo.getCustomerId();
    }

    /**
     * 按店铺 ID 批量查询店铺信息，返回 shopId -&gt; ShopInfoEntity 映射（未命中的 shopId 不进入 Map）。
     * 供 {@link #generateReturnInstockByShop} 在按店铺分组的循环开始前一次性预取，
     * 避免 {@link #resolveLinkCustomerId} 在循环内逐组单独发起 Feign 查询。
     */
    private Map<String, ShopInfoEntity> listShopInfoMap(List<String> shopIds) {
        List<String> distinctShopIds = shopIds.stream().filter(CharSequenceUtil::isNotBlank).distinct().collect(Collectors.toList());
        if (CollUtil.isEmpty(distinctShopIds)) {
            return Collections.emptyMap();
        }
        List<ShopInfoEntity> shopInfoList = FeignQuery.getByIds(ShopInfoEntity.class, distinctShopIds);
        if (CollUtil.isEmpty(shopInfoList)) {
            return Collections.emptyMap();
        }
        return shopInfoList.stream().collect(Collectors.toMap(ShopInfoEntity::getId, v -> v, (a, b) -> a));
    }

    /**
     * 关联对象的分组/标识键：B2C 用店铺id，B2B 用客户id。
     * 用于按目标对象分组生成退货入库单，以及判断某行是否已选择关联对象。
     */
    private String resolveLinkTargetKey(String type, SoReturnPrestockDetailDTO.ShopItem item) {
        return BillTypeEnum.B2B.getCode().equals(type) ? item.getCustomerId() : item.getShopId();
    }

    /**
     * 本次关联的预入库单明细行与其来源店铺项的配对，用于关联落库与按店铺分组生成退货入库单
     */
    private static class LinkedShopPair {
        private final SoReturnPrestockDetailEntity detail;
        private final SoReturnPrestockDetailDTO.ShopItem item;

        LinkedShopPair(SoReturnPrestockDetailEntity detail, SoReturnPrestockDetailDTO.ShopItem item) {
            this.detail = detail;
            this.item = item;
        }
    }

    // ===================== 删除 =====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<BatchResultDTO> deleteByIds(List<String> ids) {
        // 批量删除按整批原子处理：任意一条失败直接抛出异常，交由 @Transactional 整批回滚，
        // 不再吞异常拼装逐条结果，避免"部分条目标记成功但实际被回滚"的结果与数据库状态不一致问题
        List<BatchResultDTO> results = new ArrayList<>(ids.size());
        for (String id : ids) {
            SoReturnPrestockEntity entity = getByIdOrThrow(id);
            // 软删主表
            removeById(entity.getId());
            // 软删详情行
            soReturnPrestockDetailService.deleteByMainId(entity.getId());
            results.add(BatchResultDTO.success(id, entity.getCode()));
        }
        return results;
    }

    // ===================== 无物流单号+无参考单号自动创建（系统内部） =====================

    @Override
    public List<SoReturnPrestockEntity> listByThirdCode(String thirdCode) {
        if (CharSequenceUtil.isBlank(thirdCode)) {
            return Collections.emptyList();
        }
        return lambdaQuery()
                .eq(SoReturnPrestockEntity::getThirdCode, thirdCode)
                .eq(SoReturnPrestockEntity::getIsDeleted, false)
                .list();
    }

    @Override
    @DistributeLocker(businessType = SO_RETURN_PRESTOCK_HEADLESS_LOCK_KEY, keyName = "dto.thirdCode")
    @Transactional(rollbackFor = Exception.class)
    public String createFromOverseasWhHeadless(SoReturnPrestockDTO.Add dto) {
        // 幂等：同一第三方/平台退货单号已存在则直接返回（此场景没有物流单号可用作幂等键）
        if (CharSequenceUtil.isNotBlank(dto.getThirdCode())) {
            SoReturnPrestockEntity existing = lambdaQuery()
                    .eq(SoReturnPrestockEntity::getThirdCode, dto.getThirdCode())
                    .eq(SoReturnPrestockEntity::getIsDeleted, false)
                    .one();
            if (Objects.nonNull(existing)) {
                // 本方法不追加明细：若调用方仍带着缺口明细进来，说明入口对账与已有预入库不对齐（历史脏数据/映射异常），
                // 此处无法静默补单，打 warn 便于人工按 thirdCode 核对
                int incomingDetailCount = CollUtil.isEmpty(dto.getDetailList()) ? 0 : dto.getDetailList().size();
                int incomingQty = CollUtil.isEmpty(dto.getDetailList()) ? 0 : dto.getDetailList().stream()
                        .mapToInt(d -> Objects.nonNull(d.getReceivedQty()) ? d.getReceivedQty() : 0).sum();
                log.warn("预入库单已存在（无头件），第三方单号：{}，已有单id={}，本次入参明细行数={}、数量合计={}，跳过创建且不追加明细；若入口对账仍判有缺口请人工核对",
                        dto.getThirdCode(), existing.getId(), incomingDetailCount, incomingQty);
                return existing.getId();
            }
        } else {
            log.warn("无头件预入库单缺少第三方单号，跳过幂等校验，可能重复生成");
        }
        // 海外仓拉取明细来源于平台SKU映射（仅含产品名称，无图片/EAN），此处按 skuId 从 PLM 批量补全产品名称/图片/EAN
        enrichDetailProductInfoBySkuId(dto.getDetailList());
        SoReturnPrestockEntity entity = buildAndPersist(dto, "", PrestockSourceTypeEnum.OVERSEAS_WH.getStatus());
        // 与人工发起创建（addFromReturnInstock）保持一致：预入库单落库后同步生成其它入库单，使预入库真正增加库存
        generateOtherInstockForPrestock(entity.getId(), dto);
        // 操作日志（幂等命中已存在单时不重复写日志）
        operateLogService.addModuleOperateLog(
                String.format("海外仓自动创建预入库单【%s】", entity.getCode()),
                ModuleTypeEnum.SO_RETURN_PRESTOCK.getCode(), entity.getId(), "新增操作");
        return entity.getId();
    }

    // ===================== 由退货入库单新增/修改表单参数创建（人工触发） =====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String addFromReturnInstock(SoReturnPrestockDTO.FromInstock dto) {
        checkCustomerAndLogisticCodeForPrestock(dto.getCustomerId(), dto.getReturnLogisticCode());
        String warehouseId = resolveWarehouseIdFromInstock(dto.getWarehouseId(), dto.getDetailList());
        String returnTypeDict = resolveReturnTypeDictFromInstock(dto.getDetailList());
        List<SoReturnPrestockDetailDTO.Add> detailList = buildPrestockDetailListFromInstock(dto.getDetailList());
        SoReturnPrestockDTO.Add prestockAdd = buildPrestockAddFromInstockParams(
                dto.getType(), dto.getReturnLogisticCode(), dto.getThirdCode(), warehouseId,
                returnTypeDict, dto.getSoReturnCode(), detailList);
        String prestockId = add(prestockAdd);
        generateOtherInstockForPrestock(prestockId, prestockAdd);
        return prestockId;
    }

    /**
     * 解析创建预入库单所用的仓库 ID：优先取表单主表仓库；退货入库单表单按明细行填写仓库，
     * 主表未直接传入时按明细行 warehouseId 汇总推导（预入库单主表仅支持单一仓库，明细行仓库不一致时拒绝创建）
     */
    private String resolveWarehouseIdFromInstock(String mainWarehouseId,
                                                 List<SoReturnPrestockDetailDTO.FromInstock> detailList) {
        if (CharSequenceUtil.isNotBlank(mainWarehouseId)) {
            return mainWarehouseId;
        }
        List<String> distinctWarehouseIds = CollUtil.emptyIfNull(detailList).stream()
                .map(SoReturnPrestockDetailDTO.FromInstock::getWarehouseId)
                .filter(CharSequenceUtil::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        if (distinctWarehouseIds.isEmpty()) {
            throw new ServiceException(ApiError.SO_RETURN_PRESTOCK_WAREHOUSE_REQUIRED);
        }
        if (distinctWarehouseIds.size() > 1) {
            throw new ServiceException(ApiError.SO_RETURN_PRESTOCK_INSTOCK_WAREHOUSE_INCONSISTENT);
        }
        return distinctWarehouseIds.get(0);
    }

    /**
     * 解析创建预入库单所用的退货类型字典值：退货入库单表单按明细行填写退货类型，预入库单主表仅支持单一退货类型，
     * 按明细行 returnTypeDict 汇总推导；各行不一致时拒绝创建，行内均未填写时返回空（该字段非必填）
     */
    private String resolveReturnTypeDictFromInstock(List<SoReturnPrestockDetailDTO.FromInstock> detailList) {
        List<String> distinctReturnTypeDicts = CollUtil.emptyIfNull(detailList).stream()
                .map(SoReturnPrestockDetailDTO.FromInstock::getReturnTypeDict)
                .filter(CharSequenceUtil::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        if (distinctReturnTypeDicts.size() > 1) {
            throw new ServiceException(ApiError.SO_RETURN_PRESTOCK_INSTOCK_RETURN_TYPE_INCONSISTENT);
        }
        return CollUtil.isEmpty(distinctReturnTypeDicts) ? "" : distinctReturnTypeDicts.get(0);
    }

    // ===================== 强制关闭剩余未认领预入库单（定时任务） =====================

    @Override
    public int forceCloseUnclaimedPrestock() {
        // 仅查询主表 ID（不查询全部字段），减少一次性加载到内存的数据量：
        // 主表关联状态为未关联或部分关联，排除已关联、已强制关闭、已删除
        List<String> mainIds = lambdaQuery()
                .select(SoReturnPrestockEntity::getId)
                .in(SoReturnPrestockEntity::getClaimStatus,
                        PrestockClaimStatusEnum.UNLINKED.getStatus(),
                        PrestockClaimStatusEnum.PARTIAL.getStatus())
                .eq(SoReturnPrestockEntity::getIsDeleted, false)
                .list()
                .stream().map(SoReturnPrestockEntity::getId).collect(Collectors.toList());
        if (CollUtil.isEmpty(mainIds)) {
            log.info("[预入库单强制关闭]无待处理的未认领预入库单");
            return 0;
        }

        LocalDateTime now = LocalDateTime.now();
        int processedCount = 0;
        // 分批处理，控制 in 参数规模与批量更新粒度；通过自注入代理调用，使每批在独立事务中提交，
        // 避免所有批次共用同一个长事务导致锁持有时间过长
        for (List<String> batchIds : ListUtil.split(mainIds, FORCE_CLOSE_BATCH_SIZE)) {
            processedCount += self.forceCloseBatch(batchIds, now);
        }
        log.info("[预入库单强制关闭]本次处理预入库单数量：{}", processedCount);
        return processedCount;
    }

    /**
     * 强制关闭一批预入库单：逐单加锁、独立事务处理，避免与认领入口并发覆盖状态。
     * 外层 batchIds 仅为候选集合，每张单在处理前会重新校验主表当前关联状态。
     *
     * @param mainIds     本批候选预入库单主表 ID
     * @param operateTime 本次强制关闭操作的统一操作时间
     * @return 本批实际强制关闭处理的预入库单数量
     */
    @Override
    public int forceCloseBatch(List<String> mainIds, LocalDateTime operateTime) {
        if (CollUtil.isEmpty(mainIds)) {
            return 0;
        }
        int processedCount = 0;
        for (String mainId : mainIds) {
            processedCount += self.forceCloseSingle(mainId, operateTime);
        }
        return processedCount;
    }

    /**
     * 强制关闭单张预入库单：将其下「未关联」明细行 claim_status 置为「强制关闭」（已关联明细不变），
     * 再按明细最新关联状态回写主表。与认领入口共用分布式锁，主表回写携带 version 乐观锁。
     *
     * @param mainId      预入库单主表 ID
     * @param operateTime 本次强制关闭操作的统一操作时间
     * @return 实际处理返回 1，主表状态已不满足关闭条件则返回 0
     */
    @Override
    @DistributeLocker(businessType = SO_RETURN_PRESTOCK_LINK_LOCK_KEY, keyName = "mainId")
    @Transactional(rollbackFor = Exception.class)
    public int forceCloseSingle(String mainId, LocalDateTime operateTime) {
        SoReturnPrestockEntity main = getById(mainId);
        if (Objects.isNull(main) || Boolean.TRUE.equals(main.getIsDeleted())) {
            return 0;
        }
        String claimStatus = main.getClaimStatus();
        if (!PrestockClaimStatusEnum.UNLINKED.getStatus().equals(claimStatus)
                && !PrestockClaimStatusEnum.PARTIAL.getStatus().equals(claimStatus)) {
            return 0;
        }

        soReturnPrestockDetailService.lambdaUpdate()
                .eq(SoReturnPrestockDetailEntity::getMainId, mainId)
                .eq(SoReturnPrestockDetailEntity::getClaimStatus, PrestockClaimStatusEnum.UNLINKED.getStatus())
                .eq(SoReturnPrestockDetailEntity::getIsDeleted, false)
                .set(SoReturnPrestockDetailEntity::getClaimStatus, PrestockClaimStatusEnum.FORCE_CLOSE.getStatus())
                .update();

        refreshMainClaimStatusAfterForceClose(main, operateTime);
        return 1;
    }

    /**
     * 强制关闭后按明细最新关联状态回写主表；仅当主表仍为未关联/部分关联时才更新，并使用 version 乐观锁。
     */
    private void refreshMainClaimStatusAfterForceClose(SoReturnPrestockEntity main, LocalDateTime operateTime) {
        String currentStatus = main.getClaimStatus();
        if (!PrestockClaimStatusEnum.UNLINKED.getStatus().equals(currentStatus)
                && !PrestockClaimStatusEnum.PARTIAL.getStatus().equals(currentStatus)) {
            return;
        }
        List<SoReturnPrestockDetailEntity> details = soReturnPrestockDetailService.listByMainId(main.getId());
        if (CollUtil.isEmpty(details)) {
            return;
        }
        long linkedCount = details.stream()
                .filter(d -> PrestockClaimStatusEnum.LINKED.getStatus().equals(d.getClaimStatus()))
                .count();
        String newClaimStatus;
        if (linkedCount == details.size()) {
            newClaimStatus = PrestockClaimStatusEnum.LINKED.getStatus();
        } else if (linkedCount == 0) {
            newClaimStatus = PrestockClaimStatusEnum.FORCE_CLOSE.getStatus();
        } else {
            newClaimStatus = PrestockClaimStatusEnum.PARTIAL.getStatus();
        }
        main.setClaimStatus(newClaimStatus);
        main.setOperateTime(operateTime);
        if (!updateById(main)) {
            log.warn("[预入库单强制关闭]主表{}状态已被并发修改，跳过回写", main.getId());
        }
    }

    // ===================== 私有辅助方法 =====================

    /**
     * 构建预入库单主表并落库，随后保存详情行；由 {@link #add}
     * {@link #createFromOverseasWhHeadless} 在各自完成幂等/唯一性校验后调用。
     */
    private SoReturnPrestockEntity buildAndPersist(SoReturnPrestockDTO.Add dto, String returnLogisticCode, String sourceType) {
        if (CollUtil.isEmpty(dto.getDetailList())) {
            throw new ServiceException(ApiError.COMMON_PARAM_REQUIRED, "预入库单详情行");
        }
        LocalDateTime now = LocalDateTime.now();
        SoReturnPrestockEntity entity = new SoReturnPrestockEntity()
                .setCode(docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_YRK))
                .setType(dto.getType())
                .setReturnLogisticCode(CharSequenceUtil.emptyToDefault(returnLogisticCode, ""))
                .setClaimStatus(PrestockClaimStatusEnum.UNLINKED.getStatus())
                .setSourceType(sourceType)
                .setThirdCode(CharSequenceUtil.emptyToDefault(dto.getThirdCode(), ""))
                .setInventoryOrgId(dto.getInventoryOrgId())
                .setInventoryOrgName(CharSequenceUtil.emptyToDefault(dto.getInventoryOrgName(), ""))
                .setWarehouseId(dto.getWarehouseId())
                .setWarehouseName(CharSequenceUtil.emptyToDefault(dto.getWarehouseName(), ""))
                .setDictReturnType(CharSequenceUtil.emptyToDefault(dto.getDictReturnType(), ""))
                .setReceivedTime(now)
                .setOperateTime(now)
                .setRemark(CharSequenceUtil.emptyToDefault(dto.getRemark(), ""));
        save(entity);

        saveDetailList(entity.getId(), dto.getDetailList());
        return entity;
    }

    /**
     * 物流单号唯一性校验（新增时 updateId 为 null，修改时传入当前记录 ID）
     */
    private void checkLogisticCodeUnique(String returnLogisticCode, String updateId) {
        LambdaQueryWrapper<SoReturnPrestockEntity> wrapper = new LambdaQueryWrapper<SoReturnPrestockEntity>()
                .eq(SoReturnPrestockEntity::getReturnLogisticCode, returnLogisticCode)
                .eq(SoReturnPrestockEntity::getIsDeleted, false);
        if (CharSequenceUtil.isNotBlank(updateId)) {
            wrapper.ne(SoReturnPrestockEntity::getId, updateId);
        }
        long count = count(wrapper);
        if (count > 0) {
            throw new ServiceException(ApiError.COMMON_HAS_EXIST, "物流单号【" + returnLogisticCode + "】");
        }
    }

    /**
     * 拆行：认领数量 &lt; 当前行实际收货数量时，将剩余收货数量（receivedQty - linkQty）拆为新的未关联行，
     * 原行收货数量收敛为本次认领数量，保证拆分前后两行收货数量之和不变。
     *
     * @return 承载剩余数量的新未关联详情行（已落库）
     */
    private SoReturnPrestockDetailEntity splitDetail(SoReturnPrestockDetailEntity original, int linkQty) {
        int originalReceivedQty = Objects.nonNull(original.getReceivedQty()) ? original.getReceivedQty() : 0;
        int remainReceivedQty = originalReceivedQty - linkQty;

        SoReturnPrestockDetailEntity newDetail = buildLeftoverDetail(original, remainReceivedQty);
        soReturnPrestockDetailService.save(newDetail);

        // 原行收敛为本次认领数量，剩余部分已转入新行；由调用方在后续 updateById 中一并落库
        original.setReceivedQty(linkQty);
        return newDetail;
    }

    /**
     * 拆行（仅内存）：与 {@link #splitDetail} 逻辑一致，但拆出的剩余行不在此处落库，
     * 由调用方统一收集后作为「新增」一次性插入。
     * <p>用于关联售后单场景：拆行剩余行会回队供后续同 SKU 售后单明细继续认领，可能再次被关联并进入
     * 待落库列表。若在此处先 {@code save} 再由调用方对同一新行乐观锁 {@code updateById}，同事务内该新行
     * 的 version 一旦被其它写操作顶高即会导致乐观锁冲突。故新行改为纯插入，避免二次乐观锁更新。</p>
     *
     * @return 承载剩余数量的新未关联详情行（已预分配 ID，未落库）
     */
    private SoReturnPrestockDetailEntity splitDetailInMemory(SoReturnPrestockDetailEntity original, int linkQty) {
        int originalReceiveQty = Objects.nonNull(original.getReceivedQty()) ? original.getReceivedQty() : 0;
        int remainReceiveQty = originalReceiveQty - linkQty;

        SoReturnPrestockDetailEntity newDetail = buildLeftoverDetail(original, remainReceiveQty);
        // 原行收敛为本次认领数量，剩余部分已转入新行
        original.setReceivedQty(linkQty);
        return newDetail;
    }

    /**
     * 构建拆行产生的剩余未关联详情行：复制原行的商品信息，携带传入的剩余实际收货数量，
     * 并清空所有关联相关字段（等待后续单独关联）。落库由调用方负责。
     */
    private SoReturnPrestockDetailEntity buildLeftoverDetail(SoReturnPrestockDetailEntity original,
                                                             int remainReceivedQty) {
        SoReturnPrestockDetailEntity newDetail = new SoReturnPrestockDetailEntity()
                .setMainId(original.getMainId())
                .setParentDetailId(original.getId())
                .setSkuId(original.getSkuId())
                .setSkuNo(original.getSkuNo())
                .setProductName(original.getProductName())
                .setProductImageUrl(original.getProductImageUrl())
                .setEan(original.getEan())
                .setReceivedQty(remainReceivedQty)
                .setClaimedQty(0)
                .setClaimStatus(PrestockClaimStatusEnum.UNLINKED.getStatus())
                // 剩余未关联部分，关联相关字段清空（含 platformOrderCode、dictPlatform，均仅由关联操作写入），等待后续单独关联
                .setPlatformOrderCode("")
                .setDictPlatform("")
                .setAfterSaleId("").setAfterSaleCode("")
                .setSoId("").setSoCode("")
                .setSoReturnId("").setSoReturnCode("")
                .setShopId("").setShopName("")
                .setSalesOrgId("").setSalesOrgName("")
                .setSalesDeptId("").setSalesDeptName("")
                .setSellerId("").setSellerName("")
                .setRemark(original.getRemark())
                // 拆行保留原行不良品标识，避免剩余行丢失良/不良状态
                .setDefectiveProductFlag(Boolean.TRUE.equals(original.getDefectiveProductFlag()));
        // 预分配 ID：拆行剩余行可能在落库前被再次拆分，需以其 ID 作为下一行的 parentDetailId 溯源；
        // 与 @TableId(ASSIGN_ID) 使用同一雪花算法生成器，插入时沿用该 ID
        newDetail.setId(IdWorker.getIdStr());
        return newDetail;
    }

    /**
     * 联动刷新主表关联状态和操作时间
     * 规则：全部已关联→LINKED；全部未关联→UNLINKED；混合→PARTIAL
     */
    private void refreshMainClaimStatus(String mainId) {
        List<SoReturnPrestockDetailEntity> details = soReturnPrestockDetailService.listByMainId(mainId);
        if (CollectionUtils.isEmpty(details)) {
            return;
        }
        long linkedCount = details.stream()
                .filter(d -> PrestockClaimStatusEnum.LINKED.getStatus().equals(d.getClaimStatus()))
                .count();
        String newClaimStatus;
        if (linkedCount == 0) {
            newClaimStatus = PrestockClaimStatusEnum.UNLINKED.getStatus();
        } else if (linkedCount == details.size()) {
            newClaimStatus = PrestockClaimStatusEnum.LINKED.getStatus();
        } else {
            newClaimStatus = PrestockClaimStatusEnum.PARTIAL.getStatus();
        }

        SoReturnPrestockEntity main = new SoReturnPrestockEntity();
        main.setId(mainId);
        main.setClaimStatus(newClaimStatus);
        main.setOperateTime(LocalDateTime.now());
        updateById(main);
    }

    /**
     * 批量保存详情行（新增场景）
     */
    private void saveDetailList(String mainId, List<SoReturnPrestockDetailDTO.Add> addList) {
        for (SoReturnPrestockDetailDTO.Add dto : addList) {
            if (Objects.isNull(dto.getReceivedQty()) || dto.getReceivedQty() <= 0) {
                throw new ServiceException(ApiError.SO_RETURN_PRESTOCK_RETURN_QTY_INVALID, dto.getSkuNo());
            }
        }
        List<SoReturnPrestockDetailEntity> entities = addList.stream().map(dto -> {
            SoReturnPrestockDetailEntity d = new SoReturnPrestockDetailEntity();
            d.setMainId(mainId)
                    .setParentDetailId("")
                    .setSkuId(CharSequenceUtil.emptyToDefault(dto.getSkuId(), ""))
                    .setSkuNo(dto.getSkuNo())
                    .setProductName(CharSequenceUtil.emptyToDefault(dto.getProductName(), ""))
                    .setProductImageUrl(CharSequenceUtil.emptyToDefault(dto.getProductImageUrl(), ""))
                    .setEan(CharSequenceUtil.emptyToDefault(dto.getEan(), ""))
                    .setReceivedQty(dto.getReceivedQty())
                    .setClaimedQty(0)
                    .setClaimStatus(PrestockClaimStatusEnum.UNLINKED.getStatus())
                    .setAfterSaleId("").setAfterSaleCode("")
                    .setSoId("").setSoCode("")
                    .setSoReturnId("").setSoReturnCode("")
                    .setShopId("").setShopName("")
                    .setSalesOrgId("").setSalesOrgName("")
                    .setSalesDeptId("").setSalesDeptName("")
                    .setSellerId("").setSellerName("")
                    // 新增时尚未关联售后单/店铺，平台订单号、平台字典值均未知，留空；由后续关联操作写入
                    .setPlatformOrderCode("")
                    .setDictPlatform("")
                    .setRemark(CharSequenceUtil.emptyToDefault(dto.getRemark(), ""))
                    .setDefectiveProductFlag(Boolean.TRUE.equals(dto.getDefectiveProductFlag()));
            return d;
        }).collect(Collectors.toList());
        soReturnPrestockDetailService.saveBatch(entities, 500);
    }

    /**
     * 将 Entity 转换为详情出参 View
     */
    /**
     * 批量反查预入库单明细认领后生成的退货入库单（id/code），避免逐行查询。
     * 仅统计未作废（invalidStatus=false）的退货入库单；一个预入库单明细最多对应一张退货入库单。
     */
    private Map<String, SoReturnInstockEntity> listReturnInstockByPrestockDetailIds(List<String> prestockDetailIds) {
        if (CollUtil.isEmpty(prestockDetailIds)) {
            return Collections.emptyMap();
        }
        List<SoReturnInstockDetailEntity> instockDetails = soReturnInstockDetailService.list(
                Wrappers.<SoReturnInstockDetailEntity>lambdaQuery()
                        .in(SoReturnInstockDetailEntity::getPrestockDetailId, prestockDetailIds)
                        .ne(SoReturnInstockDetailEntity::getPrestockDetailId, ""));
        if (CollUtil.isEmpty(instockDetails)) {
            return Collections.emptyMap();
        }
        List<String> mainIds = instockDetails.stream().map(SoReturnInstockDetailEntity::getMainId)
                .distinct().collect(Collectors.toList());
        Map<String, SoReturnInstockEntity> mainById = soReturnInstockService.listByIds(mainIds).stream()
                .filter(m -> !Boolean.TRUE.equals(m.getInvalidStatus()))
                .collect(Collectors.toMap(SoReturnInstockEntity::getId, Function.identity(), (a, b) -> a));
        Map<String, SoReturnInstockEntity> result = new HashMap<>();
        for (SoReturnInstockDetailEntity d : instockDetails) {
            SoReturnInstockEntity main = mainById.get(d.getMainId());
            if (Objects.nonNull(main)) {
                result.put(d.getPrestockDetailId(), main);
            }
        }
        return result;
    }

    private SoReturnPrestockDetailDTO.View convertDetailToView(SoReturnPrestockDetailEntity e,
                                                               Map<String, SoReturnInstockEntity> returnInstockByPrestockDetailId) {
        SoReturnPrestockDetailDTO.View v = new SoReturnPrestockDetailDTO.View();
        v.setId(e.getId());
        v.setVersion(e.getVersion());
        v.setMainId(e.getMainId());
        v.setParentDetailId(e.getParentDetailId());
        v.setAfterSaleId(e.getAfterSaleId());
        v.setAfterSaleCode(e.getAfterSaleCode());
        v.setPlatformOrderCode(e.getPlatformOrderCode());
        v.setDictPlatform(e.getDictPlatform());
        v.setSoId(e.getSoId());
        v.setSoCode(e.getSoCode());
        v.setSoReturnId(e.getSoReturnId());
        v.setSoReturnCode(e.getSoReturnCode());
        v.setShopId(e.getShopId());
        v.setShopName(e.getShopName());
        v.setSkuId(e.getSkuId());
        v.setSkuNo(e.getSkuNo());
        v.setProductName(e.getProductName());
        v.setProductImageUrl(e.getProductImageUrl());
        v.setEan(e.getEan());
        v.setReceivedQty(e.getReceivedQty());
        v.setClaimedQty(e.getClaimedQty());
        v.setClaimStatus(e.getClaimStatus());
        v.setClaimStatusName(PrestockClaimStatusEnum.getName(e.getClaimStatus()));
        v.setSalesOrgId(e.getSalesOrgId());
        v.setSalesOrgName(e.getSalesOrgName());
        v.setSalesDeptId(e.getSalesDeptId());
        v.setSalesDeptName(e.getSalesDeptName());
        v.setSellerId(e.getSellerId());
        v.setSellerName(e.getSellerName());
        v.setRemark(e.getRemark());
        v.setDefectiveProductFlag(Boolean.TRUE.equals(e.getDefectiveProductFlag()));
        SoReturnInstockEntity returnInstock = returnInstockByPrestockDetailId.get(e.getId());
        if (Objects.nonNull(returnInstock)) {
            v.setReturnInstockId(returnInstock.getId());
            v.setReturnInstockCode(returnInstock.getCode());
        }
        return v;
    }

    /**
     * 校验由退货入库单表单发起创建预入库单的前提条件：
     * 退货客户为空（否则应直接保存退货入库单，创建预入库单没有意义）；退货物流单号非空（预入库单以物流单号唯一）
     */
    private void checkCustomerAndLogisticCodeForPrestock(String customerId, String returnLogisticCode) {
        if (CharSequenceUtil.isNotBlank(customerId)) {
            throw new ServiceException(ApiError.SO_RETURN_PRESTOCK_CUSTOMER_NOT_EMPTY_FORBIDDEN);
        }
        if (CharSequenceUtil.isBlank(returnLogisticCode)) {
            throw new ServiceException(ApiError.SO_RETURN_PRESTOCK_RETURN_LOGISTIC_CODE_REQUIRED);
        }
    }

    /**
     * 由【退货入库单表单】参数组装预入库单详情行；产品名称/图片/EAN 通过 SKU ID 批量补齐
     */
    private List<SoReturnPrestockDetailDTO.Add> buildPrestockDetailListFromInstock(
            List<SoReturnPrestockDetailDTO.FromInstock> instockDetailList) {
        if (CollUtil.isEmpty(instockDetailList)) {
            throw new ServiceException(ApiError.COMMON_PARAM_REQUIRED, "产品明细");
        }
        Map<String, SkuVO> skuVOMap = listSkuVOMap(instockDetailList.stream()
                .map(SoReturnPrestockDetailDTO.FromInstock::getSkuId).collect(Collectors.toList()));
        return instockDetailList.stream().map(d -> {
            if (Objects.isNull(d.getRealQty()) || d.getRealQty() <= 0) {
                throw new ServiceException(ApiError.SO_RETURN_PRESTOCK_REAL_QTY_INVALID, d.getSkuNo());
            }
            SkuVO skuVO = skuVOMap.getOrDefault(d.getSkuId(), new SkuVO());
            SoReturnPrestockDetailDTO.Add detail = new SoReturnPrestockDetailDTO.Add();
            detail.setSkuId(d.getSkuId());
            detail.setSkuNo(d.getSkuNo());
            detail.setProductName(CharSequenceUtil.sub(skuVO.getSkuName(), 0, PRODUCT_NAME_MAX_LENGTH));
            detail.setProductImageUrl(firstDisplayImageUrl(skuVO.getSkuImagesUrl()));
            detail.setEan(skuVO.getEan());
            // 手动创建场景：实际收货数量取自表单实退数量
            detail.setReceivedQty(d.getRealQty());
            detail.setRemark(d.getRemark());
            detail.setDefectiveProductFlag(Boolean.TRUE.equals(d.getDefectiveProductFlag()));
            return detail;
        }).collect(Collectors.toList());
    }

    /**
     * 按 skuId 从 PLM 批量补全预入库单明细的产品名称/图片/EAN（就地修改传入的明细列表）。
     * <p>用于海外仓拉取场景：明细来源于平台SKU映射（{@code MappingSkuViewDTO} 仅含产品名称，无图片/EAN），
     * 图片/EAN 缺失、产品名称也可能因未匹配到映射而为空。仅对已解析出 skuId 且原值为空的字段补齐，
     * 不覆盖已有值；未解析出 skuId 的明细（三无包裹）无法补全，保持原样由运营人工核对。</p>
     */
    private void enrichDetailProductInfoBySkuId(List<SoReturnPrestockDetailDTO.Add> detailList) {
        if (CollUtil.isEmpty(detailList)) {
            return;
        }
        Map<String, SkuVO> skuVOMap = listSkuVOMap(detailList.stream()
                .map(SoReturnPrestockDetailDTO.Add::getSkuId).collect(Collectors.toList()));
        if (CollUtil.isEmpty(skuVOMap)) {
            return;
        }
        for (SoReturnPrestockDetailDTO.Add detail : detailList) {
            SkuVO skuVO = skuVOMap.get(detail.getSkuId());
            if (Objects.isNull(skuVO)) {
                continue;
            }
            if (CharSequenceUtil.isBlank(detail.getProductName())) {
                detail.setProductName(CharSequenceUtil.sub(skuVO.getSkuName(), 0, PRODUCT_NAME_MAX_LENGTH));
            }
            if (CharSequenceUtil.isBlank(detail.getProductImageUrl())) {
                detail.setProductImageUrl(firstDisplayImageUrl(skuVO.getSkuImagesUrl()));
            }
            if (CharSequenceUtil.isBlank(detail.getEan())) {
                detail.setEan(skuVO.getEan());
            }
        }
    }

    /**
     * PLM 返回的 SKU 图片字段（images_url）可能是多张图片以逗号拼接的字符串，
     * 而预入库单明细 product_image_url 列长度有限（varchar(500)），直接整串写入在图片较多时
     * 会触发 "value too long"；此处仅取第一张图作为展示图，并按列长度兜底截断
     */
    private String firstDisplayImageUrl(String imagesUrl) {
        if (CharSequenceUtil.isBlank(imagesUrl)) {
            return "";
        }
        String firstUrl = CharSequenceUtil.subBefore(imagesUrl, ",", false);
        return CharSequenceUtil.sub(firstUrl, 0, PRODUCT_IMAGE_URL_MAX_LENGTH);
    }

    /**
     * 按 SKU ID 批量查询产品信息，返回 skuId -> SkuVO 映射（查询失败或未命中时对应位置不进入 Map，由调用方兜底空对象）
     */
    private Map<String, SkuVO> listSkuVOMap(List<String> skuIds) {
        List<String> distinctSkuIds = skuIds.stream().filter(CharSequenceUtil::isNotBlank).distinct().collect(Collectors.toList());
        if (CollUtil.isEmpty(distinctSkuIds)) {
            return Collections.emptyMap();
        }
        List<SkuVO> skuVOList = plmTaskFeign.listSkuProductByIds(distinctSkuIds);
        if (CollUtil.isEmpty(skuVOList)) {
            return Collections.emptyMap();
        }
        return skuVOList.stream().collect(Collectors.toMap(SkuVO::getSkuId, v -> v, (a, b) -> a));
    }

    /**
     * 组装预入库单新增入参：库存组织/仓库信息通过仓库 ID 反查补齐；来源类型固定为 MANUAL（人工在退货入库单表单发起）
     */
    private SoReturnPrestockDTO.Add buildPrestockAddFromInstockParams(String type, String returnLogisticCode,
                                                                      String thirdCode, String warehouseId, String returnTypeDict,
                                                                      String soReturnCode, List<SoReturnPrestockDetailDTO.Add> detailList) {
        if (CharSequenceUtil.isBlank(warehouseId)) {
            throw new ServiceException(ApiError.SO_RETURN_PRESTOCK_WAREHOUSE_REQUIRED);
        }
        WarehouseEntity warehouse = warehouseService.getById(warehouseId);
        if (Objects.isNull(warehouse)) {
            throw new ServiceException(ApiError.COMMON_NOT_FOUND, "仓库");
        }
        String inventoryOrgName = "";
        if (CharSequenceUtil.isNotBlank(warehouse.getOrgId())) {
            SysAccountingCompanyEntity company = sysUserFeign.getCompanyById(warehouse.getOrgId());
            inventoryOrgName = Objects.nonNull(company) ? company.getCompanyName() : "";
        }

        SoReturnPrestockDTO.Add prestockAdd = new SoReturnPrestockDTO.Add();
        prestockAdd.setReturnLogisticCode(returnLogisticCode);
        prestockAdd.setType(type);
        prestockAdd.setInventoryOrgId(warehouse.getOrgId());
        prestockAdd.setInventoryOrgName(inventoryOrgName);
        prestockAdd.setWarehouseId(warehouse.getId());
        prestockAdd.setWarehouseName(warehouse.getName());
        prestockAdd.setDictReturnType(returnTypeDict);
        prestockAdd.setThirdCode(CharSequenceUtil.emptyToDefault(thirdCode, ""));
        prestockAdd.setRemark(CharSequenceUtil.isNotBlank(soReturnCode) ? "原退货订单号：" + soReturnCode : "");
        prestockAdd.setDetailList(detailList);
        return prestockAdd;
    }

    /**
     * 预入库单创建（人工由退货入库单表单发起、或系统海外仓自动拉取）后，同时生成并自动审核一张
     * "其它入库单"（三无退货预入库类型），使预入库真正增加库存；
     * 写法参照旺店通预入库范式 {@code OtherInstockServiceImpl#buildWdtPreStock}，
     * 走同一个 {@code addAndApprove} 入口完成新增、提交、审核并触发库存联动，与退货入库单原有库存联动方式互不干扰。
     * 其它入库生成失败需要预入库单一并回滚，异常直接向上抛出，由调用方的事务统一处理。
     * <p>仅对已解析出 skuId 的明细行生成库存联动：海外仓自动拉取场景（{@link #createFromOverseasWhHeadless}）
     * 允许平台SKU未映射到内部SKU时用空skuId占位落预入库单明细
     * （见调用方 {@code buildPrestockDetailList}），若把空skuId传入其他入库单，会一路带到库存核心服务
     * （{@code OtherInstockServiceImpl#updateInventoryTransCore}）导致报错回滚整单，或落下无法追溯的空SKU库存记录。
     * 未解析行只落预入库单明细，等运营人工核实SKU后再走关联流程；全部行都未解析到SKU时整单不生成其它入库单。</p>
     */
    private void generateOtherInstockForPrestock(String prestockId, SoReturnPrestockDTO.Add prestockAdd) {
        List<SoReturnPrestockDetailDTO.Add> addDetailList = prestockAdd.getDetailList().stream()
                .filter(d -> CharSequenceUtil.isNotBlank(d.getSkuId()))
                .collect(Collectors.toList());
        if (CollUtil.isEmpty(addDetailList)) {
            log.warn("[预入库单联动生成其它入库单]全部明细行均未解析到内部SKU，跳过生成其它入库单：预入库单id={}",
                    prestockId);
            return;
        }
        SoReturnPrestockEntity prestockEntity = getByIdOrThrow(prestockId);
        Map<String, SkuVO> skuVOMap = listSkuVOMap(addDetailList.stream()
                .map(SoReturnPrestockDetailDTO.Add::getSkuId).collect(Collectors.toList()));
        // 按 skuId 分组后组内按创建顺序（create_time + id 兜底排序）与入参详情行配对，用于回填 prestockDetailId
        // 溯源到预入库单具体明细行；相比整体按下标对齐，同 SKU 场景下即使排序偶发抖动也不会跨SKU错配
        Map<String, Deque<SoReturnPrestockDetailEntity>> persistedDetailQueueBySkuId = soReturnPrestockDetailService.listByMainId(prestockId)
                .stream()
                .collect(Collectors.groupingBy(SoReturnPrestockDetailEntity::getSkuId, Collectors.toCollection(LinkedList::new)));
        List<SysDepartmentEntity> deptList = sysUserFeign.getDeptByIds(Collections.singletonList(WmsConstant.DEFAULT_WAREHOUSING_DEPT_ID));
        if (CollectionUtils.isEmpty(deptList)) {
            throw new ServiceException(ApiError.SO_RETURN_PRESTOCK_WAREHOUSING_DEPT_NOT_FOUND);
        }
        SysDepartmentEntity dept = deptList.get(0);

        OtherInstockEntity otherInstockEntity = new OtherInstockEntity();
        otherInstockEntity.setBillDate(prestockEntity.getReceivedTime().toLocalDate());
        otherInstockEntity.setInventoryDirection(InventoryDirectionEnum.ORDINARY.getCode());
        otherInstockEntity.setWarehouseId(prestockAdd.getWarehouseId());
        otherInstockEntity.setWarehouseName(prestockAdd.getWarehouseName());
        otherInstockEntity.setOrgId(prestockAdd.getInventoryOrgId());
        otherInstockEntity.setOrgName(prestockAdd.getInventoryOrgName());
        otherInstockEntity.setDeptId(dept.getId());
        otherInstockEntity.setDeptName(dept.getName());
        otherInstockEntity.setType(InstockTypeEnum.THREE_NO_PRODUCT_PRE_INSTOCK.getCode());
        otherInstockEntity.setReturnLogisticCode(prestockAdd.getReturnLogisticCode());
        otherInstockEntity.setThirdCode(CharSequenceUtil.emptyToDefault(prestockAdd.getThirdCode(), ""));
        otherInstockEntity.setSourceType(SourceTypeEnum.SO_RETURN_PRESTOCK.getCode());
        otherInstockEntity.setSourceId(prestockId);
        otherInstockEntity.setSourceCode(prestockEntity.getCode());
        otherInstockEntity.setRemark(CharSequenceUtil.format("预入库单【{}】自动生成", prestockEntity.getCode()));

        List<OtherInstockDetailEntity> detailEntityList = new ArrayList<>();
        for (SoReturnPrestockDetailDTO.Add detail : addDetailList) {
            SkuVO skuVO = skuVOMap.getOrDefault(detail.getSkuId(), new SkuVO());
            OtherInstockDetailEntity detailEntity = new OtherInstockDetailEntity();
            detailEntity.setSkuId(detail.getSkuId());
            detailEntity.setSkuNo(detail.getSkuNo());
            detailEntity.setActualQty(Objects.nonNull(detail.getReceivedQty()) ? detail.getReceivedQty() : 0);
            detailEntity.setUnit(skuVO.getUnitName());
            detailEntity.setRemark(detail.getRemark());
            detailEntity.setDefectiveProductFlag(Boolean.TRUE.equals(detail.getDefectiveProductFlag()));
            Deque<SoReturnPrestockDetailEntity> persistedQueue = persistedDetailQueueBySkuId.get(detail.getSkuId());
            if (Objects.nonNull(persistedQueue) && !persistedQueue.isEmpty()) {
                detailEntity.setPrestockDetailId(persistedQueue.pollFirst().getId());
            }
            detailEntityList.add(detailEntity);
        }
        otherInstockEntity.setDetailEntityList(detailEntityList);
        long start = System.currentTimeMillis();
        otherInstockService.addAndApprove(otherInstockEntity, false);
        long cost = System.currentTimeMillis() - start;
        if (cost > LINK_LOOP_WARN_THRESHOLD_MS) {
            log.warn("[预入库单联动生成其它入库单]addAndApprove耗时过长：预入库单={}，明细行数={}，耗时={}ms",
                    prestockEntity.getCode(), detailEntityList.size(), cost);
        }
        if (addDetailList.size() < prestockAdd.getDetailList().size()) {
            log.warn("[预入库单联动生成其它入库单]部分明细行未解析到内部SKU，未计入本次库存联动：预入库单={}，" +
                            "总行数={}，已联动行数={}",
                    prestockEntity.getCode(), prestockAdd.getDetailList().size(), addDetailList.size());
        }
    }

    /**
     * 根据 ID 获取实体，不存在则抛出业务异常
     */
    private SoReturnPrestockEntity getByIdOrThrow(String id) {
        SoReturnPrestockEntity entity = getById(id);
        if (Objects.isNull(entity) || Boolean.TRUE.equals(entity.getIsDeleted())) {
            throw new ServiceException(ApiError.COMMON_NOT_FOUND, "预入库单");
        }
        return entity;
    }
}
