package com.erp.server.plm.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.exception.ExcelCommonException;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.*;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.DisabledEnum;
import com.common.business.enums.FileTaskStatusEnum;
import com.common.business.enums.OperationTypeEnum;
import com.common.business.utils.ApplicationContextUtils;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.erp.model.plm.dto.excel.CfgMoldAlertImportExcelDTO;
import com.erp.model.plm.entity.*;
import com.erp.model.plm.enums.CfgMoldReturnAlertRuleCountDimEnum;
import com.erp.model.scm.enums.InvalidStatusEnum;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.file.feign.FileFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.plm.listener.CfgMoldAlertExcelListener;
import com.erp.server.plm.service.*;
import io.seata.spring.annotation.GlobalTransactional;
import com.common.business.annotation.DistributeLocker;
import com.erp.server.plm.mapper.CfgMoldAlertRuleMapper;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.core.exception.ServiceException;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.plm.dto.CfgMoldAlertRuleDTO;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.common.core.utils.*;
import com.common.core.enums.ApiError;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import static com.common.business.enums.FileTaskEventEnum.*;

/**
 * <p>
 * 模具预警策略 服务实现类
 * </p>
 *
 * @author jack
 * @since 2025-10-20
 */
@Slf4j
@Service
public class CfgMoldAlertRuleServiceImpl extends SuperServiceImpl<CfgMoldAlertRuleMapper, CfgMoldAlertRuleEntity> implements CfgMoldAlertRuleService {
    @Resource
    private OperateLogService operateLogService;
    @Resource
    private MoldInfoService moldInfoService;
    @Resource
    private DownloadTaskFeign downloadTaskFeign;
    @Resource
    private SysUserFeign sysUserFeign;
    @Resource
    private FileFeign fileFeign;
    @Resource
    private MoldMonitorService moldMonitorService;

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(CfgMoldAlertRuleDTO.AddDTO addDTO) {
        CfgMoldAlertRuleEntity cfgMoldAlertRuleEntity = new CfgMoldAlertRuleEntity();
        BeanMapperUtils.copy(addDTO, cfgMoldAlertRuleEntity);

        Integer count = lambdaQuery()
                .eq(CfgMoldAlertRuleEntity::getMoldId, cfgMoldAlertRuleEntity.getMoldId())
                .eq(CfgMoldAlertRuleEntity::getInvalidStatus, Boolean.FALSE)
                .count();
        if(count > 0){
            throw new ServiceException(ApiError.ERROR_HAS_EXIST,cfgMoldAlertRuleEntity.getMoldCode());
        }

        // 数据处理
        handleData(cfgMoldAlertRuleEntity);

        log.info("开始新增模具预警策略");
        boolean save = super.save(cfgMoldAlertRuleEntity);
        if(!save) {
            throw new ServiceException("模具预警策略保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("新增了一个模具预警策略【{}】",cfgMoldAlertRuleEntity.getMoldCode());
        operateLogService.addSysLogBySave(msg, "", cfgMoldAlertRuleEntity.getId(), "");
        return new BaseResultDTO.AddDTO(cfgMoldAlertRuleEntity.getId(), cfgMoldAlertRuleEntity.getId());
    }

    /**
    * 修改
    */
    @DistributeLocker(keyName = "addOrUpdateDTO.getId()")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(CfgMoldAlertRuleDTO.UpdateDTO addOrUpdateDTO) {
        CfgMoldAlertRuleEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "模具预警策略"));
        CfgMoldAlertRuleEntity cfgMoldAlertRuleEntity =  BeanMapperUtils.map(CfgMoldAlertRuleEntity.class, addOrUpdateDTO);

        Integer count = lambdaQuery()
                .eq(CfgMoldAlertRuleEntity::getMoldId, cfgMoldAlertRuleEntity.getMoldId())
                .eq(CfgMoldAlertRuleEntity::getInvalidStatus, Boolean.FALSE)
                .ne(CfgMoldAlertRuleEntity::getId, cfgMoldAlertRuleEntity.getId())
                .count();
        if(count > 0){
            throw new ServiceException(ApiError.ERROR_HAS_EXIST,cfgMoldAlertRuleEntity.getMoldCode());
        }

        cfgMoldAlertRuleEntity.setMoldId(old.getMoldId());
        // 数据处理
        handleData(cfgMoldAlertRuleEntity);
        log.info("编辑 开始修改模具预警策略数据，id：【{}】", old.getId());
        boolean save = super.updateById(cfgMoldAlertRuleEntity);
        if(!save) {
            throw new ServiceException("模具预警策略保存失败");
        }

        // 记录主单操作日志
        log.info("编辑 开始记录模具预警策略日志数据，id：【{}】", cfgMoldAlertRuleEntity.getId());
        String msg = StrUtil.format("用户【{}】编辑【{}】模具编号为【{}】的预警策略", UserContext.getDefaultLoginUser().getUserName(), "模具预警策略", old.getMoldCode());
        operateLogService.addSysLogByUpdate(old, cfgMoldAlertRuleEntity, String.valueOf(CfgMoldAlertRuleEntity.class), old.getId(), "", msg);
        return Boolean.TRUE;
    }

