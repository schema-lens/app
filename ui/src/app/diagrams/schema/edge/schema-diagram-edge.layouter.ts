import BaseLayouter, { LayoutConnectionHints } from 'diagram-js/lib/layout/BaseLayouter';
import { Connection } from 'diagram-js/lib/model/Types';
import { Point } from 'diagram-js/lib/util/Types';
import { getMid } from 'diagram-js/lib/layout/LayoutUtil';
import { isEdgeConnection } from './schema-diagram-edge.connection';
import { isNodeElement, SchemaDiagramNodeShape } from '../node/schema-diagram-node.shape';

const SELF_CONNECTION_LOOP_SIZE = 50;

export class SchemaDiagramEdgeLayouter extends BaseLayouter {

  override layoutConnection(connection: Connection, hints?: LayoutConnectionHints): Point[] {
    if (!isEdgeConnection(connection)) {
      return super.layoutConnection(connection, hints);
    }

    if (!connection.waypoints || connection.waypoints.length === 0) {
      const source = hints?.source ?? connection.source;
      const target = hints?.target ?? connection.target;

      if (source && source === target && isNodeElement(source)) {
        return this.layoutSelfConnection(source);
      }
    }

    const start: Point = hints?.connectionStart || (connection.waypoints?.[0] ?? getMid(hints?.source ?? connection.source));
    const end: Point = hints?.connectionEnd || (connection.waypoints?.[connection.waypoints.length - 1] ?? getMid(hints?.source ?? connection.target));

    return [start, ...(connection.waypoints?.slice(1, connection.waypoints.length - 1) ?? []), end];
  }

  private layoutSelfConnection(shape: SchemaDiagramNodeShape): Point[] {
    const { width: w, height: h } = shape;
    const mid = getMid(shape);
    const loopSize = Math.min(SELF_CONNECTION_LOOP_SIZE, Math.max(w, h) * 0.5);

    return [
      mid,
      { x: mid.x - w * 0.2, y: mid.y - h / 2 - loopSize },
      { x: mid.x + w * 0.2, y: mid.y - h / 2 - loopSize },
      mid,
    ];
  }
}
