package com.erp.server.sys.service.impl;

import com.common.business.service.SuperServiceImpl;
import com.common.core.utils.BeanMapper;
import com.erp.model.sys.dto.NoticeDTO;
import com.erp.model.sys.dto.NoticeReceiverDTO;
import com.erp.model.sys.entity.NoticeReceiverEntity;
import com.erp.server.sys.mapper.NoticeReceivedMapper;
import com.erp.server.sys.service.NoticeReceiverService;
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
public class NoticeReceiverServiceImpl extends SuperServiceImpl<NoticeReceivedMapper, NoticeReceiverEntity> implements NoticeReceiverService {

    @Override
    public void add(String noticeId, List<NoticeDTO.CfgNodeDTO> cfgNodeList) {
        if (CollectionUtils.isNotEmpty(cfgNodeList)) {
            List<NoticeReceiverEntity> addList = new ArrayList<>(10);
            for (NoticeDTO.CfgNodeDTO item : cfgNodeList) {
                List<NoticeReceiverDTO.AddDTO> receiverList = item.getReceiverList();
                for (NoticeReceiverDTO.AddDTO receiver : receiverList) {
                    NoticeReceiverEntity addEntity = new NoticeReceiverEntity();
                    BeanMapper.copy(receiver, addEntity);
                    addEntity.setNoticeId(noticeId);
                    addList.add(addEntity);
                }
            }
            this.saveBatch(addList);
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
    public Boolean edit(String noticeId, List<NoticeReceiverDTO.UpdateDTO> receivedList) {
        if (CollectionUtils.isEmpty(receivedList)) {
            return Boolean.TRUE;
        }
        List<NoticeReceiverEntity> dbList = this.getByNoticeId(noticeId);
        //获取到删除的 等级id
        List<String> deleteIdList = getDeleteIds(receivedList, dbList);
        if (CollectionUtils.isNotEmpty(deleteIdList)) {
            this.removeByIds(deleteIdList);
        }
        List<NoticeReceiverEntity> batchReceivedList = BeanMapper.copyList(receivedList, NoticeReceiverEntity.class);

        return this.saveOrUpdateBatch(batchReceivedList);
    }


    /**
     * 根据通知id 获取接收人信息
     *
     * @param noticeId
     * @return java.util.List<com.erp.model.sys.dto.NoticeReceiverDTO.UpdateDTO>
     * @author yl
     * @date 2023-04-26 19:37
     */
    @Override
    public List<NoticeReceiverDTO.UpdateDTO> listByNoticeId(String noticeId) {
        List<NoticeReceiverEntity> dbList = this.getByNoticeId(noticeId);
        return BeanMapper.copyList(dbList,NoticeReceiverDTO.UpdateDTO.class);
    }


    /**
     * 获取到删除的id
     *
     * @param receivedList
     * @param dbList
     * @return java.util.List<java.lang.String>
     * @author yl
     * @date 2023-04-26 16:13
     */
    private List<String> getDeleteIds(List<NoticeReceiverDTO.UpdateDTO> receivedList, List<NoticeReceiverEntity> dbList) {
        List<String> ids = receivedList.stream().filter(g -> StringUtils.isNotBlank(g.getId())).
                map(NoticeReceiverDTO.UpdateDTO::getId).collect(Collectors.toList());
        List<String> dbIds = dbList.stream().map(NoticeReceiverEntity::getId).collect(Collectors.toList());
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

    private List<NoticeReceiverEntity> getByNoticeId(String noticeId) {
        return this.lambdaQuery().eq(NoticeReceiverEntity::getNoticeId, noticeId).list();
    }
}
