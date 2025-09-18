package com.erp.server.wms.service.impl;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.common.business.annotation.DataIdempotent;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.enums.UnitEnum;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.oms.dto.PackageDTO;
import com.erp.model.oms.dto.SoB2cDTO;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.oms.entity.SoB2cLogisticsEntity;
import com.erp.model.oms.enums.PackageStatusEnum;
import com.erp.model.oms.enums.SoB2cBillStatusEnum;
import com.erp.model.oms.enums.TransferStatusEnum;
import com.erp.model.tms.dto.LogisticsBillDTO;
import com.erp.model.tms.dto.LogisticsChannelDTO;
import com.erp.model.tms.entity.TransferLogisticsChannelEntity;
import com.erp.model.tms.entity.TransferLogisticsSupplierEntity;
import com.erp.model.wms.dto.PackageForecastDTO;
import com.erp.model.wms.dto.PackageForecastDetailDTO;
import com.erp.model.wms.dto.SoB2cDeliveryDTO;
import com.erp.model.wms.entity.SoB2cDeliveryEntity;
import com.erp.model.wms.entity.SoB2cDeliveryInterceptEntity;
import com.erp.model.wms.enums.SoB2cDeliveryInterceptStatusEnum;
import com.erp.model.wms.enums.SoB2cDeliveryStatusEnum;
import com.erp.rpc.oms.feign.SoB2cFeign;
import com.erp.rpc.tms.feign.LogisticsBillFeign;
import com.erp.rpc.tms.feign.LogisticsFeign;
import com.erp.rpc.tms.feign.TransferLogisticsFeign;
import com.erp.server.wms.service.*;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Lambda
 * @Classname PackageServiceImpl
 * @Description
 * @Date 2024-01-30 11:13
 * @Created by yl
 */
@Service
@Slf4j
public class PackageServiceImpl implements PackageService {

    @Resource
    private LogisticsFeign logisticsFeign;

    @Resource
    private SoB2cFeign soB2cFeign;

    @Resource
    private SoB2cDeliveryService soB2cDeliveryService;

    @Resource
    private TransferLogisticsFeign transferLogisticsFeign;

    @Resource
    private PackageForecastService packageForecastService;

    @Resource
    private MQProducerService mqProducerService;

    @Resource
    private LogisticsBillFeign logisticsBillFeign;
    @Resource
    private CfgSettingService cfgSettingService;
    @Lazy
    @Resource
    private AsyncService asyncService;
    @Resource
    private SoB2cDeliveryInterceptService soB2cDeliveryInterceptService;

