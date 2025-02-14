package com.erp.server.sys.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.scm.dto.PurchaseOrderDetailDTO;
import com.erp.model.scm.entity.PurchaseOrderDetailEntity;
import com.erp.model.sys.dto.CfgNoticeDTO;
import com.erp.model.sys.entity.CfgNoticeDetailEntity;
import com.erp.model.wms.enums.CfgVirtualNoticeObjectTypeEnum;
import com.erp.model.wms.enums.CfgVirtualNoticeTargetTypeEnum;
import com.erp.server.sys.mapper.CfgNoticeDetailMapper;
import com.erp.server.sys.service.CfgNoticeDetailService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.erp.server.sys.service.CfgNoticeService;
import com.erp.server.sys.service.OperateLogService;
import com.erp.server.sys.service.CommonService;
import com.common.core.exception.ServiceException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.sys.dto.CfgNoticeDetailDTO;
import java.util.*;
import java.util.stream.Collectors;

import com.common.core.utils.*;
import com.common.core.enums.ApiError;

import javax.annotation.Resource;

import static com.erp.rpc.sys.feign.aspect.SysLoggingAspect.handleData;

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

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(CfgNoticeDetailDTO.AddDTO addDTO) {
        CfgNoticeDetailEntity cfgNoticeDetailEntity = new CfgNoticeDetailEntity();
        BeanMapperUtils.copy(addDTO, cfgNoticeDetailEntity);

        // 数据处理
        handleData(cfgNoticeDetailEntity);

        log.info("开始新增通知配置明细单");
        boolean save = super.save(cfgNoticeDetailEntity);
        if(!save) {
            throw new ServiceException("通知配置明细单保存失败");
        }
        return new BaseResultDTO.AddDTO(cfgNoticeDetailEntity.getId(), cfgNoticeDetailEntity.getId());
    }

    @Override
    public void addOrUpdateNoticeObjectList(List<CfgNoticeDTO.NoticeObjectDTO> noticeObjectDTOList, String id) {
        List<CfgNoticeDetailEntity> oldList = this.listByMainIdListAndType(Collections.singletonList(id), Arrays.asList(CfgVirtualNoticeObjectTypeEnum.NOTICE_USER.getCode(), CfgVirtualNoticeObjectTypeEnum.NOTICE_GROUP.getCode()));
        List<String> deleteIds = getDeleteIds(noticeObjectDTOList, oldList);
        if (CollUtil.isNotEmpty(deleteIds)) {
            this.removeByIds(deleteIds);
        }
        List<CfgNoticeDetailEntity> newList = noticeObjectDTOList.stream().map(obj -> {
            CfgNoticeDetailEntity cfgNoticeDetailEntity = new CfgNoticeDetailEntity();
            cfgNoticeDetailEntity.setMainId(id);
            cfgNoticeDetailEntity.setNoticeType(obj.getNoticeType());
            cfgNoticeDetailEntity.setNoticeValueJson(JSONUtil.)
            return cfgNoticeDetailEntity;
        }).collect(Collectors.toList());
        this.saveOrUpdateBatch(newList);
    }

    @Override
    public void addNOrUpdateoticeTimeList(List<CfgNoticeDTO.NoticeTimeDTO> noticeTimeDTOList, String id) {

    }

    /**
     * 查询需要删除的数据
     */
    private List<String> getDeleteIds(List<CfgNoticeDTO.NoticeObjectDTO> newList, List<CfgNoticeDetailEntity> oldList) {
        List<String> newIds = newList.stream().filter(g -> StringUtils.isNotBlank(g.getId())).
                map(CfgNoticeDTO.NoticeObjectDTO::getId).collect(Collectors.toList());
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
