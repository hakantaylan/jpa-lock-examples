package com.enzoracca.shedlock.runner;

import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;


@Component
public class BatchRunner {

    private Job job;

    private JobLauncher jobLauncher;

    public BatchRunner(Job job, JobLauncher jobLauncher) {
        this.job = job;
        this.jobLauncher = jobLauncher;
    }


// lockAtMostFor: You can also set the lockAtMostFor attribute which specifies how long the lock should be held in case the running node dies.
// This is just a fallback, under normal circumstances the lock is released as soon as the activities finish.
// You need to set lockAtMostFor to a value which is much longer than the normal execution time.
// If the task takes longer than lockAtMostFor, the resulting behavior may be unpredictable (more than one process will actually hold the lock).
// If you do not specify lockAtMostFor in @SchedulerLock default value from @EnableSchedulerLock will be used
// EXAMPLE: @Scheduled(cron = "0 */15 * * * *")
// @SchedulerLock(name = "scheduledTaskName", lockAtMostFor = "14m", lockAtLeastFor = "14m")
// public void scheduledTask() {
// // do something
    @Scheduled(cron = "0 */2 * * * *")
    @SchedulerLock(name = "TaskScheduler_scheduledTask",
            lockAtLeastFor = "1m", lockAtMostFor = "1m")
    public void run() throws Exception {
        jobLauncher.run(job, new JobParametersBuilder().addDate("date", new Date()).toJobParameters());
    }
}
