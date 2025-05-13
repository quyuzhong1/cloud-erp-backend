package com.erp.server.workflow.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.OperationTypeEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.dmp.dto.AfterSaleDTO;
import com.erp.model.dmp.entity.AfterSaleEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.workflow.entity.*;
import com.erp.model.workflow.enums.CfgApproveSyncSyncPlatformEnum;
import com.erp.model.workflow.enums.DictBasicEnum;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.server.workflow.mapper.CfgApproveSyncMapper;
import com.erp.server.workflow.service.*;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.core.exception.ServiceException;
import com.common.business.config.DocNoGenHelper;
import com.common.core.controller.vo.ApiResult;
import cn.hutool.core.util.ObjectUtil;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.workflow.dto.CfgApproveSyncDTO;
import java.util.*;
import java.util.stream.Collectors;

import com.common.core.utils.*;
import com.common.core.enums.ApiError;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_DMP_AFTER_SALE;
import static com.common.business.enums.FileTaskEventEnum.EXPORT_PROCESS_CFG_APPROVE_SYNC;

/**
 * <p>
 * ERP审批同步配置 服务实现类
 * </p>
 *
 * @author jack
 * @since 2025-05-12
 */
@Slf4j
@Service
public class CfgApproveSyncServiceImpl extends SuperServiceImpl<CfgApproveSyncMapper, CfgApproveSyncEntity> implements CfgApproveSyncService {
    @Resource
    private OperateLogService operateLogService;
    @Resource
    private DocNoGenHelper docNoGenHelper;

    @Resource
    private WorkMenuService workMenuService;

    @Resource
    private DictBasicService dictBasicService;

    @Resource
    private CfgApproveNoticeService cfgApproveNoticeService;

    @Resource
    private CfgApproveSyncFieldMapService cfgApproveSyncFieldMapService;

