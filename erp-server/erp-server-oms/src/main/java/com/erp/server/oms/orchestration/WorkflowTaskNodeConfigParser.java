package com.erp.server.oms.orchestration;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONUtil;
import com.common.business.enums.ServiceCodeNameEnum;
import com.common.core.constant.EnumMessage;
import com.erp.model.oms.dto.WorkflowTaskNodeConfigDTO;

/**
 * 解析 dict_basic 节点配置，兼容 classPath#method 与 JSON。
 */
public final class WorkflowTaskNodeConfigParser {

    private static final String HANDLER_TYPE_FEIGN_INVOKE = "feign_invoke";

    private WorkflowTaskNodeConfigParser() {
    }

    /**
     * 解析 dict_basic 节点配置，兼容 {@code classPath#method} 与 JSON 两种格式。
     *
     * @param rawValue 字典 value 字段原始值
     * @return 标准化后的节点配置（含 serviceCode、timeout、maxRetry 等默认值）
     */
    public static WorkflowTaskNodeConfigDTO parse(String rawValue) {
        WorkflowTaskNodeConfigDTO config = new WorkflowTaskNodeConfigDTO();
        if (CharSequenceUtil.isBlank(rawValue)) {
            return config;
        }
        String trimmed = rawValue.trim();
        if (trimmed.startsWith("{")) {
            WorkflowTaskNodeConfigDTO jsonConfig = JSONUtil.toBean(trimmed, WorkflowTaskNodeConfigDTO.class);
            fillDefaults(jsonConfig);
            return jsonConfig;
        }
        String[] split = trimmed.split("#");
        config.setHandlerType(HANDLER_TYPE_FEIGN_INVOKE);
        if (split.length >= 1) {
            config.setClassPath(split[0]);
            config.setServiceCode(resolveServiceCode(split[0]));
        }
        if (split.length >= 2) {
            config.setMethodName(split[1]);
        }
        config.setTimeoutSeconds(120);
        config.setMaxRetry(3);
        config.setIdempotent(Boolean.TRUE);
        return config;
    }

    /**
     * 构建监控展示用的目标接口标识，格式 {@code classPath#methodName}。
     */
    public static String buildTargetEndpoint(WorkflowTaskNodeConfigDTO config) {
        if (config == null) {
            return "";
        }
        if (CharSequenceUtil.isAllNotBlank(config.getClassPath(), config.getMethodName())) {
            return config.getClassPath() + "#" + config.getMethodName();
        }
        return CharSequenceUtil.blankToDefault(config.getClassPath(), "");
    }

    /**
     * 从 Controller 全类名解析微服务 code（如 com.erp.server.wms → wms）。
     */
    public static String resolveServiceCode(String classPath) {
        if (CharSequenceUtil.isBlank(classPath)) {
            return "";
        }
        String[] parts = classPath.split("\\.");
        if (parts.length < 4) {
            return "";
        }
        ServiceCodeNameEnum serviceCodeNameEnum = EnumMessage.getByCode(ServiceCodeNameEnum.class, parts[3]);
        return serviceCodeNameEnum == null ? parts[3] : serviceCodeNameEnum.getCode();
    }

    /**
     * 将微服务 code 转为可读名称，用于监控页展示。
     */
    public static String resolveServiceName(String serviceCode) {
        if (CharSequenceUtil.isBlank(serviceCode)) {
            return "";
        }
        ServiceCodeNameEnum serviceCodeNameEnum = EnumMessage.getByCode(ServiceCodeNameEnum.class, serviceCode);
        return serviceCodeNameEnum == null ? serviceCode : serviceCodeNameEnum.getName();
    }

    private static void fillDefaults(WorkflowTaskNodeConfigDTO config) {
        if (CharSequenceUtil.isBlank(config.getHandlerType())) {
            config.setHandlerType(HANDLER_TYPE_FEIGN_INVOKE);
        }
        if (CharSequenceUtil.isBlank(config.getServiceCode()) && CharSequenceUtil.isNotBlank(config.getClassPath())) {
            config.setServiceCode(resolveServiceCode(config.getClassPath()));
        }
        if (config.getTimeoutSeconds() == null) {
            config.setTimeoutSeconds(120);
        }
        if (config.getMaxRetry() == null) {
            config.setMaxRetry(3);
        }
        if (config.getIdempotent() == null) {
            config.setIdempotent(Boolean.TRUE);
        }
    }
}
