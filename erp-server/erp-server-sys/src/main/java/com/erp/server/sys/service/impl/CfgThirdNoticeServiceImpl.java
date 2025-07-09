package com.erp.server.sys.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.OperationTypeEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.vo.PagingVO;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.dmp.entity.BiDeliveryDetailItemEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.sys.dto.*;
import com.erp.model.sys.entity.*;
import com.erp.model.sys.enums.CfgThirdNoticeMethodEnum;
import com.erp.model.sys.enums.RuleTypeEnum;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.sys.feign.SysPostFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.sys.mapper.CfgThirdNoticeMapper;
import com.erp.server.sys.service.*;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.core.exception.ServiceException;
import jodd.util.StringUtil;
import org.apache.commons.lang3.StringUtils;
import org.redisson.executor.CronExpression;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolation;
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
    @Resource
    private MQProducerService mqProducerService;
    @Resource
    private SysPostFeign sysPostFeign;
    @Resource
    private DictNoticeRoleOptionService dictNoticeRoleOptionService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(CfgThirdNoticeDTO.AddDTO addDTO) {
        //校验通知人员不能全部为空
        List<String> roleTypeList = addDTO.getRoleTypeList();
        List<String> specificPersonList = addDTO.getSpecificPersonList();
        List<String> postIdList = addDTO.getPostIdList();
        checkNoticeUserNotEmpty(roleTypeList, specificPersonList, postIdList);

        String method = addDTO.getMethod();
        List<CfgApproveSyncFieldMapDTO.NoticeFieldMapDTO> pushMsgList = addDTO.getPushMsgList();
        //可能存在一条空数据，需要排除掉
        handlePushMsg(pushMsgList, method);

        //可能存在一条空数据，需要排除掉
        List<CfgRuleConditionDTO.Add> conditionList = addDTO.getConditionList();
        handleAddConditionList(conditionList);

        //校验重复
        String businessType = addDTO.getBusinessType();
        //相同单据类型 + 通知方式 通知配置
        checkDuplication("",businessType, method, conditionList);

        CfgThirdNoticeEntity cfgThirdNoticeEntity = new CfgThirdNoticeEntity();
        BeanMapperUtils.copy(addDTO, cfgThirdNoticeEntity);

        if(CollUtil.isNotEmpty(addDTO.getPostIdList())){
            String post = String.join(",", addDTO.getPostIdList());
            cfgThirdNoticeEntity.setPost(post);
        }else {
            cfgThirdNoticeEntity.setPost("");
        }
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
        //单据均为即时推送
        if(Objects.equals(cfgThirdNoticeEntity.getMethod(),CfgThirdNoticeMethodEnum.SINGLE.getCode())){
            cfgThirdNoticeEntity.setCron("");
        }else {
            // 校验cron表达式
            boolean isValid = CronExpression.isValidExpression(addDTO.getCron());
            if(Boolean.FALSE.equals(isValid)){
                throw new ServiceException("cron表达式不合法");
            }
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

    private void checkDuplication(String id , String businessType, String method, List<CfgRuleConditionDTO.Add> conditionList) {
        LambdaQueryWrapper<CfgThirdNoticeEntity> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(CfgThirdNoticeEntity::getBusinessType, businessType);
        lambdaQueryWrapper.eq(CfgThirdNoticeEntity::getMethod, method);
        lambdaQueryWrapper.ne(CfgThirdNoticeEntity::getId, id);
        List<CfgThirdNoticeEntity> oldList = this.list(lambdaQueryWrapper);
        if(CollUtil.isNotEmpty(oldList)){
            //新规则条件
            Set<CompareDTO> newCompareSet = BeanMapper.copyList(conditionList, CompareDTO.class).stream().collect(Collectors.toSet());
            //旧规则条件
            List<CfgRuleConditionDTO.ConditionElementDTO> oldConditionList = cfgRuleConditionService.listByRuleType(RuleTypeEnum.CFG_THIRD_NOTICE.getCode());
            Map<String, List<CfgRuleConditionDTO.ConditionElementDTO>> oldGroup = oldConditionList.stream().collect(Collectors.groupingBy(CfgRuleConditionDTO.ConditionElementDTO::getRuleId));

            //单据类型
            List<DictBasicDTO.ViewDTO> thirdNoticeBusinessType = dictBasicService.listByType("thirdNoticeBusinessType");
            Map<String, String> businessTypeMap = thirdNoticeBusinessType.stream().collect(Collectors.toMap(DictBasicDTO.ViewDTO::getValue, DictBasicDTO.ViewDTO::getName,(o1,o2) -> o1));

            for (CfgThirdNoticeEntity oldEntity : oldList) {
                List<CfgRuleConditionDTO.ConditionElementDTO> conditionElementDTOS = oldGroup.get(oldEntity);
                if(CollUtil.isNotEmpty(conditionElementDTOS)){
                    List<CompareDTO> oldCompareList = BeanMapper.copyList(conditionElementDTOS, CompareDTO.class);
                    Set<CompareDTO> oldCompareSet = oldCompareList.stream().collect(Collectors.toSet());
                    if(newCompareSet.contains(oldCompareSet)){
                        String msg = StrUtil.format("错误提示：【{}-{}-{}】已存在，不可重复创建",businessTypeMap.getOrDefault(oldEntity.getBusinessType(),"") , CfgThirdNoticeMethodEnum.getName(oldEntity.getMethod()) , oldEntity.getNoticeType());
                        throw new ServiceException(msg);
                    }
                }
            }
        }
    }

    private static void checkNoticeUserNotEmpty(List<String> roleTypeList, List<String> specificPersonList, List<String> postIdList) {
        if(CollUtil.isEmpty(roleTypeList) && CollUtil.isEmpty(specificPersonList) && CollUtil.isEmpty(postIdList)){
            throw new ServiceException("通知人员不能全部为空");
        }
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

        if(conditionList.size() == 1 ){ //表示只有一条，需要判断是需要删除
            conditionDTO = conditionList.stream().filter(e ->StringUtils.isNotBlank(e.getId()) && StringUtils.isBlank(e.getField()) && StringUtils.isBlank(e.getCompare())).findFirst().orElse(null);
            if(Objects.nonNull(conditionDTO)){
                conditionList.remove(conditionDTO);
            }
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
        //校验通知人员不能全部为空
        List<String> roleTypeList = addOrUpdateDTO.getRoleTypeList();
        List<String> specificPersonList = addOrUpdateDTO.getSpecificPersonList();
        List<String> postIdList = addOrUpdateDTO.getPostIdList();
        checkNoticeUserNotEmpty(roleTypeList, specificPersonList, postIdList);

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

        //校验重复
        String businessType = addOrUpdateDTO.getBusinessType();
        //相同单据类型 + 通知方式 通知配置
        checkDuplication(addOrUpdateDTO.getId(),businessType, method, BeanMapper.copyList(conditionList,CfgRuleConditionDTO.Add.class));

        if(CollUtil.isNotEmpty(addOrUpdateDTO.getPostIdList())){
            String post = String.join(",", addOrUpdateDTO.getPostIdList());
            cfgThirdNoticeEntity.setPost(post);
        }else {
            cfgThirdNoticeEntity.setPost("");
        }
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

        //单据均为即时推送
        if(Objects.equals(cfgThirdNoticeEntity.getMethod(),CfgThirdNoticeMethodEnum.SINGLE.getCode())){
            cfgThirdNoticeEntity.setCron("");
        }else {
            // 校验cron表达式
            boolean isValid = CronExpression.isValidExpression(addOrUpdateDTO.getCron());
            if(Boolean.FALSE.equals(isValid)){
                throw new ServiceException("cron表达式不合法");
            }
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
                cfgApproveSyncFieldMapService.removeByIds(removeList.stream().map(CfgApproveSyncFieldMapEntity::getId).collect(Collectors.toList()));
            }

            List<CfgApproveSyncFieldMapEntity> newList = list.stream().filter(e -> StringUtils.isBlank(e.getId())).collect(Collectors.toList());
            if (CollUtil.isNotEmpty(newList)) {
                // 操作日志
                newList.stream().forEach(e -> {
                    String newMsg = StrUtil.format("用户【{}】新增推送消息【{}】", UserContext.getDefaultLoginUser().getUserName(), e.getFieldName());
                    operateLogService.addModuleOperateLog(newMsg, ModuleTypeEnum.CFG_APPROVE_SYNC.getCode(), cfgThirdNoticeEntity.getId(), "编辑信息");
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
                        operateLogService.addModuleOperateLogByObj(cfgApproveSyncFieldMapEntity, e, ModuleTypeEnum.CFG_APPROVE_SYNC.getCode(), cfgThirdNoticeEntity.getId(), updateMsg);
                    }
                });
                cfgApproveSyncFieldMapService.updateBatchById(updateList);
            }
        }

        //规则条件
        cfgRuleConditionService.updateRuleCondition(addOrUpdateDTO.getId(), conditionList, ModuleTypeEnum.CFG_THIRD_NOTICE.getCode(), RuleTypeEnum.CFG_THIRD_NOTICE.getCode());

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
        //单据类型
        List<DictBasicDTO.ViewDTO> thirdNoticeBusinessType = dictBasicService.listByType("thirdNoticeBusinessType");
        Map<String, String> businessTypeMap = thirdNoticeBusinessType.stream().collect(Collectors.toMap(DictBasicDTO.ViewDTO::getValue, DictBasicDTO.ViewDTO::getName,(o1,o2) -> o1));
        //飞书
        for (CfgThirdNoticeDTO.ListDTO record : records) {
            record.setBusinessTypeName(businessTypeMap.getOrDefault(record.getBusinessType(),""));

            String method = record.getMethod();
            record.setMethodName(CfgThirdNoticeMethodEnum.getName(method));

            if (StringUtils.isNotBlank(record.getCron())) {
                record.setCronType(record.getCron());
            } else {
                record.setCronType("即时通知");
            }

            record.setNoticeStatusName(record.getNoticeStatus().equals(Boolean.FALSE) ? "停用" : "启用");
            if(StringUtils.isNotBlank(record.getPost())){
                //岗位id
                List<String> postIdList = Arrays.asList(record.getPost().split(","));
                List<SysPostEntity> userEntityList = sysPostFeign.listById(postIdList);
                Map<String, String> postMap = userEntityList.stream().collect(Collectors.toMap(SysPostEntity::getId, SysPostEntity::getPostName));
                if(CollUtil.isNotEmpty(postIdList)){
                    record.setPostIdList(postIdList);

                    List<String> postNameList = postIdList.stream().map(e -> postMap.get(e)).collect(Collectors.toList());
                    record.setPostNameList(postNameList);

                    record.setPost(postNameList.stream().collect(Collectors.joining(",")));
                }
            }

            if (StringUtils.isNotBlank(record.getRoleType())) {

                List<DictNoticeRoleOptionEntity> list = dictNoticeRoleOptionService.list();
                Map<String, String> dictNoticeRoleOptionMap = list.stream().collect(Collectors.toMap(DictNoticeRoleOptionEntity::getField, DictNoticeRoleOptionEntity::getFieldName , (o1,o2)-> o1));
                List<String> roleTypes = Arrays.asList(record.getRoleType().split(","));
                if (CollUtil.isNotEmpty(roleTypes)) {
                    record.setRoleTypeList(roleTypes);

                    List<String> roleTypeNameList = roleTypes.stream().map(e -> dictNoticeRoleOptionMap.getOrDefault(e, "")).filter(StringUtil::isNotBlank).collect(Collectors.toList());
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
        List<DictBasicDTO.ViewDTO> thirdNoticeBusinessType = dictBasicService.listByType("thirdNoticeBusinessType");
        thirdNoticeBusinessType.stream().filter(e -> e.getValue().equals(entity.getBusinessType())).findFirst().ifPresent(e -> data.setBusinessTypeName(e.getName()));

        String method = data.getMethod();
        data.setMethodName(CfgThirdNoticeMethodEnum.getName(method));
        if(StringUtils.isNotBlank(data.getPost())){
            //岗位id
            List<String> postIdList = Arrays.asList(data.getPost().split(","));
            List<SysPostEntity> userEntityList = sysPostFeign.listById(postIdList);
            Map<String, String> postMap = userEntityList.stream().collect(Collectors.toMap(SysPostEntity::getId, SysPostEntity::getPostName));
            if(CollUtil.isNotEmpty(postIdList)){
                data.setPostIdList(postIdList);
                data.setPostNameList(postIdList.stream().map(e -> postMap.get(e)).collect(Collectors.toList()));
            }
        }

        if(StringUtils.isNotBlank(data.getRoleType())){
            List<DictNoticeRoleOptionEntity> list = dictNoticeRoleOptionService.list();
            Map<String, String> dictNoticeRoleOptionMap = list.stream().collect(Collectors.toMap(DictNoticeRoleOptionEntity::getField, DictNoticeRoleOptionEntity::getFieldName , (o1,o2)-> o1));
            List<String> roleTypes = Arrays.asList(data.getRoleType().split(","));
            if(CollUtil.isNotEmpty(roleTypes)){
                data.setRoleTypeList(roleTypes);
                List<String> roleTypeNameList = roleTypes.stream().map(e -> dictNoticeRoleOptionMap.getOrDefault(e, "")).filter(StringUtil::isNotBlank).collect(Collectors.toList());
                data.setRoleTypeNameList(roleTypeNameList);
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
            // 按 sort 升序排序
            pushMsgList.sort(Comparator.comparingInt(CfgApproveSyncFieldMapDTO.NoticeFieldMapDTO::getSort));
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

    @Override
    public void testPush(String jsonStr) {
        mqProducerService.syncClassMsgWithDelayLevel(RocketMqTopic.RECEIVE_DDL_TO_MQ_SYS_TOPIC, RocketMqTagEnum.SYS_RECEIVE_DDL_TO_MQ_TAG.getName(),jsonStr , IdUtil.simpleUUID(),1);
    }

    @Override
    public List<CfgThirdNoticeEntity> listByMethod(String method){
        return lambdaQuery()
                .eq(CfgThirdNoticeEntity::getMethod,method)
                .eq(CfgThirdNoticeEntity::getNoticeStatus, Boolean.TRUE)
                .list();

    }
}
