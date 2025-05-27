package com.erp.server.workflow.service.impl;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.business.enums.OperationTypeEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.workflow.entity.CfgProcessFieldMapEntity;
import com.erp.model.workflow.entity.CfgProcessValueMapEntity;
import com.erp.model.workflow.entity.CfgThirdProcessEntity;
import com.erp.model.workflow.enums.CfgThirdProcessSourcePlatformEnum;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.server.workflow.mapper.CfgThirdProcessMapper;
import com.erp.server.workflow.service.*;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.core.exception.ServiceException;
import com.common.business.config.DocNoGenHelper;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.workflow.dto.CfgThirdProcessDTO;

import java.util.*;
import java.util.stream.Collectors;

import com.common.core.utils.*;
import com.common.core.enums.ApiError;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_PROCESS_THIRD_PROCESS;

/**
 * <p>
 * 三方审批生成 服务实现类
 * </p>
 *
 * @author hcg
 * @since 2025-05-23
 */
@Slf4j
@Service
public class CfgThirdProcessServiceImpl extends SuperServiceImpl<CfgThirdProcessMapper, CfgThirdProcessEntity> implements CfgThirdProcessService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private DocNoGenHelper docNoGenHelper;
    @Resource
    private CfgProcessFieldMapService cfgProcessFieldMapService;
    @Resource
    private DownloadTaskFeign downloadTaskFeign;
    @Resource
    private CfgProcessValueMapService cfgProcessValueMapService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(CfgThirdProcessDTO.AddDTO addDTO) {
        CfgThirdProcessEntity cfgThirdProcessEntity = new CfgThirdProcessEntity();
        BeanMapperUtils.copy(addDTO, cfgThirdProcessEntity);
        cfgThirdProcessEntity.setCode(docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_SFSC));
        cfgThirdProcessEntity.setId(IdWorker.getIdStr());
        // 数据处理
        handleData(cfgThirdProcessEntity);
        log.info("开始新增三方审批生成");
        // 生成单号
        // TODO 此处的null需填写生成单号类型，type查看BusinessNoTypeEnum枚举类 注意需要填写prefix 为单号前缀
        boolean save = super.save(cfgThirdProcessEntity);
        if (!save) {
            throw new ServiceException("三方审批生成保存失败");
        }
        //新增明细：field->cfg_type、cfg_id thirdCfg
        cfgProcessFieldMapService.add(addDTO.getBussinessKey(), cfgThirdProcessEntity.getId(), cfgThirdProcessEntity.getId(), addDTO.getFieldMapList());
        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据单号为【{}】", UserContext.getDefaultLoginUser().getUserName(), "三方审批生成", cfgThirdProcessEntity.getCode());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.CFG_THIRD_PROCESS.getCode(), cfgThirdProcessEntity.getId(), "新增操作");
        return new BaseResultDTO.AddDTO(cfgThirdProcessEntity.getId(), cfgThirdProcessEntity.getCode());
    }

    /**
     * 修改
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(CfgThirdProcessDTO.UpdateDTO addOrUpdateDTO) {
        CfgThirdProcessEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "三方审批生成"));
        CfgThirdProcessEntity cfgThirdProcessEntity = BeanMapperUtils.map(CfgThirdProcessEntity.class, addOrUpdateDTO);
        log.info("编辑 开始修改三方审批生成数据，单号：【{}】", old.getCode());
        boolean save = super.updateById(cfgThirdProcessEntity);
        if (!save) {
            throw new ServiceException("三方审批生成保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）
        cfgProcessFieldMapService.addOrUpdate(addOrUpdateDTO.getBussinessKey(), cfgThirdProcessEntity.getId(), cfgThirdProcessEntity.getId(), addOrUpdateDTO.getFieldMapList());
        // 记录主单操作日志
        log.info("编辑 开始记录三方审批生成日志数据，单号：【{}】", cfgThirdProcessEntity.getCode());
        String msg = StrUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), cfgThirdProcessEntity.getCode(), "三方审批生成");
        operateLogService.addModuleOperateLogByObj(old, cfgThirdProcessEntity, ModuleTypeEnum.CFG_THIRD_PROCESS.getCode(), cfgThirdProcessEntity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    public PagingVO<CfgThirdProcessDTO.ListDTO> paging(PagingDTO<CfgThirdProcessDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<CfgThirdProcessDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        String sourcePlatFormName = CfgThirdProcessSourcePlatformEnum.FS_AUDIT.getName();
        pageData.getRecords().forEach(
                e -> {
                    e.setSourcePlatformName(sourcePlatFormName);
                }
        );
        return new PagingVO<>(pageData);
    }

    @Override
    public CfgThirdProcessDTO.ViewDTO view(String id) {
        CfgThirdProcessEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到ERP审批同步配置数据"));
        CfgThirdProcessDTO.ViewDTO view = baseMapper.getView(id);
        return view;
    }

    @Override
    public BatchResultDTO enable(String id, Boolean enableStatus) {
        CfgThirdProcessEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到三方审批生成数据"));
        if (!entity.getEnableStatus().equals(enableStatus)) {
            lambdaUpdate()
                    .set(CfgThirdProcessEntity::getEnableStatus, enableStatus)
                    .eq(CfgThirdProcessEntity::getId, id)
                    .update();
            // 日志
            String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据变更为【{}】 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "三方审批生成", Objects.equals(enableStatus, Boolean.FALSE) ? "停用" : "启用");
            operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.CFG_APPROVE_SYNC.getCode(), entity.getCode(), "更新三方审批生成数据");
        }
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.UPDATE);
    }

    @Override
    public void exportList(CfgThirdProcessDTO.PagingParamDTO dto, HttpServletResponse response) {
        downloadTaskFeign.saveDownloadTask("三方审批生成导出", EXPORT_PROCESS_THIRD_PROCESS.getCode(), dto);
    }

    @Override
    public BatchResultDTO delete(String id) {
        CfgThirdProcessEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到三方审批生成数据"));
        //删除主表数据
        super.removeById(id);
        //删除子表数据
        cfgProcessFieldMapService.delete(Arrays.asList(id));
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据删除操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "三方审批生成");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.CFG_THIRD_PROCESS.getCode(), entity.getCode(), "删除三方审批生成数据");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.DELETE);
    }

    @Override
    public List<CfgThirdProcessDTO.TabListDTO> tabList(PermissionsDTO dto) {
        CfgThirdProcessDTO.PagingParamDTO searchParam = new CfgThirdProcessDTO.PagingParamDTO();
        searchParam.setPermissionSql(dto.getPermissionSql());
        List<CfgThirdProcessDTO.TabListDTO> list = baseMapper.tabList(searchParam);
        List<CfgThirdProcessDTO.TabListDTO> result = new ArrayList<>();
        CfgThirdProcessDTO.TabListDTO enable = list.stream().filter(e -> e.getTabFlag().equals("t")).findFirst().orElse(null);
        CfgThirdProcessDTO.TabListDTO disable = list.stream().filter(e -> e.getTabFlag().equals("f")).findFirst().orElse(null);
        result.add(new CfgThirdProcessDTO.TabListDTO("all", "全部", 0));
        result.add(new CfgThirdProcessDTO.TabListDTO("true", "启用", null == enable ? 0 : enable.getCount()));
        result.add(new CfgThirdProcessDTO.TabListDTO("false", "停用", null == disable ? 0 : disable.getCount()));
        return result;
    }


    /**
     * 新增修改处理数据
     */
    private void handleData(CfgThirdProcessEntity cfgThirdProcessEntity) {
        // TODO 验证数据 & 数据赋值
    }
}
