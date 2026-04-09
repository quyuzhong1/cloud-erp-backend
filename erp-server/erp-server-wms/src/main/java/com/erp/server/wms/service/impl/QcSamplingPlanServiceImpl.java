package com.erp.server.wms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.annotation.DistributeLocker;
import com.common.business.config.DocNoGenHelper;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.business.enums.OperationTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.PagingVO;
import com.common.core.constant.EnumMessage;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.MathUtil;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.dto.AqlSamplingRequest;
import com.erp.model.wms.dto.AqlSamplingResponse;
import com.erp.model.wms.dto.SamplingPlanDTO;
import com.erp.model.wms.dto.SamplingPlanSkuRefDTO;
import com.erp.model.wms.entity.QcSamplingPlanDetailEntity;
import com.erp.model.wms.entity.QcSamplingPlanEntity;
import com.erp.model.wms.entity.QcSamplingPlanQcTypeRefEntity;
import com.erp.model.wms.entity.QcSamplingPlanSkuRefEntity;
import com.erp.model.wms.enums.AqlValueEnum;
import com.erp.model.wms.enums.PlanTypeEnum;
import com.erp.model.wms.enums.QcLevelEnum;
import com.erp.model.wms.enums.QcTypeEnum;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.server.wms.convert.QcSamplingPlanConverter;
import com.erp.server.wms.mapper.QcSamplingPlanMapper;
import com.erp.server.wms.service.*;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 抽样方案表 服务实现类
 * </p>
 *
 * @author zdy
 * @since 2026-03-20
 */
@Slf4j
@Service
public class QcSamplingPlanServiceImpl extends SuperServiceImpl<QcSamplingPlanMapper, QcSamplingPlanEntity> implements QcSamplingPlanService {
    @Resource
    private OperateLogService operateLogService;
    @Resource
    private DocNoGenHelper docNoGenHelper;
    @Resource
    private QcSamplingPlanQcTypeRefService qcSamplingPlanQcTypeRefService;
    @Resource
    private QcSamplingPlanSkuRefService qcSamplingPlanSkuRefService;
    @Resource
    private QcSamplingPlanDetailService qcSamplingPlanDetailService;
    @Resource
    private PlmTaskFeign plmTaskFeign;
    @Resource
    private QcSamplingAqlRuleService qcSamplingAqlRuleService;

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(SamplingPlanDTO.AddDTO addDTO) {
        QcSamplingPlanEntity qcSamplingPlanEntity = new QcSamplingPlanEntity();
        BeanMapperUtils.copy(addDTO, qcSamplingPlanEntity);
        List<QcSamplingPlanSkuRefEntity> skuRefEntities = QcSamplingPlanConverter.INSTANCE.qcSamplingPlanSkuRefToAdd(addDTO.getSkuRefDTOList());
        List<QcSamplingPlanDetailEntity> detailEntities = QcSamplingPlanConverter.INSTANCE.qcSamplingPlanDetailToAdd(addDTO.getDetailList());
        List<QcSamplingPlanQcTypeRefEntity> qcTypeRefEntities = QcSamplingPlanConverter.INSTANCE.qcSamplingPlanQcTypeRefToAdd(addDTO.getQcTypeList());
        // 数据处理
        handleData(qcSamplingPlanEntity, skuRefEntities, detailEntities, qcTypeRefEntities);

        log.info("开始新增抽样方案单");
        // 生成单号
        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_CYFA);
        qcSamplingPlanEntity.setCode(code);
        boolean save = super.save(qcSamplingPlanEntity);
        if (!save) {
            throw new ServiceException("抽样方案单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据单号为【{}】", UserContext.getDefaultLoginUser().getUserName(), "抽样方案单", qcSamplingPlanEntity.getCode());
        //此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.QC_SAMPLING_PLAN.getCode(), qcSamplingPlanEntity.getId(), "新增操作");
        //新增明细（如果有明细的话）
        qcSamplingPlanSkuRefService.updateDetail(skuRefEntities, qcSamplingPlanEntity);
        qcSamplingPlanDetailService.updateDetail(detailEntities, qcSamplingPlanEntity);
        qcSamplingPlanQcTypeRefService.updateDetail(qcTypeRefEntities, qcSamplingPlanEntity);
        return new BaseResultDTO.AddDTO(qcSamplingPlanEntity.getId(), code);
    }