    @Override
    public PackageDTO.ScanResultDTO packageScan(PackageDTO.ScanDTO scanDTO) {
        PackageDTO.ScanResultDTO scanResult = soB2cFeign.packageScanByCode(scanDTO.getCode());
        if (Objects.isNull(scanResult)) {
            throw new ServiceException("未找到对应单号");
        }

        String packageStatus = scanResult.getPackageStatus();
        String already = PackageStatusEnum.ALREADY.getCode();
        if (already.equals(packageStatus)) {
            throw new ServiceException("订单单号已组包完成，无法重复组包");
        }



        SoB2cEntity entity = soB2cFeign.getById(scanResult.getSoId());
        //查询平台订单是否取消
        if (entity.getIsCancel()) {
            //订单拦截
            soB2cFeign.deliveryIntercept(new SoB2cDTO.RemarkDTO(entity.getId(), "平台取消或退款"));
            throw new ServiceException("平台订单已取消，无法组包");
        }
        //请求接口过慢，暂时取消 TODO
        /*else {
            if (soB2cFeign.checkPlatformShipOrder(entity.getId())) {
                //如果订单原始状态非取消，这里需要再次调用平台接口查询，是否已取消
                PlatformDeliveryInterceptDTO deliveryInterceptDTO = new PlatformDeliveryInterceptDTO();
                deliveryInterceptDTO.setSoB2cId(entity.getId());
                deliveryInterceptDTO.setDictPlatform(entity.getDictPlatform());
                deliveryInterceptDTO.setOldIsCancel(entity.getIsCancel());
                deliveryInterceptDTO.setPlatformCode(entity.getPlatformCode());
                deliveryInterceptDTO.setShopId(entity.getShopId());
                Boolean flag = PlatformSaveHandler.deliveryIntercept(deliveryInterceptDTO);
                if (flag) {
                    throw new ServiceException("平台订单已取消，无法组包");
                }
            }
        }*/

        //扫描判断：扫描判断是否平台取消以及拦截单【异常提示：订单单号被拦截/取消，不可组包操作】
        if (TransferStatusEnum.WAIT.getCode().equals(scanResult.getTransferStatus())
                || TransferStatusEnum.FAILURE.getCode().equals(scanResult.getTransferStatus())) {
            //校验订单状态中转状态为待中转/上传失败，扫描识别后非成功状态若勾选则取消勾选并禁用，若未勾选则直接禁用
            throw new ServiceException(ApiError.TRANSFER_FAILURE_NOT_PACKAGE);
        }
        if (CharSequenceUtil.isBlank(scanResult.getTransferLogisticsSupplierId()) && !TransferStatusEnum.NOT.getCode().equals(entity.getTransferStatus())) {
            throw new ServiceException(ApiError.TRANSFER_LOGISTICS_SUPPLIER_IS_NULL_NOT_PACKAGE);
        }

        if (entity.getInvalidStatus()) {
            throw new ServiceException(ApiError.INVALID_NOT_PACKAGE);
        }

        if (entity.getIsIntercept()) {
            throw new ServiceException(ApiError.LOGISTICS_INTERCEPT_NOT_PACKAGE);
        }

        String billStatus = scanResult.getBillStatus();
        //待发货
        String waitShipped = SoB2cBillStatusEnum.ENUM_WAIT_SHIPPED.getCode();
        if (!waitShipped.equals(billStatus) && !SoB2cBillStatusEnum.ENUM_SHIPPED.getCode().equals(billStatus)) {
            throw new ServiceException("仅待发货和已发货的可操作组包");
        }

        if(Objects.nonNull(scanDTO.getWeight())){
            if(scanDTO.getWeight().compareTo(BigDecimal.ZERO) <= 0){
                throw new ServiceException("重量必须大于0");
            }
            if(CharSequenceUtil.isBlank(scanDTO.getWeightUnit())){
                throw new ServiceException("重量单位不能为空");
            }
            if(!UnitEnum.WeightUnitEnum.G.getCode().equals(scanDTO.getWeightUnit()) && !UnitEnum.WeightUnitEnum.KG.getCode().equals(scanDTO.getWeightUnit())){
                throw new ServiceException("重量单位仅支持g和kg");
            }

            //转成g
            BigDecimal weightByG = scanDTO.getWeight();
            if(UnitEnum.WeightUnitEnum.KG.getCode().equals(scanDTO.getWeightUnit())){
                weightByG = scanDTO.getWeight().multiply(BigDecimal.valueOf(1000));
            }
            if(weightByG.compareTo(new BigDecimal("1000000")) >= 0){
                throw new ServiceException("超过1000KG，重量异常请核对");
            }
            // 更新订单重量
            if(CharSequenceUtil.isNotBlank(scanResult.getLogisticsId())){
                soB2cFeign.updateWeight(scanResult.getSoId(),scanResult.getLogisticsId(),weightByG);
            }
            //查询订单物流信息获取跟踪号
            SoB2cLogisticsEntity soB2cLogisticsEntity = new SoB2cLogisticsEntity();
            soB2cLogisticsEntity.setCode(scanResult.getTransportNo());
            soB2cLogisticsEntity.setLogisticsChannelId(scanResult.getLogisticsChannelId());
            soB2cLogisticsEntity.setTrackNo(scanResult.getTrackNo());
            soB2cLogisticsEntity.setWeight(weightByG);
            // 更新发货单重量
            SoB2cDeliveryDTO.UpdateWeightDTO dto = SoB2cDeliveryDTO.UpdateWeightDTO.builder()
                    .soId(scanResult.getSoId())
                    .weight(scanDTO.getWeight())
                    .weightUnit(scanDTO.getWeightUnit())
                    .build();
            soB2cDeliveryService.updateB2cDeliveryWeightBySoId(dto);
            scanResult.setWeight(weightByG);

            //更新物流商重量
            if(CharSequenceUtil.isNotBlank(soB2cLogisticsEntity.getCode()) && CharSequenceUtil.isNotBlank(soB2cLogisticsEntity.getLogisticsChannelId())){
                LogisticsBillDTO.UpdateWeight updateWeight = LogisticsBillDTO.UpdateWeight.builder()
                        .soB2cEntity(entity)
                        .soB2cLogisticsEntity(soB2cLogisticsEntity)
                        .build();
                asyncService.updateLogisticWeight(updateWeight);
            }
        }

        //查询发货单
        List<SoB2cDeliveryEntity> soB2cDeliveryEntityList = soB2cDeliveryService.listBySourceIds(Collections.singletonList(scanResult.getSoId()));
        SoB2cDeliveryEntity deliveryEntity = soB2cDeliveryEntityList.stream().filter(v->!v.getStatus().equals(SoB2cDeliveryStatusEnum.CANCEL_DELIVERY.getCode())).findFirst().orElse(null);
        if(Objects.nonNull(deliveryEntity)){
            scanResult.setWeightStatus(deliveryEntity.getIsWeigh()?"已称重":"未称重");
        }

        scanResult.setWeightUnit(UnitEnum.WeightUnitEnum.G.getCode());
        //物流渠道id
        String logisticsChannelId = scanResult.getLogisticsChannelId();
        if (CharSequenceUtil.isNotBlank(logisticsChannelId)) {
            LogisticsChannelDTO.BaseDTO baseDTO = logisticsFeign.getChannelInfoById(logisticsChannelId);
            if (Objects.nonNull(baseDTO)) {
                scanResult.setLogisticsChannelName(baseDTO.getName());
                scanResult.setLogisticsSupplierId(baseDTO.getMainId());
                scanResult.setLogisticsSupplierName(baseDTO.getLogisticsSupplierName());
                scanResult.setLogisticsSupplierShortName(baseDTO.getLogisticsSupplierShortName());
            }

            if (TransferStatusEnum.NOT.getCode().equals(entity.getTransferStatus())) {
                //判断是否是组包限制的发货物流商
                Boolean isPackageSupplier = cfgSettingService.getPackageSupplierSetting(scanResult.getLogisticsSupplierId());
                scanResult.setIsPackageSupplier(isPackageSupplier);
                scanResult.setUniqueId(getPackageUniqueId(isPackageSupplier,scanResult.getLogisticsSupplierId(),scanResult.getTransferLogisticsChannelId(),scanResult.getTransferLogisticsSupplierId(), scanResult.getShopId()));
                return scanResult;
            }

            //查询对应的中转服务商
            TransferLogisticsSupplierEntity supplierEntity = transferLogisticsFeign.getLogisticsSupplierById(scanResult.getTransferLogisticsSupplierId());
            if (ObjectUtil.isNotEmpty(supplierEntity)) {
                scanResult.setTransferLogisticsSupplierName(supplierEntity.getSupplierName());
            }

            //查询中转服务商对应的渠道
            List<TransferLogisticsChannelEntity> logisticsChannelEntityList = transferLogisticsFeign.listLogisticsChannelByMainId(Collections.singletonList(scanResult.getTransferLogisticsSupplierId()));
            if (CollectionUtils.isNotEmpty(logisticsChannelEntityList)) {
                TransferLogisticsChannelEntity transferLogisticsChannelEntity = logisticsChannelEntityList.stream().filter(req -> scanResult.getTransferLogisticsChannelId().equals(req.getId())).findFirst().orElse(null);
                if (ObjectUtil.isNotEmpty(transferLogisticsChannelEntity)) {
                    scanResult.setTransferLogisticsChannelName(transferLogisticsChannelEntity.getName());
                }
            }
        }
        //判断是否是组包限制的发货物流商
        Boolean isPackageSupplier = cfgSettingService.getPackageSupplierSetting(scanResult.getLogisticsSupplierId());
        scanResult.setIsPackageSupplier(isPackageSupplier);
        scanResult.setUniqueId(getPackageUniqueId(isPackageSupplier,scanResult.getLogisticsSupplierId(),scanResult.getTransferLogisticsChannelId(),scanResult.getTransferLogisticsSupplierId(), scanResult.getShopId()));
        return scanResult;
    }

