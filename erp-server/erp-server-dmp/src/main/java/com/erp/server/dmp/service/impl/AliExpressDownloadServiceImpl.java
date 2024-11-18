package com.erp.server.dmp.service.impl;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import com.common.business.constant.MongoTableNameContant;
import com.common.business.dto.PlatformOrderDTO;
import com.common.business.enums.BusinessTypeEnum;
import com.common.core.exception.ServiceException;
import com.erp.model.dmp.entity.PlatformApiTaskEntity;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.oms.aliexpress.dto.AliExpressShopInfoDTO;
import com.erp.oms.aliexpress.dto.PlatformAliExpressOrderDTO;
import com.erp.oms.aliexpress.handler.AliExpressOrderHandler;
import com.erp.oms.aliexpress.service.AliExpressOrderService;
import com.erp.server.dmp.enums.DownloadStatusEnum;
import com.erp.server.dmp.service.AliExpressDownloadService;
import com.erp.server.dmp.service.DmpPushTaskService;
import com.xxl.job.core.context.XxlJobHelper;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;


/**
 * 速卖通下载 服务类
 *
 * @author Jim
 * @date 22024/6/11
 */
@Slf4j
@Service
public class AliExpressDownloadServiceImpl implements AliExpressDownloadService {

    @Resource
    private MongoTemplate mongoTemplate;

    @Resource
    private AliExpressOrderHandler aliExpressOrderHandler;

    @Resource
    private DmpPushTaskService dmpPushTaskService;

    @Resource
    private BusinessServiceImpl businessService;

    @Resource
    private AliExpressOrderService aliExpressOrderService;
    @Lazy
    @Resource
    private AliExpressDownloadService aliExpressDownloadService;


    @Override
    public void handlerAddressDetail(String key, List<PlatformApiTaskEntity> list, String platform, String category, String business, Integer size) {
        // 店铺IDS
        List<String> shopIds = list.stream().map(PlatformApiTaskEntity::getShopId).distinct().collect(Collectors.toList());
        List<PlatformAliExpressOrderDTO> orderEntityList = this.mongoListPlatformOrder(shopIds, DownloadStatusEnum.FINISH.getCode(), DownloadStatusEnum.WAIT.getCode(), null, null,false, 1, size);
        if (CollectionUtils.isEmpty(orderEntityList)) {
            return;
        }
        for (PlatformAliExpressOrderDTO item : orderEntityList) {
            try {
                //下载地址处理
                PlatformAliExpressOrderDTO newDto = aliExpressOrderHandler.downloadAddress(item);
                newDto.setDownloadAddressStatus(1);
                // 判断是否有发货单下载(属于平台仓订单并且有物流信息)
                newDto.setDownloadDeliveryStatus(newDto.convertDownloadDeliveryStatus());
                newDto.setPlatformWarehouseOrder(newDto.isPlatformWarehouseOrder());
                newDto.setDownloadTime(LocalDateTime.now(ZoneId.systemDefault()).toString());
                newDto.setIsClean(2);
                List<PlatformOrderDTO> convertDto = aliExpressOrderHandler.convert(Collections.singletonList(newDto));
                // 保存和发送mq
                businessService.pullDetailProcess(newDto, convertDto.get(0), category, platform, business);
            } catch (Exception e) {
                // 发送预警
                dmpPushTaskService.sendWarnMsg(item.getDmpSyncTaskId());
                log.error("下载地址处理失败, 订单号:{}，异常信息:{}", item.getUniqueId(), e.getMessage());
            }
        }
    }

    @Override
    public List<PlatformAliExpressOrderDTO> mongoListPlatformOrder(List<String> shopIds,
                                                                   Integer downloadStatus,
                                                                   Integer downloadAddressStatus,
                                                                   Integer downloadDeliveryStatus,
                                                                   Integer downloadDeliveryDetailStatus,
                                                                   Boolean hasPlatformWarehouseOrder,
                                                                   int currentPage,
                                                                   int pageSize
    ) {
        Query query = new Query();
        Criteria criteria = Criteria.where("shopId").in(shopIds);
        if (null != downloadStatus) {
            criteria.and("downloadStatus").is(downloadStatus);
        }
        if (null != downloadAddressStatus) {
            criteria.and("downloadAddressStatus").is(downloadAddressStatus);
        }
        if (null != downloadDeliveryStatus) {
            criteria.and("downloadDeliveryStatus").is(downloadDeliveryStatus);
        }
        if (null != downloadDeliveryDetailStatus) {
            criteria.and("downloadDeliveryDetailStatus").is(downloadDeliveryDetailStatus);
        }
        if (null != hasPlatformWarehouseOrder){
            criteria.and("platformWarehouseOrder").is(hasPlatformWarehouseOrder);
        }
        query.addCriteria(criteria);
        if (currentPage > 0 && pageSize > 0) {
            query.skip((long) (currentPage - 1) * pageSize).limit(pageSize);
        }

        return mongoTemplate.find(query, PlatformAliExpressOrderDTO.class, MongoTableNameContant.THIRD_SYSTEM_ALI_EXPRESS_ORDER);
    }

