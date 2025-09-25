package com.erp.server.wms.service.impl;


import cn.hutool.core.collection.CollUtil;
import com.alibaba.excel.EasyExcel;
import com.common.business.dto.FindUserDTO;
import com.common.business.utils.ApplicationContextUtils;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.ExcelUtil;
import com.common.core.utils.FastDFSClientUtil;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.sys.dto.SysDepartmentDTO;
import com.erp.model.wms.dto.SampleScrapInfoDTO;
import com.erp.model.wms.dto.excel.SampleScrapDetailImportExcelDTO;
import com.erp.model.wms.dto.excel.SampleScrapDetailImportExcelDTO;
import com.erp.model.wms.entity.SampleScrapDetailEntity;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.wms.listener.SampleScrapDetailExcelListener;
import com.erp.server.wms.listener.SampleScrapExcelListener;
import com.erp.server.wms.mapper.SampleScrapDetailMapper;
import com.erp.server.wms.service.SampleLedgerService;
import com.erp.server.wms.service.SampleScrapDetailService;
import com.common.business.service.impl.SuperServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.wms.dto.SampleScrapDetailDTO;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 * 样品报废单明细表 服务实现类
 * </p>
 *
 * @author jack
 * @since 2025-08-20
 */
@Slf4j
@Service
public class SampleScrapDetailServiceImpl extends SuperServiceImpl<SampleScrapDetailMapper, SampleScrapDetailEntity> implements SampleScrapDetailService {


    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private SysUserFeign sysUserFeign;
    @Resource
    private SampleLedgerService sampleLedgerService;

    @Override
    public SampleScrapDetailDTO.ImportDTO importFile(MultipartFile excelFile,String id,String scrapUserId, HttpServletResponse response) {
        //sku信息
        List<SkuVO> skuList = plmTaskFeign.listApproveSku();
        Map<String, SkuVO> map = skuList.stream().collect(Collectors.toMap(SkuVO::getSkuNo, e -> e,(o1, o2)->o1));

        SampleScrapDetailExcelListener excelListenerUtil = new SampleScrapDetailExcelListener(sampleLedgerService, map, scrapUserId,id);

        try {
            EasyExcel.read(excelFile.getInputStream(), SampleScrapDetailImportExcelDTO.class, excelListenerUtil).sheet(0).doRead();
        } catch (Exception e) {
            log.error("导入样品报废单错误！", e);
            throw new ServiceException(ApiError.ERROR_95124);
        }

        List<SampleScrapDetailImportExcelDTO> errorList = excelListenerUtil.getErrorList();
        String url = "";
        if (errorList.size() > 0) {
            String fileName = "样品报废错误数据.xlsx";
            File file = ExcelUtil.exportFile(fileName, "error", errorList, SampleScrapDetailImportExcelDTO.class);
            if (file != null && !file.isDirectory()) {
                url = FastDFSClientUtil.uploadFile(file, fileName);
            }
        }

        SampleScrapDetailDTO.ImportDTO importDTO = new SampleScrapDetailDTO.ImportDTO();
        importDTO.setErrorUrl(url);
        List<SampleScrapDetailDTO.AddDTO> successList = excelListenerUtil.getSuccessList();
        if(CollUtil.isNotEmpty(successList)){
            importDTO.setSuccessList(successList);
        }
        return importDTO;
    }

    @Override
    public List<SampleScrapDetailEntity> listByMainId(String id) {
        if(StringUtils.isBlank(id)){
            return Collections.emptyList();
        }
        return lambdaQuery().eq(SampleScrapDetailEntity::getMainId, id).list();
    }

    @Override
    public Map<String,Integer> listBySku(String id,List<String> skuNos){
        return this.baseMapper.listBySku(id,skuNos);
    }


}
