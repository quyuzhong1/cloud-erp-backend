package com.erp.server.sys.service.impl;

import com.common.business.service.SuperServiceImpl;
import com.common.core.utils.BeanMapper;
import com.erp.model.sys.dto.NoticeReceivedDTO;
import com.erp.model.sys.entity.NoticeReceivedEntity;
import com.erp.server.sys.mapper.NoticeReceivedMapper;
import com.erp.server.sys.service.NoticeReceivedService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
     *
     * @param receivedList
     * @return void
     * @author yl
     * @date 2023-04-26 16:01
     */
    @Override
    public Boolean edit(String noticeId, List<NoticeReceivedDTO.UpdateDTO> receivedList) {
        if (CollectionUtils.isEmpty(receivedList)) {
            return Boolean.TRUE;
        }
        List<NoticeReceivedEntity> dbList = this.listByNoticeId(noticeId);
        //获取到删除的 等级id
        List<String> deleteIdList = getDeleteIds(receivedList, dbList);
        if (CollectionUtils.isNotEmpty(deleteIdList)) {
            this.removeByIds(deleteIdList);
        }
        List<NoticeReceivedEntity> batchReceivedList = BeanMapper.copyList(receivedList, NoticeReceivedEntity.class);

        return this.saveOrUpdateBatch(batchReceivedList);
    }

    
    /**
     * 获取到删除的id
     * @author yl
     * @date 2023-04-26 16:13
     * @param receivedList
     * @param dbList
     * @return java.util.List<java.lang.String>
     */
    private List<String> getDeleteIds(List<NoticeReceivedDTO.UpdateDTO> receivedList, List<NoticeReceivedEntity> dbList) {
        List<String> ids = receivedList.stream().filter(g -> StringUtils.isNotBlank(g.getId())).
                map(NoticeReceivedDTO.UpdateDTO::getId).collect(Collectors.toList());
        List<String> dbIds = dbList.stream().map(NoticeReceivedEntity::getId).collect(Collectors.toList());
        return dbIds.stream().filter(s -> !ids.contains(s)).collect(Collectors.toList());
    }


    /**
     * 根据通知id 获取数据
     *
     * @param noticeId
     * @return java.util.List<com.erp.model.sys.entity.NoticeReceivedEntity>
     * @author yl
     * @date 2023-04-26 16:12
     */
    private List<NoticeReceivedEntity> listByNoticeId(String noticeId) {
        return this.lambdaQuery().eq(NoticeReceivedEntity::getNoticeId, noticeId).list();
    }
}