    /**
     * 新增修改处理数据
     */
    private void handleData(CfgMoldAlertRuleEntity entity) {
        MoldInfoEntity moldInfoEntity = moldInfoService.getByIdOpt(entity.getMoldId()).orElseThrow(() -> new ServiceException("未找到模具档案数据"));
        if(!moldInfoEntity.getApproveStatus().equals(ApproveStatusEnum.APPROVE.getCode())){
            throw new ServiceException(ApiError.ERROR_MOLD_NOT_APPROVE);
        }
        //结束日期不能小于开始日期
        if (Objects.nonNull(entity.getEndDate()) && Objects.nonNull(entity.getStartDate()) && entity.getEndDate().isBefore(entity.getStartDate())) {
            throw new ServiceException(ApiError.ERROR_92008);
        }

        //校验寿命数量必须大于预警寿命（数量）
        if(entity.getLifeQty() < entity.getAlertLifeQty()){
            throw new ServiceException(ApiError.ERROR_95302);
        }

        //寿命数量、预警寿命（数量）、预警寿命（%）都有值时，修改寿命数量，则计算预警寿命（数量）=寿命数量*预警寿命（%）；若至少存在一个字段值为空，则不做自动计算
        if(Objects.isNull(entity.getAlertLifeRate())){
            BigDecimal lifeQty = new BigDecimal(entity.getLifeQty());
            BigDecimal alertLifeQty = new BigDecimal(entity.getAlertLifeQty());
            BigDecimal alertLifeRate = alertLifeQty.divide(lifeQty, 6).multiply(new BigDecimal(100));
            entity.setAlertLifeRate(alertLifeRate);
        }else if (Objects.isNull(entity.getAlertLifeQty()) && Objects.nonNull(entity.getAlertLifeRate())) {
            BigDecimal alertLifeRate = entity.getAlertLifeRate().divide(new BigDecimal(100));
            BigDecimal lifeQty = new BigDecimal(entity.getLifeQty());
            Integer alertLifeQty = lifeQty.multiply(alertLifeRate).setScale(0, BigDecimal.ROUND_DOWN).intValue();
            entity.setAlertLifeQty(alertLifeQty);
        }

        entity.setMoldCode(moldInfoEntity.getCode());
        entity.setMoldName(moldInfoEntity.getName());
        entity.setSupplierId(moldInfoEntity.getSupplierId());
        entity.setSupplierCode(moldInfoEntity.getSupplierCode());
        entity.setSupplierName(moldInfoEntity.getSupplierName());
    }

