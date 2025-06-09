package com.erp.server.workflow.service;

import com.common.business.service.SuperService;
import com.erp.model.workflow.dto.CfgQueryOptionDTO;
import com.erp.model.workflow.entity.CfgQueryOptionEntity;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 查询option配置表(数大臣单据字段) 服务类
 * </p>
 *
 * @author hcg
 * @since 2025-05-15
 */
public interface CfgQueryOptionService extends SuperService<CfgQueryOptionEntity> {


    List<CfgQueryOptionDTO.ListDTO> proDropDown(String bussinessKey);

    List<CfgQueryOptionDTO.cfgApproveSyncDropDownDTO> cfgApproveSyncDropDown(String bussinessKey,String fieldBelongsType);


    List<CfgQueryOptionDTO.TreeDTO> tree(String bussinessKey);

    void genBySql(List<CfgQueryOptionDTO.GenListDTO>list);

    List<CfgQueryOptionDTO.ViewDTO> getSystemfield(String bussinessKey);

    /**
     * 根据单据和字段查询
     * @author will
     * @date 2025/5/29 10:36
     * @param bussinessKey
     * @param sysFieldList
     * @return List<CfgQueryOptionEntity>
     */
    List<CfgQueryOptionEntity> listBySysFieldList(String bussinessKey,List<String> sysFieldList);

    List<CfgQueryOptionEntity> listByMqParams(CfgQueryOptionDTO.MqParamsDTO mqParamsDTO);

    Map<String, Object> getVariablesMapByBusinessKey(CfgQueryOptionDTO.VariablesParamsDTO dto);
}
