package com.magicvector.common.rpc.filter;


import com.github.tbwork.anole.loader.util.JSON;
import com.magicvector.common.basic.context.GlobalContext;
import com.magicvector.common.basic.model.ContextParam;
import com.magicvector.common.basic.util.Base6462Util;
import com.magicvector.common.basic.util.S;
import com.magicvector.common.rpc.statics.StaticValue;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@Slf4j
public class FeignServerFilter implements Filter {


    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        String globalContextString = request.getHeader(StaticValue.GLOBAL_CONTEXT_HEADER);
        if(S.isNotEmpty(globalContextString)){
            try{
                globalContextString = Base6462Util.decodeBase64(globalContextString);
                GlobalContext.setContext(JSON.parseObject(globalContextString, ContextParam.class));
            }
            catch (Exception e){
                log.warn("An error occurred while converting global context.");
            }
        }
        filterChain.doFilter(servletRequest, servletResponse);
    }

    @Override
    public void destroy() {

    }

}