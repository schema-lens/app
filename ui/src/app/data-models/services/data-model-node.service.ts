import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import {
  DataModelNode,
  DataModelNodeSummary,
  DataModelNodeDefinitionUpdate,
} from '../models/data-model-node.model';
import { DataModelModification } from '../models/data-model.model';
import { DataModelNodeHttpClientService } from './data-model-node-http-client.service';

@Injectable({
  providedIn: 'root',
})
export class DataModelNodeService {
  private httpClient = inject(DataModelNodeHttpClientService);

  createNode(modelId: number, node: DataModelNodeSummary): Observable<DataModelNode> {
    return this.httpClient.createNode(modelId, node);
  }

  updateNodeDefinition(modelId: number, node: DataModelNodeDefinitionUpdate): Observable<DataModelModification> {
    return this.httpClient.updateNodeDefinition(modelId, node);
  }

  deleteNode(modelId: number, nodeId: number): Observable<DataModelModification> {
    return this.httpClient.deleteNode(modelId, nodeId);
  }
}
