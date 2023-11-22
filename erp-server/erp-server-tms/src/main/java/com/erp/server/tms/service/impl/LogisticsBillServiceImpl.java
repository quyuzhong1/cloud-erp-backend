package com.erp.server.tms.service.impl;


import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.utils.date.DateUtil;
import com.erp.model.oms.entity.SoB2cLogisticsEntity;
import com.erp.model.plm.enums.ProductSalesPlatformEnum;
import com.erp.model.tms.dto.*;
import com.erp.model.tms.entity.*;
import com.erp.model.tms.enums.DictBasicEnum;
import com.erp.model.tms.enums.LogisticTrackStatusEnum;
import com.erp.rpc.oms.feign.OmsTaskFeign;
import com.erp.rpc.oms.feign.SoB2cFeign;
import com.erp.server.tms.constant.TmsConstant;
import com.erp.server.tms.mapper.LogisticsBillMapper;
import com.erp.server.tms.service.*;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.exception.ServiceException;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.tms.dto.LogisticsBillDTO;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import com.common.core.utils.*;
import com.common.core.enums.ApiError;

import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;

/**
 * <p>
 * 物流单 服务实现类
 * </p>
 *
 * @author lambda
 * @since 2023-11-09
 */
@Slf4j
@Service
public class LogisticsBillServiceImpl extends SuperServiceImpl<LogisticsBillMapper, LogisticsBillEntity> implements LogisticsBillService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private CommonService commonService;
    @Autowired
    private LogisticsBillDetailService logisticsBillDetailService;

    @Autowired
    private DictBasicService dictBasicService;


    @Autowired
    private LogisticsTrackService logisticsTrackService;

    @Autowired
    private LogisticsBillCostService logisticsBillCostService;

    @Autowired
    private SoB2cFeign soB2cFeign;

    @Autowired
    private ShippingTemplateService shippingTemplateService;

    @Autowired
    private ShippingCalculationService shippingCalculationService;

    @Autowired
    private ShippingTemplateRuleService shippingTemplateRuleService;


    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean add(LogisticsBillDTO.AddDTO addDTO) {
        LogisticsBillEntity logisticsBillEntity = new LogisticsBillEntity();
        BeanMapperUtils.copy(addDTO, logisticsBillEntity);
        // 数据处理
        handleData(logisticsBillEntity);
        boolean save = super.saveOrUpdate(logisticsBillEntity);
        if (!save) {
            throw new ServiceException("物流单保存失败");
        }

        logisticsBillDetailService.add(logisticsBillEntity,addDTO.getDetailList());

        //新增物流费用单
        addLogisticsBillCost(logisticsBillEntity);
        return save;
    }


    /**
     * 修改
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(LogisticsBillDTO.UpdateDTO updateDTO) {
        LogisticsBillEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "物流单"));
        LogisticsBillEntity logisticsBillEntity = BeanMapperUtils.map(LogisticsBillEntity.class, updateDTO);

        // 数据处理
        handleData(logisticsBillEntity);
        log.info("编辑 开始修改物流单数据，id：【{}】", old.getId());
        boolean save = super.updateById(logisticsBillEntity);
        if (!save) {
            throw new ServiceException("物流单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）
        logisticsBillDetailService.update(updateDTO, logisticsBillEntity.getId());
        // 记录主单操作日志
        log.info("编辑 开始记录物流单日志数据，id：【{}】", logisticsBillEntity.getId());
        String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", commonService.getUserInfo().getUserName(), logisticsBillEntity.getId(), "物流单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, logisticsBillEntity, null, logisticsBillEntity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    public List<LogisticsBillEntity> listBySourceIds(List<String> sourceIds) {
        return lambdaQuery().in(LogisticsBillEntity::getSourceId, sourceIds).list();
    }


    /**
     * 新增修改处理数据
     */
    private void handleData(LogisticsBillEntity logisticsBillEntity) {
        String outstockId = logisticsBillEntity.getOutstockId();
        LogisticsBillEntity entity = this.getByOutstockId(outstockId);
        if (Objects.nonNull(entity)) {
            logisticsBillEntity.setId(entity.getId());
        }
    }

    public LogisticsBillEntity getByOutstockId(String outstockId) {
        return this.lambdaQuery().eq(LogisticsBillEntity::getOutstockId, outstockId).last("LIMIT 1").one();

    }

    @Override
    public Boolean logisticsBillBatchSave(List<LogisticsBillDTO.AddDTO> addDTOList) {
        List<String> sourceIds = addDTOList.stream().map(req -> req.getSourceId()).distinct().collect(Collectors.toList());
        List<LogisticsBillEntity> billEntityList = this.listBySourceIds(sourceIds);
        for (LogisticsBillDTO.AddDTO addDTO : addDTOList) {
            LogisticsBillEntity saveEntity = new LogisticsBillEntity();
            BeanMapper.copy(addDTO, saveEntity);
            LogisticsBillEntity logisticsBillEntity = billEntityList.stream().filter(req -> req.getSourceId().equals(addDTO.getSourceId())).findFirst().orElse(null);
            if (ObjectUtil.isNotEmpty(logisticsBillEntity)) {
                saveEntity.setId(logisticsBillEntity.getId());
            }
            this.saveOrUpdate(saveEntity);
            logisticsBillDetailService.removeByMainIds(Arrays.asList(saveEntity.getId()));
            List<LogisticsBillDetailDTO.AddDTO> detailList = addDTO.getDetailList();
            List<LogisticsBillDetailEntity> detailEntityList = new ArrayList<>();
            for (LogisticsBillDetailDTO.AddDTO dto : detailList) {
                LogisticsBillDetailEntity saveDetailEntity = new LogisticsBillDetailEntity();
                saveDetailEntity.setMainId(saveEntity.getId());
                saveDetailEntity.setTrackNo(dto.getTrackNo());
                saveDetailEntity.setTrackStatus(dto.getTrackStatus() == null ? "" : dto.getTrackStatus());
                detailEntityList.add(saveDetailEntity);
            }
            logisticsBillDetailService.saveOrUpdateBatch(detailEntityList);
        }
        return Boolean.TRUE;
    }

    @Override
    public List<LogisticsBillDTO.LogisticsBillVo> listLogisticsBillVoBySourceIds(List<String> sourceIdList) {
        if (CollectionUtils.isEmpty(sourceIdList)) {
            return Collections.emptyList();
        }
        return baseMapper.listLogisticsBillVoBySourceIds(sourceIdList);
    }

    @Override
    public List<LogisticsBillEntity> listByOutstockCodeList(List<String> outstockCodeList) {
        if (CollectionUtils.isEmpty(outstockCodeList)) {
            return Collections.EMPTY_LIST;
        }
        return lambdaQuery().in(LogisticsBillEntity::getOutstockCode, outstockCodeList).list();
    }


    @Override
    public List<LogisticsBillDTO.TabListDTO> tabList(PermissionsDTO dto) {
        List<LogisticsBillDTO.TabListDTO> list = baseMapper.tabList(dto.getPermissionSql());
        String statusGroupType = DictBasicEnum.LOGISTIC_TRACK_STATUS_GROUP.getType();
        String statusType = DictBasicEnum.LOGISTIC_TRACK_STATUS.getType();

        List<DictBasicDTO.ViewDTO> dictList = dictBasicService.getByKey(statusGroupType);

        List<DictBasicDTO.ViewDTO> trackStatusList = dictBasicService.getByKey(statusType);

        List<LogisticsBillDTO.TabListDTO> resultList = new ArrayList<>(dictList.size());
        String allFlag = TmsConstant.ALL;
        for (DictBasicDTO.ViewDTO item : dictList) {
            String group = item.getCode();
            List<String> statusList;
            if (group.equals(allFlag)) {
                statusList = trackStatusList.stream().map(DictBasicDTO.ViewDTO::getCode).collect(Collectors.toList());
            } else {
                statusList = trackStatusList.stream().filter(s -> s.getRemark().equals(group)).
                        map(DictBasicDTO.ViewDTO::getCode).collect(Collectors.toList());
            }
            LogisticsBillDTO.TabListDTO tab = new LogisticsBillDTO.TabListDTO();
            String tabFlag = item.getCode();
            tab.setTabFlag(tabFlag);
            tab.setTabName(item.getName());
            Integer count = list.stream().filter(r -> statusList.contains(r.getTabFlag())).
                    mapToInt(LogisticsBillDTO.TabListDTO::getCount).sum();
            tab.setCount(count);
            resultList.add(tab);
        }


        return resultList;
    }

    @Override
    public PagingVO<LogisticsBillDTO.PagingVO> paging(PagingDTO<LogisticsBillDTO.PagingParamDTO> dto) {
        LogisticsBillDTO.PagingParamDTO params = dto.getParams();
        params.setPermissionSql(dto.getPermissionSql());
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        String statusType = DictBasicEnum.LOGISTIC_TRACK_STATUS.getType();
        List<DictBasicDTO.ViewDTO> trackStatusList = dictBasicService.getByKey(statusType);
        String allFlag = TmsConstant.ALL;
        String group = params.getType();
        List<String> statusList;
        if (group.equals(allFlag)) {
            statusList = trackStatusList.stream().map(DictBasicDTO.ViewDTO::getCode).collect(Collectors.toList());
        } else {
            statusList = trackStatusList.stream().filter(s -> s.getRemark().equals(group)).
                    map(DictBasicDTO.ViewDTO::getCode).collect(Collectors.toList());
        }

        IPage pageData = baseMapper.paging(query, params, statusList);
        List<LogisticsBillDTO.PagingVO> list = pageData.getRecords();
        fillPagingDb(list);
        return new PagingVO<>(pageData);
    }


    @Override
    public Boolean exportExcel(LogisticsBillDTO.ExportDTO params, HttpServletResponse response) {
        String statusType = DictBasicEnum.LOGISTIC_TRACK_STATUS.getType();
        List<DictBasicDTO.ViewDTO> trackStatusList = dictBasicService.getByKey(statusType);
        String allFlag = TmsConstant.ALL;
        String group = params.getType();
        List<String> statusList;
        if (group.equals(allFlag)) {
            statusList = trackStatusList.stream().map(DictBasicDTO.ViewDTO::getCode).collect(Collectors.toList());
        } else {
            statusList = trackStatusList.stream().filter(s -> s.getRemark().equals(group)).
                    map(DictBasicDTO.ViewDTO::getCode).collect(Collectors.toList());
        }
        List<LogisticsBillDTO.PagingVO> list = baseMapper.listExport(params, statusList);
        fillPagingDb(list);
        StringBuffer sb = new StringBuffer();
        String excelPath = "excel/logisticsBill.xlsx";
        String name = "自发货物流单列表";
        String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
        sb.append(date);
        sb.append(name);
        try {
            new ExcelPrintUtils().patchExport(list, response, "", excelPath);
        } catch (IOException e) {
            log.error("自发货物流单导出错 {}", e);
            return Boolean.FALSE;
        }
        return Boolean.TRUE;

    }

    private void fillPagingDb(List<LogisticsBillDTO.PagingVO> list) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        List<String> trackNoList = list.stream().map(LogisticsBillDTO.PagingVO::getTrackNo).distinct().collect(Collectors.toList());
        List<LogisticsTrackEntity> trackList = logisticsTrackService.listByTrackNoList(trackNoList);
        for (LogisticsBillDTO.PagingVO item : list) {
            String salesPlatform = item.getSalesPlatform();
            PlatformDictEnum salesPlatformEnum = PlatformDictEnum.getByCode(salesPlatform);
            String salesPlatformName = Objects.nonNull(salesPlatformEnum) ? salesPlatformEnum.getDesc() : "";
            item.setSalesPlatformName(salesPlatformName);
            //发货时间
            LocalDateTime deliveryTime = item.getDeliveryTime();
            Integer transportDays = 0;
            if (Objects.nonNull(deliveryTime)) {
                Duration duration = Duration.between(now, deliveryTime);
                transportDays = Math.toIntExact(duration.toDays());
            }
            item.setTransportDays(transportDays);
            String trackStatus = item.getTrackStatus();
            String trackStatusName = LogisticTrackStatusEnum.getName(trackStatus);
            item.setTrackStatusName(trackStatusName);
            String trackNo = item.getTrackNo();
            LogisticsTrackEntity trackEntity = trackList.stream().filter(t -> t.getTrackNo().equals(trackNo)).
                    sorted(Comparator.comparing(LogisticsTrackEntity::getCreateTime).reversed()).findFirst().orElse(null);
            if (Objects.nonNull(trackEntity)) {
                item.setTrackContent(trackEntity.getContent());
                item.setUpdateTime(trackEntity.getUpdateTime());
            }
            LocalDateTime signTime = trackList.stream().filter(t -> "6".equals(t.getStatus())).findFirst().
                    map(LogisticsTrackEntity::getCreateTime).orElse(null);
            item.setSignTime(signTime);

        }
    }

    /**
     * @description: 添加物流费用
     * @author Will
     * @date: 2023/11/20 12:27
     * @param logisticsBillEntity
     */
    private void addLogisticsBillCost(LogisticsBillEntity logisticsBillEntity) {
        LogisticsBillCostDTO.AddDTO addDTO = new LogisticsBillCostDTO.AddDTO();

        //渠道关联模板
        ShippingTemplateEntity shippingTemplateEntity = shippingTemplateService.getByChannelId(logisticsBillEntity.getChannelId());
//        if (ObjectUtil.isEmpty(shippingTemplateEntity)) {
//            throw new ServiceException(ApiError.ERROR_SHIPPING_TEMPLATE_NOT_EXIST);
//        }
        String currency="CNY";
        if(Objects.nonNull(shippingTemplateEntity)){
            currency=shippingTemplateEntity.getCurrency();
        }

        //来源b2c销售订单
        if (SourceTypeEnum.SO_B2C.getCode().equals(logisticsBillEntity.getSourceType())) {
            //物流信息
            List<SoB2cLogisticsEntity> soB2cLogisticsList = soB2cFeign.listSoB2cLogisticsByMainIdList(Arrays.asList(logisticsBillEntity.getSourceId()));
            if (CollectionUtils.isEmpty(soB2cLogisticsList)) {
                throw new ServiceException(ApiError.ERROR_SO_B2C_LOGISTICS_NOT_EXIST);
            }
            addDTO.setActualWeight(soB2cLogisticsList.get(0).getWeight());
            BigDecimal volume = soB2cLogisticsList.get(0).getHeight()
                    .multiply(soB2cLogisticsList.get(0).getWeight())
                    .multiply(soB2cLogisticsList.get(0).getLength());
            addDTO.setVolumeWeight(MathUtil.divide(volume,new BigDecimal(shippingTemplateEntity.getVolumeSetting())));
        }
        //计费重
        BigDecimal billingWeight = MathUtil.compareTo(addDTO.getActualWeight(),addDTO.getVolumeWeight()) > MathUtil.ZERO
                ? addDTO.getActualWeight() : addDTO.getVolumeWeight();
        //预估运费
        ShippingTemplateRuleDTO.ViewParamDTO viewParamDTO = new ShippingTemplateRuleDTO.ViewParamDTO();
        viewParamDTO.setWeight(addDTO.getActualWeight());
        ShippingTemplateRuleEntity shippingTemplateRule = shippingTemplateRuleService.getShippingTemplateRule(viewParamDTO);
        if (ObjectUtil.isNotEmpty(shippingTemplateRule)) {
            BigDecimal shippingCost = shippingCalculationService.calculationShippingCost(shippingTemplateEntity, shippingTemplateRule, billingWeight);
            addDTO.setEstimatedShippingCost(shippingCost);
        }

        addDTO.setCurrency(currency);
        addDTO.setChannelId(logisticsBillEntity.getChannelId());
        addDTO.setLogisticsBillId(logisticsBillEntity.getId());
        logisticsBillCostService.add(addDTO);
    }

}
