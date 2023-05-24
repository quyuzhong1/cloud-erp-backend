package com.erp.server.oms.service;

import com.common.business.service.SuperService;
import com.erp.model.oms.dto.SoChangeDTO;
import com.erp.model.oms.entity.SoChangeEntity;

import java.util.List;

/**
 * <p>
 * 销售订单变更 服务类
 * </p>
 *
 * @author lambda
 * @since 2023-05-10
 */
public interface SoChangeService extends SuperService<SoChangeEntity> {

    
    /**
     * 添加销售订单变更
     * @author yl
     * @date 2023-05-18 11:54
     * @param dto
     * @return java.lang.String
     */
    String add(SoChangeDTO.AddDTO dto);

    
    /**
     * 提交审核
     * @author yl
     * @date 2023-05-24 14:45
     * @param ids
     * @return java.lang.Boolean
     */
    Boolean submit(List<String> ids);

    
    /**
     * 新增并提交
     * @author yl
     * @date 2023-05-24 14:53
     * @param dto
     * @return java.lang.Boolean
     */
    Boolean addAndSubmit(SoChangeDTO.AddDTO dto);

    /**
     * 获取tab 列表
     * @author yl
     * @date 2023-05-24 14:56
     * @param
     * @return java.util.List<com.erp.model.oms.dto.SoChangeDTO.TabListDTO>
     */
    List<SoChangeDTO.TabListDTO> tabList();
}
