package com.erp.server.oms.sdk.sob2c;

import com.alibaba.fastjson.JSON;
import com.common.business.annotation.PlatformSoB2cAnnotate;
import com.common.business.dto.PlatformOrderDTO;
import com.common.business.enums.BusinessTypeEnum;
import com.common.business.enums.PlatformDictEnum;
import com.common.core.entity.BaseEntity;
import com.erp.model.dmp.dto.DmpInoutDTO;
import com.erp.model.dmp.dto.DmpPullSoOutStockDTO;
import com.erp.model.oms.dto.ShopInfoDTO;
import com.erp.model.oms.dto.SoB2cDTO;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.oms.enums.AuthStatusEnum;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.rpc.dmp.feign.DmpInoutTaskFeign;
import com.erp.rpc.dmp.feign.DmpMongoDbFeign;
import com.erp.server.oms.service.ISoB2cHandleService;
import com.erp.server.oms.service.PlatformOrderConsumerHandleService;
import com.erp.server.oms.service.ShopInfoService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.validation.constraints.NotBlank;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 亚马逊B2C订单处理
 *
 * @Author Jim
 * @Date 2024/03/21
 **/
@Slf4j
@Component
@PlatformSoB2cAnnotate(method = PlatformDictEnum.AMAZON)
public class AmazonSoB2cHandle extends AbstractSoB2cHandle  {

    @Resource
    private PlatformOrderConsumerHandleService platformOrderConsumerHandleService;
    @Resource
    private DmpMongoDbFeign dmpMongoDbFeign;
    @Resource
    private ShopInfoService shopInfoService;
    @Resource
    private DmpInoutTaskFeign dmpInoutTaskFeign;


    @Override
    public Boolean handleRule(SoB2cEntity mainEntity) {
        //平台仓订单不走任何规则
        if (Boolean.TRUE.equals(mainEntity.hasPlatformWarehouseOrder())) {
            return false;
        }
        try {
            platformOrderConsumerHandleService.handleRule(mainEntity);
        } catch (Exception e) {
            log.error("[亚马逊订单规则处理失败]:order={},msg={}", mainEntity.getPlatformCode(), e.getMessage());
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean handleSoOutStock(PlatformOrderDTO dto, SoB2cDTO.PullOrderResultDTO resultDTO, SoB2cEntity mainEntity) {
        // 新的亚马逊FBA订单检查历史配送记录
        if (resultDTO.isNewInsertOrder() && Boolean.TRUE.equals(mainEntity.hasPlatformWarehouseOrder())) {
            try {
                Boolean result = dmpMongoDbFeign.checkSoOutStock(new DmpPullSoOutStockDTO(mainEntity.getShopId(), mainEntity.getPlatformCode(), mainEntity.getId()));
                if (Boolean.FALSE.equals(result)){
                    log.warn("处理检查历史销售出库记录失败:platformOrderId={}", dto.getPlatformCode());
                }
            } catch (Exception e) {
                log.warn("检查历史销售出库记录失败:platformOrderId={}", dto.getPlatformCode());
            }
        }
        return true;
    }

    /**
     * 转换新中台刷新订单请求参数
     */
    @Override
    public List<DmpInoutDTO.CreateInputDTO> convertCreateInputDTOList(List sourceList){
        List<SoB2cEntity> orderEntityList = (List<SoB2cEntity>) sourceList;
        ShopInfoDTO.ListParamDTO paramDTO = new ShopInfoDTO.ListParamDTO(AuthStatusEnum.ALREADY.getCode(), PlatformDictEnum.AMAZON.getCode(), null);
        List<ShopInfoEntity> allAmzshopList = shopInfoService.listByParams(paramDTO);

        List<DmpInoutDTO.CommonDTO> commonDTOList = allAmzshopList.stream()
                .map(e -> new DmpInoutDTO.CommonDTO(PlatformDictEnum.AMAZON.getCode(), BusinessTypeEnum.ORDER.getCode(), e.getId()))
                .collect(Collectors.toList());
        // 查询任务记录
        List<DmpInoutDTO.ListDTO> dmpDetailTaskList = dmpInoutTaskFeign.inputDetailList(commonDTOList);

        // 亚马逊按账号分组
        Map<String, List<SoB2cEntity>> shopGroupMap = orderEntityList.stream()
                .collect(Collectors.groupingBy(e -> matchDetailTaskShopId(e, dmpDetailTaskList, allAmzshopList)));

        return shopGroupMap.entrySet().stream()
                .map(e -> createAmazonInputDTO(e.getKey(), e.getValue()))
                .collect(Collectors.toList());

    }

    /**
     * 匹配对应账号任务明细ShopId， 默认单号当前shopId
     */
    private String matchDetailTaskShopId(SoB2cEntity entity, List<DmpInoutDTO.ListDTO> dmpDetailTaskList, List<ShopInfoEntity> allAmzshopList) {
        if (CollectionUtils.isEmpty(allAmzshopList) || CollectionUtils.isEmpty(dmpDetailTaskList)){
            return entity.getShopId();
        }
        ShopInfoEntity curEntity = allAmzshopList.stream()
                .filter(e -> e.getId().equalsIgnoreCase(entity.getShopId()))
                .findFirst()
                .orElse(null);
        if (null == curEntity){
            return entity.getShopId();
        }
        List<String> sameAccountShopIds = allAmzshopList.stream()
                .filter(e -> e.getPlatformShopCode().equalsIgnoreCase(curEntity.getPlatformShopCode()))
                .map(BaseEntity::getId)
                .collect(Collectors.toList());
        String nextLevelId = dmpDetailTaskList
                .stream()
                .map(DmpInoutDTO.ListDTO::getNextLevelId)
                .filter(sameAccountShopIds::contains)
                .findFirst()
                .orElse(null);
        if (StringUtils.isNotBlank(nextLevelId)){
            return nextLevelId;
        }
        return entity.getShopId();
    }


    /**
     * 转换亚马逊订单拉取任务
     */
    private DmpInoutDTO.CreateInputDTO createAmazonInputDTO(String nextLevelId, List<SoB2cEntity> soB2cList) {
        DmpInoutDTO.CreateInputDTO dto = new DmpInoutDTO.CreateInputDTO();
        dto.setNextLevelId(nextLevelId);
        dto.setSystemCode(PlatformDictEnum.AMAZON.getCode());
        dto.setBillType(BusinessTypeEnum.ORDER.getCode());
        //  DmpInputTaskTaskTypeEnum	NORMAL("normal", "正常任务"),
        dto.setTaskType(NORMAL);

        // 构建 orderIdList 并封装为 JSON
        Map<String, List<String>> map = Collections.singletonMap(
                ORDER_ID_LIST,
                soB2cList.stream()
                        .map(SoB2cEntity::getPlatformCode)
                        .collect(Collectors.toList())
        );
        dto.setDetailExtendJson(JSON.toJSONString(map));
        return dto;
    }
}
