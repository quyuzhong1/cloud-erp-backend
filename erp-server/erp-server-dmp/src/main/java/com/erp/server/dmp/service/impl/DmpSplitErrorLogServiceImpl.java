package com.erp.server.dmp.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.model.dmp.entity.DmpSplitErrorLogEntity;
import com.erp.server.dmp.mapper.DmpSplitErrorLogMapper;
import com.erp.server.dmp.service.DmpSplitErrorLogService;
import com.erp.server.dmp.service.CommonService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.dmp.dto.DmpSplitErrorLogDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author Luo_WG
 * @since 2023-09-14
 */
@Slf4j
@Service
public class DmpSplitErrorLogServiceImpl extends SuperServiceImpl<DmpSplitErrorLogMapper, DmpSplitErrorLogEntity> implements DmpSplitErrorLogService {

    @Autowired
    private CommonService commonService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public String add(DmpSplitErrorLogDTO.AddDTO addDTO) {
        DmpSplitErrorLogEntity dmpSplitErrorLogEntity = new DmpSplitErrorLogEntity();
        BeanMapperUtils.copy(addDTO, dmpSplitErrorLogEntity);

        // 数据处理
        handleData(dmpSplitErrorLogEntity);

        log.info("开始新增");
        boolean save = super.save(dmpSplitErrorLogEntity);
        if(!save) {
            throw new ServiceException("保存失败");
        }

        return dmpSplitErrorLogEntity.getId();
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(DmpSplitErrorLogDTO.UpdateDTO updateDTO) {
        DmpSplitErrorLogEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, ""));
        DmpSplitErrorLogEntity dmpSplitErrorLogEntity =  BeanMapperUtils.map(DmpSplitErrorLogEntity.class, updateDTO);

        // 数据处理
        handleData(dmpSplitErrorLogEntity);
        log.info("编辑 开始修改数据，id：【{}】", old.getId());
        boolean save = super.updateById(dmpSplitErrorLogEntity);
        if(!save) {
            throw new ServiceException("保存失败");
        }

        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(DmpSplitErrorLogEntity dmpSplitErrorLogEntity) {
    }
}
