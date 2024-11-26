package com.erp.server.dmp.inout.handler.input.task.init.api.antu;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Resource;

import com.common.core.exception.ServiceException;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.common.business.threadlocal.ThirdWarehouseContext;
import com.erp.model.dmp.entity.DmpCfgApiEntity;
import com.erp.model.dmp.enums.DmpBasicSystemCodeEnum;
import com.erp.model.wms.entity.OverseasProviderEntity;
import com.erp.rpc.wms.feign.WmsOverseasWarehouseFeign;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputInitRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputTaskResponse;
import com.erp.server.dmp.inout.handler.input.task.init.DmpInputInitHandler;
import com.sdk.wms.antu.dto.request.AntuGetReturnReq;
import com.sdk.wms.antu.dto.response.AntuResponse;
import com.sdk.wms.antu.utils.AntuUtils;

import cn.hutool.core.collection.CollUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * dmp输入init任务基础处理器下的安兔api获取数据方式
 *
 * @author Administrator
 */
@Slf4j
@Service
@Scope("prototype")
public class AntuReturnInstockInitHandler extends DmpInputInitHandler {

    @Resource
    private WmsOverseasWarehouseFeign overseasWarehouseFeign;

    @Override
    public List<DmpInputTaskInitDTO> getInitData(DmpInputInitRequest dmpRequest, DmpInputTaskResponse dmpResponse) {
        String typeId = dmpCfgInputEntity.getTypeId();
        DmpCfgApiEntity dmpCfgApiEntity = dmpCfgApiService.getById(typeId);
        String apiType = dmpCfgApiEntity.getApiType();

        //查询数据
        AntuGetReturnReq returnReq = AntuGetReturnReq.builder()
                .modifyDateFrom(dmpInputTaskEntity.getStartTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .modifyDateTo(dmpInputTaskEntity.getEndTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .pageSize(100)
                .build();
        int page = 1;
        int currTotal = 0;
        List<JSONObject> allResult = new ArrayList<>();
        List<OverseasProviderEntity> overseasProviderEntityList = dmpHandlerCache.getOverseasProviderEntityList(d -> d.getCode().equals(DmpBasicSystemCodeEnum.ANTU.getCode()));
        if(CollUtil.isEmpty(overseasProviderEntityList)) {
            return Collections.emptyList();
        }
        // 取对应授权ID授权
        OverseasProviderEntity overseasProviderEntity = overseasProviderEntityList.stream()
                .filter(e -> e.getId().equalsIgnoreCase(dmpInputTaskEntity.getNextLevelId()))
                .findFirst()
                .orElse(null);
        if(null == overseasProviderEntity) {
            throw new ServiceException("安兔对应授权ID信息不存在");
        }
        if (overseasProviderEntity.getEnableDate().isAfter(LocalDate.now())) {
            return Collections.emptyList();
        }

        String authId = overseasProviderEntity.getId();
        ThirdWarehouseContext.setAuthMap(overseasProviderEntity.getAuthJson());
        while(true) {
            returnReq.setPage(page);
            log.debug("安兔退货信息请求:{}", returnReq);
            String response = AntuUtils.callService(apiType,returnReq);
            log.debug("安兔退货信息响应:{}", response);
            AntuResponse<List<JSONObject>> result = JSONObject.parseObject(response,new TypeReference<AntuResponse<List<JSONObject>>>() {}.getType());
            List<JSONObject> data = result.getData();
            int size = data.size();
            if(size == 0) {
                break;
            }
            data.forEach(e->e.put("authId", authId));
            allResult.addAll(data);
            currTotal = currTotal + size;
            Integer count = result.getCount();
            if(count == null) {
                count  = 0;
            }
            if(currTotal >= count) {
                break;
            }
            page = page + 1;
        }
        DmpInputTaskInitDTO dmpInputTaskInitDTO = new DmpInputTaskInitDTO();
        dmpInputTaskInitDTO.setMsg(JSONObject.toJSONString(allResult));
        return Collections.singletonList(dmpInputTaskInitDTO);
    }


}
