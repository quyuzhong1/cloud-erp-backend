package com.erp.server.bi.controller;

import com.erp.common.controller.BaseController;
import com.erp.common.dto.base.ApiResult;
import com.erp.common.dto.base.BaseIdDTO;
import com.erp.common.dto.base.BaseSearchDTO;
import com.erp.common.dto.base.PagingDTO;
import com.erp.common.vo.PagingVO;
import com.erp.model.bi.dto.SubjectDTO;
import com.erp.model.bi.entity.BiSubjectEntity;
import com.erp.server.bi.service.BiSubjectService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 专题表(BiSubject)表控制层
 *
 * @author yl
 * @since 2022-12-08 14:31:58
 */
@RestController
@RequestMapping("biSubject")
public class BiSubjectController extends BaseController {
    /**
     * 服务对象
     */
    @Resource
    private BiSubjectService biSubjectService;

    /**
     * 分页查询
     *

     * @return 查询结果
     */
    @PostMapping("/paging")
    public ApiResult<PagingVO<BiSubjectEntity>> queryByPage(@RequestBody @Validated PagingDTO<BaseSearchDTO> dto ) {
        return success(this.biSubjectService.queryByPage());
    }



    /**
     * 新增专题

     * @param dto 实体
     * @return 新增结果
     */
    @PostMapping("/add")
    public ApiResult add(@RequestBody @Validated SubjectDTO dto) {
        Boolean flag=this.biSubjectService.addSubject(dto);
        return flag == true ? success() : failure();
    }

    /**
     * 编辑数据
     *
     * @param biSubject 实体
     * @return 编辑结果
     */
    @PostMapping("/update")
    public ApiResult edit(BiSubjectEntity biSubject) {
         Boolean flag=this.biSubjectService.update(biSubject);
        return flag == true ? success() : failure();
    }

    /**
     * 删除数据
     *
     * @return 删除是否成功
     */
     @PostMapping("/delete")
    public ApiResult deleteById(@RequestBody @Validated BaseIdDTO dto) {
        Boolean flag=this.biSubjectService.deleteById(dto.getId());
        return flag == true ? success() : failure();
    }

}

