package com.erp.server.plm.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.*;
import com.common.business.vo.PagingVO;
import com.erp.model.plm.dto.excel.CfgMoldReturnImportExcelDTO;
import com.erp.model.plm.entity.CfgMoldReturnAlertRuleEntity;
import com.erp.model.plm.entity.MoldInfoEntity;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.file.feign.FileFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.plm.service.MoldInfoService;
import io.seata.spring.annotation.GlobalTransactional;
import com.common.business.annotation.DistributeLocker;
import com.erp.model.plm.entity.CfgMoldAlertRuleEntity;
import com.erp.server.plm.mapper.CfgMoldAlertRuleMapper;
import com.erp.server.plm.service.CfgMoldAlertRuleService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.erp.server.plm.service.OperateLogService;
import com.erp.server.plm.service.CommonService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.plm.dto.CfgMoldAlertRuleDTO;

import java.math.BigDecimal;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

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
        String msg = StrUtil.format("新增了一个模具预警策略【{}】",cfgMoldAlertRuleEntity.getMoldCode());
        operateLogService.addSysLogBySave(msg, "", cfgMoldAlertRuleEntity.getId(), "");
        return Boolean.TRUE;
    }

    /**
     * 新增修改处理数据
     */
    private void handleData(CfgMoldAlertRuleEntity entity) {
        MoldInfoEntity moldInfoEntity = moldInfoService.getByIdOpt(entity.getMoldId()).orElseThrow(() -> new ServiceException("未找到模具档案数据"));
        //结束日期不能小于开始日期
        if (Objects.nonNull(entity.getEndDate()) && Objects.nonNull(entity.getStartDate()) && entity.getEndDate().isBefore(entity.getStartDate())) {
            throw new ServiceException(ApiError.ERROR_92008);
        }

        //寿命数量、预警寿命（数量）、预警寿命（%）都有值时，修改寿命数量，则计算预警寿命（数量）=寿命数量*预警寿命（%）；若至少存在一个字段值为空，则不做自动计算
        if (Objects.nonNull(entity.getLifeQty()) && Objects.nonNull(entity.getAlertLifeQty()) && Objects.nonNull(entity.getAlertLifeRate())) {
            Integer alertLifeQty = new BigDecimal(entity.getLifeQty()).multiply(entity.getAlertLifeRate()).setScale(0, BigDecimal.ROUND_DOWN).intValue();
            entity.setAlertLifeQty(alertLifeQty);
        }




        entity.setMoldCode(moldInfoEntity.getCode());
        entity.setMoldName(moldInfoEntity.getName());
        entity.setSupplierId(moldInfoEntity.getSupplierId());
        entity.setSupplierCode(moldInfoEntity.getSupplierCode());
        entity.setSupplierName(moldInfoEntity.getSupplierName());
    }

    @Override
    public List<CfgMoldAlertRuleDTO.TabListDTO> tabList(PermissionsDTO dto) {
        return Collections.emptyList();
    }

    @Override
    public PagingVO<CfgMoldAlertRuleDTO.ListDTO> paging(PagingDTO<CfgMoldAlertRuleDTO.PagingParamDTO> dto) {
        return null;
    }

    @Override
    public CfgMoldAlertRuleDTO.ViewDTO view(String id) {
        return null;
    }

    @Override
    public BatchResultDTO delete(String id) {
        return null;
    }

    @Override
    public BatchResultDTO invalid(String id, String remark) {
        return null;
    }

    @Override
    public BatchResultDTO disabled(String id) {
        return null;
    }

    @Override
    public BatchResultDTO enable(String id) {
        return null;
    }

    @Override
    public BatchResultDTO changeDisable(CfgMoldAlertRuleEntity entity) {
        return null;
    }

    @Override
    public void exportList(CfgMoldAlertRuleDTO.PagingParamDTO dto, HttpServletResponse response) {

    }

    @Override
    public Boolean importFile(BaseDTO.ImportDTO dto) {
        return null;
    }

    @Override
    public void importCfgMoldAlert(BaseDTO.ImportDTO dto) {

    }

    @Override
    public void handleImportSuccessList(List<CfgMoldReturnImportExcelDTO> successList, List<CfgMoldReturnImportExcelDTO> errorList2, String importType) {

    }

}
