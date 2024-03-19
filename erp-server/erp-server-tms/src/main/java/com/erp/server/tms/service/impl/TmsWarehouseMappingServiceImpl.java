package com.erp.server.tms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.exception.ExcelCommonException;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.date.DateUtil;
import com.erp.model.tms.dto.TmsWarehouseMappingDTO;
import com.erp.model.tms.dto.excel.LogisticsBillCostExcelDTO;
import com.erp.model.tms.entity.TmsWarehouseMappingEntity;
import com.erp.server.tms.listener.LogisticsBillCostExcelListener;
import com.erp.server.tms.mapper.TmsWarehouseMappingMapper;
import com.erp.server.tms.service.CommonService;
import com.erp.server.tms.service.OperateLogService;
import com.erp.server.tms.service.TmsWarehouseMappingService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;
import java.util.Optional;
/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author will
 * @since 2024-03-19
 */
@Slf4j
@Service
public class TmsWarehouseMappingServiceImpl extends SuperServiceImpl<TmsWarehouseMappingMapper, TmsWarehouseMappingEntity> implements TmsWarehouseMappingService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private CommonService commonService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(TmsWarehouseMappingDTO.AddDTO addDTO) {
        TmsWarehouseMappingEntity tmsWarehouseMappingEntity = new TmsWarehouseMappingEntity();
        BeanMapperUtils.copy(addDTO, tmsWarehouseMappingEntity);

        // 数据处理
        handleData(tmsWarehouseMappingEntity);

        log.info("开始新增");
        boolean save = super.save(tmsWarehouseMappingEntity);
        if(!save) {
            throw new ServiceException("保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", commonService.getUserInfo().getUserName(), "" , tmsWarehouseMappingEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, tmsWarehouseMappingEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(tmsWarehouseMappingEntity.getId(), tmsWarehouseMappingEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(TmsWarehouseMappingDTO.UpdateDTO updateDTO) {
        TmsWarehouseMappingEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, ""));
        TmsWarehouseMappingEntity tmsWarehouseMappingEntity =  BeanMapperUtils.map(TmsWarehouseMappingEntity.class, updateDTO);

        // 数据处理
        handleData(tmsWarehouseMappingEntity);
        log.info("编辑 开始修改数据，id：【{}】", old.getId());
        boolean save = super.updateById(tmsWarehouseMappingEntity);
        if(!save) {
            throw new ServiceException("保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录日志数据，id：【{}】", tmsWarehouseMappingEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", commonService.getUserInfo().getUserName(), tmsWarehouseMappingEntity.getId(), "");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, tmsWarehouseMappingEntity, null, tmsWarehouseMappingEntity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    public PagingVO<TmsWarehouseMappingDTO.ListDTO> paging(PagingDTO<TmsWarehouseMappingDTO.PagingParamDTO> dto) {
        return null;
    }

    @Override
    public TmsWarehouseMappingDTO.ViewDTO view(String id) {
        return null;
    }

    @Override
    public BatchResultDTO delete(String id) {
        return null;
    }

    @Override
    public void downloadTemplate(HttpServletResponse response) {
        String path = "classpath:excel/tmsWarehouseMappingTemplate.xlsx";
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
        LogisticsBillCostExcelListener excelListenerUtil = new LogisticsBillCostExcelListener();
        try {
            EasyExcel.read(excelFile.getInputStream(), LogisticsBillCostExcelDTO.class, excelListenerUtil).sheet(0).doRead();
        } catch (IOException e) {
            log.error("导入错误！", e);
            throw new ServiceException(ApiError.ERROR_95124);
        } catch (ExcelCommonException e) {
            log.error("导入格式错误！", e);
            throw new ServiceException(ApiError.ERROR_1016);
        }
        List<LogisticsBillCostExcelDTO> excelDateList = excelListenerUtil.getExcelDateList();
        if (CollectionUtils.isEmpty(excelDateList)) {
            throw new ServiceException(ApiError.ERROR_95123);
        }
        List<LogisticsBillCostExcelDTO> errorList = excelListenerUtil.getErrorList();

        List<LogisticsBillCostExcelDTO> successList = excelListenerUtil.getSuccessList();
        //处理验证成功数据
        //handleImportSuccessList(successList, errorList);

        if (errorList.size() > 0) {
            StringBuffer sb = new StringBuffer();
            String excelPath = "excel/tmsWarehouseMappingError.xlsx";
            String name = "tmsWarehouseMappingError";
            String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
            sb.append(date);
            sb.append(name);
            try {
                new ExcelPrintUtils().patchExport(errorList, response, sb.toString(), excelPath);
            } catch (IOException e) {
                throw new ServiceException(ApiError.ERROR_95125);
            }
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    @Override
    public Boolean exportExcel(TmsWarehouseMappingDTO.PagingParamDTO dto, HttpServletResponse response) {
        List<TmsWarehouseMappingDTO.ListDTO> resultList = null; //this.baseMapper.listExportExcel(dto);
        if (CollectionUtils.isEmpty(resultList)) {
            throw new ServiceException(ApiError.EXPORT_DATA_EMPTY);
        }
        //数据赋值处理
        //handleDataPaging(resultList);
        String name = "仓库匹配列表";
        StringBuffer sb = new StringBuffer();
        String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
        sb.append(date);
        sb.append(name);
        String excelPath = "excel/tmsWarehouseMapping.xlsx";
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
    private void handleData(TmsWarehouseMappingEntity tmsWarehouseMappingEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
