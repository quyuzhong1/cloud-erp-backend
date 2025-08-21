package com.erp.server.wms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.wms.dto.SampleScrapInfoDTO;
import com.erp.model.wms.entity.SampleScrapDetailEntity;
import com.erp.server.wms.mapper.SampleScrapDetailMapper;
import com.erp.server.wms.service.SampleScrapDetailService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.erp.server.wms.service.OperateLogService;
import com.erp.server.wms.service.CommonService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.wms.dto.SampleScrapDetailDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 * 样品报废单明细表 服务实现类
 * </p>
 *
 * @author jack
 * @since 2025-08-20
 */
@Slf4j
@Service
public class SampleScrapDetailServiceImpl extends SuperServiceImpl<SampleScrapDetailMapper, SampleScrapDetailEntity> implements SampleScrapDetailService {
    @Autowired
    private OperateLogService operateLogService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(SampleScrapDetailDTO.AddDTO addDTO) {
        SampleScrapDetailEntity sampleScrapDetailEntity = new SampleScrapDetailEntity();
        BeanMapperUtils.copy(addDTO, sampleScrapDetailEntity);

        // 数据处理
        handleData(sampleScrapDetailEntity);

        log.info("开始新增样品报废单明细单");
        boolean save = super.save(sampleScrapDetailEntity);
        if(!save) {
            throw new ServiceException("样品报废单明细单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "样品报废单明细单" , sampleScrapDetailEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, sampleScrapDetailEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(sampleScrapDetailEntity.getId(), sampleScrapDetailEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(SampleScrapDetailDTO.UpdateDTO addOrUpdateDTO) {
        SampleScrapDetailEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "样品报废单明细单"));
        SampleScrapDetailEntity sampleScrapDetailEntity =  BeanMapperUtils.map(SampleScrapDetailEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(sampleScrapDetailEntity);
        log.info("编辑 开始修改样品报废单明细单数据，id：【{}】", old.getId());
        boolean save = super.updateById(sampleScrapDetailEntity);
        if(!save) {
            throw new ServiceException("样品报废单明细单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录样品报废单明细单日志数据，id：【{}】", sampleScrapDetailEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), sampleScrapDetailEntity.getId(), "样品报废单明细单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, sampleScrapDetailEntity, null, sampleScrapDetailEntity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    public SampleScrapDetailDTO.ImportDTO importFile(MultipartFile excelFile, HttpServletResponse response) {
        return null;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(SampleScrapDetailEntity sampleScrapDetailEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
