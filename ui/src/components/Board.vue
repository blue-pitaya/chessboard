<script lang="ts" setup>
import { Piece, pieceToImageFilename, Vec2d } from "@/util";
import TileComp from "./TileComp.vue";
import PieceComp from "./PieceComp.vue";
import { useState } from "@/state/useState";
import { computed } from "vue";
import { updateDraggingPosition, onEndDragging } from "@/scalajs/main";

const { state, updateState } = useState();

const tiles = computed(() => state.value.tiles);
const pieces = computed(() => state.value.pieces);

const onPieceDragged = (payload: { piece: Piece; deltaPos: Vec2d }) => {
  updateState(updateDraggingPosition(payload.piece, payload.deltaPos));
};
const onPieceDragEnd = (payload: { piece: Piece }) => {
  updateState(onEndDragging(payload.piece));
};
</script>

<template>
  <svg>
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
      @piec-drag-end="onPieceDragEnd"
    />
  </svg>
</template>

<style scoped>
svg {
  width: 800px;
  height: 800px;
}
</style>
