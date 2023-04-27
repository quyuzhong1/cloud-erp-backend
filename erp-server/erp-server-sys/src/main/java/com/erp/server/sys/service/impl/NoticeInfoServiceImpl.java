package com.erp.server.sys.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.UpdateStateDTO;
import com.common.business.service.SuperServiceImpl;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.erp.model.sys.dto.CfgNodeMemberDTO;
import com.erp.model.sys.dto.NoticeDTO;
import com.erp.model.sys.dto.NoticeReceiverDTO;
import com.erp.model.sys.entity.NoticeInfoEntity;
import com.erp.model.sys.entity.NoticeReceiverEntity;
import com.erp.server.sys.mapper.NoticeInfoMapper;
import com.erp.server.sys.service.*;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * <p>
 * 通知表 服务实现类
 * </p>
 *
 * @author lambda
 * @since 2023-04-20
 */
@Service
public class NoticeInfoServiceImpl extends SuperServiceImpl<NoticeInfoMapper, NoticeInfoEntity> implements NoticeInfoService {


    @Resource
    private NoticeReceiverService noticeReceiverService;

    @Resource
    private CfgNodeMemberService cfgNodeMemberService;

    @Resource
    private SysUserInfoService sysUserInfoService;

    @Resource
    private DictBasicService dictBasicService;

    /**
     * 添加通知
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-04-20 20:43
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean add(NoticeDTO.AddDTO dto) {
        NoticeInfoEntity notice = new NoticeInfoEntity();
        String id = IdWorker.getIdStr();
        notice.setId(id);
        notice.setNodeKey(dto.getNodeKey());
        notice.setModule(dto.getModule());
        Boolean result = this.save(notice);
        if (result) {
            //保存接收人信息
            noticeReceiverService.add(id, dto.getCfgNodeList());
        }
        return result;
    }


    /**
     * 编辑通知
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-04-26 15:47
     */
    @Override
    public Boolean edit(NoticeDTO.UpdateDTO dto) {
        String id = dto.getId();
        NoticeInfoEntity notice = this.getById(id);
        if (Objects.isNull(notice)) {
            throw new ServiceException(ApiError.ERROR_9045);
        }
        notice.setNodeKey(dto.getNodeKey());
        notice.setSystem(dto.getSystem());
        //通知接收人
        List<NoticeDTO.CfgNodeDTO> cfgNodeList = dto.getCfgNodeList();
        Boolean result = this.updateById(notice);
        if (result) {
            noticeReceiverService.add(id, cfgNodeList);
        }
        return result;
    }

    @Override
    public NoticeDTO.ViewDTO view(String id) {
        NoticeInfoEntity notice = this.getById(id);
        if (Objects.isNull(notice)) {
            throw new ServiceException(ApiError.ERROR_9045);
        }
        NoticeDTO.ViewDTO view = new NoticeDTO.ViewDTO();
        BeanMapper.copy(notice, view);
        String nodeKey = notice.getNodeKey();
        //根据节点id获取到对应的接收人信息
        List<NoticeReceiverDTO.UpdateDTO> receiverList = noticeReceiverService.listByNoticeId(id);
        List<CfgNodeMemberDTO.ListDTO> cfgList = cfgNodeMemberService.listByNodeKey(nodeKey);
        List<NoticeDTO.CfgNodeDTO> CfgNodeList = new ArrayList<>(cfgList.size());
        for (CfgNodeMemberDTO.ListDTO item : cfgList) {
            NoticeDTO.CfgNodeDTO cfgNodeDTO = new NoticeDTO.CfgNodeDTO();
            String type = item.getType();
            cfgNodeDTO.setType(type);
            NoticeReceiverDTO.AddDTO viewReceiver = new NoticeReceiverDTO.AddDTO();
            viewReceiver.setReceiverType(type);
            List<String> valueList = receiverList.stream().filter(r -> r.getReceiverType().equals(type)).map(NoticeReceiverDTO.UpdateDTO::getReceiverValue).collect(Collectors.toList());
            List<String> valueNameList = receiverList.stream().filter(r -> r.getReceiverType().equals(type)).map(NoticeReceiverDTO.UpdateDTO::getReceiverValueName).collect(Collectors.toList());
            viewReceiver.setReceiverValueList(valueList);
            viewReceiver.setReceiverValueNameList(valueNameList);
            cfgNodeDTO.setReceiver(viewReceiver);
            CfgNodeList.add(cfgNodeDTO);
        }
        view.setCfgNodeList(CfgNodeList);
        return view;
    }


