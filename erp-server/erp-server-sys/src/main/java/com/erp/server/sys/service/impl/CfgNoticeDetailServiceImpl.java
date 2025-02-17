package com.erp.server.sys.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONUtil;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.utils.ApplicationContextUtils;
import com.common.core.exception.ServiceException;
import com.erp.model.sys.dto.CfgNoticeDTO;
import com.erp.model.sys.entity.CfgNoticeDetailEntity;
import com.erp.model.wms.enums.CfgVirtualNoticeObjectTypeEnum;
import com.erp.model.wms.enums.CfgVirtualNoticeTimeTypeEnum;
import com.erp.server.sys.mapper.CfgNoticeDetailMapper;
import com.erp.server.sys.service.CfgNoticeDetailService;
import com.erp.server.sys.service.CfgNoticeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 通知配置明细表 服务实现类
 * </p>
 *
 * @author will
 * @since 2025-02-13
 */
@Slf4j
@Service
public class CfgNoticeDetailServiceImpl extends SuperServiceImpl<CfgNoticeDetailMapper, CfgNoticeDetailEntity> implements CfgNoticeDetailService {

    @Resource
    private CfgNoticeService cfgNoticeService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addOrUpdateNoticeObjectList(List<CfgNoticeDTO.NoticeObjectDTO> noticeObjectDTOList, String id) {
        List<CfgNoticeDetailEntity> oldList = this.listByMainIdListAndType(Collections.singletonList(id), Arrays.asList(CfgVirtualNoticeObjectTypeEnum.NOTICE_USER.getCode(), CfgVirtualNoticeObjectTypeEnum.NOTICE_GROUP.getCode()));
        List<String> newIds = noticeObjectDTOList.stream().filter(g -> StringUtils.isNotBlank(g.getId())).
                map(CfgNoticeDTO.NoticeObjectDTO::getId).collect(Collectors.toList());
        List<String> deleteIds = getDeleteIds(newIds, oldList);
        if (CollUtil.isNotEmpty(deleteIds)) {
            ApplicationContextUtils.getBean(CfgNoticeDetailServiceImpl.class).removeByIds(deleteIds);
        }
        List<CfgNoticeDetailEntity> newList = noticeObjectDTOList.stream().map(obj -> {
            CfgNoticeDetailEntity cfgNoticeDetailEntity = new CfgNoticeDetailEntity();
            cfgNoticeDetailEntity.setMainId(id);
            cfgNoticeDetailEntity.setNoticeType(obj.getNoticeType());
            cfgNoticeDetailEntity.setNoticeValueJson(JSONUtil.parseObj(obj));
            return cfgNoticeDetailEntity;
        }).collect(Collectors.toList());
        ApplicationContextUtils.getBean(CfgNoticeDetailServiceImpl.class).saveOrUpdateBatch(newList);
    }

    @Override
    public void addNOrUpdateoticeTimeList(List<CfgNoticeDTO.NoticeTimeDTO> noticeTimeDTOList, String id) {
        List<CfgNoticeDetailEntity> oldList = this.listByMainIdListAndType(Collections.singletonList(id), Arrays.asList(CfgVirtualNoticeObjectTypeEnum.NOTICE_DAY.getCode(), CfgVirtualNoticeObjectTypeEnum.NOTICE_WEEK.getCode()));
        List<String> newIds = noticeTimeDTOList.stream().filter(g -> StringUtils.isNotBlank(g.getId())).
                map(CfgNoticeDTO.NoticeTimeDTO::getId).collect(Collectors.toList());
        List<String> deleteIds = getDeleteIds(newIds, oldList);
        if (CollUtil.isNotEmpty(deleteIds)) {
            ApplicationContextUtils.getBean(CfgNoticeDetailServiceImpl.class).removeByIds(deleteIds);
        }
        List<CfgNoticeDetailEntity> newList = noticeTimeDTOList.stream().map(obj -> {
            if (CfgVirtualNoticeTimeTypeEnum.NOTICE_WEEK.getCode().equals(obj.getNoticeType())) {
                if (CharSequenceUtil.isBlank(obj.getWeekOption())) {
                    throw new ServiceException("按周发送通知，周选项不能为空");
                }
            } else {
                obj.setWeekOption("");
            }
            CfgNoticeDetailEntity cfgNoticeDetailEntity = new CfgNoticeDetailEntity();
            cfgNoticeDetailEntity.setMainId(id);
            cfgNoticeDetailEntity.setNoticeType(obj.getNoticeType());
            cfgNoticeDetailEntity.setNoticeValueJson(JSONUtil.parseObj(obj));
            return cfgNoticeDetailEntity;
        }).collect(Collectors.toList());
        ApplicationContextUtils.getBean(CfgNoticeDetailServiceImpl.class).saveOrUpdateBatch(newList);
    }

    /**
     * 查询需要删除的数据
     */
    private List<String> getDeleteIds(List<String> newIds, List<CfgNoticeDetailEntity> oldList) {
        List<String> oldIds = oldList.stream().map(CfgNoticeDetailEntity::getId).collect(Collectors.toList());
        return oldIds.stream().filter(s -> !newIds.contains(s)).collect(Collectors.toList());
    }

    @Override
    public List<CfgNoticeDetailEntity> listByMainIdList(List<String> idList) {
        if (CollUtil.isEmpty(idList)) {
            return Collections.emptyList();
        }
        return lambdaQuery().in(CfgNoticeDetailEntity::getMainId, idList).list();
    }

    /**
     * 根据主表id和类型查询
     * @Auther will
     * @Date 2025/2/14 15:13
     * @param idList
     * @return List<CfgNoticeDetailEntity>
     */
    public List<CfgNoticeDetailEntity> listByMainIdListAndType(List<String> idList,List<String> noticeTypeList) {
        if (CollUtil.isEmpty(idList) || CollUtil.isEmpty(noticeTypeList)) {
            return Collections.emptyList();
        }
        return lambdaQuery()
                .in(CfgNoticeDetailEntity::getMainId, idList)
                .in(CfgNoticeDetailEntity::getNoticeType, noticeTypeList)
                .list();
    }
}
