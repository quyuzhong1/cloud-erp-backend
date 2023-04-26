package com.erp.server.sys.service.impl;

import com.common.business.service.SuperServiceImpl;
import com.common.core.utils.BeanMapper;
import com.erp.model.sys.dto.NoticeReceivedDTO;
import com.erp.model.sys.entity.NoticeReceivedEntity;
import com.erp.server.sys.mapper.NoticeReceivedMapper;
import com.erp.server.sys.service.NoticeReceivedService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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
    public void add(String noticeId, List<NoticeReceivedDTO.AddDTO> receivedList) {
        if (CollectionUtils.isEmpty(receivedList)) {
            List<NoticeReceivedEntity> addList = new ArrayList<>(receivedList.size());
            for (NoticeReceivedDTO.AddDTO item : receivedList) {
                NoticeReceivedEntity addEntity = new NoticeReceivedEntity();
                BeanMapper.copy(item, addEntity);
                addEntity.setNoticeId(noticeId);
                addList.add(addEntity);
            }

        }
    }

    /**
     * 更改接收人
     * @author yl
     * @date 2023-04-26 16:01
     * @param receivedList
     * @return void
     */
    @Override
    public void edit(List<NoticeReceivedDTO.UpdateDTO> receivedList) {

    }
}
