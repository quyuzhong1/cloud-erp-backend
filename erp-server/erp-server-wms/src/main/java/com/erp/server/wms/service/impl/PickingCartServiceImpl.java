package com.erp.server.wms.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.config.DocNoGenHelper;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.business.enums.OperationTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.wms.dto.PickingCartDTO;
import com.erp.model.wms.dto.pickingstrategy.CfgRuleConditionDTO;
import com.erp.model.wms.entity.CfgRuleConditionEntity;
import com.erp.model.wms.entity.PickingCartEntity;
import com.erp.model.wms.entity.WaveListEntity;
import com.erp.model.wms.enums.WaveStatusEnum;
import com.erp.server.wms.mapper.PickingCartMapper;
import com.erp.server.wms.service.CfgRuleConditionService;
import com.erp.server.wms.service.PickingCartService;
import com.erp.server.wms.service.WaveListService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;

/**
 * <p>
 * 拣货车管理 服务实现类
 * </p>
 *
 * @author will
 * @since 2024-06-20
 */
@Slf4j
@Service
public class PickingCartServiceImpl extends SuperServiceImpl<PickingCartMapper, PickingCartEntity> implements PickingCartService {

    @Resource
    private DocNoGenHelper docNoGenHelper;

    @Resource
    private CfgRuleConditionService cfgRuleConditionService;

    @Resource
    private WaveListService waveListService;

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(PickingCartDTO.AddDTO addDTO) {
        PickingCartEntity pickingCartEntity = new PickingCartEntity();
        BeanMapperUtils.copy(addDTO, pickingCartEntity);

        // 数据处理
        handleData(pickingCartEntity);

        log.info("开始新增拣货车管理");
        // 生成单号
        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_JHC);
        pickingCartEntity.setCode(code);
        boolean save = super.save(pickingCartEntity);
        if(!save) {
            throw new ServiceException("拣货车管理保存失败");
        }
        return new BaseResultDTO.AddDTO(pickingCartEntity.getId(), code);
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(PickingCartDTO.UpdateDTO updateDTO) {
        PickingCartEntity old = super.getById(updateDTO.getId());
        if (Objects.isNull(old)){
            throw new ServiceException(ApiError.NOT_EXIST_BILL, "拣货车管理");
        }
        PickingCartEntity pickingCartEntity =  BeanMapperUtils.map(PickingCartEntity.class, updateDTO);

        // 数据处理
        handleData(pickingCartEntity);
        log.info("编辑 开始修改拣货车管理数据，单号：【{}】", old.getCode());
        boolean save = super.updateById(pickingCartEntity);
        if(!save) {
            throw new ServiceException("拣货车管理保存失败");
        }
        return Boolean.TRUE;
    }

    @Override
    public PagingVO<PickingCartDTO.ListDTO> paging(PagingDTO<PickingCartDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<PickingCartDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
            return new PagingVO(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO(pageData);
    }

    @Override
    public BatchResultDTO delete(String id) {
        PickingCartEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到拣货车数据"));

        //判断拣货车是否被波次使用
        List<WaveListEntity> waveList = waveListService.listByPickingCartCodeList(Collections.singletonList(entity.getCode()));
        long count = waveList.stream().filter(obj -> !CharSequenceUtil.equals(obj.getStatus(), WaveStatusEnum.FINISH.getCode())).count();
        if (count > 0) {
            throw new ServiceException(CharSequenceUtil.format("拣货车【{}】已被波次列表使用，不支持删除",entity.getCode()));
        }

        // 删除主单数据
        log.info("删除 开始删除拣货车主单数据，id：【{}】", id);
        super.removeById(id);
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.DELETE);
    }

    @Override
    public PickingCartDTO.ViewDTO view(String id) {
        PickingCartEntity entity = super.getByIdOpt(id).orElseThrow(()->new ServiceException("未找到拣货车数据"));
        PickingCartDTO.ViewDTO view = BeanMapperUtils.map(PickingCartDTO.ViewDTO.class, entity);
        //查询规则条件
        List<CfgRuleConditionEntity> ruleConditionEntities = cfgRuleConditionService.list(Wrappers.<CfgRuleConditionEntity>lambdaQuery()
                .eq(CfgRuleConditionEntity::getRuleId, id)
                .orderByAsc(CfgRuleConditionEntity::getIndex));
        List<CfgRuleConditionDTO.View> ruleConditions = BeanMapperUtils.copyList(CfgRuleConditionDTO.View.class, ruleConditionEntities);
        view.setConditionList(ruleConditions);
        return view;
    }

    @Override
    public List<PickingCartEntity> listByTypeId(String typeId) {
        return lambdaQuery().eq(PickingCartEntity::getTypeId,typeId).list();
    }

    @Override
    public Boolean updateStatus(PickingCartDTO.UpdateStatusDTO dto) {
        PickingCartEntity entity = super.getByIdOpt(dto.getId()).orElseThrow(() -> new ServiceException("未找到拣货车数据"));
        if (dto.getDisabled().equals(entity.getDisabled())) {
            String disabledName = dto.getDisabled() ? "禁用" : "启用";
            throw new ServiceException(CharSequenceUtil.format("拣货车已【{}】，不支持再次【{}】",disabledName,disabledName));
        }
        return lambdaUpdate().eq(PickingCartEntity::getId,dto.getId())
                .set(PickingCartEntity::getDisabled,dto.getDisabled())
                .update(new PickingCartEntity());
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(PickingCartEntity pickingCartEntity) {
    // TODO 验证数据 & 数据赋值
    }

    /**
     *  分页数据处理
     */
    private void fillList (List<PickingCartDTO.ListDTO> list) {
        if (CollUtil.isEmpty(list)) {
            return;
        }
    }

    @Override
    public List<PickingCartDTO.ViewDTO> searchByKeyword(String code) {
        List<PickingCartEntity> entityList = this.list(new QueryWrapper<PickingCartEntity>().like("code", code));
        List<PickingCartDTO.ViewDTO> viewDTOS = BeanMapper.copyList(entityList, PickingCartDTO.ViewDTO.class);
        return viewDTOS;
    }
}
