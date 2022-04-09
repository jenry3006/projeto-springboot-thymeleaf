package jenry.springboot.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class WebConfigSecurity extends WebSecurityConfigurerAdapter {

    @Autowired
    private ImplementacaoUserDetailService implementacaoUserDetailService;

    @Override //Configura as solicitações de acesso por http
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable() //Disabilita as configurações do padrão de memória do Spring
        .authorizeRequests()//Permitir restringir os acessos
        .antMatchers(HttpMethod.GET, "/").permitAll() //Qualquer usuário tem acesso no "/"
        .antMatchers(HttpMethod.GET, "/cadastroPessoa").hasAnyRole("ADMIN") //Qualquer usuário tem acesso no "/"
        .anyRequest().authenticated().and().formLogin().permitAll()//permite qualquer usuário
                .loginPage("/login").defaultSuccessUrl("/cadastroPessoa").failureUrl("/login?error=true")

        .and().logout().logoutSuccessUrl("/login") //mapeia URL de logout e invalida usuario autenticado
        .logoutRequestMatcher(new AntPathRequestMatcher("/logout")); //url para voltar ao login
    }

    @Override //cria autenticação do usuário com bd ou em memória
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(implementacaoUserDetailService)
        .passwordEncoder(new BCryptPasswordEncoder());

        /*auth.inMemoryAuthentication().passwordEncoder(new BCryptPasswordEncoder())
                .withUser("jenry")
                .password("$2a$10$XSp42bNqvfphlq5o67fdZejAvy6VgjWIrybkFCMrVX9Q0mtx7Mk4m")
                .roles("ADMIN");*/
    }

   /* @Override //Ignorar url especificas
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers("")
    }*/
}
