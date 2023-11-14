package com.erp.server.tms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.exception.ExcelCommonException;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.OperationTypeEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.utils.date.DateUtil;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.tms.dto.DictBasicDTO;
import com.erp.model.tms.dto.ShippingTemplateDTO;
import com.erp.model.tms.dto.ShippingTemplateRefChannelDTO;
import com.erp.model.tms.dto.excel.ShippingTemplateCityExcelDTO;
import com.erp.model.tms.entity.LogisticsBillCostEntity;
import com.erp.model.tms.entity.ShippingTemplateEntity;
import com.erp.model.tms.enums.DictBasicEnum;
import com.erp.model.tms.enums.ReconciliationStatusEnum;
import com.erp.server.tms.listener.ShippingTemplateCityExcelListener;
import com.erp.server.tms.mapper.LogisticsBillCostMapper;
import com.erp.server.tms.service.DictBasicService;
import com.erp.server.tms.service.LogisticsBillCostService;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.server.tms.service.OperateLogService;
import com.erp.server.tms.service.CommonService;
import com.common.core.exception.ServiceException;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.tms.dto.LogisticsBillCostDTO;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 * 自发货费用 服务实现类
 * </p>
 *
 * @author Will
 * @since 2023-11-06
 */
@Slf4j
@Service
public class LogisticsBillCostServiceImpl extends SuperServiceImpl<LogisticsBillCostMapper, LogisticsBillCostEntity> implements LogisticsBillCostService {
    @Autowired
    private OperateLogService operateLogService;

    @Autowired
    private CommonService commonService;

