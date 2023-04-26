package com.erp.server.sys.service;

import com.common.business.service.SuperService;
import com.erp.model.sys.dto.CfgNodeMemberDTO;
import com.erp.model.sys.entity.CfgNodeMemberEntity;

import java.util.List;

/**
 * <p>
 * 节点接收配置表 服务类
 * </p>
 *
 * @author lambda
 * @since 2023-04-26
 */
public interface CfgNodeMemberService extends SuperService<CfgNodeMemberEntity> {

    
    /**
     * 添加或者修改节点配置
     * @author yl
     * @date 2023-04-26 17:54
     * @param list
     * @return java.lang.Boolean
     */
    Boolean addOrUpdate(List<CfgNodeMemberDTO.AddOrUpdateDTO> list);

    /**
     * 根据通知节点id 获取到对应 配置的列表
     * @author yl
     * @date 2023-04-26 18:30
     * @param nodeKey
     * @return java.util.List<com.erp.model.sys.dto.CfgNodeMemberDTO.ListDTO>
     */
    List<CfgNodeMemberDTO.ListDTO> listByNodeKey(String nodeKey);
}
