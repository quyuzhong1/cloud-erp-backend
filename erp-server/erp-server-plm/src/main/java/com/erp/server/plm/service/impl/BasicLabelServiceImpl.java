package com.erp.server.plm.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.model.plm.entity.BasicLabelEntity;
import com.erp.server.plm.mapper.BasicLabelMapper;
import com.erp.server.plm.service.BasicLabelService;
import com.erp.server.plm.service.CommonService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.plm.dto.BasicLabelDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * 基础标签表 服务实现类
 * </p>
 *
 * @author Lambda
 * @since 2023-09-13
 */
@Slf4j
@Service
public class BasicLabelServiceImpl extends SuperServiceImpl<BasicLabelMapper, BasicLabelEntity> implements BasicLabelService {

    @Autowired
    private CommonService commonService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public String add(BasicLabelDTO.AddDTO addDTO) {
        BasicLabelEntity basicLabelEntity = new BasicLabelEntity();
        BeanMapperUtils.copy(addDTO, basicLabelEntity);

        // 数据处理
        handleData(basicLabelEntity);

        log.info("开始新增基础标签单");
        boolean save = super.save(basicLabelEntity);
        if(!save) {
            throw new ServiceException("基础标签单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", commonService.getUserInfo().getUserName(), "基础标签单" , basicLabelEntity.getId());

        return basicLabelEntity.getId();
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(BasicLabelDTO.UpdateDTO updateDTO) {
        BasicLabelEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "基础标签单"));
        BasicLabelEntity basicLabelEntity =  BeanMapperUtils.map(BasicLabelEntity.class, updateDTO);

        // 数据处理
        handleData(basicLabelEntity);
        log.info("编辑 开始修改基础标签单数据，id：【{}】", old.getId());
        boolean save = super.updateById(basicLabelEntity);
        if(!save) {
            throw new ServiceException("基础标签单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录基础标签单日志数据，id：【{}】", basicLabelEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", commonService.getUserInfo().getUserName(), basicLabelEntity.getId(), "基础标签单");

        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(BasicLabelEntity basicLabelEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
