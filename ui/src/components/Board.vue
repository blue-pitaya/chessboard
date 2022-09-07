<script lang="ts" setup>
import { Piece, pieceToImageFilename, Vec2d, normalizeVec2d } from "@/util";
import TileComp from "./TileComp.vue";
import PieceComp from "./PieceComp.vue";
import { useState } from "@/state/useState";
import { computed, ref } from "vue";
import {
  updateDraggingPosition,
  onEndDragging,
  onStartDragging,
} from "@/scalajs/main";

const { state, updateState } = useState();

const tiles = computed(() => state.value.tiles);
const pieces = computed(() => {
  return state.value.pieces;
});

const boardContainer = ref<SVGElement>();
const getPointerBoardPosition = (absolutePointerPos: Vec2d) => {
  const bounds = boardContainer.value.getBoundingClientRect();
  const x = absolutePointerPos.x - bounds.left;
  const y = absolutePointerPos.y - bounds.top;

  return normalizeVec2d({ x, y });
};

const onPieceDragged = (payload: { piece: Piece; deltaPos: Vec2d }) => {
  updateState(updateDraggingPosition(payload.piece, payload.deltaPos));
};
const onPieceDragEnd = (payload: { piece: Piece; pointerPos: Vec2d }) => {
  updateState(
    onEndDragging(payload.piece, getPointerBoardPosition(payload.pointerPos))
  );
};
const onPieceDragStart = (payload: { piece: Piece }) => {
  updateState(onStartDragging(payload.piece));
};
</script>

<template>
  <svg ref="boardContainer">
    <TileComp
      v-for="t in tiles"
      :key="t.id"
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
