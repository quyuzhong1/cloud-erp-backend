package com.erp.server.sys.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.OperationTypeEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.sys.dto.CfgApproveSyncFieldMapDTO;
import com.erp.model.sys.dto.CfgRuleConditionDTO;
import com.erp.model.sys.dto.DictBasicDTO;
import com.erp.model.sys.entity.CfgApproveSyncFieldMapEntity;
import com.erp.model.sys.entity.CfgRuleConditionEntity;
import com.erp.model.sys.entity.CfgThirdNoticeEntity;
import com.erp.model.sys.enums.CfgThirdNoticeMethodEnum;
import com.erp.model.sys.enums.RuleTypeEnum;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.sys.mapper.CfgThirdNoticeMapper;
import com.erp.server.sys.service.*;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.core.exception.ServiceException;
import jodd.util.StringUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.sys.dto.CfgThirdNoticeDTO;
import java.util.*;
import java.util.stream.Collectors;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_SYS_THIRD_NOTICE;

/**
 * <p>
 * 三方通知配置 服务实现类
 * </p>
 *
 * @author jack
 * @since 2025-05-23
 */
@Slf4j
@Service
public class CfgThirdNoticeServiceImpl extends SuperServiceImpl<CfgThirdNoticeMapper, CfgThirdNoticeEntity> implements CfgThirdNoticeService {
    @Resource
    private OperateLogService operateLogService;

