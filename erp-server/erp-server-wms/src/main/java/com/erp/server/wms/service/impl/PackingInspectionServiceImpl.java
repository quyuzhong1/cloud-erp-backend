package com.erp.server.wms.service.impl;

import cn.hutool.core.text.CharSequenceUtil;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.common.business.threadlocal.UserContext;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.business.constant.RedisCacheConstants;
import com.erp.model.oms.dto.SoB2cDTO;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.oms.entity.SoB2cLogisticsEntity;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.tms.entity.LogisticsChannelEntity;
import com.erp.model.tms.entity.TransferDeclareDetailEntity;
import com.erp.model.wms.dto.PackingInspectionDTO;
import com.erp.model.wms.entity.SoB2cDeliveryDetailEntity;
import com.erp.model.wms.entity.SoB2cDeliveryEntity;
import com.erp.model.wms.enums.PackingInspectionOperationEnum;
import com.erp.rpc.oms.feign.SoB2cFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.tms.feign.LogisticsFeign;
import com.erp.rpc.tms.feign.TransferDeclareFeign;
import com.erp.server.wms.convert.PackingInspectConverter;
import com.erp.server.wms.service.*;
import io.seata.common.util.CollectionUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author liuruipeng
 * @date 2023年12月13日 20:03
 */
@Slf4j
@Service
public class PackingInspectionServiceImpl implements PackingInspectionService {

    @Resource
    private SoB2cDeliveryService soB2cDeliveryService;

    @Resource
    private SoB2cDeliveryDetailService soB2cDeliveryDetailService;

    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private RedisTemplate<String, String> redisTemplate;

    @Resource
    private SoB2cFeign soB2cFeign;

    @Resource
    private OperateLogService operateLogService;

    @Resource
    private TransferDeclareFeign transferDeclareFeign;

