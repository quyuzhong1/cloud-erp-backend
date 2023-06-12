package ${package.ServiceImpl};
<#assign fieldMap={}/>
<#list table.fields as field>
 <#assign fieldMap += {field.propertyName:field.propertyName} />
</#list>
<#assign docName = "${table.comment!}">
<#if docName?ends_with("表")>
    <#assign docName = docName[0..<docName?length-1] + "单">
</#if>

import ${package.Entity}.${entity};
import ${package.Mapper}.${table.mapperName};
import ${package.Service}.${table.serviceName};
import ${superServiceImplClassPackage};
<#if fieldMap["approveStatus"]?? && fieldMap["code"]??>
import ${package.Service}.OperateLogService;
<#if fieldMap["approveUserId"]?? && fieldMap["approveUserName"]??>
import ${package.Service}.CommonService;
</#if>
import com.common.core.exception.ServiceException;
import com.erp.rpc.sys.feign.SysUserFeign;
</#if>
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;
<#if fieldMap["approveStatus"]?? && fieldMap["code"]??>
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import cn.hutool.core.collection.CollUtil;
import com.google.common.collect.Sets;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.math3.util.Pair;
import com.google.common.collect.Lists;
import io.seata.spring.annotation.GlobalTransactional;

import com.common.business.enums.ApproveStatusEnum;
<#if fieldMap["invalidStatus"]??>
import com.erp.model.scm.enums.InvalidStatusEnum;
</#if>
import com.common.business.enums.ApproveTypeEnum;
<#if fieldMap["approveUserId"]?? && fieldMap["approveUserName"]??>
import com.common.business.vo.LoginUser;
</#if>
import com.common.business.vo.PagingVO;
import com.common.business.dto.base.*;
import ${package.Dto}.${table.dtoName};
import com.common.core.enums.ApiError;
import com.common.core.utils.*;
import com.erp.model.sys.dto.SysCodeDTO;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.utils.date.DateUtil;