    @Resource
    private SysUserFeign sysUserFeign;
    @Resource
    private DictBasicService dictBasicService;
    @Resource
    private CfgApproveSyncFieldMapService cfgApproveSyncFieldMapService;
    @Resource
    private CfgRuleConditionService cfgRuleConditionService;
    @Resource
    private DownloadTaskFeign downloadTaskFeign;
    @Resource
    private Validator validator;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(CfgThirdNoticeDTO.AddDTO addDTO) {
        String method = addDTO.getMethod();
        List<CfgApproveSyncFieldMapDTO.NoticeFieldMapDTO> pushMsgList = addDTO.getPushMsgList();
        //可能存在一条空数据，需要排除掉
        handlePushMsg(pushMsgList, method);

        //可能存在一条空数据，需要排除掉
        List<CfgRuleConditionDTO.Add> conditionList = addDTO.getConditionList();
        handleAddConditionList(conditionList);

        CfgThirdNoticeEntity cfgThirdNoticeEntity = new CfgThirdNoticeEntity();
        BeanMapperUtils.copy(addDTO, cfgThirdNoticeEntity);

        if(CollUtil.isNotEmpty(addDTO.getRoleTypeList())){
            String roleType = String.join(",", addDTO.getRoleTypeList());
            cfgThirdNoticeEntity.setRoleType(roleType);
        }else {
            cfgThirdNoticeEntity.setRoleType("");
        }

        if(CollUtil.isNotEmpty(addDTO.getSpecificPersonList())){
            String specificPerson = String.join(",", addDTO.getSpecificPersonList());
            cfgThirdNoticeEntity.setSpecificPerson(specificPerson);
        }else{
            cfgThirdNoticeEntity.setSpecificPerson("");
        }

        if(CollUtil.isNotEmpty(addDTO.getNoticeMethodList())){
            String noticeMethod = String.join(",", addDTO.getNoticeMethodList());
            cfgThirdNoticeEntity.setNoticeMethod(noticeMethod);
        }else{
            cfgThirdNoticeEntity.setNoticeMethod("");
        }

        log.info("开始新增三方通知配置");
        boolean save = super.save(cfgThirdNoticeEntity);
        if(!save) {
            throw new ServiceException("三方通知配置保存失败");
        }
        // 操作日志
        String id = cfgThirdNoticeEntity.getId();
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "三方通知配置" , cfgThirdNoticeEntity.getId());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.CFG_THIRD_NOTICE.getCode(), cfgThirdNoticeEntity.getId(), "新增操作");
        //新增明细--推送信息
        List<CfgApproveSyncFieldMapEntity> fieldMapEntityList;
        if(CollUtil.isNotEmpty(pushMsgList)){
            int sort = 1;
            for (CfgApproveSyncFieldMapDTO.NoticeFieldMapDTO noticeFieldMapDTO : pushMsgList) {
                noticeFieldMapDTO.setMainId(id);
                noticeFieldMapDTO.setSort(sort++);
            }
            fieldMapEntityList = BeanMapper.copyList(pushMsgList, CfgApproveSyncFieldMapEntity.class);
            cfgApproveSyncFieldMapService.saveBatch(fieldMapEntityList);
        }
        //保存规则条件
        if(CollUtil.isNotEmpty(conditionList)){
            cfgRuleConditionService.saveRuleCondition(id, conditionList, RuleTypeEnum.CFG_THIRD_NOTICE.getCode());
        }
        return new BaseResultDTO.AddDTO(cfgThirdNoticeEntity.getId(), cfgThirdNoticeEntity.getId());
    }

    private void handlePushMsg(List<CfgApproveSyncFieldMapDTO.NoticeFieldMapDTO> pushMsgList, String method) {
        if(CollUtil.isEmpty(pushMsgList)){
            return ;
        }
        CfgApproveSyncFieldMapDTO.NoticeFieldMapDTO dto = pushMsgList.stream().filter(e -> StringUtils.isBlank(e.getFieldName()) && StringUtils.isBlank(e.getFieldSource())).findFirst().orElse(null);
        if(Objects.nonNull(dto)){
            pushMsgList.remove(dto);
        }
        if(Objects.equals(method,CfgThirdNoticeMethodEnum.SINGLE.getCode())){//通知方式：单条
            //推送信息不能为空
            if(CollUtil.isEmpty(pushMsgList)){
                throw new ServiceException("推送信息不能为空");
            }else {
                for (CfgApproveSyncFieldMapDTO.NoticeFieldMapDTO noticeFieldMapDTO : pushMsgList) {
                    Set<ConstraintViolation<CfgApproveSyncFieldMapDTO.NoticeFieldMapDTO>> violations = validator.validate(noticeFieldMapDTO);
                    if (!violations.isEmpty()) {
                        ConstraintViolation<CfgApproveSyncFieldMapDTO.NoticeFieldMapDTO> firstViolation = violations.iterator().next();
                        String message = firstViolation.getMessage();
                        throw new ServiceException(message); // 或者自定义异常处理
                    }
                }
            }
        }
    }

    private void handleAddConditionList(List<CfgRuleConditionDTO.Add> conditionList) {
        if(CollUtil.isEmpty(conditionList)){
            return ;
        }
        CfgRuleConditionDTO.Add conditionDTO = conditionList.stream().filter(e -> StringUtils.isBlank(e.getField()) && StringUtils.isBlank(e.getCompare())).findFirst().orElse(null);
        if(Objects.nonNull(conditionDTO)){
            conditionList.remove(conditionDTO);
        }
        if(CollUtil.isNotEmpty(conditionList)){
            for (CfgRuleConditionDTO.Add condition : conditionList) {
                Set<ConstraintViolation<CfgRuleConditionDTO.Add>> violations = validator.validate(condition);
                if (!violations.isEmpty()) {
                    ConstraintViolation<CfgRuleConditionDTO.Add> firstViolation = violations.iterator().next();
                    String message = firstViolation.getMessage();
                    throw new ServiceException(message); // 或者自定义异常处理
                }
            }
        }
    }
    private void handleUpdateConditionList(List<CfgRuleConditionDTO.Update> conditionList) {
        if(CollUtil.isEmpty(conditionList)){
            return ;
        }
        CfgRuleConditionDTO.Update conditionDTO = conditionList.stream().filter(e ->StringUtils.isBlank(e.getId()) && StringUtils.isBlank(e.getField()) && StringUtils.isBlank(e.getCompare())).findFirst().orElse(null);
        if(Objects.nonNull(conditionDTO)){
            conditionList.remove(conditionDTO);
        }
        if(CollUtil.isNotEmpty(conditionList)){
            for (CfgRuleConditionDTO.Update condition : conditionList) {
                Set<ConstraintViolation<CfgRuleConditionDTO.Update>> violations = validator.validate(condition);
                if (!violations.isEmpty()) {
                    ConstraintViolation<CfgRuleConditionDTO.Update> firstViolation = violations.iterator().next();
                    String message = firstViolation.getMessage();
                    throw new ServiceException(message); // 或者自定义异常处理
                }
            }
        }
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(CfgThirdNoticeDTO.UpdateDTO addOrUpdateDTO) {
        String method = addOrUpdateDTO.getMethod();
        List<CfgApproveSyncFieldMapDTO.NoticeFieldMapDTO> pushMsgList = addOrUpdateDTO.getPushMsgList();
        //可能存在一条空数据，需要排除掉
        handlePushMsg(pushMsgList, method);

        //可能存在一条空数据，需要排除掉
        List<CfgRuleConditionDTO.Update> conditionList = addOrUpdateDTO.getConditionList();
        handleUpdateConditionList(conditionList);

        CfgThirdNoticeEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "三方通知配置"));
        CfgThirdNoticeEntity cfgThirdNoticeEntity =  BeanMapperUtils.map(CfgThirdNoticeEntity.class, addOrUpdateDTO);

        if(CollUtil.isNotEmpty(addOrUpdateDTO.getRoleTypeList())){
            String roleType = String.join(",", addOrUpdateDTO.getRoleTypeList());
            cfgThirdNoticeEntity.setRoleType(roleType);
        }else{
            cfgThirdNoticeEntity.setRoleType("");
        }
        if(CollUtil.isNotEmpty(addOrUpdateDTO.getSpecificPersonList())){
            String specificPerson = String.join(",", addOrUpdateDTO.getSpecificPersonList());
            cfgThirdNoticeEntity.setSpecificPerson(specificPerson);
        }else{
            cfgThirdNoticeEntity.setSpecificPerson("");
        }

        if(CollUtil.isNotEmpty(addOrUpdateDTO.getNoticeMethodList())){
            String noticeMethod = String.join(",", addOrUpdateDTO.getNoticeMethodList());
            cfgThirdNoticeEntity.setNoticeMethod(noticeMethod);
        }else{
            cfgThirdNoticeEntity.setNoticeMethod("");
        }

        log.info("编辑 开始修改三方通知配置数据，id：【{}】", old.getId());
        boolean save = super.updateById(cfgThirdNoticeEntity);
        if(!save) {
            throw new ServiceException("三方通知配置保存失败");
        }

        // 记录主单操作日志
        log.info("编辑 开始记录三方通知配置日志数据，id：【{}】", cfgThirdNoticeEntity.getId());
        String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), cfgThirdNoticeEntity.getId(), "三方通知配置");
        operateLogService.addModuleOperateLogByObj(old, cfgThirdNoticeEntity, ModuleTypeEnum.CFG_THIRD_NOTICE.getCode(), cfgThirdNoticeEntity.getId(), msg);

        String id = addOrUpdateDTO.getId();
        //新增明细--推送信息
        if(CollUtil.isNotEmpty(pushMsgList)){
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
                    operateLogService.addModuleOperateLog(removeMsg, ModuleTypeEnum.CFG_APPROVE_SYNC.getCode(), cfgThirdNoticeEntity.getId(), "编辑信息");
                });
            }

            List<CfgApproveSyncFieldMapEntity> newList = list.stream().filter(e -> StringUtils.isBlank(e.getId())).collect(Collectors.toList());
            if (CollUtil.isNotEmpty(newList)) {
                // 操作日志
                newList.stream().forEach(e -> {
                    String newMsg = StrUtil.format("用户【{}】新增推送消息【{}】", UserContext.getDefaultLoginUser().getUserName(), e.getFieldName());
                    operateLogService.addModuleOperateLog(newMsg, ModuleTypeEnum.CFG_APPROVE_SYNC.getCode(), cfgThirdNoticeEntity.getId(), "编辑信息");
                });
            }

            List<CfgApproveSyncFieldMapEntity> updateList = list.stream().filter(e -> StringUtils.isNotBlank(e.getId())).collect(Collectors.toList());
            if (CollUtil.isNotEmpty(updateList)) {
                // 操作日志
                updateList.stream().forEach(e -> {
                    CfgApproveSyncFieldMapEntity cfgApproveSyncFieldMapEntity = oldList.stream().filter(o -> e.getId().equals(o.getId())).findFirst().orElse(null);
                    if(Objects.nonNull(cfgApproveSyncFieldMapEntity)){
                        String updateMsg = StrUtil.format("用户【{}】编辑推送消息", UserContext.getDefaultLoginUser().getUserName());
                        operateLogService.addModuleOperateLogByObj(cfgApproveSyncFieldMapEntity, e, ModuleTypeEnum.CFG_APPROVE_SYNC.getCode(), cfgThirdNoticeEntity.getId(), updateMsg);
                    }
                });
            }
            cfgApproveSyncFieldMapService.saveOrUpdateBatch(list);
        }

        //规则条件
        cfgRuleConditionService.updateRuleCondition(addOrUpdateDTO.getId(), addOrUpdateDTO.getConditionList(), ModuleTypeEnum.CFG_THIRD_NOTICE.getCode(), RuleTypeEnum.CFG_THIRD_NOTICE.getCode());

        return Boolean.TRUE;
    }

    @Override
    public List<CfgThirdNoticeDTO.TabListDTO> tabList(PermissionsDTO param) {
        CfgThirdNoticeDTO.PagingParamDTO searchParam = new CfgThirdNoticeDTO.PagingParamDTO();
        searchParam.setPermissionSql(param.getPermissionSql());
        List<CfgThirdNoticeDTO.TabListDTO> list = baseMapper.tabList(searchParam);
        List<CfgThirdNoticeDTO.TabListDTO> result = new ArrayList<>();
        CfgThirdNoticeDTO.TabListDTO enable = list.stream().filter(e -> e.getTabFlag().equals("t")).findFirst().orElse(null);
        CfgThirdNoticeDTO.TabListDTO disable = list.stream().filter(e -> e.getTabFlag().equals("f")).findFirst().orElse(null);
        result.add(new CfgThirdNoticeDTO.TabListDTO("all", "全部" , 0));
        result.add(new CfgThirdNoticeDTO.TabListDTO("true", "启用" , null == enable ? 0 : enable.getCount()));
        result.add(new CfgThirdNoticeDTO.TabListDTO("false", "停用" ,null == disable ? 0 : disable.getCount()));
        return result;
    }

    @Override
    public PagingVO<CfgThirdNoticeDTO.ListDTO> paging(PagingDTO<CfgThirdNoticeDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<CfgThirdNoticeDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
            return new PagingVO(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO(pageData);
    }

    private void fillList(List<CfgThirdNoticeDTO.ListDTO> records) {
        //飞书
        for (CfgThirdNoticeDTO.ListDTO record : records) {

            //单据类型
            String businessType = record.getBusinessType();
            record.setBusinessTypeName(SourceTypeEnum.getName(businessType));

            String method = record.getMethod();
            record.setMethodName(CfgThirdNoticeMethodEnum.getName(method));

            if (StringUtils.isNotBlank(record.getCron())) {
                record.setCronType(record.getCron());
            } else {
                record.setCronType("即时通知");
            }

            record.setNoticeStatusName(record.getNoticeStatus().equals(Boolean.FALSE) ? "停用" : "启用");

            if (StringUtils.isNotBlank(record.getRoleType())) {
                List<DictBasicDTO.ViewDTO> noticeItemPeople = dictBasicService.listByType("noticeItemPeople");
                Map<String, String> noticeItemPeopleMap = noticeItemPeople.stream().collect(Collectors.toMap(DictBasicDTO.ViewDTO::getValue, DictBasicDTO.ViewDTO::getName));
                List<String> roleTypes = Arrays.asList(record.getRoleType().split(","));
                if (CollUtil.isNotEmpty(roleTypes)) {
                    record.setRoleTypeList(roleTypes);

                    List<String> roleTypeNameList = roleTypes.stream().map(e -> noticeItemPeopleMap.getOrDefault(e, "")).filter(StringUtil::isNotBlank).collect(Collectors.toList());
                    record.setRoleTypeNameList(roleTypeNameList);

                    record.setRoleType(roleTypeNameList.stream().collect(Collectors.joining(",")));
                }
            }

            if (StringUtils.isNotBlank(record.getSpecificPerson())) {
                List<String> userIds = Arrays.asList(record.getSpecificPerson().split(","));
                List<FindUserDTO> userList = sysUserFeign.getUserListByUserIds(userIds);
                if (CollUtil.isNotEmpty(userList)) {
                    record.setSpecificPersonList(userIds);

                    List<String> specificPersonNameList = userList.stream().map(FindUserDTO::getUserName).collect(Collectors.toList());
                    record.setSpecificPersonNameList(specificPersonNameList);

                    record.setSpecificPerson(specificPersonNameList.stream().collect(Collectors.joining(",")));
                }
            }
        }
    }

    @Override
    public CfgThirdNoticeDTO.ViewDTO view(String id) {
        CfgThirdNoticeEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到三方通知配置数据"));
        // 数据填充处理
        return fillOne(entity);

    }

    private CfgThirdNoticeDTO.ViewDTO fillOne(CfgThirdNoticeEntity entity) {
        CfgThirdNoticeDTO.ViewDTO data = new CfgThirdNoticeDTO.ViewDTO();
        BeanMapper.copy(entity,data);

        //单据类型
        String businessType = data.getBusinessType();
        data.setBusinessTypeName(SourceTypeEnum.getName(businessType));

        String method = data.getMethod();
        data.setMethodName(CfgThirdNoticeMethodEnum.getName(method));

        if(StringUtils.isNotBlank(data.getRoleType())){
            List<DictBasicDTO.ViewDTO> noticeItemPeople = dictBasicService.listByType("noticeItemPeople");
            Map<String, String> noticeItemPeopleMap = noticeItemPeople.stream().collect(Collectors.toMap(DictBasicDTO.ViewDTO::getValue, DictBasicDTO.ViewDTO::getName));
            List<String> roleTypes = Arrays.asList(data.getRoleType().split(","));
            if(CollUtil.isNotEmpty(roleTypes)){
                data.setRoleTypeList(roleTypes);
                data.setRoleTypeNameList(roleTypes.stream().map(e -> noticeItemPeopleMap.get(e)).collect(Collectors.toList()));
            }
        }

        if(StringUtils.isNotBlank(data.getSpecificPerson())){
            List<String> userIds = Arrays.asList(data.getSpecificPerson().split(","));
            List<FindUserDTO> userList = sysUserFeign.getUserListByUserIds(userIds);
            if(CollUtil.isNotEmpty(userList)){
                List<String> specificPersonList = userList.stream().map(FindUserDTO::getUserId).collect(Collectors.toList());
                List<String> specificPersonNameList = userList.stream().map(FindUserDTO::getUserName).collect(Collectors.toList());
                data.setSpecificPersonList(specificPersonList);
                data.setSpecificPersonNameList(specificPersonNameList);
            }
        }
        if(StringUtils.isNotBlank(data.getNoticeMethod())){
            data.setNoticeMethodList(Arrays.asList(data.getNoticeMethod().split(",")));
        }

        //推送信息
        List<CfgApproveSyncFieldMapEntity> cfgApproveSyncFieldMapEntities = cfgApproveSyncFieldMapService.listByMainIds(Arrays.asList(entity.getId()));
        if(CollUtil.isNotEmpty(cfgApproveSyncFieldMapEntities)){
            List<CfgApproveSyncFieldMapDTO.NoticeFieldMapDTO> pushMsgList = BeanMapper.copyList(cfgApproveSyncFieldMapEntities, CfgApproveSyncFieldMapDTO.NoticeFieldMapDTO.class);
            data.setPushMsgList(pushMsgList);
        }

        //查询规则条件
        List<CfgRuleConditionEntity> ruleConditionEntities = cfgRuleConditionService.list(Wrappers.<CfgRuleConditionEntity>lambdaQuery()
                .eq(CfgRuleConditionEntity::getRuleId, entity.getId())
                .orderByAsc(CfgRuleConditionEntity::getIndex));
        List<CfgRuleConditionDTO.View> ruleConditions = BeanMapperUtils.copyList(CfgRuleConditionDTO.View.class, ruleConditionEntities);
        data.setConditionList(ruleConditions);
        return data;
    }

    @Override
    public BatchResultDTO delete(String id) {
        CfgThirdNoticeEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到三方通知配置数据"));
        // 删除主单数据
        super.removeById(id);
        // 删除子表
        cfgApproveSyncFieldMapService.lambdaUpdate()
                .set(CfgApproveSyncFieldMapEntity::getIsDeleted, Boolean.TRUE)
                .eq(CfgApproveSyncFieldMapEntity::getMainId, id)
                .update();

        // 删除日志数据
        String msg = StrUtil.format("用户【{}】操作【{}】单据删除操作 ", UserContext.getDefaultLoginUser().getUserName(), "三方通知配置");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.CFG_THIRD_NOTICE.getCode(), entity.getId(), "删除三方通知配置数据");
        return BatchResultDTO.success(entity.getId(), entity.getId(), OperationTypeEnum.DELETE);
    }

    @Override
    public BatchResultDTO enable(String id, Boolean noticeStatus) {
        CfgThirdNoticeEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到三方通知配置数据"));
        if(!entity.getNoticeStatus().equals(noticeStatus)){
            lambdaUpdate()
                    .set(CfgThirdNoticeEntity::getNoticeStatus, noticeStatus)
                    .eq(CfgThirdNoticeEntity::getId, id)
                    .update();
            // 日志
            String msg = StrUtil.format("用户【{}】操作【{}】单据变更为【{}】 ", UserContext.getDefaultLoginUser().getUserName(),  "三方通知配置",Objects.equals(noticeStatus, Boolean.FALSE) ? "停用" : "启用");
            operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.CFG_THIRD_NOTICE.getCode(), entity.getId(), "更新三方通知配置数据");
        }
        return BatchResultDTO.success(entity.getId(), entity.getId(), OperationTypeEnum.UPDATE);
    }

    @Override
    public void exportList(CfgThirdNoticeDTO.PagingParamDTO param, HttpServletResponse response) {
        downloadTaskFeign.saveDownloadTask("三方通知配置导出", EXPORT_SYS_THIRD_NOTICE.getCode(), param);
    }
}
