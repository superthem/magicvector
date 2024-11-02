package com.magicvector.common.service.util;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.magicvector.common.service.model.PageQuery;
import com.magicvector.common.service.model.PageResult;

public class PageUtil {

    public static <T> IPage<T> getPage(PageQuery query) {
        Page<T> page = new Page(query.getCurrent(), query.getSize());
        return page;
    }

    public static <T, R> PageResult<R> getPageResult(IPage<T> page) {
        PageResult<R> pageResult = new PageResult<>();
        pageResult.setPages(page.getPages());
        pageResult.setCurrent(page.getCurrent());
        pageResult.setTotal(page.getTotal());
        pageResult.setSize(page.getSize());
        return pageResult;
    }
}