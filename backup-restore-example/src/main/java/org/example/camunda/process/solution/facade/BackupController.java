package org.example.camunda.process.solution.facade;

import lombok.extern.slf4j.Slf4j;
import org.example.camunda.process.solution.model.BackupRestoreRequest;
import org.example.camunda.process.solution.service.BackupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/backup")
public class BackupController {


    @Autowired
    private BackupService backupService;

    @PostMapping("/prepare-backup")
    public void backup() {
        log.info("start preparing backup");
        backupService.prepareBackup();
        log.info("finished preparing backup");
    }

    @PostMapping("/backup")
    public void backup(@RequestBody BackupRestoreRequest backupRequest) throws InterruptedException {
        log.info("starting backup {}", backupRequest);
        backupService.backup(backupRequest);
        log.info("finished backup {}", backupRequest.getBackupId());
    }
}
