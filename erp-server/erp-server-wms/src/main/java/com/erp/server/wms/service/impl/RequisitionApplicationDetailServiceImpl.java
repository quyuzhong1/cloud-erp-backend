package com.erp.server.wms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.dto.RequisitionApplicationDTO;
import com.erp.model.wms.entity.OverseasDeliveryPlanDetailEntity;
import com.erp.model.wms.entity.RequisitionApplicationDetailEntity;
import com.erp.server.wms.mapper.RequisitionApplicationDetailMapper;
import com.erp.server.wms.service.RequisitionApplicationDetailService;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.server.wms.service.OperateLogService;
import com.erp.server.wms.service.CommonService;
import com.common.core.exception.ServiceException;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.wms.dto.RequisitionApplicationDetailDTO;
import java.util.*;
import java.util.stream.Collectors;

import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * 要货申请单明细表 服务实现类
 * </p>
 *
 * @author Luo_WG
 * @since 2023-11-16
 */
@Slf4j
@Service
public class RequisitionApplicationDetailServiceImpl extends SuperServiceImpl<RequisitionApplicationDetailMapper, RequisitionApplicationDetailEntity> implements RequisitionApplicationDetailService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private CommonService commonService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void add(RequisitionApplicationDTO.AddDTO addDTO, String mainId) {
        List<RequisitionApplicationDetailEntity> list = BeanMapper.copyList(addDTO.getDetailList(), RequisitionApplicationDetailEntity.class);

        // 数据处理
        handleData(list, mainId, Boolean.FALSE);

        log.info("开始新增要货申请单明细单");
        boolean save = super.saveBatch(list);
        if(!save) {
            throw new ServiceException("要货申请单明细单保存失败");
        }

    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void update(RequisitionApplicationDTO.UpdateDTO updateDTO, String mainId) {
        List<RequisitionApplicationDetailEntity> list = BeanMapper.copyList(updateDTO.getDetailList(), RequisitionApplicationDetailEntity.class);

        // 数据处理
        handleData(list, mainId, Boolean.FALSE);

        boolean save = super.updateBatchById(list);
        if(!save) {
            throw new ServiceException("要货申请单明细单保存失败");
        }
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(List<RequisitionApplicationDetailEntity> list, String mainId, Boolean isUpdate) {
        //需要新增的数据
        List<RequisitionApplicationDetailEntity> addList = list.stream().filter(c -> StringUtils.isBlank(c.getId())).collect(Collectors.toList());

        for (RequisitionApplicationDetailEntity requisitionApplicationDetailEntity : list) {
            requisitionApplicationDetailEntity.setMainId(mainId);

            //校验是否是修改，如果是就新增修改日志
            if (StringUtils.isNotBlank(requisitionApplicationDetailEntity.getId())) {
                RequisitionApplicationDetailEntity old = list.stream().filter(obj -> obj.getId().equals(requisitionApplicationDetailEntity.getId())).findFirst().orElse(null);
                if (ObjectUtils.isEmpty(old)) {
                    throw new ServiceException(ApiError.ERROR_NOT_REQUISITION_APPLICATION);
                }
                operateLogService.addModuleOperateLogByObj(old, requisitionApplicationDetailEntity, ModuleTypeEnum.REQUISITION_APPLICATION.getCode(), requisitionApplicationDetailEntity.getId(),"", String.format("【%s】", old.getSkuNo()));
            }
        }
        //添加操作日志
        if (CollectionUtils.isNotEmpty(addList) && isUpdate) {
            List<Pair<String, String>> addPairList = addList.stream().map(obj -> new Pair<>(mainId, obj.getSkuNo())).collect(Collectors.toList());
            operateLogService.batchAddModuleOperateLog("添加了一个SKU【%s】", ModuleTypeEnum.REQUISITION_APPLICATION.getCode(), addPairList, "编辑操作");
        }
    }
}
