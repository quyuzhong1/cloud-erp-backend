package com.erp.server.wms.service.impl;


import cn.hutool.core.util.StrUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.ApproveTypeEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.sys.entity.SysDepartmentEntity;
import com.erp.model.wms.entity.SampleLedgerFlowEntity;
import com.erp.server.wms.mapper.SampleLedgerFlowMapper;
import com.erp.server.wms.service.SampleLedgerFlowService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.erp.server.wms.service.OperateLogService;
import com.erp.server.wms.service.CommonService;
import com.common.core.exception.ServiceException;
import com.erp.server.wms.service.SampleLedgerService;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.wms.dto.SampleLedgerFlowDTO;
import java.util.*;
import java.util.stream.Collectors;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import javax.servlet.http.HttpServletResponse;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_WMS_SAMPLE_LEDGER_FLOW_REPORT;
import com.erp.model.wms.entity.SampleLedgerEntity;
/**
 * <p>
 * 样品台账 服务实现类
 * </p>
 *
 * @author wuhaotian
 * @since 2025-08-21
 */
@Slf4j
@Service
public class SampleLedgerFlowServiceImpl extends SuperServiceImpl<SampleLedgerFlowMapper, SampleLedgerFlowEntity> implements SampleLedgerFlowService {
    @Autowired
    private OperateLogService operateLogService;

    @Autowired
    private DownloadTaskFeign downloadTaskFeign;

    @Autowired
    private SysUserFeign sysUserFeign;

