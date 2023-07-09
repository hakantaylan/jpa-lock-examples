package com.enzoracca.shedlock.config;

import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.jdbctemplate.JdbcTemplateLockProvider;
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;


@Configuration
@EnableSchedulerLock(defaultLockAtMostFor = "1m")
public class ShedLockConfig {

    //Specificando usingDbTime () (introdotto in 4.9.3) il provider di blocco utilizzerà l'ora UTC in base all'ora del server DB.
    // Se non si specifica questa opzione, verrà utilizzata l'ora corrente sul client (l'ora può differire tra i client).
    //If you need to specify a schema, you can set it in the table name using the usual dot notation
    // new JdbcTemplateLockProvider(datasource, "my_schema.shedlock")
    @Bean
    public LockProvider lockProvider(DataSource dataSource) {
        return new JdbcTemplateLockProvider(
                JdbcTemplateLockProvider.Configuration.builder()
                        .withJdbcTemplate(new JdbcTemplate(dataSource))
                        .withTableName("SHEDLOCK") //for mysql linux case-sensitive
                        .usingDbTime() // Works on Postgres, MySQL, MariaDb, MS SQL, Oracle, DB2, HSQL and H2
                        .build()
        );
    }
}
