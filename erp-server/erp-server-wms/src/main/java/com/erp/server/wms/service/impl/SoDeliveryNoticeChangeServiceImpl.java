package com.erp.server.wms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.common.business.enums.OperationTypeEnum;
import com.common.business.vo.LoginUser;

import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.wms.dto.SoDeliveryNoticeDTO;
import com.erp.model.wms.entity.SoDeliveryNoticeChangeEntity;
import com.erp.model.wms.entity.SoDeliveryNoticeEntity;
import com.erp.model.wms.enums.SoDeliveryNoticeChangeTypeEnum;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.server.wms.mapper.SoDeliveryNoticeChangeMapper;
import com.erp.server.wms.service.SoDeliveryNoticeChangeService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.erp.server.wms.service.OperateLogService;
import com.common.core.exception.ServiceException;
import com.common.business.config.DocNoGenHelper;
import com.common.core.controller.vo.ApiResult;
import cn.hutool.core.util.ObjectUtil;
import com.erp.server.wms.service.SoDeliveryNoticeService;
import jodd.util.StringUtil;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.wms.dto.SoDeliveryNoticeChangeDTO;
import com.erp.model.workflow.dto.ProcessManagementDTO;
import com.erp.rpc.workflow.WorkflowFeign;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import cn.hutool.core.collection.CollUtil;

import com.common.business.enums.ApproveStatusEnum;
import com.erp.model.scm.enums.InvalidStatusEnum;
import com.common.business.enums.ApproveTypeEnum;
import com.common.business.vo.PagingVO;
import com.common.business.dto.base.*;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.utils.date.DateUtil;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.stream.Collectors;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * 发货通知变更单 服务实现类
 * </p>
 *
 * @author lrp
 * @since 2024-10-23
 */
