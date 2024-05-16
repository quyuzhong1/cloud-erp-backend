package com.erp.server.tms.service.impl;


import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.AdvanceQueryContainer;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.*;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.PagingVO;
import com.common.core.constant.CommonConstants;
import com.common.core.controller.vo.ApiResult;
import com.common.core.entity.BaseEntity;
import com.common.core.enums.ApiError;
import com.common.core.enums.CurrencyEnum;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.MathUtil;
import com.common.core.utils.date.DateUtil;
import com.erp.model.oms.dto.CfgRuleOrderHandleDTO;
import com.erp.model.oms.dto.SoB2cDTO;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.oms.entity.SoB2cLogisticsEntity;
import com.erp.model.plm.entity.ProductCustomsEntity;
import com.erp.model.tms.dto.*;
import com.erp.model.tms.entity.*;
import com.erp.model.tms.enums.*;
import com.erp.model.tms.vo.request.*;
import com.erp.model.tms.vo.response.CancelResponseVO;
import com.erp.model.tms.vo.response.InterceptResponseVO;
import com.erp.model.tms.vo.response.LogisticsOrderResponseVO;
import com.erp.model.tms.vo.response.LogisticsPrintLabelResponse;
import com.erp.model.wms.entity.SoOutstockEntity;
import com.erp.rpc.oms.feign.CfgRuleFeign;
import com.erp.rpc.oms.feign.SoB2cFeign;
import com.erp.rpc.plm.feign.LogisticsProductFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.server.tms.constant.TmsConstant;
import com.erp.server.tms.convert.LogisticsBillConverter;
import com.erp.server.tms.handler.LogisticsRegistry;
import com.erp.server.tms.mapper.LogisticsBillMapper;
import com.erp.server.tms.service.*;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 物流单 服务实现类
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
    private LogisticsBillDetailService logisticsBillDetailService;

    @Autowired
    private DictBasicService dictBasicService;
    @Autowired
    private LogisticsProductFeign logisticsProductFeign;

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

    @Autowired
    private LogisticsAuthService logisticsAuthService;

    @Autowired
    private LogisticsRegistry logisticsRegistry;

    @Autowired
    private LogisticsAddressService logisticsAddressService;
    @Autowired
    private LogisticsChannelService logisticsChannelService;

    @Autowired
    private LogisticsSaleChannelService logisticsSaleChannelService;

    @Autowired
    private LogisticsPrintTypeService logisticsPrintTypeService;

    @Autowired
    private TmsCfgCostService tmsCfgCostService;

    @Autowired
    private LogisticsBillService logisticsBillService;

    @Autowired
    private PlmTaskFeign plmTaskFeign;

    @Autowired
    private CfgRuleFeign cfgRuleFeign;

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

        logisticsBillDetailService.add(logisticsBillEntity, addDTO.getDetailList(),true);


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
        boolean save = super.updateById(logisticsBillEntity);
        if (!save) {
            throw new ServiceException("物流单保存失败");
        }
        logisticsBillDetailService.update(updateDTO, logisticsBillEntity.getId());
        // 记录主单操作日志
        String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), logisticsBillEntity.getId(), "物流单");
        operateLogService.addModuleOperateLogByObj(old, logisticsBillEntity, null, logisticsBillEntity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    public List<LogisticsBillEntity> listBySourceIds(List<String> sourceIds) {
        return lambdaQuery().in(LogisticsBillEntity::getSourceId, sourceIds).list();
    }

    @Override
    public Boolean remove(LogisticsBillDTO.RemoveDTO dto) {
        List<String> outstockIdList = dto.getOutstockIdList();
        List<LogisticsBillEntity> billEntityList = listByOutstockIds(outstockIdList);
        if (CollectionUtils.isNotEmpty(billEntityList)) {
            List<String> ids = billEntityList.stream().map(LogisticsBillEntity::getId).collect(Collectors.toList());
            logisticsBillDetailService.removeByMainIds(ids,true);
            return this.removeByIds(ids);
        }
        return Boolean.FALSE;
    }


    /**
     * 新增修改处理数据
     */
    private void handleData(LogisticsBillEntity logisticsBillEntity) {
        if(StringUtils.isBlank(logisticsBillEntity.getLogisticsSupplierId()) && StringUtils.isNotBlank(logisticsBillEntity.getChannelId())){
            LogisticsChannelEntity channelEntity = logisticsChannelService.getById(logisticsBillEntity.getChannelId());
            if(Objects.nonNull(channelEntity)){
                logisticsBillEntity.setLogisticsSupplierId(channelEntity.getMainId());
            }
        }
        //平台订单号
        if (StrUtil.equals(logisticsBillEntity.getSourceType(),SourceTypeEnum.SO_B2C.getCode())) {
            SoB2cEntity soB2cEntity = soB2cFeign.getById(logisticsBillEntity.getSourceId());
            if (ObjectUtil.isNotEmpty(soB2cEntity)) {
                logisticsBillEntity.setPlatformCode(soB2cEntity.getPlatformCode());
            }
        }

    }

    public List<LogisticsBillEntity> listByOutstockIds(List<String> outstockIds) {
        if (CollectionUtils.isEmpty(outstockIds)) {
            return Collections.emptyList();
        }
        return this.lambdaQuery().in(LogisticsBillEntity::getOutstockId, outstockIds).list();

    }

    @Override
    public Boolean logisticsBillBatchSave(List<LogisticsBillDTO.AddDTO> addDTOList) {
        List<String> sourceIds = addDTOList.stream().map(req -> req.getSourceId()).distinct().collect(Collectors.toList());
        List<LogisticsBillEntity> billEntityList = this.listBySourceIds(sourceIds);
        for (LogisticsBillDTO.AddDTO addDTO : addDTOList) {
            LogisticsBillEntity saveEntity = new LogisticsBillEntity();
            BeanMapper.copy(addDTO, saveEntity);
            this.handleData(saveEntity);
            LogisticsBillEntity logisticsBillEntity = billEntityList.stream().filter(req -> req.getSourceId().equals(addDTO.getSourceId())).findFirst().orElse(null);
            if (ObjectUtil.isNotEmpty(logisticsBillEntity)) {
                saveEntity.setId(logisticsBillEntity.getId());
                this.saveOrUpdate(saveEntity);
            } else {
                this.save(saveEntity);
            }

            logisticsBillDetailService.removeByMainIds(Arrays.asList(saveEntity.getId()),true);
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
            //新增物流费用单
            addLogisticsBillCost(saveEntity,detailEntityList);
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
    public List<LogisticsBillDTO.LogisticsBillVo> getTrackStatusByTrackNo(List<LogisticsBillDTO.LogisticsBillVo> billVoList) {
        if (CollectionUtils.isEmpty(billVoList)) {
            return billVoList;
        }
        //根据物流运单号/跟踪号获取运输状态
        List<String> trackNoList = billVoList.stream().map(LogisticsBillDTO.LogisticsBillVo::getTrackNo).filter(StringUtils::isNotBlank).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(trackNoList)) {
            return billVoList;
        }
        List<LogisticsBillDTO.LogisticsBillVo> logisticsBillVos = baseMapper.listLogisticsBillVoByTrackNo(trackNoList);
        if (CollectionUtils.isEmpty(logisticsBillVos)) {
            return billVoList;
        }
        Map<String, List<LogisticsBillDTO.LogisticsBillVo>> logisticsBillMap = logisticsBillVos.stream()
                .collect(Collectors.groupingBy(LogisticsBillDTO.LogisticsBillVo::getTrackNo));
        billVoList.stream().forEach(billVo->{
            List<LogisticsBillDTO.LogisticsBillVo> logisticsBillVoList= logisticsBillMap.get(billVo.getTrackNo());
            if (CollectionUtils.isNotEmpty(logisticsBillVoList)){
                LogisticsBillDTO.LogisticsBillVo trackBillVo = logisticsBillVoList.stream().findFirst().orElse(null);
                if (Objects.nonNull(trackBillVo)) {
                    billVo.setTrackStatusName(StringUtils.isBlank(trackBillVo.getTrackStatus()) ?
                            LogisticTrackStatusEnum.NOT_FIND.getName() : LogisticTrackStatusEnum.getName(trackBillVo.getTrackStatus()));
                    billVo.setTrackStatus(StringUtils.isBlank(trackBillVo.getTrackStatus()) ?
                            LogisticTrackStatusEnum.NOT_FIND.getCode() : trackBillVo.getTrackStatus());
                } else {
                    billVo.setTrackStatus(LogisticTrackStatusEnum.NOT_FIND.getCode());
                    billVo.setTrackStatusName(LogisticTrackStatusEnum.NOT_FIND.getName());
                }
            }else{
                billVo.setTrackStatus(LogisticTrackStatusEnum.NOT_FIND.getCode());
                billVo.setTrackStatusName(LogisticTrackStatusEnum.NOT_FIND.getName());
            }
        });
        return billVoList;
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
                statusList = Collections.emptyList();
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
            statusList = Collections.emptyList();
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
            statusList = Collections.emptyList();
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

    /**
     * 生成物流单
     *
     * @param dto
     */
    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    public LogisticsBillDTO.GenerateBillResultDTO generateBill(LogisticsBillDTO.GenerateBillDTO dto) {
        String channelId = dto.getChannelId();
        LogisticsSupplierDTO.AuthDTO auth = logisticsAuthService.getAuthByChannelId(channelId);
        if (Objects.isNull(auth)) {
            throw new ServiceException(ApiError.ERROR_LOGISTICS_CHANNEL_NOT_EXIST);
        }
        Map<String, String> authMap = logisticsAuthService.getLogisticsAuthConfig(auth.getAuthId(), auth.getLogisticsPlatform());
        LogisticsChannelEntity logisticsChannel = logisticsChannelService.getById(channelId);
        if (Objects.isNull(logisticsChannel)) {
            throw new ServiceException(ApiError.ERROR_LOGISTICS_CHANNEL_NOT_EXIST);
        }
        String aliExpress = PlatformDictEnum.ALI_EXPRESS.getCode();
        String salesPlatform = dto.getSalesPlatform();
        Boolean isAliExpress = aliExpress.equals(salesPlatform);

        String country = Objects.nonNull(dto.getReceiver())?Objects.nonNull(dto.getReceiver().getCountry())?dto.getReceiver().getCountry():"":"";
        LogisticsChannelDTO.LogisticsChannelConstraintDTO channelConstraintDTO = logisticsChannelService.getLogisticsChannelConstraint(channelId,country);
        //最高报关金额
        BigDecimal maxCustomsAmount = channelConstraintDTO.getMaxCustomsAmount();
        //最低报关金额
        BigDecimal minCustomsAmount = channelConstraintDTO.getMinCustomsAmount();

        LogisticsAddressTypeEnum deliverType = LogisticsAddressTypeEnum.DELIVER;
        //发货人信息
        List<LogisticsAddressEntity> addressList = logisticsAddressService.listByChannelIdAndShopId(channelId, dto.getShopId());
        List<LogisticsAddressEntity> deliverList = addressList.stream().filter(a -> deliverType.equals(a.getType())).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(deliverList)) {
            throw new ServiceException(ApiError.ERROR_CHANNEL_ADDRESS_NOT_EXIST, logisticsChannel.getName(), LogisticsAddressTypeEnum.DELIVER.getName());
        }
        //发货人信息
        SenderInfo senderInfo = new SenderInfo();
        LogisticsAddressEntity logisticsAddress = deliverList.get(0);
        BeanMapperUtils.copy(logisticsAddress, senderInfo);
        //地址id
        senderInfo.setId(logisticsAddress.getAddressId());

        LogisticsAddressTypeEnum refundType = LogisticsAddressTypeEnum.REFUND;
        //退货地址信息
        SenderInfo returnInfo = null;
        //退货地址
        LogisticsAddressEntity returnAddress = addressList.stream().filter(a -> refundType.equals(a.getType())).findFirst().orElse(null);
        if (Objects.nonNull(returnAddress)) {
            returnInfo = new SenderInfo();
            BeanMapperUtils.copy(returnAddress, returnInfo);
            //地址id
            returnInfo.setId(returnAddress.getAddressId());
        } else {
            returnInfo = senderInfo;
        }


        //平台
        String logisticsPlatform = auth.getLogisticsPlatform();
        LogisticsService service = logisticsRegistry.getHandler(logisticsPlatform);
        authMap.put("token", dto.getToken());
        //来源
        String sourceType = dto.getSourceType();
        //收货人
        LogisticsBillDTO.ReceiverDTO receiverDTO = dto.getReceiver();
        //转化成收货人
        ReceiverInfoVO receiverInfo = LogisticsBillConverter.INSTANCE.convertReceiver(receiverDTO);
        //申报信息
        List<LogisticsProductVO> productVOS = dto.getProductVOS();
//        List<LogisticsBillDTO.SkuDTO> skuList = dto.getSkuList();
//        List<String> skuIdList = skuList.stream().map(LogisticsBillDTO.SkuDTO::getSkuId).collect(Collectors.toList());
//        List<LogisticsProductDTO.ProductDTO> skuInfoList = logisticsProductFeign.listBySkuIdList(skuIdList);
//        List<LogisticsProductDTO.ProductDTO> ordersSkuList = buildTransferDeclareProduct(country, dto, skuInfoList, minCustomsAmount, maxCustomsAmount,isAliExpress);
        //包裹信息
        LogisticsBillDTO.PackageDTO packageDTO = dto.getPackageInfo();
//        List<LogisticsProductVO> logisticsProductList = LogisticsBillConverter.INSTANCE.convertLogisticsProduct(ordersSkuList);


        ParceInfoVO parceInfo = LogisticsBillConverter.INSTANCE.convertParceInfo(packageDTO);
        Boolean hasBattery = productVOS.stream().anyMatch(LogisticsProductVO::getIsElectric);
        //是否带电
        parceInfo.setHasBattery(hasBattery);
        Integer totalQuantity = productVOS.stream().filter(e -> Objects.nonNull(e.getQuantity())).mapToInt(LogisticsProductVO::getQuantity).sum();
        parceInfo.setTotalQuantity(totalQuantity);

        //申报总价
        BigDecimal totalPrice = productVOS.stream().filter(s -> Objects.nonNull(s.getDestDeclarePrice()) && Objects.nonNull(s.getQuantity()))
                .map(e -> MathUtil.multiply(e.getDestDeclarePrice(), e.getQuantity())).reduce(BigDecimal.ZERO, BigDecimal::add);
        parceInfo.setTotalPrice(totalPrice);
        //总重量 取包裹重量
//        Integer totalWeight = ordersSkuList.stream().filter(s -> Objects.nonNull(s.getWeight())).mapToInt(LogisticsProductDTO.ProductDTO::getWeight).sum();
//        parceInfo.setTotalWeight(totalWeight);

        //根据销售平台和渠道code 获取到原生的渠道
        LogisticsSaleChannelEntity saleChannel = logisticsSaleChannelService.getByPlatform(logisticsPlatform, logisticsChannel.getCode());
        if (Objects.isNull(saleChannel)) {
            throw new ServiceException(ApiError.ERROR_SALES_CHANNEL_NOT_EXIST, logisticsChannel.getName());
        }

        //根据订单处理规则，判断是否需要清空国家、省市数据
        Map<String,Object> map = getRuleOrderHandleMap(dto);
        CfgRuleOrderHandleDTO.RuleMatchDTO ruleOrderHandleMatchResult = cfgRuleFeign.getRuleOrderHandleMatchResult(map);
        Boolean approveSuccess = ruleOrderHandleMatchResult.getApproveSuccess();
        //匹配审核规则通过,自动提交并审核
        if (Objects.nonNull(approveSuccess) && approveSuccess) {
            //清空城市
            if (ruleOrderHandleMatchResult.getIsPushCity()) {
                receiverInfo.setCity("");
            }
            //清空省份/州
            if (ruleOrderHandleMatchResult.getIsPushProvince()) {
                receiverInfo.setProvince("");
            }
        }
        LogisticsOrderVO logisticsOrderVO = LogisticsOrderVO.builder().authMap(authMap).
                orderSource(sourceType).
                trackNo(dto.getTrackNo()).
                topUserKey(dto.getTopUserKey()).
                sourceId(dto.getOrderId()).
                oaid(dto.getOaid()).
                deliveryNo(dto.getOrderCode()).
                iossCode(dto.getIossTaxNo()).
                senderInfo(senderInfo).
                returnInfo(returnInfo).
                receiverInfoVO(receiverInfo).
                parceInfoVO(parceInfo).
                logisticsProductVOList(productVOS).
                logisticsChannelEntity(logisticsChannel).
                logisticsSaleChannel(saleChannel).
                build();
        log.info("创建订单,参数:{}", JSONUtil.toJsonStr(logisticsOrderVO));
        ApiResult<LogisticsOrderResponseVO> orderResult = service.createOrder(logisticsOrderVO);
        //表示成功
        if (orderResult.isSuccess()) {
            LogisticsBillDTO.GenerateBillResultDTO resultDTO = handleBill(orderResult.getData(), dto);
            return resultDTO;
        } else {
            LogisticsOrderResponseVO responseVO = orderResult.getData();
            StringBuilder sb = new StringBuilder(orderResult.getMsg());
            if (Objects.nonNull(responseVO)) {
                sb.append(responseVO.getMessage());
            }
            String message = sb.toString();
            throw new ServiceException(orderResult.getCode(), message);
        }


    }

    /**
     * @description: 订单处理规则匹配
     * @author Will
     * @date: 2024/5/9 17:02
     * @return Map<Object>
     */
    private Map<String,Object> getRuleOrderHandleMap (LogisticsBillDTO.GenerateBillDTO dto) {
        Map<String,Object> resultMap = new HashMap<>(4);
        resultMap.put("dictPlatform", dto.getSalesPlatform());
        resultMap.put("shop", dto.getShopId());
        resultMap.put("destCountry", ObjectUtil.isEmpty(dto.getReceiver()) ? "" : dto.getReceiver().getCountry());
        resultMap.put("logisticsChannelId", dto.getChannelId());

        //现有规则解析必须包含明细信息
        Map<String,Object> detailMap = new HashMap<>(4);
        detailMap.put("dictPlatform", dto.getSalesPlatform());
        detailMap.put("shop", dto.getShopId());
        detailMap.put("destCountry", ObjectUtil.isEmpty(dto.getReceiver()) ? "" : dto.getReceiver().getCountry());
        detailMap.put("logisticsChannelId", dto.getChannelId());

        resultMap.put("detailList", Arrays.asList(detailMap));
        return  resultMap;
    }

    @Transactional(rollbackFor = Exception.class)
    public LogisticsBillDTO.GenerateBillResultDTO handleBill(LogisticsOrderResponseVO responseVO, LogisticsBillDTO.GenerateBillDTO dto) {
        LogisticsBillDTO.GenerateBillResultDTO resultDTO = new LogisticsBillDTO.GenerateBillResultDTO();
        List<String> trackNoList = new ArrayList<>(2);

        String transportNo = responseVO.getTransportNo();
        //跟踪单号
        String trackNo = responseVO.getTrackNo();
        if (StringUtils.isNotBlank(trackNo) && !"null".equals(trackNo)) {
            trackNoList.add(trackNo);
        }
        Boolean more = responseVO.getMore();
        if (Objects.nonNull(more) && more) {
            List<LogisticsOrderResponseVO> responseList = responseVO.getLogisticsOrderResponseVOS();
            for (LogisticsOrderResponseVO item : responseList) {
                LogisticsBillDetailDTO.AddDTO detailDTO = new LogisticsBillDetailDTO.AddDTO();
                detailDTO.setTrackNo(item.getTrackNo());
                trackNoList.add(item.getTrackNo());
            }
        }
        resultDTO.setTransportNo(transportNo);
        resultDTO.setTrackNoList(trackNoList);
        return resultDTO;

    }

    @Override
    public List<LogisticsBillEntity> listByOutstockIdList(List<String> outstockIdList) {
        if (CollectionUtils.isEmpty(outstockIdList)) {
            return Collections.emptyList();
        }
        return this.lambdaQuery().in(LogisticsBillEntity::getOutstockId, outstockIdList).list();
    }

//    private List<LogisticsProductDTO.ProductDTO> buildTransferDeclareProduct(String country, LogisticsBillDTO.GenerateBillDTO dto, List<LogisticsProductDTO.ProductDTO> skuInfoList, BigDecimal minCustomsAmount, BigDecimal maxCustomsAmount, Boolean isAliExpress){
//        List<LogisticsProductDTO.ProductDTO> ordersSkuList = null;
//        if (Objects.nonNull(isAliExpress) && isAliExpress){
//            //速卖通不做sku拆分
//            ordersSkuList = new ArrayList<>(skuInfoList.size());
//            List<String> skuIds = dto.getSkuList().stream().map(LogisticsBillDTO.SkuDTO::getSkuId).distinct().collect(Collectors.toList());
//            //获取sku目的国海关编码映射关系
//            List<ProductCustomsEntity> productCustomsList = plmTaskFeign.listProductCustomsBySkuIds(ProductCustomsSkuDTO.builder().skuIds(skuIds).country(country).build());
//            for (LogisticsBillDTO.SkuDTO item : dto.getSkuList()){
//                String skuId = item.getSkuId();
//                LogisticsProductDTO.ProductDTO productDTO = skuInfoList.stream().filter(s -> s.getSkuId().equals(skuId)).findFirst().orElse(null);
//                String customCode = "";
//                ProductCustomsEntity customs = getCustomsByCountry(country,skuId,productCustomsList);
//                if (Objects.nonNull(customs)){
//                    customCode = customs.getCustomsCode();
//                }
//                if (Objects.nonNull(productDTO)) {
//                    Integer qty = item.getQty();
//                    BigDecimal destDeclarePrice = productDTO.getDestDeclarePrice();
//                    productDTO.setQuantity(qty);
////                    productDTO.setPrice(price);
//                    productDTO.setAmount(MathUtil.multiply(destDeclarePrice, qty));
//                    //目的国申报价
////                    BigDecimal destDeclarePrice = productDTO.getDestDeclarePrice();
//                    //表示最大的报关价还小于 目的过申报价
//                    if (Objects.nonNull(destDeclarePrice) && maxCustomsAmount.compareTo(BigDecimal.ZERO) != 0 && maxCustomsAmount.compareTo(destDeclarePrice) < 0) {
//                        productDTO.setDestDeclarePrice(maxCustomsAmount);
//                    }
//                    //表示最小的报关价还小于 目的过申报价
//                    if (Objects.nonNull(destDeclarePrice) && minCustomsAmount.compareTo(BigDecimal.ZERO) != 0 && destDeclarePrice.compareTo(minCustomsAmount) < 0) {
//                        productDTO.setDestDeclarePrice(minCustomsAmount);
//                    }
//
//                    //如果是速卖通的话
//                    String platformSpuNo = item.getPlatformSpuNo();
//                    productDTO.setSkuId(platformSpuNo);
//                    String sourceDetailId = item.getSourceDetailId();
//                    if (StringUtils.isNotBlank(sourceDetailId)){
//                        productDTO.setChildOrderId(Long.valueOf(sourceDetailId));
//                    }
//
//                    productDTO.setSkuNo(item.getSkuNo());
//                    productDTO.setSkuId(skuId);
//                    productDTO.setCustomsCode(customCode);
//                    ordersSkuList.add(productDTO);
//                }
//            }
//        }else {
//            List<com.erp.model.oms.dto.TransferDeclareProductDTO> transferDeclareProductBySoIds = soB2cFeign.getTransferDeclareProductBySoIds(Collections.singletonList(dto.getOrderId()));
//            ordersSkuList = new ArrayList<>(transferDeclareProductBySoIds.size());
//            List<String> skuIds = transferDeclareProductBySoIds.stream().map(com.erp.model.oms.dto.TransferDeclareProductDTO::getSkuId).collect(Collectors.toList());
//            //获取sku目的国海关编码映射关系
//            List<ProductCustomsEntity> productCustomsList = plmTaskFeign.listProductCustomsBySkuIds(ProductCustomsSkuDTO.builder().skuIds(skuIds).country(country).build());
//            for (com.erp.model.oms.dto.TransferDeclareProductDTO transferDeclareProductDTO : transferDeclareProductBySoIds) {
//                String skuId = transferDeclareProductDTO.getSkuId();
//                String customCode = "";
//                ProductCustomsEntity customs = getCustomsByCountry(country,skuId,productCustomsList);
//                if (Objects.nonNull(customs)){
//                    customCode = customs.getCustomsCode();
//                }
//                LogisticsProductDTO.ProductDTO productDTO = TransferDeclareConverter.INSTANCE.omsProductToTmsProduct(transferDeclareProductDTO);
//                Integer qty = transferDeclareProductDTO.getQty();
//                //目的国申报价
//                BigDecimal destDeclarePrice = transferDeclareProductDTO.getDestDeclarePrice();
//                productDTO.setAmount(MathUtil.multiply(destDeclarePrice, qty));
//                //目的国申报价
//                //表示最大的报关价还小于 目的过申报价
//                if (Objects.nonNull(destDeclarePrice) && maxCustomsAmount.compareTo(BigDecimal.ZERO) != 0 && maxCustomsAmount.compareTo(destDeclarePrice) < 0) {
//                    productDTO.setDestDeclarePrice(maxCustomsAmount);
//                }
//                //表示最小的报关价还小于 目的过申报价
//                if (Objects.nonNull(destDeclarePrice) && minCustomsAmount.compareTo(BigDecimal.ZERO) != 0 && destDeclarePrice.compareTo(minCustomsAmount) < 0) {
//                    productDTO.setDestDeclarePrice(minCustomsAmount);
//                }
//                productDTO.setCustomsCode(customCode);
//                ordersSkuList.add(productDTO);
//            }
//        }
//       return ordersSkuList;
//    }

    /**
     * 匹配海关编码
     *
     * @param country
     * @param skuId
     * @param productCustomsList
     * @return
     */
    private ProductCustomsEntity getCustomsByCountry(String country, String skuId, List<ProductCustomsEntity> productCustomsList) {
        ProductCustomsEntity customs = null;
        if (StringUtils.isNotEmpty(country)){
            customs = productCustomsList.stream().filter(e -> StringUtils.isNotEmpty(e.getSkuId()) && StringUtils.isNotEmpty(skuId) && skuId.equals(e.getSkuId())
                    && StringUtils.isNotEmpty(country) && e.getCountry().contains(country)).findFirst().orElse(null);

        }
        //存在默认值时，先取默认值
        if (Objects.isNull(customs)){
            customs = productCustomsList.stream().filter(e -> StringUtils.isNotEmpty(e.getSkuId()) && StringUtils.isNotEmpty(skuId) && skuId.equals(e.getSkuId())
                    && StringUtils.isNotEmpty(e.getCountry()) && CommonConstants.DEFAULT.equals(e.getCountry())).findFirst().orElse(null);
        }
        //未匹配到时，获取空值
        if (Objects.isNull(customs)){
            customs = productCustomsList.stream().filter(e -> StringUtils.isNotEmpty(e.getSkuId()) && StringUtils.isNotEmpty(skuId) && skuId.equals(e.getSkuId())
                    && StringUtils.isEmpty(e.getCountry())).findFirst().orElse(null);
        }
        return customs;
    }

    /**
     * 取消物流单
     *
     * @param dto
     * @return
     */
    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    public ApiResult<CancelResponseVO> cancelBill(LogisticsBillDTO.CancelBillDTO dto) {

        String channelId = dto.getChannelId();
        LogisticsSupplierDTO.AuthDTO auth = logisticsAuthService.getAuthByChannelId(channelId);
        if (Objects.isNull(auth)) {
            throw new ServiceException(ApiError.ERROR_LOGISTICS_CHANNEL_NOT_EXIST);
        }
        //物流商对接传运单号或参考号，在这边校验时参考号必填，如果没有运单号，判断如果有跟踪号，通过跟踪号查运单号
        //取消物流单的
        List<LogisticsCancelOrderVO> cancelOrderList = new ArrayList<>(1);
        LogisticsCancelOrderVO cancelOrderVO = new LogisticsCancelOrderVO();
        cancelOrderVO.setDeliveryNo(dto.getReferenceNumber());
        cancelOrderVO.setTransportNo(dto.getTransportNo());
        cancelOrderVO.setReason(dto.getReason());
        cancelOrderVO.setOrderId(dto.getOrderId());
        cancelOrderList.add(cancelOrderVO);
        if (StringUtils.isBlank(dto.getTransportNo())) {
            LogisticsBillDTO.BaseDTO billBase = this.getBaseByTrackNo(dto.getTrackNo());
            if (ObjectUtil.isEmpty(billBase) || Objects.isNull(billBase.getId())) {
                throw new ServiceException(ApiError.NOT_EXIST_BILL, "物流单");
            }
            cancelOrderVO.setTransportNo(billBase.getTransportNo());
        }
        Map<String, String> authMap = logisticsAuthService.getLogisticsAuthConfig(auth.getAuthId(), auth.getLogisticsPlatform());
        cancelOrderVO.setAuthMap(authMap);
        //平台
        String logisticsPlatform = auth.getLogisticsPlatform();
        LogisticsService service = logisticsRegistry.getHandler(logisticsPlatform);
        ApiResult<List<CancelResponseVO>> thirdPartyResult = service.cancelOrder(cancelOrderList);
        //是否成功
        if (thirdPartyResult.isSuccess()) {
            //将自发货费用状态改成作废
            LogisticsBillEntity logisticsBillEntity = this.lambdaQuery().eq(LogisticsBillEntity::getTransportNo, dto.getTransportNo()).last("limit 1").one();
            if (Objects.nonNull(logisticsBillEntity)) {
                logisticsBillCostService.invalidByLogisticsBillId(logisticsBillEntity.getId());
            }
        }
        ApiResult<CancelResponseVO> result = new ApiResult<>();
        result.setMsg(thirdPartyResult.getMsg());
        result.setCode(thirdPartyResult.getCode());
        if (CollectionUtils.isNotEmpty(thirdPartyResult.getData())) {
            result.setData(thirdPartyResult.getData().get(0));
        }
        return result;
    }

    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    public ApiResult<InterceptResponseVO> interceptBill(LogisticsBillDTO.CancelBillDTO dto) {
        String channelId = dto.getChannelId();
        LogisticsSupplierDTO.AuthDTO auth = logisticsAuthService.getAuthByChannelId(channelId);
        if (Objects.isNull(auth)) {
            throw new ServiceException(ApiError.ERROR_LOGISTICS_CHANNEL_NOT_EXIST);
        }
        //物流商对接传运单号或参考号，在这边校验时参考号必填，如果没有运单号，判断如果有跟踪号，通过跟踪号查运单号
        //取消物流单的
        List<LogisticsInterceptOrderVO> interceptOrderVOList = new ArrayList<>(1);
        LogisticsInterceptOrderVO interceptOrderVO = new LogisticsInterceptOrderVO();
        interceptOrderVO.setDeliveryNo(dto.getReferenceNumber());
        interceptOrderVO.setTransportNo(dto.getTransportNo());
        interceptOrderVO.setInterceptReason(dto.getReason());
        interceptOrderVO.setOrderId(dto.getOrderId());
        interceptOrderVOList.add(interceptOrderVO);
        if (StringUtils.isBlank(dto.getTransportNo())) {
            LogisticsBillDTO.BaseDTO billBase = this.getBaseByTrackNo(dto.getTrackNo());
            if (Objects.isNull(billBase.getId())) {
                throw new ServiceException(ApiError.NOT_EXIST_BILL, "物流单");
            }
            interceptOrderVO.setTransportNo(billBase.getTransportNo());
        }
        Map<String, String> authMap = logisticsAuthService.getLogisticsAuthConfig(auth.getAuthId(), auth.getLogisticsPlatform());
        interceptOrderVO.setAuthMap(authMap);
        //平台
        String logisticsPlatform = auth.getLogisticsPlatform();
        LogisticsService service = logisticsRegistry.getHandler(logisticsPlatform);
        ApiResult<List<InterceptResponseVO>> thirdPartyResult = service.interceptOrder(interceptOrderVOList);
        //是否成功
        if (thirdPartyResult.isSuccess()) {
            //将自发货费用状态改成作废
            LogisticsBillEntity logisticsBillEntity = this.lambdaQuery().eq(LogisticsBillEntity::getTransportNo, dto.getTransportNo()).last("limit 1").one();
            logisticsBillCostService.invalidByLogisticsBillId(logisticsBillEntity.getId());
        }
        ApiResult<InterceptResponseVO> result = new ApiResult<>();
        result.setMsg(thirdPartyResult.getMsg());
        result.setCode(thirdPartyResult.getCode());
        if (CollectionUtils.isNotEmpty(thirdPartyResult.getData())) {
            result.setData(thirdPartyResult.getData().get(0));
        }
        return result;
    }


    @Override
    public LogisticsBillDTO.BaseDTO getBaseByTrackNo(String trackNo) {
        if (StringUtils.isBlank(trackNo)) {
            return new LogisticsBillDTO.BaseDTO();
        }
        return baseMapper.getBaseByTrackNo(trackNo);
    }

    @Override
    public List<LogisticsBillDTO.BaseDTO> listLogisticsBillByTransportNos(List<String> transportNoList) {
        if (CollectionUtils.isEmpty(transportNoList)) {
            return Collections.emptyList();
        }
        return baseMapper.listLogisticsBillByTransportNos(transportNoList);
    }

    private void fillPagingDb(List<LogisticsBillDTO.PagingVO> list) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        List<String> trackNoList = list.stream().map(LogisticsBillDTO.PagingVO::getTrackNo).distinct().collect(Collectors.toList());
        List<LogisticsTrackEntity> trackList = logisticsTrackService.listByTrackNoList(trackNoList);
        String signCode = LogisticTrackStatusEnum.SIGN.getCode();
        for (LogisticsBillDTO.PagingVO item : list) {
            //是否签收
            Boolean isSign = signCode.equals(item.getTrackStatus());
            String salesPlatform = item.getSalesPlatform();
            PlatformDictEnum salesPlatformEnum = PlatformDictEnum.getByCode(salesPlatform);
            String salesPlatformName = Objects.nonNull(salesPlatformEnum) ? salesPlatformEnum.getDesc() : "";
            item.setSalesPlatformName(salesPlatformName);
            //发货时间
            LocalDateTime deliveryTime = item.getDeliveryTime();
            Integer transportDays = 0;
            LocalDateTime signTime = item.getSignTime();
            if (Objects.nonNull(deliveryTime)) {
                LocalDateTime compareTime = now;
                //如果是签收成功状态
                if (isSign) {
                    if (Objects.nonNull(signTime)) {
                        compareTime = signTime;
                    }
                }
                long daysBetween = ChronoUnit.DAYS.between(deliveryTime, compareTime);
                if (daysBetween >= 0) {
                    transportDays = Math.toIntExact(daysBetween) + 1;
                }
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
            String orderType = item.getOrderType();
            String orderTypeName = OrderTypeEnum.getName(orderType);
            item.setOrderTypeName(orderTypeName);
            //发货类型名称
            item.setShipmentTypeName(ShipmentTypeEnum.getName(item.getShipmentType()));
        }
    }

    /**
     * @param logisticsBillEntity
     * @description: 添加物流费用
     * @author Will
     * @date: 2023/11/20 12:27
     */
    @Override
    public void addLogisticsBillCost(LogisticsBillEntity logisticsBillEntity,List<LogisticsBillDetailEntity> list) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        LogisticsBillCostDTO.AddDTO addDTO = new LogisticsBillCostDTO.AddDTO();
        //渠道关联模板
        ShippingTemplateEntity shippingTemplateEntity = shippingTemplateService.getByChannelId(logisticsBillEntity.getChannelId());

        //来源b2c销售订单
        BigDecimal length = BigDecimal.ZERO;
        BigDecimal width = BigDecimal.ZERO;
        BigDecimal height = BigDecimal.ZERO;
        if (SourceTypeEnum.SO_B2C.getCode().equals(logisticsBillEntity.getSourceType())) {
            //物流信息
            List<SoB2cLogisticsEntity> soB2cLogisticsList = soB2cFeign.listSoB2cLogisticsByMainIdList(Arrays.asList(logisticsBillEntity.getSourceId()));
            if (CollectionUtils.isEmpty(soB2cLogisticsList)) {
                throw new ServiceException(ApiError.ERROR_SO_B2C_LOGISTICS_NOT_EXIST);
            }
            //销售订单重量单位转成kg
            BigDecimal actualWeight = MathUtil.divide(soB2cLogisticsList.get(0).getWeight(), new BigDecimal(1000), 4);
            addDTO.setActualWeight(actualWeight);

            //存在模板时计算体积重
            if (ObjectUtil.isNotEmpty(shippingTemplateEntity)) {
                //模板重量单位转成kg
                BigDecimal volumeRatio = BigDecimal.ONE;
                if (UnitEnum.WeightUnitEnum.G.getCode().equals(shippingTemplateEntity.getWeightUnit())) {
                    //g
                    volumeRatio = new BigDecimal(0.001);
                }
                length = soB2cLogisticsList.get(0).getLength();
                width = soB2cLogisticsList.get(0).getWidth();
                height = soB2cLogisticsList.get(0).getHeight();
                BigDecimal volume = height
                        .multiply(width)
                        .multiply(length);
                BigDecimal volumeWeight = MathUtil.multiply(MathUtil.divide(volume, new BigDecimal(shippingTemplateEntity.getVolumeSetting())), volumeRatio, 4);
                addDTO.setVolumeWeight(volumeWeight);
            }
        }
        if (ObjectUtil.isNotEmpty(shippingTemplateEntity)) {
            //渠道
            LogisticsChannelEntity logisticsChannelEntity = logisticsChannelService.getById(logisticsBillEntity.getChannelId());

            //重量,根据计费规则判断用何种重量计算运费
            BigDecimal weight = addDTO.getActualWeight();
            if (ShippingFeeRuleEnum.BILLING_WEIGHT.getCode().equals(logisticsChannelEntity.getFeeRule())) {
                weight = MathUtil.compareTo(addDTO.getVolumeWeight(), weight) > MathUtil.ZERO ? addDTO.getVolumeWeight() : weight;
            }
            if (ShippingFeeRuleEnum.VOLUME_WEIGHT.getCode().equals(logisticsChannelEntity.getFeeRule())) {
                weight = addDTO.getVolumeWeight();
            }
            //重量转成模板单位传入计算运费
            if (UnitEnum.WeightUnitEnum.G.getCode().equals(shippingTemplateEntity.getWeightUnit())) {
                //kg
                weight = MathUtil.multiply(weight, new BigDecimal(1000));
            }
            //预估运费
            ShippingTemplateRuleDTO.ViewParamDTO viewParamDTO = new ShippingTemplateRuleDTO.ViewParamDTO();
            viewParamDTO.setWeight(weight);
            viewParamDTO.setMainId(shippingTemplateEntity.getId());
            ShippingTemplateRuleEntity shippingTemplateRule = shippingTemplateRuleService.getShippingTemplateRule(viewParamDTO);
            if (ObjectUtil.isNotEmpty(shippingTemplateRule)) {

                ShippingCalculationDTO.ViewDTO viewDTO = shippingCalculationService.calculationFinalShippingCost(shippingTemplateEntity, shippingTemplateRule, logisticsChannelEntity, weight,
                        length, width, height);

                List<TmsCfgCostEntity> tmsCfgCostList = tmsCfgCostService.listCostAttributionAndCategory(DictCostAttributionEnum.SELF_DELIVER.getCode(), DictCostCategoryEnum.SHIPPING_COST.getCode());
                if (CollectionUtils.isEmpty(tmsCfgCostList)) {
                    throw new ServiceException("未找到自发货物流费用配置");
                }
                TmsCostDetailDTO.AddDTO costDetailAddDTO = new TmsCostDetailDTO.AddDTO();
                costDetailAddDTO.setCfgCostId(tmsCfgCostList.get(0).getId());
                costDetailAddDTO.setCostValue(viewDTO.getTotalShippingCost());
                costDetailAddDTO.setType(LogisticsBillCostTypeEnum.ESTIMATED.getCode());
                costDetailAddDTO.setSourceType(SourceTypeEnum.LOGISTICS_BILL_COST.getCode());
                addDTO.setCostDetailList(Arrays.asList(costDetailAddDTO));
            }
        }
        addDTO.setCurrency(ObjectUtil.isNotEmpty(shippingTemplateEntity) ? shippingTemplateEntity.getCurrency() : CurrencyEnum.CNY.getCurrencyCode());
        addDTO.setLogisticsBillId(logisticsBillEntity.getId());
        addDTO.setChannelId(logisticsBillEntity.getChannelId());

        for (LogisticsBillDetailEntity detailEntity : list) {
            addDTO.setLogisticsBillDetailId(detailEntity.getId());
            addDTO.setTrackNo(detailEntity.getTrackNo());
            logisticsBillCostService.add(addDTO);
        }
    }

    @Override
    public List<LogisticsBillEntity> listByShopIdList(List<String> shopIdList) {
        if (CollectionUtils.isEmpty(shopIdList)) {
            return Collections.EMPTY_LIST;
        }
        return  lambdaQuery().in(LogisticsBillEntity::getShopId,shopIdList).list();
    }

    /**
     * 打印物流面单/配货单
     *
     * @param list
     * @return java.util.List<com.erp.model.oms.dto.SoB2cDTO.WaybillDTO>
     * @Author Luo_WG
     * @Date 2023/12/20 14:34
     **/
    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    public List<SoB2cDTO.WaybillDTO> printLogisticsWaybill(List<LogisticsBillDTO.PrintLogisticsWaybillDTO> list) {
        List<SoB2cDTO.WaybillDTO> waybillDTOList = new ArrayList<>();
        List<String> soIds = list.stream().map(req -> req.getB2cSoId()).distinct().collect(Collectors.toList());
        List<SoB2cEntity> soB2cEntities = soB2cFeign.listByIds(soIds);

        for (LogisticsBillDTO.PrintLogisticsWaybillDTO dto : list) {
            String channelId = dto.getChannelId();
            LogisticsSupplierDTO.AuthDTO auth = logisticsAuthService.getAuthByChannelId(channelId);
            if (Objects.isNull(auth)) {
                throw new ServiceException(ApiError.ERROR_LOGISTICS_CHANNEL_NOT_EXIST);
            }
            Map<String, String> authMap = logisticsAuthService.getLogisticsAuthConfig(auth.getAuthId(), auth.getLogisticsPlatform());

            //请求面单参数
            List<LogisticsGetLabelVO> labelVOArrayList = new ArrayList<>();
            LogisticsGetLabelVO getLabelVO = new LogisticsGetLabelVO();
            getLabelVO.setDeliveryNo(dto.getDeliveryNo());

            //平台
            String logisticsPlatform = auth.getLogisticsPlatform();
            LogisticsService service = logisticsRegistry.getHandler(logisticsPlatform);
            if (logisticsPlatform.equals(LogisticsPlatformEnum.ALI_EXPRESS.getCode())) {
                authMap = service.getLogisticsAuthConfig(dto.getShopId());
                SoB2cEntity soB2cEntity = soB2cEntities.stream().filter(req -> req.getId().equals(dto.getB2cSoId())).findFirst().orElse(null);
                if (ObjectUtil.isNotEmpty(soB2cEntity)) {
                    getLabelVO.setDeliveryNo(soB2cEntity.getPlatformCode());
                }
            }
            //如果是保宏
            if (logisticsPlatform.equals(LogisticsPlatformEnum.BAO_HONG.getCode())) {
                SoB2cEntity soB2cEntity = soB2cEntities.stream().filter(req -> req.getId().equals(dto.getB2cSoId())).findFirst().orElse(null);
                if (ObjectUtil.isNotEmpty(soB2cEntity)) {
                    getLabelVO.setDeliveryNo(soB2cEntity.getShippingOrderNo());
                }
            }

            //运单号
            getLabelVO.setTransportNo(dto.getTransportNo());

            //物流跟踪号
            List<LogisticsBillDTO.BaseDTO> baseDTOList = this.listLogisticsBillByTransportNos(Arrays.asList(dto.getTransportNo()));
            if (CollectionUtils.isNotEmpty(baseDTOList)) {
                getLabelVO.setTrackNo(baseDTOList.get(0).getTrackNo());
            }

            //授权信息
            getLabelVO.setAuthMap(authMap);

            //查询是否打印配货单
            LogisticsPlatformEnum platformEnum = LogisticsPlatformEnum.getByCode(auth.getLogisticsPlatform());

            //查询是否配置自定义
            List<LogisticsPrintTypeDTO.ViewDTO> logisticsPrintTypeEntities = logisticsPrintTypeService.listByChannelIds(Arrays.asList(channelId));
            LogisticsPrintTypeDTO.ViewDTO logisticsPrintTypeEntity = logisticsPrintTypeEntities.stream()
                    .filter(req -> LogisticsPrintTypeEnum.ALLOCATE_CARGO_BILL.getCode().equals(req.getPrintType())
                            && LogisticsLabelTypeEnum.CUSTOM.getCode().equals(req.getLabelType())
                    ).findFirst().orElse(null);
            if (ObjectUtil.isNotEmpty(logisticsPrintTypeEntity)) {
                getLabelVO.setIsPdn("N");
            } else {
                getLabelVO.setIsPdn(platformEnum.getPrintDelivery());
            }

            //设置渠道编号
            LogisticsChannelEntity channelEntity = logisticsChannelService.getById(channelId);
            LogisticsSaleChannelEntity entity = new LogisticsSaleChannelEntity();
            entity.setCode(channelEntity.getCode());
            getLabelVO.setLogisticsSaleChannelEntity(entity);
            labelVOArrayList.add(getLabelVO);

            ApiResult<List<LogisticsPrintLabelResponse>> labelList = null;
            try {
                labelList = service.getLabelList(labelVOArrayList);

            } catch (IOException e) {
                log.info("入参：{} 获取平台物流标签失败：" + e.getMessage(), labelVOArrayList.toArray());
                return Collections.emptyList();
            }
            SoB2cDTO.WaybillDTO waybillDTO = new SoB2cDTO.WaybillDTO();
            for (LogisticsPrintLabelResponse datum : labelList.getData()) {
                if ("500".equals(datum.getCode())) {
                    log.info("入参：{} 获取平台物流标签失败", labelVOArrayList.toArray());
                    throw new ServiceException(ApiError.PRINT_WAYBILL_ERROR, datum.getMessage());
                }
            }
            //获取标签信息
            List<String> logisticsBase64 = labelList.getData().stream().map(req -> req.getBase64()).distinct().collect(Collectors.toList());


            waybillDTO.setLogisticsBase64(logisticsBase64);
            waybillDTO.setDistributeBase64(logisticsBase64);
            waybillDTO.setSoB2cId(dto.getB2cSoId());
            waybillDTO.setTrackNo(getLabelVO.getTrackNo());
            waybillDTO.setTransportNo(getLabelVO.getTransportNo());
            waybillDTOList.add(waybillDTO);
        }
        return waybillDTOList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public List<BatchResultDTO> updateBatchTrackNo(List<LogisticsBillDTO.BatchUpdateTrackNoDTO> batchUpdateTrackNoDTOList, Boolean isAdd) {
        List<LogisticsBillEntity> logisticsBillEntityList = this.listByOutstockIdList(batchUpdateTrackNoDTOList.stream().map(v->v.getSoOutstockEntity().getId()).collect(Collectors.toList()));
        List<LogisticsBillDetailEntity> logisticsBillDetailEntityList = logisticsBillDetailService.listByTrackNo(
                batchUpdateTrackNoDTOList.stream()
                        .flatMap(dto -> {
                            List<String> trackNoList = dto.getTrackNoList();
                            return trackNoList != null ? trackNoList.stream() : Stream.empty();
                        })
                        .collect(Collectors.toList())
        );
        List<BatchResultDTO> batchResultDTOList = new ArrayList<>();
        List<LogisticsBillEntity> updateEntityList = new ArrayList<>();
        List<LogisticsBillDetailEntity> addDetailEntityList = new ArrayList<>();
        for(LogisticsBillDTO.BatchUpdateTrackNoDTO batchUpdateTrackNoDTO : batchUpdateTrackNoDTOList){
            SoOutstockEntity soOutstock = batchUpdateTrackNoDTO.getSoOutstockEntity();
            LogisticsBillEntity logisticsBillEntity = logisticsBillEntityList.stream().filter(v->v.getOutstockId().equals(soOutstock.getId())).findFirst().orElse(null);
            if(Objects.isNull(logisticsBillEntity)){
                //如果是修改，返回成功
//                batchResultDTOList.add(BatchResultDTO.fail(soOutstock.getId(),soOutstock.getCode(),"未生成物流单，无法更新跟踪号"));
                continue;
            }
//            LogisticsBillDetailEntity logisticsBillDetailEntity = logisticsBillDetailEntityList.stream().filter(v->batchUpdateTrackNoDTO.getTrackNoList().contains(v.getTrackNo())).findFirst().orElse(null);
//            if(Objects.nonNull(logisticsBillDetailEntity)){
//                LogisticsBillEntity existEntity = this.getById(logisticsBillDetailEntity.getMainId());
//                //如果跟踪单号已存在判断如果是新增则报错
//                if(isAdd || !logisticsBillDetailEntity.getMainId().equals(logisticsBillEntity.getId())){
//                    String msg = StrUtil.format("跟踪号【{}】已关联销售出库单【{}】，不允许重复关联", logisticsBillDetailEntity.getTrackNo(),existEntity.getOutstockCode());
//                    batchResultDTOList.add(BatchResultDTO.fail(soOutstock.getId(),soOutstock.getCode(),msg));
//                    //不过是更新则返回，避免将原有的删除
//                    if(!isAdd){
//                        return batchResultDTOList;
//                    }
//                    continue;
//                }
//            }
            if(StringUtils.isNotBlank(batchUpdateTrackNoDTO.getLogisticsChannelId())){
                LogisticsChannelEntity logisticsChannelEntity = logisticsChannelService.getById(batchUpdateTrackNoDTO.getLogisticsChannelId());
                if(Objects.isNull(logisticsChannelEntity)){
                    throw new ServiceException("查询不到渠道");
                }
                logisticsBillEntity.setLogisticsSupplierId(logisticsChannelEntity.getMainId());
                logisticsBillEntity.setChannelId(batchUpdateTrackNoDTO.getLogisticsChannelId());
                if (CollectionUtils.isNotEmpty(batchUpdateTrackNoDTO.getTrackNoList())){
                    logisticsBillEntity.setTransportNo(String.join(",", batchUpdateTrackNoDTO.getTrackNoList()));
                }
                updateEntityList.add(logisticsBillEntity);
            }
            if(CollectionUtils.isNotEmpty(batchUpdateTrackNoDTO.getTrackNoList())){
                String channelId = logisticsBillEntity.getChannelId();
                LogisticsAuthEntity authEntity = logisticsAuthService.getByChannelId(channelId);
                for(String trackNo : batchUpdateTrackNoDTO.getTrackNoList()){
                    LogisticsBillDetailEntity detailEntity = new LogisticsBillDetailEntity();
                    detailEntity.setMainId(logisticsBillEntity.getId());
                    detailEntity.setTrackNo(trackNo);
                    detailEntity.setTrackQueryMode(LogisticsPlatformEnum.TRACK123.getCode());
                    detailEntity.setIsApiUpdate(true);
                    if(StringUtils.isNotBlank(authEntity.getId())){
                        detailEntity.setLogisticsAuthId(authEntity.getId());
                    }
                    addDetailEntityList.add(detailEntity);
                }
            }
            batchResultDTOList.add(BatchResultDTO.success(soOutstock.getId(),soOutstock.getCode(),"操作成功"));
        }
        if(CollectionUtils.isNotEmpty(updateEntityList)){
            this.updateBatchById(updateEntityList);
        }
        //如果不是新增的，将原来的删除
        if(!isAdd && CollectionUtils.isNotEmpty(logisticsBillEntityList)){
            logisticsBillDetailService.removeByMainIds(logisticsBillEntityList.stream().map(BaseEntity::getId).collect(Collectors.toList()),true);
        }
        if(CollectionUtils.isNotEmpty(addDetailEntityList)){
            logisticsBillDetailService.saveBatch(addDetailEntityList);

            //新增物流费用
            for (LogisticsBillEntity billEntity: logisticsBillEntityList) {
                List<LogisticsBillDetailEntity> detailList = addDetailEntityList.stream().filter(obj -> StrUtil.equals(obj.getMainId(), billEntity.getId())).collect(Collectors.toList());
                if (CollectionUtils.isEmpty(detailList)) {
                    continue;
                }
                //新增物流费用单
                logisticsBillService.addLogisticsBillCost(billEntity, detailList);
            }
        }
        return batchResultDTOList;
    }

    @Override
    public Map<String, List<String>> mapTrackNoAndSoOutId(List<String> ids) {
        List<LogisticsBillEntity> logisticsBillEntityList = this.listByOutstockIdList(ids);
        Map<String, List<String>> resultMap = new HashMap<>();

        if(CollectionUtils.isEmpty(logisticsBillEntityList)){
            return resultMap;
        }

        //查询明细，根据主表id分组
        Map<String, List<LogisticsBillDetailEntity>> detailEntityMap = logisticsBillDetailService.listByMainIds(
                        logisticsBillEntityList.stream()
                                .map(BaseEntity::getId)
                                .collect(Collectors.toList())
                ).stream()
                .collect(Collectors.groupingBy(LogisticsBillDetailEntity::getMainId));

        //封装结果map
        for (LogisticsBillEntity entity : logisticsBillEntityList) {
            List<LogisticsBillDetailEntity> detailEntityList = detailEntityMap.get(entity.getId());
            if (!CollectionUtils.isEmpty(detailEntityList)) {
                resultMap.computeIfAbsent(entity.getOutstockId(), k -> new ArrayList<>())
                        .addAll(detailEntityList.stream()
                                .map(LogisticsBillDetailEntity::getTrackNo)
                                .collect(Collectors.toList()));
            }
        }
        return resultMap;
    }

    @Override
    public List<String> listSoOutIdByQuery(AdvanceQueryContainer advanceQueryContainer) {
        return baseMapper.listSoOutIdByQuery(advanceQueryContainer);
    }

    @Override
    public Boolean removeLogisticsBillBySourceId(List<String> sourceId) {
        if (CollectionUtils.isEmpty(sourceId)){
            return Boolean.FALSE;
        }

        //删除物流详情
        List<LogisticsBillEntity> logisticsBillEntityList = this.listBySourceIds(sourceId);
        List<String> ids = logisticsBillEntityList.stream().map(req -> req.getId()).collect(Collectors.toList());
        logisticsBillDetailService.removeByMainIds(ids,Boolean.TRUE);

        //删除主表
        return lambdaUpdate().in(LogisticsBillEntity::getSourceId, sourceId).remove();
    }
}
