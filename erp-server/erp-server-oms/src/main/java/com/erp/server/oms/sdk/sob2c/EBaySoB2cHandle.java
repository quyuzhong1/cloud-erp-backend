package com.erp.server.oms.sdk.sob2c;

import com.alibaba.fastjson.JSON;
import com.common.business.annotation.PlatformSoB2cAnnotate;
import com.common.business.dto.PlatformOrderDTO;
import com.common.business.enums.BusinessTypeEnum;
import com.common.business.enums.PlatformDictEnum;
import com.erp.model.dmp.dto.DmpInoutDTO;
import com.erp.model.oms.dto.SoB2cDTO;
import com.erp.model.oms.dto.SoB2cErrorDTO;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.oms.enums.SoB2cBillStatusEnum;
import com.erp.model.oms.enums.SoB2cErrorTypeEnum;
import com.erp.model.wms.dto.SoOutstockDTO;
import com.erp.rpc.wms.feign.SoOutstockFeign;
import com.erp.server.oms.service.PlatformOrderConsumerHandleService;
import com.erp.server.oms.service.SoB2cCoreService;
import com.erp.server.oms.service.SoB2cErrorService;
import com.erp.server.oms.service.SoB2cService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * EBayB2C订单处理
 *
 * @Author Jim
 * @Date 2024/03/21
 **/
@Slf4j
@Component
@PlatformSoB2cAnnotate(method = PlatformDictEnum.EBAY)
public class EBaySoB2cHandle extends AbstractSoB2cHandle  {

    public static final String HISTORY = "history";
    @Resource
    private PlatformOrderConsumerHandleService platformOrderConsumerHandleService;
    @Resource
    private SoOutstockFeign soOutstockFeign;
    @Resource
    private SoB2cErrorService soB2cErrorService;
    @Resource
    private SoB2cService soB2cService;

    @Resource
    private SoB2cCoreService soB2cCoreService;
    @Override
    public Boolean handleRule(SoB2cEntity mainEntity) {
        //平台仓订单不走任何规则
        if (mainEntity.hasPlatformWarehouseOrder()) {
            return false;
        }
        try {
            platformOrderConsumerHandleService.handleRule(mainEntity);
        } catch (Exception e) {
            log.error("[EBay订单规则处理失败]:order={},msg={}", mainEntity.getPlatformCode(), e.getMessage());
        }
        return true;
    }

    @Override
    public Boolean handleSoOutStock(PlatformOrderDTO dto, SoB2cDTO.PullOrderResultDTO resultDTO, SoB2cEntity mainEntity) {
        //平台仓订单
        Boolean hasPlatformWarehouse = mainEntity.hasPlatformWarehouseOrder();
        //已发货
        String shipped = SoB2cBillStatusEnum.ENUM_SHIPPED.getCode();
        String billStatus = mainEntity.getBillStatus();
        boolean isShipped = shipped.equals(billStatus);
        //如果是已发货且是平台仓订单 就生成销售出库单
        if (isShipped && hasPlatformWarehouse) {
            try {
                SoOutstockDTO.GenerateB2cDTO generateB2cDTO = soB2cService.getSoOutstockInfoById(mainEntity.getId());

                //平台仓拆分
                List<SoOutstockDTO.GenerateB2cDTO> generateB2cList = soB2cCoreService.splitB2cSoOutstock(mainEntity,generateB2cDTO);
                boolean result = true;

                for (SoOutstockDTO.GenerateB2cDTO b2cDTO : generateB2cList) {
                    if(!soOutstockFeign.generateB2cSoOutstockByData(b2cDTO)){
                        result = false;
                    }
                }
                return result;
            } catch (Exception e) {
                log.error("[EBay生成销售出库单异常]:order={},msg={}", mainEntity.getCode(), e.getMessage());
                SoB2cErrorDTO.AddDTO addError = new SoB2cErrorDTO.AddDTO();
                addError.setType(SoB2cErrorTypeEnum.GENERATE_OUTSTOCK.getCode());
                addError.setParamJson("");
                addError.setReturnJson("");
                addError.setMainId(mainEntity.getId());
                addError.setMessage(e.getMessage());
                soB2cErrorService.add(addError);
            }
        }
        return true;
    }

    @Override
    public List<DmpInoutDTO.CreateInputDTO> convertCreateInputDTOList(List sourceList) {
        List<String> orderStatusList = Arrays.asList("4","9","6","5","7");
        List<SoB2cEntity> soB2cEntityList = (List<SoB2cEntity>) sourceList;
        List<DmpInoutDTO.CreateInputDTO> createInputDTOS = new ArrayList<>();
        for (SoB2cEntity soB2cEntity : soB2cEntityList) {
            for (String orderStatus : orderStatusList) {
                DmpInoutDTO.CreateInputDTO dto = createAliExpressInputDTO(soB2cEntity,orderStatus);
                createInputDTOS.add(dto);
            }
        }
        return createInputDTOS;
    }

    private DmpInoutDTO.CreateInputDTO createAliExpressInputDTO(SoB2cEntity e,String orderStatus) {
        DmpInoutDTO.CreateInputDTO dto = new DmpInoutDTO.CreateInputDTO();
        dto.setNextLevelId(orderStatus);
        dto.setSystemCode(PlatformDictEnum.LING_XING.getCode());
        dto.setBillType(BusinessTypeEnum.ORDER.getCode());
        dto.setTaskType(HISTORY);

        LocalDateTime startTime = e.getPlatformOrderCreateTime().minusSeconds(1);
        LocalDateTime endTime = e.getPlatformOrderCreateTime().plusSeconds(1);
        dto.setStartTime(startTime);
        dto.setEndTime(endTime);
        Map<String,Object> map = new HashMap<>();
        map.put("length", 200);
        map.put("date_type", "global_purchase_time");
        map.put("order_status", Integer.valueOf(orderStatus));
        dto.setDetailExtendJson(JSON.toJSONString(map));
        return dto;
    }
}