    @Autowired
    private DictBasicService dictBasicService;


    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(LogisticsBillCostDTO.AddDTO addDTO) {
        LogisticsBillCostEntity logisticsBillCostEntity = new LogisticsBillCostEntity();
        BeanMapperUtils.copy(addDTO, logisticsBillCostEntity);

        // 数据处理
        handleData(logisticsBillCostEntity);

        log.info("开始新增自发货费用");
        boolean save = super.save(logisticsBillCostEntity);
        if(!save) {
            throw new ServiceException("自发货费用保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", commonService.getUserInfo().getUserName(), "自发货费用" , logisticsBillCostEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, logisticsBillCostEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(logisticsBillCostEntity.getId(), logisticsBillCostEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(LogisticsBillCostDTO.UpdateDTO updateDTO) {
        LogisticsBillCostEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "自发货费用"));
        LogisticsBillCostEntity logisticsBillCostEntity =  BeanMapperUtils.map(LogisticsBillCostEntity.class, updateDTO);

        // 数据处理
        handleData(logisticsBillCostEntity);
        log.info("编辑 开始修改自发货费用数据，id：【{}】", old.getId());
        boolean save = super.updateById(logisticsBillCostEntity);
        if(!save) {
            throw new ServiceException("自发货费用保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录自发货费用日志数据，id：【{}】", logisticsBillCostEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", commonService.getUserInfo().getUserName(), logisticsBillCostEntity.getId(), "自发货费用");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, logisticsBillCostEntity, null, logisticsBillCostEntity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    public List<LogisticsBillCostDTO.TabListDTO> tabList(PermissionsDTO dto) {
        List<LogisticsBillCostDTO.TabListDTO> dbList = baseMapper.tabList(dto.getPermissionSql());
        return dbList;
    }

    @Override
    public PagingVO<LogisticsBillCostDTO.ListDTO> paging(PagingDTO<LogisticsBillCostDTO.PagingParamDTO> pagingDTO) {
        LogisticsBillCostDTO.PagingParamDTO params = pagingDTO.getParams();
        params.setPermissionSql(pagingDTO.getPermissionSql());
        Page query = new Page(pagingDTO.getCurrPage(), pagingDTO.getPageSize());
        IPage<LogisticsBillCostDTO.ListDTO> pageData = this.baseMapper.paging(query, params);
        //清空明细数据
        List<LogisticsBillCostDTO.ListDTO> records = pageData.getRecords();
        if (CollectionUtils.isEmpty(records)) {
            return new PagingVO(pageData);
        }
        //数据赋值处理
        handleDataPaging(records);
        return new PagingVO(pageData);
    }

    @Override
    public BatchResultDTO updateReconciliationStatus(String id, String reconciliationStatus) {
        LogisticsBillCostEntity entity = super.getById(id);
        Optional.ofNullable(entity).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "自发货费用"));

        //状态变更
        lambdaUpdate().eq(LogisticsBillCostEntity::getId, id)
                .set(LogisticsBillCostEntity::getReconciliationStatus, reconciliationStatus)
                .update();

        // 状态变更日志
        log.info("状态变更日志数据，id集合：【{}】", id);
        String msg = StrUtil.format("用户【{}】自发货费用【{}】的【{}】单据{}操作 ", commonService.getUserInfo().getUserName(), entity.getTransportNo(), "自发货费用", ReconciliationStatusEnum.getName(reconciliationStatus));
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.LOGISTICS_BILL_COST.getCode(), entity.getTransportNo(), "启用/停用");
        return BatchResultDTO.success(entity.getId(), entity.getTransportNo(), OperationTypeEnum.UPDATE_STATUS);
    }

    @Override
    public void downloadTemplate(HttpServletResponse response) {
        String path = "classpath:excel/logisticsBillCostTemplate.xlsx";
        String excelName = "template.xlsx";
        ResourceLoader resourceLoader = new DefaultResourceLoader();
        try {
            InputStream inputStream = resourceLoader.getResource(path).getInputStream();
            XSSFWorkbook wb = new XSSFWorkbook(inputStream);
            // 输出Excel文件
            OutputStream output = response.getOutputStream();
            response.reset();
            // 设置文件头
            response.setHeader("Content-Disposition",
                    "attchement;filename=" + new String(excelName.getBytes("gb2312"), "ISO8859-1"));
            response.setContentType("application/msexcel");
            wb.write(output);
            wb.close();
        } catch (Exception e) {
            throw new ServiceException(ApiError.ERROR_95131);
        }
    }

    @Override
    public Boolean importFile(MultipartFile excelFile, HttpServletResponse response) {
        ShippingTemplateCityExcelListener cityExcelListenerUtil = new ShippingTemplateCityExcelListener();
        try {
            EasyExcel.read(excelFile.getInputStream(), ShippingTemplateCityExcelDTO.class, cityExcelListenerUtil).sheet(1).doRead();
        } catch (IOException e) {
            log.error("导入错误！", e);
            throw new ServiceException(ApiError.ERROR_95124);
        } catch (ExcelCommonException e) {
            log.error("导入格式错误！", e);
            throw new ServiceException(ApiError.ERROR_1016);
        }
        

        return null;
    }

    @Override
    public Boolean exportExcel(LogisticsBillCostDTO.ExportExcelParamDTO dto, HttpServletResponse response) {
        List<LogisticsBillCostDTO.ListDTO> resultList = this.baseMapper.listByExportExcel(dto);
        if (CollectionUtils.isEmpty(resultList)) {
            throw new ServiceException(ApiError.EXPORT_DATA_EMPTY);
        }
        //数据赋值处理
        handleDataPaging(resultList);
        String name = "自发货列表列表";
        StringBuffer sb = new StringBuffer();
        String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
        sb.append(date);
        sb.append(name);
        String excelPath = "excel/logisticsBillCost.xlsx";
        try {
            new ExcelPrintUtils().patchExport(resultList, response, sb.toString(), excelPath);
        } catch (IOException e) {
            log.error("自发货列表列表导出出错 >>>>>{}", e);
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(LogisticsBillCostEntity logisticsBillCostEntity) {
    // TODO 验证数据 & 数据赋值
    }

    /**
     * @description: 分页查询数据格式化
     * @author Will
     * @date: 2023/11/13 16:04
     * @param records
     */
    private void handleDataPaging( List<LogisticsBillCostDTO.ListDTO> records)  {
        List<DictBasicDTO.ViewDTO> transportStatusList = dictBasicService.getByKey(DictBasicEnum.TRANSPORT_STATUS.getType());

        for (LogisticsBillCostDTO.ListDTO listDTO : records) {
            listDTO.setSourceTypeName(SourceTypeEnum.getName(listDTO.getSourceType()));
            listDTO.setReconciliationStatusName(ReconciliationStatusEnum.getName(listDTO.getReconciliationStatus()));
            String name = transportStatusList.stream().filter(obj -> obj.getCode().equals(listDTO.getTransportStatus())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
            listDTO.setTransportStatusName(name);
        }
    }
}
