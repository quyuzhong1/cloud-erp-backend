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
import com.common.business.enums.ThirdpartyPlatformEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.dmp.enums.DmpBasicSystemCodeEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.sys.entity.SysRefererConfigEntity;
import com.erp.model.sys.vo.ThirdUnionDTO;
import com.erp.model.workflow.dto.CfgApproveNoticeDTO;
import com.erp.model.workflow.dto.CfgApproveSyncFieldMapDTO;
import com.erp.model.workflow.entity.*;
import com.erp.model.workflow.enums.*;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.sys.feign.SysRefereConfigFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.sdk.fs.enmu.DepartmentIdTypeEnum;
import com.erp.sdk.fs.enmu.UserIdTypeEnum;
import com.erp.sdk.fs.service.FsService;
import com.erp.server.workflow.mapper.CfgApproveSyncMapper;
import com.erp.server.workflow.service.*;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.core.exception.ServiceException;
import com.common.business.config.DocNoGenHelper;
import com.lark.oapi.service.approval.v4.model.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.workflow.dto.CfgApproveSyncDTO;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import com.common.core.utils.*;
import com.common.core.enums.ApiError;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

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

    @Resource
    private SysRefereConfigFeign sysRefereConfigFeign;

    @Resource
    private FsService fsService;

    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private CfgSettingService cfgSettingService;
    @Resource
    private ApproveSyncRecordService approveSyncRecordService;
    @Resource
    private CfgApproveSyncService configApproveSyncService;

    @Resource
    private ProcessManagementService processManagementService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(CfgApproveSyncDTO.AddDTO addDTO) {
        //校验是否已存在
        String businessType = addDTO.getBusinessType();
        List<CfgApproveSyncEntity> existList = lambdaQuery().eq(CfgApproveSyncEntity::getBusinessType, businessType).list();
        if(CollUtil.isNotEmpty(existList)){
            throw new ServiceException("单据名称已添加配置，不可重复添加");
        }

        //可见范围类型为不可见时。viewr才能为空
        String viewerType = addDTO.getViewerType();
        List<String> viewerList = addDTO.getViewerList();
        String viewer = checkViewType(viewerType, viewerList);
        addDTO.setViewer(viewer);

        CfgApproveSyncEntity cfgApproveSyncEntity = new CfgApproveSyncEntity();
        BeanMapperUtils.copy(addDTO, cfgApproveSyncEntity);

        String syncPlatform = addDTO.getSyncPlatformList().stream().collect(Collectors.joining(","));
        cfgApproveSyncEntity.setSyncPlatform(syncPlatform);

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
        List<CfgApproveSyncFieldMapEntity> fieldMapEntityList;
        List<CfgApproveSyncFieldMapDTO.NoticeFieldMapDTO> pushMsgList = addDTO.getPushMsgList();
        if(CollUtil.isEmpty(pushMsgList)){
            throw new ServiceException("推送信息不能为空");
        }else{
            long count = pushMsgList.stream().filter(e -> e.getIsQuick().equals(Boolean.TRUE)).count();
            if(count < 1 ){
                throw new ServiceException("请至少勾选一个快捷审批");
            }
            if(count > 5 ){
                throw new ServiceException("快捷审批勾选不能超过5个");
            }
            int sort = 1;
            for (CfgApproveSyncFieldMapDTO.NoticeFieldMapDTO noticeFieldMapDTO : pushMsgList) {
                noticeFieldMapDTO.setMainId(id);
                noticeFieldMapDTO.setSort(sort++);
            }
            fieldMapEntityList = BeanMapper.copyList(pushMsgList, CfgApproveSyncFieldMapEntity.class);
            cfgApproveSyncFieldMapService.saveBatch(fieldMapEntityList);
        }

        //新增明细--通知配置
        List<CfgApproveNoticeEntity> cfgApproveNoticeEntityList = new ArrayList<>();
        addDTO.getApprove().setNoticeType(CfgApproveNoticeNoticeTypeEnum.APPROVE.getCode());
        addDTO.getApproveResult().setNoticeType(CfgApproveNoticeNoticeTypeEnum.APPROVERESULT.getCode());
        addDTO.getCc().setNoticeType(CfgApproveNoticeNoticeTypeEnum.CC.getCode());
        addDTO.getRecall().setNoticeType(CfgApproveNoticeNoticeTypeEnum.RECALL.getCode());
        List<CfgApproveNoticeDTO.NoticeSettingDTO> noticeSettingList = Arrays.asList(addDTO.getApprove(),addDTO.getApproveResult(),addDTO.getCc(),addDTO.getRecall());
        for (CfgApproveNoticeDTO.NoticeSettingDTO noticeSettingDTO : noticeSettingList) {
            if (Objects.isNull(noticeSettingDTO)) {
                continue;
            }
            //勾选了启用
            if(noticeSettingDTO.getEnableStatus()){
                if(CollUtil.isEmpty(noticeSettingDTO.getRoleTypeList()) && CollUtil.isEmpty(noticeSettingDTO.getSpecificPersonList())){
                    throw new ServiceException(CfgApproveNoticeNoticeTypeEnum.getName(noticeSettingDTO.getNoticeType()) +"勾选了启用请至少选择一个通知人员");
                }
            }
            noticeSettingDTO.setMainId(id);
            if (CollUtil.isNotEmpty(noticeSettingDTO.getRoleTypeList())) {
                noticeSettingDTO.setRoleType(noticeSettingDTO.getRoleTypeList().stream().collect(Collectors.joining(",")));
            }
            if (CollUtil.isNotEmpty(noticeSettingDTO.getSpecificPersonList())) {
                noticeSettingDTO.setSpecificPerson(noticeSettingDTO.getSpecificPersonList().stream().collect(Collectors.joining(",")));
            }
            CfgApproveNoticeEntity cfgApproveNoticeEntity = new CfgApproveNoticeEntity();
            BeanMapper.copy(noticeSettingDTO,cfgApproveNoticeEntity);
            cfgApproveNoticeEntityList.add(cfgApproveNoticeEntity);
        }
        if(CollUtil.isNotEmpty(cfgApproveNoticeEntityList) ){
            cfgApproveNoticeService.saveBatch(cfgApproveNoticeEntityList);
        }

        //组装请求体并创建（飞书的）三方审批定义
        CreateExternalApprovalResp resp = fsService.externalApprovalsCreate(buildExternalApprovalReq(cfgApproveSyncEntity));
        lambdaUpdate().set(CfgApproveSyncEntity::getApprovalCode, resp.getData().getApprovalCode())
                .eq(CfgApproveSyncEntity::getId, id)
                .update();

        return new BaseResultDTO.AddDTO(cfgApproveSyncEntity.getId(), code);
    }

    private String checkViewType(String viewerType, List<String> viewerList) {
        if(Objects.equals(viewerType, CfgApproveSyncViewerTypeEnum.DEPARTMENT.getCode()) || Objects.equals(viewerType, CfgApproveSyncViewerTypeEnum.USER.getCode())){
            if( Objects.isNull(viewerList) || CollUtil.isEmpty(viewerList)){
                throw new ServiceException("指定部门/指定用户时，审批可见人列表不能为空");
            }else {
                if(viewerList.size() > 200){
                    throw new ServiceException("审批可见人列表不能超过200上限");
                }
                return viewerList.stream().collect(Collectors.joining(","));
            }
        }
        return "";
    }

    /**
     * 组装（飞书的）三方审批定义请求体
     */
    private CreateExternalApprovalReq buildExternalApprovalReq(CfgApproveSyncEntity cfgApproveSyncEntity) {
        //三方审批相关信息
        ExternalApproval externalApproval = getExternalApproval(cfgApproveSyncEntity);
        //国际化文案
        externalApproval.setI18nResources(getExternalApprovalI18nResources(cfgApproveSyncEntity));
        //可见人列表
        getApprovalViewers(cfgApproveSyncEntity, externalApproval);
        // 创建请求对象
        return CreateExternalApprovalReq.newBuilder()
                .departmentIdType(DepartmentIdTypeEnum.OPENDEPARTMENTID.getCode())
                .userIdType(UserIdTypeEnum.UNIONID.getCode())
                .externalApproval(externalApproval)
                .build();
    }

    //三方审批相关信息
    private ExternalApproval getExternalApproval(CfgApproveSyncEntity cfgApproveSyncEntity) {
//        List<SysRefererConfigEntity> refererConfig = sysRefereConfigFeign.getByReferer(CfgApproveSyncSyncPlatformEnum.FEISHU.getCode());
//        String referer = "";
//        if(CollUtil.isNotEmpty(refererConfig)){
//            referer = refererConfig.get(0).getReferer();
//        }

        Map<String, Object> dataJson = cfgSettingService.getFsActionCallback();

        return ExternalApproval.newBuilder()
                .approvalName("@i18n@1")
                .approvalCode(cfgApproveSyncEntity.getBusinessType())
                .groupCode(cfgApproveSyncEntity.getApproveGroup())
                .groupName("@i18n@2")
                .description("@i18n@3")
                .external(ApprovalCreateExternal.newBuilder()
                        .bizName("@i18n@4")
                        .bizType(cfgApproveSyncEntity.getBusinessType())
                        .createLinkMobile("")
                        .createLinkPc("")
                        .supportPc(true)
                        .supportMobile(true)
                        .supportBatchRead(false)
                        .enableMarkReaded(false)
                        .allowBatchOperate(false)
                        .actionCallbackUrl(cfgApproveSyncEntity.getWebhookUrl())
                        .actionCallbackToken(String.valueOf(dataJson.get("actionCallbackToken")))
                        .actionCallbackKey(String.valueOf(dataJson.get("actionCallbackKey")))
                        .build())
                .build();
    }

    private void getApprovalViewers(CfgApproveSyncEntity cfgApproveSyncEntity, ExternalApproval externalApproval) {
        String viewer = cfgApproveSyncEntity.getViewer();
        String viewerType = cfgApproveSyncEntity.getViewerType();

        List<String> viewerList = Arrays.stream(viewer.split(","))
                .map(String::trim)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toList());

        List<ApprovalCreateViewers> approvalCreateViewersList = new ArrayList<>();

        if (CfgApproveSyncViewerTypeEnum.USER.getCode().equals(viewerType)) {
            List<ThirdUnionDTO> thirdUnionList = sysUserFeign.getThirdByUserIds(ThirdpartyPlatformEnum.FS.getCode(), viewerList);
            if (CollUtil.isEmpty(thirdUnionList)) {
                throw new ServiceException("审批可见人列表人员未关联第三方用户信息");
            }
            List<String> thirdUnionIds = thirdUnionList.stream()
                    .map(ThirdUnionDTO::getThirdUnionId)
                    .distinct()
                    .collect(Collectors.toList());

            approvalCreateViewersList = thirdUnionIds.stream()
                    .map(userId -> ApprovalCreateViewers.newBuilder()
                            .viewerType(viewerType)
                            .viewerUserId(userId)
                            .build())
                    .collect(Collectors.toList());

        } else if (CfgApproveSyncViewerTypeEnum.DEPARTMENT.getCode().equals(viewerType)) {
            approvalCreateViewersList = viewerList.stream()
                    .map(deptId -> ApprovalCreateViewers.newBuilder()
                            .viewerType(viewerType)
                            .viewerDepartmentId(deptId)
                            .build())
                    .collect(Collectors.toList());
        }else {
            approvalCreateViewersList.add(ApprovalCreateViewers.newBuilder()
                    .viewerType(viewerType)
                    .build());
        }

        handleApprovalViewers(approvalCreateViewersList, externalApproval, viewerType);
    }

    // 提取公共逻辑
    private void handleApprovalViewers(List<ApprovalCreateViewers> viewersList, ExternalApproval externalApproval, String viewerType) {
        int size = viewersList.size();

        if (size > 200) {
            throw new ServiceException("审批可见人列表不能超过200上限");
        }

        if (size == 0 && (CfgApproveSyncViewerTypeEnum.DEPARTMENT.getCode().equals(viewerType)
                || CfgApproveSyncViewerTypeEnum.USER.getCode().equals(viewerType))) {
            throw new ServiceException("指定部门/指定用户时，审批可见人列表不能为空");
        }
        if (size > 0) {
            externalApproval.setViewers(viewersList.toArray(new ApprovalCreateViewers[0]));
        }
    }

    //国际化文案
    private I18nResource[] getExternalApprovalI18nResources(CfgApproveSyncEntity cfgApproveSyncEntity) {
        Map<String, WorkMenuEntity> workMenuMap = workMenuService.list().stream().collect(Collectors.toMap(WorkMenuEntity::getModuleCode, item -> item));

        Map<String, DictBasicEntity> mapByType = dictBasicService.getMapByType(DictBasicEnum.TEST.getName());
        Map<String,String> values = new HashMap<>();
        //单据
        WorkMenuEntity workMenuEntity = workMenuMap.getOrDefault(cfgApproveSyncEntity.getBusinessType(),null);
        if(Objects.nonNull(workMenuEntity)){
            values.put("@i18n@1",  workMenuEntity.getModuleClassify());
        }
        //审批分组
        DictBasicEntity dictBasicEntity = mapByType.getOrDefault(cfgApproveSyncEntity.getApproveGroup(),null);
        if(Objects.nonNull(dictBasicEntity)){
            values.put("@i18n@2",  dictBasicEntity.getName());
        }

        //审批定义的说明，后续企业员工发起审批时，该说明会在审批发起页展示。
        values.put("@i18n@3", dictBasicEntity.getRemark());
        //列表中用于提示审批来自哪个三方系统。
        values.put("@i18n@4", DmpBasicSystemCodeEnum.ERP.getName());
        return mapToI18nResouceArray(values);
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(CfgApproveSyncDTO.UpdateDTO addOrUpdateDTO) {
        CfgApproveSyncEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "ERP审批同步配置"));

        //校验是否已存在
        String businessType = addOrUpdateDTO.getBusinessType();
        List<CfgApproveSyncEntity> existList = lambdaQuery().eq(CfgApproveSyncEntity::getBusinessType, businessType).ne(CfgApproveSyncEntity::getId, addOrUpdateDTO.getId()).list();
        if(CollUtil.isNotEmpty(existList)){
            throw new ServiceException("单据名称已添加配置，不可重复添加");
        }

        //可见范围类型为不可见时。viewr才能为空
        String viewer = checkViewType(addOrUpdateDTO.getViewerType(), addOrUpdateDTO.getViewerList());
        addOrUpdateDTO.setViewer(viewer);

        CfgApproveSyncEntity cfgApproveSyncEntity =  BeanMapperUtils.map(CfgApproveSyncEntity.class, addOrUpdateDTO);

        String syncPlatform = addOrUpdateDTO.getSyncPlatformList().stream().collect(Collectors.joining(","));
        cfgApproveSyncEntity.setSyncPlatform(syncPlatform);

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
            if(count < 1 ){
                throw new ServiceException("请至少勾选一个快捷审批");
            }
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
                cfgApproveSyncFieldMapService.removeByIds(removeList.stream().map(CfgApproveSyncFieldMapEntity::getId).collect(Collectors.toList()));
            }

            List<CfgApproveSyncFieldMapEntity> newList = list.stream().filter(e -> StringUtils.isBlank(e.getId())).collect(Collectors.toList());
            if (CollUtil.isNotEmpty(newList)) {
                // 操作日志
                newList.stream().forEach(e -> {
                    String newMsg = StrUtil.format("用户【{}】新增推送消息【{}】", UserContext.getDefaultLoginUser().getUserName(), e.getFieldName());
                    operateLogService.addModuleOperateLog(newMsg, ModuleTypeEnum.CFG_APPROVE_SYNC.getCode(), cfgApproveSyncEntity.getId(), "编辑信息");
                });
                cfgApproveSyncFieldMapService.saveBatch(newList);
            }

            List<CfgApproveSyncFieldMapEntity> updateList = list.stream().filter(e -> StringUtils.isNotBlank(e.getId())).collect(Collectors.toList());
            if (CollUtil.isNotEmpty(updateList)) {
                // 操作日志
                updateList.stream().forEach(e -> {
                    CfgApproveSyncFieldMapEntity cfgApproveSyncFieldMapEntity = oldList.stream().filter(o -> e.getId().equals(o.getId())).findFirst().orElse(null);
                    if(Objects.nonNull(cfgApproveSyncFieldMapEntity)){
                        String updateMsg = StrUtil.format("用户【{}】编辑推送消息", UserContext.getDefaultLoginUser().getUserName());
                        operateLogService.addModuleOperateLogByObj(cfgApproveSyncFieldMapEntity, e, ModuleTypeEnum.CFG_APPROVE_SYNC.getCode(), cfgApproveSyncEntity.getId(), updateMsg);
                    }
                });
                cfgApproveSyncFieldMapService.updateBatchById(updateList);
            }

        }

        //新增明细--通知配置
        List<CfgApproveNoticeEntity> list = new ArrayList<>();
        addOrUpdateDTO.getApprove().setNoticeType(CfgApproveNoticeNoticeTypeEnum.APPROVE.getCode());
        addOrUpdateDTO.getApproveResult().setNoticeType(CfgApproveNoticeNoticeTypeEnum.APPROVERESULT.getCode());
        addOrUpdateDTO.getCc().setNoticeType(CfgApproveNoticeNoticeTypeEnum.CC.getCode());
        addOrUpdateDTO.getRecall().setNoticeType(CfgApproveNoticeNoticeTypeEnum.RECALL.getCode());
        List<CfgApproveNoticeDTO.NoticeSettingDTO> noticeSettingList = Arrays.asList(addOrUpdateDTO.getApprove(),addOrUpdateDTO.getApproveResult(),addOrUpdateDTO.getCc(),addOrUpdateDTO.getRecall());
        for (CfgApproveNoticeDTO.NoticeSettingDTO noticeSettingDTO : noticeSettingList) {
            if (Objects.isNull(noticeSettingDTO)) {
                continue;
            }
            //勾选了启用
            if(noticeSettingDTO.getEnableStatus()){
                if(CollUtil.isEmpty(noticeSettingDTO.getRoleTypeList()) && CollUtil.isEmpty(noticeSettingDTO.getSpecificPersonList())){
                    throw new ServiceException(CfgApproveNoticeNoticeTypeEnum.getName(noticeSettingDTO.getNoticeType()) +"勾选了启用请至少选择一个通知人员");
                }
            }
            noticeSettingDTO.setMainId(id);
            if (CollUtil.isNotEmpty(noticeSettingDTO.getRoleTypeList())) {
                noticeSettingDTO.setRoleType(noticeSettingDTO.getRoleTypeList().stream().collect(Collectors.joining(",")));
            }else {
                noticeSettingDTO.setRoleType("");
            }
            if (CollUtil.isNotEmpty(noticeSettingDTO.getSpecificPersonList())) {
                noticeSettingDTO.setSpecificPerson(noticeSettingDTO.getSpecificPersonList().stream().collect(Collectors.joining(",")));
            }else {
                noticeSettingDTO.setSpecificPerson("");
            }
            CfgApproveNoticeEntity cfgApproveNoticeEntity = new CfgApproveNoticeEntity();
            BeanMapper.copy(noticeSettingDTO,cfgApproveNoticeEntity);
            list.add(cfgApproveNoticeEntity);
        }
        if(list.size() > 0 ){
            List<CfgApproveNoticeEntity> oldList = cfgApproveNoticeService.listByMainIds(Arrays.asList(id));
            //日志
            List<String> ids = list.stream().map(CfgApproveNoticeEntity::getId).filter(StringUtils::isNotEmpty).collect(Collectors.toList());
            List<CfgApproveNoticeEntity> removeList = oldList.stream().filter(e -> !ids.contains(e.getId())).collect(Collectors.toList());
            if (CollUtil.isNotEmpty(removeList)) {
                // 操作日志
                removeList.stream().forEach(e -> {
                    String removeMsg = StrUtil.format("用户【{}】删除通知配置【{}】", UserContext.getDefaultLoginUser().getUserName(), CfgApproveNoticeNoticeTypeEnum.getName(e.getNoticeType()));
                    operateLogService.addModuleOperateLog(removeMsg, ModuleTypeEnum.CFG_APPROVE_SYNC.getCode(), cfgApproveSyncEntity.getId(), "编辑信息");
                });
                cfgApproveNoticeService.removeByIds(removeList.stream().map(CfgApproveNoticeEntity::getId).collect(Collectors.toList()));
            }

            List<CfgApproveNoticeEntity> newList = list.stream().filter(e -> StringUtils.isBlank(e.getId())).collect(Collectors.toList());
            if (CollUtil.isNotEmpty(newList)) {
                // 操作日志
                newList.stream().forEach(e -> {
                    String newMsg = StrUtil.format("用户【{}】新增通知配置【{}】", UserContext.getDefaultLoginUser().getUserName(), CfgApproveNoticeNoticeTypeEnum.getName(e.getNoticeType()));
                    operateLogService.addModuleOperateLog(newMsg, ModuleTypeEnum.CFG_APPROVE_SYNC.getCode(), cfgApproveSyncEntity.getId(), "编辑信息");
                });
                cfgApproveNoticeService.saveBatch(newList);
            }

            List<CfgApproveNoticeEntity> updateList = list.stream().filter(e -> StringUtils.isNotBlank(e.getId())).collect(Collectors.toList());
            if (CollUtil.isNotEmpty(updateList)) {
                // 操作日志
                updateList.stream().forEach(e -> {
                    CfgApproveNoticeEntity cfgApproveNoticeEntity = oldList.stream().filter(o -> e.getId().equals(o.getId())).findFirst().orElse(null);
                    if (Objects.nonNull(cfgApproveNoticeEntity)) {
                        String updateMsg = StrUtil.format("用户【{}】编辑通知配置", UserContext.getDefaultLoginUser().getUserName());
                        operateLogService.addModuleOperateLogByObj(cfgApproveNoticeEntity, e, ModuleTypeEnum.CFG_APPROVE_SYNC.getCode(), cfgApproveSyncEntity.getId(), updateMsg);
                    }
                });
                cfgApproveNoticeService.updateBatchById(updateList);
            }
        }

        //组装请求体并创建（飞书的）三方审批定义
        CreateExternalApprovalResp resp = fsService.externalApprovalsCreate(buildExternalApprovalReq(cfgApproveSyncEntity));
        lambdaUpdate().set(CfgApproveSyncEntity::getApprovalCode, resp.getData().getApprovalCode())
                .eq(CfgApproveSyncEntity::getId, id)
                .update();

        return Boolean.TRUE;
    }

    @Override
    public List<CfgApproveSyncDTO.TabListDTO> tabList(PermissionsDTO param) {
        CfgApproveSyncDTO.PagingParamDTO searchParam = new CfgApproveSyncDTO.PagingParamDTO();
        searchParam.setPermissionSql(param.getPermissionSql());
        List<CfgApproveSyncDTO.TabListDTO> list = baseMapper.tabList(searchParam);
        List<CfgApproveSyncDTO.TabListDTO> result = new ArrayList<>();
        CfgApproveSyncDTO.TabListDTO enable = list.stream().filter(e -> e.getTabFlag().equals("t")).findFirst().orElse(null);
        CfgApproveSyncDTO.TabListDTO disable = list.stream().filter(e -> e.getTabFlag().equals("f")).findFirst().orElse(null);
        result.add(new CfgApproveSyncDTO.TabListDTO("all", "全部" , 0));
        result.add(new CfgApproveSyncDTO.TabListDTO("true", "启用" , null == enable ? 0 : enable.getCount()));
        result.add(new CfgApproveSyncDTO.TabListDTO("false", "停用" ,null == disable ? 0 : disable.getCount()));
        return result;
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

            item.setSyncPlatformName(getSyncPlatformName(item.getSyncPlatform()));

            item.setEnableStatusName(Objects.equals(item.getEnableStatus(), Boolean.FALSE) ? "停用" : "启用");
        });
    }

    private static String getSyncPlatformName(String syncPlatform) {
        if(StringUtils.isBlank(syncPlatform)){
            return "";
        }
        String[] split = syncPlatform.split(",");
        StringBuffer sb = new StringBuffer();
        for (String str : split) {
            sb.append(CfgApproveSyncSyncPlatformEnum.getName(str));
            sb.append(",");
        }
        return sb.toString().substring(0, sb.length()-1);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO delete(String id) {
        CfgApproveSyncEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到ERP审批同步配置数据"));

        Integer count = approveSyncRecordService.lambdaQuery().eq(ApproveSyncRecordEntity::getCfgApproveSyncId, id).count();
        if(count > 0 ){
            throw new ServiceException("ERP审批同步配置已引用，不可删除");
        }

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
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.CFG_APPROVE_SYNC.getCode(), entity.getId(), "删除ERP审批同步配置数据");
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
            operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.CFG_APPROVE_SYNC.getCode(), entity.getId(), "更新ERP审批同步配置数据");
        }
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.UPDATE);
    }

    @Override

    public void exportList(CfgApproveSyncDTO.PagingParamDTO param, HttpServletResponse response) {
        downloadTaskFeign.saveDownloadTask("ERP审批同步配置导出", EXPORT_PROCESS_CFG_APPROVE_SYNC.getCode(), param);
    }

    @Override
    public CfgApproveSyncDTO.ViewDTO view(String id) {
        CfgApproveSyncEntity cfgApproveSyncEntity = super.getByIdOpt(id).orElseThrow(()->new ServiceException("未找到ERP审批同步配置数据"));
        // 数据填充处理
        return fillOne(cfgApproveSyncEntity);
    }

    private CfgApproveSyncDTO.ViewDTO fillOne(CfgApproveSyncEntity cfgApproveSyncEntity) {
        CfgApproveSyncDTO.ViewDTO data = new CfgApproveSyncDTO.ViewDTO();
        BeanMapper.copy(cfgApproveSyncEntity,data);

        Map<String, WorkMenuEntity> workMenuMap = workMenuService.list().stream().collect(Collectors.toMap(WorkMenuEntity::getModuleCode, item -> item));

        Map<String, DictBasicEntity> mapByType = dictBasicService.getMapByType(DictBasicEnum.TEST.getName());

        //单据
        WorkMenuEntity workMenuEntity = workMenuMap.getOrDefault(data.getBusinessType(),null);
        if(Objects.nonNull(workMenuEntity)){
            data.setBusinessTypeName(workMenuEntity.getModuleClassify());
        }
        //审批分组
        DictBasicEntity dictBasicEntity = mapByType.getOrDefault(data.getApproveGroup(),null);
        if(Objects.nonNull(dictBasicEntity)){
            data.setApproveGroupName(dictBasicEntity.getName());
        }
        //可见范围
        data.setViewerTypeName(CfgApproveSyncViewerTypeEnum.getName(data.getViewerType()));
        //可见人员
        if(StringUtils.isNotBlank(data.getViewer())){
            data.setViewerList(Arrays.asList(data.getViewer().split(",")));
        }
        //同步平台，推送方式
        String syncPlatform = cfgApproveSyncEntity.getSyncPlatform();
        data.setSyncPlatformList(Arrays.asList(syncPlatform.split(",")));

        //推送消息
        List<CfgApproveSyncFieldMapEntity> cfgApproveSyncFieldMapEntities = cfgApproveSyncFieldMapService.listByMainIds(Arrays.asList(cfgApproveSyncEntity.getId()));
        if(CollUtil.isNotEmpty(cfgApproveSyncFieldMapEntities)){
            List<CfgApproveSyncFieldMapDTO.NoticeFieldMapDTO> pushMsgList = BeanMapper.copyList(cfgApproveSyncFieldMapEntities, CfgApproveSyncFieldMapDTO.NoticeFieldMapDTO.class);
            //pushMsgList 根据sort字段进行排序，sort字段是integer类型
            pushMsgList.sort(Comparator.comparingInt(CfgApproveSyncFieldMapDTO.NoticeFieldMapDTO::getSort));
            data.setPushMsgList(pushMsgList);
        }
        //通知配置
        List<CfgApproveNoticeEntity> cfgApproveNoticeEntities = cfgApproveNoticeService.listByMainIds(Arrays.asList(cfgApproveSyncEntity.getId()));
        if(CollUtil.isNotEmpty(cfgApproveNoticeEntities)){
            List<CfgApproveNoticeDTO.NoticeSettingDTO> noticeSettingList = BeanMapper.copyList(cfgApproveNoticeEntities, CfgApproveNoticeDTO.NoticeSettingDTO.class);
            for (CfgApproveNoticeDTO.NoticeSettingDTO dto : noticeSettingList) {

                if(StringUtils.isNotBlank(dto.getRoleType())){
                    dto.setRoleTypeList(Arrays.asList(dto.getRoleType().split(",")));
                }

                if(StringUtils.isNotBlank(dto.getSpecificPerson())){
                    dto.setSpecificPersonList(Arrays.asList(dto.getSpecificPerson().split(",")));
                }

                if(dto.getNoticeType().equals(CfgApproveNoticeNoticeTypeEnum.APPROVE.getCode())){
                    data.setApprove(dto);
                }else if(dto.getNoticeType().equals(CfgApproveNoticeNoticeTypeEnum.APPROVERESULT.getCode())){
                    data.setApproveResult(dto);
                }else if(dto.getNoticeType().equals(CfgApproveNoticeNoticeTypeEnum.CC.getCode())){
                    data.setCc(dto);
                }else if(dto.getNoticeType().equals(CfgApproveNoticeNoticeTypeEnum.RECALL.getCode())){
                    data.setRecall(dto);
                }
            }
        }
        return data;
    }

    @Override
    public List<CfgApproveSyncEntity> getByBusinessType(List<String> businessTypes){
        if(CollUtil.isEmpty(businessTypes)){
            return Collections.emptyList();
        }
        return lambdaQuery().in(CfgApproveSyncEntity::getBusinessType, businessTypes).list();
    }

    //构建I18nResource数组
    @Override
    public I18nResource[] mapToI18nResouceArray(Map<String, String> values) {
        // 转换为 I18nResourceText 数组
        I18nResourceText[] i18nResourceTexts = values.entrySet().stream()
                .map(entry -> I18nResourceText.newBuilder()
                        .key(entry.getKey())
                        .value(entry.getValue())
                        .build())
                .toArray(I18nResourceText[]::new);
        // 构建 I18nResource 数组（预留多语言扩展）
        return new I18nResource[]{
                I18nResource.newBuilder()
                        .locale(LocaleEnum.LOCALE_ZH_CN.getCode())
                        .texts(i18nResourceTexts)
                        .isDefault(true)
                        .build()
        };
    }

    @Override
    public void cleanFeishuTest() {

        List<CfgApproveSyncEntity> list = lambdaQuery().in(CfgApproveSyncEntity::getBusinessType,Arrays.asList( "pilotApplication", "purchaseOrder")).list();
        for (CfgApproveSyncEntity cfgApproveSyncEntity : list) {
            String businessType = cfgApproveSyncEntity.getBusinessType();
            List<String> ids = processManagementService.getTestList(businessType);
            del( cfgApproveSyncEntity,ids);

        }



    }

    public void del(CfgApproveSyncEntity cfgApproveSyncEntity,List<String> ids){
            String errorReason= "";
            //pc地址
            String pcLinkByEnv = cfgSettingService.getPcLinkByEnv();
            //erp审批同步配置表
            //国际化文案
            Map<String,String> values = new HashMap<>();
            //标题
            values.put("@i18n@title",cfgApproveSyncEntity.getTitle());
            values.put("@i18n@userName","测试");

            //获取创建时间以及更新时间
            LocalDateTime createTime = LocalDateTime.now();
            LocalDateTime updateTime = LocalDateTime.now();
            // 转换为毫秒时间戳
            String createTimeMillis = String.valueOf(createTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
            String updateTimeMillis = String.valueOf(updateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
            String endTimeMillis = String.valueOf(updateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
            ExternalInstance externalInstance = ExternalInstance.newBuilder()
                    .approvalCode(cfgApproveSyncEntity.getApprovalCode())
                    .status("TERMINATED")
                    .links(ExternalInstanceLink.newBuilder()
                            .pcLink(pcLinkByEnv)
                            .mobileLink(pcLinkByEnv)
                            .build())
                    .title("@i18n@title")
                    .userName("@i18n@userName")
                    .openId("ou_bf8736201fd34d971a12c65d966251ec")
                    .startTime(createTimeMillis)//审批发起时间
                    .endTime(endTimeMillis) //审批实例结束时间。未结束的审批为 0，Unix 毫秒时间戳。
                    .updateTime(updateTimeMillis)//审批实例最近更新时间
                    .displayMethod("BROWSER")//列表页打开审批实例的方式。 BROWSER：跳转系统默认浏览器打开, SIDEBAR：飞书中侧边抽屉打开, NORMAL：飞书内嵌页面打开
                    .updateMode("REPLACE")//更新方式。 REPLACE：全量替换, UPDATE：增量更新
                    .build();

            //国际化文案数组
            I18nResource[] i18nResources = configApproveSyncService.mapToI18nResouceArray(values);
            externalInstance.setI18nResources(i18nResources);
        for (String id : ids) {
            externalInstance.setInstanceId(id);

            // 创建请求对象
            CreateExternalInstanceReq req = CreateExternalInstanceReq.newBuilder()
                    .externalInstance(externalInstance)
                    .build();

            CreateExternalInstanceResp resp = fsService.createExternalInstance(req);
            if (!resp.success()) {
                String msg = String.format("同步三方审批实例失败:id：%s,code:%s,msg:%s,reqId:%s", id,resp.getCode(), resp.getMsg(), resp.getRequestId());
                log.error("{}", msg);
            }
        }
    }

}
