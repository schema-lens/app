import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AdminCreateUser, AdminUpdateUser, AdminUser } from '../models/admin-user.model';

@Injectable({ providedIn: 'root' })
export class AdminUserHttpClientService {
  private httpClient = inject(HttpClient);

  getUsers(): Observable<AdminUser[]> {
    return this.httpClient.get<AdminUser[]>('/admin/user');
  }

  createUser(user: AdminCreateUser): Observable<AdminUser> {
    return this.httpClient.post<AdminUser>('/admin/user', user);
  }

  updateUser(id: number, user: AdminUpdateUser): Observable<AdminUser> {
    return this.httpClient.put<AdminUser>(`/admin/user/${id}`, user);
  }

  deleteUser(id: number): Observable<unknown> {
    return this.httpClient.delete(`/admin/user/${id}`);
  }
}
