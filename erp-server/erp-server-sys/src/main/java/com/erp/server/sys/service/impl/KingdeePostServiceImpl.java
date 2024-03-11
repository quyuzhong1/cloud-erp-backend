package com.erp.server.sys.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.sys.entity.KingdeePostEntity;
import com.erp.server.sys.mapper.KingdeePostMapper;
import com.erp.server.sys.service.KingdeePostService;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.server.sys.service.OperateLogService;
import com.erp.server.sys.service.CommonService;
import com.common.core.exception.ServiceException;
import com.common.business.config.DocNoGenHelper;
import com.common.core.controller.vo.ApiResult;
import cn.hutool.core.util.ObjectUtil;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.sys.dto.KingdeePostDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * 金蝶岗位表 服务实现类
 * </p>
 *
 * @author Lambda
 * @since 2024-03-11
 */
@Slf4j
@Service
public class KingdeePostServiceImpl extends SuperServiceImpl<KingdeePostMapper, KingdeePostEntity> implements KingdeePostService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private CommonService commonService;
    @Autowired
    private DocNoGenHelper docNoGenHelper;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(KingdeePostDTO.AddDTO addDTO) {
        KingdeePostEntity kingdeePostEntity = new KingdeePostEntity();
        BeanMapperUtils.copy(addDTO, kingdeePostEntity);

        // 数据处理
        handleData(kingdeePostEntity);

        log.info("开始新增金蝶岗位单");
        // 生成单号
        // TODO 此处的null需填写生成单号类型，type查看BusinessNoTypeEnum枚举类 注意需要填写prefix 为单号前缀
        String code = docNoGenHelper.generateCode(null);
        kingdeePostEntity.setCode(code);
        boolean save = super.save(kingdeePostEntity);
        if(!save) {
            throw new ServiceException("金蝶岗位单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据单号为【{}】", commonService.getUserInfo().getUserName(), "金蝶岗位单" , kingdeePostEntity.getCode());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, kingdeePostEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(kingdeePostEntity.getId(), code);
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(KingdeePostDTO.UpdateDTO updateDTO) {
        KingdeePostEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "金蝶岗位单"));
        KingdeePostEntity kingdeePostEntity =  BeanMapperUtils.map(KingdeePostEntity.class, updateDTO);

        // 数据处理
        handleData(kingdeePostEntity);
        log.info("编辑 开始修改金蝶岗位单数据，单号：【{}】", old.getCode());
        boolean save = super.updateById(kingdeePostEntity);
        if(!save) {
            throw new ServiceException("金蝶岗位单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录金蝶岗位单日志数据，单号：【{}】", kingdeePostEntity.getCode());
            String msg = StrUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", commonService.getUserInfo().getUserName(), kingdeePostEntity.getCode(), "金蝶岗位单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, kingdeePostEntity, null, kingdeePostEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(KingdeePostEntity kingdeePostEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
