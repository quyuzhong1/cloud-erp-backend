package ${package.ServiceImpl};
<#assign fieldMap={}/>
<#list table.fields as field>
 <#assign fieldMap += {field.propertyName:field.propertyName} />
</#list>
<#assign docName = "${table.comment!}">
<#if docName?ends_with("表") && !docName?ends_with("单表") >
    <#assign docName = docName[0..<docName?length-1] + "单">
</#if>

<#if fieldMap["approveStatus"]?? && fieldMap["code"]??>
import cn.hutool.core.bean.BeanUtil;
import com.common.business.enums.OperationTypeEnum;
import com.common.business.vo.LoginUser;
</#if>

import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import ${package.Entity}.${entity};
import ${package.Mapper}.${table.mapperName};
import ${package.Service}.${table.serviceName};
import ${superServiceImplClassPackage};
import com.common.business.threadlocal.UserContext;
import ${package.Service}.OperateLogService;
import ${package.Service}.CommonService;
import com.common.core.exception.ServiceException;
<#if fieldMap["code"]??>
import com.common.business.config.DocNoGenHelper;
import com.common.core.controller.vo.ApiResult;
import cn.hutool.core.util.ObjectUtil;
</#if>
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import ${package.Dto}.${table.dtoName};
<#if fieldMap["approveStatus"]?? && fieldMap["code"]??>
import com.erp.model.workflow.dto.ProcessManagementDTO;
import com.erp.rpc.workflow.WorkflowFeign;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import cn.hutool.core.collection.CollUtil;
import com.google.common.collect.Sets;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;

import com.common.business.enums.ApproveStatusEnum;
<#if fieldMap["invalidStatus"]??>
import com.erp.model.scm.enums.InvalidStatusEnum;
</#if>
import com.common.business.enums.ApproveTypeEnum;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.business.dto.base.*;
import com.erp.model.sys.dto.SysCodeDTO;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.utils.date.DateUtil;

import javax.servlet.http.HttpServletResponse;
<#if fieldMap["approveTime"]??>
import java.time.LocalDateTime;
</#if>
import javax.annotation.Resource;
import java.util.stream.Collectors;
</#if>
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * ${table.comment!} 服务实现类
 * </p>
 *
 * @author ${author}
 * @since ${date}
 */
@Slf4j
@Service
<#if kotlin>
open class ${table.serviceImplName} : ${superServiceImplClass}<${table.mapperName}, ${entity}>(), ${table.serviceName} {

}
<#else>
public class ${table.serviceImplName} extends ${superServiceImplClass}<${table.mapperName}, ${entity}> implements ${table.serviceName} {
    @Autowired
    private OperateLogService operateLogService;
    <#if fieldMap["code"]??>
    @Autowired
    private DocNoGenHelper docNoGenHelper;
    </#if>
    <#if fieldMap["approveStatus"]?? && fieldMap["code"]??>
    @Autowired
    private WorkflowFeign workflowFeign;
    </#if>

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(${table.dtoName}.AddDTO addDTO) {
        ${entity} ${entity?uncap_first} = new ${entity}();
        BeanMapperUtils.copy(addDTO, ${entity?uncap_first});

        // 数据处理
        handleData(${entity?uncap_first});

        log.info("开始新增${docName}");
        <#if fieldMap["code"]??>
        // 生成单号
        // TODO 此处的null需填写生成单号类型，type查看BusinessNoTypeEnum枚举类 注意需要填写prefix 为单号前缀
        String code = docNoGenHelper.generateCode(null);
        ${entity?uncap_first}.setCode(code);
        </#if>
        boolean save = super.save(${entity?uncap_first});
        if(!save) {
            throw new ServiceException("${docName}保存失败");
        }

        // 操作日志
        <#if fieldMap["code"]??>
        String msg = StrUtil.format("用户【{}】新增【{}】单据单号为【{}】", UserContext.getDefaultLoginUser().getUserName(), "${docName}" , ${entity?uncap_first}.getCode());
        <#else >
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "${docName}" , ${entity?uncap_first}.getId());
        </#if>
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, ${entity?uncap_first}.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        <#if fieldMap["code"]??>
        return new BaseResultDTO.AddDTO(${entity?uncap_first}.getId(), code);
        <#else >
        return new BaseResultDTO.AddDTO(${entity?uncap_first}.getId(), ${entity?uncap_first}.getId());
        </#if>
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(${table.dtoName}.UpdateDTO updateDTO) {
        ${entity} old = super.getById(updateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "${docName!}"));
        <#if fieldMap["approveStatus"]??>
        // 待提交和审核不通过允许修改
        if (!ApproveStatusEnum.allowUpdateStatus(old.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_1029);
        }
        </#if>
        ${entity} ${entity?uncap_first} =  BeanMapperUtils.map(${entity}.class, updateDTO);

