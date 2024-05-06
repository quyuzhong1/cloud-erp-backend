package com.erp.server.wms.service.impl;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.common.business.dto.PlatformDeliveryInterceptDTO;
import com.common.business.dto.PlatformShipOrderDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.enums.UnitEnum;
import com.common.business.handler.PlatformSaveHandler;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.oms.dto.PackageDTO;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.oms.enums.PackageStatusEnum;
import com.erp.model.oms.enums.SoB2cBillStatusEnum;
import com.erp.model.oms.enums.TransferStatusEnum;
import com.erp.model.tms.dto.LogisticsChannelDTO;
import com.erp.model.tms.entity.TransferLogisticsChannelEntity;
import com.erp.model.tms.entity.TransferLogisticsSupplierEntity;
import com.erp.model.tms.enums.TransferLogisticsStatusEnum;
import com.erp.model.wms.dto.PackageForecastDTO;
import com.erp.model.wms.dto.PackageForecastDetailDTO;
import com.erp.model.wms.dto.SoB2cDeliveryDTO;
import com.erp.model.wms.entity.SoB2cDeliveryEntity;
import com.erp.model.wms.enums.SoB2cDeliveryStatusEnum;
import com.erp.rpc.oms.feign.SoB2cFeign;
import com.erp.rpc.tms.feign.LogisticsFeign;
import com.erp.rpc.tms.feign.TransferLogisticsFeign;
import com.erp.rpc.wms.feign.PackageForecastFeign;
import com.erp.rpc.wms.feign.SoB2cDeliveryFeign;
import com.erp.rpc.wms.feign.SoOutstockFeign;
import com.erp.server.wms.service.PackageForecastService;
import com.erp.server.wms.service.PackageService;
import com.erp.server.wms.service.SoB2cDeliveryService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.springframework.stereotype.Service;

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
    private PackageForecastFeign packageForecastFeign;

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
            throw new ServiceException("平台订单已取消，无法组包");
        } else {
            //如果订单原始状态非取消，这里需要再次调用平台接口查询，是否已取消
            PlatformDeliveryInterceptDTO deliveryInterceptDTO = new PlatformDeliveryInterceptDTO();
            deliveryInterceptDTO.setSoB2cId(entity.getSourceId());
            deliveryInterceptDTO.setDictPlatform(entity.getDictPlatform());
            deliveryInterceptDTO.setOldIsCancel(entity.getIsCancel());
            deliveryInterceptDTO.setPlatformCode(entity.getPlatformCode());
            deliveryInterceptDTO.setShopId(entity.getShopId());
            Boolean flag = PlatformSaveHandler.deliveryIntercept(deliveryInterceptDTO);
            if (flag) {
                throw new ServiceException("平台订单已取消，无法组包");
            }
        }

        //扫描判断：扫描判断是否平台取消以及拦截单【异常提示：订单单号被拦截/取消，不可组包操作】
        if (TransferStatusEnum.WAIT.getCode().equals(scanResult.getForcastStatus())
                || TransferStatusEnum.FAILURE.getCode().equals(scanResult.getForcastStatus())) {
            //校验订单状态中转状态为待中转/上传失败，扫描识别后非成功状态若勾选则取消勾选并禁用，若未勾选则直接禁用
            throw new ServiceException(ApiError.TRANSFER_FAILURE_NOT_PACKAGE);
        }
        if (StringUtils.isBlank(scanResult.getTransferLogisticsSupplierId()) && !TransferStatusEnum.NOT.getCode().equals(entity.getTransferStatus())) {
            throw new ServiceException(ApiError.TRANSFER_LOGISTICS_SUPPLIER_IS_NULL_NOT_PACKAGE);
        }

        if (entity.getInvalidStatus()) {
            throw new ServiceException(ApiError.INVALID_NOT_PACKAGE);
        }

        if (!TransferStatusEnum.NOT.getCode().equals(entity.getTransferStatus())) {
            TransferLogisticsStatusEnum platformTransferStatus = transferLogisticsFeign.getPlatformTransferStatus(entity.getShippingOrderNo(), scanResult.getTransferLogisticsSupplierId());
            if (ObjectUtil.isEmpty(platformTransferStatus)
                    || TransferLogisticsStatusEnum.DELETED.getCode().equals(platformTransferStatus.getCode())
                    || TransferLogisticsStatusEnum.UNUSUAL.getCode().equals(platformTransferStatus.getCode())
            ) {
                throw new ServiceException(ApiError.ORDER_CANCEL_NOT_PACKAGE);
            }
        }

        if (entity.getIsIntercept()) {
            throw new ServiceException(ApiError.LOGISTICS_INTERCEPT_NOT_PACKAGE);
        }


        String billStatus = scanResult.getBillStatus();
        //待发货
        String waitShipped = SoB2cBillStatusEnum.ENUM_WAIT_SHIPPED.getCode();
        if (!waitShipped.equals(billStatus)) {
            throw new ServiceException("仅待发货的可操作组包");
        }

        if(Objects.nonNull(scanDTO.getWeight())){
            if(scanDTO.getWeight().compareTo(BigDecimal.ZERO) <= 0){
                throw new ServiceException("重量必须大于0");
            }
            if(com.baomidou.mybatisplus.core.toolkit.StringUtils.isBlank(scanDTO.getWeightUnit())){
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
            // 更新订单重量
            if(StringUtils.isNotBlank(scanResult.getLogisticsId())){
                soB2cFeign.updateWeight(scanResult.getSoId(),scanResult.getLogisticsId(),weightByG);
            }
            // 更新发货单重量
            SoB2cDeliveryDTO.UpdateWeightDTO dto = SoB2cDeliveryDTO.UpdateWeightDTO.builder()
                    .soId(scanResult.getSoId())
                    .weight(scanDTO.getWeight())
                    .weightUnit(scanDTO.getWeightUnit())
                    .build();
            soB2cDeliveryService.updateB2cDeliveryWeightBySoId(dto);
            scanResult.setWeight(weightByG);
        }

        //查询发货单
        List<SoB2cDeliveryEntity> soB2cDeliveryEntityList = soB2cDeliveryService.listBySourceIds(Arrays.asList(scanResult.getSoId()));
        SoB2cDeliveryEntity deliveryEntity = soB2cDeliveryEntityList.stream().filter(v->!v.getStatus().equals(SoB2cDeliveryStatusEnum.CANCEL_DELIVERY.getCode())).findFirst().orElse(null);
        if(Objects.nonNull(deliveryEntity)){
            scanResult.setWeightStatus(deliveryEntity.getIsWeigh()?"已称重":"未称重");
        }

        scanResult.setWeightUnit(UnitEnum.WeightUnitEnum.G.getCode());
        //物流渠道id
        String logisticsChannelId = scanResult.getLogisticsChannelId();
        if (com.baomidou.mybatisplus.core.toolkit.StringUtils.isNotBlank(logisticsChannelId)) {
            LogisticsChannelDTO.BaseDTO baseDTO = logisticsFeign.getChannelInfoById(logisticsChannelId);
            if (Objects.nonNull(baseDTO)) {
                scanResult.setLogisticsChannelName(baseDTO.getName());
                scanResult.setLogisticsSupplierId(baseDTO.getMainId());
                scanResult.setLogisticsSupplierName(baseDTO.getLogisticsSupplierName());
            }

            if (TransferStatusEnum.NOT.getCode().equals(entity.getTransferStatus())) {
                return scanResult;
            }

            //查询对应的中转服务商
            TransferLogisticsSupplierEntity supplierEntity = transferLogisticsFeign.getLogisticsSupplierById(scanResult.getTransferLogisticsSupplierId());
            if (ObjectUtil.isNotEmpty(supplierEntity)) {
                scanResult.setTransferLogisticsSupplierName(supplierEntity.getSupplierName());
            }

            //查询中转服务商对应的渠道
            List<TransferLogisticsChannelEntity> logisticsChannelEntityList = transferLogisticsFeign.listLogisticsChannelByMainId(Arrays.asList(scanResult.getTransferLogisticsSupplierId()));
            if (CollectionUtils.isNotEmpty(logisticsChannelEntityList)) {
                TransferLogisticsChannelEntity transferLogisticsChannelEntity = logisticsChannelEntityList.stream().filter(req -> scanResult.getTransferLogisticsChannelId().equals(req.getId())).findFirst().orElse(null);
                if (ObjectUtil.isNotEmpty(transferLogisticsChannelEntity)) {
                    scanResult.setTransferLogisticsChannelName(transferLogisticsChannelEntity.getName());
                }
            }
        }

        return scanResult;
    }

    /**
     * 组包合并
     *
     * @param dto
     * @return
     */
    @Override
    public List<BatchResultDTO> mergePackage(PackageDTO.MergePackageDTO dto) {
        List<PackageForecastDTO.AddDTO> addList = assembleDbBySoIds(dto);
        List<BatchResultDTO> resultDTOList = new ArrayList<>();
        for (PackageForecastDTO.AddDTO item : addList) {
            try {
                packageForecastService.add(item);
            } catch (Exception e) {
                log.error("添加组包预报异常 {}", e.getMessage());
                item.getDetailList().forEach(v-> resultDTOList.add(BatchResultDTO.fail(item.getLogisticsSupplierId(),v.getSoCode(), StrUtil.format("添加组包预报异常 {}", ExceptionUtil.getSimpleMessage(e)))));
            }
        }

        return resultDTOList;
    }

    /**
     * 拼装数据
     *
     * @param dto
     * @return
     */
    private List<PackageForecastDTO.AddDTO> assembleDbBySoIds(PackageDTO.MergePackageDTO dto) {
        List<String> ids = dto.getIds();
        Boolean isAutoOut = dto.getIsAutoOut();

        if (CollectionUtils.isEmpty(ids)) {
            return Collections.emptyList();
        }
        List<PackageDTO.ScanResultDTO> list = soB2cFeign.listMergePackageBySoIds(ids);
        List<String> logisticsChannelIdList = list.stream().filter(a -> StringUtils.isNotBlank(a.getLogisticsChannelId())).
                map(PackageDTO.ScanResultDTO::getLogisticsChannelId).distinct().collect(Collectors.toList());

        List<LogisticsChannelDTO.BaseDTO> channelList = CollectionUtils.isNotEmpty(logisticsChannelIdList) ? logisticsFeign.listChannelInfoById(logisticsChannelIdList) : Collections.emptyList();
        for (PackageDTO.ScanResultDTO item : list) {
            String logisticsChannelId = item.getLogisticsChannelId();
            LogisticsChannelDTO.BaseDTO logisticsChannel = channelList.stream().
                    filter(l -> l.getId().equals(logisticsChannelId)).findFirst().orElse(null);
            if(logisticsChannel!=null){
                item.setLogisticsChannelName(logisticsChannel.getName());
                item.setLogisticsSupplierId(logisticsChannel.getLogisticsSupplierId());
                item.setLogisticsSupplierName(logisticsChannel.getLogisticsSupplierName());
            }
        }

        Map<String, List<PackageDTO.ScanResultDTO>> map = list.stream().filter(s -> StringUtils.isNotBlank(s.getLogisticsSupplierId())).
                collect(Collectors.groupingBy(req -> req.getLogisticsSupplierId()+"-"+req.getTransferLogisticsChannelId()+"-"+req.getTransferLogisticsSupplierId()));

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
            addDetailList.forEach(addDetail->addDetail.setWeightUnit(weightUnit));
            addDTO.setDetailList(addDetailList);
            result.add(addDTO);

            //自动发货
            if (isAutoOut) {
                soIdList.forEach(soId -> {
                    mqProducerService.asyncClassMsg(RocketMqTopic.ASYNC_MERGE_PACKAGE_DELIVERY_TOPIC, RocketMqTagEnum.ASYNC_MERGE_PACKAGE_DELIVERY_TAG.getName(),
                            soId, StrUtil.uuid().toLowerCase());
                });
            }
        }

        return result;
    }

}
