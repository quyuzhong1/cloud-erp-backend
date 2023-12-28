package com.erp.server.tms.service.impl;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.LogisticsPlatformEnum;
import com.common.business.enums.OrderTypeEnum;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.controller.vo.ApiResult;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.utils.date.DateUtil;
import com.erp.model.oms.dto.SoB2cDTO;
import com.erp.model.oms.dto.SoB2cErrorDTO;
import com.erp.model.oms.entity.SoB2cLogisticsEntity;
import com.erp.model.oms.enums.SoB2ErrorTypeEnum;
import com.erp.model.plm.dto.LogisticsProductDTO;
import com.erp.model.plm.enums.ProductSalesPlatformEnum;
import com.erp.model.tms.dto.*;
import com.erp.model.tms.entity.*;
import com.erp.model.tms.enums.DictBasicEnum;
import com.erp.model.tms.enums.LogisticTrackStatusEnum;
import com.erp.model.tms.enums.LogisticsAddressTypeEnum;
import com.erp.model.tms.enums.LogisticsPrintTypeEnum;
import com.erp.model.tms.vo.request.*;
import com.erp.model.tms.vo.response.CancelResponseVO;
import com.erp.model.tms.vo.response.InterceptResponseVO;
import com.erp.model.tms.vo.response.LogisticsOrderResponseVO;
import com.erp.model.tms.vo.response.LogisticsPrintLabelResponse;
import com.erp.rpc.oms.feign.OmsTaskFeign;
import com.erp.rpc.oms.feign.SoB2cFeign;
import com.erp.rpc.plm.feign.LogisticsProductFeign;
import com.erp.server.tms.constant.TmsConstant;
import com.erp.server.tms.convert.LogisticsBillConverter;
import com.erp.server.tms.handler.LogisticsRegistry;
import com.erp.server.tms.mapper.LogisticsBillMapper;
import com.erp.server.tms.service.*;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.exception.ServiceException;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
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

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;

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
    private CommonService commonService;
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

        logisticsBillDetailService.add(logisticsBillEntity, addDTO.getDetailList());

        //新增物流费用单
        addLogisticsBillCost(logisticsBillEntity, addDTO.getCurrency());
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
        String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", commonService.getUserInfo().getUserName(), logisticsBillEntity.getId(), "物流单");
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
            logisticsBillDetailService.removeByMainIds(ids);
            return this.removeByIds(ids);
        }
        return Boolean.FALSE;
    }


    /**
     * 新增修改处理数据
     */
    private void handleData(LogisticsBillEntity logisticsBillEntity) {

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
        String deliverType = LogisticsAddressTypeEnum.DELIVER.getCode();
        //发货人信息
        List<LogisticsAddressEntity> deliverList = logisticsAddressService.listByTypeAndChannelId(deliverType, channelId, dto.getShopId());
        if (CollectionUtils.isEmpty(deliverList)) {
            throw new ServiceException(ApiError.ERROR_CHANNEL_ADDRESS_NOT_EXIST, logisticsChannel.getName(), LogisticsAddressTypeEnum.DELIVER.getName());
        }
        //发货人信息
        SenderInfo senderInfo = new SenderInfo();
        BeanMapperUtils.copy(deliverList.get(0), senderInfo);
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
        List<LogisticsBillDTO.SkuDTO> skuList = dto.getSkuList();
        List<String> skuIdList = skuList.stream().map(LogisticsBillDTO.SkuDTO::getSkuId).collect(Collectors.toList());
        List<LogisticsProductDTO.ProductDTO> skuInfoList = logisticsProductFeign.listBySkuIdList(skuIdList);
        for (LogisticsProductDTO.ProductDTO item : skuInfoList) {
            Integer qty = skuList.stream().filter(s -> s.getSkuId().equals(item.getSkuId())).map(LogisticsBillDTO.SkuDTO::getQty).
                    findFirst().orElse(0);
            BigDecimal price = item.getDeclarePrice();
            item.setPrice(price);
            item.setQuantity(qty);
            item.setAmount(MathUtil.multiply(price, qty));
        }
        //包裹信息
        LogisticsBillDTO.PackageDTO packageDTO = dto.getPackageInfo();
        List<LogisticsProductVO> logisticsProductList = LogisticsBillConverter.INSTANCE.convertLogisticsProduct(skuInfoList);
        ParceInfoVO parceInfo = LogisticsBillConverter.INSTANCE.convertParceInfo(packageDTO);
        Boolean hasBattery = skuInfoList.stream().filter(s -> s.getIsElectric()).count() > 0;
        //是否带电
        parceInfo.setHasBattery(hasBattery);
        Integer totalQuantity = skuInfoList.stream().mapToInt(LogisticsProductDTO.ProductDTO::getQuantity).sum();
        parceInfo.setTotalQuantity(totalQuantity);

        //申报总价
        BigDecimal totalPrice = skuInfoList.stream().filter(s -> Objects.nonNull(s.getDeclarePrice())).map(LogisticsProductDTO.ProductDTO::getAmount).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
        parceInfo.setTotalPrice(totalPrice);
        //总重量
        Integer totalWeight = skuInfoList.stream().filter(s -> Objects.nonNull(s.getWeight())).mapToInt(LogisticsProductDTO.ProductDTO::getWeight).sum();
        parceInfo.setTotalWeight(totalWeight);

        //根据销售平台和渠道code 获取到原生的渠道
        LogisticsSaleChannelEntity saleChannel = logisticsSaleChannelService.getByPlatform(logisticsPlatform, logisticsChannel.getCode());
        if (Objects.isNull(saleChannel)) {
            throw new ServiceException(ApiError.ERROR_SALES_CHANNEL_NOT_EXIST, logisticsChannel.getName());
        }
        LogisticsOrderVO logisticsOrderVO = LogisticsOrderVO.builder().authMap(authMap).
                orderSource(sourceType).
                deliveryNo(dto.getOrderCode()).
                iossCode(dto.getIossTaxNo()).
                senderInfo(senderInfo).
                receiverInfoVO(receiverInfo).
                parceInfoVO(parceInfo).
                logisticsProductVOList(logisticsProductList).
                logisticsChannelEntity(logisticsChannel).
                logisticsSaleChannel(saleChannel).build();
        ApiResult<LogisticsOrderResponseVO> orderResult = service.createOrder(logisticsOrderVO);
        //表示成功
        if (orderResult.isSuccess()) {
            LogisticsBillDTO.GenerateBillResultDTO resultDTO  = handleBill(orderResult.getData(), dto);
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


    @Transactional(rollbackFor = Exception.class)
    public  LogisticsBillDTO.GenerateBillResultDTO handleBill(LogisticsOrderResponseVO responseVO, LogisticsBillDTO.GenerateBillDTO dto) {
        LogisticsBillDTO.GenerateBillResultDTO resultDTO = new LogisticsBillDTO.GenerateBillResultDTO();
        List<String> trackNoList = new ArrayList<>(2);
        LogisticsBillEntity billEntity = new LogisticsBillEntity();
        billEntity.setChannelId(dto.getChannelId());
        billEntity.setSalesPlatform(dto.getSalesPlatform());
        billEntity.setShopId(dto.getShopId());
        billEntity.setShopName(dto.getShopName());
        billEntity.setSourceType(dto.getSourceType());
        billEntity.setSourceId(dto.getOrderId());
        billEntity.setSourceCode(dto.getOrderCode());
        billEntity.setOrderTime(dto.getOrderTime());
        billEntity.setOrderType(dto.getOrderType());
        String transportNo=responseVO.getTransportNo();
        billEntity.setTransportNo(transportNo);
        //跟踪单号
        String trackNo = responseVO.getTrackNo();
        if(StringUtils.isNotBlank(trackNo)&&!"null".equals(trackNo)){
            trackNoList.add(trackNo);
        }
        List<LogisticsBillDetailDTO.AddDTO> detailList = new ArrayList<>(2);
        LogisticsBillDetailDTO.AddDTO addDTO = new LogisticsBillDetailDTO.AddDTO();
        addDTO.setTrackNo(trackNo);
        detailList.add(addDTO);
        Boolean more = responseVO.getMore();
        if (Objects.nonNull(more) && more) {
            List<LogisticsOrderResponseVO> responseList = responseVO.getLogisticsOrderResponseVOS();
            for (LogisticsOrderResponseVO item : responseList) {
                LogisticsBillDetailDTO.AddDTO detailDTO = new LogisticsBillDetailDTO.AddDTO();
                detailDTO.setTrackNo(item.getTrackNo());
                detailList.add(detailDTO);
                trackNoList.add(item.getTrackNo());
            }
        }
        this.save(billEntity);
        logisticsBillDetailService.add(billEntity, detailList);
        //新增物流费用单
        addLogisticsBillCost(billEntity, dto.getCurrency());
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

            String orderType = item.getOrderType();
            String orderTypeName = OrderTypeEnum.getName(orderType);
            item.setOrderTypeName(orderTypeName);


        }
    }

    /**
     * @param logisticsBillEntity
     * @description: 添加物流费用
     * @author Will
     * @date: 2023/11/20 12:27
     */
    private void addLogisticsBillCost(LogisticsBillEntity logisticsBillEntity, String currency) {
        try {
            LogisticsBillCostDTO.AddDTO addDTO = new LogisticsBillCostDTO.AddDTO();
            //渠道关联模板
            ShippingTemplateEntity shippingTemplateEntity = shippingTemplateService.getByChannelId(logisticsBillEntity.getChannelId());
            //来源b2c销售订单
            if (SourceTypeEnum.SO_B2C.getCode().equals(logisticsBillEntity.getSourceType())) {
                //物流信息
                List<SoB2cLogisticsEntity> soB2cLogisticsList = soB2cFeign.listSoB2cLogisticsByMainIdList(Arrays.asList(logisticsBillEntity.getSourceId()));
                if (CollectionUtils.isEmpty(soB2cLogisticsList)) {
                    throw new ServiceException(ApiError.ERROR_SO_B2C_LOGISTICS_NOT_EXIST);
                }
                addDTO.setActualWeight(soB2cLogisticsList.get(0).getWeight());
                //存在模板时计算体积重
                if (ObjectUtil.isNotEmpty(shippingTemplateEntity)) {
                    BigDecimal volume = soB2cLogisticsList.get(0).getHeight()
                            .multiply(soB2cLogisticsList.get(0).getWeight())
                            .multiply(soB2cLogisticsList.get(0).getLength());
                    addDTO.setVolumeWeight(MathUtil.divide(volume, new BigDecimal(shippingTemplateEntity.getVolumeSetting())));
                }
            }
            if (ObjectUtil.isNotEmpty(shippingTemplateEntity)) {
                //计费重
                BigDecimal billingWeight = MathUtil.compareTo(addDTO.getActualWeight(), addDTO.getVolumeWeight()) > MathUtil.ZERO
                        ? addDTO.getActualWeight() : addDTO.getVolumeWeight();
                //预估运费
                ShippingTemplateRuleDTO.ViewParamDTO viewParamDTO = new ShippingTemplateRuleDTO.ViewParamDTO();
                viewParamDTO.setWeight(addDTO.getActualWeight());
                viewParamDTO.setMainId(shippingTemplateEntity.getId());
                ShippingTemplateRuleEntity shippingTemplateRule = shippingTemplateRuleService.getShippingTemplateRule(viewParamDTO);
                if (ObjectUtil.isNotEmpty(shippingTemplateRule)) {
                    BigDecimal shippingCost = shippingCalculationService.calculationShippingCost(shippingTemplateEntity, shippingTemplateRule, billingWeight);
                    addDTO.setEstimatedShippingCost(shippingCost);
                }
            }
            addDTO.setCurrency(currency);
            addDTO.setChannelId(logisticsBillEntity.getChannelId());
            addDTO.setLogisticsBillId(logisticsBillEntity.getId());
            logisticsBillCostService.add(addDTO);
        }catch (Exception e){
           log.error("生成物流费用出错>>>>>{}",e.getMessage());
        }

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

        for (LogisticsBillDTO.PrintLogisticsWaybillDTO dto : list) {
            String channelId = dto.getChannelId();
            LogisticsSupplierDTO.AuthDTO auth = logisticsAuthService.getAuthByChannelId(channelId);
            if (Objects.isNull(auth)) {
                throw new ServiceException(ApiError.ERROR_LOGISTICS_CHANNEL_NOT_EXIST);
            }
            Map<String, String> authMap = logisticsAuthService.getLogisticsAuthConfig(auth.getAuthId(), auth.getLogisticsPlatform());
            //平台
            String logisticsPlatform = auth.getLogisticsPlatform();
            LogisticsService service = logisticsRegistry.getHandler(logisticsPlatform);


            //请求面单参数
            List<LogisticsGetLabelVO> labelVOArrayList = new ArrayList<>();
            LogisticsGetLabelVO getLabelVO = new LogisticsGetLabelVO();
            getLabelVO.setDeliveryNo(dto.getDeliveryNo());

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
            getLabelVO.setIsPdn(platformEnum.getPrintDelivery());
//            getLabelVO.setIsPcd(platformEnum.getPrintLabel());


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

            waybillDTO.setLogisticsBase64(labelList.getData().get(0).getBase64());
            waybillDTO.setDistributeBase64(labelList.getData().get(0).getBase64());
            waybillDTO.setSoB2cId(dto.getB2cSoId());
            waybillDTO.setTrackNo(getLabelVO.getTrackNo());
            waybillDTO.setTransportNo(getLabelVO.getTransportNo());
            waybillDTOList.add(waybillDTO);
        }

        soB2cFeign.updateLogisticsWaybill(waybillDTOList);
        return waybillDTOList;
    }
}
