<script lang="ts" setup>
import { useDraggable } from "@/draggable";
import { Piece, Vec2d } from "@/util";
import { computed, defineProps, ref, defineEmits } from "vue";

const props = defineProps<{
  position: Vec2d;
  size: Vec2d;
  imageFilename: string;
  piece: Piece;
}>();

const emit = defineEmits<{
  (e: "pieceDragged", payload: { piece: Piece; deltaPos: Vec2d }): void;
  (e: "piecDragEnd", payload: { piece: Piece });
}>();

const transformString = computed(
  () => `translate(${props.position.x}, ${props.position.y})`
);

const element = ref<SVGElement | null>(null);
useDraggable(
  element,
  {
    preventDefault: true,
    onMove: (deltaPos: Vec2d) => {
      emit("pieceDragged", { piece: props.piece, deltaPos });
    },
    onEnd: () => {
      emit("piecDragEnd", { piece: props.piece });
    },
  },
  computed(() => true)
);
</script>

<template>
  <image
    ref="element"
    :transform="transformString"
    :xlink:href="require('@/assets/' + imageFilename)"
    :width="size.x"
    :height="size.y"
  />
</template>