        // 数据处理
        handleData(${entity?uncap_first});
        <#if fieldMap["code"]??>
        log.info("编辑 开始修改${docName}数据，单号：【{}】", old.getCode());
        <#else >
        log.info("编辑 开始修改${docName}数据，id：【{}】", old.getId());
        </#if>
        boolean save = super.updateById(${entity?uncap_first});
        if(!save) {
            throw new ServiceException("${docName}保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
        <#if fieldMap["code"]??>
            log.info("编辑 开始记录${docName!}日志数据，单号：【{}】", ${entity?uncap_first}.getCode());
            String msg = StrUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), ${entity?uncap_first}.getCode(), "${docName!}");
        <#else >
            log.info("编辑 开始记录${docName!}日志数据，id：【{}】", ${entity?uncap_first}.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), ${entity?uncap_first}.getId(), "${docName!}");
        </#if>
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, ${entity?uncap_first}, null, ${entity?uncap_first}.getId(), msg);
        return Boolean.TRUE;
    }


    <#if fieldMap["approveStatus"]?? && fieldMap["code"]??>
    @Override
    public PagingVO<${table.dtoName}.ListDTO> paging(PagingDTO<${table.dtoName}.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<${table.dtoName}.ListDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
           return new PagingVO(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO(pageData);
    }

    @Override
    public List<${table.dtoName}.TabListDTO> tabList(PermissionsDTO param) {
        ${table.dtoName}.PagingParamDTO searchParam = new ${table.dtoName}.PagingParamDTO();
        searchParam.setPermissionSql(param.getPermissionSql());
        List<${table.dtoName}.TabListDTO> list = baseMapper.tabList(searchParam);
        // 获取状态列表
        List<String> statusList = ApproveStatusEnum.getStatusList();
        // 不存在的状态赋值为0
        List<String> existStatusList = list.stream().map(${table.dtoName}.TabListDTO::getTabFlag).collect(Collectors.toList());
        statusList.parallelStream().forEach(status -> {
            if(!existStatusList.contains(status)) {
            list.add(new ${table.dtoName}.TabListDTO(status, 0));
        }
        });
        list.add(new ${table.dtoName}.TabListDTO("all", list.stream().mapToInt(${table.dtoName}.TabListDTO::getCount).sum()));
        // 计算合计数量
        return list;
    }

    @Override
    public void exportList(${table.dtoName}.ExportDTO param, HttpServletResponse response) {
        List<${table.dtoName}.ListDTO> list = this.baseMapper.listExport(param);
        if(CollUtil.isEmpty(list)) {
           return;
        }
        // 数据处理
        fillList(list);

        // 导出数据
        StringBuffer sb = new StringBuffer();
        String excelPath = "excel/${entity?replace('Entity', '')?uncap_first}.xlsx";
        String name = "${docName}导出";
        String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
        sb.append(date).append(name);
        try {
            new ExcelPrintUtils().patchExport(list, response, sb.toString(), excelPath);
        } catch (Exception e) {
            throw new ServiceException(ApiError.ERROR_1015);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO submit(String id) {
        ${entity} entity = getById(id);
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException("未找到${docName!}数据");
        }
        validateSubmit(entity);
        // 更新单据审核状态
        log.info("提交 开始修改${docName!}状态数据，id：【{}】", id);
        this.updateApproveStatus(id, ApproveStatusEnum.APPROVE_ING.getStatus());

        // TODO 启动流程（如果需要的话）
        log.info("提交 开始启动${docName!}流程，id=：【{}】", entity.getId());
        startProcess(entity);
        // 记录操作日志
        log.info("提交 开始记录${docName}日志数据，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据提交审核 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "${docName}");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, entity.getId(), "提交操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.SUBMIT);
    }

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO addAndSubmit(${table.dtoName}.AddDTO dto) {
        // 新增
        BaseResultDTO.AddDTO result = this.add(dto);
        // 提交
        this.submit(result.getId());
        return result;
    }

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateAndSubmit(${table.dtoName}.UpdateDTO dto) {
        // 修改
        this.update(dto);
        // 提交
        this.submit(dto.getId());
    }

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO approve(ApproveOneDTO dto) {
        ApproveTypeEnum approveType = ApproveTypeEnum.getByCode(dto.getType());
        if(Objects.equals(approveType, ApproveTypeEnum.REJECT) && StrUtils.isEmpty(dto.getComment())) {
            throw new ServiceException(ApiError.REJECT_COMMENT_NOT_EMPTY);
        }
        ${entity} entity = getById(dto.getId());
        // 审核中的数据允许审核
        if(!Objects.equals(entity.getApproveStatus(), ApproveStatusEnum.APPROVE_ING)) {
            throw new ServiceException(ApiError.ERROR_98006);
        }
        // 调用流程审核
        approveProcess(entity, dto);
        // 操作日志
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据审核操作  审核结果：【{}】 审核意见 ：【{}】", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "${docName}", approveType.getName(), dto.getComment());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, entity.getId(), "审核操作");
        ApproveStatusEnum approveStatus = ApproveStatusEnum.transferApproveType(approveType);
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.approveStatus(approveStatus));
    }

    /**
    * 审核流程处理
    * @param entity
    * @param dto
    */
    private void approveProcess(${entity} entity, ApproveOneDTO dto) {
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        ProcessManagementDTO.ApproveDTO approveDTO = new ProcessManagementDTO.ApproveDTO();
        approveDTO.setBusinessId(entity.getId());
        // TODO 此处的null需修改为流程模块类型，BusinessKey查看SourceTypeEnum枚举类
        approveDTO.setBusinessKey(null);
        approveDTO.setApproveType(ApproveTypeEnum.getByCode(dto.getType()));
        approveDTO.setComment(dto.getComment());
        approveDTO.setUserId(userInfo.getUid());
        approveDTO.setVariablesMap(BeanUtil.beanToMap(entity));
        ApiResult<ProcessManagementDTO.ApproveResultDTO> approveResult = workflowFeign.approve(approveDTO);
        Integer code = approveResult.getCode();
        if (200 != code) {
            throw new ServiceException(ApiError.ERROR_94006);
        }
        ProcessManagementDTO.ApproveResultDTO data = approveResult.getData();
        if (ObjectUtil.isEmpty(data.getIsExistProcess()) || !data.getIsExistProcess()) {
            // 无需走流程的数据则直接更新状态
            approveEnd(dto, entity);
        }
    }

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO disApprove(String id) {
        ${entity} entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到${docName}单数据"));
        // 反审核条件判断
        validateDisApprove(entity);
        // TODO 检查是否有下推单据（如果支持下推的话）明细数据

        // 更新审核信息
        updateForDisApprove(id, ApproveStatusEnum.WAIT_SUBMIT.getStatus());

        // 操作日志
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据反审核操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "${docName}");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, entity.getId(), "反审核操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.DISAPPROVE);
    }

    private Boolean validateDisApprove(${entity} entity) {
        // 已审核支持反审核
        if (!Objects.equals(entity.getApproveStatus(), ApproveStatusEnum.APPROVE.getStatus())) {
            throw new ServiceException(ApiError.ERROR_98014);
        }
        // TODO 下游盘点计划单反审核
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO delete(String id) {
        ${entity} entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到${docName}数据"));
        // 只有待提交数据允许删除
        if (!Objects.equals(ApproveStatusEnum.WAIT_SUBMIT, entity.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_98032);
        }
        // TODO 删除明细数据（如果有明细数据的话）

        // 删除主单数据
        log.info("删除 开始删除${docName}主单数据，id：【{}】", id);
        super.removeById(id);
        // 删除日志数据
        log.info("删除 开始删除${docName}日志数据，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据删除操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "${docName}");
        operateLogService.addModuleOperateLog(msg, null, entity.getCode(), "删除${docName}数据");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.DELETE);
    }
    <#if fieldMap["invalidStatus"]?? && fieldMap["invalidRemark"]??>
    /**
    * 作废
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO invalid(String id, String remark) {
        ${entity} entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到${docName}数据"));
        // 待提交或审核不通过并且未作废允许作废
        if ((!ApproveStatusEnum.WAIT_SUBMIT.getStatus().equals(entity.getApproveStatus()) && !ApproveStatusEnum.REJECT.getStatus().equals(entity.getApproveStatus())) || !InvalidStatusEnum.NOT_VOIDED.getStatus().equals(entity.getInvalidStatus())) {
           throw new ServiceException(ApiError.ERROR_98005);
        }
        log.info("作废 开始修改${docName}状态数据，id：【{}】", id);
        lambdaUpdate().eq(${entity}::getId, id)
            .set(${entity}::getInvalidStatus, InvalidStatusEnum.VOIDED.getStatus())
            .set(${entity}::getInvalidRemark, remark)
            .update();

        log.info("作废 开始记录操作日志，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据作废操作 作废原因：【{}】", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "${docName}", remark);
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, entity.getId(), "作废操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.INVALID);
     }
     </#if>

    /**
    * 撤销
    */
    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO cancelProcess(String id) {
        ${entity} entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到${docName}数据"));
        // 只有审核中的单据允许撤销
        if (!Objects.equals(entity.getApproveStatus(), ApproveStatusEnum.APPROVE_ING.getStatus())) {
            throw new ServiceException(ApiError.ERROR_98007);
        }
        // TODO 撤销流程
        log.info("撤销 开始撤销流程，id：【{}】",id);

        log.info("撤销 开始修改${docName}状态，id：【{}】", id);
        updateApproveStatus(id, ApproveStatusEnum.WAIT_SUBMIT.getStatus());

        //操作日志
        log.info("撤销 开始记录操作日志，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据撤销流程操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "${docName}");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, entity.getId(), "取消流程操作");
        ProcessManagementDTO.RevokeDTO revokeDTO = new ProcessManagementDTO.RevokeDTO();
        revokeDTO.setBusinessId(entity.getId());
        // TODO 此处的null需修改为日志模块类型，BusinessKey查看SourceTypeEnum枚举类
        revokeDTO.setBusinessKey(null);
        revokeDTO.setUserId(UserContext.getDefaultLoginUser().getUid());
        workflowFeign.revokeProcess(revokeDTO);
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.CANCEL_PROCESS);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean approveEnd(ApproveOneDTO dto, ${entity} entity) {
        if (ObjectUtil.isEmpty(entity)) {
            return Boolean.TRUE;
        }
        ApproveStatusEnum approveStatus = ApproveStatusEnum.transferApproveType(dto.getType());
        updateForApprove(entity.getId(), approveStatus.getStatus());
        // todo 明细数据处理 上下游数据处理

        return Boolean.TRUE;
    }

    @Override
    public ${table.dtoName}.ViewDTO view(String id) {
        ${entity} ${entity?uncap_first} = super.getByIdOpt(id).orElseThrow(()->new ServiceException("未找到${docName}数据"));
        ${table.dtoName}.ViewDTO data = BeanMapperUtils.map(${table.dtoName}.ViewDTO.class, ${entity?uncap_first});
        // 数据填充处理
        fillOne(data);
        // TODO 查询明细数据（如果有的话）
        return data;
    }
    /**
    * 启动流程
    *
    * @param entity
    * @return void
    * @Date 2023/7/4 10:07
    **/

    public void startProcess(${entity} entity) {
        ProcessManagementDTO.StartDTO startDTO = new ProcessManagementDTO.StartDTO();
        startDTO.setBusinessId(entity.getId());
        startDTO.setBusinessCode(entity.getCode());
        // TODO 此处的null需修改为日志模块类型，BusinessKey查看SourceTypeEnum枚举类
        startDTO.setBusinessKey(null);
        startDTO.setBusinessName(entity.getCode());
        startDTO.setUserId(UserContext.getDefaultLoginUser().getUid());
        startDTO.setVariablesMap(BeanUtil.beanToMap(entity));
        ApiResult<ProcessManagementDTO.StartResultDTO> result = workflowFeign.start(startDTO);
        if (!result.isSuccess()) {
            throw new ServiceException(result.getMsg());
        }
    }
    private void fillOne(${table.dtoName}.ViewDTO data) {
        if (ObjectUtil.isEmpty(data)) {
            return;
        }
    }

    /**
    * 审核更新审核信息
    * @param id
    * @param approveStatus
    */
    public void updateForApprove(String id, String approveStatus) {
        <#if fieldMap["approveUserId"]?? && fieldMap["approveUserName"]??>
        //当前登录人
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        </#if>
        this.lambdaUpdate().eq(${entity}::getId, id)
            <#if fieldMap["approveUserId"]??>
            .set(${entity}::getApproveUserId, userInfo.getUid())
            </#if>
            <#if fieldMap["approveUserName"]??>
            .set(${entity}::getApproveUserName, userInfo.getUserName())
            </#if>
            .set(${entity}::getApproveStatus, approveStatus)
            <#if fieldMap["approveTime"]??>
            .set(${entity}::getApproveTime, LocalDateTime.now())
            </#if>
            .update(new ${entity}());
     }

    /**
    * 反审核更新审核信息
    * @param id
    * @param approveStatus
    */
    @Transactional(rollbackFor = Exception.class)
    public void updateForDisApprove(String id, String approveStatus) {
        this.lambdaUpdate().eq(${entity}::getId, id)
            <#if fieldMap["approveUserId"]??>
            .set(${entity}::getApproveUserId, "")
            </#if>
            <#if fieldMap["approveUserName"]??>
            .set(${entity}::getApproveUserName, "")
            </#if>
            .set(${entity}::getApproveStatus, approveStatus)
            <#if fieldMap["approveTime"]??>
            .set(${entity}::getApproveTime, null)
            </#if>
            .update(new ${entity}());
        }

    /**
    * 更新审核状态
    */
    @Transactional(rollbackFor = Exception.class)
    public void updateApproveStatus(String id, String approveStatus) {
        lambdaUpdate().eq(${entity}::getId, id)
        .set(${entity}::getApproveStatus, approveStatus)
        .update(new ${entity}());
    }

    /**
    * 分页查询、导出 数据处理
    */
    private void fillList(List<${table.dtoName}.ListDTO> list) {
        if(CollUtil.isEmpty(list)) {
           return;
        }

        // 属性赋值
        for(${table.dtoName}.ListDTO data : list) {
            <#if fieldMap["approveStatus"]??>
            data.setApproveStatusName(ApproveStatusEnum.getName(data.getApproveStatus()));
            </#if>
            <#if fieldMap["invalidStatus"]??>
            data.setInvalidStatusName(InvalidStatusEnum.getName(data.getInvalidStatus()));
            </#if>
            // TODO 其他如需要显示名称的字段赋值
        }
    }
    /**
    * 分页查询、导出 数据处理
    */
    private void validateSubmit(${entity} entity) {
        // 待提交或审核不通过并且未作废允许提交
        if(!ApproveStatusEnum.allowUpdateStatus(entity.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_98010);
        }
        return;
    }

    </#if>
    /**
    * 新增修改处理数据
    */
    private void handleData(${entity} ${entity?uncap_first}) {
    // TODO 验证数据 & 数据赋值
    }
}
</#if>
