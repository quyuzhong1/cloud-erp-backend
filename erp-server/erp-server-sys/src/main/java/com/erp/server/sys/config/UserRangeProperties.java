package com.erp.server.sys.config;

import com.erp.model.sys.dto.CfgUserRangeDTO;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;


/**
 * @CreateTime: 2023-06-14  18:41
 * @Author: zhangchunlin
 */
@Data
@ConfigurationProperties(prefix = UserRangeProperties.PREFIX)
@RefreshScope
@Component
public class UserRangeProperties {

    public static final String PREFIX = "user-range";

    /**
     * 区间配置，键为区间类型
     */
    private LinkedHashMap<String, List<CfgUserRangeDTO.UserRangeDataDTO>> rangeMap = new LinkedHashMap<>();

}