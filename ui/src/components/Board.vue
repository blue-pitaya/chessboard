<script lang="ts" setup>
import { Piece, pieceToImageFilename, Vec2d } from "@/util";
import TileComp from "./TileComp.vue";
import PieceComp from "./PieceComp.vue";
import { useState } from "@/state/useState";
import { computed, ref } from "vue";
import {
  updateDraggingPosition,
  onEndDragging,
  onStartDragging,
  onMouseMoveOnBoard,
} from "@/scalajs/main";

const { state, updateState } = useState();

const tiles = computed(() => state.value.tiles);
const pieces = computed(() => {
  return state.value.pieces;
});

const onPieceDragged = (payload: { piece: Piece; deltaPos: Vec2d }) => {
  updateState(updateDraggingPosition(payload.piece, payload.deltaPos));
};
const onPieceDragEnd = (payload: { piece: Piece }) => {
  updateState(onEndDragging(payload.piece));
};
const onPieceDragStart = (payload: { piece: Piece }) => {
  updateState(onStartDragging(payload.piece));
};

const onMouseMove = (e: any) => {
  if (!e?.currentTarget) return;
  const bounds = e.currentTarget.getBoundingClientRect();
  const x = e.clientX - bounds.left;
  const y = e.clientY - bounds.top;

  updateState(onMouseMoveOnBoard({ x: Math.floor(x), y: Math.floor(y) }));
};
</script>

<template>
  <svg @mousemove="onMouseMove">
    <TileComp
      v-for="t in tiles"
      :key="t.id"
      :tile="t"
      :position="t.position"
      :size="t.size"
      :color="t.color"
      :file-mark="t.fileMark"
      :rank-mark="t.rankMark"
    />
    <PieceComp
      v-for="p in pieces"
      :key="p.id"
      :position="p.position"
      :size="p.size"
      :image-filename="pieceToImageFilename(p)"
      :piece="p"
      @piece-dragged="onPieceDragged"
      @piece-drag-end="onPieceDragEnd"
      @piece-drag-start="onPieceDragStart"
    />
  </svg>
</template>

<style scoped>
svg {
  width: 800px;
  height: 800px;
}
</style>
