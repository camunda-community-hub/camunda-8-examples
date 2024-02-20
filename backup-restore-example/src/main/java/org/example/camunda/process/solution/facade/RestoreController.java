package org.example.camunda.process.solution.facade;

import lombok.extern.slf4j.Slf4j;
import org.example.camunda.process.solution.model.BackupRestoreRequest;
import org.example.camunda.process.solution.service.RestoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/restore")
public class RestoreController {

    @Autowired
    private RestoreService restoreService;


    @PostMapping("/restore")
    public void restore(@RequestBody BackupRestoreRequest restoreRequest) throws InterruptedException {
        log.info("starting restore: {}", restoreRequest);
        restoreService.restore(restoreRequest);
        log.info("restore backup {}", restoreRequest.getBackupId());
    }
}
