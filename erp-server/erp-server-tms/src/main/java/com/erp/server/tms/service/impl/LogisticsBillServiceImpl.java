package com.erp.server.tms.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
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
import com.common.business.wrapper.FeignQuery;
import com.common.core.constant.SqlConstants;
import com.common.core.controller.vo.ApiResult;
import com.common.core.entity.BaseEntity;
import com.common.core.enums.ApiError;
import com.common.core.enums.CurrencyEnum;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.FileUtil;
import com.common.core.utils.MathUtil;
import com.erp.model.dmp.enums.DmpBasicSystemCodeEnum;
import com.erp.model.oms.dto.SoB2cDTO;
import com.erp.model.oms.entity.DictBasicEntity;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.oms.entity.SoB2cLogisticsEntity;
import com.erp.model.oms.enums.DictBasicTypeEnum;
import com.erp.model.sys.entity.DictCountryEntity;
import com.erp.model.sys.entity.DictCountryOrgEntity;
import com.erp.model.sys.enums.ChargeSuperiorEnum;
import com.erp.model.tms.dto.*;
import com.erp.model.tms.entity.*;
import com.erp.model.tms.enums.*;
import com.erp.model.tms.vo.request.*;
import com.erp.model.tms.vo.response.CancelResponseVO;
import com.erp.model.tms.vo.response.InterceptResponseVO;
import com.erp.model.tms.vo.response.LogisticsOrderResponseVO;
import com.erp.model.tms.vo.response.LogisticsPrintLabelResponse;
import com.erp.model.wms.dto.FirstMileDeliveryDTO;
import com.common.business.dto.ShudiyunB2cOrderDTO;
import com.erp.model.wms.entity.SoOutstockEntity;
import com.erp.model.wms.enums.B2cDeliveryLogisticTypeEnum;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.oms.feign.CfgRuleFeign;
import com.erp.rpc.oms.feign.SoB2cFeign;
import com.erp.rpc.sys.feign.SysDictFeign;
import com.erp.rpc.wms.feign.WmsFirstMileDeliveryFeign;
import com.erp.server.tms.constant.TmsConstant;
import com.erp.server.tms.convert.LogisticsBillConverter;
import com.erp.server.tms.handler.LogisticsRegistry;
import com.erp.server.tms.mapper.LogisticsBillMapper;
import com.erp.server.tms.service.*;
import com.sdk.oms.mercado.service.MercadoSdkClientService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_TMS_LOGISTICS_BILL;

/**
 * 物流单 服务实现类
 *
 * @author lambda
 * @since 2023-11-09
 */
@Slf4j
@Service
public class LogisticsBillServiceImpl extends SuperServiceImpl<LogisticsBillMapper, LogisticsBillEntity> implements LogisticsBillService {
    @Resource
    private OperateLogService operateLogService;
    @Resource
    private LogisticsBillDetailService logisticsBillDetailService;

    @Resource
    private DictBasicService dictBasicService;

    @Resource
    private LogisticsBillCostService logisticsBillCostService;

    @Resource
    private SoB2cFeign soB2cFeign;

    @Resource
    private ShippingTemplateService shippingTemplateService;

    @Resource
    private ShippingCalculationService shippingCalculationService;

    @Resource
    private ShippingTemplateRuleService shippingTemplateRuleService;

    @Resource
    private LogisticsAuthService logisticsAuthService;

    @Resource
    private LogisticsRegistry logisticsRegistry;

    @Resource
    private LogisticsAddressService logisticsAddressService;
    @Resource
    private LogisticsChannelService logisticsChannelService;

    @Resource
    private LogisticsSaleChannelService logisticsSaleChannelService;

    @Resource
    private LogisticsPrintTypeService logisticsPrintTypeService;

    @Resource
    private TmsCfgCostService tmsCfgCostService;

    @Resource
    private LogisticsBillService logisticsBillService;

    @Resource
    private MercadoSdkClientService mercadoSdkClientService;

    @Resource
    private CfgRuleFeign cfgRuleFeign;
    @Resource
    private SysDictFeign sysDictFeign;
    @Resource
    private DownloadTaskFeign downloadTaskFeign;
    @Resource
    private WmsFirstMileDeliveryFeign wmsFirstMileDeliveryFeign;
    @Resource
    private LogisticsSupplierService logisticsSupplierService;

    @Resource
    private TmsPushMsgService tmsPushMsgService;

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

