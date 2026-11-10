import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { AdminCreateUser, AdminUpdateUser, AdminUser } from '../models/admin-user.model';
import { AdminUserHttpClientService } from './admin-user-http-client.service';

@Injectable({ providedIn: 'root' })
export class AdminUserService {
  private httpClient = inject(AdminUserHttpClientService);

  getUsers(): Observable<AdminUser[]> {
    return this.httpClient.getUsers();
  }

  createUser(user: AdminCreateUser): Observable<AdminUser> {
    return this.httpClient.createUser(user);
  }

  updateUser(id: number, user: AdminUpdateUser): Observable<AdminUser> {
    return this.httpClient.updateUser(id, user);
  }

  deleteUser(id: number): Observable<unknown> {
    return this.httpClient.deleteUser(id);
  }
}
