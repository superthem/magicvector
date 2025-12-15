package cn.magicvector.common.rest.swagger;

import cn.magicvector.common.rest.annotation.SwaggerModule;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.OperationContext;
import springfox.documentation.swagger.common.SwaggerPluginSupport;
import springfox.documentation.spi.service.OperationBuilderPlugin;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toSet;

@Component
@Order(SwaggerPluginSupport.SWAGGER_PLUGIN_ORDER + 1000)
public class SwaggerOperationTagsReaderExt implements OperationBuilderPlugin {

    @Override
    public void apply(OperationContext context) {
        // 设置操作的标签
        context.operationBuilder().tags(
                controllerTags(context).stream().collect(toSet()));
    }

    private Set<String> controllerTags(OperationContext context) {
        // 查找控制器的 SwaggerModule 注解
        Optional<SwaggerModule> controllerAnnotation = Optional.ofNullable(
                context.findControllerAnnotation(SwaggerModule.class).orElse(null)
        );
        // 提取标签
        return controllerAnnotation
                .map(tagsFromController())
                .orElse(new HashSet<>());
    }

    private Function<SwaggerModule, Set<String>> tagsFromController() {
        // 获取标签并进行排序
        return input -> Stream.of(input.name())  // 从 SwaggerModule 中获取标签名称
                .filter(tag -> !tag.isEmpty())  // 过滤掉空的标签
                .collect(toCollection(TreeSet::new)); // 使用 TreeSet 保持排序
    }

    @Override
    public boolean supports(DocumentationType documentationType) {
        return true;
    }
}