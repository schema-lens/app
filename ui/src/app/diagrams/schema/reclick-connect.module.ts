import type EventBus from 'diagram-js/lib/core/EventBus';
import type Selection from 'diagram-js/lib/features/selection/Selection';
import type Canvas from 'diagram-js/lib/core/Canvas';
import type { ElementLike } from 'diagram-js/lib/model/Types';
import type { ModuleDeclaration } from 'didi';
import { hasSecondaryModifier, isPrimaryButton } from 'diagram-js/lib/util/Mouse';
import type { NgZone } from '@angular/core';

const HIGH_PRIORITY = 2000;

interface ElementClickEvent extends MouseEvent {
  element: ElementLike;
}

export type ReclickConnectPredicate = () => boolean;
export type ReclickConnectCallback = (element: ElementLike) => void;

export class ReclickConnectHandler {
  static readonly $inject = ['eventBus', 'selection', 'canvas', 'ngZone', 'reclickConnectPredicate', 'reclickConnectCallback'];

  constructor(
    eventBus: EventBus,
    selection: Selection,
    canvas: Canvas,
    ngZone: NgZone,
    predicate: ReclickConnectPredicate,
    callback: ReclickConnectCallback,
  ) {
    eventBus.on('element.click', HIGH_PRIORITY, (event: ElementClickEvent) => {
      if (!predicate() || !isPrimaryButton(event) || hasSecondaryModifier(event)) {
        return;
      }

      const element = event.element;
      if (element === canvas.getRootElement()) {
        return;
      }

      const selected = selection.get();
      if (selected.length === 1 && selected[0] === element) {
        ngZone.run(() => callback(element));
      }
    });
  }
}

export class ReclickConnectModuleFactory {
  static create(predicate: ReclickConnectPredicate, callback: ReclickConnectCallback): ModuleDeclaration {
    return {
      __init__: ['reclickConnect'],
      reclickConnect: ['type', ReclickConnectHandler],
      reclickConnectPredicate: ['value', predicate],
      reclickConnectCallback: ['value', callback],
    };
  }
}
