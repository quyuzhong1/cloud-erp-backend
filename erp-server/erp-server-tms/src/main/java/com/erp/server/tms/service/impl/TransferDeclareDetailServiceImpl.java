package com.erp.server.tms.service.impl;


import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.erp.model.tms.dto.TransferDeclareDTO;
import com.erp.model.tms.dto.TransferDeclareDetailDTO;
import com.erp.model.tms.entity.LogisticsChannelEntity;
import com.erp.model.tms.entity.TransferDeclareDetailEntity;
import com.erp.server.tms.mapper.TransferDeclareDetailMapper;
import com.erp.server.tms.service.CommonService;
import com.erp.server.tms.service.LogisticsChannelService;
import com.erp.server.tms.service.OperateLogService;
import com.erp.server.tms.service.TransferDeclareDetailService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 中转报关详情 服务实现类
 * </p>
 *
 * @author Luo_WG
 * @since 2024-01-19
 */
@Slf4j
@Service
public class TransferDeclareDetailServiceImpl extends SuperServiceImpl<TransferDeclareDetailMapper, TransferDeclareDetailEntity> implements TransferDeclareDetailService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private CommonService commonService;
    @Autowired
    private LogisticsChannelService logisticsChannelService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void add(TransferDeclareDTO.AddDTO addDTO, String mainId) {
        List<TransferDeclareDetailEntity> transferDeclareDetailEntities = BeanMapper.copyList(addDTO.getDetailList(), TransferDeclareDetailEntity.class);

        // 数据处理
        handleData(transferDeclareDetailEntities, mainId);

        //批量新增
        boolean save = this.saveBatch(transferDeclareDetailEntities);

        log.info("开始新增中转报关详情");
        if(!save) {
            throw new ServiceException("中转报关详情保存失败");
        }
    }

    /**
     * 修改
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void update(TransferDeclareDTO.UpdateDTO updateDTO, String mainId) {
        List<TransferDeclareDetailEntity> transferDeclareDetailEntities = BeanMapper.copyList(updateDTO.getDetailList(), TransferDeclareDetailEntity.class);

        // 数据处理
        handleData(transferDeclareDetailEntities, mainId);

        //批量新增
        boolean save = this.saveOrUpdateBatch(transferDeclareDetailEntities);

        log.info("开始修改中转报关详情");
        if(!save) {
            throw new ServiceException("中转报关详情修改失败");
        }
    }

    @Override
    public List<TransferDeclareDetailDTO.ViewDTO> viewDetailList(TransferDeclareDTO.ViewDetailParamDTO dto) {
        return baseMapper.viewDetailList(dto);
    }


    /**
     * 新增修改处理数据
     */
    private void handleData(List<TransferDeclareDetailEntity> list, String mainId) {
        List<String> logisticsChannelIds = list.stream().map(req -> req.getLogisticsChannelId()).distinct().collect(Collectors.toList());
        List<LogisticsChannelEntity> logisticsChannelEntities = new ArrayList<>();

        //查询渠道信息
        if (CollectionUtil.isNotEmpty(logisticsChannelIds)) {
            logisticsChannelEntities = logisticsChannelService.listByIds(logisticsChannelIds);
        }

        for (TransferDeclareDetailEntity transferDeclareDetailEntity : list) {
            transferDeclareDetailEntity.setMainId(mainId);

            //渠道名称
            LogisticsChannelEntity channelEntity = logisticsChannelEntities.stream().filter(req -> transferDeclareDetailEntity.getLogisticsChannelId().equals(req.getId())).findFirst().orElse(null);
            if (ObjectUtil.isNotEmpty(channelEntity)) {
                transferDeclareDetailEntity.setLogisticsChannelName(channelEntity.getName());
            }
        }
    }



    public List<TransferDeclareDetailEntity> listByMainIds(List<String> mainIds) {
        if (CollectionUtil.isEmpty(mainIds)) {
            return Collections.emptyList();
        }
        return lambdaQuery().in(TransferDeclareDetailEntity::getMainId, mainIds).list();
    }
}
