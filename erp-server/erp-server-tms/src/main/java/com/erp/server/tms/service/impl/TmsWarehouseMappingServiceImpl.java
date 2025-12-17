package com.erp.server.tms.service.impl;


import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.exception.ExcelCommonException;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.OperationTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.FieldValidUtil;
import com.common.core.utils.date.DateUtil;
import com.erp.model.tms.dto.TmsWarehouseMappingDTO;
import com.erp.model.tms.dto.excel.TmsWarehouseMappingExcelDTO;
import com.erp.model.tms.entity.TmsWarehouseMappingEntity;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.wms.feign.WmsTaskFeign;
import com.erp.server.tms.listener.TmsWarehouseMappingExcelListener;
import com.erp.server.tms.mapper.TmsWarehouseMappingMapper;
import com.erp.server.tms.service.TmsWarehouseMappingService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_TMS_TMS_WAREHOUSE_MAPPING;

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

    @Resource
    private WmsTaskFeign wmsTaskFeign;
    @Resource
    private DownloadTaskFeign downloadTaskFeign;

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(TmsWarehouseMappingDTO.AddDTO addDTO) {
        TmsWarehouseMappingEntity tmsWarehouseMappingEntity = new TmsWarehouseMappingEntity();
        BeanMapperUtils.copy(addDTO, tmsWarehouseMappingEntity);

        //数据校验
        checkData(tmsWarehouseMappingEntity);

        // 数据处理
        handleData(tmsWarehouseMappingEntity);

        log.info("开始新增");
        boolean save = super.save(tmsWarehouseMappingEntity);
        if(!save) {
            throw new ServiceException("保存失败");
        }

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

        //数据校验
        checkData(tmsWarehouseMappingEntity);
        // 数据处理
        handleData(tmsWarehouseMappingEntity);
        log.info("编辑 开始修改数据，id：【{}】", old.getId());
        boolean save = super.updateById(tmsWarehouseMappingEntity);
        if(!save) {
            throw new ServiceException("保存失败");
        }
        return Boolean.TRUE;
    }

    @Override
    public PagingVO<TmsWarehouseMappingDTO.ListDTO> paging(PagingDTO<TmsWarehouseMappingDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<TmsWarehouseMappingDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        return new PagingVO(pageData);
    }

    @Override
    public TmsWarehouseMappingDTO.ViewDTO view(String id) {
        TmsWarehouseMappingEntity entity = super.getByIdOpt(id).orElseThrow(()->new ServiceException("未找到仓库匹配数据"));
        TmsWarehouseMappingDTO.ViewDTO data = BeanMapperUtils.map(TmsWarehouseMappingDTO.ViewDTO.class, entity);
        return data;
    }

    @Override
    public BatchResultDTO delete(String id) {
        TmsWarehouseMappingEntity entity = super.getByIdOpt(id).orElseThrow(()->new ServiceException("未找到仓库匹配数据"));
        // 删除主单数据
        log.info("删除 开始删除仓库匹配数据，id：【{}】", id);
        this.removeById(id);
        return BatchResultDTO.success(entity.getId(), entity.getLogisticsWarehouseCode(), OperationTypeEnum.DELETE);
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
                    "attchement;filename=" + new String(excelName.getBytes("gb2312"), StandardCharsets.ISO_8859_1));
            response.setContentType("application/msexcel");
            wb.write(output);
            wb.close();
        } catch (Exception e) {
            throw new ServiceException(ApiError.ERROR_95131);
        }
    }

    @Override
    public Boolean importFile(MultipartFile excelFile, HttpServletResponse response) {
        TmsWarehouseMappingExcelListener excelListenerUtil = new TmsWarehouseMappingExcelListener();
        try {
            EasyExcel.read(excelFile.getInputStream(), TmsWarehouseMappingExcelDTO.class, excelListenerUtil).sheet(0).doRead();
        } catch (IOException e) {
            log.error("导入错误！", e);
            throw new ServiceException(ApiError.ERROR_95124);
        } catch (ExcelCommonException e) {
            log.error("导入格式错误！", e);
            throw new ServiceException(ApiError.ERROR_1016);
        }
        List<TmsWarehouseMappingExcelDTO> excelDateList = excelListenerUtil.getExcelDateList();
        if (CollectionUtils.isEmpty(excelDateList)) {
            throw new ServiceException(ApiError.ERROR_95123);
        }
        List<TmsWarehouseMappingExcelDTO> errorList = excelListenerUtil.getErrorList();

        List<TmsWarehouseMappingExcelDTO> successList = excelListenerUtil.getSuccessList();
        //处理验证成功数据
        handleImportSuccessList(successList, errorList);

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
    public Boolean exportExcel(TmsWarehouseMappingDTO.PagingParamDTO dto) {
        downloadTaskFeign.saveDownloadTask("仓库匹配列表", EXPORT_TMS_TMS_WAREHOUSE_MAPPING.getCode(), dto);
        return Boolean.TRUE;
    }

    @Override
    public PagingVO<TmsWarehouseMappingDTO.ListDTO> exportWarehouseMapping(PagingDTO<TmsWarehouseMappingDTO.PagingParamDTO> dto) {
        dto.getParams().setPermissionSql(dto.getPermissionSql());
        Page<TmsWarehouseMappingDTO.ListDTO> page = baseMapper.listExportExcel(new Page<>(dto.getCurrPage(), dto.getPageSize()), dto.getParams());
        return new PagingVO<>(page);
    }

    /**
     * @description: 导入成功数据
     * @author Will
     * @date: 2024/3/21 19:01
     * @param successList
     * @param errorList
     */
    private void handleImportSuccessList (List<TmsWarehouseMappingExcelDTO> successList,List<TmsWarehouseMappingExcelDTO> errorList) {
        if (CollectionUtils.isEmpty(successList)) {
            return;
        }
        //仓库信息
        List<String> warehouseNameList = successList.stream().map(TmsWarehouseMappingExcelDTO::getErpWarehouseName).distinct().collect(Collectors.toList());
        List<WarehouseDTO.ListDTO> warehouseList = wmsTaskFeign.listWarehouseByNameList(warehouseNameList);
        //根据编码查询
        List<String> logisticsWarehouseCodeList = successList.stream().map(TmsWarehouseMappingExcelDTO::getLogisticsWarehouseCode).distinct().collect(Collectors.toList());
        List<TmsWarehouseMappingEntity> tmsWarehouseMappingList = this.listByLogisticsWarehouseCodeList(logisticsWarehouseCodeList);
        List<TmsWarehouseMappingEntity> resultList = new ArrayList<>();

        for (TmsWarehouseMappingExcelDTO excelDTO : successList) {
            TmsWarehouseMappingEntity addEntity = new TmsWarehouseMappingEntity();
            List<String> errorMsgList = new ArrayList<>();
            //仓库是否存在
            String warehouseId = warehouseList.stream().filter(obj -> CharSequenceUtil.equals(obj.getName(), excelDTO.getErpWarehouseName())
                            && !obj.getDisabled())
                    .findFirst().flatMap(obj -> Optional.ofNullable(obj.getId())).orElse("");
            if (CharSequenceUtil.isBlank(warehouseId)) {
                errorMsgList.add("未找到有效仓库名称或没有仓库权限");
            }
            //导入数据是否存在重复
            long count = successList.stream().filter(obj -> CharSequenceUtil.equals(excelDTO.getLogisticsWarehouseCode(), obj.getLogisticsWarehouseCode())).count();
            if (count > 1) {
                errorMsgList.add("不能导入重复仓库代码（物流商）");
            }
            //存在错误信息则
            if (CollectionUtils.isNotEmpty(errorMsgList)) {
                excelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
                errorList.add(excelDTO);
                continue;
            }
            addEntity.setErpWarehouseId(warehouseId);
            addEntity.setErpWarehouseName(excelDTO.getErpWarehouseName());
            addEntity.setLogisticsWarehouseCode(excelDTO.getLogisticsWarehouseCode());
            //导入的数据是否存在
            TmsWarehouseMappingEntity old = tmsWarehouseMappingList.stream().filter(obj -> CharSequenceUtil.equals(obj.getLogisticsWarehouseCode(), excelDTO.getLogisticsWarehouseCode()))
                    .findFirst().orElse(null);
            if (ObjectUtil.isNotEmpty(old)) {
                addEntity.setId(old.getId());
            }
            resultList.add(addEntity);
        }
        if (CollectionUtils.isEmpty(resultList)) {
           return;
        }
        this.saveOrUpdateBatch(resultList);
    }

    /**
    * 新增修改处理数据
    */
    private void handleData(TmsWarehouseMappingEntity tmsWarehouseMappingEntity) {
        List<WarehouseDTO.UpdateDTO> warehouseList = wmsTaskFeign.listWarehouseByIds(Arrays.asList(tmsWarehouseMappingEntity.getErpWarehouseId()));
        if (CollectionUtils.isEmpty(warehouseList)) {
            throw new ServiceException(ApiError.ERROR_99002);
        }
        tmsWarehouseMappingEntity.setErpWarehouseName(warehouseList.get(0).getName());
    }


    /**
     * @description: 数据校验
     * @author Will
     * @date: 2024/3/21 14:38
     * @param tmsWarehouseMappingEntity
     */
    private void checkData (TmsWarehouseMappingEntity tmsWarehouseMappingEntity) {
        //查询是否存在相同编码数据
        List<TmsWarehouseMappingEntity> oldList = listByLogisticsWarehouseCodeList(Arrays.asList(tmsWarehouseMappingEntity.getLogisticsWarehouseCode()));
        if (CollectionUtils.isNotEmpty(oldList) && !CharSequenceUtil.equals(tmsWarehouseMappingEntity.getId(),oldList.get(0).getId())) {
            throw new ServiceException(ApiError.ERROR_WAREHOUSE_MAPPING_EXIST,tmsWarehouseMappingEntity.getLogisticsWarehouseCode());
        }
    }

    /**
     * @description: 根据物流仓库编码查询
     * @author Will
     * @date: 2024/3/21 14:40
     * @param logisticsWarehouseCodeList
     * @return TmsWarehouseMappingEntity
     */
    private List<TmsWarehouseMappingEntity> listByLogisticsWarehouseCodeList (List<String> logisticsWarehouseCodeList) {
        if (CollectionUtils.isEmpty(logisticsWarehouseCodeList)) {
            return Collections.EMPTY_LIST;
        }
        return lambdaQuery().in(TmsWarehouseMappingEntity::getLogisticsWarehouseCode,logisticsWarehouseCodeList)
                .list();
    }
}