    /**
     * 修改
     */
    @DistributeLocker(keyName = "addOrUpdateDTO.getId()")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(SamplingPlanDTO.UpdateDTO addOrUpdateDTO) {
        QcSamplingPlanEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(() -> new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, "抽样方案单"));
        QcSamplingPlanEntity qcSamplingPlanEntity = BeanMapperUtils.map(QcSamplingPlanEntity.class, addOrUpdateDTO);
        List<QcSamplingPlanSkuRefEntity> skuRefEntities = QcSamplingPlanConverter.INSTANCE.qcSamplingPlanSkuRefToUpdateUpdate(addOrUpdateDTO.getSkuRefDTOList());
        List<QcSamplingPlanDetailEntity> detailEntities = QcSamplingPlanConverter.INSTANCE.qcSamplingPlanDetailToUpdateUpdate(addOrUpdateDTO.getDetailList());
        List<QcSamplingPlanQcTypeRefEntity> qcTypeRefEntities = QcSamplingPlanConverter.INSTANCE.qcSamplingPlanQcTypeRefToUpdateUpdate(addOrUpdateDTO.getQcTypeList());
        // 数据处理
        handleData(qcSamplingPlanEntity, skuRefEntities, detailEntities, qcTypeRefEntities);
        log.info("编辑 开始修改抽样方案单数据，单号：【{}】", old.getCode());
        boolean save = super.updateById(qcSamplingPlanEntity);
        if (!save) {
            throw new ServiceException("抽样方案单保存失败");
        }
        // 修改明细数据（包含增删改）（如果有明细的话）
        qcSamplingPlanSkuRefService.updateDetail(skuRefEntities, qcSamplingPlanEntity);
        qcSamplingPlanDetailService.updateDetail(detailEntities, qcSamplingPlanEntity);
        qcSamplingPlanQcTypeRefService.updateDetail(qcTypeRefEntities, qcSamplingPlanEntity);
        // 记录主单操作日志
        log.info("编辑 开始记录抽样方案单日志数据，单号：【{}】", qcSamplingPlanEntity.getCode());
        String msg = StrUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), qcSamplingPlanEntity.getCode(), "抽样方案单");
        // 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, qcSamplingPlanEntity, ModuleTypeEnum.QC_SAMPLING_PLAN.getCode(), qcSamplingPlanEntity.getId(), msg);
        return Boolean.TRUE;
    }


    @Override
    public PagingVO<SamplingPlanDTO.ListDTO> paging(PagingDTO<SamplingPlanDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<SamplingPlanDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if (CollUtil.isEmpty(pageData.getRecords())) {
            return new PagingVO(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO(pageData);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO updateStatus(String id, Boolean disabled, QcSamplingPlanEntity entity) {
        QcSamplingPlanQcTypeRefEntity qcTypeRefEntity = qcSamplingPlanQcTypeRefService.getById(id);
        if (Objects.isNull(qcTypeRefEntity)) {
            throw new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, "质检类型关联表");
        }
        Boolean dbDisabled = qcTypeRefEntity.getDisabled();
        if (dbDisabled.equals(disabled)) {
            throw new ServiceException(ApiError.COMMON_STATUS_SAME);
        }
        qcTypeRefEntity.setDisabled(disabled);
        qcSamplingPlanQcTypeRefService.updateById(qcTypeRefEntity);
        String msg = CharSequenceUtil.format("用户【{}】修改【{}】的【{}】单据{}操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), QcTypeEnum.getByCode(qcTypeRefEntity.getQcType()), disabled ? "停用" : "启用");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.TRANSFER_LOGISTICS_CHANNEL.getCode(), entity.getId(), "启用/停用");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.DISABLED);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO delete(String id, QcSamplingPlanQcTypeRefEntity qcTypeRefEntity) {
        QcSamplingPlanEntity entity = super.getByIdOpt(qcTypeRefEntity.getMainId()).orElseThrow(() -> new ServiceException("未找到抽样方案单数据"));
        if (!qcTypeRefEntity.getDisabled()) {
            return BatchResultDTO.fail(entity.getId(), entity.getCode(), "启用状态的抽样方案不允许删除");
        }
        // 删除主单数据
        log.info("删除 开始删除抽样方案单数据，id：【{}】", id);
        qcSamplingPlanQcTypeRefService.removeById(id);
        // 删除日志数据
        log.info("删除 开始删除抽样方案单日志数据，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据删除操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), QcTypeEnum.getByCode(qcTypeRefEntity.getQcType()));
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.QC_SAMPLING_PLAN.getCode(), entity.getCode(), "删除抽样方案单数据");
        //不存在对应的抽样类型时，删除全部明细
        List<QcSamplingPlanQcTypeRefEntity> qcTypeRefEntityList = qcSamplingPlanQcTypeRefService.listByMainId(entity.getId());
        if (CollUtil.isEmpty(qcTypeRefEntityList)) {
            this.removeById(entity.getId());
            // 删除明细数据
            qcSamplingPlanSkuRefService.removeByMainId(entity.getId());
            qcSamplingPlanDetailService.removeByMainId(entity.getId());
        }
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.DELETE);
    }

    @Override
    public SamplingPlanDTO.PlanDTO getSamplingPlan(SamplingPlanDTO.PlanParamDTO planDTO) {
        if (CharSequenceUtil.isBlank(planDTO.getSkuNo())) {
            List<SkuVO> skuVOS = plmTaskFeign.listSkuProductByIds(Collections.singletonList(planDTO.getSkuId()));
            if (CollUtil.isEmpty(skuVOS)) {
                throw new ServiceException("产品SKU不存在");
            }
            planDTO.setSkuNo(skuVOS.get(0).getSkuNo());
        }
        //根据质检类型获取方案列表
        List<QcSamplingPlanQcTypeRefEntity> qcTypeRefEntityList = qcSamplingPlanQcTypeRefService.listByQcType(planDTO.getQcType());
        if (CollUtil.isEmpty(qcTypeRefEntityList)) {
            throw new ServiceException(ApiError.PO_QC_SAMPLING_PLAN_QC_TYPE_IS_NULL, QcTypeEnum.getByCode(planDTO.getQcType()));
        }
        List<String> mainIds = qcTypeRefEntityList.stream().map(QcSamplingPlanQcTypeRefEntity::getMainId).distinct().collect(Collectors.toList());
        List<QcSamplingPlanEntity> entityList = this.listByIds(mainIds);
        List<QcSamplingPlanSkuRefEntity> skuRefEntityList = qcSamplingPlanSkuRefService.listByMainIds(mainIds);
        //先按照sku进行匹配，sku不存在时，再按照全量进行匹配
        QcSamplingPlanSkuRefEntity skuRefEntity = skuRefEntityList.stream().filter(e -> e.getSkuId().equals(planDTO.getSkuId())).findFirst().orElse(null);

        // 新增代码：根据匹配结果获取抽样方案
        QcSamplingPlanEntity matchedPlan = null;

        // 1. 优先匹配特定SKU的方案
        if (skuRefEntity != null) {
            matchedPlan = entityList.stream()
                    .filter(e -> e.getId().equals(skuRefEntity.getMainId()))
                    .findFirst()
                    .orElse(null);
        }

        // 2. 如果没有特定SKU的方案，则查找全量适配的方案（没有SKU限制的方案）
        if (matchedPlan == null) {
            // 获取没有SKU限制的方案（即skuRefEntityList中不包含该mainId的方案）
            Set<String> plansWithSkuRestrictions = skuRefEntityList.stream()
                    .map(QcSamplingPlanSkuRefEntity::getMainId)
                    .collect(Collectors.toSet());

            matchedPlan = entityList.stream()
                    .filter(e -> !plansWithSkuRestrictions.contains(e.getId()))
                    .findFirst()
                    .orElse(null);
        }

        if (matchedPlan == null) {
            throw new ServiceException(ApiError.PO_QC_SAMPLING_PLAN_NOT_FOUND,
                    QcTypeEnum.getByCode(planDTO.getQcType()), planDTO.getSkuNo());
        }

        // 获取方案的详细信息
        List<QcSamplingPlanDetailEntity> detailList = qcSamplingPlanDetailService.listByMainId(matchedPlan.getId());
        return calcSamplingPlan(matchedPlan, detailList, planDTO);
    }

    /**
     * 计算抽样方案
     *
     * @param matchedPlan
     * @param detailList
     * @param planDTO
     * @return
     */
    private SamplingPlanDTO.PlanDTO calcSamplingPlan(QcSamplingPlanEntity matchedPlan, List<QcSamplingPlanDetailEntity> detailList, SamplingPlanDTO.PlanParamDTO planDTO) {
        // 构建返回结果
        SamplingPlanDTO.PlanDTO result = new SamplingPlanDTO.PlanDTO();
        result.setId(matchedPlan.getId());
        result.setCode(matchedPlan.getCode());
        result.setQcType(planDTO.getQcType());
        result.setPlanType(matchedPlan.getPlanType());
        result.setQcLevel(matchedPlan.getQcLevel());
        if (PlanTypeEnum.GB.getCode().equals(matchedPlan.getPlanType())) {
            String generalAql = matchedPlan.getGeneralAql();
            result.setGeneralAql(generalAql);
            String majorAql = matchedPlan.getMajorAql();
            result.setMajorAql(majorAql);
            AqlSamplingRequest request = AqlSamplingRequest.builder()
                    .sampleQty(planDTO.getQty())
                    .qcLevel(matchedPlan.getQcLevel())
                    .aqlValue(generalAql)
                    .build();
            //根据方案类型获取抽样方案（一般缺陷）
            AqlSamplingResponse generalSamplingResponse = qcSamplingAqlRuleService.calculateSamplingPlan(request);
            if (generalSamplingResponse == null) {
                throw new ServiceException(ApiError.PO_QC_SAMPLING_PLAN_GENERAL_AQL_IS_NULL, generalAql);
            }
            if (CharSequenceUtil.isNotBlank(generalSamplingResponse.getErrorMsg())){
                throw new ServiceException(generalSamplingResponse.getErrorMsg());
            }
            result.setLotRange(generalSamplingResponse.getLotRange());
            result.setRangFrom(generalSamplingResponse.getRangFrom());
            result.setRangTo(generalSamplingResponse.getRangTo());
            result.setGeneralAcceptQty(generalSamplingResponse.getAcceptQty());
            result.setGeneralRejectQty(generalSamplingResponse.getRejectQty());
            if (MathUtil.compareTo(generalAql, majorAql) <= 0){
                result.setSampleQty(generalSamplingResponse.getSampleQty());
            }
            //根据方案类型获取抽样方案（严重缺陷）
            request.setAqlValue(majorAql);
            AqlSamplingResponse majorSamplingResponse = qcSamplingAqlRuleService.calculateSamplingPlan(request);
            if (majorSamplingResponse == null) {
                throw new ServiceException(ApiError.PO_QC_SAMPLING_PLAN_MAJOR_AQL_IS_NULL, majorAql);
            }
            if (CharSequenceUtil.isNotBlank(majorSamplingResponse.getErrorMsg())){
                throw new ServiceException(majorSamplingResponse.getErrorMsg());
            }
            if (MathUtil.compareTo(generalAql, majorAql) > 0){
                result.setSampleQty(majorSamplingResponse.getSampleQty());
            }
            result.setMajorAcceptQty(majorSamplingResponse.getAcceptQty());
            result.setMajorRejectQty(majorSamplingResponse.getRejectQty());
            return result;
        }

        //根据数量获取对应的抽样明细方案
        QcSamplingPlanDetailEntity detailEntity = null;
        if (CollUtil.isNotEmpty(detailList)) {
            detailEntity = detailList.stream().filter(e -> e.getRangFrom() <= planDTO.getQty() && planDTO.getQty() <= e.getRangTo()).findFirst().orElse(null);
        }
        if (detailEntity == null) {
            throw new ServiceException(ApiError.PO_QC_SAMPLING_PLAN_DETAIL_FOUND,
                    QcTypeEnum.getByCode(planDTO.getQcType()), planDTO.getSkuNo());
        }
        if (PlanTypeEnum.ALL.getCode().equals(matchedPlan.getPlanType()) || PlanTypeEnum.FIXED.getCode().equals(matchedPlan.getPlanType())){
            result.setSampleQty( Objects.nonNull(detailEntity.getQty()) && detailEntity.getQty() < planDTO.getQty() ? detailEntity.getQty() : planDTO.getQty());
        }else if (PlanTypeEnum.RATE.getCode().equals(matchedPlan.getPlanType())){
            result.setRate(detailEntity.getRate());
            result.setSampleQty(MathUtil.multiplyWithTwo(MathUtil.divide(new BigDecimal(planDTO.getQty()), MathUtil.BigDecimal_100), detailEntity.getRate() , 0).intValue());
        }
        result.setRangFrom(detailEntity.getRangFrom());
        result.setRangTo(detailEntity.getRangTo());
        result.setLotRange(detailEntity.getRangFrom() + "~" + detailEntity.getRangTo());
        result.setGeneralAcceptQty(detailEntity.getGeneralAcceptQty());
        result.setGeneralRejectQty(detailEntity.getGeneralRejectQty());
        result.setMajorAcceptQty(detailEntity.getMajorAcceptQty());
        result.setMajorRejectQty(detailEntity.getMajorRejectQty());
        return result;
    }

    /**
     * 新增修改处理数据
     */
    private void handleData(QcSamplingPlanEntity qcSamplingPlanEntity,
                            List<QcSamplingPlanSkuRefEntity> skuRefEntities,
                            List<QcSamplingPlanDetailEntity> detailEntities,
                            List<QcSamplingPlanQcTypeRefEntity> qcTypeRefEntities) {
        //数据重复校验 根据质检类型获取已存在的方案
        List<String> qcTypeList = qcTypeRefEntities.stream().map(QcSamplingPlanQcTypeRefEntity::getQcType).distinct().collect(Collectors.toList());
        if (qcTypeRefEntities.size() != qcTypeList.size()) {
            throw new ServiceException("单次新增质检类型不能重复");
        }
        //sku不能重复
        if (CollUtil.isEmpty(skuRefEntities)) {
            List<String> skuIds = skuRefEntities.stream().map(QcSamplingPlanSkuRefEntity::getSkuId).filter(CharSequenceUtil::isNotBlank).collect(Collectors.toList());
            if (CollUtil.isNotEmpty(skuIds)) {
                List<String> skuIds2 = skuIds.stream().distinct().collect(Collectors.toList());
                if (skuIds.size() != skuIds2.size()) {
                    throw new ServiceException("单次新增SKU不能重复");
                }
            }
        }
        List<SamplingPlanDTO.ListDTO> existList = baseMapper.listByQcTypeList(qcTypeList);

        if (CharSequenceUtil.isNotBlank(qcSamplingPlanEntity.getId())) {
            //过滤当前方案
            existList.removeIf(item -> item.getId().equals(qcSamplingPlanEntity.getId()));
        }
        if (CollUtil.isNotEmpty(existList)) {
            List<String> existIdList = existList.stream().map(SamplingPlanDTO.ListDTO::getId).collect(Collectors.toList());
            List<QcSamplingPlanSkuRefEntity> existSkuRefEntities = qcSamplingPlanSkuRefService.listByMainIds(existIdList);
            Set<String> skuSet = CollUtil.isEmpty(skuRefEntities) ? Collections.emptySet() : skuRefEntities.stream().map(QcSamplingPlanSkuRefEntity::getSkuId).collect(Collectors.toSet());
            existList.forEach(item -> {
                List<QcSamplingPlanSkuRefEntity> existSkuRefEntityList = existSkuRefEntities.stream().filter(existSkuRefEntity -> existSkuRefEntity.getMainId().equals(item.getId())).collect(Collectors.toList());
                if (CollUtil.isEmpty(existSkuRefEntityList)) {
                    //为空时，代表全量适配 且这次不能是全量适配
                    if (CollUtil.isEmpty(skuRefEntities)) {
                        throw new ServiceException(ApiError.PO_QC_QUALITY_CONTROL_TYPE_ALREADY_EXISTS, EnumMessage.getNameByCode(QcTypeEnum.class, item.getQcType()));
                    }
                } else {
                    //部分适配
                    if (CollUtil.isNotEmpty(skuSet)) {
                        //校验同类型单据种sku是否存在交集
                        Set<String> existSkuSet = existSkuRefEntityList.stream().map(QcSamplingPlanSkuRefEntity::getSkuId).collect(Collectors.toSet());
                        Collection<String> intersection = CollectionUtil.intersection(skuSet, existSkuSet);
                        if (CollUtil.isNotEmpty(intersection)) {
                            throw new ServiceException(ApiError.PO_QC_QUALITY_CONTROL_TYPE_EXISTS_PARTIAL_SKU, EnumMessage.getNameByCode(QcTypeEnum.class, item.getQcType()), String.join(",", intersection));
                        }
                    }
                }
            });
        }
        if (PlanTypeEnum.GB.getCode().equals(qcSamplingPlanEntity.getPlanType())) {
            if (CharSequenceUtil.isBlank(qcSamplingPlanEntity.getQcLevel())) {
                throw new ServiceException(ApiError.PO_QC_SAMPLING_PLAN_DETAIL_QC_LEVEL_INVALID);
            }
            if (!StringUtils.hasText(qcSamplingPlanEntity.getMajorAql())) {
                throw new ServiceException(ApiError.PO_QC_SAMPLING_PLAN_DETAIL_MAJOR_AQL_INVALID);
            }
            if (!AqlValueEnum.isValidAql(qcSamplingPlanEntity.getMajorAql())) {
                throw new ServiceException(ApiError.PO_QC_SAMPLING_PLAN_DETAIL_MAJOR_AQL_ENUM_INVALID);
            }
            if (!StringUtils.hasText(qcSamplingPlanEntity.getGeneralAql())) {
                throw new ServiceException(ApiError.PO_QC_SAMPLING_PLAN_DETAIL_GENERAL_AQL_INVALID);
            }
            if (!AqlValueEnum.isValidAql(qcSamplingPlanEntity.getGeneralAql())) {
                throw new ServiceException(ApiError.PO_QC_SAMPLING_PLAN_DETAIL_GENERAL_AQL_ENUM_INVALID);
            }
            if (CollUtil.isNotEmpty(detailEntities)) {
                throw new ServiceException(ApiError.PO_QC_SAMPLING_PLAN_DETAIL_DETAIL_EMPTY);
            }
        } else if (PlanTypeEnum.FIXED.getCode().equals(qcSamplingPlanEntity.getPlanType())) {
            validateNotAql(qcSamplingPlanEntity);
            if (CollUtil.isEmpty(detailEntities)) {
                throw new ServiceException(ApiError.PO_QC_SAMPLING_PLAN_DETAIL_DETAIL_NOT_EMPTY);
            }
            //抽样数量不能为空
            detailEntities.forEach(detail -> {
                if (Objects.isNull(detail.getQty()) || detail.getQty() <= 0) {
                    throw new ServiceException(ApiError.PO_QC_SAMPLING_PLAN_DETAIL_QTY_INVALID);
                }
                if (detail.getQty() > detail.getRangTo()) {
                    throw new ServiceException(ApiError.PO_QC_SAMPLING_PLAN_DETAIL_QTY_EXCEEDS);
                }

            });
        } else if (PlanTypeEnum.RATE.getCode().equals(qcSamplingPlanEntity.getPlanType())) {
            validateNotAql(qcSamplingPlanEntity);
            if (CollUtil.isEmpty(detailEntities)) {
                throw new ServiceException(ApiError.PO_QC_SAMPLING_PLAN_DETAIL_DETAIL_NOT_EMPTY);
            }
            //抽样比例不能为空
            detailEntities.forEach(detail -> {
                if (Objects.isNull(detail.getRate()) || detail.getRate().compareTo(BigDecimal.ZERO) < 0 || detail.getRate().compareTo(MathUtil.BigDecimal_100) > 0) {
                    throw new ServiceException(ApiError.PO_QC_SAMPLING_PLAN_DETAIL_RATE_INVALID);
                }
            });
        } else if (PlanTypeEnum.ALL.getCode().equals(qcSamplingPlanEntity.getPlanType())) {
            validateNotAql(qcSamplingPlanEntity);
            if (CollUtil.isEmpty(detailEntities)) {
                throw new ServiceException(ApiError.PO_QC_SAMPLING_PLAN_DETAIL_DETAIL_NOT_EMPTY);
            }
        }
        //抽样数量校验
        if (CollUtil.isNotEmpty(detailEntities)) {
            //明细范围必须连续，且为整数
            //明细范围前后值不允许一致
            //明细范围只能输入大于等于0的整数
            //1.根据起始值排序
            detailEntities.sort(Comparator.comparing(QcSamplingPlanDetailEntity::getRangFrom));
            for (int i = 0; i < detailEntities.size(); i++) {
                QcSamplingPlanDetailEntity detail = detailEntities.get(i);
                // 2基础校验
                validateRange(detail.getRangFrom(), detail.getRangTo());
                //3连续性校验
                if (i > 0) {
                    QcSamplingPlanDetailEntity prev = detailEntities.get(i - 1);
                    // 连续规则：前一个 end + 1 == 当前 start
                    if (!detail.getRangFrom().equals(prev.getRangTo() + 1)) {
                        throw new ServiceException(ApiError.PO_QC_SAMPLING_PLAN_DETAIL_RANGE_NOT_CONTINUOUS, prev.getRangFrom(), prev.getRangTo(), detail.getRangFrom(), detail.getRangTo());
                    }
                }
            }
            detailEntities.forEach(detail -> {
                if (Objects.isNull(detail.getGeneralRejectQty())) {
                    //缺陷拒收数默认等于缺陷允收数+1
                    detail.setGeneralRejectQty(detail.getGeneralAcceptQty() + 1);
                }
                if (Objects.isNull(detail.getMajorRejectQty())) {
                    //严重缺陷拒收数默认等于严重缺陷允收数+1
                    detail.setMajorRejectQty(detail.getMajorAcceptQty() + 1);
                }
            });
        }
    }

    private void validateNotAql(QcSamplingPlanEntity qcSamplingPlanEntity) {
        if (CharSequenceUtil.isNotBlank(qcSamplingPlanEntity.getMajorAql())) {
            throw new ServiceException(ApiError.PO_QC_SAMPLING_PLAN_DETAIL_MAJOR_AQL_EMPTY);
        }
        if (CharSequenceUtil.isNotBlank(qcSamplingPlanEntity.getGeneralAql())) {
            throw new ServiceException(ApiError.PO_QC_SAMPLING_PLAN_DETAIL_GENERAL_AQL_EMPTY);
        }
    }

    public static void validateRange(Integer start, Integer end) {
        if (start == null || end == null) {
            throw new ServiceException(ApiError.PO_QC_SAMPLING_PLAN_DETAIL_RANGE_INVALID);
        }

        if (start < 0 || end < 0) {
            throw new ServiceException(ApiError.PO_QC_SAMPLING_PLAN_DETAIL_RANGE_LESS_THAN_ZERO);
        }

        if (start.equals(end)) {
            throw new ServiceException(ApiError.PO_QC_SAMPLING_PLAN_DETAIL_RANGE_EQUAL);
        }

        if (start > end) {
            throw new ServiceException(ApiError.PO_QC_SAMPLING_PLAN_DETAIL_RANGE_GREATER_THAN_END);
        }
    }

    @Override
    public SamplingPlanDTO.ViewDTO view(String id) {
        QcSamplingPlanQcTypeRefEntity qcTypeRefEntity = qcSamplingPlanQcTypeRefService.getById(id);
        if (ObjectUtil.isEmpty(qcTypeRefEntity)) {
            throw new ServiceException(ApiError.PO_QC_SAMPLING_PLAN_DETAIL_QC_TYPE_NOT_FOUND);
        }
        QcSamplingPlanEntity qcSamplingPlanEntity = super.getByIdOpt(qcTypeRefEntity.getMainId()).orElseThrow(() -> new ServiceException("未找到抽样方案单数据"));
        SamplingPlanDTO.ViewDTO data = QcSamplingPlanConverter.INSTANCE.qcSamplingPlanEntityToViewDTO(qcSamplingPlanEntity);
        // 数据填充处理
        fillOne(data);
        return data;
    }

    private void fillOne(SamplingPlanDTO.ViewDTO data) {
        if (ObjectUtil.isEmpty(data)) {
            return;
        }
        List<QcSamplingPlanQcTypeRefEntity> qcTypeList = qcSamplingPlanQcTypeRefService.listByMainId(data.getId());
        data.setQcTypeList(QcSamplingPlanConverter.INSTANCE.qcSamplingPlanQcTypeRefEntityToViewDTO(qcTypeList));
        List<QcSamplingPlanSkuRefEntity> skuRefList = qcSamplingPlanSkuRefService.listByMainId(data.getId());
        data.setSkuRefDTOList(QcSamplingPlanConverter.INSTANCE.qcSamplingPlanSkuRefEntityToViewDTO(skuRefList));
        List<QcSamplingPlanDetailEntity> detailList = qcSamplingPlanDetailService.listByMainId(data.getId());
        data.setDetailList(QcSamplingPlanConverter.INSTANCE.qcSamplingPlanDetailEntityToViewDTO(detailList));
    }

    /**
     * 分页查询、导出 数据处理
     */
    private void fillList(List<SamplingPlanDTO.ListDTO> list) {
        if (CollUtil.isEmpty(list)) {
            return;
        }
        List<String> ids = list.stream().map(SamplingPlanDTO.ListDTO::getId).distinct().collect(Collectors.toList());
        List<SamplingPlanSkuRefDTO.SkuDTO> skuRefList = qcSamplingPlanSkuRefService.listSkuByMainIds(ids);
        Map<String, String> skuNosMap = CollUtil.isNotEmpty(skuRefList) ? skuRefList.stream().collect(Collectors.toMap(SamplingPlanSkuRefDTO.SkuDTO::getId, SamplingPlanSkuRefDTO.SkuDTO::getSkuNos)) : new HashMap<>();

        // 属性赋值
        for (SamplingPlanDTO.ListDTO data : list) {
            data.setQcTypeName(EnumMessage.getNameByCode(QcTypeEnum.class, data.getQcType()));
            data.setPlanTypeName(EnumMessage.getNameByCode(PlanTypeEnum.class, data.getPlanType()));
            data.setQcLevelName(EnumMessage.getNameByCode(QcLevelEnum.class, data.getQcLevel()));
            String skuNos = skuNosMap.get(data.getId());
            data.setSkuNos(CharSequenceUtil.isNotBlank(skuNos) ? skuNos : "全部");
        }
    }
}
