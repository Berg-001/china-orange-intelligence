package com.omip.api;
import com.omip.service.CollectionService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
@RestController @RequestMapping("/admin") @ConditionalOnProperty(name="omip.admin.enabled",havingValue="true")
public class CollectionController { private final CollectionService service; public CollectionController(CollectionService s){service=s;} @PostMapping("/collect") public Map<String,Object> collect(){return service.collectDaily();} }
