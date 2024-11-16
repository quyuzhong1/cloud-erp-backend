package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.model.plm.dto.NoticeNodeDTO;
import com.erp.model.plm.entity.NoticeNodeEntity;
import com.erp.server.plm.mapper.NoticeNodeMapper;
import com.erp.server.plm.service.NoticeNodeService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 *
 */
@Service
public class NoticeNodeServiceImpl extends ServiceImpl<NoticeNodeMapper, NoticeNodeEntity>
        implements NoticeNodeService {


    /**
     * 添加节点
     *
     * @param dto
     * @return boolean
     * @author yl
     * @date 2022-11-07 10:54
     */
    @Override
    public boolean addNoticeNode(NoticeNodeDTO dto) {
        NoticeNodeEntity nodeEntity = new NoticeNodeEntity();
        nodeEntity.setNodeName(dto.getNodeName());
        nodeEntity.setNodeFlag(dto.getNodeFlag());
        return this.save(nodeEntity);
    }

    /**
     * 获取到通知节点列表
     *
     * @param
     * @return java.util.List<java.util.Map < java.lang.String, java.lang.Object>>
     * @author yl
     * @date 2022-11-07 10:58
     */
    @Override
    public List<Map<String, Object>> getList() {
        LambdaQueryWrapper<NoticeNodeEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(NoticeNodeEntity::getId, NoticeNodeEntity::getNodeName);
        queryWrapper.notInSql(NoticeNodeEntity::getId,"select node_id from notice_message");
        return this.listMaps(queryWrapper);
    }
}




