package com.erp.server.wms.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.common.business.enums.OmsPlatformEnum;
import com.common.business.utils.CollectionUtils;
import com.common.business.utils.RedisUtil;
import com.common.business.wrapper.FeignQuery;
import com.common.core.controller.vo.ApiResult;
import com.common.core.exception.ServiceException;
import com.erp.model.sys.entity.DictCityEntity;
import com.erp.model.wms.dto.third.*;
import com.erp.model.wms.enums.ThirdWarehouseCancelResultEnum;
import com.erp.rpc.sys.feign.SysDictFeign;
import com.erp.server.wms.convert.OverseasWarehouseInboundConverter;
import com.erp.server.wms.handler.AbstractThirdWarehouseHandler;
import com.sdk.wms.antu.service.AntuService;
import com.sdk.wms.antu.dto.request.AntuBaseRequest;
import com.sdk.wms.antu.dto.request.AntuCreateInboundReq;
import com.sdk.wms.antu.dto.request.AntuCreateOutboundReq;
import com.sdk.wms.antu.dto.response.AntuResponse;
import com.sdk.wms.antu.dto.response.AntuWarehouseResp;
import com.sdk.wms.goodcang.dto.response.GoodCangResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;
import java.util.Objects;

/**
 * @author liuruipeng
 * @date 2023年11月17日 10:51
 */
@Slf4j
@Service
@Validated
public class AntuHandlerServiceImpl extends AbstractThirdWarehouseHandler {

    @Resource
    private AntuService antuService;

    @Override
    public OmsPlatformEnum getPlatForm() {
        return OmsPlatformEnum.OMS_ANTU;
    }

    @Override
    protected ApiResult<List<ThirdWarehouseSkuResp>> getSkuList(ThirdWarehouseProductReq productReq) {
        return success();
    }

    @Override
    public ApiResult<String> createInboundBill(ThirdWarehouseCreateInboundReq createInboundReq) {
        AntuCreateInboundReq antuCreateInboundReq = OverseasWarehouseInboundConverter.INSTANCE.inboundDtoToAntu(createInboundReq);
        log.warn("安兔创建入库单json :{}", JSONUtil.toJsonStr(antuCreateInboundReq));
        AntuResponse<String> antuResponse = antuService.createInboundBill(antuCreateInboundReq);
        return isSuccess(antuResponse.getAsk()) ? success(antuResponse.getData()) : failure(antuResponse.getMessage());
    }

    @Override
    protected ApiResult<String> editInboundBill(ThirdWarehouseCreateInboundReq createInboundReq) {
        AntuCreateInboundReq antuCreateInboundReq = OverseasWarehouseInboundConverter.INSTANCE.inboundDtoToAntu(createInboundReq);
        log.warn("安兔编辑入库单json :{}", JSONUtil.toJsonStr(antuCreateInboundReq));
        // 修改入库单
        AntuResponse<String> antuResponse = antuService.editInboundBill(antuCreateInboundReq);
        return isSuccess(antuResponse.getAsk()) ? success(antuResponse.getData()) : failure(antuResponse.getMessage());
    }

    @Override
    public ApiResult<String> cancelInboundBill(@Valid ThirdWarehouseCancelInboundReq cancelInboundReq) {
        AntuResponse<String> response = antuService.cancelInboundBill(cancelInboundReq.getReceivingCode());
        return isSuccess(response.getAsk()) ? success(response.getData()) : failure(response.getMessage());
    }

    @Override
    public ApiResult<String> createOutboundBill(ThirdWarehouseCreateOutboundReq createOutboundReq) {
        AntuCreateOutboundReq antuCreateOutboundReq = OverseasWarehouseInboundConverter.INSTANCE.outboundDtoToAntu(createOutboundReq);
        this.handleData(antuCreateOutboundReq);
        log.warn("安兔创建出库单json :{}", JSONUtil.toJsonStr(antuCreateOutboundReq));
        AntuResponse<String> response =  antuService.createOutboundBill(antuCreateOutboundReq);
        if(response.getMessage().contains("参考编号已存在")){
            return success(response.getOrderCode());
        }
        return isSuccess(response.getAsk()) ? success(response.getData()) : failure(response.getMessage());
    }

    @Override
    public ApiResult<String> cancelOutboundBill(@Valid ThirdWarehouseCancelOutboundReq cancelOutboundReq) {
        AntuResponse<String> response = antuService.cancelOutboundBill(cancelOutboundReq.getOrderCode(),cancelOutboundReq.getReason());
        if(!isSuccess(response.getAsk())){
            return failure(response.getMessage());
        }
        if(Objects.isNull(response.getCancelStatus())){
            return failure(response.getMessage());
        }
        if(response.getCancelStatus().equals(1)){
            return success(ThirdWarehouseCancelResultEnum.INTERCEPTING.getCode());
        }
        if(response.getCancelStatus().equals(3)){
            return success(ThirdWarehouseCancelResultEnum.INTERCEPTION_FAILED.getCode());
        }
        return success(ThirdWarehouseCancelResultEnum.INTERCEPTION_SUCCESSFUL.getCode());
    }

    @Override
    protected Boolean hasWarehouse() {
        AntuResponse<List<AntuWarehouseResp>> response = antuService.getWarehouse(AntuBaseRequest.builder()
                        .pageSize(1)
                        .page(1)
                .build());
        if(!isSuccess(response.getAsk())){
            throw new ServiceException("授权失败,"+response.getMessage());
        }
        return isSuccess(response.getAsk());
    }


    private void handleData(AntuCreateOutboundReq antuCreateOutboundReq) {
        //处理地址1
        if(StringUtils.isBlank(antuCreateOutboundReq.getAddress1())){
            antuCreateOutboundReq.setAddress1(StringUtils.isNotBlank(antuCreateOutboundReq.getAddress2())?antuCreateOutboundReq.getAddress2():antuCreateOutboundReq.getAddress3());
        }
        //处理邮编
        if(StringUtils.isNotBlank(antuCreateOutboundReq.getZipcode())){
            antuCreateOutboundReq.setZipcode(antuCreateOutboundReq.getZipcode().replace("-",""));
        }
        //处理省份
        if(StringUtils.isNotBlank(antuCreateOutboundReq.getProvince())){
            if(antuCreateOutboundReq.getProvince().length() != 2){
                List<DictCityEntity> dictCityEntityList = FeignQuery.create(DictCityEntity.class)
                        .eq(DictCityEntity::getCountryCode,antuCreateOutboundReq.getCountryCode())
                        .eq(DictCityEntity::getType,"province")
                        .last(StrUtil.format("and (code_en = '{}'  or code_pt = '{}')",antuCreateOutboundReq.getProvince(),antuCreateOutboundReq.getProvince()))
                        .list();
                if(CollectionUtil.isEmpty(dictCityEntityList)){
                    throw new ServiceException("省份转换二字码失败");
                }
                antuCreateOutboundReq.setProvince(dictCityEntityList.get(0).getCodeTwo());
            }
        }
    }
    public boolean isSuccess(String ask){
        return "Success".equals(ask);
    }
}
