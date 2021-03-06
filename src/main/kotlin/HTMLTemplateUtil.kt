package top.ntutn

import org.thymeleaf.TemplateEngine
import org.thymeleaf.context.Context
import org.thymeleaf.templateresolver.FileTemplateResolver
import kotlin.collections.HashMap

object HTMLTemplateUtil {
    val templateEngine = TemplateEngine();

    /**
     * 初始化模板引擎
     * @param templatePath 模板所在的路径
     */
    fun initEngine(templatePath: String) {
        val resolver = FileTemplateResolver()
        resolver.prefix = "$templatePath/"
        resolver.suffix = ".html";
        resolver.setTemplateMode("XML")
        resolver.order = templateEngine.templateResolvers.size
        resolver.isCacheable = false
        resolver.characterEncoding = "UTF-8"
        templateEngine.addTemplateResolver(resolver)
    }

    /**
     * 进行渲染
     * @param template 模板名
     * @param params 参数
     */
    fun render(template: String, params: Map<String, Any>): String {
        val context = Context()
        context.setVariables(params)
        return templateEngine.process(template, context)
    }
}