    @Override
    public List<CfgMoldAlertRuleDTO.TabListDTO> tabList(PermissionsDTO param) {
        CfgMoldAlertRuleDTO.PagingParamDTO searchParam = new CfgMoldAlertRuleDTO.PagingParamDTO();
        searchParam.setPermissionSql(param.getPermissionSql());
        List<CfgMoldAlertRuleDTO.TabListDTO> list = baseMapper.tabList(searchParam);
        // 获取状态列表
        List<CfgMoldAlertRuleDTO.TabListDTO> result = new ArrayList<>();
        result.add(new CfgMoldAlertRuleDTO.TabListDTO("all", "全部", 0));
        CfgMoldAlertRuleDTO.TabListDTO tTab = list.stream().filter(e -> e.getTabFlag().equals("t")).findFirst().orElse(new CfgMoldAlertRuleDTO.TabListDTO("t", "", 0));
        tTab.setTabFlagName("禁用");
        result.add(tTab);
        CfgMoldAlertRuleDTO.TabListDTO fTab = list.stream().filter(e -> e.getTabFlag().equals("f")).findFirst().orElse(new CfgMoldAlertRuleDTO.TabListDTO("f", "", 0));
        fTab.setTabFlagName("启用");
        result.add(fTab);
        return result;
    }

