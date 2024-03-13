package com.erp.server.wms.sdk.retry;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.common.business.annotation.PlatformRetryAnno;
import com.common.business.dto.*;
import com.common.business.enums.BusinessTypeEnum;
import com.common.business.enums.PlatformCategoryEnum;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.enums.SyncStatusEnum;
import com.common.core.controller.vo.ApiResult;
import com.common.core.exception.ServiceException;
import com.common.core.utils.MapUtil;
import com.common.core.utils.date.DateUtil;
import com.common.message.constant.RocketMqTopic;
import com.erp.model.dmp.dto.DmpPullSoOutStockDTO;
import com.erp.model.dmp.entity.DmpPullTaskEntity;
import com.erp.model.oms.dto.SoB2cErrorDTO;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.oms.enums.SoB2cErrorTypeEnum;
import com.erp.rpc.dmp.feign.DmpAmazonFeign;
import com.erp.rpc.dmp.feign.DmpMongoDbFeign;
import com.erp.rpc.oms.feign.SoB2cFeign;
import com.erp.sdk.oms.amz.spapi.convert.SdkFbaShipmentConverter;
import com.erp.sdk.oms.amz.spapi.dto.PlatformAmazonFbaShipmentDTO;
import com.erp.sdk.oms.amz.spapi.dto.PlatformAmazonFulfilledShipmentsDTO;
import com.erp.sdk.oms.amz.spapi.model.fulfillmentinbound.InboundShipmentItemList;
import com.erp.server.wms.rocketmq.consumer.PlatformSoOutStockConsumerService;
import com.erp.server.wms.service.IPlatformRetryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@PlatformRetryAnno(method = PlatformDictEnum.AMAZON)
public class AmazonPlatformRetry implements IPlatformRetryService {

    @Resource
    private SoB2cFeign soB2cFeign;
    @Resource
    private DmpAmazonFeign dmpMongoDbFeign;


    @Override
    public Boolean retrySoOutStock(SoB2cEntity currentEntity, List list) {
        Boolean result = dmpMongoDbFeign.checkAndSendSoOutStock(new DmpPullSoOutStockDTO(currentEntity.getShopId(), currentEntity.getPlatformCode()));
        if (result){
            String type = SoB2cErrorTypeEnum.GENERATE_OUTSTOCK.getCode();
            SoB2cErrorDTO.DeleteDTO deleteDTO = new SoB2cErrorDTO.DeleteDTO();
            deleteDTO.setMainId(currentEntity.getId());
            deleteDTO.setType(type);
            soB2cFeign.deleteError(deleteDTO);
        }
        return result;
    }
}
