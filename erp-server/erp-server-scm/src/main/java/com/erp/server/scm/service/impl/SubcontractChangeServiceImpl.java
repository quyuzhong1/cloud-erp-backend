package com.erp.server.scm.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.ApproveStatusQtyDTO;
import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.ApproveTypeEnum;
import com.common.business.service.SuperServiceImpl;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.StrUtils;
import com.common.core.utils.date.DateUtil;
import com.erp.model.scm.dto.SubcontractChangeDTO;
import com.erp.model.scm.entity.SubcontractChangeEntity;
import com.erp.model.scm.enums.InvalidStatusEnum;
import com.erp.model.scm.enums.PageListTypeEnum;
import com.erp.model.sys.dto.SysCodeDTO;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.scm.mapper.SubcontractChangeMapper;
import com.erp.server.scm.service.CommonService;
import com.erp.server.scm.service.ModuleOperateLogService;
import com.erp.server.scm.service.SubcontractChangeService;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.util.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
/**
 * <p>
 * 委外变更单 服务实现类
 * </p>
 *
 * @author will
 * @since 2023-06-08
 */
@Slf4j
@Service
public class SubcontractChangeServiceImpl extends SuperServiceImpl<SubcontractChangeMapper, SubcontractChangeEntity> implements SubcontractChangeService {

    @Autowired
    private SysUserFeign sysUserFeign;
    @Autowired
    private ModuleOperateLogService operateLogService;
    @Autowired
    private CommonService commonService;

