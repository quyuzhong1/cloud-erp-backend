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
import com.erp.model.workflow.enums.CfgApproveNoticeNoticeTypeEnum;
import com.erp.model.workflow.enums.CfgApproveSyncSyncPlatformEnum;
import com.erp.model.workflow.enums.CfgApproveSyncViewerTypeEnum;
import com.erp.model.workflow.enums.DictBasicEnum;
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
import com.google.gson.Gson;
import com.lark.oapi.service.approval.v4.model.*;
import org.springframework.stereotype.Service;
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
        addDTO.getTimeoutWarning().setNoticeType(CfgApproveNoticeNoticeTypeEnum.TIMEOUTWARNING.getCode());
        addDTO.getRecall().setNoticeType(CfgApproveNoticeNoticeTypeEnum.RECALL.getCode());
        List<CfgApproveNoticeDTO.NoticeSettingDTO> noticeSettingList = Arrays.asList(addDTO.getApprove(),addDTO.getApproveResult(),addDTO.getCc(),addDTO.getTimeoutWarning(),addDTO.getRecall());
        for (CfgApproveNoticeDTO.NoticeSettingDTO noticeSettingDTO : noticeSettingList) {
            if (Objects.isNull(noticeSettingDTO)) {
                continue;
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
        if(cfgApproveNoticeEntityList.size() > 0 ){
            cfgApproveNoticeService.saveBatch(cfgApproveNoticeEntityList);
        }

        //组装请求体并创建（飞书的）三方审批定义
        CreateExternalApprovalResp resp = fsService.externalApprovalsCreate(buildexternalApprovalReq(cfgApproveSyncEntity));
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
    private CreateExternalApprovalReq buildexternalApprovalReq(CfgApproveSyncEntity cfgApproveSyncEntity) {
        //三方审批相关信息
        ExternalApproval externalApproval = getExternalApproval(cfgApproveSyncEntity);
        //国际化文案
        I18nResource[] i18nResources = getI18nResources(cfgApproveSyncEntity);
        externalApproval.setI18nResources(i18nResources);
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
        List<SysRefererConfigEntity> refererConfig = sysRefereConfigFeign.getByReferer(CfgApproveSyncSyncPlatformEnum.FEISHU.getCode());
        String referer = "";
        if(CollUtil.isNotEmpty(refererConfig)){
            referer = refererConfig.get(0).getReferer();
        }

        ExternalApproval externalApproval = ExternalApproval.newBuilder()
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
                        .actionCallbackToken(referer)
                        .actionCallbackKey(referer)
                        .build())
                .build();
        return externalApproval;
    }

    //可见人列表
    private void getApprovalViewers(CfgApproveSyncEntity cfgApproveSyncEntity, ExternalApproval externalApproval) {
        //审批可见人列表，列表长度上限 200，只有在审批可见人列表内的用户，才可以在审批发起页看到该审批。若该参数不传值，则表示任何人不可见。
        List<ApprovalCreateViewers> approvalCreateViewers = new ArrayList<>();
        String viewer = cfgApproveSyncEntity.getViewer();
        if(cfgApproveSyncEntity.getViewerType().equals(CfgApproveSyncViewerTypeEnum.USER.getCode())){
            if(StringUtils.isNotBlank(viewer)){
                List<String> viewerList = Arrays.asList(viewer.split(","));
                //查询飞书的用户第三方信息
                List<ThirdUnionDTO> thirdUnionList = sysUserFeign.getThirdUnionIdsByUserIds(ThirdpartyPlatformEnum.FS.getCode(), viewerList);
                if(CollUtil.isEmpty(thirdUnionList)){
                    throw new ServiceException("审批可见人列表人员未关联第三方用户信息");
                }else{
                    List<String> thirdUnionIds = thirdUnionList.stream().map(ThirdUnionDTO::getThirdUnionId).distinct().collect(Collectors.toList());
                    for (String userId : thirdUnionIds) {
                        ApprovalCreateViewers approvalCreateViewer = ApprovalCreateViewers.newBuilder()
                                .viewerType(cfgApproveSyncEntity.getViewerType())
                                .viewerUserId(userId)
                                .build();
                        approvalCreateViewers.add(approvalCreateViewer);
                    }
                }
            }
        }else if(cfgApproveSyncEntity.getViewerType().equals(CfgApproveSyncViewerTypeEnum.DEPARTMENT.getCode())){
            if(StringUtils.isNotBlank(viewer)){
                List<String> viewerList = Arrays.asList(viewer.split(","));
                for (String deptId : viewerList) {
                    ApprovalCreateViewers approvalCreateViewer = ApprovalCreateViewers.newBuilder()
                            .viewerType(cfgApproveSyncEntity.getViewerType())
                            .viewerDepartmentId(deptId)
                            .build();
                    approvalCreateViewers.add(approvalCreateViewer);
                }
            }
        }
        //上限200
        if(approvalCreateViewers.size() > 200){
            throw new ServiceException("审批可见人列表不能超过200上限");
        }else if(approvalCreateViewers.size() == 0 && (cfgApproveSyncEntity.getViewerType().equals(CfgApproveSyncViewerTypeEnum.DEPARTMENT.getCode()) || cfgApproveSyncEntity.getViewerType().equals(CfgApproveSyncViewerTypeEnum.USER.getCode()))){
            throw new ServiceException("指定部门/指定用户时，审批可见人列表不能为空");
        }else if(approvalCreateViewers.size() > 0){
            ApprovalCreateViewers[] array = (ApprovalCreateViewers[]) approvalCreateViewers.toArray();
            externalApproval.setViewers(array);
        }
    }

    //国际化文案
    private I18nResource[] getI18nResources(CfgApproveSyncEntity cfgApproveSyncEntity) {
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


        Set<Map.Entry<String, String>> entries = values.entrySet();
        I18nResource[] i18nResources = new I18nResource[1];
        I18nResourceText[] i18nResourceTexts = new I18nResourceText[entries.size()];
        int i = 0;
        for (Map.Entry<String, String> entry : entries) {
            i18nResourceTexts[i++] = I18nResourceText.newBuilder()
                    .key(entry.getKey())
                    .value(entry.getValue())
                    .build();
        }
        i18nResources[0] = I18nResource.newBuilder()
                .locale("zh-CN")
                .texts(i18nResourceTexts)
                .isDefault(true)
                .build();

        return i18nResources;
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
                        String updateMsg = StrUtil.format("用户【{}】编辑推送消息", UserContext.getDefaultLoginUser().getUserName());
                        operateLogService.addModuleOperateLogByObj(cfgApproveSyncFieldMapEntity, e, ModuleTypeEnum.CFG_APPROVE_SYNC.getCode(), cfgApproveSyncEntity.getId(), updateMsg);
                    }
                });
            }
            cfgApproveSyncFieldMapService.saveOrUpdateBatch(list);
        }

        //新增明细--通知配置
        List<CfgApproveNoticeEntity> list = new ArrayList<>();
        addOrUpdateDTO.getApprove().setNoticeType(CfgApproveNoticeNoticeTypeEnum.APPROVE.getCode());
        addOrUpdateDTO.getApproveResult().setNoticeType(CfgApproveNoticeNoticeTypeEnum.APPROVERESULT.getCode());
        addOrUpdateDTO.getCc().setNoticeType(CfgApproveNoticeNoticeTypeEnum.CC.getCode());
        addOrUpdateDTO.getTimeoutWarning().setNoticeType(CfgApproveNoticeNoticeTypeEnum.TIMEOUTWARNING.getCode());
        addOrUpdateDTO.getRecall().setNoticeType(CfgApproveNoticeNoticeTypeEnum.RECALL.getCode());
        List<CfgApproveNoticeDTO.NoticeSettingDTO> noticeSettingList = Arrays.asList(addOrUpdateDTO.getApprove(),addOrUpdateDTO.getApproveResult(),addOrUpdateDTO.getCc(),addOrUpdateDTO.getTimeoutWarning(),addOrUpdateDTO.getRecall());
        for (CfgApproveNoticeDTO.NoticeSettingDTO noticeSettingDTO : noticeSettingList) {
            if (Objects.isNull(noticeSettingDTO)) {
                continue;
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
            }

            List<CfgApproveNoticeEntity> newList = list.stream().filter(e -> StringUtils.isBlank(e.getId())).collect(Collectors.toList());
            if (CollUtil.isNotEmpty(newList)) {
                // 操作日志
                newList.stream().forEach(e -> {
                    String newMsg = StrUtil.format("用户【{}】新增通知配置【{}】", UserContext.getDefaultLoginUser().getUserName(), CfgApproveNoticeNoticeTypeEnum.getName(e.getNoticeType()));
                    operateLogService.addModuleOperateLog(newMsg, ModuleTypeEnum.CFG_APPROVE_SYNC.getCode(), cfgApproveSyncEntity.getId(), "编辑信息");
                });
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
            }
            cfgApproveNoticeService.saveOrUpdateBatch(list);
        }

        //组装请求体并创建（飞书的）三方审批定义
        CreateExternalApprovalResp resp = fsService.externalApprovalsCreate(buildexternalApprovalReq(cfgApproveSyncEntity));
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
                }else if(dto.getNoticeType().equals(CfgApproveNoticeNoticeTypeEnum.TIMEOUTWARNING.getCode())){
                    data.setTimeoutWarning(dto);
                }else if(dto.getNoticeType().equals(CfgApproveNoticeNoticeTypeEnum.RECALL.getCode())){
                    data.setRecall(dto);
                }
            }
        }
        return data;
    }
}
