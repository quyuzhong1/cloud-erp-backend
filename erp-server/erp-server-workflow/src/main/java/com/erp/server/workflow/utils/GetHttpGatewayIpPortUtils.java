package com.erp.server.workflow.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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

    public static String OMS_PORT;

    @Value("${HttpGatewayIpPort.ip}")
    public void setIp(String ip) {
        GetHttpGatewayIpPortUtils.IP = ip;
    }

    @Value("${HttpGatewayIpPort.plm.port}")
    public void setPlmPort(String plmPort) {
        GetHttpGatewayIpPortUtils.PLM_PORT = plmPort;
    }

    @Value("${HttpGatewayIpPort.scm.port}")
    public void setScmPort(String scmPort) {
        GetHttpGatewayIpPortUtils.SCM_PORT = scmPort;
    }

    @Value("${HttpGatewayIpPort.wms.port}")
    public void setWmsPort(String wmsPort) {
        GetHttpGatewayIpPortUtils.WMS_PORT = wmsPort;
    }

    @Value("${HttpGatewayIpPort.oms.port}")
    public void setOmsPort(String omsPort) {
        GetHttpGatewayIpPortUtils.OMS_PORT = omsPort;
    }
}
