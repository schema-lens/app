import {
  AfterViewInit,
  ChangeDetectionStrategy,
  Component,
  computed,
  inject,
  input,
  output,
  viewChild,
} from '@angular/core';
import { DiagramHostComponent } from '../diagram-host.component';
import {
  AngularComponentShapeRenderer,
  AngularComponentShapeRendererFactory,
} from '../angular/angular-component-shape-renderer.module';
import { AngularSelectionSupportModuleFactory } from '../angular/angular-selection-support.module';
import { SchemaDiagramNodeComponent } from './node/schema-diagram-node.component';
import { SchemaDiagramNodeModule } from './node/schema-diagram-node.module';
import { NoMultiSelectModule } from '../util/no-multi-select.module';
import { KeepSelectionOnReclickModule } from '../util/keep-selection-on-reclick.module';
import { ReclickConnectModuleFactory } from './reclick-connect.module';
import { EMPTY, Observable, switchMap } from 'rxjs';
import {
  AddEdgePatch,
  AddNodePatch,
  FocusEdgePatch,
  FocusNodePatch,
  RemoveEdgePatch,
  RemoveNodePatch,
  SchemaDiagramPatch,
  UpdateEdgePatch,
  UpdateNodePatch,
} from './model/schema-diagram-patches.model';
import { takeUntilDestroyed, toObservable } from '@angular/core/rxjs-interop';
import { SchemaDiagramEdgeModule } from './edge/schema-diagram-edge.module';
import { SchemaDiagramNode } from './model/schema-diagram-node.model';
import { isNodeElement, SchemaDiagramNodeShape } from './node/schema-diagram-node.shape';
import { SchemaDiagramEdge } from './model/schema-diagram-edge.model';
import { isEdgeConnection, SchemaDiagramEdgeConnection } from './edge/schema-diagram-edge.connection';
import { DiagramLayoutService, LayoutEdge, LayoutNode } from '../diagram-layout.service';
import { Connection, ElementLike } from 'diagram-js/lib/model/Types';
import { isConnection } from 'diagram-js/lib/util/ModelUtil';
import { SchemaDiagramPositionSnapshot } from './model/schema-diagram-position-snapshot.model';
import { SchemaDiagramSelection } from './model/schema-diagram-selection.model';
import { SchemaDiagramConnectNodes } from './model/schema-diagram-connect-nodes.model';
import { AngularDisableShapeMoveWhenModuleFactory } from '../angular/angular-disable-shape-move-when.module';
import { ModuleDeclaration } from 'didi';
import SnappingModule from 'diagram-js/lib/features/snapping';

interface NodeEntry {
  readonly node: SchemaDiagramNode;
  readonly shape: SchemaDiagramNodeShape;
}

interface EdgeEntry {
  readonly edge: SchemaDiagramEdge;
  readonly connection: SchemaDiagramEdgeConnection;
}

