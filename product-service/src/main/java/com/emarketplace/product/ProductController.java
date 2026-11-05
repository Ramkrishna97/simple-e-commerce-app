package com.emarketplace.product;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import java.math.BigDecimal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
@RestController @RequestMapping("/api") @CrossOrigin(origins = {"http://localhost:4200", "https://simple-marketplace-e-com.netlify.app"})
public class ProductController {
    private final ProductRepository products; private final SellerRepository sellers; private final String internalApiKey; private final long storageLimit;
    public ProductController(ProductRepository products,SellerRepository sellers,@Value("${app.internal-api-key}") String internalApiKey,@Value("${app.storage-limit-bytes:536870912}") long storageLimit){this.products=products;this.sellers=sellers;this.internalApiKey=internalApiKey;this.storageLimit=storageLimit;}
    @PostMapping("/sellers") @ResponseStatus(HttpStatus.CREATED) public Seller seller(@RequestBody SellerRequest r){return sellers.save(new Seller(r.name(),r.username(),r.email(),r.mobileNumber()));}
    @GetMapping("/sellers") public List<Seller> sellers(Authentication auth){requireAdmin(auth);return sellers.findAll();}
    @GetMapping("/sellers/admin/count") public CountResponse sellerCount(Authentication auth){requireAdmin(auth);return new CountResponse(sellers.count());}
    @GetMapping("/sellers/{username}") public Seller seller(@PathVariable String username,Authentication auth){requireSellerOrAdmin(username,auth);return findSeller(username);}
    @PutMapping("/sellers/{username}") public Seller updateSeller(@PathVariable String username,@RequestBody SellerRequest r,Authentication auth){requireSellerOrAdmin(username,auth);Seller seller=findSeller(username);seller.update(r.name(),r.email(),r.mobileNumber());return sellers.save(seller);}
    @GetMapping("/products") public Page<Product> products(@RequestParam(defaultValue="0") int page){return products.findAll(PageRequest.of(Math.max(page, 0), 20));}
    @GetMapping("/products/{id}") public Product product(@PathVariable String id){return products.findById(id).orElseThrow(() -> new NotFoundException("Product not found"));}
    @GetMapping("/products/admin/all") public List<Product> allProducts(Authentication auth){requireAdmin(auth);return products.findAll();}
    @PostMapping("/sellers/{username}/products") @ResponseStatus(HttpStatus.CREATED) public Product add(@PathVariable String username,@RequestBody ProductRequest request,Authentication auth){requireSellerOrAdmin(username,auth);Seller seller=findSeller(username); Product product=products.save(new Product(request,username)); seller.addProduct(product.getId()); sellers.save(seller); return product;}
    @PutMapping("/sellers/{username}/products/{id}") public Product update(@PathVariable String username,@PathVariable String id,@RequestBody ProductRequest request,Authentication auth){requireSellerOrAdmin(username,auth);Product product=product(id);if(!product.getSellerUsername().equals(username))throw new NotFoundException("Product does not belong to seller");product.apply(request);return products.save(product);}
    @PostMapping(value="/sellers/{username}/products/{id}/image",consumes=MediaType.MULTIPART_FORM_DATA_VALUE) public Product uploadProductImage(@PathVariable String username,@PathVariable String id,@RequestParam("file") MultipartFile file,Authentication auth)throws IOException{requireSellerOrAdmin(username,auth);Product product=product(id);if(!product.getSellerUsername().equals(username)&&!isAdmin(auth))throw new NotFoundException("Product does not belong to seller");if(file.isEmpty())throw new IllegalArgumentException("Choose an image to upload");if(file.getContentType()==null||!file.getContentType().startsWith("image/"))throw new IllegalArgumentException("Only image files are supported");product.setProductImage(ImageCompressionService.compress(file.getBytes()));return products.save(product);}
    @GetMapping("/sellers/{username}/products") public List<Product> sellerProducts(@PathVariable String username,Authentication auth){requireSellerOrAdmin(username,auth);return products.findBySellerUsername(username);}
    @DeleteMapping("/products/{id}") @Transactional public void deleteProduct(@PathVariable String id,Authentication auth){requireAdmin(auth);Product product=product(id);sellers.findByUsername(product.getSellerUsername()).ifPresent(s->{s.removeProduct(id);sellers.save(s);});products.delete(product);}
    @PutMapping("/products/admin/category-discount") @Transactional public CountResponse categoryDiscount(@RequestBody CategoryDiscountRequest request,Authentication auth){requireAdmin(auth);List<Product> matching=products.findByCategoryIgnoreCase(request.category());matching.forEach(p->p.applyDiscount(request.discount()));products.saveAll(matching);return new CountResponse(matching.size());}
    @GetMapping("/products/admin/storage") public StorageResponse storage(Authentication auth)throws IOException{requireAdmin(auth);Path data=Path.of("data").toAbsolutePath();long used=0; if(Files.exists(data)){try(var files=Files.walk(data)){used=files.filter(Files::isRegularFile).mapToLong(p->{try{return Files.size(p);}catch(IOException e){return 0L;}}).sum();}}return new StorageResponse(used,storageLimit,Math.max(0,storageLimit-used),storageLimit==0?0:(used*100d/storageLimit));}
    @PostMapping("/internal/products/reserve") @Transactional public synchronized List<ReservedProduct> reserve(@RequestHeader("X-Internal-Api-Key") String key,@RequestBody StockRequest request){requireInternalKey(key);List<Product> requested=request.items().stream().map(item->product(item.productId())).toList();for(int i=0;i<requested.size();i++)requested.get(i).reserve(request.items().get(i).quantity());return requested.stream().map(product->new ReservedProduct(product.getId(),product.getName(),request.items().stream().filter(item->item.productId().equals(product.getId())).findFirst().orElseThrow().quantity(),product.getDiscountedPrice(),product.getAvailableQuantity())).toList();}
    @PostMapping("/internal/products/release") @Transactional public void release(@RequestHeader("X-Internal-Api-Key") String key,@RequestBody StockRequest request){requireInternalKey(key);request.items().forEach(item->product(item.productId()).release(item.quantity()));}
    private void requireInternalKey(String key){if(!internalApiKey.equals(key))throw new org.springframework.web.server.ResponseStatusException(HttpStatus.FORBIDDEN,"Invalid internal API key");}
    private Seller findSeller(String username){return sellers.findByUsername(username).orElseThrow(() -> new NotFoundException("Seller not found"));}
    private void requireSellerOrAdmin(String username,Authentication auth){if(auth==null||(!username.equals(auth.getName())&&!isAdmin(auth)))throw new org.springframework.web.server.ResponseStatusException(HttpStatus.FORBIDDEN);}
    private void requireAdmin(Authentication auth){if(auth==null||!isAdmin(auth))throw new org.springframework.web.server.ResponseStatusException(HttpStatus.FORBIDDEN);}
    private boolean isAdmin(Authentication auth){return auth.getAuthorities().stream().anyMatch(a->a.getAuthority().equals("ROLE_ADMIN"));}
    public record SellerRequest(String name,String username,String email,String mobileNumber){}
    public record CountResponse(long count){}
    public record CategoryDiscountRequest(String category,BigDecimal discount){} public record StorageResponse(long usedBytes,long totalBytes,long remainingBytes,double usedPercent){}
    public record StockItem(String productId,int quantity){} public record StockRequest(List<StockItem> items){} public record ReservedProduct(String productId,String name,int quantity,BigDecimal unitPrice,int availableQuantity){}
    @ExceptionHandler(IllegalArgumentException.class) @ResponseStatus(HttpStatus.CONFLICT) public String stockConflict(IllegalArgumentException exception){return exception.getMessage();}
    @ResponseStatus(HttpStatus.NOT_FOUND) static class NotFoundException extends RuntimeException { NotFoundException(String message){super(message);} }
}
