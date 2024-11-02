package com.magicvector.common.rest.swagger;

import com.magicvector.common.rest.util.LanguageUtil;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import springfox.documentation.spring.web.DocumentationCache;
import springfox.documentation.spring.web.plugins.DocumentationPluginsManager;
import springfox.documentation.swagger.web.InMemorySwaggerResourcesProvider;
import springfox.documentation.swagger.web.SwaggerResource;

import java.util.Comparator;
import java.util.List;

@Component
public class InMemorySwaggerResourcesProviderExt extends InMemorySwaggerResourcesProvider {

	// 修改构造函数，只保留 Environment 和 DocumentationCache
	public InMemorySwaggerResourcesProviderExt(Environment environment, DocumentationCache documentationCache, DocumentationPluginsManager documentationPluginsManager) {
		super(environment, documentationCache, documentationPluginsManager);
	}

	@Override
	public List<SwaggerResource> get() {
		List<SwaggerResource> resources = super.get();
		resources.sort(new Comparator<SwaggerResource>() {
			@Override
			public int compare(SwaggerResource o1, SwaggerResource o2) {
				// 使用 LanguageUtil 工具类将名称转换为拼音进行排序
				String p1 = LanguageUtil.toPinyin(o1.getName());
				String p2 = LanguageUtil.toPinyin(o2.getName());
				return p1.compareTo(p2);
			}
		});
		return resources;
	}
}