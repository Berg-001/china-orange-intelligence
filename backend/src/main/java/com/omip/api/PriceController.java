package com.omip.api;

import com.omip.domain.*;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.*;

@RestController @RequestMapping("/prices")
public class PriceController {
 private final MarketPriceRepository repository;
 public PriceController(MarketPriceRepository repository) { this.repository=repository; }
 @GetMapping("/latest") public List<PriceResponse> latest() { return repository.latest().stream().map(PriceResponse::from).toList(); }
 @GetMapping({"/history", "/china", "/orange", "/fcoj", "/nfc"})
 public Page<PriceResponse> history(@RequestParam(required=false) String country, @RequestParam(required=false) Product product,
  @RequestParam(required=false) Category category, @RequestParam(required=false) String market,
  @RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate from,
  @RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate to,
  @RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="100") int size,
  jakarta.servlet.http.HttpServletRequest request) {
   String path=request.getRequestURI();
   if(country != null && !"CN".equalsIgnoreCase(country)) throw new IllegalArgumentException("Only the Chinese market is supported");
   country="CN";
   if(path.endsWith("/fcoj")) product=Product.FCOJ; if(path.endsWith("/nfc")) product=Product.NFC;
   final boolean orangeOnly=path.endsWith("/orange");
   final String c=country, m=market; final Product pr=product; final Category cat=category; final LocalDate f=from,t=to;
   Specification<MarketPrice> spec=(root,q,cb)->{ List<Predicate> ps=new ArrayList<>();
    if(c!=null) ps.add(cb.equal(root.get("country"),c.toUpperCase())); if(pr!=null) ps.add(cb.equal(root.get("product"),pr));
    if(orangeOnly) ps.add(root.get("product").in(Product.FRESH_ORANGE,Product.INDUSTRIAL_ORANGE));
    if(cat!=null) ps.add(cb.equal(root.get("category"),cat)); if(m!=null) ps.add(cb.like(cb.lower(root.get("market")),"%"+m.toLowerCase()+"%"));
    if(f!=null) ps.add(cb.greaterThanOrEqualTo(root.get("referenceDate"),f)); if(t!=null) ps.add(cb.lessThanOrEqualTo(root.get("referenceDate"),t)); return cb.and(ps.toArray(Predicate[]::new));};
   return repository.findAll(spec,PageRequest.of(page,Math.min(size,500),Sort.by(Sort.Direction.DESC,"referenceDate"))).map(PriceResponse::from);
 }
}