    @Resource
    private DownloadTaskFeign downloadTaskFeign;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(CfgApproveSyncDTO.AddDTO addDTO) {
        CfgApproveSyncEntity cfgApproveSyncEntity = new CfgApproveSyncEntity();
        BeanMapperUtils.copy(addDTO, cfgApproveSyncEntity);

        // 数据处理
        handleData(cfgApproveSyncEntity);

        log.info("开始新增ERP审批同步配置");
        // 生成单号
        // TODO 此处的null需填写生成单号类型，type查看BusinessNoTypeEnum枚举类 注意需要填写prefix 为单号前缀
        String code = docNoGenHelper.generateCode(null);
        cfgApproveSyncEntity.setCode(code);
        boolean save = super.save(cfgApproveSyncEntity);
        if(!save) {
            throw new ServiceException("ERP审批同步配置保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据单号为【{}】", UserContext.getDefaultLoginUser().getUserName(), "ERP审批同步配置" , cfgApproveSyncEntity.getCode());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, cfgApproveSyncEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(cfgApproveSyncEntity.getId(), code);
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(CfgApproveSyncDTO.UpdateDTO addOrUpdateDTO) {
        CfgApproveSyncEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "ERP审批同步配置"));
        CfgApproveSyncEntity cfgApproveSyncEntity =  BeanMapperUtils.map(CfgApproveSyncEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(cfgApproveSyncEntity);
        log.info("编辑 开始修改ERP审批同步配置数据，单号：【{}】", old.getCode());
        boolean save = super.updateById(cfgApproveSyncEntity);
        if(!save) {
            throw new ServiceException("ERP审批同步配置保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录ERP审批同步配置日志数据，单号：【{}】", cfgApproveSyncEntity.getCode());
            String msg = StrUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), cfgApproveSyncEntity.getCode(), "ERP审批同步配置");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, cfgApproveSyncEntity, null, cfgApproveSyncEntity.getId(), msg);
        return Boolean.TRUE;
    }

    /**
     * 新增修改处理数据
     */
    private void handleData(CfgApproveSyncEntity cfgApproveSyncEntity) {
        // TODO 验证数据 & 数据赋值
    }

    @Override
    public List<CfgApproveSyncDTO.TabListDTO> tabList(PermissionsDTO param) {
        CfgApproveSyncDTO.PagingParamDTO searchParam = new CfgApproveSyncDTO.PagingParamDTO();
        searchParam.setPermissionSql(param.getPermissionSql());
        List<CfgApproveSyncDTO.TabListDTO> list = baseMapper.tabList(searchParam);
        // 获取状态列表
        List<Boolean> statusList = Arrays.asList(Boolean.TRUE,Boolean.FALSE);
        // 不存在的状态赋值为0
        List<Boolean> existStatusList = list.stream().map(CfgApproveSyncDTO.TabListDTO::getTabFlag).collect(Collectors.toList());
        statusList.stream().forEach(status -> {
            if (!existStatusList.contains(status)) {
                list.add(new CfgApproveSyncDTO.TabListDTO(status, "" , 0));
            }
        });
        list.forEach(item -> item.setTabFlagName(Objects.equals(item.getTabFlag(), Boolean.FALSE) ? "停用" : "启用"));
        return list;
    }

    @Override
    public PagingVO<CfgApproveSyncDTO.ListDTO> paging(PagingDTO<CfgApproveSyncDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<CfgApproveSyncDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
            return new PagingVO(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO(pageData);
    }

    private void fillList(List<CfgApproveSyncDTO.ListDTO> records) {
        Map<String, WorkMenuEntity> workMenuMap = workMenuService.list().stream().collect(Collectors.toMap(WorkMenuEntity::getModuleCode, item -> item));

        Map<String, DictBasicEntity> mapByType = dictBasicService.getMapByType(DictBasicEnum.TEST.getName());

        records.parallelStream().forEach(item -> {
            WorkMenuEntity workMenuEntity = workMenuMap.getOrDefault(item.getBusinessType(),null);
            if(Objects.nonNull(workMenuEntity)){
                item.setBusinessTypeName(workMenuEntity.getModuleClassify());
            }

            DictBasicEntity dictBasicEntity = mapByType.getOrDefault(item.getApproveGroup(),null);
            if(Objects.nonNull(dictBasicEntity)){
                item.setApproveGroupName(dictBasicEntity.getName());
            }

            item.setSyncPlatformName(CfgApproveSyncSyncPlatformEnum.getName(item.getSyncPlatform()));

            item.setEnableStatusName(Objects.equals(item.getEnableStatus(), Boolean.FALSE) ? "停用" : "启用");
        });
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO delete(String id) {
        CfgApproveSyncEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到ERP审批同步配置数据"));
        // 删除主单数据
        super.removeById(id);
        // 删除子表
        cfgApproveNoticeService.lambdaUpdate()
                .set(CfgApproveNoticeEntity::getIsDeleted, Boolean.TRUE)
                .eq(CfgApproveNoticeEntity::getMainId, id)
                .update();
        cfgApproveSyncFieldMapService.lambdaUpdate()
                .set(CfgApproveSyncFieldMapEntity::getIsDeleted, Boolean.TRUE)
                .eq(CfgApproveSyncFieldMapEntity::getMainId, id)
                .update();

        // 删除日志数据
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据删除操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "ERP审批同步配置");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.CFG_APPROVE_SYNC.getCode(), entity.getCode(), "删除ERP审批同步配置数据");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.DELETE);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO enable(String id,Boolean enableStatus) {
        CfgApproveSyncEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到ERP审批同步配置数据"));
        if(!entity.getEnableStatus().equals(enableStatus)){
            lambdaUpdate()
                    .set(CfgApproveSyncEntity::getEnableStatus, enableStatus)
                    .eq(CfgApproveSyncEntity::getId, id)
                    .update();
            // 日志
            String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据变更为【{}】 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "ERP审批同步配置",Objects.equals(enableStatus, Boolean.FALSE) ? "停用" : "启用");
            operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.CFG_APPROVE_SYNC.getCode(), entity.getCode(), "更新ERP审批同步配置数据");
        }
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.UPDATE);
    }

    @Override
    public void exportList(CfgApproveSyncDTO.PagingParamDTO param, HttpServletResponse response) {
        downloadTaskFeign.saveDownloadTask("ERP审批同步配置导出", EXPORT_PROCESS_CFG_APPROVE_SYNC.getCode(), param);
    }


}