    private String getPackageUniqueId(Boolean isPackageSupplier, String logisticsSupplierId, String transferLogisticsChannelId, String transferLogisticsSupplierId, String shopId) {
        if (isPackageSupplier){
            return logisticsSupplierId+"-"+transferLogisticsChannelId+"-"+transferLogisticsSupplierId + "-" + shopId;
        }else {
            return logisticsSupplierId+"-"+transferLogisticsChannelId+"-"+transferLogisticsSupplierId;
        }
    }


    /**
     * 组包合并
     *
     * @param dto
     * @return
     */
    @Override
    @DataIdempotent(keyIdName = "dto.ids")
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    public List<BatchResultDTO> mergePackage(PackageDTO.MergePackageDTO dto) {
        List<PackageForecastDTO.AddDTO> addList = assembleDbBySoIds(dto);
        List<BatchResultDTO> resultDTOList = new ArrayList<>();
        //自动发货
        if (dto.getIsAutoOut()) {
            List<String> soIdList = dto.getIds();
            //待处理和异常单不允许自动出库
            List<SoB2cDeliveryEntity> entities = soB2cDeliveryService.listBySourceIds(soIdList);
            String notShipmentSoCodes = entities.stream()
                    .filter(e -> SoB2cDeliveryStatusEnum.notShipment().contains(e.getStatus()))
                    .map(SoB2cDeliveryEntity::getSoCode)
                    .collect(Collectors.joining(","));
            if (StringUtils.isNotEmpty(notShipmentSoCodes)) {
                throw new ServiceException(ApiError.ERROR_99115, notShipmentSoCodes);
            }
        }
        for (PackageForecastDTO.AddDTO item : addList) {
            List<PackageForecastDetailDTO.AddDTO> detailList = item.getDetailList();
            //有拦截单的订单返回错误
            Map<String, String> hasDeliveryInterceptMap = detailList.stream().filter(PackageForecastDetailDTO.AddDTO::isHasDeliveryIntercept).collect(Collectors.toMap(PackageForecastDetailDTO.CommonDTO::getSoCode, v -> CharSequenceUtil.format("{}/{}/{}", v.getSoCode(), v.getTransportNo(), v.getTrackNo()), (v1, v2) -> v1));
            if (MapUtil.isEmpty(hasDeliveryInterceptMap)) {
                try {
                    packageForecastService.add(item);
                } catch (Exception e) {
                    log.error("添加组包预报异常 {}", e.getMessage());
                    item.getDetailList().forEach(v -> resultDTOList.add(BatchResultDTO.fail(item.getLogisticsSupplierId(), v.getSoCode(), CharSequenceUtil.format("添加组包预报异常 {}", ExceptionUtil.getSimpleMessage(e)))));
                }
                //自动发货
                if (dto.getIsAutoOut()) {
                    List<String> soIdList = item.getDetailList().stream().filter(v -> !SoB2cBillStatusEnum.ENUM_SHIPPED.getCode().equals(v.getBillStatus())).map(PackageForecastDetailDTO.AddDTO::getSoId).collect(Collectors.toList());
                    // 异步推送到MQ
                    soIdList.forEach(soId -> {
                        SendResult sendResult = mqProducerService.syncClassMsg(RocketMqTopic.ASYNC_MERGE_PACKAGE_DELIVERY_TOPIC, RocketMqTagEnum.ASYNC_MERGE_PACKAGE_DELIVERY_TAG.getName(),
                                soId, soId);
                        if (!SendStatus.SEND_OK.equals(sendResult.getSendStatus())) {
                            throw new RuntimeException(CharSequenceUtil.format("发送MQ数据异常，{}", JSONUtil.toJsonStr(sendResult)));
                        }
                    });
                }
            } else {
                hasDeliveryInterceptMap.forEach((key, val) -> {
                    resultDTOList.add(BatchResultDTO.fail(item.getLogisticsSupplierId(), key, val + " 存在拦截单，无法组包，可操作移除后再进行组包"));
                });
            }
        }

        return resultDTOList;
    }

