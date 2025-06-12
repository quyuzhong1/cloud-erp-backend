package com.erp.server.workflow.controller.feign;

import cn.hutool.core.collection.CollUtil;
import com.common.core.controller.BaseController;
import com.erp.model.workflow.dto.*;
import com.erp.model.workflow.entity.CfgQueryOptionEntity;
import com.erp.model.workflow.enums.CfgQueryOptionExtendTypeEnum;
import com.erp.server.workflow.service.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * cfgQueryOption Feign
 * @date 2025-05-29
 * @author jack
 */
@RestController
@RequestMapping("feign/cfgQueryOption")
@Slf4j
public class CfgQueryOptionFeignController extends BaseController {

    @Resource
    public CfgQueryOptionService cfgQueryOptionService;
    @Resource
    public CfgQueryOptionExtService cfgQueryOptionExtService;

    /**
     */
    @PostMapping("/listByMqParams")
    public List<CfgQueryOptionEntity> listByMqParams(@RequestBody CfgQueryOptionDTO.MqParamsDTO mqParamsDTO) {
        return cfgQueryOptionService.listByMqParams(mqParamsDTO);
    }

    /**
     */
    @PostMapping("/listByIds")
    public List<CfgQueryOptionEntity> listByIds(@RequestBody List<String> ids) {
        return cfgQueryOptionService.lambdaQuery().in(CfgQueryOptionEntity::getId, ids).list();
    }

    @PostMapping("/getRemoteValues")
    public Map<String,String> getRemoteValues(@RequestBody Map<String,String> handlerValueMap){
        return cfgQueryOptionExtService.getRemoteValues(handlerValueMap);
    }

    /**
     * 根据单据类型查询配置(只查询明细类型)
     */
    @PostMapping("/getVariablesMapByBusinessKey")
    public Map<String, Object> getVariablesMapByBusinessKey(@RequestBody CfgQueryOptionDTO.VariablesParamsDTO dto){
        return cfgQueryOptionService.getVariablesMapByBusinessKey(dto);
    }
    /**
     * 根据字段条件查询配置
     */
    @PostMapping("/listExtendByFieldCondition")
    public List<CfgQueryOptionEntity> listExtendByFieldCondition(@RequestBody CfgQueryOptionDTO.ListByFieldDTO listByFieldDTO){
        if(Objects.isNull(listByFieldDTO) || StringUtils.isBlank(listByFieldDTO.getBusinessType()) || CollUtil.isEmpty(listByFieldDTO.getFieldList())){
            return Collections.emptyList();
        }

        return cfgQueryOptionService.lambdaQuery()
                .eq(CfgQueryOptionEntity::getBussinessKey, listByFieldDTO.getBusinessType())
                .in(CfgQueryOptionEntity::getConditionField, listByFieldDTO.getFieldList())
                .eq(CfgQueryOptionEntity::getExtendType, CfgQueryOptionExtendTypeEnum.NOTICENODE.getCode())
                .list();
    }
}
