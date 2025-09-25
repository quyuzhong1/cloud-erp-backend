package com.erp.server.wms.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.EasyExcel;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.wms.dto.SampleBackDetailDTO;
import com.erp.model.wms.dto.excel.SampleBackDetailImportExcelDTO;
import com.erp.model.wms.entity.SampleBackDetailEntity;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.wms.listener.SampleBackDetailExcelListener;
import com.erp.server.wms.mapper.SampleBackDetailMapper;
import com.erp.server.wms.service.CommonService;
import com.erp.server.wms.service.OperateLogService;
import com.erp.server.wms.service.SampleBackDetailService;
import com.erp.server.wms.service.SampleLedgerService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.core.exception.ServiceException;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.util.*;
import java.util.stream.Collectors;
/**
 * <p>
 * 样品退回详情 服务实现类
 * </p>
 *
 * @author wuhaotian
 * @since 2025-08-21
 */
@Slf4j
@Service
public class SampleBackDetailServiceImpl extends SuperServiceImpl<SampleBackDetailMapper, SampleBackDetailEntity> implements SampleBackDetailService {
    @Autowired
    private OperateLogService operateLogService;

    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private SampleLedgerService sampleLedgerService;
    @Autowired
    private SysUserFeign sysUserFeign;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(SampleBackDetailDTO.AddDTO addDTO) {
        SampleBackDetailEntity sampleBackDetailEntity = new SampleBackDetailEntity();
        BeanMapperUtils.copy(addDTO, sampleBackDetailEntity);

        // 数据处理
        handleData(sampleBackDetailEntity);

        log.info("开始新增样品退回详情");
        boolean save = super.save(sampleBackDetailEntity);
        if(!save) {
            throw new ServiceException("样品退回详情保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "样品退回详情" , sampleBackDetailEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, sampleBackDetailEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(sampleBackDetailEntity.getId(), sampleBackDetailEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(SampleBackDetailDTO.UpdateDTO addOrUpdateDTO) {
        SampleBackDetailEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "样品退回详情"));
        SampleBackDetailEntity sampleBackDetailEntity =  BeanMapperUtils.map(SampleBackDetailEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(sampleBackDetailEntity);
        log.info("编辑 开始修改样品退回详情数据，id：【{}】", old.getId());
        boolean save = super.updateById(sampleBackDetailEntity);
        if(!save) {
            throw new ServiceException("样品退回详情保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录样品退回详情日志数据，id：【{}】", sampleBackDetailEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), sampleBackDetailEntity.getId(), "样品退回详情");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, sampleBackDetailEntity, null, sampleBackDetailEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(SampleBackDetailEntity sampleBackDetailEntity) {
    // TODO 验证数据 & 数据赋值
    }

    @Override
    public List<SampleBackDetailEntity> listByMainId(String mainId) {
        return lambdaQuery().eq(SampleBackDetailEntity::getMainId, mainId).list();
    }

    @Override
    public SampleBackDetailDTO.ImportDTO importFile(MultipartFile excelFile, String backUserId, HttpServletResponse response) {
        //sku信息
        List<FindUserDTO> userList = sysUserFeign.getUserList();
        List<SkuVO> skuList = plmTaskFeign.listApproveSku();
        Map<String, SkuVO> map = skuList.stream().collect(Collectors.toMap(SkuVO::getSkuNo, e -> e,(o1, o2)->o1));

        SampleBackDetailExcelListener excelListenerUtil = new SampleBackDetailExcelListener(sampleLedgerService, map, backUserId,userList);

        try {
            EasyExcel.read(excelFile.getInputStream(), SampleBackDetailImportExcelDTO.class, excelListenerUtil).sheet(0).doRead();
        } catch (Exception e) {
            log.error("导入样品退回详情错误！", e);
            throw new ServiceException(ApiError.ERROR_95124);
        }
        List<SampleBackDetailImportExcelDTO> errorList = excelListenerUtil.getErrorList();
        String url = "";
        if (errorList.size() > 0) {
            String fileName = "样品退回详情错误数据.xlsx";
            File file = ExcelUtil.exportFile(fileName, "error", errorList, SampleBackDetailImportExcelDTO.class);
            if (file != null && !file.isDirectory()) {
                url = FastDFSClientUtil.uploadFile(file, fileName);
            }
        }
        SampleBackDetailDTO.ImportDTO importDTO = new SampleBackDetailDTO.ImportDTO();
        importDTO.setErrorUrl(url);
        List<SampleBackDetailDTO.AddDTO> successList = excelListenerUtil.getSuccessList();
        if(CollUtil.isNotEmpty(successList)){
            importDTO.setSuccessList(successList);
        }
        return importDTO;
    }
}
