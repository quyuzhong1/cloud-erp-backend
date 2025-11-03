package com.erp.server.workflow.service.impl;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.MathUtil;
import com.erp.model.workflow.dto.ApproveTaskDetailDTO;
import com.erp.model.workflow.dto.CfgQueryOptionDTO;
import com.erp.model.workflow.entity.ApproveTaskDetailEntity;
import com.erp.model.workflow.entity.ApproveTaskInfoEntity;
import com.erp.model.workflow.enums.CfgQueryOptionUseTypeEnum;
import com.erp.server.workflow.convert.ApproveTaskDetailConvert;
import com.erp.server.workflow.mapper.ApproveTaskDetailMapper;
import com.erp.server.workflow.service.*;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <p>
 * 三方生成查询明细 服务实现类
 * </p>
 *
 * @author will
 * @since 2025-05-27
 */
@Slf4j
@Service
public class ApproveTaskDetailServiceImpl extends SuperServiceImpl<ApproveTaskDetailMapper, ApproveTaskDetailEntity> implements ApproveTaskDetailService {
    @Autowired
    private OperateLogService operateLogService;
    
    @Resource
    private ApproveTaskInfoService approveTaskInfoService;
    
    @Resource
    private CfgQueryOptionService cfgQueryOptionService;

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(List<ApproveTaskDetailDTO.AddDTO> detailList,String mainId) {
        List<ApproveTaskDetailEntity> approveTaskDetailLis  = BeanUtil.copyToList(detailList, ApproveTaskDetailEntity.class);
        for (ApproveTaskDetailEntity detailLi : approveTaskDetailLis) {
            detailLi.setMianId(mainId);
        }
        log.info("开始新增三方生成查询明细");
        boolean save = super.saveBatch(approveTaskDetailLis);
        if(!save) {
            throw new ServiceException("三方生成查询明细保存失败");
        }
        return new BaseResultDTO.AddDTO(mainId,mainId);
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(List<ApproveTaskDetailDTO.UpdateDTO> detailList, String mainId) {
        List<ApproveTaskDetailEntity> approveTaskDetailLis  = ApproveTaskDetailConvert.INSTANCE.approveTaskDetailUpdateToEntity(detailList);
        //处理字段信息
        handleErpField(approveTaskDetailLis,mainId);
        log.info("编辑 开始修改三方生成查询明细数据，主表id：【{}】", mainId);
        boolean save = super.updateBatchById(approveTaskDetailLis);
        if(!save) {
            throw new ServiceException("三方生成查询明细保存失败");
        }
        return Boolean.TRUE;
    }
    /**
     * 处理加校验字段信息
     * @author will
     * @date 2025/10/15 17:35
     * @param approveTaskDetailLis
     * @param mainId
     * @return void
     */
    private void handleErpField ( List<ApproveTaskDetailEntity> approveTaskDetailLis,String mainId) {
        ApproveTaskInfoEntity entity = approveTaskInfoService.getById(mainId);
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException(ApiError.TIME_NOT_NULL,"三方生成查询信息");
        }
        //明细信息
        List<ApproveTaskDetailEntity> approveTaskDetailList = this.listByMainId(mainId);
        if (ObjectUtil.isEmpty(approveTaskDetailList)) {
            throw new ServiceException(ApiError.TIME_NOT_NULL,"三方生成查询明细信息");
        }
        List<CfgQueryOptionDTO.ViewDTO> optionList = cfgQueryOptionService.getSystemfield(entity.getBussinessKey(), CfgQueryOptionUseTypeEnum.ALL_DATA.getCode());
        if (CollUtil.isEmpty(optionList)) {
            throw new ServiceException(ApiError.TIME_NOT_NULL,"字段映射信息");
        }

        Map<String, CfgQueryOptionDTO.ViewDTO> map = optionList.stream().collect(Collectors.toMap(CfgQueryOptionDTO.ViewDTO::getUniqueCode,Function.identity()));

        //保存数据可按照系统字段和实体编码进行校验
        Map<String, List<ApproveTaskDetailEntity>> detailMap = approveTaskDetailLis.stream().collect(Collectors.groupingBy(obj -> CharSequenceUtil.format("{}-{}", obj.getEntityCode(), obj.getSysField())));
        for (Map.Entry<String, List<ApproveTaskDetailEntity>> entry : detailMap.entrySet()) {
            List<ApproveTaskDetailEntity> detailEntities = entry.getValue();
            //匹配数据库配置
            ApproveTaskDetailEntity detailEntity = detailEntities.get(0);
            CfgQueryOptionDTO.ViewDTO viewDTOS = map.get(CharSequenceUtil.format("{}-{}", detailEntity.getEntityCode(), detailEntity.getSysField()));
            if (ObjectUtil.isEmpty(viewDTOS)) {
                continue;
            }
            if (detailEntities.size() > MathUtil.ONE) {
                throw new ServiceException("{}存在重复映射，请检查后保存",viewDTOS.getConditionFieldName());
            }
            detailEntity.setSysFieldName(viewDTOS.getConditionFieldName());
            detailEntity.setSysFieldRequired(viewDTOS.getIsRequired());
            detailEntity.setSysFieldType(viewDTOS.getFieldType());
        }
    }

    @Override
    public List<ApproveTaskDetailEntity> listByMainId(String id) {
        return baseMapper.listByMainId(id);
    }

    @Override
    public Boolean removeByMainId(String mainId) {
        return lambdaUpdate().eq(ApproveTaskDetailEntity::getMianId,mainId).remove();
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(ApproveTaskDetailEntity approveTaskDetailEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
