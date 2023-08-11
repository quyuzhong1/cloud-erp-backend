package com.erp.server.wms.service.impl;

import cn.hutool.json.JSONUtil;
import com.common.business.config.DocNoGenHelper;
import com.common.business.enums.BusinessNoTypeEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.entity.StocktakingPlanEntity;
import com.erp.model.wms.enums.StocktakingTypeEnum;
import com.erp.server.wms.mapper.StocktakingPlanMapper;
import com.erp.server.wms.service.StocktakingPlanDetailService;
import com.erp.server.wms.service.StocktakingPlanService;
import com.common.business.service.SuperServiceImpl;
import com.erp.server.wms.service.OperateLogService;
import com.erp.server.wms.service.CommonService;
import com.common.core.exception.ServiceException;
import com.erp.rpc.sys.feign.SysUserFeign;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import cn.hutool.core.collection.CollUtil;
import com.google.common.collect.Sets;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.math3.util.Pair;
import io.seata.spring.annotation.GlobalTransactional;

import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.ApproveTypeEnum;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.business.dto.base.*;
import com.erp.model.wms.dto.StocktakingPlanDTO;
import com.common.core.enums.ApiError;
import com.common.core.utils.*;
import com.erp.model.sys.dto.SysCodeDTO;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.utils.date.DateUtil;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
/**
 * <p>
 * 盘点计划表 服务实现类
 * </p>
 *
 * @author Cloud
 * @since 2023-08-08
 */
@Slf4j
@Service
public class StocktakingPlanServiceImpl extends SuperServiceImpl<StocktakingPlanMapper, StocktakingPlanEntity> implements StocktakingPlanService {

    @Autowired
    private SysUserFeign sysUserFeign;
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private CommonService commonService;
    @Resource
    private DocNoGenHelper docNoGenHelper;
    @Resource
    private StocktakingPlanDetailService stocktakingPlanDetailService;

