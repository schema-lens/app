import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AdminDataType, AdminDataTypeInput } from '../models/admin-data-type.model';

@Injectable({ providedIn: 'root' })
export class AdminDataTypeHttpClientService {
  private httpClient = inject(HttpClient);

  getDataTypes(): Observable<AdminDataType[]> {
    return this.httpClient.get<AdminDataType[]>('/admin/dataType');
  }

  createDataType(type: AdminDataTypeInput): Observable<AdminDataType> {
    return this.httpClient.post<AdminDataType>('/admin/dataType', type);
  }

  updateDataType(typeId: number, type: AdminDataTypeInput): Observable<AdminDataType> {
    return this.httpClient.put<AdminDataType>(`/admin/dataType/${typeId}`, type);
  }

  deleteDataType(typeId: number): Observable<unknown> {
    return this.httpClient.delete(`/admin/dataType/${typeId}`);
  }
}
