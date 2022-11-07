package com.erp.server.plm.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.common.dto.base.BaseSearchDTO;
import com.erp.common.dto.base.PagingDTO;
import com.erp.common.dto.base.UpdateStateDTO;
import com.erp.common.enums.ApiError;
import com.erp.common.exception.ServiceException;
import com.erp.common.modules.sys.dto.FindUserDTO;
import com.erp.common.vo.PagingVO;
import com.erp.model.plm.dto.NoticeMessageDTO;
import com.erp.model.plm.entity.NoticeMessageEntity;
import com.erp.model.plm.entity.NoticeNodeEntity;
import com.erp.server.plm.constant.IsConstant;
import com.erp.server.plm.enums.NoticeItemPeopleEnum;
import com.erp.server.plm.mapper.NoticeMessageMapper;
import com.erp.server.plm.service.CommonService;
import com.erp.server.plm.service.NoticeMessageService;
import com.erp.server.plm.service.NoticeNodeService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 *
 */
@Service
public class NoticeMessageServiceImpl extends ServiceImpl<NoticeMessageMapper, NoticeMessageEntity>
        implements NoticeMessageService {

    @Autowired
    private CommonService commonService;

    @Autowired
    private NoticeNodeService noticeNodeService;

    @Override
    public PagingVO<List<NoticeMessageDTO>> paging(PagingDTO<BaseSearchDTO> dto) {
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        BaseSearchDTO params = dto.getParams();
        IPage pageData = baseMapper.paging(query, params, IsConstant.YES);
        List<NoticeMessageDTO> list = pageData.getRecords();
        if (CollectionUtils.isNotEmpty(list)) {
            List<FindUserDTO> userList = commonService.getAllUser();
            for (NoticeMessageDTO item : list) {
                String createUserId = item.getCreateUserId();
                if (StringUtils.isNotBlank(createUserId)) {
                    FindUserDTO createUser = userList.stream().filter(u -> createUserId.equals(u.getUserId())).findFirst().orElse(null);
                    if (createUser != null) {
                        item.setCreateUserName(createUser.getUserName());
                    }
                }
                String updateUserId = item.getUpdateUserId();
                if (StringUtils.isNotBlank(updateUserId)) {
                    FindUserDTO updateUser = userList.stream().filter(u -> updateUserId.equals(u.getUserId())).findFirst().orElse(null);
                    if (updateUser != null) {
                        item.setUpdateUserName(updateUser.getUserName());
                    }
                }


                //其它人
                String otherPeople = item.getOtherPeople();
                List<String> otherPeopleList = new ArrayList<>();
                if (StringUtils.isNotBlank(otherPeople)) {
                    String[] other = otherPeople.split(",");
                    List<String> names = new ArrayList<>(other.length);
                    for (String otherUserId : other) {
                        FindUserDTO findUser = userList.stream().filter(u -> StringUtils.isNotBlank(otherUserId) && otherUserId.equals(u.getUserId())).findFirst().orElse(null);
                        if (findUser != null) {
                            names.add(findUser.getUserName());
                        } else {
                            names.add("");
                        }
                        otherPeopleList.add(otherUserId);
                    }
                    item.setOtherPeople(StringUtils.join(names, ","));

                }
                String itemPeople = item.getItemPeople();
                item.setItemPeopleList(Arrays.asList(itemPeople.split(",")));
                item.setItemPeopleName(NoticeItemPeopleEnum.getNameByFlags(itemPeople, ","));
            }
        }
        return new PagingVO(pageData);
    }

    /**
     * 保存通知
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2022-11-07 14:26
     */
    @Override
    @Transactional
    public Boolean add(NoticeMessageDTO dto) {
        String nodeId = dto.getNodeId();
        NoticeNodeEntity nodeEntity = noticeNodeService.getById(nodeId);
        if (Objects.isNull(nodeEntity)) {
            throw new ServiceException(ApiError.ERROR_95054);
        }
        //检查节点是否存在
        checkIfExist(nodeId, null);
        NoticeMessageEntity messageEntity = new NoticeMessageEntity();
        messageEntity.setNodeId(nodeId);
        List<String> itemPeopleList = dto.getItemPeopleList();
        List<String> otherPeopleList = dto.getOtherPeopleList();
        if (CollectionUtils.isNotEmpty(itemPeopleList)) {
            messageEntity.setItemPeople(String.join(",", itemPeopleList));
        }
        if (CollectionUtils.isNotEmpty(otherPeopleList)) {
            messageEntity.setOtherPeople(String.join(",", otherPeopleList));
        }
        if (CollectionUtils.isEmpty(otherPeopleList) && CollectionUtils.isEmpty(itemPeopleList)) {
            throw new ServiceException(ApiError.ERROR_95055);
        }
        boolean flag = this.save(messageEntity);
        if (flag) {
            //更改节点
            nodeEntity.setExistAdd(IsConstant.YES);
            noticeNodeService.updateById(nodeEntity);
        }
        return flag;
    }


    /**
     * 更改通知
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2022-11-07 14:51
     */
    @Override
    public Boolean updateNotice(NoticeMessageDTO dto) {
        String nodeId = dto.getNodeId();
        NoticeNodeEntity nodeEntity = noticeNodeService.getById(nodeId);
        if (Objects.isNull(nodeEntity)) {
            throw new ServiceException(ApiError.ERROR_95054);
        }
        //检查节点是否存在
        checkIfExist(nodeId, dto.getId());
        NoticeMessageEntity messageEntity = new NoticeMessageEntity();
        messageEntity.setNodeId(nodeId);
        List<String> itemPeopleList = dto.getItemPeopleList();
        List<String> otherPeopleList = dto.getOtherPeopleList();
        if (CollectionUtils.isNotEmpty(itemPeopleList)) {
            messageEntity.setItemPeople(String.join(",", itemPeopleList));
        }
        if (CollectionUtils.isNotEmpty(otherPeopleList)) {
            messageEntity.setOtherPeople(String.join(",", otherPeopleList));
        }
        if (CollectionUtils.isEmpty(otherPeopleList) && CollectionUtils.isEmpty(itemPeopleList)) {
            throw new ServiceException(ApiError.ERROR_95055);
        }
        messageEntity.setId(dto.getId());
        return this.updateById(messageEntity);
    }


    /**
     * 更改状态
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2022-11-07 15:01
     */
    @Override
    public Boolean updateState(UpdateStateDTO dto) {
        NoticeMessageEntity entity = new NoticeMessageEntity();
        entity.setId(dto.getId());
        Boolean stateFlag = dto.getState();
        if (stateFlag) {
            entity.setState(IsConstant.YES);
        } else {
            entity.setState(IsConstant.NO);
        }
        return this.updateById(entity);
    }

    /**
     * 检查节点是否已用过
     *
     * @param nodeId
     * @return void
     * @author yl
     * @date 2022-11-07 14:28
     */
    private void checkIfExist(String nodeId, String id) {
        LambdaQueryWrapper<NoticeMessageEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(NoticeMessageEntity::getNodeId, nodeId);
        if (StringUtils.isNotBlank(id)) {
            queryWrapper.ne(NoticeMessageEntity::getId, id);
        }
        queryWrapper.last("LIMIT 1");
        int count = this.count(queryWrapper);
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_95053);
        }
    }
}




