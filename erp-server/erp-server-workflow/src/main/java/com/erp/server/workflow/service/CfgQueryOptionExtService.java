package com.erp.server.workflow.service;
import com.erp.model.workflow.entity.CfgQueryOptionExtEntity;
import com.common.business.service.SuperService;

import java.util.Map;

/**
 * <p>
 * cfg_query_option拓展表 服务类
 * </p>
 *
 * @author jack
 * @since 2025-06-04
 */
public interface CfgQueryOptionExtService extends SuperService<CfgQueryOptionExtEntity> {


    Map<String,String> getRemoteValues(Map<String, String> map);
}
