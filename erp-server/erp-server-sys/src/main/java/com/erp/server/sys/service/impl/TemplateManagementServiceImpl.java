package com.erp.server.sys.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.business.enums.OperationTypeEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.sys.dto.TemplateManagementDTO;
import com.erp.model.sys.entity.TemplateManagementEntity;
import com.erp.model.sys.enums.TemplateManagementBizTypeEnum;
import com.erp.model.sys.enums.TemplateManagementStatusEnum;
import com.erp.model.sys.enums.TemplateManagementTypeEnum;
import com.erp.server.sys.mapper.TemplateManagementMapper;
import com.erp.server.sys.service.TemplateManagementService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.erp.server.sys.service.OperateLogService;
import com.common.core.exception.ServiceException;
import com.common.business.config.DocNoGenHelper;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;

/**
 * <p>
 * 模板管理主表 服务实现类
 * </p>
 *
 * @author jack
 * @since 2025-07-24
 */
@Slf4j
@Service
public class TemplateManagementServiceImpl extends SuperServiceImpl<TemplateManagementMapper, TemplateManagementEntity> implements TemplateManagementService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private DocNoGenHelper docNoGenHelper;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(TemplateManagementDTO.AddDTO addDTO) {
        TemplateManagementEntity templateManagementEntity = new TemplateManagementEntity();
        BeanMapperUtils.copy(addDTO, templateManagementEntity);

        Boolean isDefault = templateManagementEntity.getIsDefault();
        if(Boolean.TRUE.equals(isDefault)){
            String type = templateManagementEntity.getType();
            Integer count = lambdaQuery().eq(TemplateManagementEntity::getType, type).eq(TemplateManagementEntity::getIsDefault, Boolean.TRUE).count();
            if(count > 0){
                throw new ServiceException(ApiError.ERROR_9057,TemplateManagementTypeEnum.getName(type));
            }
        }

