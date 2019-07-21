package com.pinyougou.manager.config;

import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

/**
 * @作者:pengge
 * @时间:2019/06/25 21:51
 */
@EnableWebSecurity
public class ManagerSecurityConfig extends WebSecurityConfigurerAdapter {


    @Override
    protected void configure(HttpSecurity http) throws Exception {

        http.authorizeRequests()
                //公开资源
                .antMatchers("/css/**","/img/**","/js/**","/plugins/**","/login.html").permitAll()
                //拦截 路径
                //暂时设置所有的其他请求都需要认证通过即可 也就是用户名和密码正确即可不需要其他的角色
                .anyRequest().authenticated();
        //配置登录信息
        http.formLogin()
                .loginPage("/login.html")
                .loginProcessingUrl("/login")
                //默认成功页，总是去成功页面
                .defaultSuccessUrl("/admin/index.html",true)
                //错误到达它的错误页
                .failureUrl("/login?error");

        //退出登录设置
        http.logout().logoutUrl("/logout").invalidateHttpSession(true);


        http.csrf().disable();//关闭CSRF

        //开启同源iframe 可以访问策略
        //也就是当页面嵌套是页面是，是携带路径访问，系统不给通过，加上这个代码就可以
        http.headers().frameOptions().sameOrigin();

    }
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        //会自动添加ROLE_
        auth.inMemoryAuthentication().withUser("jack").password("{noop}123").roles("ADMIN");
    }

}
