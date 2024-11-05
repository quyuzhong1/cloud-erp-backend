package com.erp.server.file.core;

import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Slf4j
public abstract class AbstractPageFileEventHandler<T, P> extends AbstractFileEventHandler<T> {

    /**
     * 顺序获取需要下载的数据
     *
     * @param p 参数
     * @return T
     */
    @SuppressWarnings("unchecked")
    public List<T> listSeqData(P p) {
        PagingDTO<P> dto = new PagingDTO<>();
        dto.setPageSize(getPageSize());
        dto.setCurrPage(1);
        List<T> dataList = new ArrayList<>();
        boolean hasNext = true;
        int totalCount = 0;
        while (hasNext) {
            dto.setParams(p);
            PagingVO<T> data = getPageData(dto);
            if (!CollectionUtils.isEmpty(data.getList())) {
                dataList.addAll((Collection<? extends T>) data.getList());
            }
            if (totalCount == 0) {
                totalCount = data.getTotalCount();
            }
            if (totalCount <= dto.getCurrPage() * getPageSize()) {
                hasNext = false;
            }
            dto.setCurrPage(dto.getCurrPage() + 1);
        }
        return dataList;
    }

    /**
     * 分页大小，可重写
     */
    protected int getPageSize() {
        return 5000;
    }
    /**
     * 分批获取数据
     *
     * @param dto 分页参数
     * @return T 对应需下载的数据
     */
    protected abstract PagingVO<T> getPageData(PagingDTO<P> dto);
}
