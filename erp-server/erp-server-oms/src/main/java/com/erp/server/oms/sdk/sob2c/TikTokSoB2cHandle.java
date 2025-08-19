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
import com.erp.server.oms.service.ISoB2cHandleService;
import com.erp.server.oms.service.PlatformOrderConsumerHandleService;
import com.erp.server.oms.service.SoB2cErrorService;
import com.erp.server.oms.service.SoB2cService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@PlatformSoB2cAnnotate(method = PlatformDictEnum.TIK_TOK)
public class TikTokSoB2cHandle extends AbstractSoB2cHandle  {

    @Resource
    private PlatformOrderConsumerHandleService platformOrderConsumerHandleService;
    @Resource
    private SoOutstockFeign soOutstockFeign;
    @Resource
    private SoB2cErrorService soB2cErrorService;
    @Resource
    private SoB2cService soB2cService;

    @Override
    public Boolean handleRule(SoB2cEntity mainEntity) {
        //平台仓订单不走任何规则
        if (mainEntity.hasPlatformWarehouseOrder()) {
            return false;
        }
        try {
            platformOrderConsumerHandleService.handleRule(mainEntity);
        } catch (Exception e) {
            log.error("[TikTok订单规则处理失败]:order={},msg={}", mainEntity.getPlatformCode(), e.getMessage());
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
                return soOutstockFeign.generateB2cSoOutstockByData(generateB2cDTO);

            } catch (Exception e) {
                log.error("[TikTok生成销售出库单异常]:order={},msg={}", mainEntity.getCode(), e.getMessage());
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

    /**
     * 转换新中台刷新订单请求参数
     */
    @Override
    public List<DmpInoutDTO.CreateInputDTO> convertCreateInputDTOList(List sourceList) {
        Map<String, List<SoB2cEntity>> shopGroupMap = ((List<SoB2cEntity>) sourceList).stream().collect(Collectors.groupingBy(SoB2cEntity::getShopId));
        return shopGroupMap.values().stream()
                .map(this::createInputDTO)
                .collect(Collectors.toList());
    }
}
