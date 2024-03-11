package com.erp.server.sys.service.impl;


import com.common.business.config.DocNoGenHelper;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.sys.dto.KingdeeOperatorTypeDTO;
import com.erp.model.sys.entity.KingdeeOperatorTypeEntity;
import com.erp.server.sys.mapper.KingdeeOperatorTypeMapper;
import com.erp.server.sys.service.CommonService;
import com.erp.server.sys.service.KingdeeOperatorTypeService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author Lambda
 * @since 2024-03-11
 */
@Slf4j
@Service
public class KingdeeOperatorTypeServiceImpl extends SuperServiceImpl<KingdeeOperatorTypeMapper, KingdeeOperatorTypeEntity> implements KingdeeOperatorTypeService {

    @Autowired
    private CommonService commonService;
    @Autowired
    private DocNoGenHelper docNoGenHelper;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(KingdeeOperatorTypeDTO.AddDTO addDTO) {
        KingdeeOperatorTypeEntity kingdeeOperatorTypeEntity = new KingdeeOperatorTypeEntity();
        BeanMapperUtils.copy(addDTO, kingdeeOperatorTypeEntity);

        // 数据处理
        handleData(kingdeeOperatorTypeEntity);

        log.info("开始新增");
        // 生成单号
        // TODO 此处的null需填写生成单号类型，type查看BusinessNoTypeEnum枚举类 注意需要填写prefix 为单号前缀
        String code = docNoGenHelper.generateCode(null);
        kingdeeOperatorTypeEntity.setCode(code);
        boolean save = super.save(kingdeeOperatorTypeEntity);
        if(!save) {
            throw new ServiceException("保存失败");
        }



        return new BaseResultDTO.AddDTO(kingdeeOperatorTypeEntity.getId(), code);
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(KingdeeOperatorTypeDTO.UpdateDTO updateDTO) {
        KingdeeOperatorTypeEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, ""));
        KingdeeOperatorTypeEntity kingdeeOperatorTypeEntity =  BeanMapperUtils.map(KingdeeOperatorTypeEntity.class, updateDTO);

        // 数据处理
        handleData(kingdeeOperatorTypeEntity);
        log.info("编辑 开始修改数据，单号：【{}】", old.getCode());
        boolean save = super.updateById(kingdeeOperatorTypeEntity);
        if(!save) {
            throw new ServiceException("保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）


        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(KingdeeOperatorTypeEntity kingdeeOperatorTypeEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
