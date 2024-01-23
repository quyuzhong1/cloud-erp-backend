package com.erp.server.tms.service.impl;


import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.erp.model.oms.dto.SkuMappingDTO;
import com.erp.model.oms.entity.RuleConditionEntity;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.tms.dto.TransferDeclareDTO;
import com.erp.model.tms.entity.TransferDeclareDetailEntity;
import com.erp.model.wms.entity.FirstMileDeliveryDetailEntity;
import com.erp.server.tms.mapper.TransferDeclareDetailMapper;
import com.erp.server.tms.service.TransferDeclareDetailService;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.server.tms.service.OperateLogService;
import com.erp.server.tms.service.CommonService;
import com.common.core.exception.ServiceException;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.tms.dto.TransferDeclareDetailDTO;
import java.util.*;
import java.util.stream.Collectors;

import com.common.core.utils.*;
import com.common.core.enums.ApiError;
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

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void add(TransferDeclareDTO.AddDTO addDTO, String mainId) {
        List<TransferDeclareDetailEntity> transferDeclareDetailEntities = BeanMapper.copyList(addDTO.getDetailList(), TransferDeclareDetailEntity.class);

        // 数据处理
        handleData(transferDeclareDetailEntities, mainId, Boolean.FALSE);

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
        handleData(transferDeclareDetailEntities, mainId, Boolean.TRUE);

        //批量新增
        boolean save = this.saveOrUpdateBatch(transferDeclareDetailEntities);

        log.info("开始修改中转报关详情");
        if(!save) {
            throw new ServiceException("中转报关详情修改失败");
        }
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(List<TransferDeclareDetailEntity> list, String mainId, Boolean isUpdate) {

    }


    public List<TransferDeclareDetailEntity> listByMainIds(List<String> mainIds) {
        if (CollectionUtil.isEmpty(mainIds)) {
            return Collections.emptyList();
        }
        return lambdaQuery().in(TransferDeclareDetailEntity::getMainId, mainIds).list();
    }
}
