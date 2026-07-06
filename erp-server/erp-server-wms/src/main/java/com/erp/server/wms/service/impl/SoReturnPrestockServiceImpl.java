package com.erp.server.wms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.annotation.DistributeLocker;
import com.common.business.config.DocNoGenHelper;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.oms.enums.BillTypeEnum;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.sys.entity.SysAccountingCompanyEntity;
import com.erp.model.sys.entity.SysDepartmentEntity;
import com.erp.model.wms.dto.SoReturnInstockDTO;
import com.erp.model.wms.dto.SoReturnInstockDetailDTO;
import com.erp.model.wms.dto.SoReturnPrestockDTO;
import com.erp.model.wms.dto.SoReturnPrestockDetailDTO;
import com.erp.model.wms.entity.OtherInstockDetailEntity;
import com.erp.model.wms.entity.OtherInstockEntity;
import com.erp.model.wms.entity.SoReturnPrestockDetailEntity;
import com.erp.model.wms.entity.SoReturnPrestockEntity;
import com.erp.model.wms.entity.WarehouseEntity;
import com.erp.model.wms.enums.InstockTypeEnum;
import com.erp.model.wms.enums.InventoryDirectionEnum;
import com.erp.model.wms.enums.PrestockLinkStatusEnum;
import com.erp.model.wms.enums.PrestockSourceTypeEnum;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.wms.constant.WmsConstant;
import com.erp.server.wms.mapper.SoReturnPrestockMapper;
import com.erp.server.wms.service.OtherInstockService;
import com.erp.server.wms.service.SoReturnPrestockDetailService;
import com.erp.server.wms.service.SoReturnPrestockService;
import com.erp.server.wms.service.WarehouseService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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

    private static final String SO_RETURN_PRESTOCK_OVERSEAS_LOCK_KEY = "SO_RETURN_PRESTOCK_OVERSEAS";

    private static final String SO_RETURN_PRESTOCK_HEADLESS_LOCK_KEY = "SO_RETURN_PRESTOCK_HEADLESS";

    private static final String SO_RETURN_PRESTOCK_DETAIL_LINK_LOCK_KEY = "SO_RETURN_PRESTOCK_DETAIL_LINK";

    @Resource
    private DocNoGenHelper docNoGenHelper;

    @Resource
    private SoReturnPrestockDetailService soReturnPrestockDetailService;

    @Resource
    private WarehouseService warehouseService;

    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private OtherInstockService otherInstockService;

    // ===================== 分页查询 =====================

    @Override
    public PagingVO<SoReturnPrestockDTO.PagingView> paging(PagingDTO<SoReturnPrestockDTO.PagingParam> dto) {
        Page<SoReturnPrestockDTO.PagingView> page = new Page<>(dto.getPage(), dto.getPageSize());
        IPage<SoReturnPrestockDTO.PagingView> result = baseMapper.paging(page, dto.getParams());
        // 翻译枚举名称
        result.getRecords().forEach(v -> {
            v.setTypeName(BillTypeEnum.getName(v.getType()));
            v.setLinkStatusName(PrestockLinkStatusEnum.getName(v.getLinkStatus()));
            v.setSourceTypeName(PrestockSourceTypeEnum.getName(v.getSourceType()));
        });
        return new PagingVO<>(result);
    }

    // ===================== 新增 =====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String add(SoReturnPrestockDTO.Add dto) {
        // 物流单号唯一性校验
        checkLogisticCodeUnique(dto.getReturnLogisticCode(), null);
        return buildAndPersist(dto, dto.getReturnLogisticCode(), PrestockSourceTypeEnum.MANUAL.getStatus());
    }

    // ===================== 修改 =====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean update(SoReturnPrestockDTO.Update dto) {
        SoReturnPrestockEntity entity = getByIdOrThrow(dto.getId());
        entity.setInventoryOrgId(CharSequenceUtil.emptyToDefault(dto.getInventoryOrgId(), entity.getInventoryOrgId()))
              .setInventoryOrgName(CharSequenceUtil.emptyToDefault(dto.getInventoryOrgName(), entity.getInventoryOrgName()))
              .setWarehouseId(CharSequenceUtil.emptyToDefault(dto.getWarehouseId(), entity.getWarehouseId()))
              .setWarehouseName(CharSequenceUtil.emptyToDefault(dto.getWarehouseName(), entity.getWarehouseName()))
              .setReturnTypeDict(CharSequenceUtil.emptyToDefault(dto.getReturnTypeDict(), entity.getReturnTypeDict()))
              .setRemark(CharSequenceUtil.emptyToDefault(dto.getRemark(), entity.getRemark()))
              .setOperateTime(LocalDateTime.now());
        // 使用客户端提交时看到的 version 做乐观锁校验，而非重新查询出的服务端当前版本，
        // 否则并发场景下后到的过期编辑会静默覆盖他人已提交的修改；version 已在 DTO 层强制必传
        entity.setVersion(dto.getVersion());
        boolean updated = updateById(entity);
        if (!updated) {
            throw new ServiceException("预入库单数据已被修改，请刷新后重试");
        }

        // 更新详情行（仅支持修改实际收货数量与备注）
        updateDetailList(dto.getId(), dto.getDetailList());
        return Boolean.TRUE;
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
        view.setLinkStatus(entity.getLinkStatus());
        view.setLinkStatusName(PrestockLinkStatusEnum.getName(entity.getLinkStatus()));
        view.setSourceId(entity.getSourceId());
        view.setSourceCode(entity.getSourceCode());
        view.setSourceType(entity.getSourceType());
        view.setSourceTypeName(PrestockSourceTypeEnum.getName(entity.getSourceType()));
        view.setThirdCode(entity.getThirdCode());
        view.setInventoryOrgId(entity.getInventoryOrgId());
        view.setInventoryOrgName(entity.getInventoryOrgName());
        view.setWarehouseId(entity.getWarehouseId());
        view.setWarehouseName(entity.getWarehouseName());
        view.setReturnTypeDict(entity.getReturnTypeDict());
        view.setReturnInstockTime(entity.getReturnInstockTime());
        view.setOperateTime(entity.getOperateTime());
        view.setRemark(entity.getRemark());
        view.setCreateUserName(entity.getCreateUserName());
        view.setCreateTime(entity.getCreateTime());

        // 查询详情行
        List<SoReturnPrestockDetailEntity> detailEntities =
                soReturnPrestockDetailService.listByMainId(id);
        view.setDetailList(detailEntities.stream().map(this::convertDetailToView).collect(Collectors.toList()));
        return view;
    }

    // ===================== 关联售后单 =====================

    @Override
    @DistributeLocker(businessType = SO_RETURN_PRESTOCK_DETAIL_LINK_LOCK_KEY, keyName = "dto.detailId")
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO linkAfterSale(SoReturnPrestockDetailDTO.LinkAfterSale dto) {
        SoReturnPrestockDetailEntity detail = soReturnPrestockDetailService.getById(dto.getDetailId());
        if (Objects.isNull(detail) || Boolean.TRUE.equals(detail.getIsDeleted())) {
            return BatchResultDTO.fail(dto.getDetailId(), dto.getDetailId(), "详情行不存在");
        }
        if (PrestockLinkStatusEnum.LINKED.getStatus().equals(detail.getLinkStatus())) {
            return BatchResultDTO.fail(detail.getId(), detail.getSkuNo(), "该行已关联，请先解除关联");
        }
        if (Objects.isNull(dto.getLinkQty()) || dto.getLinkQty() <= 0) {
            return BatchResultDTO.fail(detail.getId(), detail.getSkuNo(), "关联数量必须大于0");
        }
        if (dto.getLinkQty() > detail.getReturnQty()) {
            return BatchResultDTO.fail(detail.getId(), detail.getSkuNo(),
                    "关联数量不能超过当前行退货数量：" + detail.getReturnQty());
        }

        // 触发拆行：关联数量 < 当前行数量时，拆出剩余数量为新行
        if (dto.getLinkQty() < detail.getReturnQty()) {
            splitDetail(detail, dto.getLinkQty());
        }

        // 更新当前行关联信息；platformOrderCode、dictPlatform 均取自本次关联的售后单自身，而非本行数据来源渠道
        detail.setAfterSaleId(dto.getAfterSaleId())
              .setAfterSaleCode(dto.getAfterSaleCode())
              .setPlatformOrderCode(CharSequenceUtil.emptyToDefault(dto.getPlatformOrderCode(), ""))
              .setDictPlatform(dto.getDictPlatform())
              .setSoId(CharSequenceUtil.emptyToDefault(dto.getSoId(), ""))
              .setSoCode(CharSequenceUtil.emptyToDefault(dto.getSoCode(), ""))
              .setSoReturnId(CharSequenceUtil.emptyToDefault(dto.getSoReturnId(), ""))
              .setSoReturnCode(CharSequenceUtil.emptyToDefault(dto.getSoReturnCode(), ""))
              .setShopId(CharSequenceUtil.emptyToDefault(dto.getShopId(), ""))
              .setShopName(CharSequenceUtil.emptyToDefault(dto.getShopName(), ""))
              .setSalesOrgId(CharSequenceUtil.emptyToDefault(dto.getSalesOrgId(), ""))
              .setSalesOrgName(CharSequenceUtil.emptyToDefault(dto.getSalesOrgName(), ""))
              .setSalesDeptId(CharSequenceUtil.emptyToDefault(dto.getSalesDeptId(), ""))
              .setSalesDeptName(CharSequenceUtil.emptyToDefault(dto.getSalesDeptName(), ""))
              .setReturnQty(dto.getLinkQty())
              .setLinkStatus(PrestockLinkStatusEnum.LINKED.getStatus());
        if (!soReturnPrestockDetailService.updateById(detail)) {
            return BatchResultDTO.fail(detail.getId(), detail.getSkuNo(), "关联失败，请重试");
        }

        // 联动更新主表关联状态和操作时间
        refreshMainLinkStatus(detail.getMainId());
        return BatchResultDTO.success(detail.getId(), detail.getSkuNo());
    }

    // ===================== 关联店铺 =====================

    @Override
    @DistributeLocker(businessType = SO_RETURN_PRESTOCK_DETAIL_LINK_LOCK_KEY, keyName = "dto.detailId")
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO linkShop(SoReturnPrestockDetailDTO.LinkShop dto) {
        SoReturnPrestockDetailEntity detail = soReturnPrestockDetailService.getById(dto.getDetailId());
        if (Objects.isNull(detail) || Boolean.TRUE.equals(detail.getIsDeleted())) {
            return BatchResultDTO.fail(dto.getDetailId(), dto.getDetailId(), "详情行不存在");
        }
        if (PrestockLinkStatusEnum.LINKED.getStatus().equals(detail.getLinkStatus())) {
            return BatchResultDTO.fail(detail.getId(), detail.getSkuNo(), "该行已关联，请先解除关联");
        }
        if (Objects.isNull(dto.getLinkQty()) || dto.getLinkQty() <= 0) {
            return BatchResultDTO.fail(detail.getId(), detail.getSkuNo(), "关联数量必须大于0");
        }
        if (dto.getLinkQty() > detail.getReturnQty()) {
            return BatchResultDTO.fail(detail.getId(), detail.getSkuNo(),
                    "关联数量不能超过当前行退货数量：" + detail.getReturnQty());
        }

        if (dto.getLinkQty() < detail.getReturnQty()) {
            splitDetail(detail, dto.getLinkQty());
        }

        // dictPlatform 取自本次关联店铺自身所属平台，而非本行数据来源渠道
        detail.setShopId(dto.getShopId())
              .setShopName(CharSequenceUtil.emptyToDefault(dto.getShopName(), ""))
              .setDictPlatform(dto.getDictPlatform())
              .setSalesOrgId(CharSequenceUtil.emptyToDefault(dto.getSalesOrgId(), ""))
              .setSalesOrgName(CharSequenceUtil.emptyToDefault(dto.getSalesOrgName(), ""))
              .setSalesDeptId(CharSequenceUtil.emptyToDefault(dto.getSalesDeptId(), ""))
              .setSalesDeptName(CharSequenceUtil.emptyToDefault(dto.getSalesDeptName(), ""))
              .setReturnQty(dto.getLinkQty())
              .setLinkStatus(PrestockLinkStatusEnum.LINKED.getStatus());
        if (!soReturnPrestockDetailService.updateById(detail)) {
            return BatchResultDTO.fail(detail.getId(), detail.getSkuNo(), "关联失败，请重试");
        }

        refreshMainLinkStatus(detail.getMainId());
        return BatchResultDTO.success(detail.getId(), detail.getSkuNo());
    }

    // ===================== 删除 =====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<BatchResultDTO> deleteByIds(List<String> ids) {
        List<BatchResultDTO> results = new ArrayList<>(ids.size());
        for (String id : ids) {
            try {
                SoReturnPrestockEntity entity = getByIdOrThrow(id);
                // 软删主表
                removeById(entity.getId());
                // 软删详情行
                soReturnPrestockDetailService.deleteByMainId(entity.getId());
                results.add(BatchResultDTO.success(id, entity.getCode()));
            } catch (Exception e) {
                log.error("删除预入库单失败, id={}", id, e);
                results.add(BatchResultDTO.fail(id, id, e.getMessage()));
            }
        }
        return results;
    }

    // ===================== 海外仓自动创建（系统内部） =====================

    @Override
    @DistributeLocker(businessType = SO_RETURN_PRESTOCK_OVERSEAS_LOCK_KEY, keyName = "dto.returnLogisticCode")
    @Transactional(rollbackFor = Exception.class)
    public String createFromOverseasWh(SoReturnPrestockDTO.Add dto) {
        // 幂等：同一物流单号已存在则直接返回
        SoReturnPrestockEntity existing = lambdaQuery()
                .eq(SoReturnPrestockEntity::getReturnLogisticCode, dto.getReturnLogisticCode())
                .eq(SoReturnPrestockEntity::getIsDeleted, false)
                .one();
        if (Objects.nonNull(existing)) {
            log.info("预入库单已存在，物流单号：{}，跳过创建", dto.getReturnLogisticCode());
            return existing.getId();
        }
        return buildAndPersist(dto, dto.getReturnLogisticCode(), PrestockSourceTypeEnum.OVERSEAS_WH.getStatus());
    }

    // ===================== 无物流单号+无参考单号自动创建（系统内部） =====================

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
                log.info("预入库单已存在（无头件），第三方单号：{}，跳过创建", dto.getThirdCode());
                return existing.getId();
            }
        } else {
            log.warn("无头件预入库单缺少第三方单号，跳过幂等校验，可能重复生成");
        }
        return buildAndPersist(dto, "", PrestockSourceTypeEnum.OVERSEAS_WH.getStatus());
    }

    // ===================== 由退货入库单新增/修改表单参数创建（人工触发） =====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String addFromReturnInstockAdd(SoReturnInstockDTO.Add dto) {
        checkCustomerAndLogisticCodeForPrestock(dto.getCustomerId(), dto.getReturnLogisticCode());
        List<SoReturnPrestockDetailDTO.Add> detailList = buildPrestockDetailListFromInstockAdd(dto.getDetailList());
        SoReturnPrestockDTO.Add prestockAdd = buildPrestockAddFromInstockParams(
                dto.getType(), dto.getReturnLogisticCode(), dto.getThirdCode(), dto.getWarehouseId(),
                dto.getSoReturnCode(), detailList);
        String prestockId = add(prestockAdd);
        generateOtherInstockForPrestock(prestockId, prestockAdd);
        return prestockId;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String addFromReturnInstockUpdate(SoReturnInstockDTO.Update dto) {
        checkCustomerAndLogisticCodeForPrestock(dto.getCustomerId(), dto.getReturnLogisticCode());
        List<SoReturnPrestockDetailDTO.Add> detailList = buildPrestockDetailListFromInstockUpdate(dto.getDetailList());
        SoReturnPrestockDTO.Add prestockAdd = buildPrestockAddFromInstockParams(
                dto.getType(), dto.getReturnLogisticCode(), null, dto.getWarehouseId(),
                dto.getSoReturnCode(), detailList);
        String prestockId = add(prestockAdd);
        generateOtherInstockForPrestock(prestockId, prestockAdd);
        return prestockId;
    }

    // ===================== 私有辅助方法 =====================

    /**
     * 构建预入库单主表并落库，随后保存详情行；由 {@link #add}、{@link #createFromOverseasWh}、
     * {@link #createFromOverseasWhHeadless} 在各自完成幂等/唯一性校验后调用。
     */
    private String buildAndPersist(SoReturnPrestockDTO.Add dto, String returnLogisticCode, String sourceType) {
        if (CollUtil.isEmpty(dto.getDetailList())) {
            throw new ServiceException(ApiError.COMMON_PARAM_REQUIRED, "预入库单详情行");
        }
        LocalDateTime now = LocalDateTime.now();
        SoReturnPrestockEntity entity = new SoReturnPrestockEntity()
                .setCode(docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_YRK))
                .setType(dto.getType())
                .setReturnLogisticCode(CharSequenceUtil.emptyToDefault(returnLogisticCode, ""))
                .setLinkStatus(PrestockLinkStatusEnum.UNLINKED.getStatus())
                .setSourceId(CharSequenceUtil.emptyToDefault(dto.getSourceId(), ""))
                .setSourceCode(CharSequenceUtil.emptyToDefault(dto.getSourceCode(), ""))
                .setSourceType(sourceType)
                .setThirdCode(CharSequenceUtil.emptyToDefault(dto.getThirdCode(), ""))
                .setInventoryOrgId(dto.getInventoryOrgId())
                .setInventoryOrgName(CharSequenceUtil.emptyToDefault(dto.getInventoryOrgName(), ""))
                .setWarehouseId(dto.getWarehouseId())
                .setWarehouseName(CharSequenceUtil.emptyToDefault(dto.getWarehouseName(), ""))
                .setReturnTypeDict(CharSequenceUtil.emptyToDefault(dto.getReturnTypeDict(), ""))
                .setReturnInstockTime(now)
                .setOperateTime(now)
                .setRemark(CharSequenceUtil.emptyToDefault(dto.getRemark(), ""));
        save(entity);

        saveDetailList(entity.getId(), dto.getDetailList());
        return entity.getId();
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
     * 拆行：将当前行剩余数量（returnQty - linkQty）创建为新的未关联详情行。
     * <p>已收货数量（receiveQty）按退货数量比例在原行与新行之间分配，避免拆行后
     * 两行收货数量之和与原值不一致（原行数量不足时按剩余全部下取整分配给新行）。</p>
     */
    private void splitDetail(SoReturnPrestockDetailEntity original, int linkQty) {
        int remainQty = original.getReturnQty() - linkQty;
        int originalReceiveQty = Objects.nonNull(original.getReceiveQty()) ? original.getReceiveQty() : 0;
        int splitReceiveQty = original.getReturnQty() == 0 ? 0
                : (int) Math.floor(originalReceiveQty * (double) remainQty / original.getReturnQty());

        SoReturnPrestockDetailEntity newDetail = new SoReturnPrestockDetailEntity()
                .setMainId(original.getMainId())
                .setParentDetailId(original.getId())
                .setSkuId(original.getSkuId())
                .setSkuNo(original.getSkuNo())
                .setProductName(original.getProductName())
                .setProductImageUrl(original.getProductImageUrl())
                .setEan(original.getEan())
                .setReturnQty(remainQty)
                .setReceiveQty(splitReceiveQty)
                .setClaimedQty(0)
                .setLinkStatus(PrestockLinkStatusEnum.UNLINKED.getStatus())
                // 剩余未关联部分，关联相关字段清空（含 platformOrderCode、dictPlatform，均仅由关联操作写入），等待后续单独关联
                .setPlatformOrderCode("")
                .setDictPlatform("")
                .setAfterSaleId("").setAfterSaleCode("")
                .setSoId("").setSoCode("")
                .setSoReturnId("").setSoReturnCode("")
                .setReturnInstockId("").setReturnInstockCode("")
                .setShopId("").setShopName("")
                .setSalesOrgId("").setSalesOrgName("")
                .setSalesDeptId("").setSalesDeptName("")
                .setRemark(original.getRemark());
        soReturnPrestockDetailService.save(newDetail);

        // 原行保留按比例分配后的收货数量，由调用方在后续 updateById 中一并落库
        original.setReceiveQty(originalReceiveQty - splitReceiveQty);
    }

    /**
     * 批量更新详情行（修改场景，仅支持修改实际收货数量与备注）
     *
     * @param mainId 当前正在修改的预入库单主表 ID，用于校验详情行归属，防止跨单篡改
     */
    private void updateDetailList(String mainId, List<SoReturnPrestockDetailDTO.Update> updateList) {
        if (CollUtil.isEmpty(updateList)) {
            return;
        }
        for (SoReturnPrestockDetailDTO.Update detailDto : updateList) {
            if (CharSequenceUtil.isBlank(detailDto.getId())) {
                continue;
            }
            SoReturnPrestockDetailEntity detail = soReturnPrestockDetailService.getById(detailDto.getId());
            if (Objects.isNull(detail) || Boolean.TRUE.equals(detail.getIsDeleted())) {
                throw new ServiceException(ApiError.COMMON_NOT_FOUND, "预入库单详情行");
            }
            if (!mainId.equals(detail.getMainId())) {
                throw new ServiceException("详情行不属于当前预入库单");
            }
            if (Objects.nonNull(detailDto.getReceiveQty())) {
                detail.setReceiveQty(detailDto.getReceiveQty());
            }
            if (Objects.nonNull(detailDto.getRemark())) {
                detail.setRemark(detailDto.getRemark());
            }
            // version 已在 DTO 层强制必传，使用客户端提交时看到的版本做乐观锁校验
            detail.setVersion(detailDto.getVersion());
            boolean updated = soReturnPrestockDetailService.updateById(detail);
            if (!updated) {
                throw new ServiceException("预入库单详情行数据已被修改，请刷新后重试");
            }
        }
    }

    /**
     * 联动刷新主表关联状态和操作时间
     * 规则：全部已关联→LINKED；全部未关联→UNLINKED；混合→PARTIAL
     */
    private void refreshMainLinkStatus(String mainId) {
        List<SoReturnPrestockDetailEntity> details = soReturnPrestockDetailService.listByMainId(mainId);
        if (CollectionUtils.isEmpty(details)) {
            return;
        }
        long linkedCount = details.stream()
                .filter(d -> PrestockLinkStatusEnum.LINKED.getStatus().equals(d.getLinkStatus()))
                .count();
        String newLinkStatus;
        if (linkedCount == 0) {
            newLinkStatus = PrestockLinkStatusEnum.UNLINKED.getStatus();
        } else if (linkedCount == details.size()) {
            newLinkStatus = PrestockLinkStatusEnum.LINKED.getStatus();
        } else {
            newLinkStatus = PrestockLinkStatusEnum.PARTIAL.getStatus();
        }

        SoReturnPrestockEntity main = new SoReturnPrestockEntity();
        main.setId(mainId);
        main.setLinkStatus(newLinkStatus);
        main.setOperateTime(LocalDateTime.now());
        updateById(main);
    }

    /**
     * 批量保存详情行（新增场景）
     */
    private void saveDetailList(String mainId, List<SoReturnPrestockDetailDTO.Add> addList) {
        for (SoReturnPrestockDetailDTO.Add dto : addList) {
            if (Objects.isNull(dto.getReturnQty()) || dto.getReturnQty() <= 0) {
                throw new ServiceException("退货数量必须大于0：" + dto.getSkuNo());
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
             .setReturnQty(dto.getReturnQty())
             .setReceiveQty(Objects.nonNull(dto.getReceiveQty()) ? dto.getReceiveQty() : 0)
             .setClaimedQty(0)
             .setLinkStatus(PrestockLinkStatusEnum.UNLINKED.getStatus())
             .setAfterSaleId("").setAfterSaleCode("")
             .setSoId("").setSoCode("")
             .setSoReturnId("").setSoReturnCode("")
             .setReturnInstockId("").setReturnInstockCode("")
             .setShopId("").setShopName("")
             .setSalesOrgId("").setSalesOrgName("")
             .setSalesDeptId("").setSalesDeptName("")
             // 新增时尚未关联售后单/店铺，平台订单号、平台字典值均未知，留空；由后续关联操作写入
             .setPlatformOrderCode("")
             .setDictPlatform("")
             .setRemark(CharSequenceUtil.emptyToDefault(dto.getRemark(), ""));
            return d;
        }).collect(Collectors.toList());
        soReturnPrestockDetailService.saveBatch(entities, 500);
    }

    /**
     * 将 Entity 转换为详情出参 View
     */
    private SoReturnPrestockDetailDTO.View convertDetailToView(SoReturnPrestockDetailEntity e) {
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
        v.setReturnInstockId(e.getReturnInstockId());
        v.setReturnInstockCode(e.getReturnInstockCode());
        v.setShopId(e.getShopId());
        v.setShopName(e.getShopName());
        v.setSkuId(e.getSkuId());
        v.setSkuNo(e.getSkuNo());
        v.setProductName(e.getProductName());
        v.setProductImageUrl(e.getProductImageUrl());
        v.setEan(e.getEan());
        v.setReturnQty(e.getReturnQty());
        v.setReceiveQty(e.getReceiveQty());
        v.setClaimedQty(e.getClaimedQty());
        v.setLinkStatus(e.getLinkStatus());
        v.setLinkStatusName(PrestockLinkStatusEnum.getName(e.getLinkStatus()));
        v.setSalesOrgId(e.getSalesOrgId());
        v.setSalesOrgName(e.getSalesOrgName());
        v.setSalesDeptId(e.getSalesDeptId());
        v.setSalesDeptName(e.getSalesDeptName());
        v.setRemark(e.getRemark());
        return v;
    }

    /**
     * 校验由退货入库单表单发起创建预入库单的前提条件：
     * 退货客户为空（否则应直接保存退货入库单，创建预入库单没有意义）；退货物流单号非空（预入库单以物流单号唯一）
     */
    private void checkCustomerAndLogisticCodeForPrestock(String customerId, String returnLogisticCode) {
        if (CharSequenceUtil.isNotBlank(customerId)) {
            throw new ServiceException("退货客户不为空时无法创建预入库单，请直接保存退货入库单");
        }
        if (CharSequenceUtil.isBlank(returnLogisticCode)) {
            throw new ServiceException("退货物流单号不能为空");
        }
    }

    /**
     * 由【新增退货入库单】表单参数组装预入库单详情行；产品名称/图片/EAN 通过 SKU ID 批量补齐
     */
    private List<SoReturnPrestockDetailDTO.Add> buildPrestockDetailListFromInstockAdd(
            List<SoReturnInstockDetailDTO.Add> instockDetailList) {
        if (CollUtil.isEmpty(instockDetailList)) {
            throw new ServiceException(ApiError.COMMON_PARAM_REQUIRED, "产品明细");
        }
        Map<String, SkuVO> skuVOMap = listSkuVOMap(instockDetailList.stream()
                .map(SoReturnInstockDetailDTO.Add::getSkuId).collect(Collectors.toList()));
        return instockDetailList.stream().map(d -> {
            if (Objects.isNull(d.getRealQty()) || d.getRealQty() <= 0) {
                throw new ServiceException("退货数量必须大于0：" + d.getSkuNo());
            }
            SkuVO skuVO = skuVOMap.getOrDefault(d.getSkuId(), new SkuVO());
            SoReturnPrestockDetailDTO.Add detail = new SoReturnPrestockDetailDTO.Add();
            detail.setSkuId(d.getSkuId());
            detail.setSkuNo(d.getSkuNo());
            detail.setProductName(skuVO.getSkuName());
            detail.setProductImageUrl(skuVO.getSkuImagesUrl());
            detail.setEan(skuVO.getEan());
            detail.setReturnQty(d.getRealQty());
            detail.setReceiveQty(d.getReceiveQty());
            detail.setRemark(d.getRemark());
            return detail;
        }).collect(Collectors.toList());
    }

    /**
     * 由【修改退货入库单】表单参数组装预入库单详情行；产品名称/图片/EAN 通过 SKU ID 批量补齐
     */
    private List<SoReturnPrestockDetailDTO.Add> buildPrestockDetailListFromInstockUpdate(
            List<SoReturnInstockDetailDTO.Update> instockDetailList) {
        if (CollUtil.isEmpty(instockDetailList)) {
            throw new ServiceException(ApiError.COMMON_PARAM_REQUIRED, "产品明细");
        }
        Map<String, SkuVO> skuVOMap = listSkuVOMap(instockDetailList.stream()
                .map(SoReturnInstockDetailDTO.Update::getSkuId).collect(Collectors.toList()));
        return instockDetailList.stream().map(d -> {
            if (Objects.isNull(d.getRealQty()) || d.getRealQty() <= 0) {
                throw new ServiceException("退货数量必须大于0：" + d.getSkuNo());
            }
            SkuVO skuVO = skuVOMap.getOrDefault(d.getSkuId(), new SkuVO());
            SoReturnPrestockDetailDTO.Add detail = new SoReturnPrestockDetailDTO.Add();
            detail.setSkuId(d.getSkuId());
            detail.setSkuNo(d.getSkuNo());
            detail.setProductName(skuVO.getSkuName());
            detail.setProductImageUrl(skuVO.getSkuImagesUrl());
            detail.setEan(skuVO.getEan());
            detail.setReturnQty(d.getRealQty());
            detail.setReceiveQty(d.getReceiveQty());
            detail.setRemark(d.getRemark());
            return detail;
        }).collect(Collectors.toList());
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
            String thirdCode, String warehouseId, String soReturnCode, List<SoReturnPrestockDetailDTO.Add> detailList) {
        if (CharSequenceUtil.isBlank(warehouseId)) {
            throw new ServiceException("仓库不能为空");
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
        prestockAdd.setThirdCode(CharSequenceUtil.emptyToDefault(thirdCode, ""));
        prestockAdd.setRemark(CharSequenceUtil.isNotBlank(soReturnCode) ? "原退货订单号：" + soReturnCode : "");
        prestockAdd.setDetailList(detailList);
        return prestockAdd;
    }

    /**
     * 由退货入库单表单发起创建预入库单后，同时生成并自动审核一张"其它入库单"（三无退货预入库类型），
     * 使预入库真正增加库存；写法参照旺店通预入库范式 {@code OtherInstockServiceImpl#buildWdtPreStock}，
     * 走同一个 {@code addAndApprove} 入口完成新增、提交、审核并触发库存联动，与退货入库单原有库存联动方式互不干扰。
     * 其它入库生成失败需要预入库单一并回滚，异常直接向上抛出，由调用方的事务统一处理。
     */
    private void generateOtherInstockForPrestock(String prestockId, SoReturnPrestockDTO.Add prestockAdd) {
        SoReturnPrestockEntity prestockEntity = getByIdOrThrow(prestockId);
        Map<String, SkuVO> skuVOMap = listSkuVOMap(prestockAdd.getDetailList().stream()
                .map(SoReturnPrestockDetailDTO.Add::getSkuId).collect(Collectors.toList()));
        // 按创建顺序与刚组装的入参详情行一一对应，用于回填 sourceDetailId 溯源到预入库单具体明细行
        List<SoReturnPrestockDetailEntity> persistedDetailList = soReturnPrestockDetailService.listByMainId(prestockId);
        List<SysDepartmentEntity> deptList = sysUserFeign.getDeptByIds(Collections.singletonList(WmsConstant.DEFAULT_WAREHOUSING_DEPT_ID));
        if (CollectionUtils.isEmpty(deptList)) {
            throw new ServiceException("获取不到仓储部门信息");
        }
        SysDepartmentEntity dept = deptList.get(0);

        OtherInstockEntity otherInstockEntity = new OtherInstockEntity();
        otherInstockEntity.setBillDate(prestockEntity.getReturnInstockTime().toLocalDate());
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
        List<SoReturnPrestockDetailDTO.Add> addDetailList = prestockAdd.getDetailList();
        for (int i = 0; i < addDetailList.size(); i++) {
            SoReturnPrestockDetailDTO.Add detail = addDetailList.get(i);
            SkuVO skuVO = skuVOMap.getOrDefault(detail.getSkuId(), new SkuVO());
            OtherInstockDetailEntity detailEntity = new OtherInstockDetailEntity();
            detailEntity.setSkuId(detail.getSkuId());
            detailEntity.setSkuNo(detail.getSkuNo());
            detailEntity.setActualQty(detail.getReturnQty());
            detailEntity.setUnit(skuVO.getUnitName());
            detailEntity.setRemark(detail.getRemark());
            if (i < persistedDetailList.size()) {
                detailEntity.setSourceDetailId(persistedDetailList.get(i).getId());
            }
            detailEntityList.add(detailEntity);
        }
        otherInstockEntity.setDetailEntityList(detailEntityList);
        otherInstockService.addAndApprove(otherInstockEntity, false);
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
