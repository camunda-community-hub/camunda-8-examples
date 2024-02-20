package org.example.camunda.process.solution.facade;

import lombok.extern.slf4j.Slf4j;
import org.example.camunda.process.solution.service.DeletionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/delete")
public class DeletionController {


    @Autowired
    private DeletionService deletionService;

    @PostMapping("/indices")
    public void deleteIndices() {
        log.info("start deleting indices");
        deletionService.deleteIndices();
        log.info("finished deleting indices");
    }
}
