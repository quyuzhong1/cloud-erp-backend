package com.erp.server.wms.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.EasyExcel;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.wms.dto.SampleScrapDetailDTO;
import com.erp.model.wms.dto.excel.SampleBorrowDetailImportExcelDTO;
import com.erp.model.wms.dto.excel.SampleScrapDetailImportExcelDTO;
import com.erp.model.wms.entity.SampleBorrowDetailEntity;
import com.erp.model.wms.entity.SampleScrapDetailEntity;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.wms.listener.SampleBorrowDetailExcelListener;
import com.erp.server.wms.listener.SampleScrapDetailExcelListener;
import com.erp.server.wms.mapper.SampleBorrowDetailMapper;
import com.erp.server.wms.service.SampleBorrowDetailService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.erp.server.wms.service.OperateLogService;
import com.erp.server.wms.service.CommonService;
import com.common.core.exception.ServiceException;
import com.erp.server.wms.service.SampleLedgerService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.wms.dto.SampleBorrowDetailDTO;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

import com.common.core.utils.*;
import com.common.core.enums.ApiError;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 * 借用变更单明细表 服务实现类
 * </p>
 *
 * @author jack
 * @since 2025-08-26
 */
@Slf4j
@Service
public class SampleBorrowDetailServiceImpl extends SuperServiceImpl<SampleBorrowDetailMapper, SampleBorrowDetailEntity> implements SampleBorrowDetailService {
    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private SysUserFeign sysUserFeign;
    @Resource
    private SampleLedgerService sampleLedgerService;


    @Override
    public List<SampleBorrowDetailEntity> listByMainId(String id) {
        if(StringUtils.isBlank(id)){
            return Collections.emptyList();
        }
        return lambdaQuery().eq(SampleBorrowDetailEntity::getMainId, id).list();
    }

    @Override
    public SampleBorrowDetailDTO.ImportDTO importFile(MultipartFile excelFile, HttpServletResponse response) {
        //sku信息
        List<SkuVO> skuList = plmTaskFeign.listApproveSku();
        Map<String, SkuVO> map = skuList.stream().collect(Collectors.toMap(SkuVO::getSkuNo, e -> e,(o1, o2)->o1));
        //用户
        List<FindUserDTO> userList = sysUserFeign.getUserList();
        SampleBorrowDetailExcelListener excelListenerUtil = new SampleBorrowDetailExcelListener(sampleLedgerService, map, userList);

        try {
            EasyExcel.read(excelFile.getInputStream(), SampleBorrowDetailImportExcelDTO.class, excelListenerUtil).sheet(0).doRead();
        } catch (Exception e) {
            log.error("导入样品借用单错误！", e);
            throw new ServiceException(ApiError.ERROR_95124);
        }
        List<SampleBorrowDetailImportExcelDTO> errorList = excelListenerUtil.getErrorList();
        String url = "";
        if (errorList.size() > 0) {
            String fileName = "样品借用错误数据.xlsx";
            File file = ExcelUtil.exportFile(fileName, "error", errorList, SampleBorrowDetailImportExcelDTO.class);
            if (file != null && !file.isDirectory()) {
                url = FastDFSClientUtil.uploadFile(file, fileName);
            }
        }
        SampleBorrowDetailDTO.ImportDTO importDTO = new SampleBorrowDetailDTO.ImportDTO();
        importDTO.setErrorUrl(url);
        List<SampleBorrowDetailDTO.AddDTO> successList = excelListenerUtil.getSuccessList();
        if(CollUtil.isNotEmpty(successList)){
            importDTO.setSuccessList(successList);
        }
        return importDTO;
    }


}
