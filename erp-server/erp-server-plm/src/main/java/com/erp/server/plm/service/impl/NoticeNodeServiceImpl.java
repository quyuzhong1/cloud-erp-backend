package com.erp.server.plm.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.model.plm.dto.NoticeNodeDTO;
import com.erp.model.plm.entity.NoticeNodeEntity;
import com.erp.server.plm.mapper.NoticeNodeMapper;
import com.erp.server.plm.service.NoticeNodeService;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
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
        List<NoticeNodeEntity> list = baseMapper.list();
        if(CollUtil.isEmpty(list)){
            return Collections.emptyList();
        }

        List<Map<String, Object>> result = CollUtil.newArrayList();
        for (NoticeNodeEntity noticeNodeEntity : list) {
            Map<String, Object> map = new HashMap<>();
            map.put("nodeName", noticeNodeEntity.getNodeName());
            map.put("id", noticeNodeEntity.getId());
            result.add(map);
        }
        return result;
    }
}




