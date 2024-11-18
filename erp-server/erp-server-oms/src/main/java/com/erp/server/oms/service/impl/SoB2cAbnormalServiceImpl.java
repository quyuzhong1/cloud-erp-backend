package com.erp.server.oms.service.impl;

import cn.hutool.core.util.ObjUtil;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.oms.dto.SoB2cAbnormalDTO;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.oms.enums.SoB2cErrorTypeEnum;
import com.erp.rpc.dmp.feign.DmpInoutTaskFeign;
import com.erp.rpc.wms.feign.SoB2cDeliveryFeign;
import com.erp.rpc.wms.feign.SoOutstockFeign;
import com.erp.server.oms.service.SoB2cAbnormalService;
import com.erp.server.oms.service.SoB2cErrorService;
import com.erp.server.oms.service.SoB2cService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @description: b2c异常订单实现
 * @author Will
 * @date: 2024/4/22 9:06
 */
@Service
public class SoB2cAbnormalServiceImpl implements SoB2cAbnormalService {

    @Resource
    private SoB2cService soB2cService;

    @Resource
    private SoOutstockFeign soOutstockFeign;

    @Resource
    private SoB2cErrorService soB2cErrorService;

    @Resource
    private SoB2cDeliveryFeign soB2cDeliveryFeign;

    @Resource
    private DmpInoutTaskFeign dmpInoutTaskFeign;


    @Override
    public PagingVO<SoB2cAbnormalDTO.ListDTO> abnormalPaging(PagingDTO<SoB2cAbnormalDTO.PagingParamDTO> pagingParamDTO) {
        return soB2cService.abnormalPaging(pagingParamDTO);
    }

    @Override
    public Boolean abnormalExportExcel(SoB2cAbnormalDTO.PagingParamDTO dto) {
        return soB2cService.abnormalExportExcel(dto);
    }

    @Override
    public List<BatchResultDTO> batchRetry(String id) {
        //返回信息
        List<BatchResultDTO> resultDTOList =  new ArrayList<>();
        //销售订单
        SoB2cEntity soB2cEntity = soB2cService.getById(id);
        if (ObjUtil.isEmpty(soB2cEntity)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_NOT_EXIST);
        }
        SoB2cErrorTypeEnum soB2cErrorTypeEnum = SoB2cErrorTypeEnum.getEnum(soB2cEntity.getSignOrderError());
        // 重试逻辑
        switch (soB2cErrorTypeEnum) {
            case SUBMIT_DELIVERY:
                resultDTOList.add(soB2cService.submitDelivery(id));
                break;
            case SIGN_DELIVERY:
                resultDTOList.add(soB2cErrorService.retryFalseDelivery(id));
                break;
            case GET_LOGISTICS_CODE:
                resultDTOList.add(soB2cService.getLogisticsCode(id, Boolean.TRUE));
                break;
            case GENERATE_OUTSTOCK:
                soOutstockFeign.afreshGenerateB2cOutstock(Arrays.asList(id));
                break;
            case INTERCEPT_SUCCESS:
                resultDTOList.add(BatchResultDTO.fail(soB2cEntity.getId(), soB2cEntity.getCode(), "拦截成功，无需重试"));
                break;
            case ORDER_FORECAST:
                resultDTOList.addAll(soB2cService.retryOrderForecast(Arrays.asList(id)));
                break;
            case INSTOCK_FORECAST:
                resultDTOList.add(BatchResultDTO.fail(soB2cEntity.getId(), soB2cEntity.getCode(), "入库预报无需重试"));
                break;
            case GENERATE_TRANSFER_INFO:
                Boolean isPush = soB2cDeliveryFeign.afreshPushTransferInfo(soB2cEntity.getId());
                BatchResultDTO resultDTO = isPush ? BatchResultDTO.success(id, soB2cEntity.getCode(), "生成直接调拨单") : BatchResultDTO.fail(id, soB2cEntity.getCode(), "生成直接调拨单");
                resultDTOList.add(resultDTO);
                break;
            case VIRTUAL_FREEZE_QTY:
                Boolean outFreeze = soB2cDeliveryFeign.afreshOutFreezeVirtualInventory(soB2cEntity.getId());
                BatchResultDTO batchResultDTO = outFreeze ? BatchResultDTO.success(id, soB2cEntity.getCode(), "虚拟仓库存扣减") : BatchResultDTO.fail(id, soB2cEntity.getCode(), "虚拟仓库存扣减");
                resultDTOList.add(batchResultDTO);
                break;
            case ORDER_FETCH:
                boolean orderFetchFlag = soB2cService.fetchOrder(Collections.singletonList(id));
                BatchResultDTO orderFetchResultDTO = orderFetchFlag ? BatchResultDTO.success(id, soB2cEntity.getCode(), "订单拉取成功") : BatchResultDTO.fail(id, soB2cEntity.getCode(), "订单拉取失败");
                resultDTOList.add(orderFetchResultDTO);
                break;
            default:
                break;
        }
        return resultDTOList;
    }

}