    @Override
    public void handlerOrderDetailDownload(List<PlatformApiTaskEntity> taskList, Integer size, String platform, String category, List<ShopInfoEntity> list) {
        List<String> queryShopIds = taskList.stream().map(PlatformApiTaskEntity::getShopId).distinct().collect(Collectors.toList());
        // 查询当前分组未下载的mongo订单
        List<PlatformAliExpressOrderDTO> orderEntityList = this.mongoListPlatformOrder(queryShopIds, DownloadStatusEnum.WAIT.getCode(), null, null, null, null,1, size);
        if (CollectionUtil.isEmpty(orderEntityList)) {
            XxlJobHelper.log("[拉取速卖通订单详情任务] 无需要执行的详情,shopId={}", JSONUtil.toJsonStr(queryShopIds));
            return;
        }
        for (PlatformAliExpressOrderDTO dto : orderEntityList) {
            singleHandlerOrderDetailDownload(platform, category, dto);
        }
    }

    @Override
    public void singleHandlerOrderDetailDownload(String platform, String category, PlatformAliExpressOrderDTO dto) {
        try {
            PlatformAliExpressOrderDTO newDto = aliExpressOrderHandler.downloadDetail(dto, null);
            newDto.setDownloadStatus(1);
            newDto.setDownloadAddressStatus(0);
            newDto.setPlatformWarehouseOrder(newDto.isPlatformWarehouseOrder());
            // 判断是否有发货单下载(属于平台仓订单并且有物流信息)
            newDto.setDownloadDeliveryStatus(newDto.convertDownloadDeliveryStatus());
            newDto.setDownloadTime(LocalDateTime.now(ZoneId.systemDefault()).toString());
            List<PlatformOrderDTO> convertDtoList = aliExpressOrderHandler.convert(Collections.singletonList(newDto));
            PlatformOrderDTO convertDto = convertDtoList.get(0);
            String business = BusinessTypeEnum.ORDER.getCode();

            businessService.pullDetailProcess(newDto, convertDto, category, platform, business);
            log.info("[拉取速卖通订单详情任务] 下载成功，uniqueId={}", dto.getUniqueId());
            XxlJobHelper.log("[拉取速卖通订单详情任务] amazonSalesOrderDetail下载成功，uniqueId={}", dto.getUniqueId());
        } catch (Exception error) {
            XxlJobHelper.log("[拉取速卖通订单详情任务] 下载失败，uniqueId={}, error={}",
                    dto.getUniqueId(),
                    error.getMessage());
            // 发送预警
            dmpPushTaskService.sendWarnMsg(dto.getDmpSyncTaskId());
        }
    }

    @Override
    public void handlerSoDeliveryDownload(List<PlatformApiTaskEntity> value, Integer size, String platform, String category, List<ShopInfoEntity> list) {
        List<String> queryShopIds = value.stream().map(PlatformApiTaskEntity::getShopId).distinct().collect(Collectors.toList());
        // 查询当前分组未下载的mongo订单
        List<PlatformAliExpressOrderDTO> orderEntityList = this.mongoListPlatformOrder(queryShopIds, DownloadStatusEnum.FINISH.getCode(), null, DownloadStatusEnum.WAIT.getCode(), null,true, 1, size);
        if (CollectionUtil.isEmpty(orderEntityList)) {
            XxlJobHelper.log("[拉取速卖通发货单任务] 无需要执行的详情,shopId={}", JSONUtil.toJsonStr(queryShopIds));
            return;
        }
        Map<String, List<PlatformAliExpressOrderDTO>> groupMap = orderEntityList.stream().collect(Collectors.groupingBy(PlatformAliExpressOrderDTO::getShopId));
        for (Map.Entry<String, List<PlatformAliExpressOrderDTO>> entry : groupMap.entrySet()) {
            aliExpressDownloadService.singleHandlerSoDeliveryDownload(entry.getKey(), platform, category, entry.getValue());
        }
    }

