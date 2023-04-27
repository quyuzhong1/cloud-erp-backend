package com.erp.server.sys.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
import java.util.Collections;
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
            //先删除
            this.delete(noticeId);
            List<NoticeReceiverEntity> addList = new ArrayList<>(10);
            for (NoticeDTO.CfgNodeDTO item : cfgNodeList) {
                NoticeReceiverDTO.AddDTO receiver = item.getReceiver();
                String receiverType = receiver.getReceiverType();
                List<String> valueList = receiver.getReceiverValueList();
                List<String> nameList = receiver.getReceiverValueNameList();
                int nameSize = CollectionUtils.isNotEmpty(nameList) ? nameList.size() : 0;
                for (int i = 0; i < valueList.size(); i++) {
                    NoticeReceiverEntity addEntity = new NoticeReceiverEntity();
                    addEntity.setReceiverType(receiverType);
                    addEntity.setReceiverValue(valueList.get(i));
                    if(CollectionUtils.isNotEmpty(nameList)){
                        if (nameSize > i) {
                            addEntity.setReceiverValueName(nameList.get(i));
                        }
                    }
                    addEntity.setNoticeId(noticeId);
                    addList.add(addEntity);
                }

            }
            this.saveBatch(addList);
        }
    }

    
    /**
     * 根据通知节点id 删除
     * @author yl
     * @date 2023-04-27 16:11
     * @param noticeId
     * @return void
     */
    private void delete(String noticeId) {
        LambdaQueryWrapper<NoticeReceiverEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(NoticeReceiverEntity::getNoticeId, noticeId);
        this.remove(queryWrapper);
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
        return BeanMapper.copyList(dbList, NoticeReceiverDTO.UpdateDTO.class);
    }


    /**
     * 根据通知节点 ids 获取数据
     *
     * @param noticeIdList
     * @return java.util.List<com.erp.model.sys.entity.NoticeReceiverEntity>
     * @author yl
     * @date 2023-04-27 14:39
     */
    @Override
    public List<NoticeReceiverEntity> listByNoticeIds(List<String> noticeIdList) {
        if (CollectionUtils.isEmpty(noticeIdList)) {
            return Collections.emptyList();
        }
        return this.lambdaQuery().in(NoticeReceiverEntity::getNoticeId, noticeIdList).list();
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
