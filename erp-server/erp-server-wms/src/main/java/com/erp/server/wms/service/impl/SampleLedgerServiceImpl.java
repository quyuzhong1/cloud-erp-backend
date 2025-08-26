package com.erp.server.wms.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.entity.SampleLedgerEntity;
import com.erp.server.wms.mapper.SampleLedgerMapper;
import com.erp.server.wms.service.SampleLedgerService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.erp.server.wms.service.OperateLogService;
import com.common.core.exception.ServiceException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.wms.dto.SampleLedgerDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
import com.common.business.dto.base.PermissionsDTO;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import javax.servlet.http.HttpServletResponse;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_WMS_SAMPLE_LEDGER_REPORT;

/**
 * <p>
 * 样品台账统计 服务实现类
 * </p>
 *
 * @author wuhaotian
 * @since 2025-08-21
 */
@Slf4j
@Service
public class SampleLedgerServiceImpl extends SuperServiceImpl<SampleLedgerMapper, SampleLedgerEntity> implements SampleLedgerService {
    @Autowired
    private OperateLogService operateLogService;

    @Autowired
    private DownloadTaskFeign downloadTaskFeign;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(SampleLedgerDTO.AddDTO addDTO) {
        SampleLedgerEntity sampleLedgerEntity = new SampleLedgerEntity();
        BeanMapperUtils.copy(addDTO, sampleLedgerEntity);

        // 数据处理
        handleData(sampleLedgerEntity);

        log.info("开始新增样品台账统计");
        boolean save = super.save(sampleLedgerEntity);
        if(!save) {
            throw new ServiceException("样品台账统计保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "样品台账统计" , sampleLedgerEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, sampleLedgerEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(sampleLedgerEntity.getId(), sampleLedgerEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(SampleLedgerDTO.UpdateDTO addOrUpdateDTO) {
        SampleLedgerEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "样品台账统计"));
        SampleLedgerEntity sampleLedgerEntity =  BeanMapperUtils.map(SampleLedgerEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(sampleLedgerEntity);
        log.info("编辑 开始修改样品台账统计数据，id：【{}】", old.getId());
        boolean save = super.updateById(sampleLedgerEntity);
        if(!save) {
            throw new ServiceException("样品台账统计保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录样品台账统计日志数据，id：【{}】", sampleLedgerEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), sampleLedgerEntity.getId(), "样品台账统计");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, sampleLedgerEntity, null, sampleLedgerEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(SampleLedgerEntity sampleLedgerEntity) {
    // TODO 验证数据 & 数据赋值
    }


    /**
     * 根据用户ID查询台账列表
     * @param dto 查询条件对象，包含用户ID、SKU编号等查询参数
     * @return 符合条件的台账实体列表，如果查询条件为空则返回空列表
     */
    @Override
    public List<SampleLedgerDTO.SkuAvailableQtyDTO> listLedgerByUserId(SampleLedgerDTO.SearchDTO dto){
        if(Objects.isNull(dto)){
            return Collections.emptyList();
        }
        if(StringUtils.isBlank(dto.getUserId())){
            return Collections.emptyList();
        }
        if(CollUtil.isNotEmpty(dto.getSkuNos()) && dto.getSkuNos().size() == 1){
            dto.setSkuNo(dto.getSkuNos().get(0));
        }
        return this.baseMapper.listSkuAvailableQtyByUserId(dto);
    }


    @Override
    public PagingVO<SampleLedgerDTO.SkuAvailableQtyDTO> listSku(PagingDTO<SampleLedgerDTO.SearchDTO> pagingDTO){
        Page<SampleLedgerDTO.SkuAvailableQtyDTO> query = new Page<>(pagingDTO.getCurrPage(), pagingDTO.getPageSize());
        SampleLedgerDTO.SearchDTO params = pagingDTO.getParams();
        if(CollUtil.isNotEmpty(params.getSkuNos()) && params.getSkuNos().size() == 1){
            params.setSkuNo(params.getSkuNos().get(0));
        }
        IPage<SampleLedgerDTO.SkuAvailableQtyDTO> pageData = this.baseMapper.listSku(query, params);
        return new PagingVO<>(pageData);
    }

    /**
     * 分页列表查询
     * @author wuhaotian
     * @date: 2025-08-21
     * @param pagingParamDTO
     * @return PagingVO<SampleLedgerDTO.ListDTO>>
     */
    @Override
    public PagingVO<SampleLedgerDTO.ListDTO> paging(PagingDTO<SampleLedgerDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<SampleLedgerDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
           return new PagingVO(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO(pageData);
    }

    /**
     * 状态统计
     * @author wuhaotian
     * @date: 2025-08-21
     * @param dto
     * @return List<SampleLedgerDTO.TabListDTO>>
     */
    @Override
    public List<SampleLedgerDTO.TabListDTO> tabList(PermissionsDTO dto) {
        // 使用一个SQL查询获取所有状态的统计数量
        List<SampleLedgerDTO.TabListDTO> list = baseMapper.getAllStatusCounts(dto.getPermissionSql());
        return list;
    }

    /**
     * 填充列表数据
     * @param records 记录列表
     */
    private void fillList(List<SampleLedgerDTO.ListDTO> records) {
        // TODO: 根据业务需求填充额外的数据
        // 例如：填充关联的明细信息、计算字段等
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
    public Boolean exportList(SampleLedgerDTO.ExportDTO dto, HttpServletResponse response) {
        downloadTaskFeign.saveDownloadTask("样品台账统计导出", EXPORT_WMS_SAMPLE_LEDGER_REPORT.getCode(), dto);
        return true;
    }

    /**
     * 获取样品台账统计分页数据（用于异步导出）
     * @author wuhaotian
     * @date: 2025-08-21
     * @param dto 分页参数
     * @return 分页结果
     */
    @Override
    public PagingVO<SampleLedgerDTO.ListDTO> getSampleLedgerPageData(PagingDTO<SampleLedgerDTO.ExportDTO> dto) {
        Page<SampleLedgerDTO.ExportDTO> query = new Page<>(dto.getCurrPage(), dto.getPageSize());
        IPage<SampleLedgerDTO.ListDTO> pageData = this.baseMapper.listExport(query, dto.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
           return new PagingVO<>(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO<>(pageData);
    }

}
