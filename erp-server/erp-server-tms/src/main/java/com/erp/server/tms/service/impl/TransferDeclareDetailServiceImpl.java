package com.erp.server.tms.service.impl;


import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.erp.model.oms.enums.TransferStatusEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.tms.dto.TransferDeclareDTO;
import com.erp.model.tms.dto.TransferDeclareDetailDTO;
import com.erp.model.tms.entity.LogisticsChannelEntity;
import com.erp.model.tms.entity.TransferDeclareDetailEntity;
import com.erp.model.tms.enums.TransferDeclareUploadStatusEnum;
import com.erp.rpc.oms.feign.SoB2cFeign;
import com.erp.server.tms.mapper.TransferDeclareDetailMapper;
import com.erp.server.tms.service.CommonService;
import com.erp.server.tms.service.LogisticsChannelService;
import com.erp.server.tms.service.OperateLogService;
import com.erp.server.tms.service.TransferDeclareDetailService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
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
    @Autowired
    private SoB2cFeign soB2cFeign;

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

        //更新订单中转状态
        List<String> soIds = addDTO.getDetailList().stream().map(req -> req.getSoId()).distinct().collect(Collectors.toList());
        soB2cFeign.updateTransferStatusBatch(soIds, TransferStatusEnum.ALREADY.getCode());
    }

    /**
     * 修改
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void update(TransferDeclareDTO.UpdateDTO updateDTO, String mainId) {
        List<TransferDeclareDetailEntity> transferDeclareDetailEntities = BeanMapper.copyList(updateDTO.getDetailList(), TransferDeclareDetailEntity.class);

        //原明细数据
        List<TransferDeclareDetailEntity> oldList = this.listByMainIds(Arrays.asList(mainId));
        List<String> deleteIds = getDeleteIds(updateDTO.getDetailList(), oldList);
        if (CollectionUtils.isNotEmpty(deleteIds)) {
            List<TransferDeclareDetailEntity> detailEntities = this.listByIds(deleteIds);
            long count = detailEntities.stream().filter(req -> TransferDeclareUploadStatusEnum.UPLOAD_SUCCESS.getCode().equals(req.getOrderUploadStatus())).count();
            if (count > 0) {
                throw new ServiceException(ApiError.UPLOAD_SUCCESS_NOT_DELETE);
            }

            List<TransferDeclareDetailEntity> removeList = oldList.stream().filter(obj -> deleteIds.contains(obj.getId())).collect(Collectors.toList());
            //操作日志
            List<Pair<String, String>> pairList = removeList.stream().map(obj -> new Pair<>(obj.getMainId(), obj.getSoCode())).collect(Collectors.toList());
            operateLogService.batchAddModuleOperateLog("删除了一个订单【%s】", ModuleTypeEnum.TRANSFER_DECLARE.getCode(), pairList, "编辑操作");
            this.removeByIds(deleteIds);
        }

        // 数据处理
        handleData(transferDeclareDetailEntities, mainId);

        //批量新增
        boolean save = this.saveOrUpdateBatch(transferDeclareDetailEntities);

        log.info("开始修改中转报关详情");
        if(!save) {
            throw new ServiceException("中转报关详情修改失败");
        }
    }

    private List<String> getDeleteIds(List<TransferDeclareDetailDTO.UpdateDTO> newList, List<TransferDeclareDetailEntity> oldList) {
        List<String> newIds = newList.stream().filter(g -> StringUtils.isNotBlank(g.getId())).
                map(TransferDeclareDetailDTO.UpdateDTO::getId).collect(Collectors.toList());
        List<String> oldIds = oldList.stream().map(TransferDeclareDetailEntity
                ::getId).collect(Collectors.toList());
        return oldIds.stream().filter(s -> !newIds.contains(s)).collect(Collectors.toList());
    }

    @Override
    public List<TransferDeclareDetailDTO.ViewDTO> viewDetailList(TransferDeclareDTO.ViewDetailParamDTO dto) {
        return baseMapper.viewDetailList(dto);
    }

    @Override
    public Boolean updateOrderUploadStatus(String id, String status) {
        return lambdaUpdate().set(TransferDeclareDetailEntity::getId, id).set(TransferDeclareDetailEntity::getOrderUploadStatus, status).update();
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
