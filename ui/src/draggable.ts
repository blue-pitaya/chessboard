import { computed, ref } from "vue-demi";
import { MaybeComputedRef, noop } from "@vueuse/shared";
import { isClient, resolveUnref } from "@vueuse/shared";
import { useEventListener } from "@vueuse/core";
import type { PointerType, Position } from "@vueuse/core";
import { defaultWindow } from "@vueuse/core";
import { Vec2d } from "@/util";
import { ComputedRef, watchEffect } from "vue";

export interface UseDraggableOptions {
  /**
   * Only start the dragging when click on the element directly
   *
   * @default false
   */
  exact?: MaybeComputedRef<boolean>;

  /**
   * Prevent events defaults
   *
   * @default false
   */
  preventDefault?: MaybeComputedRef<boolean>;

  /**
   * Prevent events propagation
   *
   * @default false
   */
  stopPropagation?: MaybeComputedRef<boolean>;

  /**
   * Element to attach `pointermove` and `pointerup` events to.
   *
   * @default window
   */
  draggingElement?: MaybeComputedRef<
    HTMLElement | SVGElement | Window | Document | null | undefined
  >;

  /**
   * Pointer types that listen to.
   *
   * @default ['mouse', 'touch', 'pen']
   */
  pointerTypes?: PointerType[];

  /**
   * Initial position of the element.
   *
   * @default { x: 0, y: 0}
   */
  initialValue?: MaybeComputedRef<Position>;

  /**
   * Callback when the dragging starts. Return `false` to prevent dragging.
   */
  onStart?: (position: Position, event: PointerEvent) => void | false;

  /**
   * Callback during dragging.
   */
  onMove?: (position: Position, event: PointerEvent) => void;

  /**
   * Callback when dragging end.
   */
  onEnd?: (position: Position, event: PointerEvent) => void;
}

/**
 * Make elements draggable.
 *
 * @see https://vueuse.org/useDraggable
 * @param target
 * @param options
 */
export function useDraggable(
  target: MaybeComputedRef<HTMLElement | SVGElement | null | undefined>,
  options: UseDraggableOptions = {},
  isEnabled: ComputedRef<boolean>
) {
  const draggingElement = options.draggingElement ?? defaultWindow;
  const position = ref<Vec2d>({ x: 0, y: 0 });
  const pressedDelta = ref<Vec2d>();

  const filterEvent = (e: PointerEvent) => {
    if (options.pointerTypes)
      return options.pointerTypes.includes(e.pointerType as PointerType);
    return true;
  };

  const handleEvent = (e: PointerEvent) => {
    if (resolveUnref(options.preventDefault)) e.preventDefault();
    if (resolveUnref(options.stopPropagation)) e.stopPropagation();
  };

  const start = (e: PointerEvent) => {
    if (!filterEvent(e)) return;
    if (resolveUnref(options.exact) && e.target !== resolveUnref(target))
      return;
    position.value = { x: 0, y: 0 };
    pressedDelta.value = {
      x: e.pageX,
      y: e.pageY,
    };
    handleEvent(e);
  };
  const move = (e: PointerEvent) => {
    if (!filterEvent(e)) return;
    if (!pressedDelta.value) return;
    position.value = {
      x: e.pageX - pressedDelta.value.x,
      y: e.pageY - pressedDelta.value.y,
    };
    options.onMove?.(position.value, e);
    handleEvent(e);
  };
  const end = (e: PointerEvent) => {
    if (!filterEvent(e)) return;
    if (!pressedDelta.value) return;
    pressedDelta.value = undefined;
    options.onEnd?.(position.value, e);
    handleEvent(e);
  };

  const clearListeners = ref(noop);

  if (isClient) {
    watchEffect(() => {
      clearListeners.value();

      if (isEnabled.value) {
        const stopListener1 = useEventListener(
          target,
          "pointerdown",
          start,
          true
        );
        const stopListener2 = useEventListener(
          draggingElement,
          "pointermove",
          move,
          true
        );
        const stopListener3 = useEventListener(
          draggingElement,
          "pointerup",
          end,
          true
        );

        clearListeners.value = () => {
          stopListener1();
          stopListener2();
          stopListener3();
        };
      }
    });
  }

  return {
    position,
    isDragging: computed(() => !!pressedDelta.value),
  };
}

export type UseDraggableReturn = ReturnType<typeof useDraggable>;
