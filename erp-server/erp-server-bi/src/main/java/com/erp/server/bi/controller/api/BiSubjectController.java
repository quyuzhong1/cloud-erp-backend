package com.erp.server.bi.controller.api;

import cn.hutool.core.util.ObjectUtil;
import com.common.business.dto.base.*;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.anno.StateEnumValue;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.core.controller.BaseController;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.validator.UpdateGroup;
import com.common.business.vo.PagingVO;
import com.common.core.enums.LogActionEnum;
import com.erp.model.bi.dto.*;
import com.erp.model.bi.entity.BiSubjectEntity;
import com.erp.model.bi.vo.CategorySubjectVO;
import com.erp.model.oms.dto.SoChangeDTO;
import com.erp.server.bi.service.BiSubjectService;
import com.erp.server.bi.service.BiSubjectShareService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

/**
 * 专题管理
 *
 * @author yl
 * @since 2022-12-08 14:31:58
 */
@Slf4j
@RestController
@LogSystemModule("专题管理")
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
    @LogAction(value = LogActionEnum.INSERT, desc = "新增专题")
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
    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "编辑数据:id={id}, 专题名={name}")
    @PostMapping("/update")
    public ApiResult<String> edit(@RequestBody @Validated(value = {UpdateGroup.class}) SubjectDTO biSubject) {
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
    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "设置仪表盘的分享:id={id},名称={name}")
    @PostMapping("/setShare")
    public ApiResult<String> setShare(@RequestBody @Validated UpdateSubjectShareDTO dto) {
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
    @LogAction(value = LogActionEnum.DELETE, desc = "删除专题")
    @PostMapping("/delete")
    public ApiResult<Object> deleteById(@RequestBody @Validated BaseIdDTO dto) {
        boolean flag = this.biSubjectService.deleteById(dto.getId());
        return flag ? success() : failure();
    }

    /**
     * 设置专题状态
     *
     * @return 删除是否成功
     */
    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "设置专题状态:id={id},状态值={state}(true=禁用,false=启用)")
    @PostMapping("/updateState")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "bi:subject:updateState",
            serviceClass = BiSubjectService.class
    )
    public ApiResult<Object> updateState(@RequestBody @Validated UpdateStateDTO dto) {
        boolean flag = this.biSubjectService.updateState(dto);
        return flag ? success() : failure();
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
    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "复制专题:专题id={subjectId}")
    @PostMapping("/copy")
    public ApiResult<String> copy(@RequestBody @Validated CopySubjectDTO dto) {
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

    /**
     * 专题批量设置权限
     * @author Jim
     * @date:  2023-09-14
     * @param dtoList
     * @return ApiResult<List<BatchResultDTO>>
     */
    @LogAction(value = LogActionEnum.CUSTOM_BATCH_UPDATE, desc = "专题批量设置权限:id={id}")
    @PostMapping("/batchShare")
    public ApiResult<List<BatchResultDTO>> batchShare(@RequestBody @Validated List<BiBatchShareDTO> dtoList) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dtoList.size());
        for (BiBatchShareDTO dto : dtoList) {
            BatchResultDTO submit;
            try {
                submit = biSubjectService.updateShare(dto.checkAndGetShareFlagIdList(), dto.getId(), dto.getShareFlag());
            }catch (Exception e){
                log.error("专题批量设置权限失败:{}", e.getMessage());
                BiSubjectEntity entity = biSubjectService.getById(dto.getId());
                if (ObjectUtil.isEmpty(entity)) {
                    submit = BatchResultDTO.fail(dto.getId(), dto.getId(), "专题不存在, 提交失败");
                    resultDTOS.add(submit);
                    continue;
                }
                submit = BatchResultDTO.fail(entity.getId(), "", e.getMessage());
            }
            resultDTOS.add(submit);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

}

