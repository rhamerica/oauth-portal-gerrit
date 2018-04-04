package com.rhamerica.oauth.gerrit;

import com.google.gerrit.extensions.annotations.Exports;
import com.google.gerrit.extensions.auth.oauth.OAuthServiceProvider;
import com.google.gerrit.httpd.plugins.HttpPluginModule;

class HttpModule extends HttpPluginModule {

    @Override
    protected void configureServlets() {
        bind(OAuthServiceProvider.class)
                //.annotatedWith(Exports.named(RhaOAuthService.CONFIG_SUFFIX))
                .annotatedWith(Exports.named(Constants.PLUGIN_NAME))
                .to(RhaOAuthService.class);
    }
}

