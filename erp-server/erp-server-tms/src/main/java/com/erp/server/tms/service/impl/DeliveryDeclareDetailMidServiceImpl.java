package com.erp.server.tms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.annotation.DistributeLocker;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.vo.PagingVO;
import com.common.core.entity.ConditionElement;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.server.rule.SpElServer;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.plm.dto.ProductDetailDTO;
import com.erp.model.plm.enums.CombinationDeclareTypeEnums;
import com.erp.model.tms.dto.AutoGenerateBillDTO;
import com.erp.model.tms.dto.CfgSettingValueDTO;
import com.erp.model.tms.dto.DeliveryDeclareDetailMidDTO;
import com.erp.model.tms.entity.CfgDeclareRuleConditionEntity;
import com.erp.model.tms.entity.CfgDeclareRuleEntity;
import com.erp.model.tms.entity.CfgSettingEntity;
import com.erp.model.tms.entity.DeliveryDeclareDetailMidEntity;
import com.erp.model.tms.enums.BillGenerateTimingEnum;
import com.erp.model.tms.enums.CfgSettingEnum;
import com.erp.model.tms.enums.DeclareStatusEnum;
import com.erp.model.tms.enums.DeliveryDeclareDetailMidGenerateStatusEnum;
import com.erp.model.wms.dto.FirstMileDeliveryDTO;
import com.erp.model.wms.dto.WmsCartonDetailDTO;
import com.erp.model.wms.entity.FirstMileDeliveryDetailEntity;
import com.erp.model.wms.entity.FirstMileDeliveryEntity;
import com.erp.model.wms.entity.SoDeliveryNoticeDetailEntity;
import com.erp.model.wms.entity.SoDeliveryNoticeEntity;
import com.erp.model.wms.enums.PackingTaskStatusEnum;
import com.erp.model.wms.enums.WmsDeclareStatusEnum;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.wms.feign.SoDeliveryNoticeFeign;
import com.erp.rpc.wms.feign.WmsFirstMileDeliveryFeign;
import com.erp.server.tms.mapper.DeliveryDeclareDetailMidMapper;
import com.erp.server.tms.service.CfgDeclareRuleConditionService;
import com.erp.server.tms.service.CfgDeclareRuleService;
import com.erp.server.tms.service.CfgSettingService;
import com.erp.server.tms.service.DeliveryDeclareDetailMidService;
import com.erp.server.tms.service.OperateLogService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.LinkedHashMap;
import java.util.stream.Collectors;

/**
 * 报关明细中间表服务实现类
 *
 * @author jack
 * @since 2026-04-27
 */
