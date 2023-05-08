package com.erp.server.workflow.utils;

import io.lettuce.core.dynamic.annotation.Command;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * 工作台页面IP网关跳转配置
 * @Author Luo_WG
 * @Date 2023/4/27 10:00
 **/
@Slf4j
@Component
public class GetHttpGatewayIpPortUtils {

    public static String IP;

    public static String PLM_PORT;

    public static String SCM_PORT;

    public static String WMS_PORT;

    @Value("${HttpGatewayIpPort.ip}")
    public void setIP(String IP) {
        GetHttpGatewayIpPortUtils.IP = IP;
    }

    @Value("${HttpGatewayIpPort.plm.port}")
    public void setPLM_PORT(String PLM_PORT) {
        GetHttpGatewayIpPortUtils.PLM_PORT = PLM_PORT;
    }

    @Value("${HttpGatewayIpPort.scm.port}")
    public void setSCM_PORT(String SCM_PORT) {
        GetHttpGatewayIpPortUtils.SCM_PORT = SCM_PORT;
    }

    @Value("${HttpGatewayIpPort.wms.port}")
    public void setWMS_PORT(String WMS_PORT) {
        GetHttpGatewayIpPortUtils.WMS_PORT = WMS_PORT;
    }
}
