package com.erp.server.sys.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.sys.entity.KingdeeUserRefPostEntity;
import com.erp.server.sys.mapper.KingdeeUserRefPostMapper;
import com.erp.server.sys.service.KingdeeUserRefPostService;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.server.sys.service.OperateLogService;
import com.erp.server.sys.service.CommonService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.sys.dto.KingdeeUserRefPostDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * 金蝶员工任岗表 服务实现类
 * </p>
 *
 * @author Lambda
 * @since 2024-03-11
 */
@Slf4j
@Service
public class KingdeeUserRefPostServiceImpl extends SuperServiceImpl<KingdeeUserRefPostMapper, KingdeeUserRefPostEntity> implements KingdeeUserRefPostService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private CommonService commonService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(KingdeeUserRefPostDTO.AddDTO addDTO) {
        KingdeeUserRefPostEntity kingdeeUserRefPostEntity = new KingdeeUserRefPostEntity();
        BeanMapperUtils.copy(addDTO, kingdeeUserRefPostEntity);

        // 数据处理
        handleData(kingdeeUserRefPostEntity);

        log.info("开始新增金蝶员工任岗单");
        boolean save = super.save(kingdeeUserRefPostEntity);
        if(!save) {
            throw new ServiceException("金蝶员工任岗单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", commonService.getUserInfo().getUserName(), "金蝶员工任岗单" , kingdeeUserRefPostEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, kingdeeUserRefPostEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(kingdeeUserRefPostEntity.getId(), kingdeeUserRefPostEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(KingdeeUserRefPostDTO.UpdateDTO updateDTO) {
        KingdeeUserRefPostEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "金蝶员工任岗单"));
        KingdeeUserRefPostEntity kingdeeUserRefPostEntity =  BeanMapperUtils.map(KingdeeUserRefPostEntity.class, updateDTO);

        // 数据处理
        handleData(kingdeeUserRefPostEntity);
        log.info("编辑 开始修改金蝶员工任岗单数据，id：【{}】", old.getId());
        boolean save = super.updateById(kingdeeUserRefPostEntity);
        if(!save) {
            throw new ServiceException("金蝶员工任岗单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录金蝶员工任岗单日志数据，id：【{}】", kingdeeUserRefPostEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", commonService.getUserInfo().getUserName(), kingdeeUserRefPostEntity.getId(), "金蝶员工任岗单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, kingdeeUserRefPostEntity, null, kingdeeUserRefPostEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(KingdeeUserRefPostEntity kingdeeUserRefPostEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
