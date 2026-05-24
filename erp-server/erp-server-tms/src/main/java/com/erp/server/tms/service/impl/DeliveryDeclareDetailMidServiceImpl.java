package com.erp.server.tms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.utils.date.DateUtil;
import com.erp.model.tms.dto.TmsDeclareBillDTO;
import com.erp.model.tms.entity.*;
import io.seata.spring.annotation.GlobalTransactional;
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
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.plm.dto.BomChildrenSkuDTO;
import com.erp.model.plm.dto.ProductDetailDTO;
import com.erp.model.tms.dto.DeliveryDeclareDetailMidDTO;
import com.erp.model.tms.enums.DeclareDeclareTypeEnum;
import com.erp.model.tms.enums.DeclareNatureLevyEnum;
import com.erp.model.tms.enums.DeclarePackTypeEnum;
import com.erp.model.tms.enums.DeclareStatusEnum;
import com.erp.model.tms.enums.DeclareSupervisionMethodEnum;
import com.erp.model.tms.enums.DeclareTransactionMethodEnum;
import com.erp.model.tms.enums.DeliveryDeclareDetailMidGenerateStatusEnum;
import com.erp.model.wms.dto.FirstMileDeliveryDTO;
import com.erp.model.wms.dto.SoDeliveryNoticeDTO;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.model.wms.enums.WmsDeclareStatusEnum;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.wms.feign.SoDeliveryNoticeFeign;
import com.erp.rpc.wms.feign.WmsFirstMileDeliveryFeign;
import com.erp.rpc.wms.feign.WmsWarehouseFeign;
import com.erp.server.tms.mapper.DeliveryDeclareDetailMidMapper;
import com.erp.server.tms.service.CfgDeclareRuleService;
import com.erp.server.tms.service.DeliveryDeclareDetailMidService;
import com.erp.server.tms.service.OperateLogService;
import com.erp.server.tms.service.TmsDeclareBillService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
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

    @Resource
    private OperateLogService operateLogService;
    @Resource
    private WmsFirstMileDeliveryFeign wmsFirstMileDeliveryFeign;
    @Resource
    private SoDeliveryNoticeFeign soDeliveryNoticeFeign;
    @Resource
    private PlmTaskFeign plmTaskFeign;
    @Resource
    private WmsWarehouseFeign wmsWarehouseFeign;
    @Resource
    @Lazy
    private TmsDeclareBillService tmsDeclareBillService;
    @Resource
    private CfgDeclareRuleService cfgDeclareRuleService;
    @Lazy
    @Resource
    private DeliveryDeclareDetailMidService self;

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
        List<DeliveryDeclareDetailMidDTO.TabListDTO> result = new ArrayList<>();
        result.add(new DeliveryDeclareDetailMidDTO.TabListDTO("all","全部",0));

        DeliveryDeclareDetailMidDTO.TabListDTO wait = list.stream().filter(e -> Objects.equals(DeliveryDeclareDetailMidGenerateStatusEnum.WAIT.getCode(), e.getTabFlag()))
                .findFirst().orElse(new DeliveryDeclareDetailMidDTO.TabListDTO(DeliveryDeclareDetailMidGenerateStatusEnum.WAIT.getCode(),DeliveryDeclareDetailMidGenerateStatusEnum.WAIT.getName(),0));
        DeliveryDeclareDetailMidDTO.TabListDTO finish = list.stream().filter(e -> Objects.equals(DeliveryDeclareDetailMidGenerateStatusEnum.FINISH.getCode(), e.getTabFlag()))
                .findFirst().orElse(new DeliveryDeclareDetailMidDTO.TabListDTO(DeliveryDeclareDetailMidGenerateStatusEnum.FINISH.getCode(),DeliveryDeclareDetailMidGenerateStatusEnum.FINISH.getName(),0));

        wait.setTabFlagName(DeliveryDeclareDetailMidGenerateStatusEnum.WAIT.getName());
        result.add(wait);
        finish.setTabFlagName(DeliveryDeclareDetailMidGenerateStatusEnum.FINISH.getName());
        result.add(finish);
        return result;
    }

    @Override
    public void exportList(DeliveryDeclareDetailMidDTO.ExportDTO param, HttpServletResponse response) {
        List<DeliveryDeclareDetailMidDTO.ListDTO> list = this.baseMapper.listExport(param);
        if(CollUtil.isEmpty(list)) {
           return;
        }
        // 数据处理
        fillList(list);

        // 导出数据
        StringBuffer sb = new StringBuffer();
        String excelPath = "excel/deliveryDeclareDetailMid.xlsx";
        String name = "报关明细中间单导出";
        String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
        sb.append(date).append(name);
        try {
            new ExcelPrintUtils().patchExport(list, response, sb.toString(), excelPath);
        } catch (Exception e) {
            throw new ServiceException(ApiError.FILE_EXPORT_FAILED);
        }
    }

    @Override
    public List<DeliveryDeclareDetailMidEntity> listByDeclareBillIdList(List<String> declareBillIdList) {
        return baseMapper.listByDeclareBillIdList(declareBillIdList);
    }

    @Override
    public List<TmsDeclareBillDTO.SourceDeliveryDetailDTO> listSourceByDeclareIdList(List<String> declareBillIdList) {
        return baseMapper.listSourceByDeclareIdList(declareBillIdList);
    }

    @Override
    public Boolean deleteDeliveryDeclareDetailMid(List<String> sourceIds) {
        if (CollUtil.isEmpty(sourceIds)) {
            return  Boolean.TRUE;
        }
        return lambdaUpdate().in(DeliveryDeclareDetailMidEntity::getSourceId,sourceIds).remove();
    }

    /**
     * 删除中间表数据。
     *
     * @param declareBillIds 报关单id集合
     * @return 是否处理成功
     */
    public Boolean removeByDeclareBillIds(List<String> declareBillIds) {
        if (CollUtil.isEmpty(declareBillIds)) {
            return Boolean.TRUE;
        }
        // 恢复生成状态，并解除与已删除报关单的关联。
        return lambdaUpdate()
                .in(DeliveryDeclareDetailMidEntity::getDeclareId, declareBillIds)
                .remove();
    }
    /**
     * 按中间表报关id恢复为待生成状态。
     *
     * @param declareBillIds 报关单id集合
     * @return 是否处理成功
     */
    @Override
    public Boolean restoreWaitGenerateByDeclareBillIds(List<String> declareBillIds) {
        if (CollUtil.isEmpty(declareBillIds)) {
            return Boolean.TRUE;
        }
        // 编辑报关单删掉来源明细时，只恢复对应中间表行，不影响其它来源行。
        return lambdaUpdate()
                .in(DeliveryDeclareDetailMidEntity::getDeclareId, declareBillIds)
                .set(DeliveryDeclareDetailMidEntity::getGenerateStatus, DeliveryDeclareDetailMidGenerateStatusEnum.WAIT.getCode())
                .set(DeliveryDeclareDetailMidEntity::getDeclareId, "")
                .set(DeliveryDeclareDetailMidEntity::getDeclareCode, "")
                .set(DeliveryDeclareDetailMidEntity::getDeclareDetailId, "")
                .update();
    }

    /**
     * 按中间表id恢复为待生成状态。
     *
     * @param ids 中间表id集合
     * @return 是否处理成功
     */
    @Override
    public Boolean restoreWaitGenerateByIds(List<String> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Boolean.TRUE;
        }
        // 编辑报关单删掉来源明细时，只恢复对应中间表行，不影响其它来源行。
        return lambdaUpdate()
                .in(DeliveryDeclareDetailMidEntity::getId, ids)
                .set(DeliveryDeclareDetailMidEntity::getGenerateStatus, DeliveryDeclareDetailMidGenerateStatusEnum.WAIT.getCode())
                .set(DeliveryDeclareDetailMidEntity::getDeclareId, "")
                .set(DeliveryDeclareDetailMidEntity::getDeclareCode, "")
                .set(DeliveryDeclareDetailMidEntity::getDeclareDetailId, "")
                .update();
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
        DeliveryDeclareDetailMidDTO.ViewDTO viewDTO = BeanMapperUtils.map(DeliveryDeclareDetailMidDTO.ViewDTO.class, entity);
        if (CharSequenceUtil.isBlank(viewDTO.getTransferWarehouseNames())) {
            viewDTO.setTransferWarehouseNames(getTransferWarehouseNames(viewDTO.getTransferWarehouseIds()));
        }
        return viewDTO;
    }

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
        List<DeliveryDeclareDetailMidEntity> entityList = super.listByIds(distinctIds);
        if (CollectionUtils.isEmpty(entityList) || entityList.size() != distinctIds.size()) {
            throw new ServiceException(ApiError.LOGISTICS_DECLARE_DETAIL_MID_PREVIEW_NOT_FOUND);
        }
        if (distinctIds.size() < 2) {
            throw new ServiceException(ApiError.LOGISTICS_DECLARE_DETAIL_MID_MERGE_MIN_COUNT_REQUIRED);
        }
        validateMergePreviewStatus(entityList);

        Set<String> sourceTypeSet = entityList.stream()
                .map(DeliveryDeclareDetailMidEntity::getSourceType)
                .filter(CharSequenceUtil::isNotBlank)
                .collect(Collectors.toSet());
        if (sourceTypeSet.size() != 1) {
            throw new ServiceException(ApiError.LOGISTICS_DECLARE_DETAIL_MID_PREVIEW_SOURCE_TYPE_CONFLICT);
        }

        Map<String, DeliveryDeclareDetailMidEntity> entityMap = entityList.stream()
                .collect(Collectors.toMap(DeliveryDeclareDetailMidEntity::getId, item -> item, (oldValue, newValue) -> oldValue));
        return distinctIds.stream()
                .map(entityMap::get)
                .filter(Objects::nonNull)
                .map(item -> BeanMapperUtils.map(DeliveryDeclareDetailMidDTO.MergePreviewDTO.class, item))
                .collect(Collectors.toList());
    }

    /**
     * 合并后预览
     *
     * @param ids 报关明细中间表id集合
     * @return 合并后预览列表
     * @throws ServiceException 校验失败时抛出
     * @author jack
     * @date 2026-05-06
     */
    @Override
    public List<TmsDeclareBillDTO.MergeDeclareBillDTO> mergeAfterPreview(List<String> ids) {
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
        List<DeliveryDeclareDetailMidEntity> entityList = super.listByIds(distinctIds);
        if (CollectionUtils.isEmpty(entityList) || entityList.size() != distinctIds.size()) {
            throw new ServiceException(ApiError.LOGISTICS_DECLARE_DETAIL_MID_PREVIEW_NOT_FOUND);
        }
        //必须勾选2条以上
        if(distinctIds.size() < 2){
            throw new ServiceException(ApiError.LOGISTICS_DECLARE_DETAIL_MID_MERGE_MIN_COUNT_REQUIRED);
        }

        validateMergePreviewStatus(entityList);

        //单据分类是相同的单据【B2B和头程】
        Set<String> sourceTypeSet = entityList.stream()
                .map(DeliveryDeclareDetailMidEntity::getSourceType)
                .filter(CharSequenceUtil::isNotBlank)
                .collect(Collectors.toSet());
        if (sourceTypeSet.size() != 1) {
            throw new ServiceException(ApiError.LOGISTICS_DECLARE_DETAIL_MID_PREVIEW_SOURCE_TYPE_CONFLICT);
        }
        String sourceType = sourceTypeSet.iterator().next();

        // 中间表入口以用户勾选的明细为准，不能再按来源单整单拉取，否则会把未勾选行带入预览。
        List<TmsDeclareBillDTO.SourceDeliveryDetailDTO> sourceDetailList = buildSelectedSourceDetailForPreview(entityList, distinctIds, sourceType);
        if (CollUtil.isEmpty(sourceDetailList)) {
            throw new ServiceException(ApiError.LOGISTICS_DECLARE_SOURCE_DETAIL_NOT_FOUND_FOR_SAVE);
        }
        validatePreviewSourceConsistent(sourceType, sourceDetailList);
        List<TmsDeclareBillDTO.MergeDeclareBillDTO> previewList = tmsDeclareBillService.autoMergeDeclareBillView(
                new TmsDeclareBillDTO.AutoMergeDeclareBillViewDTO(Boolean.TRUE, sourceDetailList));
//        validatePreviewDeclareInfo(previewList);
        return previewList;
    }

    /**
     * 自动生成报关明细中间表
     *
     * @param list 自动生成参数
     * @return 是否生成成功
     * @throws ServiceException 自动生成失败时抛出
     * @author jack
     * @date 2026-04-29
     */
    @Override
    @DistributeLocker(businessType = "tmsAutoGenerateDeclareMidData", keyName = "list.sourceId", waiteTime = 60)
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    public Boolean autoGenerateMidData(List<TmsDeclareBillDTO.SourceDeliveryDetailDTO> list) {
        // 自动生成依赖来源明细和来源类型，缺一则不触发生成。
        if (CollUtil.isEmpty(list)) {
            log.warn("自动生成报关明细中间表失败：来源明细为空");
            return Boolean.FALSE;
        }
        List<TmsDeclareBillDTO.SourceDeliveryDetailDTO> sourceDetailList = list.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        if (CollUtil.isEmpty(sourceDetailList)) {
            log.warn("自动生成报关明细中间表失败：有效来源明细为空");
            return Boolean.FALSE;
        }
        Set<String> sourceTypeSet = sourceDetailList.stream()
                .map(TmsDeclareBillDTO.SourceDeliveryDetailDTO::getSourceType)
                .filter(CharSequenceUtil::isNotBlank)
                .collect(Collectors.toSet());
        if (sourceTypeSet.size() != 1) {
            log.warn("自动生成报关明细中间表失败：来源类型不唯一，sourceTypeSet={}", sourceTypeSet);
            return Boolean.FALSE;
        }
        String sourceType = sourceTypeSet.iterator().next();
        if (!Objects.equals(sourceType, SourceTypeEnum.FIRST_MILE_DELIVERY.getCode())
                && !Objects.equals(sourceType, SourceTypeEnum.SO_DELIVERY_NOTICE.getCode())) {
            log.warn("自动生成报关明细中间表失败：来源类型不支持，sourceType={}", sourceType);
            return Boolean.FALSE;
        }
        List<TmsDeclareBillDTO.MergeDeclareBillDTO> mergeDeclareBillList = tmsDeclareBillService.autoMergeDeclareBillView(
                new TmsDeclareBillDTO.AutoMergeDeclareBillViewDTO(Boolean.FALSE, sourceDetailList));
        if (CollUtil.isEmpty(mergeDeclareBillList)) {
            log.warn("自动生成报关明细中间表失败：合并报关结果为空，sourceType={}，sourceDetailCount={}", sourceType, sourceDetailList.size());
            return Boolean.FALSE;
        }
        return self.batchAddMergeDetail(mergeDeclareBillList);
    }

    @Override
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    public Boolean batchAddMergeDetail(List<TmsDeclareBillDTO.MergeDeclareBillDTO> list) {
        List<TmsDeclareBillDTO.SourceDeliveryDetailDTO> sourceDetailList = collectSourceDetailList(list);
        if (CollUtil.isEmpty(sourceDetailList)) {
            throw new ServiceException(ApiError.LOGISTICS_DECLARE_DETAIL_SAVE_REQUIRED);
        }
        String sourceType = resolveMidSourceType(sourceDetailList);
        String declareBillType = resolveDeclareBillType(sourceType);
        validateLatestProductLogistics(sourceDetailList);
        // 保存以最新 PLM 报关资料为准，前端提交的预览结果只作为选择范围，保存前必须重新生成合并结果。
        List<TmsDeclareBillDTO.MergeDeclareBillDTO> latestMergeList = tmsDeclareBillService.autoMergeDeclareBillView(
                new TmsDeclareBillDTO.AutoMergeDeclareBillViewDTO(Boolean.TRUE, sourceDetailList));
        List<TmsDeclareBillDTO.MergeDeclareBillDetailDTO> latestDetailList = collectMergeDetailList(latestMergeList);
        if (CollUtil.isEmpty(latestDetailList)) {
            throw new ServiceException(ApiError.LOGISTICS_DECLARE_DETAIL_SAVE_REQUIRED);
        }
        validateMergeDetailSource(latestDetailList, sourceType);

        List<DeliveryDeclareDetailMidEntity> changedMidList = new ArrayList<>();
        for (TmsDeclareBillDTO.MergeDeclareBillDTO mergeDeclareBillDTO : latestMergeList) {
            if (Objects.isNull(mergeDeclareBillDTO) || CollUtil.isEmpty(mergeDeclareBillDTO.getDeclareBillList())) {
                continue;
            }
            List<TmsDeclareBillDTO.MergeDeclareBillDetailDTO> declareBillList = mergeDeclareBillDTO.getDeclareBillList();
            List<TmsDeclareBillDetailEntity> detailEntityList = declareBillList.stream()
                    .map(this::buildDeclareBillDetailEntity)
                    .collect(Collectors.toList());
            TmsDeclareBillEntity declareBillEntity = buildDeclareBillEntity(declareBillType, declareBillList);
            BaseResultDTO.AddDTO addResult = tmsDeclareBillService.add(declareBillEntity, detailEntityList,
                    SourceTypeEnum.getEnum(declareBillType), false);
            changedMidList.addAll(saveGeneratedMidData(sourceType, declareBillList, detailEntityList,
                    addResult.getId(), addResult.getCode()));
        }
        updateFinishedSourceDeclareStatus(declareBillType, sourceType, changedMidList);
        return Boolean.TRUE;
    }

    @Override
    public List<DeliveryDeclareDetailMidEntity> listBySourceIdList(List<String> sourceIds) {
        return lambdaQuery()
                .in(DeliveryDeclareDetailMidEntity::getSourceId, sourceIds)
                .list();
    }

    /**
     * 校验中间表明细是否允许进入合并预览。
     *
     * <p>业务规则：待生成明细可直接预览；已生成明细只有在关联报关单仍为待确认时才允许重新预览，
     * 便于在报关单未确认前重新合并调整，已确认或已报关的数据不能再次参与生成。</p>
     */
    private void validateMergePreviewStatus(List<DeliveryDeclareDetailMidEntity> entityList) {
        List<String> declareIds = entityList.stream()
                .map(DeliveryDeclareDetailMidEntity::getDeclareId)
                .filter(StringUtils::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        Map<String, TmsDeclareBillEntity> declareBillMap = Collections.emptyMap();
        if (CollUtil.isNotEmpty(declareIds)) {
            declareBillMap = tmsDeclareBillService.lambdaQuery()
                    .in(TmsDeclareBillEntity::getId, declareIds)
                    .list()
                    .stream()
                    .collect(Collectors.toMap(TmsDeclareBillEntity::getId, item -> item, (oldValue, newValue) -> oldValue));
        }
        for (DeliveryDeclareDetailMidEntity entity : entityList) {
            if (CharSequenceUtil.equals(entity.getGenerateStatus(), DeliveryDeclareDetailMidGenerateStatusEnum.WAIT.getCode())) {
                continue;
            }
            if (!CharSequenceUtil.equals(entity.getGenerateStatus(), DeliveryDeclareDetailMidGenerateStatusEnum.FINISH.getCode())
                    || CharSequenceUtil.isBlank(entity.getDeclareId())) {
                throw new ServiceException(ApiError.LOGISTICS_DECLARE_DETAIL_MID_PREVIEW_STATUS_LIMIT);
            }
            TmsDeclareBillEntity declareBill = declareBillMap.get(entity.getDeclareId());
            if (Objects.isNull(declareBill)
                    || !CharSequenceUtil.equals(declareBill.getDeclareStatus(), DeclareStatusEnum.WAIT.getCode())) {
                throw new ServiceException(ApiError.LOGISTICS_DECLARE_DETAIL_MID_PREVIEW_STATUS_LIMIT);
            }
        }
    }

    /**
     * 校验合并预览来源维度一致性。
     *
     * <p>同一次合并预览只能进入一个国家、一个境外收货人类型；否则后续报关规则和报关单头信息
     * 无法稳定落到同一张单上。</p>
     */
    private void validatePreviewSourceConsistent(String sourceType, List<TmsDeclareBillDTO.SourceDeliveryDetailDTO> sourceDetailList) {
        Set<String> countrySet = sourceDetailList.stream()
                .map(TmsDeclareBillDTO.SourceDeliveryDetailDTO::getCountryId)
                .filter(CharSequenceUtil::isNotBlank)
                .collect(Collectors.toSet());
        if (countrySet.size() > 1) {
            throw new ServiceException(ApiError.LOGISTICS_DECLARE_DETAIL_MID_PREVIEW_COUNTRY_CONFLICT);
        }

        String declareBillType = resolveDeclareBillType(sourceType);
        Set<String> receiverTypeSet = new HashSet<>();
        for (TmsDeclareBillDTO.SourceDeliveryDetailDTO sourceDetail : sourceDetailList) {
            CfgDeclareRuleEntity cfgDeclareRule = cfgDeclareRuleService.listMatchedRule(
                    buildDeclareRuleMatchParamMap(declareBillType, Collections.singletonList(sourceDetail)));
            if (Objects.isNull(cfgDeclareRule)) {
                throw new ServiceException(ApiError.LOGISTICS_DECLARE_RULE_NOT_FOUND_FOR_SOURCE);
            }
            if (CharSequenceUtil.isNotBlank(cfgDeclareRule.getReceiverType())) {
                receiverTypeSet.add(cfgDeclareRule.getReceiverType());
            }
        }
        if (receiverTypeSet.size() > 1) {
            throw new ServiceException(ApiError.LOGISTICS_DECLARE_MERGE_RECEIVER_TYPE_DIFF);
        }
    }

    /**
     * 校验最终预览报关资料完整性。
     *
     * <p>该校验放在自动合并之后执行，确保检查的是经过最新 PLM 资料补齐、组合品最新 BOM 重拆后的
     * 最终报关明细，而不是中间表历史快照。</p>
     */
    private void validatePreviewDeclareInfo(List<TmsDeclareBillDTO.MergeDeclareBillDTO> previewList) {
        List<TmsDeclareBillDTO.MergeDeclareBillDetailDTO> detailList = collectMergeDetailList(previewList);
        for (int i = 0; i < detailList.size(); i++) {
            TmsDeclareBillDTO.MergeDeclareBillDetailDTO detail = detailList.get(i);
            int rowNo = i + 1;
            requirePreviewField(detail.getHsCode(), rowNo, "海关编码");
            requirePreviewField(detail.getProductNameCn(), rowNo, "报关品名");
            requirePreviewField(detail.getDeclareElement(), rowNo, "申报要素");
            requirePreviewField(detail.getUnit(), rowNo, "单位");
            requirePreviewField(detail.getDeclareCurrency(), rowNo, "币种");
            if (Objects.isNull(detail.getUnitPrice()) || detail.getUnitPrice().compareTo(BigDecimal.ZERO) <= 0) {
                throw new ServiceException(ApiError.LOGISTICS_DECLARE_DETAIL_FIELD_POSITIVE_REQUIRED, rowNo, "单价");
            }
        }
    }

    private void requirePreviewField(String value, int rowNo, String fieldName) {
        if (CharSequenceUtil.isBlank(value)) {
            throw new ServiceException(ApiError.LOGISTICS_DECLARE_DETAIL_FIELD_REQUIRED, rowNo, fieldName);
        }
    }

    /**
     * 根据用户勾选的中间表明细构造预览来源明细。
     *
     * <p>组合品拆分子 SKU 需要先按旧 BOM 历史版本校验全选；如果最新 BOM 已变化，
     * 预览阶段只替换为最新 BOM 结果，不删除旧中间表数据。</p>
     */
    private List<TmsDeclareBillDTO.SourceDeliveryDetailDTO> buildSelectedSourceDetailForPreview(List<DeliveryDeclareDetailMidEntity> selectedMidList,
                                                                                                List<String> selectedIds,
                                                                                                String sourceType) {
        List<String> sourceIds = selectedMidList.stream()
                .map(DeliveryDeclareDetailMidEntity::getSourceId)
                .filter(CharSequenceUtil::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        Map<String, TmsDeclareBillDTO.SourceDeliveryDetailDTO> sourceContextMap = loadSourceContextMap(sourceType, sourceIds);
        List<DeliveryDeclareDetailMidEntity> sourceMidList = listBySourceIdList(sourceIds);
        Set<String> selectedIdSet = new HashSet<>(selectedIds);

        List<String> bomHistoryIds = selectedMidList.stream()
                .map(DeliveryDeclareDetailMidEntity::getBomHistoryId)
                .filter(CharSequenceUtil::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        Map<String, List<BomChildrenSkuDTO>> historyMap = listBomHistoryMap(bomHistoryIds);
        List<String> parentSkuIds = historyMap.values().stream()
                .flatMap(Collection::stream)
                .map(BomChildrenSkuDTO::getParentSkuId)
                .filter(CharSequenceUtil::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        Map<String, ProductDetailDTO.ProductLogisticDTO> parentLogisticsMap = getProductLogisticsMap(parentSkuIds);

        Set<String> consumedComboGroup = new HashSet<>();
        List<TmsDeclareBillDTO.SourceDeliveryDetailDTO> result = new ArrayList<>();
        for (DeliveryDeclareDetailMidEntity selectedMid : selectedMidList) {
            if (CharSequenceUtil.isBlank(selectedMid.getBomHistoryId())) {
                result.add(buildSourceDetailFromMid(selectedMid, sourceContextMap.get(buildSourceBoxSkuKey(selectedMid)), null));
                continue;
            }
            String comboGroupKey = buildSourceBoxBomKey(selectedMid);
            if (!consumedComboGroup.add(comboGroupKey)) {
                continue;
            }
            List<BomChildrenSkuDTO> historyList = historyMap.get(selectedMid.getBomHistoryId());
            if (CollUtil.isEmpty(historyList)) {
                throw new ServiceException(ApiError.LOGISTICS_DECLARE_BOM_HISTORY_NOT_FOUND,
                        selectedMid.getSourceCode(), selectedMid.getBoxNo(), selectedMid.getSkuNo(), selectedMid.getBomHistoryId());
            }
            List<DeliveryDeclareDetailMidEntity> oldGroupList = findOldComboMidGroup(sourceMidList, selectedMid, historyList);
            validateComboChildFullSelected(selectedMid, oldGroupList, selectedIdSet, historyList);

            String parentSkuId = historyList.get(0).getParentSkuId();
            ProductDetailDTO.ProductLogisticDTO latestParentLogistics = parentLogisticsMap.get(parentSkuId);
            if (Objects.isNull(latestParentLogistics)) {
                throw new ServiceException(ApiError.LOGISTICS_DECLARE_LATEST_PRODUCT_LOGISTIC_NOT_FOUND,
                        CharSequenceUtil.blankToDefault(historyList.get(0).getParentSkuNo(), parentSkuId));
            }
            if (isComboBomChanged(selectedMid.getBomHistoryId(), historyList, latestParentLogistics)) {
                result.addAll(buildLatestComboSourceDetails(oldGroupList, historyList, latestParentLogistics, sourceContextMap));
                continue;
            }
            for (DeliveryDeclareDetailMidEntity oldMid : oldGroupList) {
                result.add(buildSourceDetailFromMid(oldMid, sourceContextMap.get(buildSourceBoxSkuKey(oldMid)), parentSkuId));
            }
        }
        return result;
    }

    private Map<String, TmsDeclareBillDTO.SourceDeliveryDetailDTO> loadSourceContextMap(String sourceType, List<String> sourceIds) {
        if (CollUtil.isEmpty(sourceIds)) {
            return Collections.emptyMap();
        }
        List<TmsDeclareBillDTO.SourceDeliveryDetailDTO> sourceDetailList = new ArrayList<>();
        // 只借用 WMS 来源明细补齐国家、仓库、组织等上下文字段，最终选择范围仍由中间表勾选行控制。
        if (Objects.equals(sourceType, SourceTypeEnum.FIRST_MILE_DELIVERY.getCode())) {
            sourceDetailList = wmsFirstMileDeliveryFeign.listBeforePushFmDeclare(new TmsDeclareBillDTO.PushDeclareBeforeParamDTO(Boolean.TRUE, sourceIds));
        } else if (Objects.equals(sourceType, SourceTypeEnum.SO_DELIVERY_NOTICE.getCode())) {
            sourceDetailList = soDeliveryNoticeFeign.listBeforePushB2bDeclare(new TmsDeclareBillDTO.PushDeclareBeforeParamDTO(Boolean.TRUE, sourceIds));
        }
        if (CollUtil.isEmpty(sourceDetailList)) {
            return Collections.emptyMap();
        }
        sourceDetailList.forEach(item -> item.setSourceType(sourceType));
        return sourceDetailList.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(this::buildSourceBoxSkuKey, item -> item, (oldValue, newValue) -> oldValue));
    }

    private Map<String, List<BomChildrenSkuDTO>> listBomHistoryMap(List<String> bomHistoryIds) {
        if (CollUtil.isEmpty(bomHistoryIds)) {
            return Collections.emptyMap();
        }
        List<BomChildrenSkuDTO> historyList = baseMapper.listBomHistoryByIds(bomHistoryIds);
        if (CollUtil.isEmpty(historyList)) {
            return Collections.emptyMap();
        }
        return historyList.stream()
                .filter(item -> CharSequenceUtil.isNotBlank(item.getBomHistoryId()))
                .collect(Collectors.groupingBy(BomChildrenSkuDTO::getBomHistoryId));
    }

    private List<DeliveryDeclareDetailMidEntity> findOldComboMidGroup(List<DeliveryDeclareDetailMidEntity> sourceMidList,
                                                                      DeliveryDeclareDetailMidEntity selectedMid,
                                                                      List<BomChildrenSkuDTO> historyList) {
        Set<String> childSkuIds = historyList.stream()
                .map(BomChildrenSkuDTO::getSkuId)
                .filter(CharSequenceUtil::isNotBlank)
                .collect(Collectors.toSet());
        return sourceMidList.stream()
                .filter(item -> CharSequenceUtil.equals(item.getSourceId(), selectedMid.getSourceId()))
                .filter(item -> CharSequenceUtil.equals(item.getBoxNo(), selectedMid.getBoxNo()))
                .filter(item -> CharSequenceUtil.equals(item.getBomHistoryId(), selectedMid.getBomHistoryId()))
                .filter(item -> childSkuIds.contains(item.getSkuId()))
                .collect(Collectors.toList());
    }

    private void validateComboChildFullSelected(DeliveryDeclareDetailMidEntity selectedMid,
                                                List<DeliveryDeclareDetailMidEntity> oldGroupList,
                                                Set<String> selectedIdSet,
                                                List<BomChildrenSkuDTO> historyList) {
        if (CollUtil.isEmpty(oldGroupList)) {
            throw new ServiceException(ApiError.LOGISTICS_DECLARE_BOM_HISTORY_NOT_FOUND,
                    selectedMid.getSourceCode(), selectedMid.getBoxNo(), selectedMid.getSkuNo(), selectedMid.getBomHistoryId());
        }
        List<String> missingSkuNoList = oldGroupList.stream()
                .filter(item -> !selectedIdSet.contains(item.getId()))
                .map(DeliveryDeclareDetailMidEntity::getSkuNo)
                .filter(CharSequenceUtil::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        if (CollUtil.isNotEmpty(missingSkuNoList)) {
            String parentSkuNo = historyList.stream()
                    .map(BomChildrenSkuDTO::getParentSkuNo)
                    .filter(CharSequenceUtil::isNotBlank)
                    .findFirst()
                    .orElse("");
            throw new ServiceException(ApiError.LOGISTICS_DECLARE_COMBO_CHILD_NOT_FULL_SELECTED,
                    selectedMid.getSourceCode(), selectedMid.getBoxNo(), parentSkuNo, String.join("、", missingSkuNoList));
        }
    }

    private boolean isComboBomChanged(String oldBomHistoryId,
                                      List<BomChildrenSkuDTO> historyList,
                                      ProductDetailDTO.ProductLogisticDTO latestParentLogistics) {
        // BOM 历史 id 相同但子件数量变化也视为 BOM 变化，避免旧拆分行继续参与下推。
        if (!CharSequenceUtil.equals(oldBomHistoryId, latestParentLogistics.getBomHistoryId())) {
            return true;
        }
        Map<String, Integer> oldChildQtyMap = historyList.stream()
                .collect(Collectors.toMap(BomChildrenSkuDTO::getSkuId,
                        item -> Objects.isNull(item.getQuantity()) ? 1 : item.getQuantity(),
                        (oldValue, newValue) -> oldValue));
        Map<String, Integer> latestChildQtyMap = latestParentLogistics.getChildList().stream()
                .collect(Collectors.toMap(ProductDetailDTO.ProductLogisticDTO::getSkuId,
                        item -> Objects.isNull(item.getChildQty()) ? 1 : item.getChildQty(),
                        (oldValue, newValue) -> oldValue));
        return !oldChildQtyMap.equals(latestChildQtyMap);
    }

    private List<TmsDeclareBillDTO.SourceDeliveryDetailDTO> buildLatestComboSourceDetails(List<DeliveryDeclareDetailMidEntity> oldGroupList,
                                                                                         List<BomChildrenSkuDTO> historyList,
                                                                                         ProductDetailDTO.ProductLogisticDTO latestParentLogistics,
                                                                                         Map<String, TmsDeclareBillDTO.SourceDeliveryDetailDTO> sourceContextMap) {
        if (CollUtil.isEmpty(latestParentLogistics.getChildList())) {
            throw new ServiceException(ApiError.LOGISTICS_COMBO_DECLARE_CHILD_EMPTY,
                    CharSequenceUtil.blankToDefault(latestParentLogistics.getSkuNo(), latestParentLogistics.getSkuId()));
        }
        DeliveryDeclareDetailMidEntity firstOldMid = oldGroupList.get(0);
        TmsDeclareBillDTO.SourceDeliveryDetailDTO parentContext = sourceContextMap.get(buildSourceBoxSkuKey(
                firstOldMid.getSourceId(), firstOldMid.getBoxNo(), latestParentLogistics.getSkuId()));
        if (Objects.isNull(parentContext)) {
            parentContext = buildSourceDetailFromMid(firstOldMid, null, latestParentLogistics.getSkuId());
        }
        // 旧中间表只保存拆分后的子件数量，最新 BOM 重拆时需先反算父 SKU 数量再乘以最新子件用量。
        int parentQty = calculateParentQty(oldGroupList, historyList);
        List<TmsDeclareBillDTO.SourceDeliveryDetailDTO> result = new ArrayList<>();
        for (ProductDetailDTO.ProductLogisticDTO childLogistics : latestParentLogistics.getChildList()) {
            TmsDeclareBillDTO.SourceDeliveryDetailDTO childSource = copySourceDetail(parentContext);
            childSource.setParentSkuId(latestParentLogistics.getSkuId());
            childSource.setSkuId(childLogistics.getSkuId());
            childSource.setSkuNo(childLogistics.getSkuNo());
            childSource.setBomHistoryId(latestParentLogistics.getBomHistoryId());
            childSource.setBomVersion(latestParentLogistics.getBomVersion());
            childSource.setQty(parentQty * (Objects.isNull(childLogistics.getChildQty()) ? 1 : childLogistics.getChildQty()));
            result.add(childSource);
        }
        return result;
    }

    private int calculateParentQty(List<DeliveryDeclareDetailMidEntity> oldGroupList, List<BomChildrenSkuDTO> historyList) {
        Map<String, Integer> historyQtyMap = historyList.stream()
                .collect(Collectors.toMap(BomChildrenSkuDTO::getSkuId,
                        item -> Objects.isNull(item.getQuantity()) ? 1 : item.getQuantity(),
                        (oldValue, newValue) -> oldValue));
        for (DeliveryDeclareDetailMidEntity oldMid : oldGroupList) {
            Integer childBomQty = historyQtyMap.get(oldMid.getSkuId());
            if (Objects.nonNull(oldMid.getQty()) && Objects.nonNull(childBomQty) && childBomQty > 0) {
                return Math.max(1, oldMid.getQty() / childBomQty);
            }
        }
        return 1;
    }

    private List<TmsDeclareBillDTO.SourceDeliveryDetailDTO> collectSourceDetailList(List<TmsDeclareBillDTO.MergeDeclareBillDTO> list) {
        return collectMergeDetailList(list).stream()
                .map(TmsDeclareBillDTO.MergeDeclareBillDetailDTO::getSourceDeliveryDetailList)
                .filter(CollUtil::isNotEmpty)
                .flatMap(Collection::stream)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private List<TmsDeclareBillDTO.MergeDeclareBillDetailDTO> collectMergeDetailList(List<TmsDeclareBillDTO.MergeDeclareBillDTO> list) {
        if (CollUtil.isEmpty(list)) {
            return Collections.emptyList();
        }
        return list.stream()
                .filter(Objects::nonNull)
                .map(TmsDeclareBillDTO.MergeDeclareBillDTO::getDeclareBillList)
                .filter(CollUtil::isNotEmpty)
                .flatMap(Collection::stream)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private String resolveMidSourceType(List<TmsDeclareBillDTO.SourceDeliveryDetailDTO> sourceDetailList) {
        Set<String> sourceTypeSet = sourceDetailList.stream()
                .map(TmsDeclareBillDTO.SourceDeliveryDetailDTO::getSourceType)
                .filter(CharSequenceUtil::isNotBlank)
                .collect(Collectors.toSet());
        if (sourceTypeSet.size() != 1) {
            throw new ServiceException(ApiError.LOGISTICS_DECLARE_DETAIL_MID_PREVIEW_SOURCE_TYPE_CONFLICT);
        }
        String sourceType = sourceTypeSet.iterator().next();
        if (!Objects.equals(sourceType, SourceTypeEnum.FIRST_MILE_DELIVERY.getCode())
                && !Objects.equals(sourceType, SourceTypeEnum.SO_DELIVERY_NOTICE.getCode())) {
            throw new ServiceException(ApiError.LOGISTICS_DECLARE_BILL_TYPE_MISMATCH);
        }
        return sourceType;
    }

    private String resolveDeclareBillType(String sourceType) {
        if (Objects.equals(sourceType, SourceTypeEnum.FIRST_MILE_DELIVERY.getCode())) {
            return SourceTypeEnum.FM_DECLARE_BILL.getCode();
        }
        if (Objects.equals(sourceType, SourceTypeEnum.SO_DELIVERY_NOTICE.getCode())) {
            return SourceTypeEnum.B2B_DECLARE_BILL.getCode();
        }
        throw new ServiceException(ApiError.LOGISTICS_DECLARE_BILL_TYPE_MISMATCH);
    }

    private void validateMergeDetailSource(List<TmsDeclareBillDTO.MergeDeclareBillDetailDTO> detailList, String sourceType) {
        Set<String> sourceKeySet = new HashSet<>();
        for (TmsDeclareBillDTO.MergeDeclareBillDetailDTO detail : detailList) {
            if (CollUtil.isEmpty(detail.getSourceDeliveryDetailList())) {
                throw new ServiceException(ApiError.LOGISTICS_DECLARE_DETAIL_SAVE_REQUIRED);
            }
            for (TmsDeclareBillDTO.SourceDeliveryDetailDTO sourceDetail : detail.getSourceDeliveryDetailList()) {
                if (Objects.isNull(sourceDetail) || CharSequenceUtil.isBlank(sourceDetail.getSourceId())
                        || CharSequenceUtil.isBlank(sourceDetail.getBoxNo()) || CharSequenceUtil.isBlank(sourceDetail.getSkuId())) {
                    throw new ServiceException(ApiError.LOGISTICS_DECLARE_SOURCE_DETAIL_NOT_FOUND_FOR_SAVE);
                }
                if (!CharSequenceUtil.equals(sourceType, sourceDetail.getSourceType())) {
                    throw new ServiceException(ApiError.LOGISTICS_DECLARE_DETAIL_MID_PREVIEW_SOURCE_TYPE_CONFLICT);
                }
                String key = buildSourceBoxSkuKey(sourceDetail);
                if (!sourceKeySet.add(key)) {
                    throw new ServiceException(ApiError.LOGISTICS_DECLARE_DETAIL_SOURCE_DUPLICATE, 1);
                }
            }
        }
    }

    private void validateLatestProductLogistics(List<TmsDeclareBillDTO.SourceDeliveryDetailDTO> sourceDetailList) {
        List<String> skuIds = sourceDetailList.stream()
                .map(TmsDeclareBillDTO.SourceDeliveryDetailDTO::getSkuId)
                .filter(CharSequenceUtil::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        Map<String, ProductDetailDTO.ProductLogisticDTO> logisticsMap = getProductLogisticsMap(skuIds);
        for (TmsDeclareBillDTO.SourceDeliveryDetailDTO sourceDetail : sourceDetailList) {
            if (!logisticsMap.containsKey(sourceDetail.getSkuId())) {
                throw new ServiceException(ApiError.LOGISTICS_DECLARE_LATEST_PRODUCT_LOGISTIC_NOT_FOUND,
                        CharSequenceUtil.blankToDefault(sourceDetail.getSkuNo(), sourceDetail.getSkuId()));
            }
        }
    }

    private TmsDeclareBillDetailEntity buildDeclareBillDetailEntity(TmsDeclareBillDTO.MergeDeclareBillDetailDTO detailDTO) {
        TmsDeclareBillDetailEntity detailEntity = new TmsDeclareBillDetailEntity();
        detailEntity.setSkuId(CharSequenceUtil.isNotBlank(detailDTO.getLeadSkuId()) ? detailDTO.getLeadSkuId() : null);
        detailEntity.setSkuNo(detailDTO.getSkuNo());
        detailEntity.setCustomsCode(detailDTO.getHsCode());
        detailEntity.setDeclareChineseName(detailDTO.getProductNameCn());
        detailEntity.setDeclareElement(detailDTO.getDeclareElement());
        detailEntity.setDeclareUnit(detailDTO.getUnit());
        detailEntity.setPrice(Objects.isNull(detailDTO.getUnitPrice()) ? BigDecimal.ZERO : detailDTO.getUnitPrice());
        detailEntity.setQty(Objects.isNull(detailDTO.getQty()) ? 0 : detailDTO.getQty());
        detailEntity.setDeclareCurrency(detailDTO.getDeclareCurrency());
        detailEntity.setDeclareCurrencySymbol(detailDTO.getDeclareCurrencySymbol());
        detailEntity.setSourceCountry(detailDTO.getSourceCountry());
        detailEntity.setSourceCountryName(detailDTO.getSourceCountryName());
        detailEntity.setToCountry(detailDTO.getToCountry());
        detailEntity.setToCountryName(detailDTO.getToCountryName());
        detailEntity.setSourceCargo(detailDTO.getSourceCargo());
        detailEntity.setExemption(detailDTO.getExemption());
        return detailEntity;
    }

    private TmsDeclareBillEntity buildDeclareBillEntity(String declareBillType,
                                                       List<TmsDeclareBillDTO.MergeDeclareBillDetailDTO> declareBillList) {
        TmsDeclareBillEntity entity = new TmsDeclareBillEntity();
        entity.setType(declareBillType);
        entity.setDeclareType(DeclareDeclareTypeEnum.INDEPENDENT.getCode());
        entity.setDictSupervisionMethod(DeclareSupervisionMethodEnum.COMMONLY.getCode());
        entity.setDictNatureLevy(DeclareNatureLevyEnum.COMMONLY.getCode());
        entity.setDictPackType(DeclarePackTypeEnum.CARTON.getCode());
        entity.setDictTransactionMethod(DeclareTransactionMethodEnum.EXW.getCode());
        entity.setDeclareStatus(DeclareStatusEnum.WAIT.getCode());
        entity.setDeclareDate(LocalDate.now());
        entity.setShippingFee(BigDecimal.ZERO);
        entity.setInsuranceFee(BigDecimal.ZERO);
        entity.setOtherFee(BigDecimal.ZERO);

        TmsDeclareBillDTO.MergeDeclareBillDetailDTO firstDetail = declareBillList.get(0);
        entity.setCountry(firstDetail.getToCountry());
        entity.setCountryName(firstDetail.getToCountryName());
        entity.setToArea(firstDetail.getToCountry());
        entity.setToPort(firstDetail.getToCountry());

        List<TmsDeclareBillDTO.SourceDeliveryDetailDTO> sourceDetailList = declareBillList.stream()
                .map(TmsDeclareBillDTO.MergeDeclareBillDetailDTO::getSourceDeliveryDetailList)
                .filter(CollUtil::isNotEmpty)
                .flatMap(Collection::stream)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        TmsDeclareBillDTO.SelectedSkuHeaderParamDTO headerParamDTO = new TmsDeclareBillDTO.SelectedSkuHeaderParamDTO();
        headerParamDTO.setSourceDeliveryDetailList(sourceDetailList);
        TmsDeclareBillDTO.SelectedSkuHeaderDTO headerDTO = tmsDeclareBillService.querySelectedSkuHeader(headerParamDTO, SourceTypeEnum.getEnum(declareBillType));
        entity.setTransportNo(headerDTO.getTransportNo());
        entity.setBoxQty(Objects.isNull(headerDTO.getBoxQty()) ? 0 : headerDTO.getBoxQty());
        entity.setGrossWeight(Objects.isNull(headerDTO.getGrossWeight()) ? BigDecimal.ZERO : headerDTO.getGrossWeight());
        entity.setNetWeight(Objects.isNull(headerDTO.getNetWeight()) ? BigDecimal.ZERO : headerDTO.getNetWeight());
        entity.setBusinessType(resolveBusinessType(declareBillType, sourceDetailList));

        CfgDeclareRuleEntity cfgDeclareRule = cfgDeclareRuleService.listMatchedRule(buildDeclareRuleMatchParamMap(declareBillType, sourceDetailList));
        if (Objects.isNull(cfgDeclareRule)) {
            throw new ServiceException(ApiError.LOGISTICS_DECLARE_RULE_NOT_FOUND_FOR_SOURCE);
        }
        entity.setSenderId(cfgDeclareRule.getSenderId());
        entity.setReceiverId(cfgDeclareRule.getReceiverId());
        entity.setSenderName(cfgDeclareRule.getSenderName());
        entity.setReceiverName(cfgDeclareRule.getReceiverName());
        entity.setSenderType(cfgDeclareRule.getSenderType());
        entity.setReceiverType(cfgDeclareRule.getReceiverType());
        return entity;
    }

    private String resolveBusinessType(String declareBillType, List<TmsDeclareBillDTO.SourceDeliveryDetailDTO> sourceDetailList) {
        List<String> sourceIds = sourceDetailList.stream()
                .map(TmsDeclareBillDTO.SourceDeliveryDetailDTO::getSourceId)
                .filter(CharSequenceUtil::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        if (CollUtil.isEmpty(sourceIds)) {
            return "";
        }
        if (CharSequenceUtil.equals(declareBillType, SourceTypeEnum.FM_DECLARE_BILL.getCode())) {
            return tmsDeclareBillService.getCanGenerateDeliveryOrder(TmsDeclareBillDTO.QuerySourceDTO.builder().ids(sourceIds).build())
                    .stream()
                    .map(TmsDeclareBillDTO.DeliveryDTO::getBusinessType)
                    .filter(CharSequenceUtil::isNotBlank)
                    .distinct()
                    .collect(Collectors.joining(","));
        }
        return tmsDeclareBillService.getCanGenerateSoOut(TmsDeclareBillDTO.QuerySourceDTO.builder().ids(sourceIds).build())
                .stream()
                .map(TmsDeclareBillDTO.SoOutDTO::getBusinessType)
                .filter(CharSequenceUtil::isNotBlank)
                .distinct()
                .collect(Collectors.joining(","));
    }

    private Map<String, String> buildDeclareRuleMatchParamMap(String declareBillType,
                                                              List<TmsDeclareBillDTO.SourceDeliveryDetailDTO> sourceDetailList) {
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("ruleType", declareBillType);
        paramMap.put("countryCode", joinDistinct(sourceDetailList.stream()
                .map(TmsDeclareBillDTO.SourceDeliveryDetailDTO::getCountryId)
                .collect(Collectors.toList())));
        paramMap.put("fromWarehouseId", joinDistinct(sourceDetailList.stream()
                .map(TmsDeclareBillDTO.SourceDeliveryDetailDTO::getFromWarehouseId)
                .collect(Collectors.toList())));
        paramMap.put("transferWarehouseId", joinDistinct(sourceDetailList.stream()
                .map(TmsDeclareBillDTO.SourceDeliveryDetailDTO::getTransferWarehouseIds)
                .collect(Collectors.toList())));
        if (CharSequenceUtil.equals(declareBillType, SourceTypeEnum.FM_DECLARE_BILL.getCode())) {
            paramMap.put("destWarehouseId", joinDistinct(sourceDetailList.stream()
                    .map(TmsDeclareBillDTO.SourceDeliveryDetailDTO::getDestWarehouseId)
                    .collect(Collectors.toList())));
        } else {
            paramMap.put("salesOrgId", joinDistinct(sourceDetailList.stream()
                    .map(TmsDeclareBillDTO.SourceDeliveryDetailDTO::getSalesOrgId)
                    .collect(Collectors.toList())));
        }
        return paramMap;
    }

    private String joinDistinct(List<String> values) {
        if (CollUtil.isEmpty(values)) {
            return "";
        }
        return values.stream()
                .filter(CharSequenceUtil::isNotBlank)
                .distinct()
                .collect(Collectors.joining(","));
    }

    /**
     * 保存中间表生成结果。
     *
     * <p>普通明细复用原中间表行更新为已生成；组合品 BOM 已变化时，必须等报关单保存成功后，
     * 才逻辑删除同来源单、箱号、父 SKU 下的旧拆分行，并写入最新 BOM 对应的生成行。</p>
     */
    private List<DeliveryDeclareDetailMidEntity> saveGeneratedMidData(String sourceType,
                                                                      List<TmsDeclareBillDTO.MergeDeclareBillDetailDTO> declareBillList,
                                                                      List<TmsDeclareBillDetailEntity> detailEntityList,
                                                                      String declareId,
                                                                      String declareCode) {
        List<TmsDeclareBillDTO.SourceDeliveryDetailDTO> sourceDetailList = declareBillList.stream()
                .map(TmsDeclareBillDTO.MergeDeclareBillDetailDTO::getSourceDeliveryDetailList)
                .filter(CollUtil::isNotEmpty)
                .flatMap(Collection::stream)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        List<String> sourceIds = sourceDetailList.stream()
                .map(TmsDeclareBillDTO.SourceDeliveryDetailDTO::getSourceId)
                .filter(CharSequenceUtil::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        List<DeliveryDeclareDetailMidEntity> existingMidList = listBySourceIdList(sourceIds);
        Map<String, DeliveryDeclareDetailMidEntity> existingKeyMap = existingMidList.stream()
                .filter(item -> CharSequenceUtil.equals(item.getSourceType(), sourceType))
                .collect(Collectors.toMap(this::buildSourceBoxSkuKey, item -> item, (oldValue, newValue) -> oldValue));
        Set<String> sourceKeySet = sourceDetailList.stream()
                .map(this::buildSourceBoxSkuKey)
                .collect(Collectors.toSet());
        List<DeliveryDeclareDetailMidEntity> generatedConflictList = existingMidList.stream()
                .filter(item -> sourceKeySet.contains(buildSourceBoxSkuKey(item)))
                .filter(item -> CharSequenceUtil.isNotBlank(item.getDeclareId())
                        || DeliveryDeclareDetailMidGenerateStatusEnum.FINISH.getCode().equals(item.getGenerateStatus()))
                .collect(Collectors.toList());
        if (CollUtil.isNotEmpty(generatedConflictList)) {
            String repeatSourceCode = generatedConflictList.stream()
                    .map(DeliveryDeclareDetailMidEntity::getSourceCode)
                    .filter(CharSequenceUtil::isNotBlank)
                    .distinct()
                    .collect(Collectors.joining("、"));
            throw new ServiceException(ApiError.LOGISTICS_DECLARE_SOURCE_GENERATED, repeatSourceCode);
        }

        Set<String> comboGroupKeys = sourceDetailList.stream()
                .filter(item -> CharSequenceUtil.isNotBlank(item.getParentSkuId()))
                .map(this::buildSourceBoxParentKey)
                .collect(Collectors.toSet());
        Set<String> changedComboGroupKeys = findChangedComboGroupKeys(comboGroupKeys, sourceDetailList, existingMidList);
        if (CollUtil.isNotEmpty(changedComboGroupKeys)) {
            List<String> deleteIds = findComboMidIds(existingMidList, changedComboGroupKeys);
            if (CollUtil.isNotEmpty(deleteIds)) {
                // 预览阶段不允许清理旧行；只有报关单主明细保存成功后，才在同一事务内清理旧 BOM 拆分行。
                super.removeByIds(deleteIds);
            }
        }

        List<DeliveryDeclareDetailMidEntity> addMidList = new ArrayList<>();
        List<DeliveryDeclareDetailMidEntity> changedMidList = new ArrayList<>();
        for (int i = 0; i < declareBillList.size(); i++) {
            TmsDeclareBillDTO.MergeDeclareBillDetailDTO declareDetail = declareBillList.get(i);
            TmsDeclareBillDetailEntity billDetail = detailEntityList.get(i);
            if (CollUtil.isEmpty(declareDetail.getSourceDeliveryDetailList())) {
                continue;
            }
            for (TmsDeclareBillDTO.SourceDeliveryDetailDTO sourceDetail : declareDetail.getSourceDeliveryDetailList()) {
                String sourceKey = buildSourceBoxSkuKey(sourceDetail);
                DeliveryDeclareDetailMidEntity existingMid = existingKeyMap.get(sourceKey);
                boolean comboChanged = changedComboGroupKeys.contains(buildSourceBoxParentKey(sourceDetail));
                if (Objects.nonNull(existingMid) && !comboChanged) {
                    fillGeneratedMid(existingMid, sourceType, sourceDetail, declareDetail, declareId, declareCode, billDetail.getId());
                    changedMidList.add(existingMid);
                    continue;
                }
                DeliveryDeclareDetailMidEntity addMid = new DeliveryDeclareDetailMidEntity();
                fillGeneratedMid(addMid, sourceType, sourceDetail, declareDetail, declareId, declareCode, billDetail.getId());
                addMidList.add(addMid);
                changedMidList.add(addMid);
            }
        }
        List<DeliveryDeclareDetailMidEntity> updateMidList = changedMidList.stream()
                .filter(item -> CharSequenceUtil.isNotBlank(item.getId()))
                .collect(Collectors.toList());
        if (CollUtil.isNotEmpty(updateMidList)) {
            super.updateBatchById(updateMidList);
        }
        if (CollUtil.isNotEmpty(addMidList)) {
            super.saveBatch(addMidList);
        }
        return changedMidList;
    }

    /**
     * 识别本次保存中需要替换旧拆分行的组合品范围。
     *
     * <p>判断维度为同来源单、同箱号、同父 SKU；只要子件集合或 BOM 历史版本不同，
     * 就按 BOM 变化处理，避免新旧拆分结果混在同一箱号下。</p>
     */
    private Set<String> findChangedComboGroupKeys(Set<String> comboGroupKeys,
                                                  List<TmsDeclareBillDTO.SourceDeliveryDetailDTO> sourceDetailList,
                                                  List<DeliveryDeclareDetailMidEntity> existingMidList) {
        if (CollUtil.isEmpty(comboGroupKeys)) {
            return Collections.emptySet();
        }
        Map<String, List<TmsDeclareBillDTO.SourceDeliveryDetailDTO>> submitGroupMap = sourceDetailList.stream()
                .filter(item -> comboGroupKeys.contains(buildSourceBoxParentKey(item)))
                .collect(Collectors.groupingBy(this::buildSourceBoxParentKey));
        Map<String, List<BomChildrenSkuDTO>> historyByParentMap = listBomHistoryByParentMap(sourceDetailList.stream()
                .map(TmsDeclareBillDTO.SourceDeliveryDetailDTO::getParentSkuId)
                .filter(CharSequenceUtil::isNotBlank)
                .distinct()
                .collect(Collectors.toList()));
        Set<String> changedGroupKeys = new HashSet<>();
        for (String comboGroupKey : comboGroupKeys) {
            List<TmsDeclareBillDTO.SourceDeliveryDetailDTO> submitList = submitGroupMap.get(comboGroupKey);
            if (CollUtil.isEmpty(submitList)) {
                continue;
            }
            List<DeliveryDeclareDetailMidEntity> existingGroup = findComboMidList(existingMidList, comboGroupKey, historyByParentMap);
            if (CollUtil.isEmpty(existingGroup)) {
                continue;
            }
            Set<String> submitSkuSet = submitList.stream().map(TmsDeclareBillDTO.SourceDeliveryDetailDTO::getSkuId).collect(Collectors.toSet());
            Set<String> existingSkuSet = existingGroup.stream().map(DeliveryDeclareDetailMidEntity::getSkuId).collect(Collectors.toSet());
            Set<String> submitBomHistorySet = submitList.stream().map(TmsDeclareBillDTO.SourceDeliveryDetailDTO::getBomHistoryId).collect(Collectors.toSet());
            Set<String> existingBomHistorySet = existingGroup.stream().map(DeliveryDeclareDetailMidEntity::getBomHistoryId).collect(Collectors.toSet());
            if (!submitSkuSet.equals(existingSkuSet) || !submitBomHistorySet.equals(existingBomHistorySet)) {
                changedGroupKeys.add(comboGroupKey);
            }
        }
        return changedGroupKeys;
    }

    private Map<String, List<BomChildrenSkuDTO>> listBomHistoryByParentMap(List<String> parentSkuIds) {
        if (CollUtil.isEmpty(parentSkuIds)) {
            return Collections.emptyMap();
        }
        List<BomChildrenSkuDTO> historyList = baseMapper.listBomHistoryByParentSkuIds(parentSkuIds);
        if (CollUtil.isEmpty(historyList)) {
            return Collections.emptyMap();
        }
        return historyList.stream()
                .filter(item -> CharSequenceUtil.isNotBlank(item.getParentSkuId()))
                .collect(Collectors.groupingBy(BomChildrenSkuDTO::getParentSkuId));
    }

    private List<DeliveryDeclareDetailMidEntity> findComboMidList(List<DeliveryDeclareDetailMidEntity> existingMidList,
                                                                  String comboGroupKey,
                                                                  Map<String, List<BomChildrenSkuDTO>> historyByParentMap) {
        String[] parts = comboGroupKey.split("\\|", -1);
        if (parts.length < 3) {
            return Collections.emptyList();
        }
        String sourceId = parts[0];
        String boxNo = parts[1];
        String parentSkuId = parts[2];
        List<BomChildrenSkuDTO> historyList = historyByParentMap.get(parentSkuId);
        if (CollUtil.isEmpty(historyList)) {
            return Collections.emptyList();
        }
        Set<String> bomHistoryIds = historyList.stream().map(BomChildrenSkuDTO::getBomHistoryId).collect(Collectors.toSet());
        Set<String> childSkuIds = historyList.stream().map(BomChildrenSkuDTO::getSkuId).collect(Collectors.toSet());
        return existingMidList.stream()
                .filter(item -> CharSequenceUtil.equals(item.getSourceId(), sourceId))
                .filter(item -> CharSequenceUtil.equals(item.getBoxNo(), boxNo))
                .filter(item -> bomHistoryIds.contains(item.getBomHistoryId()))
                .filter(item -> childSkuIds.contains(item.getSkuId()))
                .collect(Collectors.toList());
    }

    private List<String> findComboMidIds(List<DeliveryDeclareDetailMidEntity> existingMidList, Set<String> comboGroupKeys) {
        List<String> parentSkuIds = comboGroupKeys.stream()
                .map(key -> key.split("\\|", -1))
                .filter(parts -> parts.length >= 3)
                .map(parts -> parts[2])
                .filter(CharSequenceUtil::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        Map<String, List<BomChildrenSkuDTO>> historyByParentMap = listBomHistoryByParentMap(parentSkuIds);
        return comboGroupKeys.stream()
                .map(key -> findComboMidList(existingMidList, key, historyByParentMap))
                .flatMap(Collection::stream)
                .map(DeliveryDeclareDetailMidEntity::getId)
                .filter(CharSequenceUtil::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
    }

    private void fillGeneratedMid(DeliveryDeclareDetailMidEntity midEntity,
                                  String sourceType,
                                  TmsDeclareBillDTO.SourceDeliveryDetailDTO sourceDetail,
                                  TmsDeclareBillDTO.MergeDeclareBillDetailDTO declareDetail,
                                  String declareId,
                                  String declareCode,
                                  String declareDetailId) {
        midEntity.setGenerateStatus(DeliveryDeclareDetailMidGenerateStatusEnum.FINISH.getCode());
        midEntity.setSourceType(sourceType);
        midEntity.setSourceId(sourceDetail.getSourceId());
        midEntity.setSourceCode(CharSequenceUtil.blankToDefault(sourceDetail.getSourceCode(), ""));
        midEntity.setBusinessId(CharSequenceUtil.blankToDefault(sourceDetail.getBusinessId(), ""));
        midEntity.setBusinessCode(CharSequenceUtil.blankToDefault(sourceDetail.getBusinessCode(), ""));
        midEntity.setBusinessType(sourceType);
        midEntity.setContractNo(declareCode);
        midEntity.setSkuId(CharSequenceUtil.blankToDefault(sourceDetail.getSkuId(), ""));
        midEntity.setSkuNo(CharSequenceUtil.blankToDefault(sourceDetail.getSkuNo(), ""));
        midEntity.setBomHistoryId(CharSequenceUtil.blankToDefault(sourceDetail.getBomHistoryId(), ""));
        midEntity.setBomVersion(CharSequenceUtil.blankToDefault(sourceDetail.getBomVersion(), ""));
        midEntity.setCurrency(CharSequenceUtil.blankToDefault(declareDetail.getDeclareCurrency(), ""));
        midEntity.setCurrencySymbol(CharSequenceUtil.blankToDefault(declareDetail.getDeclareCurrencySymbol(), ""));
        midEntity.setDeclareId(declareId);
        midEntity.setDeclareCode(declareCode);
        midEntity.setDeclareDetailId(declareDetailId);
        midEntity.setBoxNo(CharSequenceUtil.blankToDefault(sourceDetail.getBoxNo(), ""));
        midEntity.setHsCode(CharSequenceUtil.blankToDefault(declareDetail.getHsCode(), ""));
        midEntity.setProductNameCn(CharSequenceUtil.blankToDefault(declareDetail.getProductNameCn(), ""));
        midEntity.setDeclareElement(CharSequenceUtil.blankToDefault(declareDetail.getDeclareElement(), ""));
        midEntity.setUnit(CharSequenceUtil.blankToDefault(declareDetail.getUnit(), ""));
        midEntity.setUnitPrice(Objects.isNull(declareDetail.getUnitPrice()) ? BigDecimal.ZERO : declareDetail.getUnitPrice());
        midEntity.setQty(Objects.isNull(sourceDetail.getQty()) ? 0 : sourceDetail.getQty());
        midEntity.setFromWarehouseId(sourceDetail.getFromWarehouseId());
        midEntity.setFromWarehouseName(sourceDetail.getFromWarehouseName());
        midEntity.setDestWarehouseId(sourceDetail.getDestWarehouseId());
        midEntity.setDestWarehouseName(sourceDetail.getDestWarehouseName());
        midEntity.setTransferWarehouseIds(CharSequenceUtil.blankToDefault(sourceDetail.getTransferWarehouseIds(), ""));
        midEntity.setTransferWarehouseNames(CharSequenceUtil.blankToDefault(sourceDetail.getTransferWarehouseNames(), ""));
        midEntity.setSalesOrgId(sourceDetail.getSalesOrgId());
        midEntity.setSalesOrgName(sourceDetail.getSalesOrgName());
    }

    /**
     * 按来源单完整性更新报关状态。
     *
     * <p>中间表支持明细维度下推后，来源单不能因本次部分明细生成就置为 finish；
     * 只有该来源单不存在待生成中间表明细时，才通知 WMS 更新为已完成。</p>
     */
    private void updateFinishedSourceDeclareStatus(String declareBillType,
                                                   String sourceType,
                                                   List<DeliveryDeclareDetailMidEntity> changedMidList) {
        if (CollUtil.isEmpty(changedMidList)) {
            return;
        }
        List<String> sourceIds = changedMidList.stream()
                .map(DeliveryDeclareDetailMidEntity::getSourceId)
                .filter(CharSequenceUtil::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        if (CollUtil.isEmpty(sourceIds)) {
            return;
        }
        List<String> waitSourceIds = baseMapper.listWaitSourceIds(sourceType, sourceIds);
        Set<String> waitSourceIdSet = CollUtil.isEmpty(waitSourceIds) ? Collections.emptySet() : new HashSet<>(waitSourceIds);
        List<String> finishSourceIds = sourceIds.stream()
                .filter(sourceId -> !waitSourceIdSet.contains(sourceId))
                .collect(Collectors.toList());
        if (CollUtil.isEmpty(finishSourceIds)) {
            return;
        }
        if (CharSequenceUtil.equals(declareBillType, SourceTypeEnum.FM_DECLARE_BILL.getCode())) {
            wmsFirstMileDeliveryFeign.updateStatus(new FirstMileDeliveryDTO.UpdateStatusDTO(finishSourceIds, null, WmsDeclareStatusEnum.FINISH.getCode()));
        } else {
            soDeliveryNoticeFeign.updateDeclareStatus(new SoDeliveryNoticeDTO.DeclareStatusDTO(finishSourceIds, WmsDeclareStatusEnum.FINISH.getCode()));
        }
    }

    private TmsDeclareBillDTO.SourceDeliveryDetailDTO buildSourceDetailFromMid(DeliveryDeclareDetailMidEntity mid,
                                                                               TmsDeclareBillDTO.SourceDeliveryDetailDTO context,
                                                                               String parentSkuId) {
        TmsDeclareBillDTO.SourceDeliveryDetailDTO detailDTO = copySourceDetail(context);
        detailDTO.setSourceId(CharSequenceUtil.blankToDefault(mid.getSourceId(), detailDTO.getSourceId()));
        detailDTO.setSourceType(CharSequenceUtil.blankToDefault(mid.getSourceType(), detailDTO.getSourceType()));
        detailDTO.setSourceCode(CharSequenceUtil.blankToDefault(mid.getSourceCode(), detailDTO.getSourceCode()));
        detailDTO.setBusinessId(CharSequenceUtil.blankToDefault(mid.getBusinessId(), detailDTO.getBusinessId()));
        detailDTO.setBusinessCode(CharSequenceUtil.blankToDefault(mid.getBusinessCode(), detailDTO.getBusinessCode()));
        detailDTO.setBoxNo(CharSequenceUtil.blankToDefault(mid.getBoxNo(), detailDTO.getBoxNo()));
        detailDTO.setParentSkuId(parentSkuId);
        detailDTO.setSkuId(mid.getSkuId());
        detailDTO.setSkuNo(mid.getSkuNo());
        detailDTO.setBomHistoryId(mid.getBomHistoryId());
        detailDTO.setBomVersion(mid.getBomVersion());
        detailDTO.setHsCode(mid.getHsCode());
        detailDTO.setProductNameCn(mid.getProductNameCn());
        detailDTO.setDeclareElement(mid.getDeclareElement());
        detailDTO.setUnit(mid.getUnit());
        detailDTO.setUnitPrice(mid.getUnitPrice());
        detailDTO.setQty(mid.getQty());
        detailDTO.setDeclareCurrency(mid.getCurrency());
        detailDTO.setDeclareCurrencySymbol(mid.getCurrencySymbol());
        detailDTO.setFromWarehouseId(CharSequenceUtil.blankToDefault(mid.getFromWarehouseId(), detailDTO.getFromWarehouseId()));
        detailDTO.setFromWarehouseName(CharSequenceUtil.blankToDefault(mid.getFromWarehouseName(), detailDTO.getFromWarehouseName()));
        detailDTO.setDestWarehouseId(CharSequenceUtil.blankToDefault(mid.getDestWarehouseId(), detailDTO.getDestWarehouseId()));
        detailDTO.setDestWarehouseName(CharSequenceUtil.blankToDefault(mid.getDestWarehouseName(), detailDTO.getDestWarehouseName()));
        detailDTO.setTransferWarehouseIds(CharSequenceUtil.blankToDefault(mid.getTransferWarehouseIds(), detailDTO.getTransferWarehouseIds()));
        detailDTO.setTransferWarehouseNames(CharSequenceUtil.blankToDefault(mid.getTransferWarehouseNames(), detailDTO.getTransferWarehouseNames()));
        detailDTO.setSalesOrgId(CharSequenceUtil.blankToDefault(mid.getSalesOrgId(), detailDTO.getSalesOrgId()));
        detailDTO.setSalesOrgName(CharSequenceUtil.blankToDefault(mid.getSalesOrgName(), detailDTO.getSalesOrgName()));
        return detailDTO;
    }

    private TmsDeclareBillDTO.SourceDeliveryDetailDTO copySourceDetail(TmsDeclareBillDTO.SourceDeliveryDetailDTO source) {
        TmsDeclareBillDTO.SourceDeliveryDetailDTO copy = new TmsDeclareBillDTO.SourceDeliveryDetailDTO();
        if (Objects.nonNull(source)) {
            BeanUtil.copyProperties(source, copy);
        }
        return copy;
    }

    private String buildSourceBoxBomKey(DeliveryDeclareDetailMidEntity mid) {
        return buildSourceBoxBomKey(mid.getSourceId(), mid.getBoxNo(), mid.getBomHistoryId());
    }

    private String buildSourceBoxBomKey(String sourceId, String boxNo, String bomHistoryId) {
        return CharSequenceUtil.join("|", CharSequenceUtil.blankToDefault(sourceId, ""),
                CharSequenceUtil.blankToDefault(boxNo, ""), CharSequenceUtil.blankToDefault(bomHistoryId, ""));
    }

    private String buildSourceBoxSkuKey(DeliveryDeclareDetailMidEntity mid) {
        return buildSourceBoxSkuKey(mid.getSourceId(), mid.getBoxNo(), mid.getSkuId());
    }

    private String buildSourceBoxSkuKey(TmsDeclareBillDTO.SourceDeliveryDetailDTO sourceDetail) {
        return buildSourceBoxSkuKey(sourceDetail.getSourceId(), sourceDetail.getBoxNo(), sourceDetail.getSkuId());
    }

    private String buildSourceBoxSkuKey(String sourceId, String boxNo, String skuId) {
        return CharSequenceUtil.join("|", CharSequenceUtil.blankToDefault(sourceId, ""),
                CharSequenceUtil.blankToDefault(boxNo, ""), CharSequenceUtil.blankToDefault(skuId, ""));
    }

    private String buildSourceBoxParentKey(TmsDeclareBillDTO.SourceDeliveryDetailDTO sourceDetail) {
        if (CharSequenceUtil.isBlank(sourceDetail.getParentSkuId())) {
            return "";
        }
        return CharSequenceUtil.join("|", CharSequenceUtil.blankToDefault(sourceDetail.getSourceId(), ""),
                CharSequenceUtil.blankToDefault(sourceDetail.getBoxNo(), ""), sourceDetail.getParentSkuId());
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
     * 处理中间表默认数据
     *
     * @param entity 报关明细中间表实体
     * @throws RuntimeException 当前方法不主动抛出业务异常
     * @author jack
     * @date 2026-04-29
     */
    private void handleData(DeliveryDeclareDetailMidEntity entity) {
        if (CharSequenceUtil.isBlank(entity.getGenerateStatus())) {
            entity.setGenerateStatus(DeliveryDeclareDetailMidGenerateStatusEnum.WAIT.getCode());
        }
        if (CharSequenceUtil.isBlank(entity.getTransferWarehouseNames())) {
            entity.setTransferWarehouseNames(getTransferWarehouseNames(entity.getTransferWarehouseIds()));
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
        Map<String, String> transferWarehouseNameMap = getTransferWarehouseNameMap(list.stream()
                .map(DeliveryDeclareDetailMidDTO.ListDTO::getTransferWarehouseIds)
                .collect(Collectors.toList()));
        for (DeliveryDeclareDetailMidDTO.ListDTO data : list) {
            if(StringUtils.isNotBlank(data.getDeclareStatus())){
                data.setDeclareStatusName(DeclareStatusEnum.getName(data.getDeclareStatus()));
            }else {
                data.setDeclareStatus("not");
                data.setDeclareStatusName("待生成");
            }
            data.setGenerateStatusName(DeliveryDeclareDetailMidGenerateStatusEnum.getName(data.getGenerateStatus()));
            if (CharSequenceUtil.isBlank(data.getTransferWarehouseNames())) {
                data.setTransferWarehouseNames(buildTransferWarehouseNames(data.getTransferWarehouseIds(), transferWarehouseNameMap));
            }
            fillLatestProductLogistic(data, productLogisticMap.get(data.getSkuId()));
        }
    }

    /**
     * 获取中转仓名称
     *
     * @param transferWarehouseIds 中转仓id
     * @return 中转仓名称
     * @throws RuntimeException 远程调用异常时抛出
     * @author jack
     * @date 2026-05-06
     */
    private String getTransferWarehouseNames(String transferWarehouseIds) {
        if (CharSequenceUtil.isBlank(transferWarehouseIds)) {
            return "";
        }
        return buildTransferWarehouseNames(transferWarehouseIds,
                getTransferWarehouseNameMap(Collections.singletonList(transferWarehouseIds)));
    }

    /**
     * 获取中转仓名称映射
     *
     * @param transferWarehouseIdsList 中转仓id集合
     * @return 中转仓名称映射
     * @throws RuntimeException 远程调用异常时抛出
     * @author jack
     * @date 2026-05-06
     */
    @Override
    public Map<String, String> getTransferWarehouseNameMap(List<String> transferWarehouseIdsList) {
        if (CollectionUtils.isEmpty(transferWarehouseIdsList)) {
            return Collections.emptyMap();
        }
        List<String> warehouseIds = transferWarehouseIdsList.stream()
                .filter(CharSequenceUtil::isNotBlank)
                .flatMap(item -> CharSequenceUtil.split(item, ",").stream())
                .filter(CharSequenceUtil::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(warehouseIds)) {
            return Collections.emptyMap();
        }
        List<WarehouseDTO.ListDTO> warehouseList = wmsWarehouseFeign.listByIds(warehouseIds);
        if (CollectionUtils.isEmpty(warehouseList)) {
            return Collections.emptyMap();
        }
        return warehouseList.stream()
                .filter(item -> CharSequenceUtil.isNotBlank(item.getId()))
                .collect(Collectors.toMap(WarehouseDTO.ListDTO::getId,
                        item -> CharSequenceUtil.blankToDefault(item.getName(), ""),
                        (oldValue, newValue) -> oldValue));
    }

    /**
     * 构建中转仓名称
     *
     * @param transferWarehouseIds 中转仓id
     * @param transferWarehouseNameMap 中转仓名称映射
     * @return 中转仓名称
     * @throws RuntimeException 当前方法不主动抛出业务异常
     * @author jack
     * @date 2026-05-06
     */
    @Override
    public String buildTransferWarehouseNames(String transferWarehouseIds, Map<String, String> transferWarehouseNameMap) {
        if (CharSequenceUtil.isBlank(transferWarehouseIds) || CollUtil.isEmpty(transferWarehouseNameMap)) {
            return "";
        }
        List<String> warehouseNameList = new ArrayList<String>();
        for (String warehouseId : CharSequenceUtil.split(transferWarehouseIds, ",")) {
            if (CharSequenceUtil.isBlank(warehouseId)) {
                continue;
            }
            String warehouseName = transferWarehouseNameMap.get(warehouseId);
            if (CharSequenceUtil.isNotBlank(warehouseName)) {
                warehouseNameList.add(warehouseName);
            }
        }
        if (CollectionUtils.isEmpty(warehouseNameList)) {
            return "";
        }
        return String.join(",", warehouseNameList);
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
        data.setLatestCurrency(productLogisticDTO.getDeclareCurrency());
        data.setLatestCurrencySymbol(productLogisticDTO.getDeclareCurrencySymbol());
    }
}