    @Override
    public PagingVO<SubcontractChangeDTO.ListDTO> paging(PagingDTO<SubcontractChangeDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<SubcontractChangeDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
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
    public List<SubcontractChangeDTO.TabListDTO> tabList(PermissionsDTO param) {
        SubcontractChangeDTO.PagingParamDTO searchParam = new SubcontractChangeDTO.PagingParamDTO();
        searchParam.setPermissionSql(param.getPermissionSql());
        List<ApproveStatusQtyDTO> statusList = this.baseMapper.listCount(searchParam);

        return null;
    }

    @Override
    public void exportList(SubcontractChangeDTO.ExportDTO param, HttpServletResponse response) {
        List<SubcontractChangeDTO.ListDTO> list = this.baseMapper.listExport(param);
        if(CollUtil.isEmpty(list)) {
           return;
        }
        // 数据处理
        fillList(list);

        // 导出数据
        StringBuffer sb = new StringBuffer();
        String excelPath = "excel/subcontractChangeOrder.xlsx";
        String name = "委外变更单导出";
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
    public String add(SubcontractChangeDTO.AddDTO addDTO) {
        SubcontractChangeEntity subcontractChangeEntity = new SubcontractChangeEntity();
        BeanMapperUtils.copy(addDTO, subcontractChangeEntity);

        // 数据处理
        handleData(subcontractChangeEntity);

        log.info("开始新增委外变更单");
        // 生成单号
        // TODO 此处的null需填写生成单号的分类和类型，category查看BusinessNoConstant，type查看BusinessNoTypeEnum枚举类
        String code = sysUserFeign.getBusinessNo(new SysCodeDTO(null, null));
        subcontractChangeEntity.setCode(code);
        boolean save = super.save(subcontractChangeEntity);
        if(!save) {
           throw new ServiceException("委外变更单保存失败");
        }

        // 操作日志
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(String.format("新增了一个委外变更单【%s】", code), null, subcontractChangeEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）
        return subcontractChangeEntity.getId();
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void update(SubcontractChangeDTO.UpdateDTO updateDTO) {
        SubcontractChangeEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException("未找到委外变更单"));
        // 待提交和审核不通过允许修改
        if (!ApproveStatusEnum.WAIT_SUBMIT.getStatus().equals(old.getApproveStatus()) && !ApproveStatusEnum.REJECT.getStatus().equals(old.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_1029);
        }

        SubcontractChangeEntity subcontractChangeEntity =  BeanMapperUtils.map(SubcontractChangeEntity.class, updateDTO);

        // 数据处理
        handleData(subcontractChangeEntity);

        log.info("编辑 开始修改委外变更单数据，单号：【{}】", old.getCode());
        boolean save = super.updateById(subcontractChangeEntity);
        if(!save) {
           throw new ServiceException("委外变更单保存失败");
        }

        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
        log.info("编辑 开始记录委外变更单日志数据，单号：【{}】", subcontractChangeEntity.getCode());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, subcontractChangeEntity, null, subcontractChangeEntity.getId(), "", "");
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void submit(List<String> ids) {
       if (CollUtil.isEmpty(ids)) {
          throw new ServiceException(ApiError.ERROR_98004);
       }
       List<SubcontractChangeEntity> list = super.listByIds(ids);
       if (CollUtil.isEmpty(list)) {
          throw new ServiceException("未找到委外变更单数据");
       }
       // 待提交或审核不通过并且未作废允许提交
       long count = list.stream().filter(obj -> (!ApproveStatusEnum.WAIT_SUBMIT.getStatus().equals(obj.getApproveStatus()) && !ApproveStatusEnum.REJECT.getStatus().equals(obj.getApproveStatus())) || !InvalidStatusEnum.NOT_VOIDED.getStatus().equals(obj.getInvalidStatus())).count();
       if (count > 0) {
          throw new ServiceException(ApiError.ERROR_98010);
       }

       // 更新单据审核状态
       log.info("提交 开始修改委外变更单状态数据，id集合：【{}】", JSONObject.toJSONString(ids));
       this.updateApproveStatus(ids, ApproveStatusEnum.APPROVE_ING.getStatus());

       // TODO 启动流程（如果需要的话）

       // 记录操作日志
       log.info("提交 开始记录委外变更单日志数据，id集合：【{}】", JSONObject.toJSONString(ids));
       List<Pair<String, String>> pairList = list.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
       // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
       operateLogService.batchAddModuleOperateLog("提交了一个委外变更单【%s】", null, pairList, "提交操作");
    }

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void addAndSubmit(SubcontractChangeDTO.AddDTO dto) {
        // 新增
        String id = this.add(dto);
        // 提交
        this.submit(Arrays.asList(id));
    }

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateAndSubmit(SubcontractChangeDTO.UpdateDTO dto) {
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
        List<SubcontractChangeEntity> list = super.listByIds(ids);
        if (CollUtil.isEmpty(list)) {
            throw new ServiceException("未找到委外变更单数据");
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
        operateLogService.batchAddModuleOperateLog(String.format("审核【%s】了一个委外变更单", approveType.getName()).concat("【%s】").concat(StrUtils.isNotEmpty(dto.getComment()) ? String.format("，意见：%s", dto.getComment()) : ""),
                            null, pairList, "审核操作");
    }

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void disApprove(List<String> ids) {
        List<SubcontractChangeEntity> list = super.listByIds(ids);
        if (CollUtil.isEmpty(list)) {
            throw new ServiceException("未找到委外变更单数据");
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
        operateLogService.batchAddModuleOperateLog("反审核了一个委外变更单【%s】", null, pairList, "反审核操作");
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void delete(List<String> ids) {
       List<SubcontractChangeEntity> list = super.listByIds(ids);
       if (CollUtil.isEmpty(list)) {
         throw new ServiceException("未找到委外变更单数据");
       }
       // 只有待提交且未作废的数据允许删除
       long count = list.stream().filter(obj -> !ApproveStatusEnum.WAIT_SUBMIT.getStatus().equals(obj.getApproveStatus()) || obj.getInvalidStatus() ).count();
       if (count > 0) {
         throw new ServiceException(ApiError.ERROR_98009);
       }
       // 删除日志数据
       log.info("删除 开始删除委外变更单日志数据，id集合：【{}】", JSONObject.toJSONString(ids));
       operateLogService.removeByBusinessIds(ids);

       // TODO 删除明细数据（如果有明细数据的话）

       // 删除主单数据
       log.info("删除 开始删除委外变更单主单数据，id集合：【{}】", JSONObject.toJSONString(ids));
       super.removeByIds(ids);
    }

    /**
    * 撤销
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void cancelProcess(List<String> ids) {
        List<SubcontractChangeEntity> list = super.listByIds(ids);
        // 只有待提交的数据允许撤销
        long count = list.stream().filter(obj -> !ApproveStatusEnum.APPROVE_ING.getStatus().equals(obj.getApproveStatus())).count();
        if (count > 0) {
           throw new ServiceException(ApiError.ERROR_98007);
        }
        // TODO 撤销流程
        log.info("撤销 开始撤销流程，id集合：【{}】",JSONObject.toJSONString(ids));

        log.info("撤销 开始修改委外变更单状态，id集合：【{}】", JSONObject.toJSONString(ids));
        updateApproveStatus(ids, ApproveStatusEnum.WAIT_SUBMIT.getStatus());

        //操作日志
        log.info("撤销 开始记录操作日志，id集合：【{}】", JSONObject.toJSONString(ids));
        List<Pair<String, String>> pairList = list.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.batchAddModuleOperateLog("委外变更单【%s】取消流程", null, pairList, "取消流程操作");
    }

    @Override
    public SubcontractChangeDTO.ViewDTO view(String id) {
        SubcontractChangeEntity subcontractChangeEntity = super.getByIdOpt(id).orElseThrow(()->new ServiceException("未找到委外变更单数据"));
        SubcontractChangeDTO.ViewDTO data = BeanMapperUtils.map(SubcontractChangeDTO.ViewDTO.class, subcontractChangeEntity);
        // TODO 查询明细数据（如果有的话）
        return data;
    }

    /**
    * 审核更新审核信息
    * @param ids
    * @param approveStatus
    */
    public void updateForApprove(List<String> ids, String approveStatus) {
        //当前登录人
        LoginUser userInfo = commonService.getUserInfo();
        this.lambdaUpdate().in(SubcontractChangeEntity::getId, ids)
            .set(SubcontractChangeEntity::getApproveUserId, userInfo.getUid())
            .set(SubcontractChangeEntity::getApproveUserName, userInfo.getUserName())
            .set(SubcontractChangeEntity::getApproveStatus, approveStatus)
            .set(SubcontractChangeEntity::getApproveTime, LocalDateTime.now())
            .update();
     }

    /**
    * 反审核更新审核信息
    * @param ids
    * @param approveStatus
    */
    @Transactional(rollbackFor = Exception.class)
    public void updateForDisApprove(List<String> ids, String approveStatus) {
        this.lambdaUpdate().in(SubcontractChangeEntity::getId, ids)
            .set(SubcontractChangeEntity::getApproveUserId, "")
            .set(SubcontractChangeEntity::getApproveUserName, "")
            .set(SubcontractChangeEntity::getApproveStatus, approveStatus)
            .set(SubcontractChangeEntity::getApproveTime, null)
            .update();
        }

    /**
    * 更新审核状态
    */
    @Transactional(rollbackFor = Exception.class)
    public void updateApproveStatus(List<String> ids, String approveStatus) {
        lambdaUpdate().in(SubcontractChangeEntity::getId, ids)
        .set(SubcontractChangeEntity::getApproveStatus, approveStatus)
        .update();
    }

    /**
    * 分页查询、导出 数据处理
    */
    private void fillList(List<SubcontractChangeDTO.ListDTO> list) {
        if(CollUtil.isEmpty(list)) {
           return;
        }

        // 属性赋值
        for(SubcontractChangeDTO.ListDTO data : list) {
            data.setApproveStatusName(ApproveStatusEnum.getName(data.getApproveStatus()));
            data.setInvalidStatusName(InvalidStatusEnum.getName(data.getInvalidStatus()));
            // TODO 其他如需要显示名称的字段赋值
        }
    }

    /**
    * 分页查询同一主单多行明细只有第一行显示主单字段，其他行赋空
    */
    private void hideData(List<SubcontractChangeDTO.ListDTO> list) {
        Set<String> mainIds = Sets.newHashSet();
        // 同一个主单的其他行明细，只显示第一行的主单字段
        for(SubcontractChangeDTO.ListDTO data : list) {
            if (mainIds.contains(data.getId())) {
                data.setCode(null);
                data.setApproveStatus(null);
                data.setApproveStatusName(null);
                data.setInvalidStatus(null);
                data.setInvalidStatusName(null);
                data.setApproveUserName(null);
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
    private void handleData(SubcontractChangeEntity subcontractChangeEntity) {
        // TODO 验证数据 & 数据赋值
    }

}
