package com.erp.server.bi.controller;

import com.erp.common.controller.BaseController;
import com.erp.common.dto.base.*;
import com.erp.common.modules.validator.UpdateGroup;
import com.erp.common.vo.PagingVO;
import com.erp.model.bi.dto.CategorySubjectDTO;
import com.erp.model.bi.dto.SubjectDTO;
import com.erp.model.bi.dto.SubjectPagingDTO;
import com.erp.model.bi.dto.UpdateSubjectShareDTO;
import com.erp.server.bi.service.BiSubjectService;
import com.erp.server.bi.service.BiSubjectShareService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 专题表(BiSubject)表控制层
 *
 * @author yl
 * @since 2022-12-08 14:31:58
 */
@RestController
@RequestMapping("bi/subject")
public class BiSubjectController extends BaseController {

    /**
     * 专题服务
     */
    @Resource
    private BiSubjectService biSubjectService;


    @Resource
    private BiSubjectShareService biSubjectShareService;

    /**
     * 分页查询
     *
     * @return 查询结果
     */
    @PostMapping("/paging")
    public ApiResult<PagingVO<SubjectPagingDTO>> queryByPage(@RequestBody @Validated PagingDTO<BaseSearchDTO> dto) {
        PagingVO<SubjectPagingDTO> pagingVO = this.biSubjectService.queryByPage(dto);
        return success(pagingVO);
    }


    /**
     * 新增专题
     *
     * @param dto 实体
     * @return 新增结果
     */
    @PostMapping("/add")
    public ApiResult<String> add(@RequestBody @Validated SubjectDTO dto) {
        String id = this.biSubjectService.addSubject(dto);
        if (StringUtils.isNotBlank(id)) {
            return success(id);
        }
        return failure();
    }

    /**
     * 编辑数据
     *
     * @param biSubject 实体
     * @return 编辑结果
     */
    @PostMapping("/update")
    public ApiResult edit(@RequestBody @Validated(value = {UpdateGroup.class}) SubjectDTO biSubject) {
        String id = this.biSubjectService.update(biSubject);
        if (StringUtils.isNotBlank(id)) {
            return success(id);
        }
        return failure();
    }


    /**
     * 设置仪表盘的分享
     *
     * @return 查询结果
     */
    @PostMapping("/setShare")
    public ApiResult setShare(@RequestBody @Validated UpdateSubjectShareDTO dto) {
        Boolean flag = biSubjectShareService.setShare(dto);
        return flag == true ? success() : failure();
    }

    /**
     * 删除数据
     *
     * @return 删除是否成功
     */
    @PostMapping("/delete")
    public ApiResult deleteById(@RequestBody @Validated BaseIdDTO dto) {
        Boolean flag = this.biSubjectService.deleteById(dto.getId());
        return flag == true ? success() : failure();
    }

    /**
     * 设置专题状态
     *
     * @return 删除是否成功
     */
    @PostMapping("/updateState")
    public ApiResult updateState(@RequestBody @Validated UpdateStateDTO dto) {
        Boolean flag = this.biSubjectService.updateState(dto);
        return flag == true ? success() : failure();
    }



    /**
     * 专题首页
     *
     * @return 删除是否成功
     */
    @PostMapping("/homePage")
    public ApiResult<List<CategorySubjectDTO>> homePage(@RequestBody @Validated BaseSearchDTO dto) {
        List<CategorySubjectDTO> list = this.biSubjectService.homePage(dto.getSearchKeyword());
        return  success(list);
    }


    /**
     * 复制专题
     */
    @PostMapping("/copy")
    public ApiResult copy(@RequestBody @Validated BaseIdDTO dto) {
        Boolean copyResult = biSubjectService.copy(dto.getId());
        return copyResult==true?success():failure();
    }


}

