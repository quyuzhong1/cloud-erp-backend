package com.erp.server.tms.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.OperationTypeEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.constant.EnumMessage;
import com.common.core.entity.BaseEntity;
import com.erp.model.oms.entity.SoReturnDetailEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.sys.entity.DictCountryEntity;
import com.erp.model.tms.dto.TmsB2cDeclareReconciliationDetailDTO;
import com.erp.model.tms.dto.TmsCfgCostDTO;
import com.erp.model.tms.dto.TmsFirstMileReconciliationDTO;
import com.erp.model.tms.entity.*;
import com.erp.model.tms.enums.*;
import com.erp.model.wms.enums.LogisticsMethodEnum;
import com.erp.rpc.sys.feign.SysDictFeign;
import com.erp.server.tms.mapper.TmsFirstMileReconciliationDetailMapper;
import com.erp.server.tms.service.*;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.exception.ServiceException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.tms.dto.TmsFirstMileReconciliationDetailDTO;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.common.core.utils.*;
import com.common.core.enums.ApiError;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 * 头程对账单明细 服务实现类
 * </p>
 *
 * @author Jim
 * @since 2024-03-25
 */
@Slf4j
@Service
public class TmsFirstMileReconciliationDetailServiceImpl extends SuperServiceImpl<TmsFirstMileReconciliationDetailMapper, TmsFirstMileReconciliationDetailEntity> implements TmsFirstMileReconciliationDetailService {
    @Resource
    private OperateLogService operateLogService;
    @Resource
    private CommonService commonService;
    @Lazy
    @Resource
    private ShippingTemplateService shippingTemplateService;
    @Resource
    private SysDictFeign sysDictFeign;
    @Resource
    private TmsCfgCostService tmsCfgCostService;
    @Resource
    private TmsCostDetailService tmsCostDetailService;
    @Lazy
    @Resource
    private TmsFirstMileLogisticService tmsFirstMileLogisticService;


    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(TmsFirstMileReconciliationDetailDTO.AddDTO addDTO) {
        TmsFirstMileReconciliationDetailEntity tmsFirstMileReconciliationDetailEntity = new TmsFirstMileReconciliationDetailEntity();
        BeanMapperUtils.copy(addDTO, tmsFirstMileReconciliationDetailEntity);

        // 数据处理
        handleData(tmsFirstMileReconciliationDetailEntity);

        log.info("开始新增头程对账单明细");
        boolean save = super.save(tmsFirstMileReconciliationDetailEntity);
        if (!save) {
            throw new ServiceException("头程对账单明细保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", commonService.getUserInfo().getUserName(), "头程对账单明细", tmsFirstMileReconciliationDetailEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, tmsFirstMileReconciliationDetailEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(tmsFirstMileReconciliationDetailEntity.getId(), tmsFirstMileReconciliationDetailEntity.getId());
    }

    /**
     * 修改
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(TmsFirstMileReconciliationDetailDTO.UpdateDTO updateDTO) {
        TmsFirstMileReconciliationDetailEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "头程对账单明细"));
        TmsFirstMileReconciliationDetailEntity tmsFirstMileReconciliationDetailEntity = BeanMapperUtils.map(TmsFirstMileReconciliationDetailEntity.class, updateDTO);

        // 数据处理
        handleData(tmsFirstMileReconciliationDetailEntity);
        log.info("编辑 开始修改头程对账单明细数据，id：【{}】", old.getId());
        boolean save = super.updateById(tmsFirstMileReconciliationDetailEntity);
        if (!save) {
            throw new ServiceException("头程对账单明细保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
        log.info("编辑 开始记录头程对账单明细日志数据，id：【{}】", tmsFirstMileReconciliationDetailEntity.getId());
        String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", commonService.getUserInfo().getUserName(), tmsFirstMileReconciliationDetailEntity.getId(), "头程对账单明细");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, tmsFirstMileReconciliationDetailEntity, null, tmsFirstMileReconciliationDetailEntity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    public PagingVO<TmsFirstMileReconciliationDetailDTO.ListDTO> paging(PagingDTO<TmsFirstMileReconciliationDetailDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page<?> query = new Page<>(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<TmsFirstMileReconciliationDetailDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if (CollUtil.isEmpty(pageData.getRecords())) {
            return new PagingVO<>(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO<>(pageData);
    }

    private void fillList(List<TmsFirstMileReconciliationDetailDTO.ListDTO> records) {
        // 查询

        for (TmsFirstMileReconciliationDetailDTO.ListDTO record : records) {


        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO updateStatus(String id, String status) {
        TmsFirstMileReconciliationDetailEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到头程对账单明细数据"));
        if (!StrUtil.equals(entity.getStatus(), ReconciliationStatusEnum.TO_BE_CONFIRM.getCode())) {
            throw new ServiceException("只有待对账数据支持更新对账");
        }
        lambdaUpdate().eq(TmsFirstMileReconciliationDetailEntity::getId, id)
                .set(TmsFirstMileReconciliationDetailEntity::getStatus, status)
                .update();
        // 记录主单操作日志
        operateLogService.addModuleOperateLog(StrUtil.format("头程对账单【{}】更新状态为【{}】", entity.getSourceCode(), TmsB2cDeclareReconciliationStatusEnum.getName(status)), ModuleTypeEnum.TMS_FIRST_MILE_RECONCILIATION.getCode(), entity.getId(), "更新状态操作");
        return BatchResultDTO.success(entity.getId(), entity.getSourceCode(), OperationTypeEnum.UPDATE_STATUS);
    }

    @Override
    public TmsFirstMileReconciliationDetailDTO.ImportDTO importFile(TmsB2cDeclareReconciliationDetailDTO.ExcelImportDTO excelImportDTO, HttpServletResponse response) {
        return null;
    }

    @Override
    public PagingVO<TmsFirstMileReconciliationDetailDTO.ListDTO> waitReconciliationPaging(PagingDTO<TmsFirstMileReconciliationDetailDTO.PagingParamDTO> pagingParamDTO) {
        // 查询已签收（待对账）
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page<?> query = new Page<>(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<TmsFirstMileReconciliationDetailDTO.ListDTO> pageData = tmsFirstMileLogisticService.waitReconciliationPaging(query, pagingParamDTO.getParams());
        if (CollUtil.isEmpty(pageData.getRecords())) {
            return new PagingVO<>(pageData);
        }
        // 数据处理
        fillWaitReconciliationPaging(pageData.getRecords());
        return new PagingVO<>(pageData);
    }

    @Override
    public void exportList(TmsFirstMileReconciliationDetailDTO.ExportDTO dto, HttpServletResponse response) {

    }

    @Override
    public List<TmsFirstMileReconciliationDetailEntity> listByMainIds(List<String> mainIds) {
        return lambdaQuery()
                .in(TmsFirstMileReconciliationDetailEntity::getMainId, mainIds)
                .list();
    }

    private void fillWaitReconciliationPaging(List<TmsFirstMileReconciliationDetailDTO.ListDTO> records) {
        // 统计预计费用
        List<String> logisticsBillIds = records.stream()
                .map(TmsFirstMileReconciliationDetailDTO.ListDTO::getSourceId)
                .distinct()
                .collect(Collectors.toList());
        List<TmsCostDetailEntity> costList = tmsCostDetailService.query()
                .select("SUM(COALESCE(cost_value,0)) as cost_value", TmsCostDetailEntity.MAIN_ID, TmsCostDetailEntity.CFG_COST_ID)
                .eq(TmsCostDetailEntity.TYPE, LogisticsBillCostTypeEnum.ESTIMATED.getCode())
                .in(TmsCostDetailEntity.MAIN_ID, logisticsBillIds)
                .groupBy(TmsCostDetailEntity.MAIN_ID, TmsCostDetailEntity.CFG_COST_ID)
                .list();
        
        // 计费方式
        List<String> channelIds = records.stream().map(TmsFirstMileReconciliationDetailDTO.ListDTO::getLogisticsChannelId).distinct().collect(Collectors.toList());
        List<ShippingTemplateEntity> templateList = shippingTemplateService.getByChannelIds(channelIds);
        Map<String, List<ShippingTemplateEntity>> templateMap = templateList.stream()
                .collect(Collectors.groupingBy(ShippingTemplateEntity::getLogisticsChannelId));
        
        //国家信息
        List<String> countryIdList = records.stream()
                .flatMap(route -> Stream.of(route.getFromCountry(), route.getToCountry()))
                .filter(StringUtils::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        Map<String, String> countryMap = new HashMap<>();
        if (!CollectionUtils.isEmpty(countryIdList)){
            countryMap = sysDictFeign.listCountryByIds(countryIdList)
                    .stream()
                    .collect(Collectors.toMap(BaseEntity::getId, DictCountryEntity::getNameCn));
        }
        // 头程物流费用配置
        List<TmsCfgCostDTO.DropDownDTO> cfgCostList = tmsCfgCostService.listDropDown(new TmsCfgCostDTO.DropDownParamDTO(DictCostAttributionEnum.FIRST_MILE.getCode()));

        for (TmsFirstMileReconciliationDetailDTO.ListDTO record : records) {
            record.setSourceType(SourceTypeEnum.LOGISTICS_BILL.getCode());
            // 出库单号=发货单号
            // 待对账类型都是预估
            record.setType(DetailReconciliationTypeEnum.ESTIMATED.getCode());
            // 补充单位
            if (StringUtils.isBlank(record.getVolumeWeightUnit())){
                record.setVolumeWeightUnit(record.getActualWeightUnit());
            }
            if (StringUtils.isBlank(record.getBillingWeightUnit())){
                record.setBillingWeightUnit(record.getActualWeightUnit());
            }

            // 运输状态
            FmLogisticTrackStatusEnum statusEnum = FmLogisticTrackStatusEnum.getNameByCode(record.getTransportStatus());
            record.setTransportStatusName(null == statusEnum ? "" : statusEnum.getName());
            // 计费方式
            List<ShippingTemplateEntity> shippingTemplateList = templateMap.get(record.getLogisticsChannelId());
            if (!CollectionUtils.isEmpty(shippingTemplateList)){
                ShippingTemplateEntity shippingTemplateEntity = shippingTemplateList.stream().findFirst().orElse(null);
                record.setBillingMethod(shippingTemplateEntity.getBillingMethod());
                record.setBillingMethodName(EnumMessage.getNameByCode(ShippingBillingMethodEnum.class, shippingTemplateEntity.getBillingMethod()));
            } else {
                record.setBillingMethod("");
                record.setBillingMethodName("");
            }
            record.setToCountryName(countryMap.getOrDefault(record.getToCountry(),""));
            record.setFromCountryName(countryMap.getOrDefault(record.getFromCountry(),""));

            // 按配置分组统计费用
            List<TmsCostDetailEntity> currentCostList = costList.stream().filter(e -> e.getMainId().equalsIgnoreCase(record.getSourceId())).collect(Collectors.toList());
            Map<String, BigDecimal> costIdSumMap = currentCostList.stream()
                    .collect(Collectors.groupingBy(TmsCostDetailEntity::getCfgCostId,
                            Collectors.reducing(BigDecimal.ZERO, TmsCostDetailEntity::getCostValue, BigDecimal::add)));

            List<TmsFirstMileReconciliationDetailDTO.CostInfoDTO> curCostListDTO = cfgCostList.stream()
                    .map(e -> new TmsFirstMileReconciliationDetailDTO.CostInfoDTO(e.getId(), e.getCostName(), costIdSumMap.getOrDefault(e.getId(), BigDecimal.ZERO)))
                    .collect(Collectors.toList());
            record.setCostList(curCostListDTO);

            // 合计费用
            BigDecimal totalCost = costIdSumMap.values().stream().reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
            record.setTotalLogisticsCost(totalCost);

            // 对账状态
            TmsB2cDeclareReconciliationStatusEnum status = TmsB2cDeclareReconciliationStatusEnum.WAIT_CONFIRM;
            record.setStatus(status.getCode());
            record.setStatusName(status.getName());
        }

    }


    /**
     * 新增修改处理数据
     */
    private void handleData(TmsFirstMileReconciliationDetailEntity tmsFirstMileReconciliationDetailEntity) {
        // TODO 验证数据 & 数据赋值
    }
}
