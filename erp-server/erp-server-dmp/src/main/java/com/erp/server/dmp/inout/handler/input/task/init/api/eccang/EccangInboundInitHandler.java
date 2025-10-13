package com.erp.server.dmp.inout.handler.input.task.init.api.eccang;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.common.business.enums.OmsPlatformEnum;
import com.common.business.enums.OverseasInstockStatusEnum;
import com.common.business.threadlocal.ThirdWarehouseContext;
import com.common.core.exception.ServiceException;
import com.common.core.utils.MathUtil;
import com.erp.model.dmp.entity.DmpCfgApiEntity;
import com.erp.model.wms.entity.OverseasProviderEntity;
import com.erp.rpc.wms.feign.WmsOverseasWarehouseFeign;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputInitRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputTaskResponse;
import com.erp.server.dmp.inout.handler.input.task.init.DmpInputInitHandler;
import com.erp.server.dmp.inout.utils.DmpHandlerCache;
import com.sdk.wms.antu.dto.request.AntuGetReceiptReq;
import com.sdk.wms.antu.dto.response.AntuReceiptResp;
import com.sdk.wms.antu.dto.response.AntuResponse;
import com.sdk.wms.antu.utils.AntuUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * dmp输入init任务基础处理器下的速派通api获取数据方式
 *
 * @author Administrator
 */
@Slf4j
@Service
@Scope("prototype")
public abstract class EccangInboundInitHandler extends DmpInputInitHandler {


    @Resource
    private DmpHandlerCache dmpHandlerCache;

    @Resource
    private WmsOverseasWarehouseFeign overseasWarehouseFeign;

    @Override
    public List<DmpInputTaskInitDTO> getInitData(DmpInputInitRequest dmpRequest, DmpInputTaskResponse dmpResponse) {
        List<AntuReceiptResp> allResult = new ArrayList<>();

        String typeId = dmpCfgInputEntity.getTypeId();
        DmpCfgApiEntity dmpCfgApiEntity = dmpCfgApiService.getById(typeId);
        String apiType = dmpCfgApiEntity.getApiType();

        List<OverseasProviderEntity> overseasProviderEntityList = dmpHandlerCache.getOverseasProviderEntityList(d -> d.getCode().equals(getPlatForm().getCode()));
        if (CollUtil.isEmpty(overseasProviderEntityList)) {
            return Collections.emptyList();
        }
        // 取对应授权ID授权
        OverseasProviderEntity overseasProviderEntity = overseasProviderEntityList.stream()
                .filter(e -> e.getId().equalsIgnoreCase(dmpInputTaskEntity.getNextLevelId()))
                .findFirst()
                .orElse(null);
        if (null == overseasProviderEntity) {
            throw new ServiceException(getPlatForm().getName() + "对应授权ID信息不存在");
        }
        if (overseasProviderEntity.getEnableDate().isAfter(LocalDate.now())) {
            return Collections.emptyList();
        }
        //查询待签收、部分签收状态的入库单
        List<String> receiveCodeList = overseasWarehouseFeign.getReceiptNumbersForStatus(Arrays.asList(OverseasInstockStatusEnum.TO_BE_SIGNED.getCode()
                , OverseasInstockStatusEnum.PARTIAL_SIGNED.getCode()
                , OverseasInstockStatusEnum.SIGNED.getCode()), overseasProviderEntity.getId());

        if(CollUtil.isEmpty(receiveCodeList)){
            return Collections.emptyList();
        }
        Integer page = 1;
        ThirdWarehouseContext.setAuthMap(overseasProviderEntity.getAuthJson());

        //查询数据
        AntuGetReceiptReq antuGetReceiptReq = AntuGetReceiptReq.builder()
                .page(page)
                .pageSize(MathUtil.NUMBER_100)
                .receivingCodeArr(receiveCodeList)
                .build();
        String response = AntuUtils.callService(getPlatForm(), apiType, antuGetReceiptReq);
        log.info(getPlatForm().getName() + "（" + apiType + "）api接口返回数据：{}", response);
        AntuResponse<List<AntuReceiptResp>> result = JSONObject.parseObject(response, new TypeReference<AntuResponse<List<AntuReceiptResp>>>() {
        }.getType());

        allResult.addAll(result.getData());
        page++;

        DmpInputTaskInitDTO dmpInputTaskInitDTO = new DmpInputTaskInitDTO();
        dmpInputTaskInitDTO.setMsg(JSONObject.toJSONString(allResult));
        return Collections.singletonList(dmpInputTaskInitDTO);
    }

    public abstract OmsPlatformEnum getPlatForm();
}
