package com.erp.rpc.workflow.feign;

import com.common.business.config.FeignErrorDecoder;
import com.erp.model.workflow.dto.CfgQueryOptionDTO;
import com.erp.model.workflow.dto.ProcessTaskManagementDTO;
import com.erp.model.workflow.entity.CfgQueryOptionEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

/**
 * cfgQueryOption Feign
 * @date 2025-05-29
 * @author jack
 */
@FeignClient(name = "erp-workflow", contextId = "cfgQueryOption",configuration = {FeignErrorDecoder.class})
public interface CfgQueryOptionFeign {

    /**
     * 根据业务ID查询审批记录
     */
    @PostMapping("/feign/cfgQueryOption/listByMqParams")
    List<CfgQueryOptionEntity> listByMqParams(@RequestBody CfgQueryOptionDTO.MqParamsDTO mqParamsDTO);
    /**
     * 根据业务ID查询审批记录
     */
    @PostMapping("/feign/cfgQueryOption/listByIds")
    List<CfgQueryOptionEntity> listByIds(@RequestBody List<String> ids);

    /**
     * 值映射处理
     */
    @PostMapping("/feign/cfgQueryOption/getRemoteValues")
    Map<String,String> getRemoteValues(@RequestBody Map<String,String> handlerValueMap);

    /**
     * 根据单据类型查询配置
     */
    @PostMapping("/feign/cfgQueryOption/getVariablesMapByBusinessKey")
    Map<String, Object> getVariablesMapByBusinessKey(@RequestBody CfgQueryOptionDTO.VariablesParamsDTO dto);

    /**
     * 根据字段条件查询配置
     */
    @PostMapping("/feign/cfgQueryOption/listExtendByFieldCondition")
    List<CfgQueryOptionEntity> listExtendByFieldCondition(@RequestBody CfgQueryOptionDTO.ListByFieldDTO listByFieldDTO);
}
