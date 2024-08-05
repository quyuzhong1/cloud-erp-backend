package com.erp.server.file.core;

import com.common.business.dto.base.PagingDTO;
import com.erp.server.file.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Slf4j
public abstract class AbstractPageFileEventHandler<T, P> extends AbstractFileEventHandler<T> {

    @Resource(name = "fileExecutor")
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;
    // 以500条数据进行拆分
    private static final int LIMIT = 500;

    /**
     * 顺序获取需要下载的数据
     *
     * @param p 参数
     * @return T
     */
    public List<T> listSeqData(P p) {

        int count = count(p);
        if (count > getNumberLimit()) {
            throw new BusinessException(String.format("获取的数据量大于条数%s限制,请增加筛选条件导出", getNumberLimit()));
        }
        PagingDTO<P> dto = new PagingDTO<>();
        dto.setPageSize(LIMIT);
        // 获取页数
        int page = count / LIMIT + 1;
        List<T> dataList = new ArrayList<>();
        for (int i = 1; i <= page; i++) {
            dto.setCurrPage(i);
            dto.setParams(p);
            List<T> data = getPageData(dto);
            dataList.addAll(data);
        }
        return dataList;
    }

    /**
     * 异步获取需要下载的数据，排序会乱
     *
     * @param p 下载任务
     * @return T
     */
    public List<T> listAsyncData(P p) {
        int count = count(p);
        if (count > getNumberLimit()) {
            throw new BusinessException(String.format("获取的数据量大于条数%s限制,请增加筛选条件导出", getNumberLimit()));
        }
        List<T> dataList = new ArrayList<>();
        List<CompletableFuture<List<T>>> futures = new ArrayList<>();
        PagingDTO<P> dto = new PagingDTO<>();
        dto.setPageSize(LIMIT);
        // 获取页数
        int page = count / LIMIT + 1;
        for (int i = 0; i < page; i++) {
            dto.setCurrPage(i);
            dto.setParams(p);
            CompletableFuture<List<T>> future = CompletableFuture.supplyAsync(() -> getPageData(dto), threadPoolTaskExecutor);
            futures.add(future);
        }
        // 所有future一起执行
        CompletableFuture<Void> allFuture = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        // 获取执行完的future结果
        allFuture.thenRun(() -> {
            for (CompletableFuture<List<T>> future : futures) {
                try {
                    List<T> data = future.get();
                    dataList.addAll(data);
                } catch (InterruptedException | ExecutionException e) {
                    log.error("执行失败，请重试");
                    Thread.currentThread().interrupt();
                }
            }
        });
        return dataList;
    }


    /**
     * 获取需要下载的数据条数
     *
     * @param p 前端传入的请求参数
     * @return 条数
     */
    protected abstract int count(P p);


    /**
     * 分批获取数据
     *
     * @param dto 分页参数
     * @return T 对应需下载的数据
     */
    protected abstract List<T> getPageData(PagingDTO<P> dto);

    /**
     * 条数限制
     * 默认200000
     *
     * @return 条数
     */
    public int getNumberLimit() {
        return 200000;
    }
}
