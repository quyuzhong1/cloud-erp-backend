package com.erp.server.wms.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.common.business.dto.PlatformShipOrderDTO;
import com.common.business.handler.PlatformSaveHandler;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.message.constant.RedisKeyConstant;
import com.erp.model.oms.dto.SoB2cDTO;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.oms.entity.SoB2cLogisticsEntity;
import com.erp.model.oms.enums.SoB2cBillStatusEnum;
import com.erp.model.oms.enums.TransferStatusEnum;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.tms.entity.TransferDeclareDetailEntity;
import com.erp.model.tms.enums.TransferDeclareUploadStatusEnum;
import com.erp.model.wms.dto.PackingInspectionDTO;
import com.erp.model.wms.entity.SoB2cDeliveryDetailEntity;
import com.erp.model.wms.entity.SoB2cDeliveryEntity;
import com.erp.model.wms.enums.PackingInspectionOperationEnum;
import com.erp.model.wms.enums.SoB2cDeliveryStatusEnum;
import com.erp.rpc.oms.feign.SoB2cFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.tms.feign.TransferDeclareFeign;
import com.erp.server.wms.convert.PackingInspectConverter;
import com.erp.server.wms.service.*;
import io.seata.common.util.CollectionUtils;
import io.seata.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;
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
    private CommonService commonService;

    @Resource
    private TransferDeclareFeign transferDeclareFeign;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public PackingInspectionDTO.ViewDTO scan(PackingInspectionDTO.ScanDTO dto) {
        if(PackingInspectionOperationEnum.BY_ORDER.getCode().equals(dto.getOperationType())){
            throw new ServiceException("不支持该操作类型");
        }
        SoB2cDeliveryEntity entity = soB2cDeliveryService.getByBusinessCode(dto.getBusinessCode());
        if(Objects.isNull(entity)){
            throw new ServiceException("查询不到发货单，请确认扫描单号");
        }

        TransferDeclareDetailEntity declareDetailEntity = transferDeclareFeign.getBySoId(entity.getSourceId());
        if (ObjectUtil.isEmpty(declareDetailEntity)) {
            declareDetailEntity = new TransferDeclareDetailEntity();
        }

        SoB2cEntity soB2cEntity = soB2cFeign.getById(entity.getSourceId());
        if(ObjectUtil.isEmpty(soB2cEntity)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_NOT_EXIST);
        }
        //验证订单平台是否取消
        if (soB2cEntity.getIsCancel()) {
            //订单拦截
            soB2cFeign.deliveryIntercept(new SoB2cDTO.RemarkDTO(soB2cEntity.getId(), "平台取消"));
            return null;
        }

        //因为明细只保存父级SKU，所以如果有组合品没办法直接更新明细，将明细sku拆分放到redis，扫描时操作redis的值，在最后全部扫描完成统一更新数据库
        PackingInspectionDTO.ViewDTO viewDTO = this.getViewDTO(entity.getId());
        //redis没有值，说明可能是开始扫描，或者过期
        if(Objects.isNull(viewDTO)){
            List<SoB2cDeliveryDetailEntity> detailEntityList = soB2cDeliveryDetailService.listByMainIds(Collections.singletonList(entity.getId()));
            if(CollectionUtils.isEmpty(detailEntityList)){
                throw new ServiceException("发货单详情为空");
            }
            //组合产品，按最新BOM拆分为子产品和销售数量显示
            //根据SKU查询BOM判断是否是组合SKU
            List<String> skuIdList = detailEntityList.stream().map(SoB2cDeliveryDetailEntity::getSkuId).distinct().collect(Collectors.toList());
            //查询sku基础信息
            List<SkuVO> skuVOList = plmTaskFeign.getSkuInfoByIds(skuIdList);
            Map<String,SkuVO> skuVOMap = skuVOList.stream().collect(Collectors.toMap(SkuVO::getSkuId,Function.identity()));
            PackingInspectionDTO.ViewDTO addViewDTO;
            addViewDTO = PackingInspectConverter.INSTANCE.convertViewDTO(entity,detailEntityList);
            addViewDTO.setScannedSkuList(new ArrayList<>());
            //设置sku信息
            for (PackingInspectionDTO.ViewDTO.ScanSkuInfo scanSkuInfo: addViewDTO.getWaitScanSkuList()){
                SkuVO skuVO = skuVOMap.get(scanSkuInfo.getSkuId());
                if(Objects.nonNull(skuVO)){
                    scanSkuInfo.setProductName(skuVO.getSkuName());
                    scanSkuInfo.setSkuImageUrl(skuVO.getSkuImagesUrl());
                    scanSkuInfo.setSkuNo(skuVO.getSkuNo());
                    scanSkuInfo.setWarehouseLocation(skuVO.getWarehouseLocation());
                    scanSkuInfo.setEan(skuVO.getEan());
                }
            }
            addViewDTO.setSkuSpeciesQty(addViewDTO.getWaitScanSkuList().size()+addViewDTO.getScannedSkuList().size());
            addViewDTO.setSkuTotalQty(addViewDTO.getWaitScanSkuList().stream().mapToInt(PackingInspectionDTO.ViewDTO.ScanSkuInfo::getSaleQty).sum()+addViewDTO.getScannedSkuList().stream().mapToInt(PackingInspectionDTO.ViewDTO.ScanSkuInfo::getSaleQty).sum());
            Iterator<PackingInspectionDTO.ViewDTO.ScanSkuInfo> it = addViewDTO.getWaitScanSkuList().iterator();
            while (it.hasNext()) {
                PackingInspectionDTO.ViewDTO.ScanSkuInfo scanSkuInfo = it.next();
                if(scanSkuInfo.getScannedQty() > 0){
                    addViewDTO.getScannedSkuList().add(scanSkuInfo);
                }
                if(scanSkuInfo.getScannedQty().equals(scanSkuInfo.getSaleQty())){
                    it.remove();
                }
            }
            viewDTO = addViewDTO;
            //跟踪号赋值
            if(viewDTO.getTrackNo() == null){
                List<SoB2cLogisticsEntity> soB2cLogisticsEntityList = soB2cFeign.listSoB2cLogisticsByMainIdList(Arrays.asList(soB2cEntity.getId()));
                if(CollectionUtils.isNotEmpty(soB2cLogisticsEntityList)){
                    SoB2cLogisticsEntity soB2cLogisticsEntity = soB2cLogisticsEntityList.get(0);
                    viewDTO.setTrackNo(soB2cLogisticsEntity.getTrackNo());
                }
            }
        }
        if(StringUtils.isNotBlank(dto.getSkuNo())){
            //可能扫描sku编号或ean码
            List<PackingInspectionDTO.ViewDTO.ScanSkuInfo> waitScanList = viewDTO.getWaitScanSkuList();
            PackingInspectionDTO.ViewDTO.ScanSkuInfo skuInfo = waitScanList.stream().filter(v->v.getSkuNo().equals(dto.getSkuNo())).findFirst().orElse(null);
            PackingInspectionDTO.ViewDTO.ScanSkuInfo eanInfo = waitScanList.stream().filter(v->dto.getSkuNo().equals(v.getEan())).findFirst().orElse(null);
            if(Objects.nonNull(skuInfo) && Objects.nonNull(eanInfo) && !skuInfo.getSkuId().equals(eanInfo.getSkuId())){
                throw new ServiceException("有超过一个sku编号或ean码匹配，请确认");
            }
            if(Objects.isNull(dto.getScanQty())){
                throw new ServiceException("扫描数量不能为空");
            }
            if(dto.getScanQty() <= 0){
                throw new ServiceException("扫描数量必须大于0");
            }
            //修改对应SKU扫描数量
            Iterator<PackingInspectionDTO.ViewDTO.ScanSkuInfo> it = waitScanList.iterator();
            List<PackingInspectionDTO.ViewDTO.ScanSkuInfo> scannedList = viewDTO.getScannedSkuList();
            boolean matchFlag = false;
            while (it.hasNext()) {
                PackingInspectionDTO.ViewDTO.ScanSkuInfo scanSkuInfo = it.next();
                if(dto.getSkuNo().equals(scanSkuInfo.getSkuNo()) || dto.getSkuNo().equals(scanSkuInfo.getEan())){
                    matchFlag = true;
                    if(scanSkuInfo.getWaitScanQty() <= 0){
                        throw new ServiceException("SKU已验货完成，无需再次验货");
                    }
                    //更新已扫描的数据
                    PackingInspectionDTO.ViewDTO.ScanSkuInfo scanned = scannedList.stream().filter(v->(v.getSkuNo().equals(dto.getSkuNo()) || dto.getSkuNo().equals(v.getEan())) && !v.getScannedQty().equals(v.getSaleQty())).findFirst().orElse(null);
                    if(Objects.isNull(scanned)){
                        viewDTO.getScannedSkuList().add(scanSkuInfo);
                    }else{
                        scanned.setScannedQty(scanSkuInfo.getSaleQty() - scanSkuInfo.getWaitScanQty());
                    }
                    if(dto.getScanQty() > scanSkuInfo.getWaitScanQty()){
                        throw new ServiceException("扫描数量大于待扫描数量");
                    }
                    scanSkuInfo.setWaitScanQty(scanSkuInfo.getWaitScanQty()-dto.getScanQty());
                    scanSkuInfo.setScannedQty(scanSkuInfo.getSaleQty() - scanSkuInfo.getWaitScanQty());
                    //如果已扫描数等于销售数，放到已扫描队列
                    if(scanSkuInfo.getScannedQty().equals(scanSkuInfo.getSaleQty())){
                        it.remove();
                    }
                    break;
                }
            }
            if(!matchFlag){
                PackingInspectionDTO.ViewDTO.ScanSkuInfo scanned = scannedList.stream().filter(v->(v.getSkuNo().equals(dto.getSkuNo())||dto.getSkuNo().equals(v.getEan())) && v.getScannedQty().equals(v.getSaleQty())).findFirst().orElse(null);
                if(Objects.nonNull(scanned)){
                    throw new ServiceException("SKU已验货完成，无需再次验货");
                }
                throw new ServiceException("SKU或EAN不匹配，验货失败");
            }
        }else{
            if(entity.getIsInspection()){
                throw new ServiceException("订单已验货，无法重复验货");
            }
        }
        //判断是否全部扫描完成
        if(CollectionUtils.isEmpty(viewDTO.getWaitScanSkuList())){
            //更新数据
            List<SoB2cDeliveryDetailEntity> detailEntityList = soB2cDeliveryDetailService.listByMainIds(Collections.singletonList(entity.getId()));
            detailEntityList.forEach(v-> v.setWaitScanQty(0));
            entity.setIsInspection(true);
            entity.setInspectionTime(LocalDateTime.now());
            viewDTO.setStatus(true);
            if(!soB2cDeliveryService.updateById(entity)){
                throw new ServiceException("发货单更新失败");
            }
            if(!soB2cDeliveryDetailService.updateBatchById(detailEntityList)){
                throw new ServiceException("发货单明细更新失败");
            }
            String msg = StrUtil.format("用户【{}】更新【{}】单据单号为【{}】包装验货完成", commonService.getUserInfo().getUserName(), "b2c发货单", entity.getCode());
            operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SO_B2C_DELIVERY.getCode(), entity.getId(), "包装验货");
        }
        //数据存redis
        this.saveViewDTO(entity.getId(),viewDTO);

        viewDTO.setTransferStatus(soB2cEntity.getTransferStatus());
        viewDTO.setOrderUploadStatus(declareDetailEntity.getOrderUploadStatus());

        //直接出库
        if (CollectionUtils.isEmpty(viewDTO.getWaitScanSkuList()) && dto.getIsAutoOut()  && entity.getIsInspection()) {
            //如果是待上传或上传失败则直接返回
            if (StrUtil.equals(soB2cEntity.getTransferStatus(), TransferStatusEnum.WAIT.getCode()) || StrUtil.equals(declareDetailEntity.getOrderUploadStatus(), TransferDeclareUploadStatusEnum.WAIT_UPLOAD.getCode()) ||
                    StrUtil.equals(declareDetailEntity.getOrderUploadStatus(),TransferDeclareUploadStatusEnum.UPLOAD_FAILURE.getCode())) {
                return viewDTO;
            }

            //调用第三方平台SDK发货
            try {
                if (soB2cFeign.checkPlatformShipOrder(entity.getSourceId())) {
                    //调用第三方平台SDK发货
                    PlatformShipOrderDTO platformShipOrderDTO = new PlatformShipOrderDTO();
                    platformShipOrderDTO.setSoB2cId(entity.getSourceId());
                    platformShipOrderDTO.setDictPlatform(entity.getDictPlatform());
                    PlatformSaveHandler.shipOrder(platformShipOrderDTO);
                }
            } catch (Exception e) {
                log.error("销售单【{}】 标记发货失败 >>>错误信息{}", e.getMessage());
                throw new ServiceException(ApiError.PLATFORM_SHIP_ORDER_ERROR, entity.getDictPlatform(), e.getMessage());
            }

            //获取一个当前时间当作发货时间
            LocalDateTime deliveryTime = LocalDateTime.now();

            //将发货状态更新为已发货
            entity.setStatus(SoB2cDeliveryStatusEnum.SHIPPED.getCode());
            entity.setDeliveryTime(deliveryTime);
            if (!soB2cDeliveryService.updateById(entity)) {
                throw new ServiceException("发货单更新失败");
            }

            //修改订单状态待发货
            SoB2cDTO.UpdateDeliveryTimeDTO updateDeliveryTimeDTO = new SoB2cDTO.UpdateDeliveryTimeDTO();
            updateDeliveryTimeDTO.setSoB2cIds(Arrays.asList(entity.getSourceId()));
            updateDeliveryTimeDTO.setStatus(SoB2cBillStatusEnum.ENUM_SHIPPED.getCode());
            updateDeliveryTimeDTO.setDeliveryTime(LocalDateTime.now());
            soB2cFeign.updateSoB2cStatusAndDeliveryTime(updateDeliveryTimeDTO);

            //出库
            soB2cDeliveryService.generateB2cSoOutstock(entity);

            String msg = StrUtil.format("用户【{}】通过【{}】触发单据编号【{}】的自动发货功能", commonService.getUserInfo().getUserName(), "包装验货", entity.getCode());
            operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SO_B2C_DELIVERY.getCode(), entity.getId(), "包装验货");
        }
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
        redisTemplate.delete(StrUtil.format(RedisKeyConstant.WMS_PACKING_INSPECTION, id));
    }


    private void saveViewDTO(String id, PackingInspectionDTO.ViewDTO viewDTO) {
        String json = JSONObject.toJSONString(viewDTO);
        redisTemplate.opsForValue().set(StrUtil.format(RedisKeyConstant.WMS_PACKING_INSPECTION, id), json,1, TimeUnit.DAYS);
    }

    private PackingInspectionDTO.ViewDTO getViewDTO(String id) {
        String json = redisTemplate.opsForValue().get(StrUtil.format(RedisKeyConstant.WMS_PACKING_INSPECTION, id));
        return JSONObject.parseObject(json,new TypeReference<PackingInspectionDTO.ViewDTO>() {}.getType());
    }
}
