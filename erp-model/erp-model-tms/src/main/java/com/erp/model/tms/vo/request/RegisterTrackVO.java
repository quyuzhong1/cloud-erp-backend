package com.erp.model.tms.vo.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @author zdy
 * @ClassName RegisterTrackVO

 * @date 2023年11月21日
 * @version: 1.0
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterTrackVO implements Serializable {
    List<LogisticsRegisterVO> logisticsRegisterVOS;
    /**
     * 授权信息
     */
    private Map<String, String> authMap;
}
