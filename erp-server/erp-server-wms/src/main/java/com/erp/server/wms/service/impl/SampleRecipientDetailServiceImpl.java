package com.erp.server.wms.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.EasyExcel;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.BaseResultDTO;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.ExcelUtil;
import com.common.core.utils.FastDFSClientUtil;
import com.erp.model.plm.dto.ProductDetailDTO;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.plm.vo.ProductVO;
import com.erp.model.wms.dto.SampleRecipientDetailDTO;
import com.erp.model.wms.dto.excel.SampleRecipientDetailImportExcelDTO;
import com.erp.model.wms.entity.SampleRecipientDetailEntity;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.wms.listener.SampleRecipientDetailExcelListener;
import com.erp.server.wms.mapper.SampleRecipientDetailMapper;
import com.erp.server.wms.service.OperateLogService;
import com.erp.server.wms.service.SampleRecipientDetailService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
/**
 * <p>
 * 样品领用单明细 服务实现类
 * </p>
 *
 * @author wuhaotian
 * @since 2025-08-21
 */
@Slf4j
@Service
public class SampleRecipientDetailServiceImpl extends SuperServiceImpl<SampleRecipientDetailMapper, SampleRecipientDetailEntity> implements SampleRecipientDetailService {
    @Autowired
    private OperateLogService operateLogService;

    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private SysUserFeign sysUserFeign;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(SampleRecipientDetailDTO.AddDTO addDTO) {
        SampleRecipientDetailEntity sampleRecipientDetailEntity = new SampleRecipientDetailEntity();
        BeanMapperUtils.copy(addDTO, sampleRecipientDetailEntity);

        // 数据处理
        handleData(sampleRecipientDetailEntity);

        log.info("开始新增样品领用单明细");
        boolean save = super.save(sampleRecipientDetailEntity);
        if(!save) {
            throw new ServiceException("样品领用单明细保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "样品领用单明细" , sampleRecipientDetailEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, sampleRecipientDetailEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(sampleRecipientDetailEntity.getId(), sampleRecipientDetailEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(SampleRecipientDetailDTO.UpdateDTO addOrUpdateDTO) {
        SampleRecipientDetailEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "样品领用单明细"));
        SampleRecipientDetailEntity sampleRecipientDetailEntity =  BeanMapperUtils.map(SampleRecipientDetailEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(sampleRecipientDetailEntity);
        log.info("编辑 开始修改样品领用单明细数据，id：【{}】", old.getId());
        boolean save = super.updateById(sampleRecipientDetailEntity);
        if(!save) {
            throw new ServiceException("样品领用单明细保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录样品领用单明细日志数据，id：【{}】", sampleRecipientDetailEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), sampleRecipientDetailEntity.getId(), "样品领用单明细");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, sampleRecipientDetailEntity, null, sampleRecipientDetailEntity.getId(), msg);
        return Boolean.TRUE;
    }


    @Override
    public SampleRecipientDetailDTO.ImportDTO importFile(MultipartFile excelFile, HttpServletResponse response) {
        //sku信息
        List<SkuVO> skuList = plmTaskFeign.listApproveSku();
        Map<String, SkuVO> map = skuList.stream().collect(Collectors.toMap(SkuVO::getSkuNo, e -> e, (o1, o2) -> o1));

        //用户
        List<FindUserDTO> userList = sysUserFeign.getUserList();
        SampleRecipientDetailExcelListener excelListenerUtil = new SampleRecipientDetailExcelListener(map, userList);

        try {
            EasyExcel.read(excelFile.getInputStream(), SampleRecipientDetailImportExcelDTO.class, excelListenerUtil).sheet(0).doRead();
        } catch (Exception e) {
            log.error("导入样品领用单明细错误！", e);
            throw new ServiceException(ApiError.ERROR_95124);
        }

        List<SampleRecipientDetailImportExcelDTO> errorList = excelListenerUtil.getErrorList();
        String url = "";
        if (errorList.size() > 0) {
            String fileName = "样品领用单明细错误数据.xlsx";
            File file = ExcelUtil.exportFile(fileName, "error", errorList, SampleRecipientDetailImportExcelDTO.class);
            if (file != null && !file.isDirectory()) {
                url = FastDFSClientUtil.uploadFile(file, fileName);
            }
        }

        SampleRecipientDetailDTO.ImportDTO importDTO = new SampleRecipientDetailDTO.ImportDTO();
        importDTO.setErrorUrl(url);
        List<SampleRecipientDetailDTO.AddDTO> successList = excelListenerUtil.getSuccessList();
        if (CollUtil.isNotEmpty(successList)) {
            //获取产品名称映射
            List<String> skuIds = successList.stream().map(SampleRecipientDetailDTO.AddDTO::getSkuNo).collect(Collectors.toList());
            List<ProductDetailEntity> productList = plmTaskFeign.listBySkuNos(skuIds);
            Map<String, String> productNameMap = productList.stream().collect(Collectors.toMap(ProductDetailEntity::getSkuNo, ProductDetailEntity::getName, (o1, o2) -> o1));
            for (SampleRecipientDetailDTO.AddDTO addDTO : successList) {
                addDTO.setProductName(productNameMap.get(addDTO.getSkuNo()));
            }
            importDTO.setSuccessList(successList);
        }
        return importDTO;
    }

    /**
    * 新增修改处理数据
    */
    private void handleData(SampleRecipientDetailEntity sampleRecipientDetailEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