    @Lazy
    @Resource
    private AsyncService asyncService;
    @Resource
    private LogisticsFeign logisticsFeign;
    @Resource
    private CfgSettingService cfgSettingService;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public PackingInspectionDTO.ViewDTO scan(PackingInspectionDTO.ScanDTO dto) {
        if (PackingInspectionOperationEnum.BY_ORDER.getCode().equals(dto.getOperationType())) {
            throw new ServiceException("不支持该操作类型");
        }

        //并行获取基础数据
        CompletableFuture<SoB2cDeliveryEntity> deliveryFuture = CompletableFuture.supplyAsync(
                () -> soB2cDeliveryService.getByBusinessCode(dto.getBusinessCode())
        );

        CompletableFuture<TransferDeclareDetailEntity> declareFuture = deliveryFuture.thenCompose(entity -> {
            if (entity == null) {
                throw new ServiceException("查询不到发货单，请确认扫描单号");
            }
            return CompletableFuture.supplyAsync(() -> transferDeclareFeign.getBySoId(entity.getSourceId()));
        });

        CompletableFuture<SoB2cEntity> soB2cFuture = deliveryFuture.thenCompose(entity ->
                CompletableFuture.supplyAsync(() -> soB2cFeign.getById(entity.getSourceId()))
        );

        //等待所有并行任务完成
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(deliveryFuture, declareFuture, soB2cFuture);
        allFutures.join();

        SoB2cDeliveryEntity entity = deliveryFuture.getNow(null);
        TransferDeclareDetailEntity declareDetailEntity = declareFuture.getNow(null);
        SoB2cEntity soB2cEntity = soB2cFuture.getNow(null);

        //快速失败检查
        if (soB2cEntity == null) {
            throw new ServiceException(ApiError.SO_B2C_NOT_FOUND);
        }
        if (soB2cEntity.getIsCancel()) {
            soB2cFeign.deliveryIntercept(new SoB2cDTO.RemarkDTO(soB2cEntity.getId(), "平台取消或退款"));
            return null;
        }
        if (soB2cEntity.getIsIntercept()) {
            throw new ServiceException(ApiError.LOGISTICS_ORDER_INTERCEPTED_NOT_PACKAGE);
        }
        if (soB2cEntity.getInvalidStatus()) {
            throw new ServiceException(ApiError.LOGISTICS_ORDER_VOIDED_NOT_PACKAGE);
        }

        //获取或创建ViewDTO
        PackingInspectionDTO.ViewDTO viewDTO = this.getViewDTO(entity.getId());

        if (viewDTO == null) {
            // 并行获取明细和SKU信息
            CompletableFuture<List<SoB2cDeliveryDetailEntity>> detailFuture = CompletableFuture.supplyAsync(
                    () -> soB2cDeliveryDetailService.listByMainIds(Collections.singletonList(entity.getId()))
            );

            CompletableFuture<Map<String, SkuVO>> skuFuture = detailFuture.thenCompose(details -> {
                if (CollectionUtils.isEmpty(details)) {
                    throw new ServiceException("发货单详情为空");
                }

                Set<String> skuIdSet = new HashSet<>();
                details.forEach(d -> skuIdSet.add(d.getSkuId()));

                return CompletableFuture.supplyAsync(() -> {
                    List<SkuVO> skuList = plmTaskFeign.listSkuPurchaseByIds(new ArrayList<>(skuIdSet));
                    return skuList.stream().collect(Collectors.toMap(SkuVO::getSkuId, Function.identity()));
                });
            });

            // 等待并构建ViewDTO
            List<SoB2cDeliveryDetailEntity> details = detailFuture.join();
            Map<String, SkuVO> skuMap = skuFuture.join();

            viewDTO = PackingInspectConverter.INSTANCE.convertViewDTO(entity, details);
            viewDTO.setScannedSkuList(new ArrayList<>());

            // 设置SKU信息
            viewDTO.getWaitScanSkuList().forEach(scanInfo -> {
                SkuVO sku = skuMap.get(scanInfo.getSkuId());
                if (sku != null) {
                    scanInfo.setProductName(sku.getSkuName());
                    scanInfo.setSkuImageUrl(sku.getSkuImagesUrl());
                    scanInfo.setSkuNo(sku.getSkuNo());
                    scanInfo.setWarehouseLocation(sku.getWarehouseLocation());
                    scanInfo.setEan(sku.getEan());
                }
            });

            // 计算总数
            viewDTO.setSkuSpeciesQty(viewDTO.getWaitScanSkuList().size());
            viewDTO.setSkuTotalQty(viewDTO.getWaitScanSkuList().stream()
                    .mapToInt(PackingInspectionDTO.ViewDTO.ScanSkuInfo::getSaleQty).sum());

            // 处理已扫描项
            Iterator<PackingInspectionDTO.ViewDTO.ScanSkuInfo> it = viewDTO.getWaitScanSkuList().iterator();
            while (it.hasNext()) {
                PackingInspectionDTO.ViewDTO.ScanSkuInfo info = it.next();
                if (info.getScannedQty() > 0) {
                    viewDTO.getScannedSkuList().add(info);
                }
                if (info.getScannedQty().equals(info.getSaleQty())) {
                    it.remove();
                }
            }
        }

        if (CharSequenceUtil.isNotBlank(dto.getSkuNo())) {
            // 参数校验
            if (dto.getScanQty() == null || dto.getScanQty() <= 0) {
                throw new ServiceException(dto.getScanQty() == null ? "扫描数量不能为空" : "扫描数量必须大于0");
            }

            List<PackingInspectionDTO.ViewDTO.ScanSkuInfo> waitScanList = viewDTO.getWaitScanSkuList();
            List<PackingInspectionDTO.ViewDTO.ScanSkuInfo> scannedList = viewDTO.getScannedSkuList();

            Optional<PackingInspectionDTO.ViewDTO.ScanSkuInfo> matchedSku = waitScanList.stream()
                    .filter(v -> dto.getSkuNo().equals(v.getSkuNo()) || dto.getSkuNo().equals(v.getEan()))
                    .findFirst();

            if (!matchedSku.isPresent()) {
                // 检查是否已扫描完成
                boolean alreadyScanned = scannedList.stream()
                        .anyMatch(v -> (v.getSkuNo().equals(dto.getSkuNo()) || dto.getSkuNo().equals(v.getEan()))
                                && v.getScannedQty().equals(v.getSaleQty()));

                if (alreadyScanned) {
                    throw new ServiceException("SKU已验货完成，无需再次验货");
                }
                throw new ServiceException("SKU或EAN不匹配，验货失败");
            }

            PackingInspectionDTO.ViewDTO.ScanSkuInfo scanSkuInfo = matchedSku.get();
            if (scanSkuInfo.getWaitScanQty() <= 0) {
                throw new ServiceException("SKU已验货完成，无需再次验货");
            }

            if (dto.getScanQty() > scanSkuInfo.getWaitScanQty()) {
                throw new ServiceException("扫描数量大于待扫描数量");
            }

            // 更新扫描数量
            scanSkuInfo.setWaitScanQty(scanSkuInfo.getWaitScanQty() - dto.getScanQty());
            scanSkuInfo.setScannedQty(scanSkuInfo.getSaleQty() - scanSkuInfo.getWaitScanQty());

            // 更新已扫描列表
            Optional<PackingInspectionDTO.ViewDTO.ScanSkuInfo> existingScanned = scannedList.stream()
                    .filter(v -> (v.getSkuNo().equals(dto.getSkuNo()) || dto.getSkuNo().equals(v.getEan()))
                            && !v.getScannedQty().equals(v.getSaleQty()))
                    .findFirst();

            if (existingScanned.isPresent()) {
                existingScanned.get().setScannedQty(scanSkuInfo.getSaleQty() - scanSkuInfo.getWaitScanQty());
            } else {
                scannedList.add(scanSkuInfo);
            }

            // 如果扫描完成，从待扫描列表移除
            if (scanSkuInfo.getScannedQty().equals(scanSkuInfo.getSaleQty())) {
                waitScanList.remove(scanSkuInfo);
            }
        } else if(entity.getIsInspection()){
            throw new ServiceException("订单已验货，无法重复验货");
        }
            // 只有未验货的订单才能进行整体验货
            // 判断是否全部扫描完成
            if (CollectionUtils.isEmpty(viewDTO.getWaitScanSkuList())) {

                if (dto.getIsPrint() && (viewDTO.getTrackNo() == null || viewDTO.getPaperSize() == null)) {
                    List<SoB2cLogisticsEntity> logisticsList = soB2cFeign.listSoB2cLogisticsByMainIdList(
                            Collections.singletonList(soB2cEntity.getId()));

                    if (CollectionUtils.isNotEmpty(logisticsList)) {
                        SoB2cLogisticsEntity logistics = logisticsList.get(0);
                        viewDTO.setTrackNo(logistics.getTrackNo());

                        LogisticsChannelEntity channel = logisticsFeign.getChannelById(logistics.getLogisticsChannelId());
                        if (channel != null) {
                            viewDTO.setPaperSize(channel.getPaperSize());
                            viewDTO.setPrinterName(cfgSettingService.getPrinterNameByPaperSize(channel.getPaperSize()));
                        }
                    }
                }

                // 批量更新
                List<SoB2cDeliveryDetailEntity> detailEntityList = soB2cDeliveryDetailService.listByMainIds(
                        Collections.singletonList(entity.getId()));

                detailEntityList.forEach(v -> v.setWaitScanQty(0));
                entity.setIsInspection(true);
                entity.setInspectionTime(LocalDateTime.now());
                entity.setIsAutoOut(dto.getIsAutoOut());
                viewDTO.setStatus(true);

                // 批量更新操作
                boolean updateSuccess = soB2cDeliveryService.updateById(entity)
                        && soB2cDeliveryDetailService.updateBatchById(detailEntityList);

                if (!updateSuccess) {
                    throw new ServiceException("发货单更新失败");
                }

                // 记录日志
                String msg = CharSequenceUtil.format(
                        "用户【{}】更新【{}】单据单号为【{}】包装验货完成",
                        UserContext.getDefaultLoginUser().getUserName(),
                        "b2c发货单",
                        entity.getCode());
                operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SO_B2C_DELIVERY.getCode(), entity.getId(), "包装验货");
            }

