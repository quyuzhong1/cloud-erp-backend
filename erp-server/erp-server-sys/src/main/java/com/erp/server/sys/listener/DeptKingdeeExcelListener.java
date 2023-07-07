package com.erp.server.sys.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.common.core.utils.FieldValidUtil;
import com.erp.model.sys.dto.excel.DeptKingdeeImportExcelDTO;
import com.erp.model.sys.entity.DeptKingdeeEntity;
import com.erp.model.sys.entity.SysDepartmentEntity;
import com.erp.server.sys.constant.SysConstant;
import com.erp.server.sys.service.DeptKingdeeService;
import com.erp.server.sys.service.SysDepartmentService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Lambda
 * @Classname UserKingdeePostExcelListener
 * @Date 2023-06-02 16:05
 * @Created by yl
 */
public class DeptKingdeeExcelListener extends AnalysisEventListener<DeptKingdeeImportExcelDTO> {


    private DeptKingdeeService deptKingdeeService;

    private List<SysDepartmentEntity> deptList;

    //添加的
    private List<SysDepartmentEntity> addDeptList;

    private List<DeptKingdeeEntity> deptKingdeeList;

    private SysDepartmentService sysDepartmentService;

    private List<DeptKingdeeEntity> addOrUpdateList = new ArrayList<>();

    private List<DeptKingdeeImportExcelDTO> errorList = new ArrayList<>();


    public DeptKingdeeExcelListener(DeptKingdeeService deptKingdeeService, List<SysDepartmentEntity> deptList, List<DeptKingdeeEntity> deptKingdeeList, SysDepartmentService sysDepartmentService) {
        this.deptKingdeeService = deptKingdeeService;
        this.deptList = deptList;
        this.deptKingdeeService = deptKingdeeService;
        this.sysDepartmentService = sysDepartmentService;
        this.deptKingdeeList = deptKingdeeList;
        addDeptList = new ArrayList<>(10);
    }

    /**
     * 解析一行 执行一遍
     *
     * @param excelDTO
     * @param analysisContext
     * @return void
     * @author yl
     * @date 2023-06-02 16:06
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void invoke(DeptKingdeeImportExcelDTO excelDTO, AnalysisContext analysisContext) {
        List<String> errorMsgList = new ArrayList<>();
        //基础验证
        List<String> msgList = FieldValidUtil.fieldValid(excelDTO);
        if (CollectionUtils.isNotEmpty(msgList)) {
            errorMsgList.addAll(msgList);
        }
        //部门名
        String deptName = excelDTO.getKingdeeDeptName();
        SysDepartmentEntity deptInfo = deptList.stream().filter(u -> u.getName().equals(deptName)).findFirst().orElse(null);
        String id = "";
        //就要加的一个部门
        if (Objects.isNull(deptInfo)) {
            SysDepartmentEntity addSysDept = new SysDepartmentEntity();
            addSysDept.setCode(excelDTO.getKingdeeDeptCode());
            addSysDept.setName(deptName);
            addSysDept.setSyncKingdeeId(excelDTO.getSyncKingdeeId());
            addSysDept.setSyncKingdeeStatus("3");
            addSysDept.setType(SysConstant.DEPARTMENT_TYPE);
            id = IdWorker.getIdStr();
            addSysDept.setId(id);
            addDeptList.add(addSysDept);
        } else {
            id = deptInfo.getId();
        }
        if (StringUtils.isBlank(id)) {
            errorMsgList.add("自研系统部门不存在");
        }
        //存在错误数据则直接返回
        if (errorMsgList.size() > 0) {
            excelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
            errorList.add(excelDTO);
            return;
        }


        //查看到对应的部门
        DeptKingdeeEntity dept = deptKingdeeList.stream().filter(p -> p.getKingdeeDeptCode().equals(excelDTO.getKingdeeDeptCode())).findFirst().orElse(new DeptKingdeeEntity());
        dept.setKingdeeDeptCode(excelDTO.getKingdeeDeptCode());
        dept.setUseOrgId(excelDTO.getUseOrgId());
        dept.setUseOrgName(excelDTO.getUseOrgName());
        dept.setDeptId(id);
        dept.setKingdeeDeptName(deptName);
        dept.setDeptName(deptName);
        addOrUpdateList.add(dept);
    }


    /**
     * 全部解析完成后执行
     *
     * @param analysisContext
     * @return void
     * @author yl
     * @date 2023-06-02 16:06
     */
    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
//        if (CollectionUtils.isNotEmpty(addOrUpdateList)) {
//            deptKingdeeService.saveOrUpdateBatch(addOrUpdateList);
//        }
        if (CollectionUtils.isNotEmpty(addDeptList)) {
            sysDepartmentService.saveBatch(addDeptList);
        }

    }

    public List<DeptKingdeeImportExcelDTO> getErrorList() {
        return errorList;
    }
}
