package com.erp.server.sys.service.impl;

import com.common.business.service.SuperServiceImpl;
import com.erp.model.sys.dto.NoticeReceivedDTO;
import com.erp.model.sys.entity.NoticeReceivedEntity;
import com.erp.server.sys.mapper.NoticeReceivedMapper;
import com.erp.server.sys.service.NoticeReceivedService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 通知接收人信息 服务实现类
 * </p>
 *
 * @author lambda
 * @since 2023-04-20
 */
@Service
public class NoticeReceivedServiceImpl extends SuperServiceImpl<NoticeReceivedMapper, NoticeReceivedEntity> implements NoticeReceivedService {

    @Override
    public void add(String id, List<NoticeReceivedDTO.AddDTO> receivedList) {

    }
}