@Slf4j
@Service
public class SoDeliveryNoticeChangeServiceImpl extends SuperServiceImpl<SoDeliveryNoticeChangeMapper, SoDeliveryNoticeChangeEntity> implements SoDeliveryNoticeChangeService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private DocNoGenHelper docNoGenHelper;
    @Autowired
    private WorkflowFeign workflowFeign;

    @Resource
    private SoDeliveryNoticeService soDeliveryNoticeService;

    @Resource
    private PlmTaskFeign plmTaskFeign;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(SoDeliveryNoticeChangeDTO.AddDTO addDTO) {
        SoDeliveryNoticeChangeEntity soDeliveryNoticeChangeEntity = new SoDeliveryNoticeChangeEntity();
        BeanMapperUtils.copy(addDTO, soDeliveryNoticeChangeEntity);

        // 数据处理
        handleData(soDeliveryNoticeChangeEntity);

        log.info("开始新增发货通知变更单");
        // 生成单号
        // TODO 此处的null需填写生成单号类型，type查看BusinessNoTypeEnum枚举类 注意需要填写prefix 为单号前缀
        String code = docNoGenHelper.generateCode(null);
        soDeliveryNoticeChangeEntity.setCode(code);
        boolean save = super.save(soDeliveryNoticeChangeEntity);
        if(!save) {
            throw new ServiceException("发货通知变更单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据单号为【{}】", UserContext.getDefaultLoginUser().getUserName(), "发货通知变更单" , soDeliveryNoticeChangeEntity.getCode());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, soDeliveryNoticeChangeEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(soDeliveryNoticeChangeEntity.getId(), code);
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(SoDeliveryNoticeChangeDTO.UpdateDTO updateDTO) {
        SoDeliveryNoticeChangeEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "发货通知变更单"));
        // 待提交和审核不通过允许修改
//        if (!ApproveStatusEnum.allowUpdateStatus(old.getApproveStatus())) {
//            throw new ServiceException(ApiError.ERROR_1029);
//        }
        SoDeliveryNoticeChangeEntity soDeliveryNoticeChangeEntity =  BeanMapperUtils.map(SoDeliveryNoticeChangeEntity.class, updateDTO);

        // 数据处理
        handleData(soDeliveryNoticeChangeEntity);
        log.info("编辑 开始修改发货通知变更单数据，单号：【{}】", old.getCode());
        boolean save = super.updateById(soDeliveryNoticeChangeEntity);
        if(!save) {
            throw new ServiceException("发货通知变更单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录发货通知变更单日志数据，单号：【{}】", soDeliveryNoticeChangeEntity.getCode());
            String msg = StrUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), soDeliveryNoticeChangeEntity.getCode(), "发货通知变更单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, soDeliveryNoticeChangeEntity, null, soDeliveryNoticeChangeEntity.getId(), msg);
        return Boolean.TRUE;
    }


    @Override
    public PagingVO<SoDeliveryNoticeChangeDTO.ListDTO> paging(PagingDTO<SoDeliveryNoticeChangeDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<SoDeliveryNoticeChangeDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
           return new PagingVO(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO(pageData);
    }

    @Override
    public List<SoDeliveryNoticeChangeDTO.TabListDTO> tabList(PermissionsDTO param) {
        SoDeliveryNoticeChangeDTO.PagingParamDTO searchParam = new SoDeliveryNoticeChangeDTO.PagingParamDTO();
        searchParam.setPermissionSql(param.getPermissionSql());
        List<SoDeliveryNoticeChangeDTO.TabListDTO> list = baseMapper.tabList(searchParam);
        // 获取状态列表
        List<String> statusList = ApproveStatusEnum.getStatusList();
        // 不存在的状态赋值为0
        List<String> existStatusList = list.stream().map(SoDeliveryNoticeChangeDTO.TabListDTO::getTabFlag).collect(Collectors.toList());
        statusList.parallelStream().forEach(status -> {
            if(!existStatusList.contains(status)) {
            list.add(new SoDeliveryNoticeChangeDTO.TabListDTO(status, 0));
        }
        });
        list.add(new SoDeliveryNoticeChangeDTO.TabListDTO("all", list.stream().mapToInt(SoDeliveryNoticeChangeDTO.TabListDTO::getCount).sum()));
        // 计算合计数量
        return list;
    }

    @Override
    public void exportList(SoDeliveryNoticeChangeDTO.ExportDTO param, HttpServletResponse response) {
        List<SoDeliveryNoticeChangeDTO.ListDTO> list = this.baseMapper.listExport(param);
        if(CollUtil.isEmpty(list)) {
           return;
        }
        // 数据处理
        fillList(list);

        // 导出数据
        StringBuffer sb = new StringBuffer();
        String excelPath = "excel/soDeliveryNoticeChange.xlsx";
        String name = "发货通知变更单导出";
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
        SoDeliveryNoticeChangeEntity entity = getById(id);
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException("未找到发货通知变更单数据");
        }
        validateSubmit(entity);
        // 更新单据审核状态
        log.info("提交 开始修改发货通知变更单状态数据，id：【{}】", id);
        this.updateApproveStatus(id, ApproveStatusEnum.APPROVE_ING.getStatus());

        // TODO 启动流程（如果需要的话）
        log.info("提交 开始启动发货通知变更单流程，id=：【{}】", entity.getId());
        startProcess(entity);
        // 记录操作日志
        log.info("提交 开始记录发货通知变更单日志数据，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据提交审核 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "发货通知变更单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, entity.getId(), "提交操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.SUBMIT);
    }

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO addAndSubmit(SoDeliveryNoticeChangeDTO.AddDTO dto) {
        // 新增
        BaseResultDTO.AddDTO result = this.add(dto);
        // 提交
        this.submit(result.getId());
        return result;
    }

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateAndSubmit(SoDeliveryNoticeChangeDTO.UpdateDTO dto) {
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
        SoDeliveryNoticeChangeEntity entity = getById(dto.getId());
        // 审核中的数据允许审核
        if(!Objects.equals(entity.getApproveStatus(), ApproveStatusEnum.APPROVE_ING)) {
            throw new ServiceException(ApiError.ERROR_98006);
        }
        // 调用流程审核
        approveProcess(entity, dto);
        // 操作日志
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据审核操作  审核结果：【{}】 审核意见 ：【{}】", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "发货通知变更单", approveType.getName(), dto.getComment());
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
    private void approveProcess(SoDeliveryNoticeChangeEntity entity, ApproveOneDTO dto) {
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
        SoDeliveryNoticeChangeEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到发货通知变更单单数据"));
        // 反审核条件判断
        validateDisApprove(entity);
        // TODO 检查是否有下推单据（如果支持下推的话）明细数据

        // 更新审核信息
        updateForDisApprove(id, ApproveStatusEnum.WAIT_SUBMIT.getStatus());

        // 操作日志
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据反审核操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "发货通知变更单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, entity.getId(), "反审核操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.DISAPPROVE);
    }

    private Boolean validateDisApprove(SoDeliveryNoticeChangeEntity entity) {
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
        SoDeliveryNoticeChangeEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到发货通知变更单数据"));
        // 只有待提交数据允许删除
        if (!Objects.equals(ApproveStatusEnum.WAIT_SUBMIT, entity.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_98032);
        }
        // TODO 删除明细数据（如果有明细数据的话）

        // 删除主单数据
        log.info("删除 开始删除发货通知变更单主单数据，id：【{}】", id);
        super.removeById(id);
        // 删除日志数据
        log.info("删除 开始删除发货通知变更单日志数据，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据删除操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "发货通知变更单");
        operateLogService.addModuleOperateLog(msg, null, entity.getCode(), "删除发货通知变更单数据");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.DELETE);
    }

    /**
    * 撤销
    */
    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO cancelProcess(String id) {
        SoDeliveryNoticeChangeEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到发货通知变更单数据"));
        // 只有审核中的单据允许撤销
        if (!Objects.equals(entity.getApproveStatus(), ApproveStatusEnum.APPROVE_ING.getStatus())) {
            throw new ServiceException(ApiError.ERROR_98007);
        }
        // TODO 撤销流程
        log.info("撤销 开始撤销流程，id：【{}】",id);

        log.info("撤销 开始修改发货通知变更单状态，id：【{}】", id);
        updateApproveStatus(id, ApproveStatusEnum.WAIT_SUBMIT.getStatus());

        //操作日志
        log.info("撤销 开始记录操作日志，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据撤销流程操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "发货通知变更单");
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
    public Boolean approveEnd(ApproveOneDTO dto, SoDeliveryNoticeChangeEntity entity) {
        if (ObjectUtil.isEmpty(entity)) {
            return Boolean.TRUE;
        }
        ApproveStatusEnum approveStatus = ApproveStatusEnum.transferApproveType(dto.getType());
        updateForApprove(entity.getId(), approveStatus.getStatus());
        // todo 明细数据处理 上下游数据处理

        return Boolean.TRUE;
    }

    @Override
    public PagingVO<SoDeliveryNoticeChangeDTO.ProductDTO> addProductPaging(PagingDTO<SoDeliveryNoticeChangeDTO.ProductAddDTO> pagingParamDTO) {
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<SoDeliveryNoticeChangeDTO.ProductDTO> pageData = this.baseMapper.productPaging(query, pagingParamDTO.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
            return new PagingVO(pageData);
        }
        List<String> skuIds = pageData.getRecords().stream().map(v->v.getSkuId()).collect(Collectors.toList());
        List<SkuVO> skuVOS = plmTaskFeign.listSkuProductByIds(skuIds);
        // 数据处理
        for (SoDeliveryNoticeChangeDTO.ProductDTO record : pageData.getRecords()) {
            record.setMaxCanChangeQty(record.getSaleQty() - record.getAllNoticeQty() + record.getCurrentNoticeQty());
            SkuVO skuVO = skuVOS.stream().filter(v->v.getSkuId().equals(record.getSkuId())).findFirst().orElse(new SkuVO());
            record.setProductName(skuVO.getSkuName());
        }
        List<String> skuNos = pagingParamDTO.getParams().getSkuNoList();
        if(CollUtil.isNotEmpty(skuNos)){
            List<SoDeliveryNoticeChangeDTO.ProductDTO> productDTOS = new ArrayList<>();
            for (String skuNo : skuNos) {
                SoDeliveryNoticeChangeDTO.ProductDTO productDTO = pageData.getRecords().stream().filter(v->v.getSkuNo().equals(skuNo)).findFirst().orElse(new SoDeliveryNoticeChangeDTO.ProductDTO());
                productDTOS.add(productDTO);
            }
            pageData.setRecords(productDTOS);
        }
        return new PagingVO(pageData);
    }

    @Override
    public SoDeliveryNoticeChangeDTO.ViewDTO view(SoDeliveryNoticeChangeDTO.ViewIdDTO viewIdDTO) {
        String type = viewIdDTO.getType();
        String id = viewIdDTO.getId();
        SoDeliveryNoticeChangeEntity soDeliveryNoticeChangeEntity = new SoDeliveryNoticeChangeEntity();
        SoDeliveryNoticeEntity soDeliveryNoticeEntity;
        if("pushDown".equals(type)){
            soDeliveryNoticeEntity = soDeliveryNoticeService.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到发货通知单数据"));
        }else{
            soDeliveryNoticeChangeEntity = this.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到发货通知变更单数据"));
            soDeliveryNoticeEntity = soDeliveryNoticeService.getByIdOpt(soDeliveryNoticeChangeEntity.getSourceId()).orElseThrow(() -> new ServiceException("未找到发货通知单数据"));
        }
        SoDeliveryNoticeDTO.View noticeView = soDeliveryNoticeService.view(soDeliveryNoticeEntity.getId());
        SoDeliveryNoticeChangeDTO.ViewDTO viewDTO = BeanUtil.toBean(noticeView,SoDeliveryNoticeChangeDTO.ViewDTO.class);
        viewDTO.setId(soDeliveryNoticeChangeEntity.getId());
        viewDTO.setNoticeId(soDeliveryNoticeEntity.getId());
        viewDTO.setCode(soDeliveryNoticeChangeEntity.getCode());
        viewDTO.setNoticeCode(soDeliveryNoticeEntity.getCode());
        viewDTO.setApproveStatus(soDeliveryNoticeChangeEntity.getApproveStatus());
        viewDTO.setApproveStatusName(ApproveStatusEnum.getName(soDeliveryNoticeChangeEntity.getApproveStatus()));
        viewDTO.setSoId(soDeliveryNoticeEntity.getSourceId());
        viewDTO.setSoCode(soDeliveryNoticeEntity.getSourceCode());
        viewDTO.setChangeReason(soDeliveryNoticeChangeEntity.getChangeReason());
        if(StringUtil.isNotBlank(soDeliveryNoticeChangeEntity.getId())){
            List<SoDeliveryNoticeChangeDTO.ViewDetail> detailList = baseMapper.listViewDetailList(soDeliveryNoticeChangeEntity.getId());
            for (SoDeliveryNoticeChangeDTO.ViewDetail viewDetail : detailList) {
                viewDetail.setChangeTypeName(SoDeliveryNoticeChangeTypeEnum.getName(viewDetail.getChangeType()));
                viewDetail.setMaxCanChangeQty(viewDetail.getSaleQty() - viewDetail.getAllNoticeQty() + viewDetail.getCurrentNoticeQty());
            }
            viewDTO.setViewDetailList(detailList);
        }
        return viewDTO;
    }
    /**
    * 启动流程
    *
    * @param entity
    * @return void
    * @Date 2023/7/4 10:07
    **/

    public void startProcess(SoDeliveryNoticeChangeEntity entity) {
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
    private void fillOne(SoDeliveryNoticeChangeDTO.ViewDTO data) {
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
        //当前登录人
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        this.lambdaUpdate().eq(SoDeliveryNoticeChangeEntity::getId, id)
            .set(SoDeliveryNoticeChangeEntity::getApproveUserId, userInfo.getUid())
            .set(SoDeliveryNoticeChangeEntity::getApproveUserName, userInfo.getUserName())
            .set(SoDeliveryNoticeChangeEntity::getApproveStatus, approveStatus)
            .set(SoDeliveryNoticeChangeEntity::getApproveTime, LocalDateTime.now())
            .update(new SoDeliveryNoticeChangeEntity());
     }

    /**
    * 反审核更新审核信息
    * @param id
    * @param approveStatus
    */
    @Transactional(rollbackFor = Exception.class)
    public void updateForDisApprove(String id, String approveStatus) {
        this.lambdaUpdate().eq(SoDeliveryNoticeChangeEntity::getId, id)
            .set(SoDeliveryNoticeChangeEntity::getApproveUserId, "")
            .set(SoDeliveryNoticeChangeEntity::getApproveUserName, "")
            .set(SoDeliveryNoticeChangeEntity::getApproveStatus, approveStatus)
            .set(SoDeliveryNoticeChangeEntity::getApproveTime, null)
            .update(new SoDeliveryNoticeChangeEntity());
        }

    /**
    * 更新审核状态
    */
    @Transactional(rollbackFor = Exception.class)
    public void updateApproveStatus(String id, String approveStatus) {
        lambdaUpdate().eq(SoDeliveryNoticeChangeEntity::getId, id)
        .set(SoDeliveryNoticeChangeEntity::getApproveStatus, approveStatus)
        .update(new SoDeliveryNoticeChangeEntity());
    }

    /**
    * 分页查询、导出 数据处理
    */
    private void fillList(List<SoDeliveryNoticeChangeDTO.ListDTO> list) {
        if(CollUtil.isEmpty(list)) {
           return;
        }

        // 属性赋值
        for(SoDeliveryNoticeChangeDTO.ListDTO data : list) {
            data.setApproveStatusName(ApproveStatusEnum.getName(data.getApproveStatus()));
            data.setInvalidStatusName(InvalidStatusEnum.getName(data.getInvalidStatus()));
            // TODO 其他如需要显示名称的字段赋值
        }
    }
    /**
    * 分页查询、导出 数据处理
    */
    private void validateSubmit(SoDeliveryNoticeChangeEntity entity) {
        // 待提交或审核不通过并且未作废允许提交
//        if(!ApproveStatusEnum.allowUpdateStatus(entity.getApproveStatus())) {
//            throw new ServiceException(ApiError.ERROR_98010);
//        }
        return;
    }

    /**
    * 新增修改处理数据
    */
    private void handleData(SoDeliveryNoticeChangeEntity soDeliveryNoticeChangeEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
