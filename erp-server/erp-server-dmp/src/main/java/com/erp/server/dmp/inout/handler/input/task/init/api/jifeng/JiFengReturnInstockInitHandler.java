package com.erp.server.dmp.inout.handler.input.task.init.api.jifeng;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.common.business.threadlocal.ThirdWarehouseContext;
import com.common.business.wrapper.FeignQuery;
import com.common.core.exception.ServiceException;
import com.erp.model.dmp.entity.DmpCfgApiEntity;
import com.erp.model.dmp.enums.DmpBasicSystemCodeEnum;
import com.erp.model.oms.enums.AuthStatusEnum;
import com.erp.model.wms.entity.OverseasProviderEntity;
import com.erp.rpc.wms.feign.OverseasProviderFeign;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputInitRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputTaskResponse;
import com.erp.server.dmp.inout.handler.input.task.init.DmpInputInitHandler;
import com.erp.server.dmp.inout.utils.DmpHandlerCache;
import com.sdk.wms.goodcang.dto.request.GoodCangGetReturnInstockReq;
import com.sdk.wms.goodcang.dto.response.GoodCangResponse;
import com.sdk.wms.goodcang.dto.response.GoodCangReturnInstockResp;
import com.sdk.wms.goodcang.utils.GoodCangUtils;
import com.sdk.wms.jifeng.dto.request.JiFengReturnOrderRequest;
import com.sdk.wms.jifeng.dto.response.JiFengBaseResp;
import com.sdk.wms.jifeng.dto.response.JiFengReturnOrderResp;
import com.sdk.wms.jifeng.service.JiFengService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * dmp输入init任务基础处理器下的旺店通api获取数据方式
 *
 * @author Administrator
 */
@Slf4j
@Service
@Scope("prototype")
public class JiFengReturnInstockInitHandler extends DmpInputInitHandler {

    @Resource
    private DmpHandlerCache dmpHandlerCache;

    @Resource
    private JiFengService jiFengService;

    @Resource
    private OverseasProviderFeign overseasProviderFeign;

    @Override
    public List<DmpInputTaskInitDTO> getInitData(DmpInputInitRequest dmpRequest, DmpInputTaskResponse dmpResponse) {

        List<OverseasProviderEntity> overseasProviderEntityList = FeignQuery.create(OverseasProviderEntity.class)
                .eq(OverseasProviderEntity::getAuthStatus, AuthStatusEnum.ALREADY.getCode())
                .eq(OverseasProviderEntity::getCode, DmpBasicSystemCodeEnum.JIFENG.getCode())
                .list();
        if (CollUtil.isEmpty(overseasProviderEntityList)) {
            throw new ServiceException("极风授权信息不存在");
        }
        // 取对应授权ID授权
        OverseasProviderEntity overseasProviderEntity = overseasProviderEntityList.stream()
                .filter(e -> e.getId().equalsIgnoreCase(dmpInputTaskEntity.getNextLevelId()))
                .findFirst()
                .orElse(null);
        if(null == overseasProviderEntity) {
            throw new ServiceException("极风对应授权ID信息不存在");
        }
        String authId = overseasProviderEntity.getId();
        JiFengReturnOrderRequest request = new JiFengReturnOrderRequest();
        //极风时间查询需要提前9小时
        request.setBeginTime(dmpInputTaskEntity.getStartTime().minusHours(9).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        request.setEndTime(dmpInputTaskEntity.getEndTime().minusHours(9).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        JiFengBaseResp<List<JiFengReturnOrderResp.RowsDTO>> resp = jiFengService.getReturnOrder(overseasProviderEntity.getAuthJson(),request);
        if (resp == null) {
            return Collections.emptyList();
        }
        if (resp.getCode() != 0) {
            if(resp.getMessage().contains("Invalid ACCESS TOKEN")){
                overseasProviderEntity = overseasProviderFeign.refreshToken(overseasProviderEntity);
                resp = jiFengService.getReturnOrder(overseasProviderEntity.getAuthJson(),request);
                if (resp == null) {
                    return Collections.emptyList();
                }
                if (resp.getCode() != 0) {
                    log.error("极风接口返回异常:{}", JSON.toJSONString(resp));
                    throw new ServiceException("极风接口返回异常:" + resp.getMessage());
                }
            }else{
                log.error("极风接口返回异常:{}", JSON.toJSONString(resp));
                throw new ServiceException("极风接口返回异常:" + resp.getMessage());
            }
        }
        if(CollUtil.isEmpty(resp.getData())) {
            return Collections.emptyList();
        }
        resp.getData().forEach(v->v.setAuthId(authId));
        DmpInputTaskInitDTO dmpInputTaskInitDTO = new DmpInputTaskInitDTO();
        dmpInputTaskInitDTO.setMsg(JSONObject.toJSONString(resp.getData()));
        return Collections.singletonList(dmpInputTaskInitDTO);
    }

}
