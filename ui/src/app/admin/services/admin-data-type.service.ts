import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { AdminDataType, AdminDataTypeInput } from '../models/admin-data-type.model';
import { AdminDataTypeHttpClientService } from './admin-data-type-http-client.service';

@Injectable({ providedIn: 'root' })
export class AdminDataTypeService {
  private httpClient = inject(AdminDataTypeHttpClientService);

  getDataTypes(): Observable<AdminDataType[]> {
    return this.httpClient.getDataTypes();
  }

  createDataType(type: AdminDataTypeInput): Observable<AdminDataType> {
    return this.httpClient.createDataType(type);
  }

  updateDataType(type: AdminDataType): Observable<AdminDataType> {
    const { name, parameters, mappings } = type;
    return this.httpClient.updateDataType(type.typeId, { name, parameters, mappings });
  }

  deleteDataType(typeId: number): Observable<unknown> {
    return this.httpClient.deleteDataType(typeId);
  }
}
