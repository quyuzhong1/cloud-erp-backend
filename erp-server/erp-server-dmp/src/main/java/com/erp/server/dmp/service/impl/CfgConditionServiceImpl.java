package com.erp.server.dmp.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.common.core.enums.RuleCompareEnum;
import com.erp.model.dmp.entity.CfgConditionEntity;
import com.erp.server.dmp.mapper.CfgConditionMapper;
import com.erp.server.dmp.service.CfgConditionService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.erp.server.dmp.service.OperateLogService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.dmp.dto.CfgConditionDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * 条件配置表 服务实现类
 * </p>
 *
 * @author lrp
 * @since 2025-01-20
 */
@Slf4j
@Service
public class CfgConditionServiceImpl extends SuperServiceImpl<CfgConditionMapper, CfgConditionEntity> implements CfgConditionService {
    @Autowired
    private OperateLogService operateLogService;

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(CfgConditionDTO.AddDTO addDTO) {
        CfgConditionEntity cfgConditionEntity = new CfgConditionEntity();
        BeanMapperUtils.copy(addDTO, cfgConditionEntity);

        // 数据处理
        handleData(cfgConditionEntity);

        log.info("开始新增条件配置单");
        boolean save = super.save(cfgConditionEntity);
        if(!save) {
            throw new ServiceException("条件配置单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "条件配置单" , cfgConditionEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, cfgConditionEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(cfgConditionEntity.getId(), cfgConditionEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(CfgConditionDTO.UpdateDTO addOrUpdateDTO) {
        CfgConditionEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "条件配置单"));
        CfgConditionEntity cfgConditionEntity =  BeanMapperUtils.map(CfgConditionEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(cfgConditionEntity);
        log.info("编辑 开始修改条件配置单数据，id：【{}】", old.getId());
        boolean save = super.updateById(cfgConditionEntity);
        if(!save) {
            throw new ServiceException("条件配置单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录条件配置单日志数据，id：【{}】", cfgConditionEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), cfgConditionEntity.getId(), "条件配置单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, cfgConditionEntity, null, cfgConditionEntity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    public List<CfgConditionDTO.ListDTO> listPromptWorkCondition() {
        return baseMapper.listDeclareCondition();
    }

    @Override
    public List<CfgConditionDTO.TreeDTO> tree() {

        List<CfgConditionEntity> conditionEntityList = this.list();
        List<CfgConditionDTO.TreeDTO> resultList = new ArrayList<>(conditionEntityList.size());
        Map<String, String> map = new HashMap<>();
        for (RuleCompareEnum item : RuleCompareEnum.values()) {
            map.put(item.getCode(), item.getName());
        }

        for (CfgConditionEntity item : conditionEntityList) {
            String conditionField = item.getConditionField();
            CfgConditionDTO.TreeDTO tree = new CfgConditionDTO.TreeDTO();
            tree.setConditionField(conditionField);
            String logicStr = item.getLogic();
            List<String> logicList = Arrays.asList(logicStr.split(","));
            List<CfgConditionDTO.TreeDTO> childrenList = new ArrayList<>(logicList.size());
            for (String logic : logicList) {
                CfgConditionDTO.TreeDTO children = new CfgConditionDTO.TreeDTO();
                children.setConditionField(conditionField);
                children.setLogic(logic);
                children.setLogicName(map.getOrDefault(logic, ""));
                childrenList.add(children);
            }
            tree.setChildren(childrenList);
            resultList.add(tree);


        }
        return resultList;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(CfgConditionEntity cfgConditionEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