    @Override
    public PackageDTO.WeightDTO getOrderWeight(PackageDTO.WeightParamDTO dto) {
        PackageDTO.WeightDTO weightDTO = new PackageDTO.WeightDTO();
        //默认重量
        weightDTO.setWeight(BigDecimal.ZERO);
        weightDTO.setWeightUnit(UnitEnum.WeightUnitEnum.KG.getCode());

        PackageDTO.ScanResultDTO scanResult = soB2cFeign.packageScanByCode(dto.getCode());
        if (ObjectUtil.isEmpty(scanResult)) {
            return weightDTO;
        }
        List<SoB2cDeliveryEntity> soB2cDeliveryList = soB2cDeliveryService.listBySourceIds(Collections.singletonList(scanResult.getSoId()),SoB2cDeliveryStatusEnum.CANCEL_DELIVERY.getCode());
        if (CollectionUtils.isEmpty(soB2cDeliveryList)) {
            return  weightDTO;
        }
        weightDTO.setWeight(soB2cDeliveryList.get(0).getWeight());
        weightDTO.setWeightUnit(soB2cDeliveryList.get(0).getWeightUnit());
        return weightDTO;
    }

    /**
     * 拼装数据
     *
     * @param dto
     * @return
     */
    private List<PackageForecastDTO.AddDTO> assembleDbBySoIds(PackageDTO.MergePackageDTO dto) {
        List<String> ids = dto.getIds();

        if (CollectionUtils.isEmpty(ids)) {
            return Collections.emptyList();
        }
        List<PackageDTO.ScanResultDTO> list = soB2cFeign.listMergePackageBySoIds(ids);
        List<String> logisticsChannelIdList = list.stream().filter(a -> CharSequenceUtil.isNotBlank(a.getLogisticsChannelId())).
                map(PackageDTO.ScanResultDTO::getLogisticsChannelId).distinct().collect(Collectors.toList());
        List<SoB2cDeliveryInterceptEntity> soB2cDeliveryInterceptEntityList = soB2cDeliveryInterceptService.listBySourceIds(ids).stream().filter(v->!v.getHandleStatus().equals(SoB2cDeliveryInterceptStatusEnum.CANCEL.getStatus())).collect(Collectors.toList());


        List<LogisticsChannelDTO.BaseDTO> channelList = CollectionUtils.isNotEmpty(logisticsChannelIdList) ? logisticsFeign.listChannelInfoById(logisticsChannelIdList) : Collections.emptyList();
        for (PackageDTO.ScanResultDTO item : list) {
            String logisticsChannelId = item.getLogisticsChannelId();
            LogisticsChannelDTO.BaseDTO logisticsChannel = channelList.stream().
                    filter(l -> l.getId().equals(logisticsChannelId)).findFirst().orElse(null);
            if(logisticsChannel!=null){
                item.setLogisticsChannelName(logisticsChannel.getName());
                item.setLogisticsSupplierId(logisticsChannel.getLogisticsSupplierId());
                item.setIsPackageSupplier(cfgSettingService.getPackageSupplierSetting(logisticsChannel.getLogisticsSupplierId()));
                item.setLogisticsSupplierName(logisticsChannel.getLogisticsSupplierName());
                item.setLogisticsSupplierShortName(logisticsChannel.getLogisticsSupplierShortName());
            }
            item.setUniqueId(getPackageUniqueId(item.getIsPackageSupplier(),item.getLogisticsSupplierId(),item.getTransferLogisticsChannelId(),item.getTransferLogisticsSupplierId(), item.getShopId()));
        }

        Map<String, List<PackageDTO.ScanResultDTO>> map = list.stream().filter(s -> CharSequenceUtil.isNotBlank(s.getLogisticsSupplierId())).
                collect(Collectors.groupingBy(PackageDTO.ScanResultDTO::getUniqueId));

        LocalDate nowDate = LocalDate.now();
        String weightUnit= UnitEnum.WeightUnitEnum.G.getCode();
        List<PackageForecastDTO.AddDTO> result = new ArrayList<>(map.size());
        for (Map.Entry<String, List<PackageDTO.ScanResultDTO>> entry : map.entrySet()) {
            List<PackageDTO.ScanResultDTO> detailList = entry.getValue();

            BigDecimal totalPackageWeight=detailList.stream().
                    map(PackageDTO.ScanResultDTO::getWeight).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
            PackageForecastDTO.AddDTO addDTO = new PackageForecastDTO.AddDTO();
            addDTO.setLogisticsSupplierId(detailList.get(0).getLogisticsSupplierId());
            String logisticsSupplierName = detailList.get(0).getLogisticsSupplierName();
            addDTO.setLogisticsSupplierName(logisticsSupplierName);
            addDTO.setTotalPackageQty(detailList.size());
            addDTO.setBillDate(nowDate);
            addDTO.setWeightUnit(weightUnit);
            addDTO.setTotalPackageWeight(totalPackageWeight);
            List<PackageForecastDetailDTO.AddDTO> addDetailList= BeanMapperUtils.copyList(PackageForecastDetailDTO.AddDTO.class,detailList);
            addDetailList.forEach(addDetail->{
                addDetail.setWeightUnit(weightUnit);
                SoB2cDeliveryInterceptEntity interceptEntity = soB2cDeliveryInterceptEntityList.stream().filter(v->v.getSoId().equals(addDetail.getSoId())).findFirst().orElse(null);
                addDetail.setHasDeliveryIntercept(Objects.nonNull(interceptEntity) && addDetail.getIsIntercept());
            });
            addDTO.setDetailList(addDetailList);
            result.add(addDTO);
        }
        return result;
    }

}