    @Override
    public PagingVO<CfgMoldAlertRuleDTO.ListDTO> paging(PagingDTO<CfgMoldAlertRuleDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<CfgMoldAlertRuleDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if (CollUtil.isEmpty(pageData.getRecords())) {
            return new PagingVO(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO(pageData);
    }

    /**
     * 分页查询、导出 数据处理
     */
    private void fillList(List<CfgMoldAlertRuleDTO.ListDTO> list) {
        if (CollUtil.isEmpty(list)) {
            return;
        }

        // 属性赋值
        for (CfgMoldAlertRuleDTO.ListDTO data : list) {
            data.setCountDimName(CfgMoldReturnAlertRuleCountDimEnum.getName(data.getCountDim()));
            data.setInvalidStatusName(InvalidStatusEnum.getName(data.getInvalidStatus()));
            data.setDisabledName(DisabledEnum.getName(data.getDisabled()));
        }
    }

    @Override
    public CfgMoldAlertRuleDTO.ViewDTO view(String id) {
        CfgMoldAlertRuleEntity cfgMoldAlertRuleEntity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到模具预警策略数据"));
        CfgMoldAlertRuleDTO.ViewDTO data = BeanMapperUtils.map(CfgMoldAlertRuleDTO.ViewDTO.class, cfgMoldAlertRuleEntity);
        // 数据填充处理
        fillOne(data);
        return data;
    }

    private void fillOne(CfgMoldAlertRuleDTO.ViewDTO data) {
        if (ObjectUtil.isEmpty(data)) {
            return;
        }
        data.setCountDimName(CfgMoldReturnAlertRuleCountDimEnum.getName(data.getCountDim()));
        data.setInvalidStatusName(InvalidStatusEnum.getName(data.getInvalidStatus()));
        data.setDisabledName(DisabledEnum.getName(data.getDisabled()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO delete(String id) {
        CfgMoldAlertRuleEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到模具预警策略数据"));
        // 只有禁用数据才能删除
        if (Objects.equals(DisabledEnum.ENABLE.getCode(), entity.getDisabled())) {
            throw new ServiceException(ApiError.ERROR_1069);
        }
        // 删除主单数据
        log.info("删除 开始删除模具预警策略主单数据，id：【{}】", id);
        super.removeById(id);
        // 删除日志数据
        log.info("删除 开始删除模具预警策略日志数据，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】模具编码为【{}】的【{}】单据删除操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getMoldCode(), "模具预警策略");
        operateLogService.addSysLogBySave(msg, "", entity.getId(), "");

        //同步删除模具预警监控
        moldMonitorService.lambdaUpdate()
                .eq(MoldMonitorEntity::getSourceId, entity.getId())
                .set(MoldMonitorEntity::getIsDeleted,Boolean.TRUE)
                .update();

        return BatchResultDTO.success(entity.getId(), entity.getMoldCode(), OperationTypeEnum.DELETE);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO invalid(String id, String remark) {
        CfgMoldAlertRuleEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到模具预警策略数据"));
        // 未作废允许作废
        if(!InvalidStatusEnum.NOT_VOIDED.getStatus().equals(entity.getInvalidStatus())){
            throw new ServiceException(ApiError.ERROR_98012);
        }
        log.info("作废 开始修改模具预警策略状态数据，id：【{}】", id);
        lambdaUpdate().eq(CfgMoldAlertRuleEntity::getId, id)
                .set(CfgMoldAlertRuleEntity::getInvalidStatus, InvalidStatusEnum.VOIDED.getStatus())
                .set(CfgMoldAlertRuleEntity::getInvalidRemark, remark)
                .update();

        // 作废日志数据
        log.info("作废 开始记录操作日志，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】模具编码为【{}】的【{}】单据作废操作 作废原因：【{}】", UserContext.getDefaultLoginUser().getUserName(), entity.getMoldCode(), "模具预警策略", remark);
        operateLogService.addSysLogBySave(msg, "", id, "");
        return BatchResultDTO.success(entity.getId(), entity.getMoldCode(), OperationTypeEnum.INVALID);
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO updateStatus(String id, Boolean disabled) {
        CfgMoldAlertRuleEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到模具预警策略数据"));
        log.info("启用/禁用 开始修改模具预警策略状态数据，id：【{}】", id);
        lambdaUpdate().eq(CfgMoldAlertRuleEntity::getId, id)
                .set(CfgMoldAlertRuleEntity::getDisabled, disabled)
                .update();
        // 禁用 日志数据
        log.info("启用/禁用  开始记录操作日志，id：【{}】", id);
        String msg = StrUtil.format("{}了一个模具预警策略【{}】",disabled ? "禁用" : "启用", entity.getMoldCode());
        operateLogService.addSysLogBySave(msg, "", id, "");
        return BatchResultDTO.success(entity.getId(), entity.getMoldCode(), OperationTypeEnum.DISABLED);
    }

    @Override
    public void exportList(CfgMoldAlertRuleDTO.PagingParamDTO param, HttpServletResponse response) {
        downloadTaskFeign.saveDownloadTask("模具预警策略导出", EXPORT_PLM_CFG_MOLD_ALERT.getCode(), param);
    }

    @Override
    public Boolean importFile(BaseDTO.ImportDTO dto) {
        dto.setUserId(UserContext.getDefaultLoginUser().getUid());
        downloadTaskFeign.saveImportTask("导入模具预警策略", IMPORT_PLM_CFG_MOLD_ALERT.getCode(), dto);
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void importCfgMoldAlert(BaseDTO.ImportDTO dto) {
        //已审核且未作废的模具
        List<MoldInfoEntity> moldInfoEntities = moldInfoService.lambdaQuery()
                .eq(MoldInfoEntity::getApproveStatus, ApproveStatusEnum.APPROVE.getCode())
                .eq(MoldInfoEntity::getInvalidStatus, Boolean.FALSE)
                .eq(MoldInfoEntity::getIsDeleted, Boolean.FALSE)
                .list();
        Map<String, MoldInfoEntity> moldInfoMap = moldInfoEntities.stream().collect(Collectors.toMap(MoldInfoEntity::getCode, Function.identity(), (o1, o2) -> o1));

        //用户
        List<FindUserDTO> userList = sysUserFeign.getUserList();

        //设置操作人
        FindUserDTO findUserDTO = userList.stream().filter(e -> StringUtils.isNotBlank(dto.getUserId()) && Objects.equals(e.getUserId(), dto.getUserId())).findFirst().orElse(null);
        if(Objects.nonNull(findUserDTO)){
            LoginUser user = new LoginUser();
            user.setUid(findUserDTO.getUserId());
            user.setUserName(findUserDTO.getUserName());
            user.setRealName(findUserDTO.getRealName());
            user.setUserAccount(findUserDTO.getMobile());
            user.setMobile(findUserDTO.getMobile());
            UserContext.setLoginUser(user);
        }

        CfgMoldAlertExcelListener excelListenerUtil = new CfgMoldAlertExcelListener(dto.getTaskId(),dto.getImportType(),dto.getImportCount(),moldInfoMap);
        try {
            byte[] bytes = fileFeign.downloadFile(dto.getFileUrl());
            EasyExcel.read(new ByteArrayInputStream(bytes), CfgMoldAlertImportExcelDTO.class, excelListenerUtil).sheet(0).doRead();
        } catch (ExcelCommonException e) {
            log.error("导入格式错误！", e);
            throw new ServiceException(ApiError.ERROR_1016);
        }

        BaseDTO.ImportResultDTO importResultDTO = new BaseDTO.ImportResultDTO();
        importResultDTO.setTaskId(dto.getTaskId());
        importResultDTO.setCount(excelListenerUtil.getCount());
        List<CfgMoldAlertImportExcelDTO> errorList = excelListenerUtil.getErrorList();
        String url = "";
        if (CollectionUtils.isNotEmpty(errorList)) {
            //排序
            List<CfgMoldAlertImportExcelDTO> sortedErrorList = errorList.stream()
                    .sorted(Comparator.comparing(CfgMoldAlertImportExcelDTO::getMoldCode))
                    .collect(Collectors.toList());
            String fileName = "模具预警策略错误信息.xlsx";
            File file = ExcelUtil.exportFile(fileName, "error", sortedErrorList, CfgMoldAlertImportExcelDTO.class);
            if (!file.isDirectory()) {
                url = FastDFSClientUtil.uploadFile(file, fileName);
            }
        }
        importResultDTO.setRemark("处理完成，失败" + errorList.size() + "条");
        importResultDTO.setErrorUrl(url);
        importResultDTO.setFinishTime(LocalDateTime.now());
        importResultDTO.setStatus(FileTaskStatusEnum.FINISH.getCode());
        downloadTaskFeign.updateTask(importResultDTO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleImportSuccessList(List<CfgMoldAlertImportExcelDTO> successList, List<CfgMoldAlertImportExcelDTO> errorList2, String importType) {
        if (CollectionUtils.isEmpty(successList)) {
            return;
        }
        CfgMoldAlertRuleServiceImpl bean = ApplicationContextUtils.getBean(CfgMoldAlertRuleServiceImpl.class);

        List<String> moldIdList = successList.stream().map(CfgMoldAlertImportExcelDTO::getMoldId).distinct().collect(Collectors.toList());
        List<CfgMoldAlertRuleEntity> oldList = lambdaQuery()
                .in(CfgMoldAlertRuleEntity::getMoldId, moldIdList)
                .eq(CfgMoldAlertRuleEntity::getInvalidStatus,Boolean.FALSE)
                .list();
        Map<String, CfgMoldAlertRuleEntity> oldMap = oldList.stream().collect(Collectors.toMap(CfgMoldAlertRuleEntity::getMoldId, Function.identity(), (o1, o2) -> o1));

        List<String> errorMsgList = new ArrayList<>();
        errorMsgList.add("模具策略已存在");
        String errorMsg1 = FieldValidUtil.getMsgSort(errorMsgList);

        for (CfgMoldAlertImportExcelDTO excelDTO : successList) {
            String moldId = excelDTO.getMoldId();

            CfgMoldAlertRuleEntity old = oldMap.getOrDefault(moldId, null);
            if(Objects.nonNull(old)){
                excelDTO.setErrorMsg(errorMsg1);
                errorList2.add(excelDTO);
            }else {
                CfgMoldAlertRuleDTO.AddDTO addDTO = new CfgMoldAlertRuleDTO.AddDTO();
                BeanMapper.copy(excelDTO, addDTO);
                bean.add(addDTO);
            }
        }
    }

    @Override
    public List<CfgMoldAlertRuleDTO.ListDTO> listAll(List<String> ids) {
        LocalDate today = LocalDate.now();
        //一个月后的日期
        LocalDate oneMonthLater = today.plusMonths(1);
        return this.baseMapper.listAll(ids,today,oneMonthLater);
    }
}