        //同步速递云运单
        pushSdyFieldHandler(logisticsBillEntity,SyncOperateEnum.OPERATE_APPROVE.getCode());
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
        String msg = CharSequenceUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), logisticsBillEntity.getId(), "物流单");
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
            List<LogisticsBillEntity> logisticsBillEntityList = this.listByIds(ids);
            //同步速递云运单
            logisticsBillEntityList.forEach(req -> pushSdyFieldHandler(req, SyncOperateEnum.OPERATE_DELETE.getCode()));

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
        if (CharSequenceUtil.equals(logisticsBillEntity.getSourceType(),SourceTypeEnum.SO_B2C.getCode())) {
            SoB2cEntity soB2cEntity = soB2cFeign.getById(logisticsBillEntity.getSourceId());
            if (ObjectUtil.isNotEmpty(soB2cEntity)) {
                logisticsBillEntity.setPlatformCode(soB2cEntity.getPlatformCode());
            }
        }
        if(CharSequenceUtil.isNotBlank(logisticsBillEntity.getOutstockId())){
            List<FirstMileDeliveryDTO.BusinessDTO> businessDTOList = wmsFirstMileDeliveryFeign.getBusinessCodeByIds(Collections.singletonList(logisticsBillEntity.getOutstockId()));
            logisticsBillEntity.setBusinessCode(CollectionUtils.isNotEmpty(businessDTOList) ? businessDTOList.get(0).getBusinessCode() : "");
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

            //同步速递云运单
            pushSdyFieldHandler(saveEntity, SyncOperateEnum.OPERATE_APPROVE.getCode());
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
        Page<LogisticsBillDTO.PagingVO> query = new Page<>(dto.getCurrPage(), dto.getPageSize());
        IPage<LogisticsBillDTO.PagingVO> pageData = baseMapper.paging(query, params);
        List<LogisticsBillDTO.PagingVO> list = pageData.getRecords();
        fillPagingDb(list);
        return new PagingVO<>(pageData);
    }



    @Override
    public Boolean exportExcel(LogisticsBillDTO.PagingParamDTO params) {
        downloadTaskFeign.saveDownloadTask("自发货物流单列表", EXPORT_TMS_LOGISTICS_BILL.getCode(), params);
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
        Map<String, String> authMap = logisticsAuthService.getLogisticsAuthConfig(auth.getAuthId(),dto.getShopId(), auth.getLogisticsPlatform());
        LogisticsChannelEntity logisticsChannel = logisticsChannelService.getById(channelId);
        if (Objects.isNull(logisticsChannel)) {
            throw new ServiceException(ApiError.ERROR_LOGISTICS_CHANNEL_NOT_EXIST);
        }
        String country = Objects.nonNull(dto.getReceiver())?Objects.nonNull(dto.getReceiver().getCountry())?dto.getReceiver().getCountry():"":"";
        LogisticsAddressTypeEnum deliverType = LogisticsAddressTypeEnum.DELIVER;
        //物流类型是中转发货则使用中转地址类型
        if (StringUtils.isNotBlank(dto.getLogisticType()) && LogisticsAddressTypeEnum.TRANSFER.getCode().equals(dto.getLogisticType())) {
            deliverType = LogisticsAddressTypeEnum.TRANSFER;
        }
        //收货人地址信息
        List<LogisticsAddressEntity> addressList = logisticsAddressService.listByChannelIdAndShopId(channelId, dto.getShopId());
        LogisticsAddressTypeEnum finalDeliverType = deliverType;
        List<LogisticsAddressEntity> deliverList = addressList.stream().filter(a -> finalDeliverType.equals(a.getType())).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(deliverList)) {
            throw new ServiceException(ApiError.ERROR_CHANNEL_ADDRESS_NOT_EXIST, logisticsChannel.getName(), deliverType.getName());
        }
        //发货人信息
        LogisticsAddressEntity logisticsAddress = deliverList.get(0);
        SenderInfo senderInfo = LogisticsBillConverter.INSTANCE.convertSender(logisticsAddress);
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
        if (CharSequenceUtil.isNotBlank(dto.getToken())){
            authMap.put("token", dto.getToken());
        }
        //来源
        String sourceType = dto.getSourceType();
        //订单类型
        String orderType = dto.getOrderType();
        //收货人
        LogisticsBillDTO.ReceiverDTO receiverDTO = dto.getReceiver();
        //转化成收货人
        ReceiverInfoVO receiverInfo = LogisticsBillConverter.INSTANCE.convertReceiver(receiverDTO);
        //申报信息-sku拆分
        SoB2cEntity soB2cEntity = soB2cFeign.getById(dto.getOrderId());

        if (ObjectUtil.isNotEmpty(soB2cEntity)) {
            SoB2cDTO.LabelDTO labelJsonDTO = JSONUtil.toBean(soB2cEntity.getLabelJson(), SoB2cDTO.LabelDTO.class);
            //增加判断null值
            if (Objects.nonNull(labelJsonDTO) && Objects.nonNull(labelJsonDTO.getIsPlatformWarehouseOrder()) && labelJsonDTO.getIsPlatformWarehouseOrder()) {
                //中转地址
                LogisticsAddressEntity logisticsAddressEntity = addressList.stream().filter(a -> LogisticsAddressTypeEnum.TRANSFER.equals(a.getType())).findFirst().orElse(null);
                if (Objects.nonNull(logisticsAddressEntity)) {
                    receiverInfo = LogisticsBillConverter.INSTANCE.LogisticsAddressEntityToReceiverInfoVO(logisticsAddressEntity);
                }
            }
        }


        //申报信息
        List<LogisticsProductVO> productVOS = dto.getProductVOS();
        //包裹信息
        LogisticsBillDTO.PackageDTO packageDTO = dto.getPackageInfo();
        ParceInfoVO parceInfo = LogisticsBillConverter.INSTANCE.convertParceInfo(packageDTO);
        Boolean hasBattery = productVOS.stream().filter(e -> Objects.nonNull(e.getIsElectric())).anyMatch(LogisticsProductVO::getIsElectric);
        //是否带电
        parceInfo.setHasBattery(hasBattery);
        Integer totalQuantity = productVOS.stream().filter(e -> Objects.nonNull(e.getQuantity())).mapToInt(LogisticsProductVO::getQuantity).sum();
        parceInfo.setTotalQuantity(totalQuantity);

        //申报总价
        BigDecimal totalPrice = productVOS.stream().filter(s -> Objects.nonNull(s.getDestDeclarePrice()) && Objects.nonNull(s.getQuantity()))
                .map(e -> MathUtil.multiply(e.getDestDeclarePrice(), e.getQuantity())).reduce(BigDecimal.ZERO, BigDecimal::add);
        parceInfo.setTotalPrice(totalPrice);
        //根据销售平台和渠道code 获取到原生的渠道
        LogisticsSaleChannelEntity saleChannel = logisticsSaleChannelService.getByPlatform(logisticsPlatform, logisticsChannel.getCode());
        if (Objects.isNull(saleChannel)) {
            throw new ServiceException(ApiError.ERROR_SALES_CHANNEL_NOT_EXIST, logisticsChannel.getName());
        }
        //根据订单处理规则，判断是否需要清空国家、省市数据
        Map<String,Object> map = getRuleOrderHandleMap(dto);
        LogisticsOrderVO logisticsOrderVO = LogisticsOrderVO.builder().authMap(authMap).
                orderSource(sourceType).
                orderType(orderType).
                trackNo(dto.getTrackNo()).
                topUserKey(dto.getTopUserKey()).
                sourceId(dto.getOrderId()).
                oaid(dto.getOaid()).
                deliveryNo(dto.getOrderCode()).
                platformCode(dto.getPlatformCode()).
                packageNumber(dto.getPackageNumber()).
                country(country).
                voecTaxNo(dto.getVoecTaxNo()).
                iossCode(getIossCodeByCountry(country,logisticsChannel.getIsIossPrepay(),dto.getIossTaxNo())).
                senderInfo(senderInfo).
                returnInfo(returnInfo).
                receiverInfoVO(receiverInfo).
                parceInfoVO(parceInfo).
                logisticsProductVOList(productVOS).
                logisticsChannelEntity(logisticsChannel).
                logisticsSaleChannel(saleChannel).
                build();
        //根据规则处理物流单请求参数
        logisticsOrderVO = cfgRuleFeign.handleRuleOrderLogistic(LogisticsOrderRuleVO.builder().logisticsOrderVO(logisticsOrderVO).map(map).build());
        log.info("创建订单,参数:{}", JSONUtil.toJsonStr(logisticsOrderVO));
        ApiResult<LogisticsOrderResponseVO> orderResult = service.createOrder(logisticsOrderVO);
        //表示成功
        if (orderResult.isSuccess()) {
            return LogisticsBillDTO.GenerateBillResultDTO.builder()
                    .trackNo(orderResult.getData().getTrackNo())
                    .transportNo(orderResult.getData().getTransportNo())
                    .build();
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
     * 根据国家是否归属欧盟进行判断是否填写ioss号
     * @param country
     * @param isIossPrepay
     * @param iossTaxNo
     * @return
     */
    private String getIossCodeByCountry(String country, Boolean isIossPrepay, String iossTaxNo) {
        if (StringUtils.isEmpty(country) || Objects.isNull(isIossPrepay) || !isIossPrepay || StringUtils.isEmpty(iossTaxNo)){
            return StringUtils.EMPTY;
        }
        //判断国家是否是欧盟
        List<DictCountryOrgEntity> dictList = sysDictFeign.listCountryOrgByOrgCode("EU");
        DictCountryOrgEntity countryOrg = dictList.stream().filter(e -> e.getCountryId().equals(country)).findFirst().orElse(null);
        if (Objects.isNull(countryOrg)){
            return StringUtils.EMPTY;
        }else {
            return iossTaxNo;
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

    @Override
    public List<LogisticsBillEntity> listByOutstockIdList(List<String> outstockIdList) {
        if (CollectionUtils.isEmpty(outstockIdList)) {
            return Collections.emptyList();
        }
        return this.lambdaQuery().in(LogisticsBillEntity::getOutstockId, outstockIdList).list();
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
        cancelOrderVO.setPlatformCode(dto.getPlatformCode());
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
        Map<String, String> authMap = logisticsAuthService.getLogisticsAuthConfig(auth.getAuthId(),dto.getShopId(), auth.getLogisticsPlatform());
        cancelOrderVO.setAuthMap(authMap);
        //平台
        String logisticsPlatform = auth.getLogisticsPlatform();
        LogisticsService service = logisticsRegistry.getHandler(logisticsPlatform);
        ApiResult<List<CancelResponseVO>> thirdPartyResult = service.cancelOrder(cancelOrderList);
        //是否成功
        if (thirdPartyResult.isSuccess()) {
            //将自发货费用状态改成作废
            LogisticsBillEntity logisticsBillEntity = this.lambdaQuery().eq(LogisticsBillEntity::getTransportNo, dto.getTransportNo()).last(SqlConstants.LIMIT_1).one();
            if (Objects.nonNull(logisticsBillEntity)) {
                logisticsBillCostService.invalidByLogisticsBillId(logisticsBillEntity.getId());
            }
        }else{
            if(thirdPartyResult.getCode()!=-1){
                String msg = thirdPartyResult.getData().stream().map(v->v.getMessage()+";").collect(Collectors.toList()).toString();
                thirdPartyResult.setMsg(msg);
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
        interceptOrderVO.setPlatformCode(dto.getPlatformCode());
        interceptOrderVOList.add(interceptOrderVO);
        if (StringUtils.isBlank(dto.getTransportNo())) {
            LogisticsBillDTO.BaseDTO billBase = this.getBaseByTrackNo(dto.getTrackNo());
            if (Objects.isNull(billBase.getId())) {
                throw new ServiceException(ApiError.NOT_EXIST_BILL, "物流单");
            }
            interceptOrderVO.setTransportNo(billBase.getTransportNo());
        }
        Map<String, String> authMap = logisticsAuthService.getLogisticsAuthConfig(auth.getAuthId(),dto.getShopId(), auth.getLogisticsPlatform());
        interceptOrderVO.setAuthMap(authMap);
        //平台
        String logisticsPlatform = auth.getLogisticsPlatform();
        LogisticsService service = logisticsRegistry.getHandler(logisticsPlatform);
        ApiResult<List<InterceptResponseVO>> thirdPartyResult = service.interceptOrder(interceptOrderVOList);
        //是否成功
        if (thirdPartyResult.isSuccess()) {
            //将自发货费用状态改成作废
            LogisticsBillEntity logisticsBillEntity = this.lambdaQuery().eq(LogisticsBillEntity::getTransportNo, dto.getTransportNo()).last(SqlConstants.LIMIT_1).one();
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
        String signCode = LogisticTrackStatusEnum.SIGN.getCode();
        //销售平台字典表数据
        Map<String, String> salesPlatformMap = new HashMap<>();
        List<DictBasicEntity> salesPlatformList = FeignQuery.create(DictBasicEntity.class)
                .eq(DictBasicEntity::getType, DictBasicTypeEnum.SALES_PLATFORM.getType())
                .eq(DictBasicEntity::getStatus, Boolean.TRUE)
                .eq(DictBasicEntity::getIsDeleted, Boolean.FALSE)
                .list();
        if(CollectionUtils.isNotEmpty(salesPlatformList)){
            salesPlatformMap = salesPlatformList.stream().collect(Collectors.toMap(DictBasicEntity::getValue, DictBasicEntity::getName));
        }
        for (LogisticsBillDTO.PagingVO item : list) {
            //是否签收
            boolean isSign = signCode.equals(item.getTrackStatus());
            item.setSalesPlatformName(salesPlatformMap.get(item.getSalesPlatform()));
            //发货时间
            LocalDateTime deliveryTime = item.getDeliveryTime();
            int transportDays = 0;
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
                    volumeRatio = new BigDecimal("0.001");
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
            //目的国
            List<DictCountryEntity> dictCountryEntityList = sysDictFeign.listCountryByNames(Arrays.asList(logisticsBillEntity.getToCountry()));
            viewParamDTO.setToCountry(CollUtil.isNotEmpty(dictCountryEntityList) ? dictCountryEntityList.get(0).getId() : "");
            viewParamDTO.setWeight(weight);
            viewParamDTO.setMainId(shippingTemplateEntity.getId());
            ShippingTemplateRuleEntity shippingTemplateRule = shippingTemplateRuleService.getShippingTemplateRule(viewParamDTO);
            if (ObjectUtil.isNotEmpty(shippingTemplateRule)) {

                ShippingCalculationDTO.ViewDTO viewDTO = shippingCalculationService.calculationFinalShippingCost(shippingTemplateEntity, shippingTemplateRule, logisticsChannelEntity, weight,
                        length, width, height);

                List<TmsCfgCostEntity> tmsCfgCostList = tmsCfgCostService.listCostAttributionAndCategory(DictCostAttributionEnum.SELF_DELIVER.getCode(), null);
                List<TmsCostDetailDTO.AddDTO>  costDetailList = new ArrayList<>();
                //预估运费
                TmsCostDetailDTO.AddDTO costDetailAddDTO = new TmsCostDetailDTO.AddDTO();
                if (CollectionUtils.isNotEmpty(tmsCfgCostList)){
                    TmsCfgCostEntity tmsCfgCostEntity = tmsCfgCostList.stream().filter(e -> CharSequenceUtil.isNotBlank(e.getDictCostCategory()) && e.getDictCostCategory().equals(DictCostCategoryEnum.SHIPPING_COST.getCode())).findFirst().orElse(null);
                    if (Objects.nonNull(tmsCfgCostEntity)){
                        costDetailAddDTO.setCfgCostId(tmsCfgCostEntity.getId());
                        //取值优化为费用类型【运费+挂号费+操作费用】【若有折扣则按照折扣计算】
                        BigDecimal shippingCost = viewDTO.getShippingCost().add(viewDTO.getRegistrationCost()).add(viewDTO.getOperatingCost()).subtract(viewDTO.getDiscountCost());
                        costDetailAddDTO.setCostValue(shippingCost);
                        costDetailAddDTO.setType(LogisticsBillCostTypeEnum.ESTIMATED.getCode());
                        costDetailAddDTO.setSourceType(SourceTypeEnum.LOGISTICS_BILL_COST.getCode());
                        costDetailList.add(costDetailAddDTO);
                    }
                }
                //预估报关费用
                TmsCostDetailDTO.AddDTO costDetailAddDTO1 = new TmsCostDetailDTO.AddDTO();
                if (CollectionUtils.isNotEmpty(tmsCfgCostList)){
                    TmsCfgCostEntity tmsCfgCostEntity = tmsCfgCostList.stream().filter(e -> CharSequenceUtil.isNotBlank(e.getDictCostCategory()) && e.getDictCostCategory().equals(DictCostCategoryEnum.DECLARE_COST.getCode())).findFirst().orElse(null);
                    if (Objects.nonNull(tmsCfgCostEntity)){
                        costDetailAddDTO1.setCfgCostId(tmsCfgCostEntity.getId());
                        //目前无计算规则取值显示为0
                        costDetailAddDTO1.setCostValue(BigDecimal.ZERO);
                        costDetailAddDTO1.setType(LogisticsBillCostTypeEnum.ESTIMATED.getCode());
                        costDetailAddDTO1.setSourceType(SourceTypeEnum.LOGISTICS_BILL_COST.getCode());
                        costDetailList.add(costDetailAddDTO1);
                    }
                }
                //预估其他费用
                TmsCostDetailDTO.AddDTO costDetailAddDTO2 = new TmsCostDetailDTO.AddDTO();
                if (CollectionUtils.isNotEmpty(tmsCfgCostList)){
                    TmsCfgCostEntity tmsCfgCostEntity = tmsCfgCostList.stream().filter(e -> CharSequenceUtil.isNotBlank(e.getDictCostCategory()) && e.getDictCostCategory().equals(DictCostCategoryEnum.OTHER_COST.getCode())).findFirst().orElse(null);
                    if (Objects.nonNull(tmsCfgCostEntity)){
                        costDetailAddDTO2.setCfgCostId(tmsCfgCostEntity.getId());
                        //按照计算模板计算类型【超尺寸附加费+签名费+燃油附加费+保险费】【若有折扣则按照折扣计算】
                        BigDecimal otherCost = viewDTO.getOversizeSurchargeCost().add(viewDTO.getSignatureCost()).add(viewDTO.getFuelSurchargeCost()).add(viewDTO.getPremiumCost()).subtract(viewDTO.getDiscountCost());
                        costDetailAddDTO2.setCostValue(otherCost);
                        costDetailAddDTO2.setType(LogisticsBillCostTypeEnum.ESTIMATED.getCode());
                        costDetailAddDTO2.setSourceType(SourceTypeEnum.LOGISTICS_BILL_COST.getCode());
                        costDetailList.add(costDetailAddDTO2);
                    }
                }
                addDTO.setCostDetailList(costDetailList);
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

    @Override
    public List<LogisticsBillEntity> listBySoOutStockIdList(List<String> outstockIdList) {
        if (CollectionUtils.isEmpty(outstockIdList)) {
            return Collections.EMPTY_LIST;
        }
        return  lambdaQuery().in(LogisticsBillEntity::getOutstockId,outstockIdList).list();
    }

    @Override
    public ApiResult<String> updateLogisticWeight(LogisticsBillDTO.UpdateWeight dto) {
        SoB2cLogisticsEntity soB2cLogisticsEntity = dto.getSoB2cLogisticsEntity();
        SoB2cEntity soB2cEntity = dto.getSoB2cEntity();
        String channelId = soB2cLogisticsEntity.getLogisticsChannelId();
        LogisticsSupplierDTO.AuthDTO auth = logisticsAuthService.getAuthByChannelId(channelId);
        if (Objects.isNull(auth)) {
            throw new ServiceException(ApiError.ERROR_LOGISTICS_CHANNEL_NOT_EXIST);
        }
        if (StringUtils.isBlank(soB2cEntity.getCode())) {
            throw new ServiceException("订单为空");
        }
        if (StringUtils.isBlank(soB2cLogisticsEntity.getCode())) {
            throw new ServiceException("物流单号为空");
        }
        if (Objects.isNull(soB2cLogisticsEntity.getWeight())) {
            throw new ServiceException("重量为空");
        }
        Map<String, String> authMap = logisticsAuthService.getLogisticsAuthConfig(auth.getAuthId(),soB2cEntity.getShopId(), auth.getLogisticsPlatform());
        LogisticsUpdateWeightVO logisticsUpdateWeightVO = LogisticsUpdateWeightVO.builder()
                .deliveryNo(soB2cEntity.getCode())
                .platformCode(soB2cEntity.getPlatformCode())
                .transportNo(soB2cLogisticsEntity.getCode())
                .weight(soB2cLogisticsEntity.getWeight())
                .authMap(authMap)
                .trackNo(soB2cLogisticsEntity.getTrackNo())
                .build();
        //平台
        String logisticsPlatform = auth.getLogisticsPlatform();
        LogisticsService service = logisticsRegistry.getHandler(logisticsPlatform);
        ApiResult<String> result = service.updateWeight(logisticsUpdateWeightVO);
        if(result.getCode() != -1){
            if(result.isSuccess()){
                log.warn("物流商更新重量成功:{}",JSONUtil.toJsonStr(logisticsUpdateWeightVO));
            }else{
                log.warn("物流商更新重量失败:{}",JSONUtil.toJsonStr(logisticsUpdateWeightVO));
            }
        }
        return result;
    }

    @Override
    public PagingVO<LogisticsBillDTO.PagingVO> exportLogisticsBill(PagingDTO<LogisticsBillDTO.PagingParamDTO> dto) {
        Page<LogisticsBillDTO.PagingVO> page = baseMapper.listExport(new Page<>(dto.getCurrPage(), dto.getPageSize()), dto.getParams());
        fillPagingDb(page.getRecords());
        return new PagingVO<>(page);
    }

    @Override
    public List<LogisticsBillDTO.LogisticsBillVo> listLogisticsBillVoByTrackNo(List<String> trackNoList) {
        if (CollectionUtils.isEmpty(trackNoList)){
            return Collections.emptyList();
        }
        return baseMapper.listLogisticsBillVoByTrackNo(trackNoList);
    }

    @Override
    public LogisticsBillDTO.BaseDTO getByTrackNoOrTransportNo(String logisticsCode) {
        if (StringUtils.isBlank(logisticsCode)) {
            return new LogisticsBillDTO.BaseDTO();
        }
        return baseMapper.getByTrackNoOrTransportNo(logisticsCode);
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
    public List<SoB2cDTO.WaybillDTO> printLogisticsWaybill(List<LogisticsBillDTO.PrintLogisticsWaybillDTO> list) {
        List<SoB2cDTO.WaybillDTO> waybillDTOList = new ArrayList<>();
        List<String> soIds = list.stream().map(req -> req.getB2cSoId()).distinct().collect(Collectors.toList());
        List<SoB2cEntity> soB2cEntities = soB2cFeign.listByIds(soIds);
        List<SoB2cLogisticsEntity> logisticsEntityList = soB2cFeign.listSoB2cLogisticsByMainIdList(soIds);
        List<String> errorList = new ArrayList<>();
        for (LogisticsBillDTO.PrintLogisticsWaybillDTO dto : list) {
            try {
                SoB2cEntity soB2cEntity = soB2cEntities.stream().filter(req -> req.getId().equals(dto.getB2cSoId())).findFirst().orElse(new SoB2cEntity());
                String channelId = dto.getChannelId();
                LogisticsSupplierDTO.AuthDTO auth = logisticsAuthService.getAuthByChannelId(channelId);
                if (Objects.isNull(auth)) {
                    throw new ServiceException(ApiError.ERROR_LOGISTICS_CHANNEL_NOT_EXIST);
                }
                Map<String, String> authMap = logisticsAuthService.getLogisticsAuthConfig(auth.getAuthId(),dto.getShopId(), auth.getLogisticsPlatform());

                //请求面单参数
                List<LogisticsGetLabelVO> labelVOArrayList = new ArrayList<>();
                LogisticsGetLabelVO getLabelVO = new LogisticsGetLabelVO();
                getLabelVO.setDeliveryNo(dto.getDeliveryNo());
                getLabelVO.setOrderId(dto.getB2cSoId());
                //平台
                String logisticsPlatform = auth.getLogisticsPlatform();
                LogisticsService service = logisticsRegistry.getHandler(logisticsPlatform);
                if (logisticsPlatform.equals(LogisticsPlatformEnum.ALI_EXPRESS.getCode())) {
//                authMap = service.getLogisticsAuthConfigByShopId(dto.getShopId());
                    if (ObjectUtil.isNotEmpty(soB2cEntity)) {
                        getLabelVO.setDeliveryNo(soB2cEntity.getPlatformCode());
                    }
                }
                getLabelVO.setPlatformCode(soB2cEntity.getPlatformCode());
                //如果是保宏
                if (logisticsPlatform.equals(LogisticsPlatformEnum.BAO_HONG.getCode())) {
                    if (ObjectUtil.isNotEmpty(soB2cEntity)) {
                        getLabelVO.setDeliveryNo(soB2cEntity.getShippingOrderNo());
                    }
                }

                //运单号
                getLabelVO.setTransportNo(dto.getTransportNo());

                //物流跟踪号
                SoB2cLogisticsEntity soB2cLogisticsEntity = logisticsEntityList.stream().filter(v->v.getMainId().equals(soB2cEntity.getId())).findFirst().orElse(null);
                if (Objects.isNull(soB2cLogisticsEntity)){
                    throw new ServiceException("销售订单【{}】物流信息为空不能进行面单打印", soB2cEntity.getCode());
                }
                getLabelVO.setTransportNo(soB2cLogisticsEntity.getCode());
                getLabelVO.setTrackNo(soB2cLogisticsEntity.getTrackNo());

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

                //发起请求第三方接口获取标签信息
                ApiResult<List<LogisticsPrintLabelResponse>> labelList = null;
                try {
                    labelList = service.getLabelList(labelVOArrayList);
                } catch (IOException e) {
                    throw new ServiceException("获取物流面单异常"+e.getMessage());
                }

                //校验是否请求成功
                if (!labelList.isSuccess()) {
                    throw new ServiceException(ApiError.PRINT_WAYBILL_ERROR, labelList.getMsg());
                }
                for (LogisticsPrintLabelResponse datum : labelList.getData()) {
                    if ("500".equals(datum.getCode())) {
                        throw new ServiceException(ApiError.PRINT_WAYBILL_ERROR, datum.getMessage());
                    }
                }

                //获取标签信息
                List<String> logisticsBase64 = labelList.getData().stream().map(req -> req.getBase64()).distinct().collect(Collectors.toList());

                //如果是美客户多且是中转发货需要调美客多接口再打一张平台标签
                String labelUrl = "";
                if (logisticsPlatform.equals(LogisticsPlatformEnum.MERCADOLIBRE.getCode()) && B2cDeliveryLogisticTypeEnum.TRANSIT_SHIPMENT.getCode().equals(dto.getLogisticType())) {
                    try {
                        JSONObject jsonObject = JSON.parseObject(soB2cEntity.getExtendData());
                        String shipmentId = String.valueOf(jsonObject.get("shipmentId"));
                        labelUrl = mercadoSdkClientService.printShippingLabel(authMap, Long.valueOf(shipmentId));

                        String base64 = FileUtil.convertPdfUrlToBase64(labelUrl);
                        logisticsBase64.add(base64);
                    } catch (IOException e) {
                        log.error("token信息={},入参params={}, 美客多标签打印失败，返回值 responseMap={}", authMap , JSON.parseObject(soB2cEntity.getExtendData()), JSONUtil.toJsonStr(labelUrl));
                        throw new ServiceException(ApiError.PRINT_WAYBILL_ERROR, e.getMessage());
                    }
                }

                //返回值
                SoB2cDTO.WaybillDTO waybillDTO = new SoB2cDTO.WaybillDTO();
                waybillDTO.setLogisticsBase64(logisticsBase64);
                waybillDTO.setDistributeBase64(logisticsBase64);
                waybillDTO.setSoB2cId(dto.getB2cSoId());
                waybillDTO.setTrackNo(getLabelVO.getTrackNo());
                waybillDTO.setTransportNo(getLabelVO.getTransportNo());
                waybillDTOList.add(waybillDTO);
            }catch (Exception e){
                String msg = CharSequenceUtil.format("{}获取物流面单异常->{}",dto.getDeliveryNo(),e.getMessage());
                log.error(msg,e);
                errorList.add(msg);
            }
        }
        if(CollectionUtils.isNotEmpty(errorList)){
            throw new ServiceException(errorList.stream()
                    .map(s -> s + "<br/>")
                    .collect(Collectors.joining()));
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
//                    String msg = CharSequenceUtil.format("跟踪号【{}】已关联销售出库单【{}】，不允许重复关联", logisticsBillDetailEntity.getTrackNo(),existEntity.getOutstockCode());
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
                List<LogisticsBillDetailEntity> detailList = addDetailEntityList.stream().filter(obj -> CharSequenceUtil.equals(obj.getMainId(), billEntity.getId())).collect(Collectors.toList());
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

    @Override
    public void initLogisticsBillBusinessCode() {
        List<LogisticsBillEntity> list = this.lambdaQuery()
                .select(LogisticsBillEntity::getId,LogisticsBillEntity::getOutstockId)
                .eq(LogisticsBillEntity::getOrderType, OrderTypeEnum.FIRST_MILE.getCode()).list();
        List<List<LogisticsBillEntity>> partition = ListUtil.partition(list, 100);
        for (List<LogisticsBillEntity> billEntityList : partition){
            List<String> outStockIds = billEntityList.stream().map(LogisticsBillEntity::getOutstockId).distinct().collect(Collectors.toList());
            List<FirstMileDeliveryDTO.BusinessDTO> businessDTOList = wmsFirstMileDeliveryFeign.getBusinessCodeByIds(outStockIds);
            if (CollectionUtils.isEmpty(businessDTOList)){
                continue;
            }
            for (LogisticsBillEntity entity : billEntityList){
                FirstMileDeliveryDTO.BusinessDTO businessDTO = businessDTOList.stream().filter(e -> Objects.equals(entity.getOutstockId(), e.getId())).findFirst().orElse(null);
                if (Objects.isNull(businessDTO)){
                    continue;
                }
                String businessCode = CharSequenceUtil.isBlank(businessDTO.getBusinessCode()) ? StrUtil.EMPTY : businessDTO.getBusinessCode();
                this.lambdaUpdate().eq(LogisticsBillEntity::getId, entity.getId()).set(LogisticsBillEntity::getBusinessCode, businessCode).update();
            }
        }
    }

    /**
     * 同步速递云销售出库单
     * @param entity
     * @param operateEnum
     */
    public void pushSdyFieldHandler(LogisticsBillEntity entity, String operateEnum) {
        if (ObjectUtil.isEmpty(entity)) {
            log.error("运单同步数帝云失败入参：error={}", entity);
            return;
        }
        DateTimeFormatter localDateTime = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        List<LogisticsBillDetailEntity> detailEntityList = logisticsBillDetailService.listByMainIds(Arrays.asList(entity.getId()));
                LogisticsChannelEntity channelEntity = logisticsChannelService.getById(entity.getChannelId());

        String supplierName = "";
        if(Objects.nonNull(channelEntity)){
            LogisticsSupplierEntity supplierEntity = logisticsSupplierService.getById(channelEntity.getMainId());
            supplierName = supplierEntity.getSupplierName();
        }

        for (int i = 0; i < detailEntityList.size(); i++) {
            LogisticsBillDetailEntity logisticsBillDetailEntity = detailEntityList.get(i);

            ShudiyunB2cOrderDTO shudiyunB2cOrderDTO = new ShudiyunB2cOrderDTO();

            shudiyunB2cOrderDTO.setBiz_uni_key(entity.getId()+logisticsBillDetailEntity.getId());
            shudiyunB2cOrderDTO.setBiz_no(entity.getTransportNo());
            if (entity.getDeliveryTime() != null) {
                shudiyunB2cOrderDTO.setBiz_time(localDateTime.format(entity.getDeliveryTime()));
            }
            //默认运单
            shudiyunB2cOrderDTO.setTransaction_type("运单");
            shudiyunB2cOrderDTO.setTransaction_sub_type("普通运单");
            shudiyunB2cOrderDTO.setBiz_status(LogisticTrackStatusEnum.getName(logisticsBillDetailEntity.getTrackStatus()));
            if (entity.getVersion() == null) {
                entity.setVersion(0);
            }
            if (logisticsBillDetailEntity.getVersion() == null) {
                entity.setVersion(0);
            }
            shudiyunB2cOrderDTO.setStatus(shudiyunB2cOrderDTO.sdyStatusHandle(operateEnum, entity.getVersion(), logisticsBillDetailEntity.getVersion()));

            if (entity.getDeliveryTime() != null) {
                shudiyunB2cOrderDTO.setDelivery_time(localDateTime.format(entity.getDeliveryTime()));
            }
            if (logisticsBillDetailEntity.getSignTime() != null) {
                shudiyunB2cOrderDTO.setLogistics_delivery_time(localDateTime.format(logisticsBillDetailEntity.getSignTime()));
            } else {
                shudiyunB2cOrderDTO.setLogistics_delivery_time(localDateTime.format(LocalDateTime.now()));
            }
            shudiyunB2cOrderDTO.setDelivery_number(entity.getOutstockCode());

            if (CharSequenceUtil.isBlank(supplierName)) {
                shudiyunB2cOrderDTO.setLogistic_company("无");
            } else {
                shudiyunB2cOrderDTO.setLogistic_company(supplierName);
            }

            if (channelEntity != null && CharSequenceUtil.isNotBlank(channelEntity.getMainId())) {
                shudiyunB2cOrderDTO.setLogistic_company_code(channelEntity.getMainId());
            } else {
                shudiyunB2cOrderDTO.setLogistic_company_code("无");
            }

            shudiyunB2cOrderDTO.setWaybill_number(entity.getTransportNo());
            shudiyunB2cOrderDTO.setForeign_waybill_number(logisticsBillDetailEntity.getTrackNo());
            shudiyunB2cOrderDTO.setSource_system("SDC");
            shudiyunB2cOrderDTO.setRoot_node_no_initial(logisticsBillDetailEntity.getTrackNo());

            TmsPushMsgEntity tmsPushMsgEntity = new TmsPushMsgEntity();
            tmsPushMsgEntity.setTargetPlatform(DmpBasicSystemCodeEnum.SDY.getCode());
            tmsPushMsgEntity.setSourceType(SourceTypeEnum.SDY_LOGISTICS_BILL.getCode());
            tmsPushMsgEntity.setSourceId(logisticsBillDetailEntity.getId());
            tmsPushMsgEntity.setSourceCode(entity.getTransportNo());
            tmsPushMsgEntity.setSyncOperate(operateEnum);
            tmsPushMsgEntity.setPushData(JSON.toJSONString(shudiyunB2cOrderDTO));
            tmsPushMsgService.save(tmsPushMsgEntity);
        }
    }

    @Override
    public List<LogisticsBillEntity> queryToSdy(LocalDateTime startTime, LocalDateTime endTime, Integer pageSize, int offset) {
        return baseMapper.queryToSdy(startTime, endTime, pageSize, offset);
    }
}
