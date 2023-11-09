package com.erp.server.tms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.tms.dto.LogisticsBillDTO;
import com.erp.model.tms.entity.LogisticsBillDetailEntity;
import com.erp.model.tms.entity.LogisticsBillEntity;
import com.erp.model.wms.dto.FbaDeliveryDetailDTO;
import com.erp.model.wms.entity.FbaDeliveryDetailEntity;
import com.erp.server.tms.mapper.LogisticsBillDetailMapper;
import com.erp.server.tms.service.LogisticsBillDetailService;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.server.tms.service.OperateLogService;
import com.erp.server.tms.service.CommonService;
import com.common.core.exception.ServiceException;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.tms.dto.LogisticsBillDetailDTO;
import java.util.*;
import java.util.stream.Collectors;

import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * 物流单明细表 服务实现类
 * </p>
 *
 * @author lambda
 * @since 2023-11-09
 */
@Slf4j
@Service
public class LogisticsBillDetailServiceImpl extends SuperServiceImpl<LogisticsBillDetailMapper, LogisticsBillDetailEntity> implements LogisticsBillDetailService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private CommonService commonService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean add(LogisticsBillDTO.AddDTO addDTO, String mainId) {
        List<LogisticsBillDetailEntity> list = BeanMapper.copyList(addDTO.getDetailList(), LogisticsBillDetailEntity.class);

        //处理明细数据
        handleData(list, mainId, Boolean.FALSE);
        //批量新增
        return this.saveBatch(list);
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(LogisticsBillDTO.UpdateDTO updateDTO, String mainId) {
        List<LogisticsBillDetailDTO.UpdateDTO> detailList = updateDTO.getDetailList();
        //原明细数据
        List<LogisticsBillDetailEntity> oldList = this.listByMainIds(Arrays.asList(mainId));
        List<String> deleteIds = getDeleteIds(detailList, oldList);
        if (CollectionUtils.isNotEmpty(deleteIds)) {
            List<LogisticsBillDetailEntity> removeList = oldList.stream().filter(obj -> deleteIds.contains(obj.getId())).collect(Collectors.toList());
            //操作日志
            List<Pair<String, String>> pairList = removeList.stream().map(obj -> new Pair<>(obj.getMainId(), obj.getTrackNo())).collect(Collectors.toList());
            operateLogService.batchAddModuleOperateLog("删除了一个运输单【%s】", ModuleTypeEnum.LOGISTICS_BILL.getCode(),pairList,"编辑操作");
            this.removeByIds(deleteIds);
        }

        List<LogisticsBillDetailEntity> list = BeanMapper.copyList(detailList, LogisticsBillDetailEntity.class);

        // 数据处理
        handleData(list, mainId, Boolean.TRUE);

        //新增或修改明细
        return this.saveOrUpdateBatch(list);
    }

    public List<LogisticsBillDetailEntity> listByMainIds(List<String> mainIds) {
        return lambdaQuery().in(LogisticsBillDetailEntity::getMainId, mainIds).list();
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(List<LogisticsBillDetailEntity> logisticsBillDetailEntityList, String mainId, Boolean isUpdate) {
    // TODO 验证数据 & 数据赋值
    }

    /**
     * 查询需要删除的数据
     */
    private List<String> getDeleteIds(List< LogisticsBillDetailDTO.UpdateDTO> newList, List< LogisticsBillDetailEntity > oldList) {
        List<String> newIds = newList.stream().filter(g -> StringUtils.isNotBlank(g.getId())).
                map(LogisticsBillDetailDTO.UpdateDTO::getId).collect(Collectors.toList());
        List<String> oldIds = oldList.stream().map(LogisticsBillDetailEntity
                ::getId).collect(Collectors.toList());
        return oldIds.stream().filter(s -> !newIds.contains(s)).collect(Collectors.toList());
    }
}