    @Override
    public PagingVO<StocktakingPlanDTO.ListDTO> paging(PagingDTO<StocktakingPlanDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<StocktakingPlanDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
           return new PagingVO(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO(pageData);
    }

    @Override
    public List<StocktakingPlanDTO.TabListDTO> tabList(PermissionsDTO param) {
        StocktakingPlanDTO.PagingParamDTO searchParam = new StocktakingPlanDTO.PagingParamDTO();
        searchParam.setPermissionSql(param.getPermissionSql());
        List<StocktakingPlanDTO.TabListDTO> list = baseMapper.tabList(searchParam);
        // 获取状态列表
        List<String> statusList = ApproveStatusEnum.getStatusList();
        // 不存在的状态赋值为0
        List<String> existStatusList = list.stream().map(StocktakingPlanDTO.TabListDTO::getTabFlag).collect(Collectors.toList());
        statusList.parallelStream().forEach(status -> {
            if(!existStatusList.contains(status)) {
               list.add(new StocktakingPlanDTO.TabListDTO(status, 0));
            }
        });
        list.add(new StocktakingPlanDTO.TabListDTO("all", list.stream().mapToInt(StocktakingPlanDTO.TabListDTO::getCount).sum()));
        // 计算合计数量
        return list;
    }

    @Override
    public void exportList(StocktakingPlanDTO.ExportDTO param, HttpServletResponse response) {
        List<StocktakingPlanDTO.ListDTO> list = this.baseMapper.listExport(param);
        if(CollUtil.isEmpty(list)) {
           return;
        }
        // 数据处理
        fillList(list);

        // 导出数据
        StringBuffer sb = new StringBuffer();
        String excelPath = "excel/stocktakingPlan.xlsx";
        String name = "盘点计划单导出";
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
    public String add(StocktakingPlanDTO.AddDTO addDTO) {
        // 数据处理
        handleData(addDTO);
        log.debug("开始新增盘点计划单 param = {}", JSONUtil.toJsonStr(addDTO));
        // 生成单号
        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.STOCKTAKING_PLAN);
        StocktakingPlanEntity stocktakingPlanEntity = new StocktakingPlanEntity(addDTO, code);
        // 保存主数据
        boolean save = super.save(stocktakingPlanEntity);
        if(!save) {
           throw new ServiceException(ApiError.SAVE_BILL_FAIL, "盘点计划");
        }
        // 保存明细数据
        stocktakingPlanDetailService.saveList(addDTO.getDetailList(), stocktakingPlanEntity.getId());
        // 操作日志
        operateLogService.addModuleOperateLog(String.format("新增盘点计划单【%s】", code), ModuleTypeEnum.STOCKTAKING_PLAN.getCode(), stocktakingPlanEntity.getId(), "新增操作");
        return stocktakingPlanEntity.getCode();
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void update(StocktakingPlanDTO.UpdateDTO updateDTO) {
        StocktakingPlanEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException("未找到盘点计划单"));
        // 待提交和审核不通过允许修改
        if (!ApproveStatusEnum.WAIT_SUBMIT.getStatus().equals(old.getApproveStatus()) && !ApproveStatusEnum.REJECT.getStatus().equals(old.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_1029);
        }

        StocktakingPlanEntity stocktakingPlanEntity =  BeanMapperUtils.map(StocktakingPlanEntity.class, updateDTO);

        // 数据处理
//        handleData(stocktakingPlanEntity);

        log.info("编辑 开始修改盘点计划单数据，单号：【{}】", old.getCode());
        boolean save = super.updateById(stocktakingPlanEntity);
        if(!save) {
           throw new ServiceException("盘点计划单保存失败");
        }

        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
        log.info("编辑 开始记录盘点计划单日志数据，单号：【{}】", stocktakingPlanEntity.getCode());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, stocktakingPlanEntity, null, stocktakingPlanEntity.getId(), "", "");
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void submit(List<String> ids) {
       if (CollUtil.isEmpty(ids)) {
          throw new ServiceException(ApiError.ERROR_98004);
       }
       List<StocktakingPlanEntity> list = super.listByIds(ids);
       if (CollUtil.isEmpty(list)) {
          throw new ServiceException("未找到盘点计划单数据");
       }
       // 待提交或审核不通过并且未作废允许提交
       long count = list.stream().filter(obj -> (!ApproveStatusEnum.WAIT_SUBMIT.getStatus().equals(obj.getApproveStatus()) && !ApproveStatusEnum.REJECT.getStatus().equals(obj.getApproveStatus()))).count();
       if (count > 0) {
          throw new ServiceException(ApiError.ERROR_98010);
       }

       // 更新单据审核状态
       log.info("提交 开始修改盘点计划单状态数据，id集合：【{}】", JSONObject.toJSONString(ids));
       this.updateApproveStatus(ids, ApproveStatusEnum.APPROVE_ING.getStatus());

       // TODO 启动流程（如果需要的话）

       // 记录操作日志
       log.info("提交 开始记录盘点计划单日志数据，id集合：【{}】", JSONObject.toJSONString(ids));
       List<Pair<String, String>> pairList = list.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
       // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
       operateLogService.batchAddModuleOperateLog("提交了一个盘点计划单【%s】", null, pairList, "提交操作");
    }

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void addAndSubmit(StocktakingPlanDTO.AddDTO dto) {
        // 新增
        String id = this.add(dto);
        // 提交
        this.submit(Arrays.asList(id));
    }

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateAndSubmit(StocktakingPlanDTO.UpdateDTO dto) {
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
        List<StocktakingPlanEntity> list = super.listByIds(ids);
        if (CollUtil.isEmpty(list)) {
            throw new ServiceException("未找到盘点计划单数据");
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
        operateLogService.batchAddModuleOperateLog(String.format("审核【%s】了一个盘点计划单", approveType.getName()).concat("【%s】").concat(StrUtils.isNotEmpty(dto.getComment()) ? String.format("，意见：%s", dto.getComment()) : ""),
                            null, pairList, "审核操作");
    }

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void disApprove(List<String> ids) {
        List<StocktakingPlanEntity> list = super.listByIds(ids);
        if (CollUtil.isEmpty(list)) {
            throw new ServiceException("未找到盘点计划单数据");
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
        operateLogService.batchAddModuleOperateLog("反审核了一个盘点计划单【%s】", null, pairList, "反审核操作");
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void delete(List<String> ids) {
       List<StocktakingPlanEntity> list = super.listByIds(ids);
       if (CollUtil.isEmpty(list)) {
         throw new ServiceException("未找到盘点计划单数据");
       }
       // 只有待提交且未作废的数据允许删除
       long count = list.stream().filter(obj -> !ApproveStatusEnum.WAIT_SUBMIT.getStatus().equals(obj.getApproveStatus()) ).count();
       if (count > 0) {
         throw new ServiceException(ApiError.ERROR_98009);
       }
       // 删除日志数据
       log.info("删除 开始删除盘点计划单日志数据，id集合：【{}】", JSONObject.toJSONString(ids));
       operateLogService.removeByBusinessIds(ids);

       // TODO 删除明细数据（如果有明细数据的话）

       // 删除主单数据
       log.info("删除 开始删除盘点计划单主单数据，id集合：【{}】", JSONObject.toJSONString(ids));
       super.removeByIds(ids);
    }

    /**
    * 撤销
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void cancelProcess(List<String> ids) {
        List<StocktakingPlanEntity> list = super.listByIds(ids);
        // 只有待提交的数据允许撤销
        long count = list.stream().filter(obj -> !ApproveStatusEnum.APPROVE_ING.getStatus().equals(obj.getApproveStatus())).count();
        if (count > 0) {
           throw new ServiceException(ApiError.ERROR_98007);
        }
        // TODO 撤销流程
        log.info("撤销 开始撤销流程，id集合：【{}】",JSONObject.toJSONString(ids));

        log.info("撤销 开始修改盘点计划单状态，id集合：【{}】", JSONObject.toJSONString(ids));
        updateApproveStatus(ids, ApproveStatusEnum.WAIT_SUBMIT.getStatus());

        //操作日志
        log.info("撤销 开始记录操作日志，id集合：【{}】", JSONObject.toJSONString(ids));
        List<Pair<String, String>> pairList = list.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.batchAddModuleOperateLog("盘点计划单【%s】取消流程", null, pairList, "取消流程操作");
    }

    @Override
    public StocktakingPlanDTO.ViewDTO view(String id) {
        StocktakingPlanEntity stocktakingPlanEntity = super.getByIdOpt(id).orElseThrow(()->new ServiceException("未找到盘点计划单数据"));
        StocktakingPlanDTO.ViewDTO data = BeanMapperUtils.map(StocktakingPlanDTO.ViewDTO.class, stocktakingPlanEntity);
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
        this.lambdaUpdate().in(StocktakingPlanEntity::getId, ids)
            .set(StocktakingPlanEntity::getApproveUserId, userInfo.getUid())
            .set(StocktakingPlanEntity::getApproveUserName, userInfo.getUserName())
            .set(StocktakingPlanEntity::getApproveStatus, approveStatus)
            .set(StocktakingPlanEntity::getApproveTime, LocalDateTime.now())
            .update();
     }

    /**
    * 反审核更新审核信息
    * @param ids
    * @param approveStatus
    */
    @Transactional(rollbackFor = Exception.class)
    public void updateForDisApprove(List<String> ids, String approveStatus) {
        this.lambdaUpdate().in(StocktakingPlanEntity::getId, ids)
            .set(StocktakingPlanEntity::getApproveUserId, "")
            .set(StocktakingPlanEntity::getApproveUserName, "")
            .set(StocktakingPlanEntity::getApproveStatus, approveStatus)
            .set(StocktakingPlanEntity::getApproveTime, null)
            .update();
        }

    /**
    * 更新审核状态
    */
    @Transactional(rollbackFor = Exception.class)
    public void updateApproveStatus(List<String> ids, String approveStatus) {
        lambdaUpdate().in(StocktakingPlanEntity::getId, ids)
        .set(StocktakingPlanEntity::getApproveStatus, approveStatus)
        .update();
    }

    /**
    * 分页查询、导出 数据处理
    */
    private void fillList(List<StocktakingPlanDTO.ListDTO> list) {
        if(CollUtil.isEmpty(list)) {
           return;
        }

        // 属性赋值
        for(StocktakingPlanDTO.ListDTO data : list) {
            data.setApproveStatusName(data.getApproveStatus().getName());
            data.setModeName(data.getMode().getName());
            data.setTypeName(data.getMode().getName());
        }
    }

    /**
    * 分页查询同一主单多行明细只有第一行显示主单字段，其他行赋空
    */
    private void hideData(List<StocktakingPlanDTO.ListDTO> list) {
        Set<String> mainIds = Sets.newHashSet();
        // 同一个主单的其他行明细，只显示第一行的主单字段
        for(StocktakingPlanDTO.ListDTO data : list) {
            if (mainIds.contains(data.getId())) {
                data.setCode(null);
                data.setApproveStatus(null);
                data.setApproveStatusName(null);
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
    private void handleData(StocktakingPlanDTO.AddDTO dto) {
        // 按照仓库盘点和仓位盘点需要验证动销时间必填
        StocktakingTypeEnum type = dto.getType();
        if (StocktakingTypeEnum.BY_SKU.equals(type)) {
           return;
        }
        // activeSalesTimeList
        List<LocalDateTime> activeSalesTimeList = dto.getActiveSalesTimeList();
        if (activeSalesTimeList.size() > 1) {
              throw new ServiceException(ApiError.TIME_NOT_NULL, "动销时间");
        }
        ValidatorUtil.isNotNull(activeSalesTimeList, ApiError.TIME_NOT_NULL, "动销时间");
        ValidatorUtil.isNotNull(activeSalesTimeList.get(0), ApiError.TIME_NOT_NULL, "动销开始时间");
        ValidatorUtil.isNotNull(activeSalesTimeList.get(1), ApiError.TIME_NOT_NULL, "动销结束时间");
        if (activeSalesTimeList.get(1).compareTo(activeSalesTimeList.get(0)) <= 0) {
            throw new ServiceException(ApiError.START_GE_END_ERROR, "动销开始时间", "动销结束时间");
        }
    }

}
