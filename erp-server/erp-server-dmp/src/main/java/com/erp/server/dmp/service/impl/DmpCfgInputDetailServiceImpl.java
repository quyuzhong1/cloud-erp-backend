package com.erp.server.dmp.service.impl;


import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.common.business.dto.DmpInputFeignDTO;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.enums.OperationTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.dmp.dto.DmpCfgInputDetailDTO;
import com.erp.model.dmp.dto.DmpInoutDTO;
import com.erp.model.dmp.entity.DmpCfgInputDetailEntity;
import com.erp.model.dmp.enums.DmpInputTaskTaskTypeEnum;
import com.erp.server.dmp.mapper.DmpCfgInputDetailMapper;
import com.erp.server.dmp.service.DmpCfgInputDetailService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
/**
 * <p>
 * 外部系统接口明细 服务实现类
 * </p>
 *
 * @author shukai
 * @since 2024-06-11
 */
@Slf4j
@Service
public class DmpCfgInputDetailServiceImpl extends SuperServiceImpl<DmpCfgInputDetailMapper, DmpCfgInputDetailEntity> implements DmpCfgInputDetailService {
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(DmpCfgInputDetailDTO.AddDTO addDTO) {
        DmpCfgInputDetailEntity dmpCfgInputDetailEntity = new DmpCfgInputDetailEntity();
        BeanMapperUtils.copy(addDTO, dmpCfgInputDetailEntity);

        // 数据处理
        handleData(dmpCfgInputDetailEntity);

        log.info("开始新增外部系统接口明细");
        boolean save = super.save(dmpCfgInputDetailEntity);
        if(!save) {
            throw new ServiceException("外部系统接口明细保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "外部系统接口明细" , dmpCfgInputDetailEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(dmpCfgInputDetailEntity.getId(), dmpCfgInputDetailEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(DmpCfgInputDetailDTO.UpdateDTO updateDTO) {
        DmpCfgInputDetailEntity old = super.getById(updateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "外部系统接口明细"));
        DmpCfgInputDetailEntity dmpCfgInputDetailEntity =  BeanMapperUtils.map(DmpCfgInputDetailEntity.class, updateDTO);

        // 数据处理
        handleData(dmpCfgInputDetailEntity);
        log.info("编辑 开始修改外部系统接口明细数据，id：【{}】", old.getId());
        boolean save = super.updateById(dmpCfgInputDetailEntity);
        if(!save) {
            throw new ServiceException("外部系统接口明细保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录外部系统接口明细日志数据，id：【{}】", dmpCfgInputDetailEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), dmpCfgInputDetailEntity.getId(), "外部系统接口明细");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        return Boolean.TRUE;
    }

    @Override
    public List<DmpInoutDTO.ListDTO> listBySystemCodeAndBillType(List<String> systemCodeList, List<String> billTypeList, List<String> nextLevelIdList) {
        return baseMapper.listBySystemCodeAndBillType(systemCodeList, billTypeList, nextLevelIdList);
    }

    @Override
    public void optionDmpCfgInputDetail(DmpInputFeignDTO.CfgOptionDTO cfgOptionDTO) {
        //需要添加的配置不存在则新增，存在则修改
        DmpCfgInputDetailEntity detailEntity = baseMapper.getDmpCfgInputDetailByOption(cfgOptionDTO);
        if (ObjUtil.isEmpty(detailEntity)) {
            throw new ServiceException(ApiError.NOT_EXIST, CharSequenceUtil.format("{}平台{}编码配置不存在,请检查", cfgOptionDTO.getSystem(), cfgOptionDTO.getCode()));
        }
        if (CharSequenceUtil.isBlank(detailEntity.getId())) {
            LocalDateTime now = LocalDateTime.now();
            detailEntity.setNextLevelId(cfgOptionDTO.getNextLevelId());
            detailEntity.setLastTime(now);
            detailEntity.setNextTime(now);
            detailEntity.setIntervalTime(600);
            detailEntity.setOverrideTime(0);
            detailEntity.setMaxRetryCount(3);
            detailEntity.setExecTimeout(1200);
            detailEntity.setTaskType(DmpInputTaskTaskTypeEnum.NORMAL.getCode());
            detailEntity.setMaxIntervalTime(3);
        }
        if (OperationTypeEnum.ADD.getStatus().equals(cfgOptionDTO.getOption())) {
            //新增或更新
            detailEntity.setDisabled(Boolean.FALSE);
            super.saveOrUpdate(detailEntity);
            return;
        }
        if (OperationTypeEnum.UPDATE.getStatus().equals(cfgOptionDTO.getOption())) {
            if (CharSequenceUtil.isBlank(detailEntity.getId())) {
                String nextLevelId = cfgOptionDTO.getNextLevelId();
                cfgOptionDTO.setNextLevelId(cfgOptionDTO.getOldNextLevelId());
                DmpCfgInputDetailEntity oldDetailEntity = baseMapper.getDmpCfgInputDetailByOption(cfgOptionDTO);
                if (ObjUtil.isNotEmpty(oldDetailEntity)) {
                    detailEntity.setId(oldDetailEntity.getId());
                    detailEntity.setNextLevelId(nextLevelId);
                }
            }
            //新增或更新
            detailEntity.setDisabled(Boolean.FALSE);
            detailEntity.setNextLevelId(cfgOptionDTO.getNextLevelId());
            super.saveOrUpdate(detailEntity);
            return;
        }
        if (OperationTypeEnum.DELETE.getStatus().equals(cfgOptionDTO.getOption())) {
            if(CharSequenceUtil.isBlank(detailEntity.getId())) {
                //删除时如果没有id则说明配置不存在，无需删除
                return;
            }
            //删除
            super.removeById(detailEntity.getId());
        }
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(DmpCfgInputDetailEntity dmpCfgInputDetailEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
