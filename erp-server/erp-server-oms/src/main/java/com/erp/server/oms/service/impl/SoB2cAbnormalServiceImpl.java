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
import com.erp.model.oms.dto.RuleLogisticsDTO;
import com.erp.model.oms.dto.SoB2cAbnormalDTO;
import com.erp.model.oms.dto.SoB2cErrorDTO;
import com.erp.model.oms.entity.SoB2cDetailEntity;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.oms.entity.SoB2cLogisticsEntity;
import com.erp.model.oms.enums.SoB2cBillStatusEnum;
import com.erp.model.oms.enums.SoB2cErrorTypeEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.tms.dto.LogisticsMappingDTO;
import com.erp.model.tms.enums.LogisticsMappingTypeEnum;
import com.erp.rpc.tms.feign.LogisticsMappingFeign;
import com.erp.rpc.wms.feign.SoB2cDeliveryFeign;
import com.erp.rpc.wms.feign.SoOutstockFeign;
import com.erp.server.oms.service.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
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
    private SoB2cDetailService soB2cDetailService;
    @Resource
    private SoB2cLogisticsService soB2cLogisticsService;
    @Resource
    private SoOutstockFeign soOutstockFeign;

    @Resource
    private SoB2cErrorService soB2cErrorService;

    @Resource
    private SoB2cDeliveryFeign soB2cDeliveryFeign;

    @Resource
    private OperateLogService operateLogService;

    @Resource
    private LogisticsMappingFeign logisticsMappingFeign;
    @Resource
    private RuleLogisticsService ruleLogisticsService;


    @Override
    public PagingVO<SoB2cAbnormalDTO.ListDTO> abnormalPaging(PagingDTO<SoB2cAbnormalDTO.PagingParamDTO> pagingParamDTO) {
        return soB2cService.abnormalPaging(pagingParamDTO);
    }

    @Override
    public Boolean abnormalExportExcel(SoB2cAbnormalDTO.PagingParamDTO dto) {
        return soB2cService.abnormalExportExcel(dto);
    }

    @Override
    public List<BatchResultDTO> batchRetry(String id,String type) {
        //返回信息
        List<BatchResultDTO> resultDTOList =  new ArrayList<>();
        //销售订单
        SoB2cEntity soB2cEntity = soB2cService.getById(id);
        if (ObjUtil.isEmpty(soB2cEntity)) {
            throw new ServiceException(ApiError.SO_B2C_NOT_FOUND);
        }
        SoB2cErrorTypeEnum soB2cErrorTypeEnum = SoB2cErrorTypeEnum.getEnum(soB2cEntity.getSignOrderError());
        if(SoB2cErrorTypeEnum.GET_LOGISTICS_LABEL.getCode().equals(type)){
            soB2cErrorTypeEnum = SoB2cErrorTypeEnum.GET_LOGISTICS_LABEL;
        }
        if(Objects.isNull(soB2cErrorTypeEnum)){
            return resultDTOList;
        }
        Boolean autoSubmitDelivery;
        // 重试逻辑
        switch (soB2cErrorTypeEnum) {
            case SUBMIT_DELIVERY:
                // 查询配置的海外仓物流
                SoB2cLogisticsEntity soB2cLogisticsEntity = soB2cLogisticsService.getByMainId(id);
                String channelId = soB2cLogisticsEntity.getLogisticsChannelId();
                List<SoB2cDetailEntity> soB2cDetailEntityList = soB2cDetailService.listByMainId(id);
                List<LogisticsMappingDTO.ViewDTO> viewDTOS = logisticsMappingFeign.listByChannelIdAndType(channelId, LogisticsMappingTypeEnum.WAREHOUSE.getCode());
                LogisticsMappingDTO.ViewDTO viewDTO = viewDTOS.stream().filter(v->v.getWarehouseId().equals(soB2cDetailEntityList.get(0).getWarehouseId())).findFirst().orElse(null);
                String warehouseLogisticsChannelId = Objects.nonNull(viewDTO)?viewDTO.getPlatformLogisticsChannelId():"";
                resultDTOList.add(soB2cService.submitDelivery(id, warehouseLogisticsChannelId));
                break;
            case SIGN_DELIVERY:
                resultDTOList.add(soB2cErrorService.retryFalseDelivery(id));
                break;
            case GET_LOGISTICS_CODE:
                autoSubmitDelivery = getLogisticsRuleResult(id);
                resultDTOList.add(soB2cService.getLogisticsCode(id, autoSubmitDelivery));
                break;
            case PACKAGE_PLAN_GENERATE:
                resultDTOList.add(soB2cService.retryPackagePlan(id));
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
            case GET_LOGISTICS_LABEL:
                SoB2cLogisticsEntity soB2cLogistics = soB2cLogisticsService.getByMainId(id);
                soB2cEntity.setBillStatus(SoB2cBillStatusEnum.ENUM_IN_DISTRIBUTION.getCode());
                BatchResultDTO resultDTO1 = soB2cService.getLogisticsLabel(soB2cEntity,soB2cLogistics);
                if(resultDTO1.getSuccess()){
                    autoSubmitDelivery = getLogisticsRuleResult(id);
                    if(autoSubmitDelivery){
                        try {
                            //提交发货
                            soB2cService.submitDelivery(id, "");
                        }catch (Exception e){
                            SoB2cErrorDTO.AddDTO addError = new SoB2cErrorDTO.AddDTO();
                            addError.setType(SoB2cErrorTypeEnum.SUBMIT_DELIVERY.getCode());
                            addError.setParamJson("");
                            addError.setReturnJson("");
                            addError.setMainId(id);
                            addError.setMessage(e.getMessage());
                            soB2cErrorService.add(addError);
                        }
                    }
                }
                resultDTOList.add(resultDTO1);
                break;
            default:
                break;
        }
        return resultDTOList;
    }

    private Boolean getLogisticsRuleResult(String id) {
        Map<String, Object> ruleMap = new HashMap<>();
        List<SoB2cDetailEntity> detailList = soB2cDetailService.listByMainId(id);
        ruleMap = soB2cService.handleMatchJson(id, detailList, ruleMap);
        List<Map<String, Object>> mapList = (List<Map<String, Object>>) ruleMap.get("detailList");
        //要匹配渠道id 是空的 如果有就 不用匹配了返回成功
        String logisticsChannelIdKey="logisticsChannelId";
        mapList.forEach(v->v.remove(logisticsChannelIdKey));
        if(id.equals("2001631240814084098")){
            System.out.println(123);
        }
        RuleLogisticsDTO.RuleMatchResultDTO matchResult = ruleLogisticsService.getRuleOrderMatchResult(ruleMap);
        if(Objects.isNull(matchResult) || Objects.isNull(matchResult.getLogisticsSupplierId())){
            return false;
        }
        return  matchResult.getAutoGetTrackNotOfRangeDelivery() || matchResult.getAutoGetTrackNo();
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
