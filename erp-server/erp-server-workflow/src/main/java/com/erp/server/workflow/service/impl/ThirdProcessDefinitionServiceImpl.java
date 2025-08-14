package com.erp.server.workflow.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.config.DocNoGenHelper;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.OperationTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.workflow.dto.ThirdProcessDefinitionDTO;
import com.erp.model.workflow.entity.DictBasicEntity;
import com.erp.model.workflow.entity.ThirdProcessDefinitionEntity;
import com.erp.model.workflow.enums.ProcessSourcePlatformEnum;
import com.erp.model.workflow.enums.ThirdProcessDefinitionStatusEnum;
import com.erp.model.workflow.enums.ThirdProcessDefinitionTypeEnum;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.server.workflow.mapper.ThirdProcessDefinitionMapper;
import com.erp.server.workflow.service.DictBasicService;
import com.erp.server.workflow.service.ThirdProcessDefinitionService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_THIRD_PROCESS_DEFINITION;

/**
 * <p>
 * 三方审批定义 服务实现类
 * </p>
 *
 * @author hcg
 * @since 2025-05-12
 */
@Slf4j
@Service
public class ThirdProcessDefinitionServiceImpl extends SuperServiceImpl<ThirdProcessDefinitionMapper, ThirdProcessDefinitionEntity> implements ThirdProcessDefinitionService {
    @Resource
    private DocNoGenHelper docNoGenHelper;

    @Resource
    private DownloadTaskFeign downloadTaskFeign;
    @Autowired
    private DictBasicService dictBasicService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(ThirdProcessDefinitionDTO.AddDTO addDTO) {
        Optional.ofNullable(this.getOne(new LambdaQueryWrapper<ThirdProcessDefinitionEntity>()
                        .eq(ThirdProcessDefinitionEntity::getApprovalCode, addDTO.getApprovalCode())
                        .eq(ThirdProcessDefinitionEntity::getIsDeleted, false)))
                .ifPresent(exists -> {
                    throw new ServiceException("审批定义code {} 已存在",addDTO.getApprovalCode());
                });
        ThirdProcessDefinitionEntity thirdProcessDefinitionEntity = new ThirdProcessDefinitionEntity();
        BeanMapperUtils.copy(addDTO, thirdProcessDefinitionEntity);
        thirdProcessDefinitionEntity.setSourcePlatform(ProcessSourcePlatformEnum.FS.getCode());
        thirdProcessDefinitionEntity.setStatus(ThirdProcessDefinitionStatusEnum.ACTIVE.getCode());
        // 数据处理
        handleData(thirdProcessDefinitionEntity);
        log.info("开始新增三方审批定义");
        boolean save = super.save(thirdProcessDefinitionEntity);
        if(!save) {
            throw new ServiceException("三方审批定义保存失败");
        }

        return new BaseResultDTO.AddDTO(thirdProcessDefinitionEntity.getId(), thirdProcessDefinitionEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(ThirdProcessDefinitionDTO.UpdateDTO addOrUpdateDTO) {
        ThirdProcessDefinitionEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "三方审批定义"));
        ThirdProcessDefinitionEntity thirdProcessDefinitionEntity =  BeanMapperUtils.map(ThirdProcessDefinitionEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(thirdProcessDefinitionEntity);
        log.info("编辑 开始修改三方审批定义数据，id：【{}】", old.getId());
        boolean save = super.updateById(thirdProcessDefinitionEntity);
        if(!save) {
            throw new ServiceException("三方审批定义保存失败");
        }
        return Boolean.TRUE;
    }

    @Override
    public List<ThirdProcessDefinitionDTO.DropDownDTO> dropDown(String type) {
        //1、定时拉取获取定义状态
        Map<String, String> valueToNameMap = dictBasicService.getByType("approveGroup").stream().collect(Collectors.toMap(DictBasicEntity::getValue, DictBasicEntity::getName));
        //2、保存启动条件时验证定义状态
        List<ThirdProcessDefinitionEntity> thirdProcessDefinitionEntities = this.list(new LambdaQueryWrapper<ThirdProcessDefinitionEntity>().eq(ThirdProcessDefinitionEntity::getStatus, ThirdProcessDefinitionStatusEnum.ACTIVE.getCode())
                .eq(ThirdProcessDefinitionEntity::getType, type).eq(ThirdProcessDefinitionEntity::getIsDeleted, false));
        //stream遍历thirdProcessDefinitionEntities 处理entity
        List<ThirdProcessDefinitionDTO.DropDownDTO> dropDownDTOS = thirdProcessDefinitionEntities.stream().map(thirdProcessDefinitionEntity -> {
            ThirdProcessDefinitionDTO.DropDownDTO dropDownDTO = new ThirdProcessDefinitionDTO.DropDownDTO();
            BeanMapperUtils.copy(thirdProcessDefinitionEntity, dropDownDTO);
            dropDownDTO.setCode(thirdProcessDefinitionEntity.getApprovalCode());
            dropDownDTO.setName(valueToNameMap.get(thirdProcessDefinitionEntity.getDictApprovalGroup())+thirdProcessDefinitionEntity.getName());
            return dropDownDTO;
        }).collect(Collectors.toList());
        return dropDownDTOS;
    }

    @Override
    public PagingVO<ThirdProcessDefinitionDTO.ListDTO> paging(PagingDTO<ThirdProcessDefinitionDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<ThirdProcessDefinitionDTO.ListDTO> pageData = baseMapper.paging(query,  pagingParamDTO.getParams());
        for (int i = 0; i < pageData.getRecords().size(); i++) {
            ThirdProcessDefinitionDTO.ListDTO dto = pageData.getRecords().get(i);
            ThirdProcessDefinitionTypeEnum byCode = ThirdProcessDefinitionTypeEnum.getByCode(dto.getType());
            dto.setTypeName(byCode.getName());
            dto.setEnableStatusName(dto.getEnableStatus() ? "启用":"禁用");
        }
        return new PagingVO<>(pageData);
    }

    @Override
    public ThirdProcessDefinitionDTO.ViewDTO view(String id) {
        ThirdProcessDefinitionEntity thirdProcessDefinitionEntity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到三方审批定义"));
        ThirdProcessDefinitionDTO.ViewDTO viewDTO = BeanMapperUtils.map(ThirdProcessDefinitionDTO.ViewDTO.class, thirdProcessDefinitionEntity);
        return viewDTO;
    }

    @Override
    public BatchResultDTO delete(String id) {
        ThirdProcessDefinitionEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到三方审批定义"));
        super.removeById(id);
        return BatchResultDTO.success(entity.getId(), entity.getApprovalCode(), OperationTypeEnum.DELETE);
    }

    @Override
    public BatchResultDTO enable(String id, Boolean enableStatus) {
        ThirdProcessDefinitionEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到三方审批定义"));
        if (!entity.getEnableStatus().equals(enableStatus)) {
            lambdaUpdate().set(ThirdProcessDefinitionEntity::getEnableStatus, enableStatus)
                    .eq(ThirdProcessDefinitionEntity::getId, id)
                    .update();
        }
        return BatchResultDTO.success(entity.getId(), entity.getId(), OperationTypeEnum.UPDATE);
    }

    @Override
    public void exportList(ThirdProcessDefinitionDTO.PagingParamDTO dto, HttpServletResponse response) {
        downloadTaskFeign.saveDownloadTask("审批定义导出", EXPORT_THIRD_PROCESS_DEFINITION.getCode(), dto);
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(ThirdProcessDefinitionEntity thirdProcessDefinitionEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
