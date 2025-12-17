package com.erp.server.sys.controller.api;

import com.common.business.dto.base.BaseDropDownDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.sys.dto.DepartmentDTO;
import com.erp.model.sys.dto.DeptUserDTO;
import com.erp.model.sys.dto.SysDepartmentDTO;
import com.erp.model.sys.entity.SysDepartmentEntity;
import com.erp.model.sys.vo.SysDeptDropDownVO;
import com.erp.server.sys.service.SysDepartmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;


/**
 * 部门表
 *
 * @author yl
 * @email ylstrive@gmail.com
 * @date 2022-07-11 14:05:47
 */
@RestController
@LogSystemModule("部门管理")
@RequestMapping("department")
public class SysDepartmentController extends BaseController {


    @Autowired
    private SysDepartmentService sysDepartmentService;

    /**
     * 分页列表
     */
    @RequestMapping("/tree")
    public ApiResult tree() {
        List<DepartmentDTO> treeVO=sysDepartmentService.findDepartmentTree();
        return success(treeVO);
    }

    @GetMapping("/deptUserTree")
    public ApiResult deptUserTree() {
        List<DeptUserDTO> deptUserList = sysDepartmentService.deptUserTree();
        return success(deptUserList);
    }
    /**
     * 级联下拉
     */
    @GetMapping("/cascadeTree")
    public ApiResult<List<DeptUserDTO.Tree>> cascadeTree() {
        return success(sysDepartmentService.cascadeTree());
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
    @LogAction(value = LogActionEnum.INSERT, desc = "保存或更新部门信息")
    @RequestMapping("/saveOrUpdate")
    public ApiResult saveOrUpdate(@RequestBody SysDepartmentEntity sysDepartment) {
        sysDepartmentService.saveOrUpdateSysDept(sysDepartment);
        return success();
    }


    @LogAction(value = LogActionEnum.CUSTOM_BATCH_INSERT, desc = "批量保存部门信息:名称={name}")
    @RequestMapping("/batchSave")
    public ApiResult batchSave(@RequestBody List<SysDepartmentDTO> sysDepartmentTree) {
        sysDepartmentService.saveBatchDepartment(sysDepartmentTree);
        return success();
    }

    @RequestMapping("/list")
    public ApiResult list() {
       List<SysDepartmentEntity> list=sysDepartmentService.list();
        return success(list);
    }



    /**
     * 删除
     */
    @LogAction(value = LogActionEnum.DELETE, desc = "删除部门信息")
    @RequestMapping("/delete")
    public ApiResult delete(@RequestBody List<String> ids) {
        List<BatchResultDTO> resultDTOList = sysDepartmentService.removeByIdList(ids);
        return resultDTOList.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOList) : failure(resultDTOList);
    }

    /**
     * 部门下拉列表
     * @return
     */
    @GetMapping("/drop/down")
    ApiResult<List<SysDeptDropDownVO>> listDeptDropDown(){
        List<SysDepartmentEntity> entities = sysDepartmentService.listDept();
        List<SysDeptDropDownVO> resultList = entities.stream()
                .map(x ->
                        new SysDeptDropDownVO(x.getId(), x.getName())
                )
                .collect(Collectors.toList());
        return success(resultList);
    }

    /**
     * 导入部门关联金蝶信息
     * @param file
     * @return
     */
    @LogAction(value = LogActionEnum.IMPORT, desc = "导入部门关联金蝶信息")
    @PostMapping(value = "importDepatKingdee")
    public ApiResult<Void> importDepatKingdee(@RequestParam(value = "file") MultipartFile file) throws IOException {
        sysDepartmentService.importDeptKingdee(file);
        return success();
    }

}
