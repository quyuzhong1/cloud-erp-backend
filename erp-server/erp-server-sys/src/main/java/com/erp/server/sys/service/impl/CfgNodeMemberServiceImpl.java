package com.erp.server.sys.service.impl;

import com.common.business.service.SuperServiceImpl;
import com.common.core.utils.BeanMapper;
import com.erp.model.sys.dto.CfgNodeMemberDTO;
import com.erp.model.sys.entity.CfgNodeMemberEntity;
import com.erp.server.sys.mapper.CfgNodeMemberMapper;
import com.erp.server.sys.service.CfgNodeMemberService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 节点接收配置表 服务实现类
 * </p>
 *
 * @author lambda
 * @since 2023-04-26
 */
@Service
public class CfgNodeMemberServiceImpl extends SuperServiceImpl<CfgNodeMemberMapper, CfgNodeMemberEntity> implements CfgNodeMemberService {


    /**
     * 添加或者修改节点配置
     *
     * @param list
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-04-26 17:54
     */
    @Override
    public Boolean addOrUpdate(List<CfgNodeMemberDTO.AddOrUpdateDTO> list) {
        if (CollectionUtils.isEmpty(list)) {
            return Boolean.TRUE;
        }
        List<CfgNodeMemberEntity> addOrUpdateList = BeanMapper.copyList(list, CfgNodeMemberEntity.class);
        return this.saveOrUpdateBatch(addOrUpdateList);
    }

    /**
     * 根据通知节点id 获取到对应 配置的列表
     *
     * @param nodeKey
     * @return java.util.List<com.erp.model.sys.dto.CfgNodeMemberDTO.ListDTO>
     * @author yl
     * @date 2023-04-26 18:30
     */
    @Override
    public List<CfgNodeMemberDTO.ListDTO> listByNodeKey(String nodeKey) {
        List<CfgNodeMemberEntity> dbList = this.getByNodeKey(nodeKey);
        return BeanMapper.copyList(dbList,CfgNodeMemberDTO.ListDTO.class);
    }

    public List<CfgNodeMemberEntity> getByNodeKey(String nodeKey) {
        return this.lambdaQuery().eq(CfgNodeMemberEntity::getNodeKey, nodeKey).list();
    }
}
