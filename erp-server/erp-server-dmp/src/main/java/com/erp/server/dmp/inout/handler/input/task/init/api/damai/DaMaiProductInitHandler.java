package com.erp.server.dmp.inout.handler.input.task.init.api.damai;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.business.wrapper.FeignQuery;
import com.common.core.exception.ServiceException;
import com.erp.model.dmp.enums.DmpBasicSystemCodeEnum;
import com.erp.model.oms.enums.AuthStatusEnum;
import com.erp.model.wms.entity.OverseasProviderEntity;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputInitRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputTaskResponse;
import com.erp.server.dmp.inout.handler.input.task.init.DmpInputInitHandler;
import com.erp.server.dmp.inout.utils.DmpHandlerCache;
import com.sdk.wms.damai.dto.response.DaMaiPageBaseResp;
import com.sdk.wms.damai.dto.response.DaMaiSkuResp;
import com.sdk.wms.damai.service.DaMaiService;
import com.sdk.wms.jifeng.dto.response.JiFengBaseResp;
import com.sdk.wms.jifeng.dto.response.JiFengProductResp;
import com.sdk.wms.jifeng.service.JiFengService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * dmp输入init任务基础处理器下的极风api获取数据方式
 */
@Slf4j
@Service
@Scope("prototype")
public class DaMaiProductInitHandler extends DmpInputInitHandler {

    @Resource
    private DmpHandlerCache dmpHandlerCache;

    @Resource
    private DaMaiService daMaiService;

    @Override
    public List<DmpInputTaskInitDTO> getInitData(DmpInputInitRequest dmpRequest, DmpInputTaskResponse dmpResponse) {
        List<DmpInputTaskInitDTO> resultList = new ArrayList<>();

        List<OverseasProviderEntity> overseasProviderEntityList = dmpHandlerCache.getOverseasProviderEntityList(d -> d.getCode().equals(DmpBasicSystemCodeEnum.DA_MAI.getCode()));
        if(CollUtil.isEmpty(overseasProviderEntityList)) {
            return Collections.emptyList();
        }
        // 取对应授权ID授权
        OverseasProviderEntity overseasProviderEntity = overseasProviderEntityList.stream()
                .filter(e -> e.getId().equalsIgnoreCase(dmpInputTaskEntity.getNextLevelId()))
                .findFirst()
                .orElse(null);
        if(null == overseasProviderEntity) {
            throw new ServiceException(DmpBasicSystemCodeEnum.DA_MAI.getCode() + "对应授权ID信息不存在,nextId:"+dmpInputTaskEntity.getNextLevelId());
        }

        DaMaiPageBaseResp<List<DaMaiSkuResp>> resp = daMaiService.getSkuList(overseasProviderEntity.getAuthJson());
        if(resp == null) {
            return Collections.emptyList();
        }
        if(StringUtils.isNotBlank(resp.getMsg())) {
            throw new ServiceException("大卖仓获取产品列表失败,msg:"+resp.getMsg());
        }
        if(CollUtil.isEmpty(resp.getData())) {
            return Collections.emptyList();
        }
        String id = overseasProviderEntity.getId();
        DmpInputTaskInitDTO dmpInputTaskInitDTO = new DmpInputTaskInitDTO();
        JSONArray parseArray = JSON.parseArray(JSONObject.toJSONString(resp.getData()));
        parseArray.forEach(p -> {
            JSONObject j = (JSONObject)p;
            j.put("authId", id);
        });
        dmpInputTaskInitDTO.setMsg(parseArray.toJSONString());
        resultList.add(dmpInputTaskInitDTO);
        return resultList;
    }

}