@Component({
  selector: 'app-schema-diagram',
  template: `
    <div class="schema-diagram" [class.connect-mode]="connectMode()">
      <app-diagram-host [modules]="allModules()" (elementsChanged)="onElementsChanged()" />
    </div>
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [DiagramHostComponent],
})
export class SchemaDiagramComponent implements AfterViewInit {
  additionalModules = input<ModuleDeclaration[]>([]);
  patches$ = input<Observable<SchemaDiagramPatch>>(EMPTY);
  diagramModified = output();
  diagramHost = viewChild.required(DiagramHostComponent);

  connectMode = input<boolean>(false);
  connectNodes = output<SchemaDiagramConnectNodes>();

  private currentSelection: SchemaDiagramSelection | null = null;
  selectionChanged = output<SchemaDiagramSelection | null>();

  private isApplyingPatch = false;

  // eslint-disable-next-line @typescript-eslint/no-non-null-assertion
  private angularRenderer: AngularComponentShapeRenderer = null!; // initialized after view init
  private diagramLayoutService = inject(DiagramLayoutService);

  readonly MODULES = [
    AngularComponentShapeRendererFactory.create({ node: SchemaDiagramNodeComponent }),
    AngularSelectionSupportModuleFactory.create({
      selectionChange: (oldSelection, newSelection) => {
        if (this.connectMode() && newSelection.length > 0) {
          this.handleConnectModeSelection(oldSelection, newSelection);
        } else {
          this.updateSelectionInfo(newSelection);
          this.updateRelationshipHighlightFromSelection(oldSelection, newSelection);
        }
      },
    }),
    AngularDisableShapeMoveWhenModuleFactory.when(() => this.connectMode()),
    SchemaDiagramNodeModule,
    SchemaDiagramEdgeModule,
    NoMultiSelectModule,
    KeepSelectionOnReclickModule,
    SnappingModule,
    ReclickConnectModuleFactory.create(
      () => this.connectMode(),
      (element) => this.handleConnectModeSelection([element], [element], true),
    ),
  ];

  allModules = computed(() => [...this.MODULES, ...this.additionalModules()]);

  private nodes: Record<number, NodeEntry> = {};
  private edges: Record<number, EdgeEntry> = {};

  constructor() {
    this.dispatchPatchesFromInput();
    this.resetSelectionOnConnectModeChange();
  }

  private dispatchPatchesFromInput(): void {
    toObservable(this.patches$).pipe(
      switchMap(patches$ => patches$),
      takeUntilDestroyed(),
    ).subscribe(patch => this.dispatchPatch(patch));
  }

  private resetSelectionOnConnectModeChange(): void {
    toObservable(this.connectMode).pipe(takeUntilDestroyed()).subscribe(() => {
      const diagramHost = this.diagramHost();

      if (diagramHost.isInitialized()) {
        diagramHost.unselectAll();
      }
    });
  }

  ngAfterViewInit(): void {
    const diagramHost = this.diagramHost();

    this.angularRenderer = diagramHost.runInDiagramContext<AngularComponentShapeRenderer>(diagram =>
      diagram.get('angularComponentShapeRenderer'),
    );
  }

  private dispatchPatch(patch: SchemaDiagramPatch): void {
    this.isApplyingPatch = true;

    try {
      if (patch.type === 'node:add') {
        this.addNode(patch);
      } else if (patch.type === 'node:update') {
        this.updateNode(patch);
      } else if (patch.type === 'node:remove') {
        this.removeNode(patch);
      } else if (patch.type === 'edge:add') {
        this.addEdge(patch);
      } else if (patch.type === 'edge:update') {
        this.updateEdge(patch);
      } else if (patch.type === 'edge:remove') {
        this.removeEdge(patch);
      } else if (patch.type === 'node:focus') {
        this.focusNode(patch);
      } else if (patch.type === 'edge:focus') {
        this.focusEdge(patch);
      } else if (patch.type === 'layout:auto') {
        this.autoLayout();
      } else if (patch.type === 'diagram:clear') {
        this.clearDiagram();
      } else {
        throw new Error('Unknown patch type');
      }
    } finally {
      this.isApplyingPatch = false;
    }
  }

  onElementsChanged(): void {
    if (!this.isApplyingPatch) {
      this.diagramModified.emit();
    }
  }

  private addNode({ node, position }: AddNodePatch): void {
    if (this.nodes[node.id]) {
      throw new Error('Node already exists');
    }

    const minDimensions = SchemaDiagramNodeComponent.estimateDimensions(node);
    const x = position?.x;
    const y = position?.y;
    const width = position?.width ? Math.max(position.width, minDimensions.width) : minDimensions.width;
    const height = position?.height ? Math.max(position.height, minDimensions.height) : minDimensions.height;

    const shape = this.diagramHost().addShape({
      id: `node_${node.id}`, x, y, width, height, minDimensions,
    }) as SchemaDiagramNodeShape;
    this.angularRenderer.setShapeInput(shape, 'node', node);

    this.nodes[node.id] = { node, shape };
  }

  private updateNode({ node }: UpdateNodePatch): void {
    const entry = this.nodes[node.id];
    if (!entry) {
      throw new Error('Node does not exist');
    }

    const minDimensions = SchemaDiagramNodeComponent.estimateDimensions(node);
    const width = Math.max(entry.shape.width, minDimensions.width);
    const height = Math.max(entry.shape.height, minDimensions.height);
    const dimensionsChanged = entry.shape.width !== width || entry.shape.height !== height;

    this.diagramHost().updateAll([entry.shape], {
      [entry.shape.id]: { width, height, minDimensions },
    });

    if (dimensionsChanged) {
      this.diagramHost().recropConnections([
        ...(entry.shape.incoming ?? []),
        ...(entry.shape.outgoing ?? []),
      ]);
    }

    this.angularRenderer.setShapeInput(entry.shape, 'node', node);
    this.nodes[node.id] = { ...entry, node };
  }

  private removeNode({ nodeId }: RemoveNodePatch): void {
    const entry = this.nodes[nodeId];
    if (!entry) {
      throw new Error('Node does not exist');
    }

    this.diagramHost().removeShape(entry.shape);
    delete this.nodes[nodeId];
  }

  private addEdge({ edge, position }: AddEdgePatch): void {
    if (this.edges[edge.id]) {
      throw new Error('Edge already exists');
    }

    const fromNode = this.nodes[edge.fromNode];
    const toNode = this.nodes[edge.toNode];

    if (!fromNode || !toNode) {
      throw new Error('Edge references non-existing node');
    }

    const connection = this.diagramHost().addConnection(fromNode.shape, toNode.shape, {
      id: `edge_${edge.id}`,
      waypoints: position?.points && position.points.length > 1 ? [...position.points] : undefined,
      edge,
    });

    this.edges[edge.id] = { edge, connection: connection as SchemaDiagramEdgeConnection };
  }

  private updateEdge({ edge }: UpdateEdgePatch): void {
    const entry = this.edges[edge.id];
    if (!entry) {
      throw new Error('Edge does not exist');
    }

    this.diagramHost().updateAll([entry.connection], {
      [entry.connection.id]: { edge },
    });

    this.edges[edge.id] = { ...entry, edge };
  }

  private removeEdge({ edgeId }: RemoveEdgePatch): void {
    const entry = this.edges[edgeId];
    if (!entry) {
      throw new Error('Edge does not exist');
    }

    this.diagramHost().removeConnection(entry.connection);
    delete this.edges[edgeId];
  }

  private focusNode({ nodeId }: FocusNodePatch): void {
    const entry = this.nodes[nodeId];
    if (entry) {
      this.diagramHost().focusElement(entry.shape);
    }
  }

  private focusEdge({ edgeId }: FocusEdgePatch): void {
    const entry = this.edges[edgeId];
    if (entry) {
      this.diagramHost().focusElement(entry.connection);
    }
  }

  private autoLayout(): void {
    const layoutNodes: LayoutNode[] = Object
      .values(this.nodes)
      .map(({ shape }) => ({ id: shape.id, width: shape.width, height: shape.height }));

    const layoutEdges: LayoutEdge[] = Object.values(this.edges).map(({ connection }) =>
      ({ id: connection.id, fromId: String(connection.source?.id), toId: String(connection.target?.id) })
    );

    const layout = this.diagramLayoutService.layoutDigraph(layoutNodes, layoutEdges);

    const updatedElements = [
      ...Object.values(this.nodes).map(({ shape }) => shape),
      ...Object.values(this.edges).map(({ connection }) => connection),
    ];
    const edgeUpdates = Object.entries(layout.edges).map(([id, waypoints]) => [id, { waypoints }]);
    const updates = { ...layout.nodes, ...Object.fromEntries(edgeUpdates) };

    this.diagramHost().updateAll(updatedElements, updates);
  }

  private clearDiagram(): void {
    for (const { connection } of Object.values(this.edges)) {
      this.diagramHost().removeConnection(connection);
    }
    for (const { shape } of Object.values(this.nodes)) {
      this.diagramHost().removeShape(shape);
    }
    this.edges = {};
    this.nodes = {};
  }

  private handleConnectModeSelection(
    oldSelection: ElementLike[],
    newSelection: ElementLike[],
    allowSameNode = false,
  ): void {
    const oldNode = oldSelection.length === 1 && isNodeElement(oldSelection[0]) ? oldSelection[0] : null;
    const newNode = newSelection.length === 1 && isNodeElement(newSelection[0]) ? newSelection[0] : null;

    if (oldNode && newNode && (oldNode !== newNode || allowSameNode)) {
      const fromNode = this.nodes[+oldNode.id.substring('node_'.length)]?.node;
      const toNode = this.nodes[+newNode.id.substring('node_'.length)]?.node;

      if (fromNode && toNode) {
        this.connectNodes.emit({ from: fromNode, to: toNode });
      }
    }
  }

  private updateSelectionInfo(newSelection: ElementLike[]): void {
    const selection = this.resolveAsSelectionModel(newSelection);

    if (!this.isSelectionEqual(this.currentSelection, selection)) {
      this.currentSelection = selection;
      this.selectionChanged.emit(selection);
    }
  }

  private resolveAsSelectionModel(newSelection: ElementLike[]): SchemaDiagramSelection | null {
    if (newSelection.length !== 1) {
      return null;
    }

    if (isNodeElement(newSelection[0])) {
      return { type: 'node', node: this.nodes[+newSelection[0].id.substring('node_'.length)].node };
    }

    if (isConnection(newSelection[0]) && isEdgeConnection(newSelection[0] as Connection)) {
      return { type: 'edge', edge: (newSelection[0] as SchemaDiagramEdgeConnection).edge };
    }

    return null;
  }

  private isSelectionEqual(a: SchemaDiagramSelection | null, b: SchemaDiagramSelection | null): boolean {
    if (a === null) {
      return b === null;
    }
    if (b === null) {
      return false;
    }
    if (a.type === 'node') {
      return b.type === 'node' && a.node.id === b.node.id;
    }
    return b.type === 'edge' && a.edge.id === b.edge.id;
  }

  private updateRelationshipHighlightFromSelection(oldSelection: ElementLike[], newSelection: ElementLike[]): void {
    const resolveSelectedEdgeConnections = (selection: ElementLike[]): SchemaDiagramEdgeConnection[] =>
      selection.some(element => !isConnection(element) || !isEdgeConnection(element as Connection))
        ? []
        : (selection as SchemaDiagramEdgeConnection[]);

    const highlightUpdates: Record<number, number[]> = {};
    resolveSelectedEdgeConnections(oldSelection).forEach(({ edge }) => (highlightUpdates[edge.toNode] = []));
    resolveSelectedEdgeConnections(newSelection).forEach(({ edge }) => (highlightUpdates[edge.toNode] ??= []).push(edge.id));

    Object.entries(highlightUpdates).forEach(([nodeId, edgeIds]) =>
      this.angularRenderer.setShapeInput(this.nodes[+nodeId].shape, 'highlightEdgeIds', edgeIds),
    );
  }

  snapshotDiagramPositions(): SchemaDiagramPositionSnapshot {
    return {
      nodes: Object.fromEntries(Object.entries(this.nodes).map(([id, { shape }]) => {
        const { x, y, width, height } = shape;
        return [id, { x, y, width, height }];
      })),
      edges: Object.fromEntries(Object.entries(this.edges).map(([id, { connection }]) => {
        return [id, { points: [...connection.waypoints] }];
      })),
    };
  }
}
