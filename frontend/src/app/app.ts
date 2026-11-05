import { CommonModule, CurrencyPipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';

interface Product { id:string; name:string; description:string; category:string; images:string[]; reviews:string[]; discount:number; mrpPrice:number; discountedPrice:number; availableQuantity:number; sellerUsername:string; }
interface ProductPage { content:Product[]; number:number; totalPages:number; }
interface Registration { name:string; username:string; email:string; mobileNumber:string; password:string; role:'USER'|'SELLER'; }
interface Address { line1:string; line2:string; city:string; state:string; postalCode:string; country:string; }
interface Profile { name:string; username:string; email:string; mobileNumber:string; profileImageUrl?:string; addresses?:Address[]; listedProductIds?:string[]; orderedProductIds?:string[]; }
interface CartItem { productId:string; quantity:number; } interface CartResponse { username:string; items:CartItem[]; }
interface OrderItem { productId:string; productName:string; quantity:number; unitPrice:number; } interface Order { id:string; username:string; orderedAt:string; status:string; totalAmount:number; items:OrderItem[]; }
interface Notification { id:string; senderUsername:string; recipientUsername:string; recipientRole:string; message:string; createdAt:string; readMessage:boolean; }

@Component({selector:'app-root',imports:[CommonModule,CurrencyPipe,FormsModule],templateUrl:'./app.html',styleUrl:'./app.css'})
export class App implements OnInit {
  products:Product[]=[]; page=0; totalPages=0; view:'grid'|'list'='grid'; loading=true; error='';
  serverWakingUp=false; retryCount=0;
  username=sessionStorage.getItem('username')||''; password=''; token=sessionStorage.getItem('token')||''; role=sessionStorage.getItem('role')||''; loginError='';
  showLogin=false; showRegistration=false; showCart=false; showOrders=false; showAccount=false; showNotifications=false;
  cartItems:CartItem[]=[]; orders:Order[]=[]; cartLoading=false; ordering=false; orderError=''; guestCartId=this.getOrCreateGuestCartId();
  registration:Registration={name:'',username:'',email:'',mobileNumber:'',password:'',role:'USER'};
  profile:Profile={name:'',username:'',email:'',mobileNumber:'',addresses:[]}; currentPassword=''; newPassword=''; accountMessage='';
  sellerProducts:Product[]=[]; productForm={name:'',description:'',category:'',mrpPrice:0,discountedPrice:0,discount:0,availableQuantity:1,images:[] as string[],reviews:[] as string[]};
  adminUserCount=0; adminSellerCount=0; broadcastForm={audience:'ALL',message:''}; notifications:Notification[]=[];

  private get authUrl(): string { return this.serviceUrl('8081', '/api/auth'); }
  private get userUrl(): string { return this.serviceUrl('8082', '/api/users'); }
  private get productUrl(): string { return this.serviceUrl('8083', '/api'); }
  private get cartBaseUrl(): string { return this.serviceUrl('8084', '/api/carts'); }
  private get orderUrl(): string { return this.serviceUrl('8085', '/api/orders'); }
  private get notificationUrl(): string { return this.serviceUrl('8086', '/api/notifications'); }

  private serviceUrl(port: string, path: string): string {
    if (typeof window !== 'undefined' && window.location.hostname !== 'localhost' && window.location.hostname !== '127.0.0.1') {
      return path;
    }
    return `http://localhost:${port}${path}`;
  }

  constructor(private readonly http:HttpClient,private readonly changes:ChangeDetectorRef){}
  ngOnInit():void{this.loadProducts();this.loadCart();if(this.token){this.loadOrders();this.loadNotifications();}}
  loadProducts(page=this.page):void{
    this.loading=true;
    this.http.get<ProductPage>(`${this.productUrl}/products?page=${page}`).subscribe({
      next:r=>{
        this.products=r.content;
        this.page=r.number;
        this.totalPages=r.totalPages;
        this.error='';
        this.loading=false;
        this.serverWakingUp=false;
        this.retryCount=0;
        this.changes.detectChanges();
      },
      error:()=>{
        this.retryCount++;
        this.serverWakingUp=true;
        this.loading=true;
        this.changes.detectChanges();
        setTimeout(()=>this.loadProducts(page),4000);
      }
    });
  }
  login():void{this.loginError='';this.http.post<{accessToken:string;role:string}>(`${this.authUrl}/login`,{username:this.username,password:this.password}).subscribe({next:r=>{this.token=r.accessToken;this.role=r.role;sessionStorage.setItem('username',this.username);sessionStorage.setItem('token',this.token);sessionStorage.setItem('role',this.role);this.password='';this.showLogin=false;this.mergeGuestCart();this.loadOrders();this.loadNotifications();this.changes.detectChanges();},error:()=>{this.loginError='Login failed. Check your credentials or backend connection.';this.changes.detectChanges();}});}
  createAccount():void{const r=this.registration;this.loginError='';this.http.post(`${this.authUrl}/register`,{username:r.username,email:r.email,password:r.password,role:r.role}).subscribe({next:()=>{const url=r.role==='SELLER'?`${this.productUrl}/sellers`:`${this.userUrl}`;this.http.post(url,r).subscribe({next:()=>{this.username=r.username;this.password=r.password;this.showRegistration=false;this.login();},error:()=>{this.loginError='Login was created, but the profile service is unavailable.';this.changes.detectChanges();}});},error:e=>{this.loginError=typeof e.error==='string'?e.error:'Could not create account.';this.changes.detectChanges();}});}
  openAccount():void{this.showAccount=true;this.accountMessage='';if(this.role==='ADMIN'){this.loadAdmin();return;}const url=this.role==='SELLER'?`${this.productUrl}/sellers/${encodeURIComponent(this.username)}`:`${this.userUrl}/${encodeURIComponent(this.username)}`;this.http.get<Profile>(url,{headers:this.authHeaders()}).subscribe({next:p=>{this.profile={...p,addresses:p.addresses||[]};if(this.role==='SELLER')this.loadSellerProducts();this.changes.detectChanges();},error:()=>{this.accountMessage='Profile could not be loaded.';this.changes.detectChanges();}});}
  saveProfile():void{const url=this.role==='SELLER'?`${this.productUrl}/sellers/${encodeURIComponent(this.username)}`:`${this.userUrl}/${encodeURIComponent(this.username)}`;this.http.put<Profile>(url,this.profile,{headers:this.authHeaders()}).subscribe({next:p=>{this.profile={...this.profile,...p};this.accountMessage='Profile saved.';this.changes.detectChanges();},error:()=>this.accountMessage='Profile could not be saved.'});if(this.role==='USER')this.saveAddresses();}
  saveAddresses():void{this.http.put<Profile>(`${this.userUrl}/${encodeURIComponent(this.username)}/addresses`,this.profile.addresses||[],{headers:this.authHeaders()}).subscribe();}
  addAddress():void{(this.profile.addresses||=[]).push({line1:'',line2:'',city:'',state:'',postalCode:'',country:'India'});} removeAddress(index:number):void{this.profile.addresses?.splice(index,1);this.saveAddresses();}
  clearPhoto():void{this.profile.profileImageUrl='';this.http.put(`${this.userUrl}/${encodeURIComponent(this.username)}/profile-image`,{imageUrl:''},{headers:this.authHeaders()}).subscribe(()=>{this.accountMessage='Profile photo deleted.';this.changes.detectChanges();});}
  uploadPhoto(event:Event):void{const input=event.target as HTMLInputElement;const file=input.files?.[0];if(!file)return;const data=new FormData();data.append('file',file);this.accountMessage='Uploading photo…';this.http.post<Profile>(`${this.userUrl}/${encodeURIComponent(this.username)}/profile-image/upload`,data,{headers:this.authHeaders()}).subscribe({next:p=>{this.profile.profileImageUrl=p.profileImageUrl;this.accountMessage='Profile photo uploaded.';input.value='';this.changes.detectChanges();},error:e=>{this.accountMessage=typeof e.error==='string'?e.error:'Photo upload failed.';input.value='';this.changes.detectChanges();}});}
  changePassword():void{this.http.put(`${this.authUrl}/${encodeURIComponent(this.username)}/password`,{currentPassword:this.currentPassword,newPassword:this.newPassword},{headers:this.authHeaders()}).subscribe({next:()=>{this.currentPassword='';this.newPassword='';this.accountMessage='Password changed.';this.changes.detectChanges();},error:()=>{this.accountMessage='Current password is incorrect.';this.changes.detectChanges();}});}
  loadSellerProducts():void{this.http.get<Product[]>(`${this.productUrl}/sellers/${encodeURIComponent(this.username)}/products`,{headers:this.authHeaders()}).subscribe(p=>{this.sellerProducts=p;this.changes.detectChanges();});}
  addSellerProduct():void{this.http.post<Product>(`${this.productUrl}/sellers/${encodeURIComponent(this.username)}/products`,this.productForm,{headers:this.authHeaders()}).subscribe({next:()=>{this.productForm={name:'',description:'',category:'',mrpPrice:0,discountedPrice:0,discount:0,availableQuantity:1,images:[],reviews:[]};this.accountMessage='Product listed for sale.';this.loadSellerProducts();this.loadProducts(0);},error:()=>this.accountMessage='Product could not be listed.'});}
  loadAdmin():void{this.http.get<{count:number}>(`${this.userUrl}/admin/count`,{headers:this.authHeaders()}).subscribe(v=>{this.adminUserCount=v.count;this.changes.detectChanges();});this.http.get<{count:number}>(`${this.productUrl}/sellers/admin/count`,{headers:this.authHeaders()}).subscribe(v=>{this.adminSellerCount=v.count;this.changes.detectChanges();});this.http.get<Order[]>(`${this.orderUrl}/admin/all`,{headers:this.authHeaders()}).subscribe(v=>{this.orders=v;this.changes.detectChanges();});}
  sendBroadcast():void{this.http.post(`${this.notificationUrl}/broadcast`,this.broadcastForm,{headers:this.authHeaders()}).subscribe({next:()=>{const audience=this.broadcastForm.audience==='ALL'?'all buyers and sellers':this.broadcastForm.audience==='USER'?'all buyers':'all sellers';this.accountMessage=`Notification queued for ${audience}.`;this.broadcastForm.message='';this.changes.detectChanges();},error:()=>{this.accountMessage='Broadcast notification could not be sent.';this.changes.detectChanges();}});}
  loadNotifications():void{if(!this.token)return;this.http.get<Notification[]>(`${this.notificationUrl}/${encodeURIComponent(this.username)}`,{headers:this.authHeaders()}).subscribe({next:v=>{this.notifications=v;this.changes.detectChanges();}});} openInbox():void{this.showNotifications=true;this.loadNotifications();} markRead(n:Notification):void{if(n.readMessage)return;this.http.put<Notification>(`${this.notificationUrl}/${encodeURIComponent(this.username)}/${n.id}/read`,{},{headers:this.authHeaders()}).subscribe(v=>{n.readMessage=v.readMessage;this.changes.detectChanges();});} get unreadCount():number{return this.notifications.filter(n=>!n.readMessage).length;}
  addToCart(product:Product):void{this.http.post<CartResponse>(`${this.cartUrl()}/items`,{productId:product.id,quantity:1},{headers:this.requestHeaders()}).subscribe({next:c=>{this.cartItems=c.items;this.showCart=true;this.changes.detectChanges();},error:()=>{this.error='Cart service is unavailable.';this.changes.detectChanges();}});} loadCart():void{this.cartLoading=true;this.http.get<CartResponse>(this.cartUrl(),{headers:this.requestHeaders()}).subscribe({next:c=>{this.cartItems=c.items;this.cartLoading=false;this.changes.detectChanges();},error:()=>{this.cartLoading=false;this.changes.detectChanges();}});} openCart():void{this.showCart=true;this.loadCart();} removeFromCart(id:string):void{this.http.delete<CartResponse>(`${this.cartUrl()}/items/${id}`,{headers:this.requestHeaders()}).subscribe(c=>{this.cartItems=c.items;this.changes.detectChanges();});}
  cartProduct(i:CartItem):Product|undefined{return this.products.find(p=>p.id===i.productId);} get cartCount():number{return this.cartItems.reduce((t,i)=>t+i.quantity,0);} checkout():void{if(!this.token){this.showCart=false;this.showLogin=true;this.loginError='Log in or create an account to continue with this cart.';return;}this.ordering=true;this.orderError='';this.http.post<Order>(`${this.orderUrl}/${encodeURIComponent(this.username)}/checkout`,{},{headers:this.authHeaders()}).subscribe({next:o=>{this.ordering=false;this.cartItems=[];this.showCart=false;this.showOrders=true;this.orders=[o,...this.orders];this.loadProducts(this.page);this.changes.detectChanges();},error:e=>{this.ordering=false;this.orderError=e.error?.detail||e.error?.message||'Order could not be placed.';this.changes.detectChanges();}});} loadOrders():void{if(!this.token)return;this.http.get<Order[]>(`${this.orderUrl}/${encodeURIComponent(this.username)}`,{headers:this.authHeaders()}).subscribe({next:o=>{this.orders=o;this.changes.detectChanges();}});} openOrders():void{this.showOrders=true;this.loadOrders();}
  private mergeGuestCart():void{this.http.post<CartResponse>(`${this.cartBaseUrl}/${encodeURIComponent(this.username)}/merge/${this.guestCartId}`,{},{headers:this.authHeaders()}).subscribe({next:c=>{this.cartItems=c.items;localStorage.removeItem('guestCartId');this.guestCartId='';this.changes.detectChanges();},error:()=>this.loadCart()});} private cartUrl():string{return this.token?`${this.cartBaseUrl}/${encodeURIComponent(this.username)}`:`${this.cartBaseUrl}/guest/${this.guestCartId}`;} private requestHeaders():HttpHeaders{return this.token?this.authHeaders():new HttpHeaders();} private authHeaders():HttpHeaders{return new HttpHeaders({Authorization:`Bearer ${this.token}`});} private getOrCreateGuestCartId():string{const current=localStorage.getItem('guestCartId');if(current)return current;const created=crypto.randomUUID();localStorage.setItem('guestCartId',created);return created;}
  signOut():void{this.username='';this.token='';this.role='';this.showCart=this.showOrders=this.showAccount=this.showNotifications=false;this.orders=[];this.notifications=[];sessionStorage.clear();this.guestCartId=this.getOrCreateGuestCartId();this.loadCart();} previous():void{if(this.page>0)this.loadProducts(this.page-1);} next():void{if(this.page+1<this.totalPages)this.loadProducts(this.page+1);}
}