import javax.servlet.http.HttpServletResponse;
<#if fieldMap["approveTime"]??>
import java.time.LocalDateTime;
</#if>
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
</#if>
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

    <#if fieldMap["approveStatus"]?? && fieldMap["code"]??>
    @Autowired
    private SysUserFeign sysUserFeign;
    @Autowired
    private OperateLogService operateLogService;
    <#if fieldMap["approveUserId"]?? && fieldMap["approveUserName"]??>
    @Autowired
    private CommonService commonService;
    </#if>
    </#if>

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
        // 同一主单多行明细只有第一行显示主单字段，其他行赋空
        hideData(pageData.getRecords());
        return new PagingVO(pageData);
    }

    @Override
    public List<${table.dtoName}.TabListDTO> tabList(PermissionsDTO param) {
        ${table.dtoName}.PagingParamDTO searchParam = new ${table.dtoName}.PagingParamDTO();
        searchParam.setPermissionSql(param.getPermissionSql());

        return null;
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

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public String add(${table.dtoName}.AddDTO addDTO) {
        ${entity} ${entity?uncap_first} = new ${entity}();
        BeanMapperUtils.copy(addDTO, ${entity?uncap_first});

        // 数据处理
        handleData(${entity?uncap_first});

        log.info("开始新增${docName}");
        // 生成单号
        // TODO 此处的null需填写生成单号的分类和类型，category查看BusinessNoConstant，type查看BusinessNoTypeEnum枚举类
        String code = sysUserFeign.getBusinessNo(new SysCodeDTO(null, null));
        ${entity?uncap_first}.setCode(code);
        boolean save = super.save(${entity?uncap_first});
        if(!save) {
           throw new ServiceException("${docName}保存失败");
        }

        // 操作日志
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(String.format("新增了一个${docName}【%s】", code), null, ${entity?uncap_first}.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）
        return ${entity?uncap_first}.getId();
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void update(${table.dtoName}.UpdateDTO updateDTO) {
        ${entity} old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException("未找到${docName}"));
        // 待提交和审核不通过允许修改
        if (!ApproveStatusEnum.WAIT_SUBMIT.getStatus().equals(old.getApproveStatus()) && !ApproveStatusEnum.REJECT.getStatus().equals(old.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_1029);
        }

        ${entity} ${entity?uncap_first} =  BeanMapperUtils.map(${entity}.class, updateDTO);

        // 数据处理
        handleData(${entity?uncap_first});

        log.info("编辑 开始修改${docName}数据，单号：【{}】", old.getCode());
        boolean save = super.updateById(${entity?uncap_first});
        if(!save) {
           throw new ServiceException("${docName}保存失败");
        }

        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
        log.info("编辑 开始记录${docName!}日志数据，单号：【{}】", ${entity?uncap_first}.getCode());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, ${entity?uncap_first}, null, ${entity?uncap_first}.getId(), "", "");
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void submit(List<String> ids) {
       if (CollUtil.isEmpty(ids)) {
          throw new ServiceException(ApiError.ERROR_98004);
       }
       List<${entity}> list = super.listByIds(ids);
       if (CollUtil.isEmpty(list)) {
          throw new ServiceException("未找到${docName!}数据");
       }
       // 待提交或审核不通过并且未作废允许提交
       long count = list.stream().filter(obj -> (!ApproveStatusEnum.WAIT_SUBMIT.getStatus().equals(obj.getApproveStatus()) && !ApproveStatusEnum.REJECT.getStatus().equals(obj.getApproveStatus())) || !InvalidStatusEnum.NOT_VOIDED.getStatus().equals(obj.getInvalidStatus())).count();
       if (count > 0) {
          throw new ServiceException(ApiError.ERROR_98010);
       }

       // 更新单据审核状态
       log.info("提交 开始修改${docName!}状态数据，id集合：【{}】", JSONObject.toJSONString(ids));
       this.updateApproveStatus(ids, ApproveStatusEnum.APPROVE_ING.getStatus());

       // TODO 启动流程（如果需要的话）

       // 记录操作日志
       log.info("提交 开始记录${docName}日志数据，id集合：【{}】", JSONObject.toJSONString(ids));
       List<Pair<String, String>> pairList = list.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
       // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
       operateLogService.batchAddModuleOperateLog("提交了一个${docName}【%s】", null, pairList, "提交操作");
    }

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void addAndSubmit(${table.dtoName}.AddDTO dto) {
        // 新增
        String id = this.add(dto);
        // 提交
        this.submit(Arrays.asList(id));
    }

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateAndSubmit(${table.dtoName}.UpdateDTO dto) {
        // 修改
        this.update(dto);
        // 提交
        this.submit(Arrays.asList(dto.getId()));
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void approve(BaseApproveParamDTO dto) {
        List<String> ids = dto.getIds();
        ApproveTypeEnum approveType = ApproveTypeEnum.getByCode(dto.getType());
        if(Objects.equals(approveType, ApproveTypeEnum.REJECT) && StrUtils.isEmpty(dto.getComment())) {
           throw new ServiceException("审核不通过请填写审核意见");
        }
        List<${entity}> list = super.listByIds(ids);
        if (CollUtil.isEmpty(list)) {
            throw new ServiceException("未找到${docName}数据");
        }
        // 审核中的数据允许审核
        long count = list.stream().filter(obj -> !ApproveStatusEnum.APPROVE_ING.getStatus().equals(obj.getApproveStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98006);
        }
        // 新审核状态
        ApproveStatusEnum approveStatus = Objects.equals(ApproveTypeEnum.PASS, approveType) ? ApproveStatusEnum.APPROVE : ApproveStatusEnum.REJECT;
        if(Objects.equals(ApproveTypeEnum.PASS, approveType)) {
           // TODO 审核通过流程处理
        } else if (Objects.equals(ApproveTypeEnum.REJECT, approveType)) {
           // TODO 终止审批流程
        }

        // 更新审核信息
        updateForApprove(ids, approveStatus.getStatus());

        // 操作日志
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        List<Pair<String, String>> pairList = list.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog(String.format("审核【%s】了一个${docName}", approveType.getName()).concat("【%s】").concat(StrUtils.isNotEmpty(dto.getComment()) ? String.format("，意见：%s", dto.getComment()) : ""),
                            null, pairList, "审核操作");
    }

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void disApprove(List<String> ids) {
        List<${entity}> list = super.listByIds(ids);
        if (CollUtil.isEmpty(list)) {
            throw new ServiceException("未找到${docName}数据");
        }
        // 已审核支持反审核
        long count = list.stream().filter(obj -> !Objects.equals(ApproveStatusEnum.APPROVE.getStatus(), obj.getApproveStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98014);
        }
        // TODO 检查是否有下推单据（如果支持下推的话）

        // 更新审核信息
        updateForDisApprove(ids, ApproveStatusEnum.WAIT_SUBMIT.getStatus());

        // 操作日志
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        List<Pair<String, String>> pairList = list.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog("反审核了一个${docName}【%s】", null, pairList, "反审核操作");
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void delete(List<String> ids) {
       List<${entity}> list = super.listByIds(ids);
       if (CollUtil.isEmpty(list)) {
         throw new ServiceException("未找到${docName}数据");
       }
       // 只有待提交且未作废的数据允许删除
       long count = list.stream().filter(obj -> !ApproveStatusEnum.WAIT_SUBMIT.getStatus().equals(obj.getApproveStatus())<#if fieldMap["invalidStatus"]??> || obj.getInvalidStatus()</#if> ).count();
       if (count > 0) {
         throw new ServiceException(ApiError.ERROR_98009);
       }
       // 删除日志数据
       log.info("删除 开始删除${docName}日志数据，id集合：【{}】", JSONObject.toJSONString(ids));
       operateLogService.removeByBusinessIds(ids);

       // TODO 删除明细数据（如果有明细数据的话）

       // 删除主单数据
       log.info("删除 开始删除${docName}主单数据，id集合：【{}】", JSONObject.toJSONString(ids));
       super.removeByIds(ids);
    }
    <#if fieldMap["invalidStatus"]?? && fieldMap["invalidRemark"]??>
    /**
    * 作废
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void invalid(List<String> ids, String remark) {
        List<${entity}> list = super.listByIds(ids);
        if (CollUtil.isEmpty(list)) {
        throw new ServiceException("未找到${docName}数据");
        }
        // 待提交或审核不通过并且未作废允许作废
        long count = list.stream().filter(obj -> (!ApproveStatusEnum.WAIT_SUBMIT.getStatus().equals(obj.getApproveStatus()) && !ApproveStatusEnum.REJECT.getStatus().equals(obj.getApproveStatus())) || !InvalidStatusEnum.NOT_VOIDED.getStatus().equals(obj.getInvalidStatus())).count();
        if (count > 0) {
           throw new ServiceException(ApiError.ERROR_98005);
        }
        log.info("作废 开始修改${docName}状态数据，id集合：【{}】", JSONObject.toJSONString(ids));
        lambdaUpdate().in(${entity}::getId, ids)
            .set(${entity}::getInvalidStatus, InvalidStatusEnum.VOIDED.getStatus())
            .set(${entity}::getInvalidRemark, remark)
            .update();

        log.info("作废 开始记录操作日志，id集合：【{}】", JSONObject.toJSONString(ids));
        List<Pair<String, String>> pairList = list.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.batchAddModuleOperateLog("作废了一个${docName}【%s】，作废原因：".concat(remark), null, pairList, "作废操作");
     }
     </#if>

    /**
    * 撤销
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void cancelProcess(List<String> ids) {
        List<${entity}> list = super.listByIds(ids);
        // 只有待提交的数据允许撤销
        long count = list.stream().filter(obj -> !ApproveStatusEnum.APPROVE_ING.getStatus().equals(obj.getApproveStatus())).count();
        if (count > 0) {
           throw new ServiceException(ApiError.ERROR_98007);
        }
        // TODO 撤销流程
        log.info("撤销 开始撤销流程，id集合：【{}】",JSONObject.toJSONString(ids));

        log.info("撤销 开始修改${docName}状态，id集合：【{}】", JSONObject.toJSONString(ids));
        updateApproveStatus(ids, ApproveStatusEnum.WAIT_SUBMIT.getStatus());

        //操作日志
        log.info("撤销 开始记录操作日志，id集合：【{}】", JSONObject.toJSONString(ids));
        List<Pair<String, String>> pairList = list.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.batchAddModuleOperateLog("${docName}【%s】取消流程", null, pairList, "取消流程操作");
    }

    @Override
    public ${table.dtoName}.ViewDTO view(String id) {
        ${entity} ${entity?uncap_first} = super.getByIdOpt(id).orElseThrow(()->new ServiceException("未找到${docName}数据"));
        ${table.dtoName}.ViewDTO data = BeanMapperUtils.map(${table.dtoName}.ViewDTO.class, ${entity?uncap_first});
        // TODO 查询明细数据（如果有的话）
        return data;
    }

    /**
    * 审核更新审核信息
    * @param ids
    * @param approveStatus
    */
    public void updateForApprove(List<String> ids, String approveStatus) {
        <#if fieldMap["approveUserId"]?? && fieldMap["approveUserName"]??>
        //当前登录人
        LoginUser userInfo = commonService.getUserInfo();
        </#if>
        this.lambdaUpdate().in(${entity}::getId, ids)
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
            .update();
     }

    /**
    * 反审核更新审核信息
    * @param ids
    * @param approveStatus
    */
    @Transactional(rollbackFor = Exception.class)
    public void updateForDisApprove(List<String> ids, String approveStatus) {
        this.lambdaUpdate().in(${entity}::getId, ids)
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
            .update();
        }

    /**
    * 更新审核状态
    */
    @Transactional(rollbackFor = Exception.class)
    public void updateApproveStatus(List<String> ids, String approveStatus) {
        lambdaUpdate().in(${entity}::getId, ids)
        .set(${entity}::getApproveStatus, approveStatus)
        .update();
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
    * 分页查询同一主单多行明细只有第一行显示主单字段，其他行赋空
    */
    private void hideData(List<${table.dtoName}.ListDTO> list) {
        Set<String> mainIds = Sets.newHashSet();
        // 同一个主单的其他行明细，只显示第一行的主单字段
        for(${table.dtoName}.ListDTO data : list) {
            if (mainIds.contains(data.getId())) {
                data.setCode(null);
                data.setApproveStatus(null);
                data.setApproveStatusName(null);
                <#if fieldMap["invalidStatus"]??>
                data.setInvalidStatus(null);
                data.setInvalidStatusName(null);
                </#if>
                <#if fieldMap["approveUserName"]??>
                data.setApproveUserName(null);
                </#if>
                data.setCreateUserName(null);
                data.setCreateTime(null);
                // TODO 其他需要赋空值字段
                continue;
            }
            mainIds.add(data.getId());
        }
    }

    /**
    * 新增修改处理数据
    */
    private void handleData(${entity} ${entity?uncap_first}) {
        // TODO 验证数据 & 数据赋值
    }
    </#if>

}
</#if>
