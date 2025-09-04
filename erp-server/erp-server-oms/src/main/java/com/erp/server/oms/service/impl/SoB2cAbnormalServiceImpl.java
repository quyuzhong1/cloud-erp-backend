package com.erp.server.oms.service.impl;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjUtil;
import com.alibaba.fastjson.JSON;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.oms.dto.OperateLogDTO;
import com.erp.model.oms.dto.SoB2cAbnormalDTO;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.oms.enums.SoB2cErrorTypeEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.rpc.wms.feign.SoB2cDeliveryFeign;
import com.erp.rpc.wms.feign.SoOutstockFeign;
import com.erp.server.oms.service.OperateLogService;
import com.erp.server.oms.service.SoB2cAbnormalService;
import com.erp.server.oms.service.SoB2cErrorService;
import com.erp.server.oms.service.SoB2cService;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @description: b2c异常订单实现
 * @author Will
 * @date: 2024/4/22 9:06
 */
@Service
public class SoB2cAbnormalServiceImpl implements SoB2cAbnormalService {

    private static final Logger log = LoggerFactory.getLogger(SoB2cAbnormalServiceImpl.class);
    @Resource
    private SoB2cService soB2cService;

    @Resource
    private SoOutstockFeign soOutstockFeign;

    @Resource
    private SoB2cErrorService soB2cErrorService;

    @Resource
    private SoB2cDeliveryFeign soB2cDeliveryFeign;

    @Resource
    private OperateLogService operateLogService;


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
        if(Objects.isNull(soB2cErrorTypeEnum)){
            return resultDTOList;
        }
        // 重试逻辑
        switch (soB2cErrorTypeEnum) {
            case SUBMIT_DELIVERY:
                resultDTOList.add(soB2cService.submitDelivery(id, ""));
                break;
            case SIGN_DELIVERY:
                resultDTOList.add(soB2cErrorService.retryFalseDelivery(id));
                break;
            case GET_LOGISTICS_CODE:
                resultDTOList.add(soB2cService.getLogisticsCode(id, Boolean.TRUE));
                break;
            case GENERATE_OUTSTOCK:
                Boolean flag = soOutstockFeign.afreshGenerateB2cOutstock(Arrays.asList(id));
                BatchResultDTO outStockResultDTO = flag ? BatchResultDTO.success(id, soB2cEntity.getCode(), "重试成功") : BatchResultDTO.fail(id, soB2cEntity.getCode(), "重试失败");
                resultDTOList.add(outStockResultDTO);
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
                List<BatchResultDTO> resultDTOS = soB2cService.fetchOrder(Collections.singletonList(id));
                resultDTOList.addAll(resultDTOS);
                break;
            default:
                break;
        }
        return resultDTOList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void clearAbnormal(SoB2cAbnormalDTO.ClearAbnormalDTO dto) {
        List<SoB2cEntity> soB2cEntityList = soB2cService.listByIds(dto.getIds());
        if(CollectionUtils.isEmpty(soB2cEntityList)){
            return;
        }
        List<String> codes = soB2cEntityList.stream().map(SoB2cEntity::getCode).collect(Collectors.toList());
        String msg = CharSequenceUtil.format("用户【{}】清除订单异常，备注【{}】", UserContext.getDefaultLoginUser().getUserName(),dto.getRemark());
        List<OperateLogDTO.AddModuleOperateLogDTO> operateLogList = new ArrayList<>();
        soB2cEntityList.forEach(v->{
            v.setSignOrderError("");
            OperateLogDTO.AddModuleOperateLogDTO addModuleOperateLogDTO = OperateLogDTO.AddModuleOperateLogDTO.builder()
                    .content(msg)
                    .businessId(v.getId())
                    .moduleType(ModuleTypeEnum.SO_B2C.getCode())
                    .operation("清除异常")
                    .build();
            operateLogList.add(addModuleOperateLogDTO);
        });
        soB2cService.updateBatchById(soB2cEntityList);
        //清除异常
        soB2cErrorService.deleteByMainIds(dto.getIds());

        operateLogService.batchAddModuleOperateLog(operateLogList);

        log.error("清除异常销售订单异常：{}", JSON.toJSONString(codes));
    }

}
