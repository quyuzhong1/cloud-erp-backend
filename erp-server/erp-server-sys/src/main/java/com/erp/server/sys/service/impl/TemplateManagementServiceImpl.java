package com.erp.server.sys.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.sys.dto.TemplateManagementDTO;
import com.erp.model.sys.entity.TemplateManagementEntity;
import com.erp.server.sys.mapper.TemplateManagementMapper;
import com.erp.server.sys.service.TemplateManagementService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
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
import com.erp.model.sys.dto.TemplateManagementDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * 合同模板主表 服务实现类
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

        // 数据处理
        handleData(templateManagementEntity);

        log.info("开始新增合同模板主单");
        // 生成单号
        // TODO 此处的null需填写生成单号类型，type查看BusinessNoTypeEnum枚举类 注意需要填写prefix 为单号前缀
        String code = docNoGenHelper.generateCode(null);
        templateManagementEntity.setCode(code);
        boolean save = super.save(templateManagementEntity);
        if(!save) {
            throw new ServiceException("合同模板主单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据单号为【{}】", UserContext.getDefaultLoginUser().getUserName(), "合同模板主单" , templateManagementEntity.getCode());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.TEMPLATE_MANAGEMENT.getCode(), templateManagementEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(templateManagementEntity.getId(), code);
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(TemplateManagementDTO.UpdateDTO addOrUpdateDTO) {
        TemplateManagementEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "合同模板主单"));
        TemplateManagementEntity templateManagementEntity =  BeanMapperUtils.map(TemplateManagementEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(templateManagementEntity);
        log.info("编辑 开始修改合同模板主单数据，单号：【{}】", old.getCode());
        boolean save = super.updateById(templateManagementEntity);
        if(!save) {
            throw new ServiceException("合同模板主单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录合同模板主单日志数据，单号：【{}】", templateManagementEntity.getCode());
            String msg = StrUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), templateManagementEntity.getCode(), "合同模板主单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, templateManagementEntity, null, templateManagementEntity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    public List<TemplateManagementDTO.TabListDTO> tabList(PermissionsDTO param) {
        TemplateManagementDTO.PagingParamDTO searchParam = new TemplateManagementDTO.PagingParamDTO();
        searchParam.setPermissionSql(param.getPermissionSql());
        List<TemplateManagementDTO.TabListDTO> list = baseMapper.tabList(searchParam);
        List<TemplateManagementDTO.TabListDTO> result = new ArrayList<>();
        TemplateManagementDTO.TabListDTO enable = list.stream().filter(e -> e.getTabFlag().equals("t")).findFirst().orElse(null);
        TemplateManagementDTO.TabListDTO disable = list.stream().filter(e -> e.getTabFlag().equals("f")).findFirst().orElse(null);
        result.add(new TemplateManagementDTO.TabListDTO("all", "全部" , 0));
        result.add(new TemplateManagementDTO.TabListDTO("false", "启用" , null == enable ? 0 : enable.getCount()));
        result.add(new TemplateManagementDTO.TabListDTO("true", "停用" ,null == disable ? 0 : disable.getCount()));
        return result;
    }

    @Override
    public PagingVO<TemplateManagementDTO.ListDTO> paging(PagingDTO<TemplateManagementDTO.PagingParamDTO> dto) {
        return null;
    }

    @Override
    public TemplateManagementDTO.ViewDTO view(String id) {
        return null;
    }

    @Override
    public BatchResultDTO delete(String id) {
        return null;
    }

    @Override
    public BatchResultDTO setStatus(String id, Boolean enableStatus) {
        return null;
    }

    @Override
    public BatchResultDTO setDefault(String id) {
        return null;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(TemplateManagementEntity templateManagementEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