    @Override
    // @Transactional(rollbackFor = Exception.class, transactionManager = "mongoTransactionManager")
    public void singleHandlerSoDeliveryDownload(String shopId, String platform, String category, List<PlatformAliExpressOrderDTO> dtoList) {
        try {
            AliExpressShopInfoDTO shopInfoDTO = aliExpressOrderService.getShopInfoByShopId(shopId);
            if (null == shopInfoDTO) {
                log.error("[速卖通发货单下载]  获取 token 失败: shopId={}", shopId);
                String msg = StrUtil.format("[速卖通发货单下载]  获取 token 失败: shopId={}", shopId);
                throw new ServiceException(msg);
            }
            List<PlatformAliExpressOrderDTO> deliveryList = aliExpressOrderHandler.getDeliveryList(shopInfoDTO, dtoList);
            // 批量更新到mongo
            businessService.handleSaveOrUpdateMongo(deliveryList, MongoTableNameContant.THIRD_SYSTEM_ALI_EXPRESS_ORDER, PlatformAliExpressOrderDTO.class, new ArrayList<>());
            List<String> orderIds = dtoList.stream().map(e -> e.getAliExpressOrder().getOrderId()).distinct().collect(Collectors.toList());
            log.info("[拉取速卖通发货单任务] 下载成功，orderIds={}", orderIds);
            XxlJobHelper.log("[拉取速卖通发货单任务] 下载成功，orderIds={}", orderIds);
        } catch (Exception error) {
            log.error("[拉取速卖通发货单任务] 下载失败，orderIds={}, error={}", shopId, ExceptionUtil.stacktraceToString(error));
            XxlJobHelper.log("[拉取速卖通发货单任务] 下载失败，orderIds={}, error={}",
                    shopId,
                    ExceptionUtil.stacktraceToString(error));
        }
    }


    @Override
    public void handlerSoDeliveryDetailDownload(List<PlatformApiTaskEntity> value, Integer size, String platform, String category, List<ShopInfoEntity> list) {
        List<String> queryShopIds = value.stream().map(PlatformApiTaskEntity::getShopId).distinct().collect(Collectors.toList());
        // 查询当前分组未下载的mongo订单
        List<PlatformAliExpressOrderDTO> orderEntityList = this.mongoListPlatformOrder(queryShopIds, DownloadStatusEnum.FINISH.getCode(), null, DownloadStatusEnum.FINISH.getCode(), DownloadStatusEnum.WAIT.getCode(), null,1, size);
        if (CollectionUtil.isEmpty(orderEntityList)) {
            XxlJobHelper.log("[拉取速卖通订单详情任务] 无需要执行的详情,shopId={}", JSONUtil.toJsonStr(queryShopIds));
            return;
        }
        for (PlatformAliExpressOrderDTO dto : orderEntityList) {
            singleHandlerSoDeliveryDetailDownload(platform, category, dto);
        }
    }

    @Override
    public void singleHandlerSoDeliveryDetailDownload(String platform, String category, PlatformAliExpressOrderDTO dto) {
        try {
            PlatformAliExpressOrderDTO newDto = aliExpressOrderHandler.downloadDeliveryDetail(dto);
            newDto.setDownloadDeliveryStatus(1);
            newDto.setDownloadDeliveryDetailStatus(1);
            newDto.setPlatformWarehouseOrder(newDto.isPlatformWarehouseOrder());
            newDto.setDownloadTime(LocalDateTime.now(ZoneId.systemDefault()).toString());
            List<PlatformOrderDTO> convertDtoList = aliExpressOrderHandler.convert(Collections.singletonList(newDto));
            PlatformOrderDTO convertDto = convertDtoList.get(0);
            String business = BusinessTypeEnum.ORDER.getCode();

            businessService.pullDetailProcess(newDto, convertDto, category, platform, business);
            log.info("[拉取速卖通发货单详情任务] 下载成功，uniqueId={}", dto.getUniqueId());
            XxlJobHelper.log("[拉取速卖通发货单详情任务] 下载成功，uniqueId={}", dto.getUniqueId());
        } catch (Exception error) {
            XxlJobHelper.log("[拉取速卖通发货单详情任务] 下载失败，uniqueId={}, error={}",
                    dto.getUniqueId(),
                    ExceptionUtil.stacktraceToString(error));
            // 发送预警
            dmpPushTaskService.sendWarnMsg(dto.getDmpSyncTaskId());
        }
    }
}
