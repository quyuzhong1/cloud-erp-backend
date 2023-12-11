package com.erp.server.oms.rocketmq;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.common.business.config.DocNoGenHelper;
import com.common.business.dto.DmpSyncMqDTO;
import com.common.business.dto.DmpSyncTaskIdDTO;
import com.common.business.dto.PlatformOrderDTO;
import com.common.business.dto.PlatformOrderDetailDTO;
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.business.enums.SyncStatusEnum;
import com.common.core.controller.vo.ApiResult;
import com.common.core.exception.ServiceException;
import com.common.message.constant.RocketMqTopic;
import com.common.message.handler.AbstractPlatformConsumerHandler;
import com.erp.model.dmp.entity.DmpPullTaskEntity;
import com.erp.model.msg.dto.WarnMsgInfoDTO;
import com.erp.model.oms.dto.ListingInfoWithSkuMappingDTO;
import com.erp.model.oms.entity.*;
import com.erp.model.oms.enums.SoB2cBillStatusEnum;
import com.erp.model.wms.enums.BillTypeEnum;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.server.oms.service.*;
import io.seata.common.util.CollectionUtils;
import io.seata.common.util.StringUtils;
import jodd.util.CollectionUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 下载平台订单消费服务
 *
 * @author Cloud
 */
@Service
@Slf4j
@RocketMQMessageListener(topic = RocketMqTopic.PLATFORM_PULL_DATA_TOPIC,
        selectorExpression = "third_system_order_tag",
        consumerGroup = "${spring.cloud.nacos.discovery.namespace}-platform_pull_order_consumer",
        consumeMode = ConsumeMode.ORDERLY)
public class PlatformOrderConsumerService<T extends DmpSyncTaskIdDTO> extends AbstractPlatformConsumerHandler<T> {

    @Resource
    private DmpTaskFeign dmpTaskFeign;
    @Resource
    private SoB2cService soB2cService;
    @Resource
    private SoB2cDetailService soB2cDetailService;
    @Resource
    private SoB2cLogisticsService soB2cLogisticsService;
    @Resource
    private SoB2cReceiverService soB2cReceiverService;
    @Resource
    private SoB2cFinanceService soB2cFinanceService;
    @Resource
    private DocNoGenHelper docNoGenHelper;
    @Resource
    private CustomerB2cService customerB2cService;
    @Resource
    private CustomerB2cAddressService customerB2cAddressService;
    @Resource
    private CustomerB2cGroupService customerB2cGroupService;
    @Resource
    private CustomerB2cContactService customerB2cContactService;
    @Resource
    private CustomerB2cSellerService customerB2cSellerService;
    @Resource
    private ShopInfoService shopInfoService;


    @Override
    public void updateSyncTaskStatus(String id, SyncStatusEnum code, String msg) {
        dmpTaskFeign.updateSyncInfo(new DmpSyncMqDTO.ParamDTO(id, code.getCode(), msg));
    }

    @Override
    public void sendWarnMsg(String syncTaskId) {
//        dmpTaskFeign.sendWarnMsg(syncTaskId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResult<?> handle(Object ext) {
        PlatformOrderDTO dto = JSONUtil.toBean(ext.toString(), PlatformOrderDTO.class);

        // 查询关联关系
        List<String> platformSkuList = dto.getDetails()
                .stream()
                .map(PlatformOrderDetailDTO::getPlatformSkuNo)
                .distinct().collect(Collectors.toList());
        Map<String, ListingInfoWithSkuMappingDTO> listingInfoWithSkuMappingDTOMap = soB2cDetailService.mapListingByPlatformSkuNo(platformSkuList, dto.getDictPlatform());

        // 查询当前店铺信息
        ShopInfoEntity shopInfo = shopInfoService.getById(dto.getShopId());
        if (null == shopInfo) {
            throw new ServiceException("未找到订单的店铺" + dto.getShopId());
        }

        // 主表更新或保存
        SoB2cEntity mainEntity = soB2cService.saveOrUpdateEntity(dto);
        // 详情更新或保存
        List<SoB2cDetailEntity> detailList = soB2cDetailService.saveOrUpdateEntity(dto, mainEntity, listingInfoWithSkuMappingDTOMap, shopInfo);
        //物流信息更新保存
        soB2cLogisticsService.saveOrUpdateEntity(dto, mainEntity);
        //买家信息更新保存
        SoB2cReceiverEntity receiverEntity = soB2cReceiverService.saveOrUpdateEntity(dto, mainEntity);
        //财务信息更新保存
        soB2cFinanceService.saveOrUpdateEntity(dto, mainEntity);

        //客户信息
        CustomerB2cEntity customerB2cEntity = customerB2cService.saveOrUpdateEntity(dto, mainEntity, receiverEntity);

        customerB2cAddressService.saveOrUpdateEntity(dto, customerB2cEntity, receiverEntity);

        customerB2cContactService.saveOrUpdateEntity(dto, customerB2cEntity);

        customerB2cSellerService.saveOrUpdateEntity(dto, customerB2cEntity);

        receiverEntity.setCustomerId(customerB2cEntity.getId());
        if (!soB2cReceiverService.updateById(receiverEntity)) {
            throw new ServiceException("记录客户ID失败");
        }

        //自动匹配订单规则
        if (SoB2cBillStatusEnum.ENUM_WAIT_DISTRIBUTION.getCode().equalsIgnoreCase(mainEntity.getBillStatus())) {
//            JSONObject jsonObject = new JSONObject();
//            Boolean isSuccess = soB2cService.approveRule(mainEntity.getId(), detailList, jsonObject);
//            if (isSuccess) {
//                //自动匹配配货规则
//                soB2cService.distributionRule(mainEntity.getId(), detailList, jsonObject);
//            }
        }
        return ApiResult.success();
    }
}
