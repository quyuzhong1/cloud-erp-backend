package com.erp.server.tms.service.impl;


import cn.hutool.core.text.CharSequenceUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.exception.ExcelCommonException;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.tms.dto.LogisticsCarrierDTO;
import com.erp.model.tms.dto.excel.LogisticsCarrierExcelDTO;
import com.erp.model.tms.entity.LogisticsCarrierEntity;
import com.erp.server.tms.listener.LogisticsCarrierExcelListener;
import com.erp.server.tms.mapper.LogisticsCarrierMapper;
import com.erp.server.tms.service.LogisticsCarrierService;
import com.erp.server.tms.service.OperateLogService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * <p>
 * 物流快递/海运/空运公司列表 服务实现类
 * </p>
 *
 * @author zdy
 * @since 2024-05-08
 */
@Slf4j
@Service
public class LogisticsCarrierServiceImpl extends SuperServiceImpl<LogisticsCarrierMapper, LogisticsCarrierEntity> implements LogisticsCarrierService {
    @Resource
    private OperateLogService operateLogService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(LogisticsCarrierDTO.AddDTO addDTO) {
        LogisticsCarrierEntity logisticsCarrierEntity = new LogisticsCarrierEntity();
        BeanMapperUtils.copy(addDTO, logisticsCarrierEntity);

        // 数据处理
        handleData(logisticsCarrierEntity);

        log.info("开始新增物流快递/海运/空运公司列单");
        boolean save = super.save(logisticsCarrierEntity);
        if(!save) {
            throw new ServiceException("物流快递/海运/空运公司列单保存失败");
        }

        // 操作日志
        String msg = CharSequenceUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "物流快递/海运/空运公司列单" , logisticsCarrierEntity.getId());
        
        operateLogService.addModuleOperateLog(msg, null, logisticsCarrierEntity.getId(), "新增操作");
        

        return new BaseResultDTO.AddDTO(logisticsCarrierEntity.getId(), logisticsCarrierEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(LogisticsCarrierDTO.UpdateDTO updateDTO) {
        LogisticsCarrierEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "物流快递/海运/空运公司列单"));
        LogisticsCarrierEntity logisticsCarrierEntity =  BeanMapperUtils.map(LogisticsCarrierEntity.class, updateDTO);

        // 数据处理
        handleData(logisticsCarrierEntity);
        log.info("编辑 开始修改物流快递/海运/空运公司列单数据，id：【{}】", old.getId());
        boolean save = super.updateById(logisticsCarrierEntity);
        if(!save) {
            throw new ServiceException("物流快递/海运/空运公司列单保存失败");
        }
        

        // 记录主单操作日志
            log.info("编辑 开始记录物流快递/海运/空运公司列单日志数据，id：【{}】", logisticsCarrierEntity.getId());
            String msg = CharSequenceUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), logisticsCarrierEntity.getId(), "物流快递/海运/空运公司列单");
        
        operateLogService.addModuleOperateLogByObj(old, logisticsCarrierEntity, null, logisticsCarrierEntity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    public Boolean importFile(MultipartFile excelFile, String logisticsType, HttpServletResponse response) {

        LogisticsCarrierExcelListener excelListenerUtil = new LogisticsCarrierExcelListener();

        try {
            EasyExcel.read(excelFile.getInputStream(), LogisticsCarrierExcelDTO.class, excelListenerUtil).sheet(0).doRead();
        } catch (IOException e) {
            log.error("导入错误！", e);
            throw new ServiceException(ApiError.ERROR_95124);
        } catch (ExcelCommonException e) {
            log.error("导入格式错误！", e);
            throw new ServiceException(ApiError.ERROR_1016);
        }
        //验证导入数据是否为空
        List<LogisticsCarrierExcelDTO> excelDateList = excelListenerUtil.getAllList();
        if (CollectionUtils.isEmpty(excelDateList)) {
            throw new ServiceException(ApiError.ERROR_95123);
        }
        //导入数据处理
        List<LogisticsCarrierExcelDTO> successList = excelListenerUtil.getSuccessList();
        //导出错误数据
        List<LogisticsCarrierExcelDTO> errorList = excelListenerUtil.getErrorList();

        handleImportSuccessData (successList,errorList,logisticsType);
//
//
//        if (CollectionUtils.isNotEmpty(errorList)) {
//            StringBuffer sb = new StringBuffer();
//            String excelPath = "excel/bomCombination.xlsx";
//            String name = "bomCombination";
//            String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
//            sb.append(date);
//            sb.append(name);
//            try {
//                new ExcelPrintUtils().patchExport(errorList, response, sb.toString(), excelPath);
//            } catch (IOException e) {
//                throw new ServiceException(ApiError.ERROR_95125);
//            }
//        }
        return Boolean.TRUE;
    }

    /**
     * 物流下拉框
     * @param searchDTO
     * @return
     */
    @Override
    public PagingVO<LogisticsCarrierDTO.PagingVO> dropDown(PagingDTO<LogisticsCarrierDTO.SearchDTO> searchDTO) {
        Page query = new Page(searchDTO.getCurrPage(), searchDTO.getPageSize());
        LogisticsCarrierDTO.SearchDTO params = searchDTO.getParams();
        IPage<LogisticsCarrierDTO.PagingVO> listIPage = baseMapper.dropDown(query, params);
        return new PagingVO<>(listIPage);
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(LogisticsCarrierEntity logisticsCarrierEntity) {
    
    }

    /**
     * @param successList
     * @param errorList
     * @param logisticsType
     * @description: 导入数据新增处理
     * @author Will
     * @date: 2023/8/17 15:52
     */
    private void handleImportSuccessData (List<LogisticsCarrierExcelDTO> successList, List<LogisticsCarrierExcelDTO> errorList, String logisticsType) {
        if (CollectionUtils.isEmpty(successList)) {
            return;
        }
        List<LogisticsCarrierEntity> entityList = BeanMapperUtils.copyList(LogisticsCarrierEntity.class, successList);
        entityList.forEach(logisticsCarrierEntity -> logisticsCarrierEntity.setLogisticsType(logisticsType));
        this.saveBatch(entityList);
    }
}
