package com.erp.server.workflow.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.nacos.common.utils.StringUtils;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.business.enums.OperationTypeEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.dmp.dto.AfterSaleDTO;
import com.erp.model.dmp.entity.AfterSaleEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.workflow.dto.CfgApproveNoticeDTO;
import com.erp.model.workflow.dto.CfgApproveSyncFieldMapDTO;
import com.erp.model.workflow.entity.*;
import com.erp.model.workflow.enums.CfgApproveSyncSyncPlatformEnum;
import com.erp.model.workflow.enums.CfgApproveSyncViewerTypeEnum;
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
        //可见范围类型为不可见时。viewr才能为空
        if(!Objects.equals(addDTO.getViewerType(), CfgApproveSyncViewerTypeEnum.NONE.getCode())
                && CollUtil.isEmpty(addDTO.getViewerList())){
            throw new ServiceException("请选择部门/人员");
        }else{
            String viewer = addDTO.getViewerList().stream().collect(Collectors.joining(","));
            addDTO.setViewer(viewer);
        }

        CfgApproveSyncEntity cfgApproveSyncEntity = new CfgApproveSyncEntity();
        BeanMapperUtils.copy(addDTO, cfgApproveSyncEntity);

        log.info("开始新增ERP审批同步配置");
        // 生成单号
        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_SFSP);
        cfgApproveSyncEntity.setCode(code);
        boolean save = super.save(cfgApproveSyncEntity);
        if(!save) {
            throw new ServiceException("ERP审批同步配置保存失败");
        }
        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据单号为【{}】", UserContext.getDefaultLoginUser().getUserName(), "ERP审批同步配置" , cfgApproveSyncEntity.getCode());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.CFG_APPROVE_SYNC.getCode(), cfgApproveSyncEntity.getId(), "新增操作");

        String id = cfgApproveSyncEntity.getId();
        //新增明细--推送信息
        List<CfgApproveSyncFieldMapDTO.NoticeFieldMapDTO> pushMsgList = addDTO.getPushMsgList();
        if(CollUtil.isEmpty(pushMsgList)){
            throw new ServiceException("推送信息不能为空");
        }else{
            long count = pushMsgList.stream().filter(e -> e.getIsQuick().equals(Boolean.TRUE)).count();
            if(count > 5){
                throw new ServiceException("快捷审批勾选不能超过5个");
            }
            int sort = 1;
            for (CfgApproveSyncFieldMapDTO.NoticeFieldMapDTO noticeFieldMapDTO : pushMsgList) {
                noticeFieldMapDTO.setMainId(id);
                noticeFieldMapDTO.setSort(sort++);
            }
            List<CfgApproveSyncFieldMapEntity> list = BeanMapper.copyList(pushMsgList, CfgApproveSyncFieldMapEntity.class);
            cfgApproveSyncFieldMapService.saveBatch(list);
        }

        //新增明细--通知配置
        List<CfgApproveNoticeDTO.NoticeSettingDTO> noticeSettingList = addDTO.getNoticeSettingList();
        if(CollUtil.isEmpty(noticeSettingList)){
            throw new ServiceException("通知配置不能为空");
        }else{
            for (CfgApproveNoticeDTO.NoticeSettingDTO noticeSettingDTO : noticeSettingList) {
                noticeSettingDTO.setMainId(id);
                if(CollUtil.isNotEmpty(noticeSettingDTO.getNoticeTypeList())){
                    noticeSettingDTO.setNoticeType(noticeSettingDTO.getNoticeTypeList().stream().collect(Collectors.joining(",")));
                }
                if(CollUtil.isNotEmpty(noticeSettingDTO.getRoleTypeList())){
                    noticeSettingDTO.setRoleType(noticeSettingDTO.getRoleTypeList().stream().collect(Collectors.joining(",")));
                }
                if(CollUtil.isNotEmpty(noticeSettingDTO.getSpecificPersonList())){
                    noticeSettingDTO.setSpecificPerson(noticeSettingDTO.getSpecificPersonList().stream().collect(Collectors.joining(",")));
                }
            }
            List<CfgApproveNoticeEntity> list = BeanMapper.copyList(noticeSettingList, CfgApproveNoticeEntity.class);
            cfgApproveNoticeService.saveBatch(list);
        }
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

        //可见范围类型为不可见时。viewr才能为空
        if(!Objects.equals(addOrUpdateDTO.getViewerType(), CfgApproveSyncViewerTypeEnum.NONE.getCode())
                && CollUtil.isEmpty(addOrUpdateDTO.getViewerList())){
            throw new ServiceException("请选择部门/人员");
        }else{
            String viewer = addOrUpdateDTO.getViewerList().stream().collect(Collectors.joining(","));
            addOrUpdateDTO.setViewer(viewer);
        }
        CfgApproveSyncEntity cfgApproveSyncEntity =  BeanMapperUtils.map(CfgApproveSyncEntity.class, addOrUpdateDTO);

        log.info("编辑 开始修改ERP审批同步配置数据，单号：【{}】", old.getCode());
        boolean save = super.updateById(cfgApproveSyncEntity);
        if(!save) {
            throw new ServiceException("ERP审批同步配置保存失败");
        }
        // 记录主单操作日志
        log.info("编辑 开始记录ERP审批同步配置日志数据，单号：【{}】", cfgApproveSyncEntity.getCode());
        String msg = StrUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), old.getCode(), "ERP审批同步配置");
        operateLogService.addModuleOperateLogByObj(old, cfgApproveSyncEntity, ModuleTypeEnum.CFG_APPROVE_SYNC.getCode(), cfgApproveSyncEntity.getId(), msg);

        String id = addOrUpdateDTO.getId();
        //新增明细--推送信息
        List<CfgApproveSyncFieldMapDTO.NoticeFieldMapDTO> pushMsgList = addOrUpdateDTO.getPushMsgList();
        if(CollUtil.isEmpty(pushMsgList)){
            throw new ServiceException("推送信息不能为空");
        }else{
            long count = pushMsgList.stream().filter(e -> e.getIsQuick().equals(Boolean.TRUE)).count();
            if(count > 5){
                throw new ServiceException("快捷审批勾选不能超过5个");
            }
            int sort = 1;
            for (CfgApproveSyncFieldMapDTO.NoticeFieldMapDTO noticeFieldMapDTO : pushMsgList) {
                noticeFieldMapDTO.setMainId(id);
                noticeFieldMapDTO.setSort(sort++);
            }
            List<CfgApproveSyncFieldMapEntity> list = BeanMapper.copyList(pushMsgList, CfgApproveSyncFieldMapEntity.class);

            List<CfgApproveSyncFieldMapEntity> oldList = cfgApproveSyncFieldMapService.listByMainIds(Arrays.asList(id));

            List<String> ids = list.stream().map(CfgApproveSyncFieldMapEntity::getId).filter(StringUtils::isNotEmpty).collect(Collectors.toList());

            List<CfgApproveSyncFieldMapEntity> removeList = oldList.stream().filter(e -> !ids.contains(e.getId())).collect(Collectors.toList());
            if (CollUtil.isNotEmpty(removeList)) {
                // 操作日志
                removeList.stream().forEach(e -> {
                    String removeMsg = StrUtil.format("用户【{}】删除推送消息【{}】", UserContext.getDefaultLoginUser().getUserName(), e.getFieldName());
                    operateLogService.addModuleOperateLog(removeMsg, ModuleTypeEnum.CFG_APPROVE_SYNC.getCode(), cfgApproveSyncEntity.getId(), "编辑信息");
                });
            }

            List<CfgApproveSyncFieldMapEntity> newList = list.stream().filter(e -> StringUtils.isBlank(e.getId())).collect(Collectors.toList());
            if (CollUtil.isNotEmpty(newList)) {
                // 操作日志
                newList.stream().forEach(e -> {
                    String newMsg = StrUtil.format("用户【{}】新增推送消息【{}】", UserContext.getDefaultLoginUser().getUserName(), e.getFieldName());
                    operateLogService.addModuleOperateLog(newMsg, ModuleTypeEnum.CFG_APPROVE_SYNC.getCode(), cfgApproveSyncEntity.getId(), "编辑信息");
                });
            }

            List<CfgApproveSyncFieldMapEntity> updateList = list.stream().filter(e -> StringUtils.isNotBlank(e.getId())).collect(Collectors.toList());
            if (CollUtil.isNotEmpty(updateList)) {
                // 操作日志
                updateList.stream().forEach(e -> {
                    CfgApproveSyncFieldMapEntity cfgApproveSyncFieldMapEntity = oldList.stream().filter(o -> e.getId().equals(o.getId())).findFirst().orElse(null);
                    if(Objects.nonNull(cfgApproveSyncFieldMapEntity)){
                        String updateMsg = StrUtil.format("用户【{}】编辑推送消息 ", UserContext.getDefaultLoginUser().getUserName());
                        operateLogService.addModuleOperateLogByObj(cfgApproveSyncFieldMapEntity, e, ModuleTypeEnum.CFG_APPROVE_SYNC.getCode(), cfgApproveSyncEntity.getId(), updateMsg);
                    }
                });
            }
            cfgApproveSyncFieldMapService.saveOrUpdateBatch(list);
        }

        //新增明细--通知配置
        List<CfgApproveNoticeDTO.NoticeSettingDTO> noticeSettingList = addOrUpdateDTO.getNoticeSettingList();
        if(CollUtil.isEmpty(noticeSettingList)){
            throw new ServiceException("通知配置不能为空");
        }else{
            for (CfgApproveNoticeDTO.NoticeSettingDTO noticeSettingDTO : noticeSettingList) {
                noticeSettingDTO.setMainId(id);
                if(CollUtil.isNotEmpty(noticeSettingDTO.getNoticeTypeList())){
                    noticeSettingDTO.setNoticeType(noticeSettingDTO.getNoticeTypeList().stream().collect(Collectors.joining(",")));
                }
                if(CollUtil.isNotEmpty(noticeSettingDTO.getRoleTypeList())){
                    noticeSettingDTO.setRoleType(noticeSettingDTO.getRoleTypeList().stream().collect(Collectors.joining(",")));
                }
                if(CollUtil.isNotEmpty(noticeSettingDTO.getSpecificPersonList())){
                    noticeSettingDTO.setSpecificPerson(noticeSettingDTO.getSpecificPersonList().stream().collect(Collectors.joining(",")));
                }
            }
            List<CfgApproveNoticeEntity> list = BeanMapper.copyList(noticeSettingList, CfgApproveNoticeEntity.class);

            List<CfgApproveNoticeEntity> oldList = cfgApproveNoticeService.listByMainIds(Arrays.asList(id));
            DetailEntityChangeLogger.logChanges(list, oldList, id, ModuleTypeEnum.CFG_APPROVE_SYNC.getCode(), "", "", "",UserContext.getDefaultLoginUser().getUserName(), new DetailEntityChangeLogger.Logger() {
                @Override
                public void addModuleOperateLog(String message, String moduleType, String entityId, String operationType) {
                    operateLogService.addModuleOperateLog(message, moduleType, entityId, operationType);
                }

                @Override
                public void addModuleOperateLogByObj(Object oldObj, Object newObj, String moduleType, String entityId, String operationType) {
                    operateLogService.addModuleOperateLogByObj(oldObj, newObj, moduleType, entityId, operationType);
                }
            });
            cfgApproveNoticeService.saveOrUpdateBatch(list);
        }
        return Boolean.TRUE;
    }

    /**
     * 新增修改处理数据
     */
    private void handleData(CfgApproveSyncEntity cfgApproveSyncEntity) {



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

            getSyncPlatform(item);

            item.setEnableStatusName(Objects.equals(item.getEnableStatus(), Boolean.FALSE) ? "停用" : "启用");
        });
    }

    private static void getSyncPlatform(CfgApproveSyncDTO.ListDTO item) {
        if(StringUtils.isBlank(item.getSyncPlatform())){
            return;
        }
        String[] split = item.getSyncPlatform().split(",");
        StringBuffer sb = new StringBuffer();
        for (String str : split) {
            sb.append(CfgApproveSyncSyncPlatformEnum.getName(str));
            sb.append(",");
        }
        item.setSyncPlatformName(sb.toString().substring(0, sb.length()-1));
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