@Slf4j
@Service
public class DeliveryDeclareDetailMidServiceImpl extends SuperServiceImpl<DeliveryDeclareDetailMidMapper, DeliveryDeclareDetailMidEntity>
        implements DeliveryDeclareDetailMidService {

    private static final String RULE_TYPE_FIRST_MILE_DECLARE = "fmDeclareBill";
    private static final String RULE_TYPE_B2B_DECLARE = "b2bDeclareBill";
    private static final String CONDITION_VALUE_TYPE_STRING = "String";

    @Resource
    private OperateLogService operateLogService;
    @Resource
    private WmsFirstMileDeliveryFeign wmsFirstMileDeliveryFeign;
    @Resource
    private SoDeliveryNoticeFeign soDeliveryNoticeFeign;
    @Resource
    private PlmTaskFeign plmTaskFeign;
    @Resource
    private CfgSettingService cfgSettingService;
    @Resource
    private CfgDeclareRuleService cfgDeclareRuleService;
    @Resource
    private CfgDeclareRuleConditionService cfgDeclareRuleConditionService;
    @Resource
    private SpElServer spElServer;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BaseResultDTO.AddDTO add(DeliveryDeclareDetailMidDTO.AddDTO addDTO) {
        DeliveryDeclareDetailMidEntity entity = BeanMapperUtils.map(DeliveryDeclareDetailMidEntity.class, addDTO);
        handleData(entity);
        boolean save = super.save(entity);
        if (!save) {
            throw new ServiceException(ApiError.BILL_SAVE_FAIL, "报关明细中间表");
        }
        operateLogService.addModuleOperateLog("新增报关明细中间表",
                null, entity.getId(), "新增操作");
        return new BaseResultDTO.AddDTO(entity.getId(), entity.getId());
    }

    @Override
    @DistributeLocker(keyName = "addOrUpdateDTO.getId()")
    @Transactional(rollbackFor = Exception.class)
    public Boolean update(DeliveryDeclareDetailMidDTO.UpdateDTO addOrUpdateDTO) {
        DeliveryDeclareDetailMidEntity old = super.getById(addOrUpdateDTO.getId());
        if (Objects.isNull(old)) {
            throw new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, "报关明细中间表");
        }
        DeliveryDeclareDetailMidEntity entity = BeanMapperUtils.map(DeliveryDeclareDetailMidEntity.class, addOrUpdateDTO);
        handleData(entity);
        boolean save = super.updateById(entity);
        if (!save) {
            throw new ServiceException(ApiError.BILL_UPDATE_FAILED);
        }
        operateLogService.addModuleOperateLogByObj(old, entity, null, entity.getId(), "更新报关明细中间表");
        return Boolean.TRUE;
    }

    @Override
    public PagingVO<DeliveryDeclareDetailMidDTO.ListDTO> paging(PagingDTO<DeliveryDeclareDetailMidDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page<DeliveryDeclareDetailMidDTO.ListDTO> query = new Page<DeliveryDeclareDetailMidDTO.ListDTO>(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<DeliveryDeclareDetailMidDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if (CollUtil.isEmpty(pageData.getRecords())) {
            return new PagingVO<DeliveryDeclareDetailMidDTO.ListDTO>(pageData);
        }
        fillList(pageData.getRecords());
        return new PagingVO<DeliveryDeclareDetailMidDTO.ListDTO>(pageData);
    }

    @Override
    public List<DeliveryDeclareDetailMidDTO.TabListDTO> tabList(PermissionsDTO param) {
        DeliveryDeclareDetailMidDTO.PagingParamDTO searchParam = new DeliveryDeclareDetailMidDTO.PagingParamDTO();
        searchParam.setPermissionSql(param.getPermissionSql());
        List<DeliveryDeclareDetailMidDTO.TabListDTO> list = baseMapper.tabList(searchParam);
        List<String> statusList = Arrays.asList(
                DeliveryDeclareDetailMidGenerateStatusEnum.WAIT.getCode(),
                DeliveryDeclareDetailMidGenerateStatusEnum.FINISH.getCode()
        );
        List<String> existStatusList = list.stream()
                .map(DeliveryDeclareDetailMidDTO.TabListDTO::getTabFlag)
                .collect(Collectors.toList());
        for (String status : statusList) {
            if (!existStatusList.contains(status)) {
                list.add(new DeliveryDeclareDetailMidDTO.TabListDTO(status, 0));
            }
        }
        list.add(new DeliveryDeclareDetailMidDTO.TabListDTO("all",
                list.stream().mapToInt(DeliveryDeclareDetailMidDTO.TabListDTO::getCount).sum()));
        return list;
    }

    /**
     * 查询报关明细中间表详情
     *
     * @param id 主键id
     * @return 报关明细中间表详情
     * @throws ServiceException 数据不存在时抛出
     * @author jack
     * @date 2026-04-29
     */
    @Override
    public DeliveryDeclareDetailMidDTO.ViewDTO view(String id) {
        DeliveryDeclareDetailMidEntity entity = super.getByIdOpt(id)
                .orElseThrow(() -> new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, "报关明细中间表"));
        return BeanMapperUtils.map(DeliveryDeclareDetailMidDTO.ViewDTO.class, entity);
    }

    /**
     * 合并前预览
     *
     * @param ids 报关明细中间表id集合
     * @return 合并前预览列表
     * @throws ServiceException 校验失败时抛出
     * @author jack
     * @date 2026-04-29
     */
    @Override
    public List<DeliveryDeclareDetailMidDTO.MergePreviewDTO> mergePreview(List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            throw new ServiceException(ApiError.LOGISTICS_DECLARE_DETAIL_MID_PREVIEW_REQUIRED);
        }
        List<String> distinctIds = ids.stream()
                .filter(CharSequenceUtil::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(distinctIds)) {
            throw new ServiceException(ApiError.LOGISTICS_DECLARE_DETAIL_MID_PREVIEW_REQUIRED);
        }
        if(distinctIds.size() <= 0){
            throw new ServiceException(ApiError.LOGISTICS_DECLARE_DETAIL_MID_MERGE_MIN_COUNT_REQUIRED);
        }
        List<DeliveryDeclareDetailMidEntity> entityList = super.listByIds(distinctIds);
        if (CollectionUtils.isEmpty(entityList) || entityList.size() != distinctIds.size()) {
            throw new ServiceException(ApiError.LOGISTICS_DECLARE_DETAIL_MID_PREVIEW_NOT_FOUND);
        }
        if (entityList.stream().anyMatch(item -> !CharSequenceUtil.equals(item.getGenerateStatus(),
                DeliveryDeclareDetailMidGenerateStatusEnum.WAIT.getCode()))) {
            throw new ServiceException(ApiError.LOGISTICS_DECLARE_DETAIL_MID_PREVIEW_STATUS_LIMIT);
        }
        Set<String> sourceTypeSet = entityList.stream()
                .map(DeliveryDeclareDetailMidEntity::getSourceType)
                .filter(CharSequenceUtil::isNotBlank)
                .collect(Collectors.toSet());
        if (sourceTypeSet.size() != 1) {
            throw new ServiceException(ApiError.LOGISTICS_DECLARE_DETAIL_MID_PREVIEW_SOURCE_TYPE_CONFLICT);
        }
        Map<String, DeliveryDeclareDetailMidDTO.PreviewCountryDTO> countryMap = getPreviewCountryMap(entityList, sourceTypeSet.iterator().next());
        validatePreviewCountry(entityList, countryMap);
        validatePreviewDeclareRule(entityList, countryMap, sourceTypeSet.iterator().next());
        return entityList.stream()
                .sorted(Comparator.comparing(item -> distinctIds.indexOf(item.getId())))
                .map(item -> buildMergePreviewDTO(item, countryMap.get(item.getId())))
                .collect(Collectors.toList());
    }

    /**
     * 自动生成报关明细中间表
     *
     * @param dto 自动生成参数
     * @return 是否生成成功
     * @throws ServiceException 自动生成失败时抛出
     * @author jack
     * @date 2026-04-29
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean autoGenerateMidData(AutoGenerateBillDTO dto) {
        // 自动生成依赖来源单据、来源类型和生成时机，缺一则不触发生成。
        if (Objects.isNull(dto)
                || CharSequenceUtil.isBlank(dto.getId())
                || Objects.isNull(dto.getSourceTypeEnum())
                || Objects.isNull(dto.getBillGenerateTimingEnum())) {
            return Boolean.FALSE;
        }
        if (Boolean.TRUE.equals(dto.getCheckCfg()) && !checkAutoGenerateCfg(dto)) {
            return Boolean.FALSE;
        }
        if (SourceTypeEnum.FIRST_MILE_DELIVERY == dto.getSourceTypeEnum()) {
            return generateFirstMileMid(dto);
        }
        if (SourceTypeEnum.SO_DELIVERY_NOTICE == dto.getSourceTypeEnum()) {
            return generateSoDeliveryNoticeMid(dto);
        }
        return Boolean.FALSE;
    }

    /**
     * 校验自动生成配置
     *
     * @param dto 自动生成参数
     * @return 配置是否允许自动生成
     * @throws RuntimeException 当前方法不主动抛出业务异常
     * @author jack
     * @date 2026-04-29
     */
    private Boolean checkAutoGenerateCfg(AutoGenerateBillDTO dto) {
        CfgSettingEntity cfgSettingEntity = cfgSettingService.getByKey(CfgSettingEnum.BILL_AUTO_ADD.getCode());
        if (Objects.isNull(cfgSettingEntity) || Boolean.TRUE.equals(cfgSettingEntity.getDisabled())) {
            return Boolean.FALSE;
        }
        CfgSettingValueDTO.BillAutoAddDTO cfg = BeanUtil.toBean(cfgSettingEntity.getDataJson(), CfgSettingValueDTO.BillAutoAddDTO.class);
        if (Objects.isNull(cfg)) {
            return Boolean.FALSE;
        }
        if (SourceTypeEnum.FIRST_MILE_DELIVERY == dto.getSourceTypeEnum()) {
            return Boolean.TRUE.equals(cfg.getIsAutoFirstMileDeclare())
                    && CharSequenceUtil.equals(cfg.getFirstMileDeclareGenerateTiming(), dto.getBillGenerateTimingEnum().getCode());
        }
        if (SourceTypeEnum.SO_DELIVERY_NOTICE == dto.getSourceTypeEnum()) {
            return Boolean.TRUE.equals(cfg.getIsAutoB2BDeclare())
                    && CharSequenceUtil.equals(cfg.getB2BDeclareGenerateTiming(), dto.getBillGenerateTimingEnum().getCode());
        }
        return Boolean.FALSE;
    }

    /**
     * 生成头程报关明细中间表数据
     *
     * @param dto 自动生成参数
     * @return 是否生成成功
     * @throws ServiceException 来源单据或商品申报信息异常时抛出
     * @author jack
     * @date 2026-04-29
     */
    private Boolean generateFirstMileMid(AutoGenerateBillDTO dto) {
        // 跨服务查询头程发货单，当前事务只负责保存本模块中间表数据。
        List<FirstMileDeliveryEntity> headerList = wmsFirstMileDeliveryFeign.listByIds(Collections.singletonList(dto.getId()));
        if (CollectionUtils.isEmpty(headerList)) {
            throw new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, "头程发货单");
        }
        FirstMileDeliveryEntity header = headerList.get(0);
        if (Boolean.TRUE.equals(header.getInvalidStatus())) {
            return Boolean.FALSE;
        }
        if (dto.getBillGenerateTimingEnum() == BillGenerateTimingEnum.AFTER_APPROVE
                && !CharSequenceUtil.equals(header.getApproveStatus(), com.common.business.enums.ApproveStatusEnum.APPROVE.getCode())) {
            return Boolean.FALSE;
        }
        if (!Objects.equals(header.getDeclareStatus(), WmsDeclareStatusEnum.WAIT)) {
            return Boolean.FALSE;
        }
        if (!CharSequenceUtil.equals(header.getPackingStatus(), PackingTaskStatusEnum.PACKED.getCode())) {
            return Boolean.FALSE;
        }
        // 报关中间表按已装箱明细生成，未装箱数据不进入生成流程。
        List<FirstMileDeliveryDetailEntity> detailList = wmsFirstMileDeliveryFeign.listDetailByMainIds(Collections.singletonList(header.getId()));
        if (CollectionUtils.isEmpty(detailList)) {
            return Boolean.FALSE;
        }
        List<WmsCartonDetailDTO.ListPackingDetailDTO> packingDetailList = wmsFirstMileDeliveryFeign.listDeclarePackingDetail(Collections.singletonList(header.getId()));
        if (CollectionUtils.isEmpty(packingDetailList)) {
            return Boolean.FALSE;
        }
        List<FirstMileDeliveryDTO.BusinessDTO> businessList = wmsFirstMileDeliveryFeign.getBusinessCodeByIds(Collections.singletonList(header.getId()));
        Map<String, FirstMileDeliveryDTO.BusinessDTO> businessMap = CollectionUtils.isEmpty(businessList)
                ? Collections.<String, FirstMileDeliveryDTO.BusinessDTO>emptyMap()
                : businessList.stream()
                .filter(item -> CharSequenceUtil.isNotBlank(item.getDetailId()))
                .collect(Collectors.toMap(FirstMileDeliveryDTO.BusinessDTO::getDetailId, item -> item, (o1, o2) -> o1));
        Map<String, List<FirstMileDeliveryDetailEntity>> detailMap = detailList.stream()
                .collect(Collectors.groupingBy(FirstMileDeliveryDetailEntity::getSkuId));
        Map<String, ProductDetailDTO.ProductLogisticDTO> logisticsMap = getProductLogisticsMap(detailList.stream()
                .map(FirstMileDeliveryDetailEntity::getSkuId)
                .filter(CharSequenceUtil::isNotBlank)
                .distinct()
                .collect(Collectors.toList()));
        Set<String> existingKeys = getExistingKeys(header.getId(), SourceTypeEnum.FIRST_MILE_DELIVERY.getCode());
        Map<String, DeliveryDeclareDetailMidEntity> pendingMap = new HashMap<String, DeliveryDeclareDetailMidEntity>();
        for (WmsCartonDetailDTO.ListPackingDetailDTO packingDetail : packingDetailList) {
            FirstMileDeliveryDetailEntity detail = matchFirstMileDetail(detailMap.get(packingDetail.getSkuId()), packingDetail);
            if (Objects.isNull(detail)) {
                throw new ServiceException(ApiError.LOGISTICS_DECLARE_DETAIL_MID_DETAIL_MATCH_FAILED,
                        "头程发货", packingDetail.getSkuNo());
            }
            ProductDetailDTO.ProductLogisticDTO productLogisticDTO = logisticsMap.get(detail.getSkuId());
            if (Objects.isNull(productLogisticDTO)) {
                throw new ServiceException(ApiError.LOGISTICS_PRODUCT_LOGISTIC_NOT_FOUND, detail.getSkuNo());
            }
            validateProductLogistic(productLogisticDTO, detail.getSkuNo());
            FirstMileDeliveryDTO.BusinessDTO businessDTO = businessMap.get(detail.getId());
            buildRows(pendingMap, existingKeys,
                    SourceTypeEnum.FIRST_MILE_DELIVERY.getCode(),
                    header.getId(),
                    header.getCode(),
                    detail.getId(),
                    CharSequenceUtil.blankToDefault(detail.getSourceDetailId(), header.getSourceId()),
                    Objects.isNull(businessDTO) ? "" : businessDTO.getBusinessCode(),
                    CharSequenceUtil.blankToDefault(header.getSourceType(), header.getDemandType()),
                    "",
                    packingDetail,
                    detail.getSkuNo(),
                    productLogisticDTO,
                    header.getDeliveryWarehouseId(),
                    header.getDeliveryWarehouseName(),
                    header.getDestWarehouseId(),
                    header.getDestWarehouseName(),
                    header.getTransferWarehouseIds(),
                    header.getInventoryOrgId(),
                    header.getInventoryOrgName());
        }
        if (!pendingMap.isEmpty()) {
            super.saveBatch(new ArrayList<DeliveryDeclareDetailMidEntity>(pendingMap.values()));
        }
        return Boolean.TRUE;
    }

    /**
     * 生成发货通知报关明细中间表数据
     *
     * @param dto 自动生成参数
     * @return 是否生成成功
     * @throws ServiceException 来源单据或商品申报信息异常时抛出
     * @author jack
     * @date 2026-04-29
     */
    private Boolean generateSoDeliveryNoticeMid(AutoGenerateBillDTO dto) {
        // 跨服务查询发货通知单，当前事务只负责保存本模块中间表数据。
        List<SoDeliveryNoticeEntity> headerList = soDeliveryNoticeFeign.listByIds(Collections.singletonList(dto.getId()));
        if (CollectionUtils.isEmpty(headerList)) {
            throw new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, "发货通知单");
        }
        SoDeliveryNoticeEntity header = headerList.get(0);
        if (Boolean.TRUE.equals(header.getInvalidStatus())) {
            return Boolean.FALSE;
        }
        if (dto.getBillGenerateTimingEnum() == BillGenerateTimingEnum.AFTER_APPROVE
                && !CharSequenceUtil.equals(header.getApproveStatus(), com.common.business.enums.ApproveStatusEnum.APPROVE.getCode())) {
            return Boolean.FALSE;
        }
        if (!CharSequenceUtil.equals(header.getPackingStatus(), PackingTaskStatusEnum.PACKED.getCode())) {
            return Boolean.FALSE;
        }
        // 报关中间表按已装箱明细生成，未装箱数据不进入生成流程。
        List<SoDeliveryNoticeDetailEntity> detailList = soDeliveryNoticeFeign.listDetailByMainIds(Collections.singletonList(header.getId()));
        if (CollectionUtils.isEmpty(detailList)) {
            return Boolean.FALSE;
        }
        List<WmsCartonDetailDTO.ListPackingDetailDTO> packingDetailList = soDeliveryNoticeFeign.listDeclarePackingDetail(Collections.singletonList(header.getId()));
        if (CollectionUtils.isEmpty(packingDetailList)) {
            return Boolean.FALSE;
        }
        Map<String, List<SoDeliveryNoticeDetailEntity>> detailMap = detailList.stream()
                .collect(Collectors.groupingBy(SoDeliveryNoticeDetailEntity::getSkuId));
        Map<String, ProductDetailDTO.ProductLogisticDTO> logisticsMap = getProductLogisticsMap(detailList.stream()
                .map(SoDeliveryNoticeDetailEntity::getSkuId)
                .filter(CharSequenceUtil::isNotBlank)
                .distinct()
                .collect(Collectors.toList()));
        Set<String> existingKeys = getExistingKeys(header.getId(), SourceTypeEnum.SO_DELIVERY_NOTICE.getCode());
        Map<String, DeliveryDeclareDetailMidEntity> pendingMap = new HashMap<String, DeliveryDeclareDetailMidEntity>();
        for (WmsCartonDetailDTO.ListPackingDetailDTO packingDetail : packingDetailList) {
            SoDeliveryNoticeDetailEntity detail = matchSoDeliveryNoticeDetail(detailMap.get(packingDetail.getSkuId()), packingDetail);
            if (Objects.isNull(detail)) {
                throw new ServiceException(ApiError.LOGISTICS_DECLARE_DETAIL_MID_DETAIL_MATCH_FAILED,
                        "发货通知", packingDetail.getSkuNo());
            }
            ProductDetailDTO.ProductLogisticDTO productLogisticDTO = logisticsMap.get(detail.getSkuId());
            if (Objects.isNull(productLogisticDTO)) {
                throw new ServiceException(ApiError.LOGISTICS_PRODUCT_LOGISTIC_NOT_FOUND, detail.getSkuNo());
            }
            validateProductLogistic(productLogisticDTO, detail.getSkuNo());
            buildRows(pendingMap, existingKeys,
                    SourceTypeEnum.SO_DELIVERY_NOTICE.getCode(),
                    header.getId(),
                    header.getCode(),
                    detail.getId(),
                    header.getSourceId(),
                    header.getSourceCode(),
                    header.getSourceType(),
                    "",
                    packingDetail,
                    detail.getSkuNo(),
                    productLogisticDTO,
                    header.getWarehouseId(),
                    header.getWarehouseName(),
                    "",
                    "",
                    header.getTransferWarehouseIds(),
                    header.getSalesOrgId(),
                    header.getSalesOrgName());
        }
        if (!pendingMap.isEmpty()) {
            super.saveBatch(new ArrayList<DeliveryDeclareDetailMidEntity>(pendingMap.values()));
        }
        return Boolean.TRUE;
    }

    /**
     * 构建报关明细中间表行
     *
     * @param pendingMap 待保存数据
     * @param existingKeys 已存在唯一键
     * @param sourceType 来源类型
     * @param sourceId 来源单据id
     * @param sourceCode 来源单号
     * @param sourceDetailId 来源明细id
     * @param businessId 业务单据id
     * @param businessCode 业务单号
     * @param businessType 业务类型
     * @param contractNo 合同协议号
     * @param packingDetail 装箱明细
     * @param originSkuNo 原始商品编码
     * @param productLogisticDTO 商品物流信息
     * @param fromWarehouseId 发货仓id
     * @param fromWarehouseName 发货仓名称
     * @param destWarehouseId 目的仓id
     * @param destWarehouseName 目的仓名称
     * @param transferWarehouseIds 中转仓id
     * @param salesOrgId 销售组织id
     * @param salesOrgName 销售组织名称
     * @throws ServiceException 组合品拆分信息异常时抛出
     * @author jack
     * @date 2026-04-29
     */
    private void buildRows(Map<String, DeliveryDeclareDetailMidEntity> pendingMap,
                           Set<String> existingKeys,
                           String sourceType,
                           String sourceId,
                           String sourceCode,
                           String sourceDetailId,
                           String businessId,
                           String businessCode,
                           String businessType,
                           String contractNo,
                           WmsCartonDetailDTO.ListPackingDetailDTO packingDetail,
                           String originSkuNo,
                           ProductDetailDTO.ProductLogisticDTO productLogisticDTO,
                           String fromWarehouseId,
                           String fromWarehouseName,
                           String destWarehouseId,
                           String destWarehouseName,
                           String transferWarehouseIds,
                           String salesOrgId,
                           String salesOrgName) {
        // 组合品按申报类型拆分为子件申报，普通商品直接按装箱数量申报。
        if (isSplitCombination(productLogisticDTO)) {
            if (CollectionUtils.isEmpty(productLogisticDTO.getChildList())) {
                throw new ServiceException(ApiError.LOGISTICS_COMBO_DECLARE_CHILD_EMPTY, originSkuNo);
            }
            for (ProductDetailDTO.ProductLogisticDTO child : productLogisticDTO.getChildList()) {
                if (Objects.isNull(child.getChildQty())) {
                    throw new ServiceException(ApiError.LOGISTICS_COMBO_DECLARE_CHILD_QTY_EMPTY, originSkuNo);
                }
                validateProductLogistic(child, child.getSkuNo());
                // 子件申报数量=装箱数量*BOM子件用量。
                Integer qty = safePackQty(packingDetail.getPackQty()) * child.getChildQty();
                addMidRow(pendingMap, existingKeys, sourceType, sourceId, sourceCode, sourceDetailId, businessId,
                        businessCode, businessType, contractNo, stringifyBoxNo(packingDetail.getBoxNo()), child, qty,
                        fromWarehouseId, fromWarehouseName, destWarehouseId, destWarehouseName,
                        transferWarehouseIds, salesOrgId, salesOrgName, originSkuNo);
            }
            return;
        }
        addMidRow(pendingMap, existingKeys, sourceType, sourceId, sourceCode, sourceDetailId, businessId,
                businessCode, businessType, contractNo, stringifyBoxNo(packingDetail.getBoxNo()), productLogisticDTO,
                safePackQty(packingDetail.getPackQty()), fromWarehouseId, fromWarehouseName, destWarehouseId,
                destWarehouseName, transferWarehouseIds, salesOrgId, salesOrgName, "");
    }

    /**
     * 新增报关明细中间表行
     *
     * @param pendingMap 待保存数据
     * @param existingKeys 已存在唯一键
     * @param sourceType 来源类型
     * @param sourceId 来源单据id
     * @param sourceCode 来源单号
     * @param sourceDetailId 来源明细id
     * @param businessId 业务单据id
     * @param businessCode 业务单号
     * @param businessType 业务类型
     * @param contractNo 合同协议号
     * @param boxNo 箱号
     * @param productLogisticDTO 商品物流信息
     * @param qty 申报数量
     * @param fromWarehouseId 发货仓id
     * @param fromWarehouseName 发货仓名称
     * @param destWarehouseId 目的仓id
     * @param destWarehouseName 目的仓名称
     * @param transferWarehouseIds 中转仓id
     * @param salesOrgId 销售组织id
     * @param salesOrgName 销售组织名称
     * @param comboSkuNo 组合品商品编码
     * @throws RuntimeException 当前方法不主动抛出业务异常
     * @author jack
     * @date 2026-04-29
     */
    private void addMidRow(Map<String, DeliveryDeclareDetailMidEntity> pendingMap,
                           Set<String> existingKeys,
                           String sourceType,
                           String sourceId,
                           String sourceCode,
                           String sourceDetailId,
                           String businessId,
                           String businessCode,
                           String businessType,
                           String contractNo,
                           String boxNo,
                           ProductDetailDTO.ProductLogisticDTO productLogisticDTO,
                           Integer qty,
                           String fromWarehouseId,
                           String fromWarehouseName,
                           String destWarehouseId,
                           String destWarehouseName,
                           String transferWarehouseIds,
                           String salesOrgId,
                           String salesOrgName,
                           String comboSkuNo) {
        String key = buildUniqueKey(sourceId, sourceDetailId, boxNo, productLogisticDTO.getSkuId());
        if (existingKeys.contains(key)) {
            return;
        }
        DeliveryDeclareDetailMidEntity existEntity = pendingMap.get(key);
        if (Objects.nonNull(existEntity)) {
            // 同一来源明细、箱号和商品在本批次内合并数量，避免重复行。
            existEntity.setQty(Objects.isNull(existEntity.getQty()) ? qty : existEntity.getQty() + qty);
            return;
        }
        DeliveryDeclareDetailMidEntity entity = new DeliveryDeclareDetailMidEntity();
        entity.setDeclareStatus(DeclareStatusEnum.WAIT.getCode());
        entity.setGenerateStatus(DeliveryDeclareDetailMidGenerateStatusEnum.WAIT.getCode());
        entity.setSourceId(sourceId);
        entity.setSourceCode(sourceCode);
        entity.setSourceType(sourceType);
        entity.setSourceDetailId(sourceDetailId);
        entity.setBusinessId(CharSequenceUtil.blankToDefault(businessId, ""));
        entity.setBusinessCode(CharSequenceUtil.blankToDefault(businessCode, ""));
        entity.setBusinessType(CharSequenceUtil.blankToDefault(businessType, ""));
        entity.setContractNo(CharSequenceUtil.blankToDefault(contractNo, ""));
        entity.setSkuId(productLogisticDTO.getSkuId());
        entity.setSkuNo(productLogisticDTO.getSkuNo());
        entity.setComboSkuNo(CharSequenceUtil.blankToDefault(comboSkuNo, ""));
        entity.setCurrency(CharSequenceUtil.blankToDefault(productLogisticDTO.getDeclareCurrency(), ""));
        entity.setCurrencySymbol(CharSequenceUtil.blankToDefault(productLogisticDTO.getDeclareCurrencySymbol(), ""));
        entity.setDeclareId("");
        entity.setDeclareCode("");
        entity.setDeclareDetailId("");
        entity.setBoxNo(CharSequenceUtil.blankToDefault(boxNo, ""));
        entity.setHsCode(CharSequenceUtil.blankToDefault(productLogisticDTO.getCustomsCode(), ""));
        entity.setProductNameCn(CharSequenceUtil.blankToDefault(productLogisticDTO.getDeclareChineseName(), ""));
        entity.setDeclareElement(CharSequenceUtil.blankToDefault(productLogisticDTO.getDeclareElement(), ""));
        entity.setUnit(CharSequenceUtil.blankToDefault(productLogisticDTO.getDeclareUnit(), ""));
        entity.setUnitPrice(Objects.isNull(productLogisticDTO.getPrice()) ? BigDecimal.ZERO : productLogisticDTO.getPrice());
        entity.setQty(Objects.isNull(qty) ? 0 : qty);
        entity.setFromWarehouseId(CharSequenceUtil.blankToDefault(fromWarehouseId, ""));
        entity.setFromWarehouseName(CharSequenceUtil.blankToDefault(fromWarehouseName, ""));
        entity.setDestWarehouseId(CharSequenceUtil.blankToDefault(destWarehouseId, ""));
        entity.setDestWarehouseName(CharSequenceUtil.blankToDefault(destWarehouseName, ""));
        entity.setTransferWarehouseIds(CharSequenceUtil.blankToDefault(transferWarehouseIds, ""));
        entity.setSalesOrgId(CharSequenceUtil.blankToDefault(salesOrgId, ""));
        entity.setSalesOrgName(CharSequenceUtil.blankToDefault(salesOrgName, ""));
        existingKeys.add(key);
        pendingMap.put(key, entity);
    }

    /**
     * 查询已存在中间表唯一键
     *
     * @param sourceId 来源单据id
     * @param sourceType 来源类型
     * @return 已存在唯一键集合
     * @throws RuntimeException 当前方法不主动抛出业务异常
     * @author jack
     * @date 2026-04-29
     */
    private Set<String> getExistingKeys(String sourceId, String sourceType) {
        List<DeliveryDeclareDetailMidEntity> existList = this.lambdaQuery()
                .eq(DeliveryDeclareDetailMidEntity::getSourceId, sourceId)
                .eq(DeliveryDeclareDetailMidEntity::getSourceType, sourceType)
                .list();
        if (CollectionUtils.isEmpty(existList)) {
            return new HashSet<String>();
        }
        return existList.stream()
                .map(item -> buildUniqueKey(item.getSourceId(), item.getSourceDetailId(), item.getBoxNo(), item.getSkuId()))
                .collect(Collectors.toSet());
    }

    /**
     * 查询商品物流信息映射
     *
     * @param skuIds 商品id集合
     * @return 商品id到商品物流信息的映射
     * @throws RuntimeException 远程调用异常时抛出
     * @author jack
     * @date 2026-04-29
     */
    private Map<String, ProductDetailDTO.ProductLogisticDTO> getProductLogisticsMap(List<String> skuIds) {
        if (CollectionUtils.isEmpty(skuIds)) {
            return Collections.emptyMap();
        }
        // 从PLM批量获取最新商品物流申报信息，避免分页列表逐行远程调用。
        List<ProductDetailDTO.ProductLogisticDTO> list = plmTaskFeign.listProductLogisticsByIds(skuIds);
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyMap();
        }
        return list.stream()
                .filter(item -> CharSequenceUtil.isNotBlank(item.getSkuId()))
                .collect(Collectors.toMap(ProductDetailDTO.ProductLogisticDTO::getSkuId, item -> item, (o1, o2) -> o1));
    }

    /**
     * 匹配头程发货明细
     *
     * @param detailList 发货明细集合
     * @param packingDetail 装箱明细
     * @return 匹配到的头程发货明细
     * @throws RuntimeException 当前方法不主动抛出业务异常
     * @author jack
     * @date 2026-04-29
     */
    private FirstMileDeliveryDetailEntity matchFirstMileDetail(List<FirstMileDeliveryDetailEntity> detailList,
                                                               WmsCartonDetailDTO.ListPackingDetailDTO packingDetail) {
        if (CollectionUtils.isEmpty(detailList)) {
            return null;
        }
        for (FirstMileDeliveryDetailEntity detail : detailList) {
            if (CharSequenceUtil.isNotBlank(detail.getFnSku())
                    && CharSequenceUtil.equals(detail.getFnSku(), packingDetail.getFnSku())) {
                return detail;
            }
            if (CharSequenceUtil.isNotBlank(detail.getPlatformSkuNo())
                    && CharSequenceUtil.equals(detail.getPlatformSkuNo(), packingDetail.getFnSku())) {
                return detail;
            }
        }
        return detailList.get(0);
    }

    /**
     * 匹配发货通知明细
     *
     * @param detailList 发货通知明细集合
     * @param packingDetail 装箱明细
     * @return 匹配到的发货通知明细
     * @throws RuntimeException 当前方法不主动抛出业务异常
     * @author jack
     * @date 2026-04-29
     */
    private SoDeliveryNoticeDetailEntity matchSoDeliveryNoticeDetail(List<SoDeliveryNoticeDetailEntity> detailList,
                                                                    WmsCartonDetailDTO.ListPackingDetailDTO packingDetail) {
        if (CollectionUtils.isEmpty(detailList)) {
            return null;
        }
        for (SoDeliveryNoticeDetailEntity detail : detailList) {
            if (CharSequenceUtil.equals(detail.getSkuNo(), packingDetail.getSkuNo())) {
                return detail;
            }
        }
        return detailList.get(0);
    }

    /**
     * 判断组合品是否需要拆分申报
     *
     * @param productLogisticDTO 商品物流信息
     * @return 是否拆分申报
     * @throws RuntimeException 当前方法不主动抛出业务异常
     * @author jack
     * @date 2026-04-29
     */
    private boolean isSplitCombination(ProductDetailDTO.ProductLogisticDTO productLogisticDTO) {
        return Objects.nonNull(productLogisticDTO)
                && Boolean.TRUE.equals(productLogisticDTO.getIsCombination())
                && CharSequenceUtil.equals(productLogisticDTO.getCombinationDeclareType(), CombinationDeclareTypeEnums.SPLIT.getCode());
    }

    /**
     * 获取安全装箱数量
     *
     * @param qty 原始数量
     * @return 非空数量
     * @throws RuntimeException 当前方法不主动抛出业务异常
     * @author jack
     * @date 2026-04-29
     */
    private Integer safePackQty(Integer qty) {
        return Objects.isNull(qty) ? 0 : qty;
    }

    /**
     * 转换箱号文本
     *
     * @param boxNo 原始箱号
     * @return 箱号文本
     * @throws RuntimeException 当前方法不主动抛出业务异常
     * @author jack
     * @date 2026-04-29
     */
    private String stringifyBoxNo(Object boxNo) {
        return Objects.isNull(boxNo) ? "" : String.valueOf(boxNo);
    }

    /**
     * 构建中间表唯一键
     *
     * @param sourceId 来源单据id
     * @param sourceDetailId 来源明细id
     * @param boxNo 箱号
     * @param skuId 商品id
     * @return 唯一键
     * @throws RuntimeException 当前方法不主动抛出业务异常
     * @author jack
     * @date 2026-04-29
     */
    private String buildUniqueKey(String sourceId, String sourceDetailId, String boxNo, String skuId) {
        return CharSequenceUtil.join("|",
                CharSequenceUtil.blankToDefault(sourceId, ""),
                CharSequenceUtil.blankToDefault(sourceDetailId, ""),
                CharSequenceUtil.blankToDefault(boxNo, ""),
                CharSequenceUtil.blankToDefault(skuId, ""));
    }

    /**
     * 校验商品物流申报信息
     *
     * @param productLogisticDTO 商品物流信息
     * @param skuNo 商品编码
     * @throws ServiceException 申报信息缺失时抛出
     * @author jack
     * @date 2026-04-29
     */
    private void validateProductLogistic(ProductDetailDTO.ProductLogisticDTO productLogisticDTO, String skuNo) {
        if (Objects.isNull(productLogisticDTO)) {
            throw new ServiceException(ApiError.LOGISTICS_PRODUCT_LOGISTIC_NOT_FOUND, skuNo);
        }
        if (CharSequenceUtil.hasBlank(productLogisticDTO.getSkuId(),
                productLogisticDTO.getSkuNo(),
                productLogisticDTO.getCustomsCode(),
                productLogisticDTO.getDeclareChineseName(),
                productLogisticDTO.getDeclareElement(),
                productLogisticDTO.getDeclareUnit(),
                productLogisticDTO.getDeclareCurrency())) {
            throw new ServiceException(ApiError.LOGISTICS_PRODUCT_LOGISTIC_DECLARE_INFO_INCOMPLETE, skuNo);
        }
        if (Objects.isNull(productLogisticDTO.getPrice())) {
            throw new ServiceException(ApiError.LOGISTICS_PRODUCT_LOGISTIC_DECLARE_PRICE_REQUIRED, skuNo);
        }
    }

    /**
     * 查询合并预览国家信息
     *
     * @param entityList 报关明细中间表集合
     * @param sourceType 来源类型
     * @return 中间表id到国家信息映射
     * @throws ServiceException 上游数据缺失时抛出
     * @author jack
     * @date 2026-04-29
     */
    private Map<String, DeliveryDeclareDetailMidDTO.PreviewCountryDTO> getPreviewCountryMap(List<DeliveryDeclareDetailMidEntity> entityList,
                                                                String sourceType) {
        if (CharSequenceUtil.equals(sourceType, SourceTypeEnum.FIRST_MILE_DELIVERY.getCode())) {
            return getFirstMilePreviewCountryMap(entityList);
        }
        if (CharSequenceUtil.equals(sourceType, SourceTypeEnum.SO_DELIVERY_NOTICE.getCode())) {
            return getSoDeliveryNoticePreviewCountryMap(entityList);
        }
        throw new ServiceException(ApiError.LOGISTICS_DECLARE_DETAIL_MID_PREVIEW_SOURCE_TYPE_CONFLICT);
    }

    /**
     * 查询头程合并预览国家信息
     *
     * @param entityList 报关明细中间表集合
     * @return 中间表id到国家信息映射
     * @throws ServiceException 头程发货单缺失时抛出
     * @author jack
     * @date 2026-04-29
     */
    private Map<String, DeliveryDeclareDetailMidDTO.PreviewCountryDTO> getFirstMilePreviewCountryMap(List<DeliveryDeclareDetailMidEntity> entityList) {
        List<String> sourceIds = entityList.stream()
                .map(DeliveryDeclareDetailMidEntity::getSourceId)
                .filter(CharSequenceUtil::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        List<FirstMileDeliveryEntity> headerList = wmsFirstMileDeliveryFeign.listByIds(sourceIds);
        Map<String, FirstMileDeliveryEntity> headerMap = CollectionUtils.isEmpty(headerList)
                ? Collections.<String, FirstMileDeliveryEntity>emptyMap()
                : headerList.stream()
                .filter(item -> CharSequenceUtil.isNotBlank(item.getId()))
                .collect(Collectors.toMap(FirstMileDeliveryEntity::getId, item -> item, (o1, o2) -> o1));
        Map<String, DeliveryDeclareDetailMidDTO.PreviewCountryDTO> resultMap = new HashMap<String, DeliveryDeclareDetailMidDTO.PreviewCountryDTO>();
        for (DeliveryDeclareDetailMidEntity entity : entityList) {
            FirstMileDeliveryEntity header = headerMap.get(entity.getSourceId());
            if (Objects.isNull(header)) {
                throw new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, "头程发货单");
            }
            resultMap.put(entity.getId(), new DeliveryDeclareDetailMidDTO.PreviewCountryDTO(header.getCountryId(), header.getCountryName()));
        }
        return resultMap;
    }

    /**
     * 查询B2B发货通知合并预览国家信息
     *
     * @param entityList 报关明细中间表集合
     * @return 中间表id到国家信息映射
     * @throws ServiceException 发货通知明细缺失时抛出
     * @author jack
     * @date 2026-04-29
     */
    private Map<String, DeliveryDeclareDetailMidDTO.PreviewCountryDTO> getSoDeliveryNoticePreviewCountryMap(List<DeliveryDeclareDetailMidEntity> entityList) {
        List<String> sourceIds = entityList.stream()
                .map(DeliveryDeclareDetailMidEntity::getSourceId)
                .filter(CharSequenceUtil::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        List<SoDeliveryNoticeDetailEntity> detailList = soDeliveryNoticeFeign.listDetailByMainIds(sourceIds);
        Map<String, SoDeliveryNoticeDetailEntity> detailMap = CollectionUtils.isEmpty(detailList)
                ? Collections.<String, SoDeliveryNoticeDetailEntity>emptyMap()
                : detailList.stream()
                .filter(item -> CharSequenceUtil.isNotBlank(item.getId()))
                .collect(Collectors.toMap(SoDeliveryNoticeDetailEntity::getId, item -> item, (o1, o2) -> o1));
        Map<String, DeliveryDeclareDetailMidDTO.PreviewCountryDTO> resultMap = new HashMap<String, DeliveryDeclareDetailMidDTO.PreviewCountryDTO>();
        for (DeliveryDeclareDetailMidEntity entity : entityList) {
            SoDeliveryNoticeDetailEntity detail = detailMap.get(entity.getSourceDetailId());
            if (Objects.isNull(detail)) {
                throw new ServiceException(ApiError.BILL_DETAIL_NOT_FOUND, "发货通知单");
            }
            resultMap.put(entity.getId(), new DeliveryDeclareDetailMidDTO.PreviewCountryDTO(detail.getToCountry(), detail.getToCountry()));
        }
        return resultMap;
    }

    /**
     * 校验预览国家一致
     *
     * @param entityList 报关明细中间表集合
     * @param countryMap 国家映射
     * @throws ServiceException 国家为空或不一致时抛出
     * @author jack
     * @date 2026-04-29
     */
    private void validatePreviewCountry(List<DeliveryDeclareDetailMidEntity> entityList,
                                        Map<String, DeliveryDeclareDetailMidDTO.PreviewCountryDTO> countryMap) {
        Set<String> countrySet = new HashSet<String>();
        for (DeliveryDeclareDetailMidEntity entity : entityList) {
            DeliveryDeclareDetailMidDTO.PreviewCountryDTO countryDTO = countryMap.get(entity.getId());
            if (Objects.isNull(countryDTO) || CharSequenceUtil.isBlank(countryDTO.getCountryId())) {
                throw new ServiceException(ApiError.LOGISTICS_DECLARE_DETAIL_MID_PREVIEW_COUNTRY_CONFLICT);
            }
            countrySet.add(countryDTO.getCountryId());
        }
        if (countrySet.size() > 1) {
            throw new ServiceException(ApiError.LOGISTICS_DECLARE_DETAIL_MID_PREVIEW_COUNTRY_CONFLICT);
        }
    }

    /**
     * 校验预览命中的报关规则发货人类型一致
     *
     * @param entityList 报关明细中间表集合
     * @param countryMap 国家映射
     * @param sourceType 来源类型
     * @throws ServiceException 规则未命中或发货人类型不一致时抛出
     * @author jack
     * @date 2026-04-29
     */
    private void validatePreviewDeclareRule(List<DeliveryDeclareDetailMidEntity> entityList,
                                            Map<String, DeliveryDeclareDetailMidDTO.PreviewCountryDTO> countryMap,
                                            String sourceType) {
        String ruleType = getDeclareRuleType(sourceType);
        List<CfgDeclareRuleEntity> ruleList = cfgDeclareRuleService.lambdaQuery()
                .eq(CfgDeclareRuleEntity::getRuleType, ruleType)
                .eq(CfgDeclareRuleEntity::getDisabled, Boolean.FALSE)
                .list();
        if (CollectionUtils.isEmpty(ruleList)) {
            throw new ServiceException(ApiError.LOGISTICS_DECLARE_DETAIL_MID_PREVIEW_RULE_NOT_FOUND);
        }
        List<String> ruleIds = ruleList.stream().map(CfgDeclareRuleEntity::getId).collect(Collectors.toList());
        Map<String, List<CfgDeclareRuleConditionEntity>> conditionMap = cfgDeclareRuleConditionService.lambdaQuery()
                .in(CfgDeclareRuleConditionEntity::getRuleId, ruleIds)
                .orderByAsc(CfgDeclareRuleConditionEntity::getIndex)
                .list()
                .stream()
                .collect(Collectors.groupingBy(CfgDeclareRuleConditionEntity::getRuleId, LinkedHashMap::new, Collectors.toList()));
        Set<String> senderTypeSet = new HashSet<String>();
        for (DeliveryDeclareDetailMidEntity entity : entityList) {
            Set<String> matchedSenderTypes = matchPreviewSenderTypes(ruleList, conditionMap, entity, countryMap.get(entity.getId()));
            if (CollectionUtils.isEmpty(matchedSenderTypes)) {
                throw new ServiceException(ApiError.LOGISTICS_DECLARE_DETAIL_MID_PREVIEW_RULE_NOT_FOUND);
            }
            senderTypeSet.addAll(matchedSenderTypes);
        }
        if (senderTypeSet.size() > 1) {
            throw new ServiceException(ApiError.LOGISTICS_DECLARE_DETAIL_MID_PREVIEW_SENDER_TYPE_CONFLICT);
        }
    }

    /**
     * 匹配预览行发货人类型
     *
     * @param ruleList 规则集合
     * @param conditionMap 规则条件映射
     * @param entity 报关明细中间表
     * @param countryDTO 国家信息
     * @return 命中的发货人类型集合
     * @throws RuntimeException 当前方法不主动抛出业务异常
     * @author jack
     * @date 2026-04-29
     */
    private Set<String> matchPreviewSenderTypes(List<CfgDeclareRuleEntity> ruleList,
                                                Map<String, List<CfgDeclareRuleConditionEntity>> conditionMap,
                                                DeliveryDeclareDetailMidEntity entity,
                                                DeliveryDeclareDetailMidDTO.PreviewCountryDTO countryDTO) {
        Set<String> senderTypeSet = new HashSet<String>();
        Map<String, Object> ruleData = buildPreviewRuleData(entity, countryDTO);
        for (CfgDeclareRuleEntity rule : ruleList) {
            List<CfgDeclareRuleConditionEntity> conditionList = conditionMap.get(rule.getId());
            if (CollectionUtils.isEmpty(conditionList)) {
                continue;
            }
            List<ConditionElement> conditionElementList = conditionList.stream()
                    .sorted(Comparator.comparing(CfgDeclareRuleConditionEntity::getIndex,
                            Comparator.nullsLast(Integer::compareTo)))
                    .map(this::buildConditionElement)
                    .collect(Collectors.toList());
            if (Boolean.TRUE.equals(spElServer.matchExpressionByConditionList(conditionElementList, ruleData, ""))) {
                senderTypeSet.add(rule.getSenderType());
            }
        }
        return senderTypeSet;
    }

    /**
     * 构建报关规则匹配参数
     *
     * @param entity 报关明细中间表
     * @param countryDTO 国家信息
     * @return 规则匹配参数
     * @throws RuntimeException 当前方法不主动抛出业务异常
     * @author jack
     * @date 2026-04-29
     */
    private Map<String, Object> buildPreviewRuleData(DeliveryDeclareDetailMidEntity entity,
                                                     DeliveryDeclareDetailMidDTO.PreviewCountryDTO countryDTO) {
        Map<String, Object> data = new HashMap<String, Object>();
        String countryId = Objects.isNull(countryDTO) ? "" : CharSequenceUtil.blankToDefault(countryDTO.getCountryId(), "");
        data.put("country", countryId);
        data.put("countryId", countryId);
        data.put("countryCode", countryId);
        data.put("destCountry", countryId);
        data.put("destinationCountry", countryId);
        data.put("receiveCountry", countryId);
        data.put("fromWarehouseId", CharSequenceUtil.blankToDefault(entity.getFromWarehouseId(), ""));
        data.put("deliveryWarehouseId", CharSequenceUtil.blankToDefault(entity.getFromWarehouseId(), ""));
        data.put("warehouseId", CharSequenceUtil.blankToDefault(entity.getFromWarehouseId(), ""));
        data.put("destWarehouseId", CharSequenceUtil.blankToDefault(entity.getDestWarehouseId(), ""));
        data.put("toWarehouseId", CharSequenceUtil.blankToDefault(entity.getDestWarehouseId(), ""));
        data.put("transferWarehouseIds", CharSequenceUtil.blankToDefault(entity.getTransferWarehouseIds(), ""));
        data.put("salesOrgId", CharSequenceUtil.blankToDefault(entity.getSalesOrgId(), ""));
        data.put("detailList", Collections.singletonList(new HashMap<String, Object>(data)));
        return data;
    }

    /**
     * 构建规则条件元素
     *
     * @param condition 规则条件
     * @return 规则条件元素
     * @throws RuntimeException 当前方法不主动抛出业务异常
     * @author jack
     * @date 2026-04-29
     */
    private ConditionElement buildConditionElement(CfgDeclareRuleConditionEntity condition) {
        ConditionElement element = new ConditionElement();
        element.setLeftBracket(condition.getLeftBracket());
        element.setField(condition.getField());
        element.setCompare(condition.getCompare());
        element.setValue(condition.getValue());
        element.setRightBracket(condition.getRightBracket());
        element.setLogic(condition.getLogic());
        element.setValueType(CONDITION_VALUE_TYPE_STRING);
        return element;
    }

    /**
     * 获取报关规则类型
     *
     * @param sourceType 来源类型
     * @return 报关规则类型
     * @throws ServiceException 来源类型不支持时抛出
     * @author jack
     * @date 2026-04-29
     */
    private String getDeclareRuleType(String sourceType) {
        if (CharSequenceUtil.equals(sourceType, SourceTypeEnum.FIRST_MILE_DELIVERY.getCode())) {
            return RULE_TYPE_FIRST_MILE_DECLARE;
        }
        if (CharSequenceUtil.equals(sourceType, SourceTypeEnum.SO_DELIVERY_NOTICE.getCode())) {
            return RULE_TYPE_B2B_DECLARE;
        }
        throw new ServiceException(ApiError.LOGISTICS_DECLARE_DETAIL_MID_PREVIEW_SOURCE_TYPE_CONFLICT);
    }

    /**
     * 构建合并前预览返回行
     *
     * @param entity 报关明细中间表
     * @param countryDTO 国家信息
     * @return 合并前预览返回行
     * @throws RuntimeException 当前方法不主动抛出业务异常
     * @author jack
     * @date 2026-04-29
     */
    private DeliveryDeclareDetailMidDTO.MergePreviewDTO buildMergePreviewDTO(DeliveryDeclareDetailMidEntity entity,
                                                                             DeliveryDeclareDetailMidDTO.PreviewCountryDTO countryDTO) {
        DeliveryDeclareDetailMidDTO.MergePreviewDTO dto = new DeliveryDeclareDetailMidDTO.MergePreviewDTO();
        dto.setId(entity.getId());
        dto.setSourceId(entity.getSourceId());
        dto.setSourceCode(entity.getSourceCode());
        dto.setSourceType(entity.getSourceType());
        dto.setBusinessCode(entity.getBusinessCode());
        dto.setBoxNo(entity.getBoxNo());
        dto.setSkuNo(entity.getSkuNo());
        dto.setHsCode(entity.getHsCode());
        dto.setProductNameCn(entity.getProductNameCn());
        dto.setDeclareElement(entity.getDeclareElement());
        dto.setUnit(entity.getUnit());
        dto.setUnitPrice(entity.getUnitPrice());
        dto.setQty(entity.getQty());
        dto.setCurrency(entity.getCurrency());
        if (Objects.nonNull(countryDTO)) {
            dto.setCountryId(countryDTO.getCountryId());
            dto.setCountryName(countryDTO.getCountryName());
        }
        return dto;
    }

    /**
     * 处理中间表默认数据
     *
     * @param entity 报关明细中间表实体
     * @throws RuntimeException 当前方法不主动抛出业务异常
     * @author jack
     * @date 2026-04-29
     */
    private void handleData(DeliveryDeclareDetailMidEntity entity) {
        if (CharSequenceUtil.isBlank(entity.getDeclareStatus())) {
            entity.setDeclareStatus(DeclareStatusEnum.WAIT.getCode());
        }
        if (CharSequenceUtil.isBlank(entity.getGenerateStatus())) {
            entity.setGenerateStatus(DeliveryDeclareDetailMidGenerateStatusEnum.WAIT.getCode());
        }
        if (Objects.isNull(entity.getComboSkuNo())) {
            entity.setComboSkuNo("");
        }
    }

    /**
     * 填充分页列表展示数据
     *
     * @param list 分页列表
     * @throws RuntimeException 远程查询商品物流信息异常时抛出
     * @author jack
     * @date 2026-04-29
     */
    private void fillList(List<DeliveryDeclareDetailMidDTO.ListDTO> list) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        Map<String, ProductDetailDTO.ProductLogisticDTO> productLogisticMap = getProductLogisticsMap(list.stream()
                .map(DeliveryDeclareDetailMidDTO.ListDTO::getSkuId)
                .filter(CharSequenceUtil::isNotBlank)
                .distinct()
                .collect(Collectors.toList()));
        for (DeliveryDeclareDetailMidDTO.ListDTO data : list) {
            data.setDeclareStatusName(DeclareStatusEnum.getName(data.getDeclareStatus()));
            data.setGenerateStatusName(DeliveryDeclareDetailMidGenerateStatusEnum.getName(data.getGenerateStatus()));
            if (Objects.isNull(data.getComboSkuNo())) {
                data.setComboSkuNo("");
            }
            fillLatestProductLogistic(data, productLogisticMap.get(data.getSkuId()));
        }
    }

    /**
     * 填充最新商品物流信息
     *
     * @param data 列表数据
     * @param productLogisticDTO 商品物流信息
     * @throws RuntimeException 当前方法不主动抛出业务异常
     * @author jack
     * @date 2026-04-29
     */
    private void fillLatestProductLogistic(DeliveryDeclareDetailMidDTO.ListDTO data,
                                           ProductDetailDTO.ProductLogisticDTO productLogisticDTO) {
        if (Objects.isNull(productLogisticDTO)) {
            return;
        }
        data.setLatestHsCode(CharSequenceUtil.blankToDefault(productLogisticDTO.getCustomsCode(), ""));
        data.setLatestProductNameCn(CharSequenceUtil.blankToDefault(productLogisticDTO.getDeclareChineseName(), ""));
        data.setLatestDeclareElement(CharSequenceUtil.blankToDefault(productLogisticDTO.getDeclareElement(), ""));
        data.setLatestUnit(CharSequenceUtil.blankToDefault(productLogisticDTO.getDeclareUnit(), ""));
        data.setLatestUnitPrice(productLogisticDTO.getPrice());
    }
}
