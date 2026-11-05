package com.emarketplace.order;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
interface OrderRepository extends JpaRepository<PurchaseOrder,String>{List<PurchaseOrder> findByUsernameOrderByOrderedAtDesc(String username);}
