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
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.PagingVO;
import com.common.core.constant.EnumMessage;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.dto.SamplingPlanDTO;
import com.erp.model.wms.entity.QcSamplingPlanDetailEntity;
import com.erp.model.wms.entity.QcSamplingPlanEntity;
import com.erp.model.wms.entity.QcSamplingPlanQcTypeRefEntity;
import com.erp.model.wms.entity.QcSamplingPlanSkuRefEntity;
import com.erp.model.wms.enums.PlanTypeEnum;
import com.erp.model.wms.enums.QcLevelEnum;
import com.erp.model.wms.enums.QcTypeEnum;
import com.erp.server.wms.convert.QcSamplingPlanConverter;
import com.erp.server.wms.mapper.QcSamplingPlanMapper;
import com.erp.server.wms.service.*;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public BatchResultDTO updateStatus(String id, Boolean disabled) {
        return null;
    }

    @Override
    public BatchResultDTO delete(String id) {
        return null;
    }

    /**
     * 新增修改处理数据
     */
    private void handleData(QcSamplingPlanEntity qcSamplingPlanEntity,
                            List<QcSamplingPlanSkuRefEntity> skuRefEntities,
                            List<QcSamplingPlanDetailEntity> detailEntities,
                            List<QcSamplingPlanQcTypeRefEntity> qcTypeRefEntities) {
        //数据重复校验 根据质检类型获取已存在的方案
        List<SamplingPlanDTO.ListDTO> existList = baseMapper.listByQcTypeList(qcTypeRefEntities.stream().map(QcSamplingPlanQcTypeRefEntity::getQcType).collect(Collectors.toList()));
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
                    //为空时，代表全量适配
                    throw new ServiceException(ApiError.PO_QC_QUALITY_CONTROL_TYPE_ALREADY_EXISTS, EnumMessage.getNameByCode(QcTypeEnum.class, item.getQcType()));
                } else {
                    //部分适配
                    if (CollUtil.isEmpty(skuRefEntities)) {
                        throw new ServiceException(ApiError.PO_QC_QUALITY_CONTROL_TYPE_EXISTS_PARTIAL, EnumMessage.getNameByCode(QcTypeEnum.class, item.getQcType()));
                    } else {
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
                throw new ServiceException("国标AQL表检验水平不能为空");
            }
            if (Objects.isNull(qcSamplingPlanEntity.getMajorAql())) {
                throw new ServiceException("国标AQL表 严重缺陷AQL不能为空");
            }
            if (Objects.isNull(qcSamplingPlanEntity.getGeneralAql())) {
                throw new ServiceException("国标AQL表 一般缺陷AQL 不能为空");
            }
            if (CollUtil.isNotEmpty(detailEntities)) {
                throw new ServiceException("国标AQL表 抽样明细要为空");
            }
        } else if (PlanTypeEnum.FIXED.getCode().equals(qcSamplingPlanEntity.getPlanType())) {
            if (CollUtil.isEmpty(detailEntities)) {
                throw new ServiceException("固定抽样方案表 不能为空");
            }
            //抽样数量不能为空
            detailEntities.forEach(detail -> {
                if (Objects.isNull(detail.getQty()) || detail.getQty() <= 0) {
                    throw new ServiceException("固定抽样方案表 抽样数量只能输入大于0的整数");
                }
                if (detail.getQty() > (detail.getRangTo() - detail.getRangFrom())) {
                    throw new ServiceException("固定抽样方案表 抽样数量不能大于批量范围");
                }
                if (Objects.isNull(detail.getGeneralRejectQty())) {
                    //缺陷拒收数默认等于缺陷允收数+1
                    detail.setGeneralRejectQty(detail.getGeneralAcceptQty() + 1);
                }
                if (Objects.isNull(detail.getMajorRejectQty())) {
                    //严重缺陷拒收数默认等于严重缺陷允收数+1
                    detail.setMajorRejectQty(detail.getMajorAcceptQty() + 1);
                }
            });
        } else if (PlanTypeEnum.RATE.getCode().equals(qcSamplingPlanEntity.getPlanType())) {
            if (CollUtil.isEmpty(detailEntities)) {
                throw new ServiceException("百分比抽样方案表 不能为空");
            }
            //抽样比例不能为空
            detailEntities.forEach(detail -> {
                if (Objects.isNull(detail.getRate()) || detail.getRate().compareTo(BigDecimal.ZERO) <= 0 || detail.getRate().compareTo(BigDecimal.ONE) >= 0) {
                    throw new ServiceException("百分比抽样方案表 抽样比例只能输入大于0的小数且小于1");
                }
                if (Objects.isNull(detail.getGeneralRejectQty())) {
                    //缺陷拒收数默认等于缺陷允收数+1
                    detail.setGeneralRejectQty(detail.getGeneralAcceptQty() + 1);
                }
                if (Objects.isNull(detail.getMajorRejectQty())) {
                    //严重缺陷拒收数默认等于严重缺陷允收数+1
                    detail.setMajorRejectQty(detail.getMajorAcceptQty() + 1);
                }
            });
        } else if (PlanTypeEnum.ALL.getCode().equals(qcSamplingPlanEntity.getPlanType())) {
            if (CollUtil.isEmpty(detailEntities)) {
                throw new ServiceException("全检抽样方案表 不能为空");
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

    @Override
    public SamplingPlanDTO.ViewDTO view(String id) {
        QcSamplingPlanEntity qcSamplingPlanEntity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到抽样方案单数据"));
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
        // 属性赋值
        for (SamplingPlanDTO.ListDTO data : list) {
            data.setQcTypeName(EnumMessage.getNameByCode(QcTypeEnum.class, data.getQcType()));
            data.setPlanTypeName(EnumMessage.getNameByCode(PlanTypeEnum.class, data.getPlanType()));
            data.setQcLevelName(EnumMessage.getNameByCode(QcLevelEnum.class, data.getQcLevel()));
        }
    }
}
