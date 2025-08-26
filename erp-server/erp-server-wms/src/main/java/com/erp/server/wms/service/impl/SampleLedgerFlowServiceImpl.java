package com.erp.server.wms.service.impl;


import cn.hutool.core.util.StrUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.entity.SampleLedgerFlowEntity;
import com.erp.server.wms.mapper.SampleLedgerFlowMapper;
import com.erp.server.wms.service.SampleLedgerFlowService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.erp.server.wms.service.OperateLogService;
import com.erp.server.wms.service.CommonService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.wms.dto.SampleLedgerFlowDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import javax.servlet.http.HttpServletResponse;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_WMS_SAMPLE_LEDGER_FLOW_REPORT;
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
    // TODO 验证数据 & 数据赋值
    }

    /**
     * 分页列表查询
     * @author wuhaotian
     * @date: 2025-08-21
     * @param pagingParamDTO
     * @return PagingVO<SampleLedgerFlowDTO.ListDTO>>
     */
    @Override
    public PagingVO<SampleLedgerFlowDTO.ListDTO> paging(PagingDTO<SampleLedgerFlowDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<SampleLedgerFlowDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
           return new PagingVO(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO(pageData);
    }

    /**
     * 填充列表数据
     * @param records 记录列表
     */
    private void fillList(List<SampleLedgerFlowDTO.ListDTO> records) {
        // TODO: 根据业务需求填充额外的数据
        // 例如：填充关联的明细信息、计算字段等
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
