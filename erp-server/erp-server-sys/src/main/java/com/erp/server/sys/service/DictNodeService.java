package com.erp.server.sys.service;

import com.common.business.service.SuperService;
import com.erp.model.sys.dto.DictNodeDTO;
import com.erp.model.sys.entity.DictNodeEntity;

import java.util.List;

/**
 * <p>
 * 通知信息表 服务类
 * </p>
 *
 * @author lambda
 * @since 2023-04-26
 */
public interface DictNodeService extends SuperService<DictNodeEntity> {

    
    /**
     * 添加节点
     * @author yl
     * @date 2023-04-26 14:32
     * @param dto
     * @return java.lang.Boolean
     */
    Boolean add(DictNodeDTO.AddDTO dto);

    
    /**
     * 根据业务类型获取到通知节点
     * @author yl
     * @date 2023-04-26 14:46
     * @param businessType
     * @return java.util.List<com.erp.model.sys.dto.DictNodeDTO.ViewDTO>
     */
    List<DictNodeDTO.ViewDTO> listByBusinessType(String businessType);
}
