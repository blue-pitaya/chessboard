<script lang="ts" setup>
import { Tile, Vec2d } from "@/util";
import { computed, defineProps } from "vue";

const props = defineProps<{
  tile: Tile;
  position: Vec2d;
  size: Vec2d;
  color: string;
  fileMark?: string;
  rankMark?: string;
}>();

const transformString = computed(
  () => `translate(${props.position.x}, ${props.position.y})`
);

const fillColor = computed(() =>
  props.tile.isHighlighted ? "purple" : props.color
);
</script>

<template>
  <rect
    :transform="transformString"
    :width="size.x"
    :height="size.y"
    :fill="fillColor"
  />
  <foreignObject :transform="transformString" :width="size.x" :height="size.y">
    <div class="markContainer">
      <span>{{ rankMark }}</span>
      <span />
      <span />
      <span />
      <span />
      <span />
      <span />
      <span />
      <span>{{ fileMark }}</span>
    </div>
  </foreignObject>
</template>

<style scoped>
.markContainer {
  width: 100%;
  height: 100%;
  color: black;
  display: grid;
  grid-template-columns: auto 1fr auto;
  grid-template-rows: auto 1fr auto;
}

.markContainer span {
  pointer-events: none;
  user-select: none;
  margin: 5px;
}
</style>