        log.info("开始新增模板管理");
        // 生成单号
        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_MB);
        templateManagementEntity.setCode(code);
        boolean save = super.save(templateManagementEntity);
        if (!save) {
            throw new ServiceException("模板管理保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据单号为【{}】", UserContext.getDefaultLoginUser().getUserName(), "模板管理", templateManagementEntity.getCode());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.TEMPLATE_MANAGEMENT.getCode(), templateManagementEntity.getId(), "新增操作");
        return new BaseResultDTO.AddDTO(templateManagementEntity.getId(), code);
    }

    /**
     * 修改
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(TemplateManagementDTO.UpdateDTO addOrUpdateDTO) {
        TemplateManagementEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "模板管理"));
        TemplateManagementEntity templateManagementEntity = BeanMapperUtils.map(TemplateManagementEntity.class, addOrUpdateDTO);

        Boolean isDefault = templateManagementEntity.getIsDefault();
        if(Boolean.TRUE.equals(isDefault)){
            String type = templateManagementEntity.getType();
            Integer count = lambdaQuery().ne(TemplateManagementEntity::getId,old.getId()).eq(TemplateManagementEntity::getType, type).eq(TemplateManagementEntity::getIsDefault, Boolean.TRUE).count();
            if(count > 0){
                throw new ServiceException(ApiError.ERROR_9057,TemplateManagementTypeEnum.getName(type));
            }
        }

        log.info("编辑 开始修改模板管理数据，单号：【{}】", old.getCode());
        boolean save = super.updateById(templateManagementEntity);
        if (!save) {
            throw new ServiceException("模板管理保存失败");
        }
        // 记录操作日志
        log.info("编辑 开始记录模板管理日志数据，单号：【{}】", templateManagementEntity.getCode());
        String msg = StrUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), templateManagementEntity.getCode(), "模板管理");
        operateLogService.addModuleOperateLogByObj(old, templateManagementEntity, ModuleTypeEnum.TEMPLATE_MANAGEMENT.getCode(), templateManagementEntity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    public List<TemplateManagementDTO.TabListDTO> tabList(PermissionsDTO param) {
        TemplateManagementDTO.PagingParamDTO searchParam = new TemplateManagementDTO.PagingParamDTO();
        searchParam.setPermissionSql(param.getPermissionSql());
        List<TemplateManagementDTO.TabListDTO> list = baseMapper.tabList(searchParam);
        List<TemplateManagementDTO.TabListDTO> result = new ArrayList<>();
        TemplateManagementDTO.TabListDTO enable = list.stream().filter(e -> e.getTabFlag().equals("f")).findFirst().orElse(null);
        TemplateManagementDTO.TabListDTO disable = list.stream().filter(e -> e.getTabFlag().equals("t")).findFirst().orElse(null);
        result.add(new TemplateManagementDTO.TabListDTO("all", "全部", 0));
        result.add(new TemplateManagementDTO.TabListDTO("false", "启用", null == enable ? 0 : enable.getCount()));
        result.add(new TemplateManagementDTO.TabListDTO("true", "停用", null == disable ? 0 : disable.getCount()));
        return result;
    }

    @Override
    public PagingVO<TemplateManagementDTO.ListDTO> paging(PagingDTO<TemplateManagementDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<TemplateManagementDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
            return new PagingVO(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO(pageData);
    }

    private void fillList(List<TemplateManagementDTO.ListDTO> records) {
        for (TemplateManagementDTO.ListDTO record : records) {
            //业务类型
            record.setBizTypeName(TemplateManagementBizTypeEnum.getName(record.getBizTypeName()));
            //模板类型
            record.setTypeName(TemplateManagementTypeEnum.getName(record.getType()));
            //状态
            record.setStatusName(TemplateManagementStatusEnum.getName(record.getStatus()));
        }
    }

    @Override
    public TemplateManagementDTO.ViewDTO view(String id) {
        TemplateManagementEntity old = super.getById(id);
        old = Optional.ofNullable(old).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "模板管理"));
        TemplateManagementDTO.ViewDTO view = new TemplateManagementDTO.ViewDTO();
        BeanMapper.copy(old,view);

        // 数据填充处理
        fillOne(view);
        return view;
    }

    private void fillOne(TemplateManagementDTO.ViewDTO view) {
        //业务类型
        view.setBizTypeName(TemplateManagementBizTypeEnum.getName(view.getBizTypeName()));
        //模板类型
        view.setTypeName(TemplateManagementTypeEnum.getName(view.getType()));
        //状态
        view.setStatusName(TemplateManagementStatusEnum.getName(view.getStatus()));
    }

    @Override
    public BatchResultDTO delete(String id) {
        TemplateManagementEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "模板管理"));

        //判断合同管理是否有引用

        // 删除主单数据
        super.removeById(id);
        // 删除日志数据
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据删除操作", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "模板管理");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.TEMPLATE_MANAGEMENT.getCode(), id, "删除模板管理");
        return BatchResultDTO.success(entity.getId(), entity.getName(), OperationTypeEnum.DELETE);
    }

    @Override
    public BatchResultDTO setDisabled(String id, Boolean disabledStatus) {
        TemplateManagementEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "模板管理"));

        entity.setDisabled(disabledStatus);
        super.updateById(entity);

        // 日志数据
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据更新启用状态由{}为{}", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "模板管理", disabledStatus ? "停用" : "启用", disabledStatus ? "启用" : "停用" );
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.TEMPLATE_MANAGEMENT.getCode(), entity.getId(), "更新模板管理");
        return BatchResultDTO.success(entity.getId(), entity.getName(), OperationTypeEnum.UPDATE);
    }

    @Override
    public BatchResultDTO setDefault(String id) {
        TemplateManagementEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "模板管理"));

        Boolean isDefault = entity.getIsDefault();
        Boolean newValue = isDefault ? Boolean.FALSE : Boolean.TRUE;
        if(Boolean.TRUE.equals(newValue)){
            String type = entity.getType();
            Integer count = lambdaQuery().eq(TemplateManagementEntity::getType, type).eq(TemplateManagementEntity::getIsDefault, Boolean.TRUE).count();
            if(count > 0){
                throw new ServiceException(ApiError.ERROR_9057,TemplateManagementTypeEnum.getName(type));
            }
        }

        entity.setIsDefault(newValue);
        super.updateById(entity);

        // 日志数据
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据更新默认状态由{}为{}", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "模板管理", isDefault ? "默认" : "不默认", newValue ?  "默认" : "不默认" );
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.TEMPLATE_MANAGEMENT.getCode(), entity.getId(), "更新模板管理");
        return BatchResultDTO.success(entity.getId(), entity.getName(), OperationTypeEnum.UPDATE);
    }

    @Override
    public PagingVO<TemplateManagementDTO.PageSelectDTO> pagingSelect(TemplateManagementDTO.SelectDTO dto) {
        return null;
    }
}