        //数据存redis
        this.saveViewDTO(entity.getId(), viewDTO);

        viewDTO.setTransferStatus(soB2cEntity.getTransferStatus());
        viewDTO.setOrderUploadStatus(declareDetailEntity != null ? declareDetailEntity.getOrderUploadStatus() : null);
        return viewDTO;
    }

    @Override
    public void reset(String id) {
        SoB2cDeliveryEntity entity = soB2cDeliveryService.getById(id);
        if(Objects.isNull(entity)){
            throw new ServiceException("查询的发货单为空");
        }
        List<SoB2cDeliveryDetailEntity> detailEntityList = soB2cDeliveryDetailService.listByMainIds(Collections.singletonList(entity.getId()));
        detailEntityList.forEach(v-> v.setWaitScanQty(v.getDeliveryQty()));
        entity.setIsInspection(false);
        entity.setInspectionTime(null);
        if(!soB2cDeliveryService.updateById(entity)){
            throw new ServiceException("发货单更新失败");
        }
        if(!soB2cDeliveryDetailService.updateBatchById(detailEntityList)){
            throw new ServiceException("发货单明细更新失败");
        }
        this.deleteViewDTO(id);
    }

    private void deleteViewDTO(String id) {
        redisTemplate.delete(CharSequenceUtil.format(RedisCacheConstants.WMS_PACKING_INSPECTION, id));
    }


    private void saveViewDTO(String id, PackingInspectionDTO.ViewDTO viewDTO) {
        String json = JSONObject.toJSONString(viewDTO);
        redisTemplate.opsForValue().set(CharSequenceUtil.format(RedisCacheConstants.WMS_PACKING_INSPECTION, id), json,1, TimeUnit.DAYS);
    }

    private PackingInspectionDTO.ViewDTO getViewDTO(String id) {
        String json = redisTemplate.opsForValue().get(CharSequenceUtil.format(RedisCacheConstants.WMS_PACKING_INSPECTION, id));
        return JSONObject.parseObject(json,new TypeReference<PackingInspectionDTO.ViewDTO>() {}.getType());
    }
}
