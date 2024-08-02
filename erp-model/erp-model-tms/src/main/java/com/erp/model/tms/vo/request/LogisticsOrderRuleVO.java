package com.erp.model.tms.vo.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LogisticsOrderRuleVO implements Serializable {

    private LogisticsOrderVO logisticsOrderVO;

    private Map<String,Object> map;
}
