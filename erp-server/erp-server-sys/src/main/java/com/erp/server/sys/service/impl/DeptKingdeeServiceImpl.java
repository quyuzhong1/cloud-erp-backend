package com.erp.server.sys.service.impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.utils.ExcelUtil;
import com.erp.model.sys.dto.DeptKingdeeDTO;
import com.erp.model.sys.dto.excel.DeptKingdeeImportExcelDTO;
import com.erp.model.sys.entity.DeptKingdeeEntity;
import com.erp.model.sys.entity.SysAccountingCompanyEntity;
import com.erp.model.sys.entity.SysDepartmentEntity;
import com.erp.server.sys.listener.DeptKingdeeExcelListener;
import com.erp.server.sys.mapper.DeptKingdeeMapper;
import com.erp.server.sys.service.DeptKingdeeService;
import com.erp.server.sys.service.SysAccountingCompanyService;
import com.erp.server.sys.service.SysDepartmentService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author Lambda
 * @since 2023-07-07
 */
@Service
public class DeptKingdeeServiceImpl extends SuperServiceImpl<DeptKingdeeMapper, DeptKingdeeEntity> implements DeptKingdeeService {


    @Resource
    private SysDepartmentService sysDepartmentService;

    @Resource
    private SysAccountingCompanyService sysAccountingCompanyService;

    @Override
    public Boolean importFile(MultipartFile excelFile, HttpServletResponse response) {
        //部门信息
        List<SysDepartmentEntity> departmentList = sysDepartmentService.list();
        List<DeptKingdeeEntity> deptKingdeeList = this.list();
        List<SysAccountingCompanyEntity> accountingCompanyList = sysAccountingCompanyService.list();
        DeptKingdeeExcelListener excelListener = new DeptKingdeeExcelListener(this, departmentList, deptKingdeeList, sysDepartmentService,accountingCompanyList);
        try {
            EasyExcel.read(excelFile.getInputStream(), DeptKingdeeImportExcelDTO.class, excelListener).sheet(0).doRead();
        } catch (Exception e) {
            log.error("金蝶部门导入错误！", e);
            return Boolean.FALSE;
        }
        List<DeptKingdeeImportExcelDTO> errorList = excelListener.getErrorList();
        if (errorList.size() > 0) {
            String fileName = "金蝶部门错误信息";
            ExcelUtil.export(fileName, "DeptKingdeeServiceError", errorList, DeptKingdeeImportExcelDTO.class, response);
            return Boolean.FALSE;
        }
        return Boolean.TRUE;

    }


    @Override
    public DeptKingdeeEntity getInfo(DeptKingdeeDTO.FindDeptKingdeeDTO dto) {

        return this.lambdaQuery()
                .eq(StringUtils.isNotBlank(dto.getOrgCode()),DeptKingdeeEntity::getUseOrgCode, dto.getOrgCode())
                .eq(StringUtils.isNotBlank(dto.getOrgId()),DeptKingdeeEntity::getUseOrgId,dto.getOrgId())
                .eq(DeptKingdeeEntity::getDeptId, dto.getDeptId()).last("LIMIT 1")
                .one();
    }
}
