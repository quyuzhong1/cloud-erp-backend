package com.erp.server.bi.controller.api;

import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.core.controller.BaseController;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.dto.base.BaseSearchDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.UpdateStateDTO;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.validator.UpdateGroup;
import com.common.business.vo.PagingVO;
import com.erp.model.bi.dto.*;
import com.erp.model.bi.vo.CategorySubjectVO;
import com.erp.server.bi.service.BiSubjectService;
import com.erp.server.bi.service.BiSubjectShareService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * 专题表(BiSubject)表控制层
 *
 * @author yl
 * @since 2022-12-08 14:31:58
 */
@RestController
@RequestMapping("subject")
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
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "bi:subject:paging",
            tableAlias = "bi_subject"
    )
    public ApiResult<PagingVO<SubjectPagingDTO>> queryByPage(@RequestBody @Validated PagingDTO<BaseSearchDTO> dto) {
        PagingVO<SubjectPagingDTO> pagingVO = this.biSubjectService.queryByPage(dto);
        return success(pagingVO);
    }


    /**
     * 检查能否编辑
     */
    @PostMapping("/checkToEdit")
//    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
//            tableField = "create_user_id",
//            menuCode = "bi:subject:edit",
//            tableAlias = "bi_subject"
//    )
    public void checkEditSubject(@RequestBody @Validated BaseIdDTO idDTO) {
        biSubjectService.checkEditSubject(idDTO);
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
        String id = biSubjectShareService.setShare(dto);
        if (StringUtils.isBlank(id)) {
            return failure();
        }
        return success(id);
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
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "bi:subject:updateState",
            serviceClass = BiSubjectService.class
    )
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
    public ApiResult<List<CategorySubjectVO>> homePage(@RequestBody @Validated BaseSearchDTO dto) {
        List<CategorySubjectVO> list = this.biSubjectService.homePage(dto.getSearchKeyword());
        return success(list);
    }


    /**
     * 复制专题
     */
    @PostMapping("/copy")
    public ApiResult copy(@RequestBody @Validated CopySubjectDTO dto) {
        String copySubjectId = biSubjectService.copy(dto);
        if (StringUtils.isBlank(copySubjectId)) {
            return failure();
        }
        return success(copySubjectId);
    }


    /**
     * 专题列表
     */
    @PostMapping("/list")
    public ApiResult<List<CategorySubjectDTO>> list(@RequestBody @Validated BaseSearchDTO dto) {
        List<CategorySubjectDTO> list = biSubjectService.categoryList(dto.getSearchKeyword());
        return success(list);
    }


}

