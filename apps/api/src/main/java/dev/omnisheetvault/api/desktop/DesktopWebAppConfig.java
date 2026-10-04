package dev.omnisheetvault.api.desktop;

import java.io.IOException;
import java.time.Duration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

/**
 * The desktop edition serves the web app itself, from the bundled build, on the same origin as the API. Vite's
 * hashed assets are cached for good; any other path the router owns gets {@code index.html}; an unknown API path
 * stays a 404 instead of turning into the page.
 */
@Configuration(proxyBeanMethods = false)
@Profile("desktop")
class DesktopWebAppConfig implements WebMvcConfigurer {

    private static final String INDEX = "index.html";

    private final DesktopProperties properties;

    DesktopWebAppConfig(DesktopProperties properties) {
        this.properties = properties;
    }

    /** The resource handler never sees the empty path of {@code /}, so the root is forwarded to the page explicitly. */
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("forward:/" + INDEX);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String webApp = properties.webPath().toAbsolutePath().toUri().toString();
        String location = webApp.endsWith("/") ? webApp : webApp + "/";
        registry.addResourceHandler("/assets/**")
                .addResourceLocations(location + "assets/")
                .setCacheControl(CacheControl.maxAge(Duration.ofDays(365)).cachePublic());
        registry.addResourceHandler("/**")
                .addResourceLocations(location)
                .setCacheControl(CacheControl.noCache())
                .resourceChain(false)
                .addResolver(new SinglePageAppResolver());
    }

    /** A file when there is one, otherwise the app's page, so a reload on {@code /characters/…} still opens the app. */
    private static final class SinglePageAppResolver extends PathResourceResolver {

        @Override
        protected Resource getResource(String resourcePath, Resource location) throws IOException {
            Resource requested = super.getResource(resourcePath, location);
            if (requested != null || resourcePath.startsWith("api/")) {
                return requested;
            }
            return super.getResource(INDEX, location);
        }
    }
}
