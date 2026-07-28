package com.erp.server.dmp.handler;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.fastjson.JSON;
import com.common.business.dto.WebhookResult;
import com.common.core.exception.ServiceException;
import com.common.core.utils.ValidatorUtil;
import com.erp.model.sys.openapi.AiyaChangeAttributeDTO;
import com.erp.rpc.wms.feign.WarehouseLocationMoveFeign;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * 爱亚转移单反馈（changeAttribute）：按对方裸报文接收，落《仓位移动》。
 * <p>
 * 入口：{@code POST /webhook/receive/aiyaChangeAttribute}，响应由 WebhookController 转为 MetaResponse。
 */
@Slf4j
public class AiyaChangeAttributeWebhookHandler implements WebhookHandler {

    private final WarehouseLocationMoveFeign warehouseLocationMoveFeign =
            SpringUtil.getBean(WarehouseLocationMoveFeign.class);

    private static WebhookResult metaSuccess(String moveId) {
        WebhookResult result = new WebhookResult();
        result.setSuccess(Boolean.TRUE);
        result.setFlag("SUCCESS");
        result.setMessage(null);
        result.setData(CharSequenceUtil.nullToEmpty(moveId));
        return result;
    }

    private static WebhookResult metaFail(String code, String message) {
        WebhookResult result = new WebhookResult();
        result.setSuccess(Boolean.FALSE);
        result.setFlag(code);
        result.setMessage(message);
        result.setData(null);
        return result;
    }

    @Override
    public void verify(String data, Map<String, String> headers, String serviceFlag) {
        // 讨论结论：与同模块其它 Webhook 一致，不做业务鉴权；仅依赖网关 AuthPassPath 免登。
        // 接口调用审计：成功落单/幂等命中后由 WMS 写入仓位移动 operate_log（operation=爱亚Webhook接收）。
    }

    @Override
    public WebhookResult process(String data, Map<String, String> headers, String serviceFlag) {
        if (CharSequenceUtil.isBlank(data)) {
            return metaFail("INVALID_DATA", "请求体不能为空");
        }
        log.warn("webhook 接收爱亚转移单反馈, body={}", data);
        AiyaChangeAttributeDTO dto;
        try {
            dto = JSON.parseObject(data, AiyaChangeAttributeDTO.class);
        } catch (Exception e) {
            log.error("爱亚转移单报文解析失败", e);
            return metaFail("INVALID_DATA", "请求体解析失败");
        }
        if (dto == null) {
            return metaFail("INVALID_DATA", "请求体不能为空");
        }
        try {
            ValidatorUtil.validateEntity(dto);
        } catch (ServiceException e) {
            return metaFail("INVALID_DATA", CharSequenceUtil.blankToDefault(e.getMsg(), "请求参数校验失败"));
        }
        try {
            String moveId = warehouseLocationMoveFeign.receiveAiyaChangeAttribute(dto);
            return metaSuccess(moveId);
        } catch (ServiceException e) {
            log.error("爱亚转移单落仓位移动业务失败 changeAttributeNumber={}",
                    dto.getChangeAttributeNumber(), e);
            return metaFail("INVALID_OPERATION", CharSequenceUtil.blankToDefault(e.getMsg(), "业务处理失败"));
        } catch (Exception e) {
            log.error("爱亚转移单落仓位移动系统异常 changeAttributeNumber={}",
                    dto.getChangeAttributeNumber(), e);
            return metaFail("INTERNAL_ERROR", "NETWORK ERROR");
        }
    }
}