    /**
     * 根据节点key 获取对应数据
     *
     * @param nodeKeys
     * @return java.util.List<com.erp.model.sys.entity.NoticeInfoEntity>
     * @author yl
     * @date 2023-04-26 18:41
     */
    @Override
    public List<NoticeInfoEntity> listByNodeKeys(List<String> nodeKeys) {
        if (CollectionUtils.isEmpty(nodeKeys)) {
            return Collections.emptyList();
        }
        return this.lambdaQuery().in(NoticeInfoEntity::getNodeKey, nodeKeys).list();
    }


    /**
     * 启用 禁用 通知节点
     *
     * @param dto
     * @return
     */
    @Override
    public Boolean updateStatus(UpdateStateDTO dto) {
        //通知id
        String id = dto.getId();
        NoticeInfoEntity notice = this.getById(id);
        if (Objects.isNull(notice)) {
            throw new ServiceException(ApiError.ERROR_9045);
        }
        notice.setDisabled(dto.getState());
        return this.updateById(notice);
    }

    @Override
    public PagingVO<NoticeDTO.PagingViewDTO> paging(PagingDTO<NoticeDTO.PagingParamDTO> dto) {
        NoticeDTO.PagingParamDTO params = dto.getParams();
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        IPage<NoticeDTO.PagingViewDTO> pageData = baseMapper.paging(query, params);
        List<NoticeDTO.PagingViewDTO> list = pageData.getRecords();
        if (CollectionUtils.isEmpty(list)) {
            return new PagingVO(pageData);
        }
        //通知节点id
        List<String> noticeIdList = list.stream().map(NoticeDTO.PagingViewDTO::getId).collect(Collectors.toList());

        List<NoticeReceiverEntity> noticeReceiverList = noticeReceiverService.listByNoticeIds(noticeIdList);
        //项目角色
//        String itemRole = NoticeReceiverEnum.ITEM_ROLE.getCode();
        //其它人员
//        String otherPeople = NoticeReceiverEnum.OTHER_PEOPLE.getCode();
        //人员id
//        List<String> userIds = noticeReceiverList.stream().filter(n -> otherPeople.equals(n.getReceiverType())).map(NoticeReceiverEntity::getReceiverValue).collect(Collectors.toList());
        //项目角色
//        List<String> itemRoleValueList = noticeReceiverList.stream().filter(n -> itemRole.equals(n.getReceiverType())).map(NoticeReceiverEntity::getReceiverValue).collect(Collectors.toList());

//        List<FindUserDTO> userInfoList = sysUserInfoService.getUserListByUserIds(userIds);
//        List<DictBasicEntity> dictBasicList = dictBasicService.listByValues(itemRoleValueList);
        List<String> idFlagList = new ArrayList<>(10);
        for (NoticeDTO.PagingViewDTO item : list) {
            String id = item.getId();
            String type = item.getType();
            String receiverValueName = noticeReceiverList.stream().
                    filter(n -> n.getNoticeId().equals(id) && type.equals(n.getReceiverType())).
                    map(NoticeReceiverEntity::getReceiverValueName).
                    collect(Collectors.joining(","));

            String receiverValue = noticeReceiverList.stream().
                    filter(n -> n.getNoticeId().equals(id) && type.equals(n.getReceiverType())).
                    map(NoticeReceiverEntity::getReceiverValue).
                    collect(Collectors.joining(","));

            item.setReceiverValue(receiverValue);
            item.setReceiverValueName(receiverValueName);
            if (idFlagList.contains(id)) {
                item.setNodeName("");
                item.setNodeKey("");
                item.setDisabled("");
                item.setCreateTime("");
                item.setCreateUserName("");
                item.setUpdateTime("");
                item.setUpdateUserName("");

            }

            idFlagList.add(id);
        }

        return new PagingVO(pageData);
    }
}
