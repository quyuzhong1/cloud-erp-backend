package com.erp.server.oms.sdk.sob2c;

import com.common.business.annotation.PlatformSoB2cAnnotate;
import com.common.business.dto.PlatformOrderDTO;
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
import java.util.stream.Collectors;

/**
 * 美客多-全球
 */
@Slf4j
@Component
@PlatformSoB2cAnnotate(method = PlatformDictEnum.MERCADOLIBRE)
public class MercadoSoB2cHandle  extends AbstractSoB2cHandle {

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
            log.error("[美客多订单规则处理失败]:order={},msg={}", mainEntity.getPlatformCode(), e.getMessage());
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
                log.error("[美客多生成销售出库单异常]:order={},", mainEntity.getCode(), e);
                SoB2cErrorDTO.AddDTO addError = new SoB2cErrorDTO.AddDTO();
                addError.setType(SoB2cErrorTypeEnum.GENERATE_OUTSTOCK.getCode());
                addError.setParamJson("");
                addError.setReturnJson("");
                addError.setMainId(mainEntity.getId());
                addError.setMessage(e.getMessage());
                soB2cErrorService.add(addError);
                return false;
            }
        }
        return true;
    }

    @Override
    public List<DmpInoutDTO.CreateInputDTO> convertCreateInputDTOList(List sourceList) {
        Map<String, List<SoB2cEntity>> shopGroupMap = ((List<SoB2cEntity>) sourceList).stream().collect(Collectors.groupingBy(SoB2cEntity::getShopId));
        return shopGroupMap.values().stream()
                .map(this::createInputDTO)
                .collect(Collectors.toList());
    }
}
