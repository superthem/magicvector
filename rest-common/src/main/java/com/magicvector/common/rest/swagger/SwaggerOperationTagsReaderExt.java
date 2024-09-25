package com.magicvector.common.rest.swagger;

import com.magicvector.common.rest.annotation.SwaggerModule;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import springfox.documentation.spi.service.contexts.OperationContext;
import springfox.documentation.spring.web.readers.operation.DefaultTagsProvider;
import springfox.documentation.swagger.common.SwaggerPluginSupport;
import springfox.documentation.swagger.readers.operation.SwaggerOperationTagsReader;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toSet;

@Component
@Order(SwaggerPluginSupport.SWAGGER_PLUGIN_ORDER + 1000)
public class SwaggerOperationTagsReaderExt extends SwaggerOperationTagsReader {

    // 构造函数：需要 DefaultTagsProvider
    public SwaggerOperationTagsReaderExt(DefaultTagsProvider defaultTagsProvider) {
        super(defaultTagsProvider); // 使用 DefaultTagsProvider 初始化父类
    }

    @Override
    public void apply(OperationContext context) {
        context.operationBuilder().tags(
                controllerTags(context).stream().collect(toSet()));
    }

    private Set<String> controllerTags(OperationContext context) {
        java.util.Optional<SwaggerModule> controllerAnnotation = java.util.Optional.ofNullable(
                context.findControllerAnnotation(SwaggerModule.class).orNull()
        );

        return controllerAnnotation
                .map(tagsFromController())
                .orElse(new HashSet<String>());
    }

    private Function<SwaggerModule, Set<String>> tagsFromController() {
        return input -> Stream.of(input.name())  // 从 SwaggerModule 中获取标签名称
                .filter(tag -> !tag.isEmpty())  // 过滤掉空的标签
                .collect(toCollection(TreeSet::new)); // 使用 TreeSet 保持排序
    }
}