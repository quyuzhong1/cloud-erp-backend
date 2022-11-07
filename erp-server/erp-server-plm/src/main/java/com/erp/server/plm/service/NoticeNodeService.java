package com.erp.server.plm.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.model.plm.dto.NoticeNodeDTO;
import com.erp.model.plm.entity.NoticeNodeEntity;

import java.util.List;
import java.util.Map;

/**
 *
 */
public interface NoticeNodeService extends IService<NoticeNodeEntity> {

    boolean addNoticeNode(NoticeNodeDTO dto);

    List<Map<String,Object>> getList();
}
