package com.erp.server.tms.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.constant.SqlConstants;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.tms.dto.TransferDeclareDTO;
import com.erp.model.tms.dto.TransferDeclareDetailDTO;
import com.erp.model.tms.entity.LogisticsChannelEntity;
import com.erp.model.tms.entity.TransferDeclareDetailEntity;
import com.erp.model.tms.enums.TransferDeclareUploadStatusEnum;
import com.erp.model.tms.enums.TransferLogisticsStatusEnum;
import com.erp.rpc.oms.feign.SoB2cFeign;
import com.erp.rpc.wms.feign.SoOutstockFeign;
import com.erp.server.tms.mapper.TransferDeclareDetailMapper;
import com.erp.server.tms.service.LogisticsChannelService;
import com.erp.server.tms.service.OperateLogService;
import com.erp.server.tms.service.TransferDeclareDetailService;
import com.erp.server.tms.service.TransferDeclareProductService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
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
    @Resource
    private OperateLogService operateLogService;
    @Resource
    private LogisticsChannelService logisticsChannelService;
    @Resource
    private SoB2cFeign soB2cFeign;
    @Resource
    private TransferDeclareProductService transferDeclareProductService;
    @Resource
    private SoOutstockFeign soOutstockFeign;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void add(TransferDeclareDTO.AddDTO addDTO, String mainId) {
        List<TransferDeclareDetailEntity> transferDeclareDetailEntities = BeanMapper.copyList(addDTO.getDetailList(), TransferDeclareDetailEntity.class);

        // 数据处理
        handleData(transferDeclareDetailEntities, mainId);

        //批量新增
        boolean save = this.saveBatch(transferDeclareDetailEntities);
        //拆分订单sku并新增报关明细
        transferDeclareProductService.saveOrUpdateTransferDeclareProducts(transferDeclareDetailEntities);
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

        //原明细数据
        List<TransferDeclareDetailEntity> oldList = this.listByMainIds(Arrays.asList(mainId));
        List<String> deleteIds = getDeleteIds(updateDTO.getDetailList(), oldList);
        if (deleteIds.size() >= oldList.size()) {
            throw new ServiceException(ApiError.PLEASE_KEEP_LEAST_ONE_DATA);
        }

        if (CollectionUtils.isNotEmpty(deleteIds)) {

            List<TransferDeclareDetailEntity> removeList = oldList.stream().filter(obj -> deleteIds.contains(obj.getId())).collect(Collectors.toList());
            //操作日志
            List<Pair<String, String>> pairList = removeList.stream().map(obj -> new Pair<>(obj.getMainId(), obj.getSoCode())).collect(Collectors.toList());
            operateLogService.batchAddModuleOperateLog("删除了一个订单【%s】", ModuleTypeEnum.TRANSFER_DECLARE.getCode(), pairList, "编辑操作");
            this.removeByIds(deleteIds);
            //删除明细对应的sku拆分记录
            transferDeclareProductService.removeByDeclareDetailIds(deleteIds);

        }

        // 数据处理
        handleData(transferDeclareDetailEntities, mainId);

        //批量新增
        boolean save = this.saveOrUpdateBatch(transferDeclareDetailEntities);
        transferDeclareProductService.saveOrUpdateTransferDeclareProducts(transferDeclareDetailEntities);
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
    public Boolean updateOrderUploadStatus(String id, String status, String shippingOrderNo, String failureReason) {
        return lambdaUpdate()
                .eq(TransferDeclareDetailEntity::getId, id)
                .set(TransferDeclareDetailEntity::getOrderUploadStatus, status)
                .set(TransferDeclareDetailEntity::getShippingOrderNo, shippingOrderNo)
                .set(TransferDeclareDetailEntity::getFailureReason, failureReason)
                .update();
    }

    @Override
    public Boolean updateTransferStatus(String id, String transferStatus) {
        return lambdaUpdate()
                .eq(TransferDeclareDetailEntity::getId, id)
                .set(TransferDeclareDetailEntity::getTransferStatus, transferStatus)
                .update();
    }

    @Override
    public List<TransferDeclareDetailEntity> listWaitSyncTransferStatus() {
        return lambdaQuery().eq(TransferDeclareDetailEntity::getOrderUploadStatus, TransferDeclareUploadStatusEnum.UPLOAD_SUCCESS.getCode())
                .ne(TransferDeclareDetailEntity::getTransferStatus, TransferLogisticsStatusEnum.DELETED.getCode())
                .ne(TransferDeclareDetailEntity::getTransferStatus, TransferLogisticsStatusEnum.OUTSTOCK.getCode())
                .ne(TransferDeclareDetailEntity::getTransferStatus, TransferLogisticsStatusEnum.SIGNED.getCode()).list();
    }

    @Override
    public void deleteByMainIds(List<String> mainIds) {
        if (CollectionUtils.isEmpty(mainIds)) {
            return;
        }

        //校验有上传成功的单据不能删除
        List<TransferDeclareDetailEntity> detailEntities = this.listByMainIds(mainIds);

        //删除明细
        lambdaUpdate().in(TransferDeclareDetailEntity::getMainId, mainIds).remove();

        //删除明细对应的sku拆分记录
        List<String> ids = detailEntities.stream().map(req -> req.getId()).collect(Collectors.toList());
        transferDeclareProductService.removeByDeclareDetailIds(ids);
    }


    /**
     * 根据销售订单id查询
     * @param soId
     * @return
     */
    @Override
    public TransferDeclareDetailEntity getBySoId(String soId) {
        return this.lambdaQuery().eq(TransferDeclareDetailEntity::getSoId, soId).last(SqlConstants.LIMIT_1).one();
    }

//    @Override
//    public Boolean updateOutstockStatus(TransferDeclareDTO.UpdateOutstockStatusDTO dto) {
//        if (CollectionUtils.isEmpty(dto.getSoIds())) {
//            return Boolean.FALSE;
//        }
//
//        return lambdaUpdate()
//                .set(TransferDeclareDetailEntity::getOutstockStatus, dto.getStatus())
//                .in(TransferDeclareDetailEntity::getSoId, dto.getSoIds())
//                .update();
//    }

    @Override
    public List<TransferDeclareDetailEntity> listByLogisticsChannelIdList(List<String> logisticsChannelIdList) {
        if (CollectionUtils.isEmpty(logisticsChannelIdList)) {
            return Collections.EMPTY_LIST;
        }
        return lambdaQuery().in(TransferDeclareDetailEntity::getLogisticsChannelId,logisticsChannelIdList).list();
    }

    @Override
    public List<TransferDeclareDetailEntity> listBySoCodeList(List<String> soCodeList) {
        if (CollectionUtils.isEmpty(soCodeList)) {
            return Collections.EMPTY_LIST;
        }
        return lambdaQuery().in(TransferDeclareDetailEntity::getSoCode,soCodeList).list();
    }

    /**
     * 新增修改处理数据
     */
    private void handleData(List<TransferDeclareDetailEntity> list, String mainId) {
        List<String> logisticsChannelIds = list.stream().map(req -> req.getLogisticsChannelId()).distinct().collect(Collectors.toList());
        List<LogisticsChannelEntity> logisticsChannelEntities = new ArrayList<>();

        //查询渠道信息
        if (CollUtil.isNotEmpty(logisticsChannelIds)) {
            logisticsChannelEntities = logisticsChannelService.listByIds(logisticsChannelIds);
        }

        //校验是否存在订单
        List<String> soCodeIds = list.stream().filter(req -> StringUtils.isBlank(req.getId())).map(req -> req.getSoCode()).distinct().collect(Collectors.toList());
        List<TransferDeclareDetailEntity> detailEntityList = this.listBySoCodeList(soCodeIds);
        if (CollectionUtils.isNotEmpty(detailEntityList)) {
            throw new ServiceException(ApiError.TRANSFER_DECLARE_SO_EXISTS, detailEntityList.get(0).getSoCode());
        }


        List<String> soIds = list.stream().map(req -> req.getSoId()).collect(Collectors.toList());
        List<SoB2cEntity> soB2cEntityList = soB2cFeign.listByIds(soIds);

        for (TransferDeclareDetailEntity transferDeclareDetailEntity : list) {
            transferDeclareDetailEntity.setMainId(mainId);
//            //销售订单
//            SoB2cEntity soB2cEntity = soB2cEntityList.stream()
//                    .filter(req -> req.getId().equals(transferDeclareDetailEntity.getSoId())
//                            && SoB2cBillStatusEnum.ENUM_SHIPPED.getCode().equals(req.getBillStatus()))
//                    .findFirst().orElse(null);
//            if (ObjectUtil.isNotEmpty(soB2cEntity)) {
//                transferDeclareDetailEntity.setOutstockStatus(TransferOutstockStatusEnum.OUTSTOCK.getCode());
//            } else {
//                transferDeclareDetailEntity.setOutstockStatus(TransferOutstockStatusEnum.UN_OUTSTOCK.getCode());
//            }

            //渠道名称
            LogisticsChannelEntity channelEntity = logisticsChannelEntities.stream().filter(req -> transferDeclareDetailEntity.getLogisticsChannelId().equals(req.getId())).findFirst().orElse(null);
            if (ObjectUtil.isNotEmpty(channelEntity)) {
                transferDeclareDetailEntity.setLogisticsChannelName(channelEntity.getName());
            }
        }
    }



    @Override
    public List<TransferDeclareDetailEntity> listByMainIds(List<String> mainIds) {
        if (CollectionUtil.isEmpty(mainIds)) {
            return Collections.emptyList();
        }
        return lambdaQuery().in(TransferDeclareDetailEntity::getMainId, mainIds).list();
    }
    /**
     * 用于分页查询 子查询关联过滤
     * @param mainIds
     * @param params
     * @return
     */
    @Override
    public List<TransferDeclareDetailEntity> listByCondition(List<String> mainIds, TransferDeclareDTO.PagingParamDTO params) {
        if (CollectionUtils.isEmpty(mainIds)){
            return Collections.emptyList();
        }
        return baseMapper.listByCondition(mainIds,params);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateTransferStatusByBatch(List<TransferDeclareDTO.UpdateForcastStatusDTO> list) {
        for (TransferDeclareDTO.UpdateForcastStatusDTO updateForcastStatusDTO : list) {
            lambdaUpdate()
                    .eq(TransferDeclareDetailEntity::getSoId, updateForcastStatusDTO.getSoId())
                    .set(TransferDeclareDetailEntity::getTransferStatus, updateForcastStatusDTO.getStatus())
                    .update();
        }
        return Boolean.TRUE;
    }
}