    @Autowired
    private SampleLedgerService sampleLedgerService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    public BaseResultDTO.AddDTO add(SampleLedgerFlowDTO.AddDTO addDTO) {
        SampleLedgerFlowEntity sampleLedgerFlowEntity = new SampleLedgerFlowEntity();
        BeanMapperUtils.copy(addDTO, sampleLedgerFlowEntity);

        // 数据处理
        handleData(sampleLedgerFlowEntity);

        log.info("开始新增样品台账");
        boolean save = super.save(sampleLedgerFlowEntity);
        if(!save) {
            throw new ServiceException("样品台账保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "样品台账" , sampleLedgerFlowEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, sampleLedgerFlowEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(sampleLedgerFlowEntity.getId(), sampleLedgerFlowEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    public Boolean update(SampleLedgerFlowDTO.UpdateDTO addOrUpdateDTO) {
        SampleLedgerFlowEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "样品台账"));
        SampleLedgerFlowEntity sampleLedgerFlowEntity =  BeanMapperUtils.map(SampleLedgerFlowEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(sampleLedgerFlowEntity);
        log.info("编辑 开始修改样品台账数据，id：【{}】", old.getId());
        boolean save = super.updateById(sampleLedgerFlowEntity);
        if(!save) {
            throw new ServiceException("样品台账保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录样品台账日志数据，id：【{}】", sampleLedgerFlowEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), sampleLedgerFlowEntity.getId(), "样品台账");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, sampleLedgerFlowEntity, null, sampleLedgerFlowEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(SampleLedgerFlowEntity sampleLedgerFlowEntity) {
        // 如果deptName为空但deptId不为空，则根据deptId获取deptName
        if (StrUtil.isBlank(sampleLedgerFlowEntity.getDeptName()) && StrUtil.isNotBlank(sampleLedgerFlowEntity.getDeptId())) {
            try {
                // 这里需要根据实际的API调用来获取部门名称
                // 由于没有直接根据ID获取部门名称的接口，暂时留空
                // TODO: 实现根据deptId获取deptName的逻辑
            } catch (Exception e) {
                log.warn("获取部门名称失败，deptId：{}，错误：{}", sampleLedgerFlowEntity.getDeptId(), e.getMessage());
            }
        }
    }

    /**
     * 分页列表查询
     * @author wuhaotian
     * @date: 2025-08-21
     * @param pagingParamDTO
     * @return PagingVO<SampleLedgerFlowDTO.ListDTO>>
     */
    @Override
    public PagingVO<SampleLedgerFlowDTO.ListDTO> paging(PagingDTO<SampleLedgerFlowDTO.PagingParamDTO> dto) {
        dto.getParams().setPermissionSql(dto.getPermissionSql());
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        IPage<SampleLedgerFlowDTO.ListDTO> pageData = this.baseMapper.paging(query, dto.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
           return new PagingVO(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO(pageData);
    }

    /**
     * 新增样品台账流水
     * @author wuhaotian
     * @date: 2025-08-21
     * @param addDTO 台账流水新增参数
     * @return 是否成功
     */
    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    public Boolean addSampleLedgerFlow(SampleLedgerFlowDTO.AddFlowDTO addDTO) {
        if (addDTO == null || CollUtil.isEmpty(addDTO.getDetailList())) {
            log.warn("新增样品台账流水参数为空或明细列表为空");
            return false;
        }
        
        try {
            List<SampleLedgerFlowEntity> flowEntities = new ArrayList<>();
            
            for (SampleLedgerFlowDTO.AddFlowDTO.FlowDetailDTO detail : addDTO.getDetailList()) {
                SampleLedgerFlowEntity flowEntity = new SampleLedgerFlowEntity();
                
                // 设置基本信息
                flowEntity.setSourceType(addDTO.getSourceType());
                flowEntity.setSourceCode(addDTO.getSourceCode());
                flowEntity.setSourceId(addDTO.getSourceId());
                flowEntity.setSourceDetailId(detail.getSourceDetailId());
                flowEntity.setDictBizType(addDTO.getApproveType());
                flowEntity.setOperateTime(addDTO.getOperateTime());
                flowEntity.setBillDate(addDTO.getBillDate());
                
                // 设置用户和部门信息
                flowEntity.setUserId(addDTO.getUserId());
                flowEntity.setUserName(addDTO.getUserName());
                flowEntity.setDeptId(addDTO.getDeptId());
//                flowEntity.setDeptName(addDTO.getDeptName());
                flowEntity.setUseUserId(addDTO.getUseUserId());
                flowEntity.setUseUserName(addDTO.getUseUserName());
                
                // 设置SKU信息
                flowEntity.setSkuNo(detail.getSkuNo());
                flowEntity.setSkuId(detail.getSkuId());
                flowEntity.setProductName(detail.getProductName());
                flowEntity.setQty(detail.getQty());
                flowEntity.setSampleLedgerId(detail.getSampleLedgerId());
                
                flowEntities.add(flowEntity);
            }
            
            // 批量保存
            boolean result = super.saveBatch(flowEntities);
            if (result) {
                log.info("新增样品台账流水成功，单据类型：{}，单据编号：{}，明细数量：{}", 
                    addDTO.getSourceType(), addDTO.getSourceCode(), flowEntities.size());
                
                // 更新SampleLedger主表数量
                updateSampleLedgerQty(addDTO);
            } else {
                log.error("新增样品台账流水失败，单据类型：{}，单据编号：{}", 
                    addDTO.getSourceType(), addDTO.getSourceCode());
            }
            
            return result;
            
        } catch (Exception e) {
            log.error("新增样品台账流水异常，单据类型：{}，单据编号：{}，错误：{}", 
                addDTO.getSourceType(), addDTO.getSourceCode(), e.getMessage(), e);
            throw new ServiceException("新增样品台账流水失败：" + e.getMessage());
        }
    }

    /**
     * 更新SampleLedger主表数量
     * 通过查询明细表，按四个条件分组求和来更新主表数量
     */
    private void updateSampleLedgerQty(SampleLedgerFlowDTO.AddFlowDTO addDTO) {
        try {
            for (SampleLedgerFlowDTO.AddFlowDTO.FlowDetailDTO detail : addDTO.getDetailList()) {
                // 查询该SKU在明细表中的数量总和
                Integer totalQty = this.baseMapper.selectTotalQtyByConditions(
                    addDTO.getUserId(),
                    addDTO.getUseUserId(),
                    detail.getSkuNo(),
                    detail.getSkuId()
                );
                
                if (totalQty != null) {
                    // 更新或插入SampleLedger主表记录
                    updateOrInsertSampleLedger(
                        addDTO.getUserId(),
                        addDTO.getUserName(),
                        addDTO.getDeptId(),
                        addDTO.getDeptName(),
                        detail.getSkuNo(),
                        detail.getSkuId(),
                        detail.getProductName(),
                        addDTO.getUseUserId(),
                        addDTO.getUseUserName(),
                        totalQty
                    );
                }
            }
        } catch (Exception e) {
            log.error("更新SampleLedger主表数量失败，错误：{}", e.getMessage(), e);
            // 更新失败不影响主流程，只记录日志
        }
    }

    /**
     * 更新或插入SampleLedger主表记录
     */
    private void updateOrInsertSampleLedger(String userId, String userName, String deptId, String deptName,
                                          String skuNo, String skuId, String productName, String useUserId, 
                                          String useUserName, Integer totalQty) {
        try {
            // 查询是否已存在记录
            SampleLedgerEntity existingLedger = this.baseMapper.selectSampleLedgerByConditions(
                userId, useUserId, skuNo, skuId
            );
            
            if (existingLedger != null) {
                // 更新现有记录
                existingLedger.setQty(totalQty);
                existingLedger.setUserName(userName);
                existingLedger.setDeptName(deptName);
                existingLedger.setProductName(productName);
                existingLedger.setUseUserName(useUserName);
                // 使用SampleLedgerService来更新
                sampleLedgerService.updateById(existingLedger);
                log.debug("更新SampleLedger记录成功，ID：{}，数量：{}", existingLedger.getId(), totalQty);
            } else {
                // 插入新记录
                SampleLedgerEntity newLedger = new SampleLedgerEntity();
                newLedger.setUserId(userId);
                newLedger.setUserName(userName);
                newLedger.setDeptId(deptId);
                newLedger.setDeptName(deptName);
                newLedger.setSkuNo(skuNo);
                newLedger.setSkuId(skuId);
                newLedger.setProductName(productName);
                newLedger.setUseUserId(useUserId);
                newLedger.setUseUserName(useUserName);
                newLedger.setQty(totalQty);
                // 使用SampleLedgerService来插入
                sampleLedgerService.save(newLedger);
                log.debug("插入SampleLedger记录成功，数量：{}", totalQty);
            }
        } catch (Exception e) {
            log.error("更新或插入SampleLedger记录失败，错误：{}", e.getMessage(), e);
        }
    }

    /**
     * 填充列表数据
     * @param records 记录列表
     */
    private void fillList(List<SampleLedgerFlowDTO.ListDTO> records) {
        if (CollUtil.isEmpty(records)) {
            return;
        }
        
        // 如果需要根据deptId填充deptName，可以在这里实现
        // 收集需要查询的deptId
        List<String> deptIds = records.stream()
                .filter(record -> StrUtil.isNotBlank(record.getDeptId()) && StrUtil.isBlank(record.getDeptName()))
                .map(SampleLedgerFlowDTO.ListDTO::getDeptId)
                .collect(Collectors.toList());
        List<SysDepartmentEntity> deptByIds = sysUserFeign.getDeptByIds(deptIds);
        Map<String, String> collect = deptByIds.stream().collect(Collectors.toMap(SysDepartmentEntity::getId, SysDepartmentEntity::getName, (v1, v2) -> v1));
        if (CollUtil.isNotEmpty(deptIds)) {
            try {
                for (SampleLedgerFlowDTO.ListDTO record : records) {
                    record.setDeptName(collect.get(record.getDeptId()));
                    record.setDictBizType(ApproveTypeEnum.getName(record.getDictBizType()));
                    record.setSourceType(SourceTypeEnum.getName(record.getSourceType()));
                }
            } catch (Exception e) {
                log.warn("批量获取部门名称失败，错误：{}", e.getMessage());
            }
        }
    }

    /**
     * 获取样品台账流水分页数据（用于异步导出）
     * @author wuhaotian
     * @date: 2025-08-21
     * @param dto 分页参数
     * @return 分页结果
     */
    @Override
    public PagingVO<SampleLedgerFlowDTO.ListDTO> getSampleLedgerFlowPageData(PagingDTO<SampleLedgerFlowDTO.ExportDTO> dto) {
        Page<SampleLedgerFlowDTO.ExportDTO> query = new Page<>(dto.getCurrPage(), dto.getPageSize());
        IPage<SampleLedgerFlowDTO.ListDTO> pageData = this.baseMapper.listExport(query, dto.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
           return new PagingVO<>(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO<>(pageData);
    }

    /**
     * 异步导出
     * @author wuhaotian
     * @date: 2025-08-21
     * @param dto
     * @param response
     * @return
     */
    @Override
    public Boolean exportList(SampleLedgerFlowDTO.ExportDTO dto, HttpServletResponse response) {
        downloadTaskFeign.saveDownloadTask("样品台账流水导出", EXPORT_WMS_SAMPLE_LEDGER_FLOW_REPORT.getCode(), dto);
        return true;
    }

}
