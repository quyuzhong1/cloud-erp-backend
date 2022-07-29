package com.cloud.erp.admin.modules.sys.controller;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.cloud.erp.admin.modules.sys.dto.SysDepartmentDTO;
import com.cloud.erp.admin.modules.sys.entity.SysDepartmentEntity;
import com.cloud.erp.admin.modules.sys.service.SysDepartmentService;
import com.cloud.erp.admin.modules.sys.vo.SysDepartmentVO;
import com.erp.common.controller.BaseController;
import com.erp.common.dto.ApiResult;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


/**
 * 部门表
 *
 * @author yl
 * @email ylstrive@gmail.com
 * @date 2022-07-11 14:05:47
 */
@RestController
@RequestMapping("sys/department")
public class SysDepartmentController extends BaseController {


    @Autowired
    private SysDepartmentService sysDepartmentService;

    /**
     * 列表
     */
    @RequestMapping("/tree")
    public ApiResult tree() {
        List<SysDepartmentVO>  treeVO=sysDepartmentService.findDepartmentTree();
        return success(treeVO);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    public ApiResult info(@PathVariable("id") String id) {
        SysDepartmentEntity sysDepartment = sysDepartmentService.getById(id);
        return success(sysDepartment);
    }

    /**
     * 保存
     */
    @RequestMapping("/saveOrUpdate")
    public ApiResult save(@RequestBody SysDepartmentEntity sysDepartment) {
        String id=sysDepartment.getId();
        if(StringUtils.isBlank(id)){
            id= IdWorker.getIdStr();
        }
        String parentId=sysDepartment.getParentId();
        if(StringUtils.isBlank(parentId)){
            sysDepartment.setParentId("0");
        }
        sysDepartment.setId(id);
        sysDepartmentService.saveOrUpdate(sysDepartment);
        return success();
    }


    @RequestMapping("/batchSave")
    public ApiResult batchSave(@RequestBody List<SysDepartmentDTO> sysDepartmentTree) {
        sysDepartmentService.saveBatchDepartment(sysDepartmentTree);
        return success();
    }

    @RequestMapping("/list")
    public ApiResult list() {
       List<SysDepartmentEntity>  list=sysDepartmentService.list();
        return success(list);
    }



    /**
     * 删除
     */
    @RequestMapping("/delete")
    public ApiResult delete(@RequestBody List<String> ids) {
        sysDepartmentService.removeByIdList(ids);
        return success();
    }

}
